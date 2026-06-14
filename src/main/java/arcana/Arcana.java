package arcana;

import arcana.aspects.ItemAspectRegistry;
import arcana.aura.Taint;
import arcana.blocks.WardedCampfireBlock;
import arcana.commands.ArcanaCommands;
import arcana.effects.AspectPowerStatusEffect;
import arcana.effects.SetBonusStatusEffect;
import arcana.enchantments.ArcanaEnchantmentComponents;
import arcana.enchantments.LootSwapEffect;
import arcana.items.CrimsonLeechItem;
import arcana.recipes.alchemy.AlchemyRecipe;
import arcana.recipes.arcane_crafting.ShapedArcaneCraftingRecipe;
import arcana.recipes.crafting.VoidPuttyRepairRecipe;
import arcana.recipes.crafting.WandRecipe;
import arcana.recipes.infusion.InfusionEnchantmentRecipe;
import arcana.recipes.infusion.SimpleInfusionRecipe;
import arcana.research.BuiltinResearch;
import arcana.research.Research;
import arcana.research.ResearchLoader;
import arcana.util.RegistryMappingLoader;
import arcana.warp.WarpEvents;
import arcana.worldgen.ArcanaFeatures;
import com.unascribed.lib39.dessicant.api.DessicantControl;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.entity.event.v1.ServerEntityCombatEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.block.DispenserBlock;
import net.minecraft.block.dispenser.ProjectileDispenserBehavior;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.resource.ResourceType;
import net.minecraft.util.Identifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class Arcana implements ModInitializer{
	
	public static final String MODID = "arcana";
	public static final Logger LOGGER = LoggerFactory.getLogger(MODID);
	public static final ArcanaConfig CONFIG = ArcanaConfig.createToml(FabricLoader.getInstance().getConfigDir(), "", MODID, ArcanaConfig.class);
	
	@Override
	public void onInitialize(){
		LOGGER.info("Loading Arcana");
		
		// for dessicant tips
		DessicantControl.optIn(MODID);
		
		ArcanaSounds.setup();
		ArcanaEnchantmentComponents.setup();
		ArcanaRegistry.setup();
		
		Registry.register(Registries.RECIPE_SERIALIZER, arcId("wand"), WandRecipe.SERIALIZER);
		Registry.register(Registries.RECIPE_SERIALIZER, arcId("void_putty_repair"), VoidPuttyRepairRecipe.SERIALIZER);
		
		ShapedArcaneCraftingRecipe.setup();
		AlchemyRecipe.setup();
		SimpleInfusionRecipe.setup();
		InfusionEnchantmentRecipe.setup();
		Research.setup();
		WarpEvents.setup();
		
		ArcanaFeatures.addToWorldgen();
		
		ResourceManagerHelper serverResources = ResourceManagerHelper.get(ResourceType.SERVER_DATA);
		serverResources.registerReloadListener(arcId("aspects"), ItemAspectRegistry::new);
		serverResources.registerReloadListener(new ResearchLoader());
		serverResources.registerReloadListener(new RegistryMappingLoader<>("taint_maps", Taint.TAINT_MAP));
		serverResources.registerReloadListener(new RegistryMappingLoader<>("untaint_maps", Taint.UNTAINT_MAP));
		serverResources.registerReloadListener(new RegistryMappingLoader<>("purifying_maps", LootSwapEffect.PURIFYING_MAP));
		serverResources.registerReloadListener(new RegistryMappingLoader<>("transmutative_maps", LootSwapEffect.TRANSMUTATIVE_MAP));
		
		ArcanaCommands.register();
		
		ServerTickEvents.END_WORLD_TICK.register(world -> world.getPlayers().forEach(BuiltinResearch::checkTick));
		ServerTickEvents.END_WORLD_TICK.register(world -> world.getPlayers().forEach(SetBonusStatusEffect::handleArmourSetBonus));
		ServerTickEvents.END_WORLD_TICK.register(world -> world.getPlayers().forEach(AspectPowerStatusEffect::handleExclusivity));
		ServerTickEvents.END_WORLD_TICK.register(WardedCampfireBlock::handleTime);
		ServerEntityCombatEvents.AFTER_KILLED_OTHER_ENTITY.register(CrimsonLeechItem::handleEntityDeath);
		
		DispenserBlock.registerBehavior(ArcanaRegistry.TAINT_IN_A_BOTTLE, new ProjectileDispenserBehavior(ArcanaRegistry.TAINT_IN_A_BOTTLE));
	}
	
	public static Identifier arcId(String s){
		return Identifier.of(MODID, s);
	}
	
	// resolves non-namespaced IDs in the arcana namespace, otherwise uses the given namespace
	public static Identifier maybeArcId(String s){
		if(s.contains(":"))
			return Identifier.of(s);
		else
			return arcId(s);
	}
}