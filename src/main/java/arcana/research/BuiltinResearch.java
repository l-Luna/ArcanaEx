package arcana.research;

import arcana.ArcanaRegistry;
import arcana.ArcanaTags;
import arcana.aura.AuraChunk;
import arcana.aura.AuraWorld;
import arcana.aura.Node;
import arcana.aura.NodeTypes;
import arcana.cca_components.Researcher;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Box;

import static arcana.Arcana.arcId;

public final class BuiltinResearch{
	
	public static final Identifier rootEntry = arcId("root");
	
	public static final Identifier researchTutorialEntry = arcId("research");
	public static final Identifier researchTutorialPuzzle = arcId("chemistry_intro_puzzle");
	
	public static final Identifier wandMilestonePuzzle = arcId("milestone_has_wand");
	public static final Identifier infusionMilestonePuzzle = arcId("milestone_has_infusion");
	public static final Identifier bossMilestonePuzzle = arcId("milestone_has_boss");
	
	public static final Identifier fluxPuzzle = arcId("flux_build_research");
	public static final Identifier highestReachPuzzle = arcId("highest_reach");
	public static final Identifier lowestDepthsPuzzle = arcId("lowest_depths");
	public static final Identifier distilleryPathfinderPuzzle = arcId("distillery_pathfinder");
	public static final Identifier drinkableTaintPuzzle = arcId("crimson_initiation");
	
	public static final Identifier silverwoodEntry = arcId("silverwood_trees");
	public static final Identifier greatwoodEntry = arcId("greatwood_trees");
	public static final Identifier primordialPearlEntry = arcId("primordial_pearl");
	public static final Identifier hungryNodesEntry = arcId("hungry_nodes");
	public static final Identifier eldritchNodesEntry = arcId("eldritch_nodes");
	public static final Identifier nodalGeodesEntry = arcId("nodal_geodes");
	
	public static final Identifier crimsonEquipmentEntry = arcId("crimson_equipment");
	public static final Identifier projectingAddendum = arcId("crimson_equipment/projecting");
	public static final Identifier crimsonBladeAddendum = arcId("crimson_equipment/crimson_blade");
	public static final Identifier crimsonLongbowAddendum = arcId("crimson_equipment/crimson_longbow");
	public static final Identifier crimsonLeechAddendum = arcId("crimson_equipment/crimson_leech");
	
	public static final Identifier nodeChannellingEntry = arcId("node_channelling");
	public static final Identifier nodeChannelling2Entry = arcId("node_channelling_2");
	
	public static final Identifier researchExpertiseEntry = arcId("research_expertise");
	public static final Identifier researchMasteryEntry = arcId("research_mastery");
	
	public static final Identifier arcaniumSetBonusAddendum = arcId("magical_metallurgy/set_bonus");
	
	public static void checkInventory(PlayerEntity player){
		Researcher researcher = Researcher.from(player);
		if(player.getInventory().contains(ArcanaTags.SILVERWOOD_LOGS))
			finishInfoEntry(player, silverwoodEntry);
		if(player.getInventory().contains(ArcanaTags.GREATWOOD_LOGS))
			finishInfoEntry(player, greatwoodEntry);
		if(player.getInventory().contains(ArcanaRegistry.PRIMORDIAL_PEARL.getDefaultStack()))
			finishInfoEntry(player, primordialPearlEntry);
		if(player.getInventory().containsAny(stack -> stack.isOf(ArcanaRegistry.WAND)) && !researcher.isPuzzleComplete(wandMilestonePuzzle)){
			researcher.completePuzzle(wandMilestonePuzzle);
			researcher.doSync();
		}
		if(player.getInventory().containsAny(stack -> EnchantmentHelper.hasAnyEnchantmentsIn(stack, ArcanaTags.UNLOCKS_PROJECTING_ENTRY)) && !researcher.isAddendumComplete(projectingAddendum)){
			researcher.completeEntry(Research.getEntry(crimsonEquipmentEntry));
			researcher.completeAddendum(projectingAddendum);
			researcher.doSync();
		}
		if(player.getInventory().contains(ArcanaRegistry.CRIMSON_BLADE.getDefaultStack()) && !researcher.isAddendumComplete(crimsonBladeAddendum)){
			researcher.completeEntry(Research.getEntry(crimsonEquipmentEntry));
			researcher.completeAddendum(crimsonBladeAddendum);
			researcher.doSync();
		}
		if(player.getInventory().contains(ArcanaRegistry.CRIMSON_LONGBOW.getDefaultStack()) && !researcher.isAddendumComplete(crimsonLongbowAddendum)){
			researcher.completeEntry(Research.getEntry(crimsonEquipmentEntry));
			researcher.completeAddendum(crimsonLongbowAddendum);
			researcher.doSync();
		}
		if(player.getInventory().contains(ArcanaRegistry.CRIMSON_LEECH.getDefaultStack()) && !researcher.isAddendumComplete(crimsonLeechAddendum)){
			researcher.completeEntry(Research.getEntry(crimsonEquipmentEntry));
			researcher.completeAddendum(crimsonLeechAddendum);
			researcher.doSync();
		}
	}
	
	public static void checkTick(PlayerEntity player){
		AuraWorld aura = AuraWorld.from(player.getWorld());
		Box nodeBox = new Box(player.getPos().add(6, 6, 6), player.getPos().subtract(6, 6, 6));
		for(Node node : aura.getNodesInBounds(nodeBox)){
			if(node.getType() == NodeTypes.ELDRITCH)
				finishInfoEntry(player, eldritchNodesEntry);
			if(node.getType() == NodeTypes.HUNGRY)
				finishInfoEntry(player, hungryNodesEntry);
			if(node.getTag() != null && node.getTag().getBoolean("in_geode"))
				finishInfoEntry(player, nodalGeodesEntry);
		}
		
		Researcher researcher = Researcher.from(player);
		if(player.getPos().y < player.getWorld().getBottomY() + 20 && !researcher.isPuzzleComplete(lowestDepthsPuzzle)){
			researcher.completePuzzle(lowestDepthsPuzzle);
			researcher.doSync();
		}
		if(player.getPos().y > player.getWorld().getTopY() - 30 && !researcher.isPuzzleComplete(highestReachPuzzle)){
			researcher.completePuzzle(highestReachPuzzle);
			researcher.doSync();
		}
		AuraChunk auraHere = AuraChunk.from(player.getWorld(), player.getBlockPos());
		if(auraHere != null && auraHere.flux() > 40 && !researcher.isPuzzleComplete(fluxPuzzle)){
			researcher.completePuzzle(fluxPuzzle);
			researcher.doSync();
		}
		
		// TODO: set bonus addenda should really be in SetBonusStatusEffect::handleArmourSetBonus
		if(player.hasStatusEffect(RegistryEntry.of(ArcanaRegistry.ARCANE_AURA)) && !researcher.isAddendumComplete(arcaniumSetBonusAddendum)){
			researcher.completeAddendum(arcaniumSetBonusAddendum);
			researcher.doSync();
		}
	}
	
	public static void finishInfoEntry(PlayerEntity player, Identifier entryAndPuzzle){
		// puzzle prevents modified clients from continuing early; force-completing avoids tagging these as root
		var researcher = Researcher.from(player);
		if(!researcher.isPuzzleComplete(entryAndPuzzle)){
			researcher.completePuzzle(entryAndPuzzle);
			researcher.completeEntry(Research.getEntry(entryAndPuzzle));
			researcher.doSync();
		}
	}
}