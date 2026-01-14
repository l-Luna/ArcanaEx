package arcana;

import arcana.aspects.ItemAspectRegistry;
import arcana.aura.TaintMapLoader;
import arcana.blocks.WardedCampfireBlock;
import arcana.commands.ArcanaCommands;
import arcana.effects.AspectPowerStatusEffect;
import arcana.effects.SetBonusStatusEffect;
import arcana.enchantments.LootSwapEnchantment;
import arcana.entities.ThrownTaintBottleEntity;
import arcana.items.CrimsonLeechItem;
import arcana.recipes.*;
import arcana.research.BuiltinResearch;
import arcana.research.Research;
import arcana.research.ResearchLoader;
import arcana.warp.WarpEvents;
import arcana.worldgen.HangingNodeFeature;
import arcana.worldgen.SurfaceNodeFeature;
import arcana.worldgen.geodes.NodalGeodes;
import arcana.worldgen.greatwood.GreatwoodTree;
import arcana.worldgen.silverwood.SilverwoodTree;
import com.unascribed.lib39.dessicant.api.DessicantControl;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.entity.event.v1.ServerEntityCombatEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.CommonLifecycleEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.loot.v2.LootTableEvents;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.minecraft.block.DispenserBlock;
import net.minecraft.block.dispenser.ProjectileDispenserBehavior;
import net.minecraft.entity.projectile.ProjectileEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.resource.ResourceType;
import net.minecraft.util.Identifier;
import net.minecraft.util.Util;
import net.minecraft.util.math.Position;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.World;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class Arcana implements ModInitializer{
	
	public static final String modid = "arcana";
	public static final Logger logger = LoggerFactory.getLogger(modid);
	
	public static final ItemAspectRegistry aspectRegistry = new ItemAspectRegistry();
	public static final ResearchLoader researchLoader = new ResearchLoader();
	public static final TaintMapLoader taintMapLoader = new TaintMapLoader();
	
	@Override
	public void onInitialize(){
		logger.info("Loading Arcana");
		
		// for dessicant tips
		DessicantControl.optIn(modid);
		
		ArcanaSounds.setup();
		ArcanaRegistry.setup();
		
		Registry.register(Registry.RECIPE_SERIALIZER, arcId("wand"), WandRecipe.SERIALIZER);
		Registry.register(Registry.RECIPE_SERIALIZER, arcId("void_putty_repair"), VoidPuttyRepairRecipe.SERIALIZER);
		
		ShapedArcaneCraftingRecipe.setup();
		AlchemyRecipe.setup();
		InfusionRecipe.setup();
		Research.setup();
		WarpEvents.setup();
		
		SurfaceNodeFeature.addToWorldgen();
		NodalGeodes.addToWorldgen();
		HangingNodeFeature.addToWorldgen();
		SilverwoodTree.addToWorldgen();
		GreatwoodTree.addToWorldgen();
		
		ResourceManagerHelper.get(ResourceType.SERVER_DATA).registerReloadListener(aspectRegistry);
		ResourceManagerHelper.get(ResourceType.SERVER_DATA).registerReloadListener(researchLoader);
		ResourceManagerHelper.get(ResourceType.SERVER_DATA).registerReloadListener(taintMapLoader);
		
		CommonLifecycleEvents.TAGS_LOADED.register((registries, client) -> aspectRegistry.applyAssociations());
		
		ArcanaCommands.register();
		
		ServerTickEvents.END_WORLD_TICK.register(world -> world.getPlayers().forEach(BuiltinResearch::checkTick));
		ServerTickEvents.END_WORLD_TICK.register(world -> world.getPlayers().forEach(SetBonusStatusEffect::handleArmourSetBonus));
		ServerTickEvents.END_WORLD_TICK.register(world -> world.getPlayers().forEach(AspectPowerStatusEffect::handleExclusivity));
		ServerTickEvents.END_WORLD_TICK.register(WardedCampfireBlock::handleTime);
		ServerEntityCombatEvents.AFTER_KILLED_OTHER_ENTITY.register(CrimsonLeechItem::handleEntityDeath);
		
		LootTableEvents.MODIFY.register(LootSwapEnchantment::modifyLootTable);
		
		DispenserBlock.registerBehavior(ArcanaRegistry.TAINT_IN_A_BOTTLE, new ProjectileDispenserBehavior(){
			protected ProjectileEntity createProjectile(World world, Position position, ItemStack stack){
				return Util.make(new ThrownTaintBottleEntity(position.getX(), position.getY(), position.getZ(), world),
						entity -> entity.setItem(stack));
			}
		});
	}
	
	public static Identifier arcId(String s){
		return new Identifier(modid, s);
	}
	
	// resolves non-namespaced IDs in the arcana namespace, otherwise uses the given namespace
	public static Identifier maybeArcId(String s){
		if(s.contains(":"))
			return new Identifier(s);
		else
			return arcId(s);
	}
}