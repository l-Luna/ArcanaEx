package arcana.research;

import arcana.ArcanaRegistry;
import arcana.ArcanaTags;
import arcana.aura.AuraChunk;
import arcana.aura.AuraWorld;
import arcana.aura.Node;
import arcana.aura.NodeTypes;
import arcana.components.Researcher;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Box;

import java.util.List;

import static arcana.Arcana.arcId;

public final class BuiltinResearch{
	
	public static final Identifier rootResearch = arcId("root");
	
	public static final Identifier fluxResearch = arcId("flux");
	
	public static final Identifier fluxPuzzle = arcId("flux_build_research");
	public static final Identifier highestReachPuzzle = arcId("highest_reach");
	public static final Identifier lowestDepthsPuzzle = arcId("lowest_depths");
	public static final Identifier distilleryPathfinderPuzzle = arcId("distillery_pathfinder");
	public static final Identifier drinkableTaintPuzzle = arcId("crimson_initiation");
	
	public static final Identifier silverwoodResearch = arcId("silverwood_trees");
	public static final Identifier greatwoodResearch = arcId("greatwood_trees");
	public static final Identifier primordialPearlResearch = arcId("primordial_pearl");
	public static final Identifier hungryNodesResearch = arcId("hungry_nodes");
	public static final Identifier eldritchNodesResearch = arcId("eldritch_nodes");
	public static final Identifier nodalGeodesResearch = arcId("nodal_geodes");
	
	public static final Identifier researchExpertiseResearch = arcId("research_expertise");
	public static final Identifier researchMasteryResearch = arcId("research_mastery");
	
	public static final Identifier arcaniumSetBonusAddendum = arcId("magical_metallurgy/set_bonus");
	
	public static final List<Identifier> infoResearch = List.of(
			silverwoodResearch,
			greatwoodResearch,
			primordialPearlResearch,
			hungryNodesResearch,
			eldritchNodesResearch,
			nodalGeodesResearch,
			fluxResearch
	);
	
	public static void checkInventory(PlayerEntity player){
		if(player.getInventory().contains(ArcanaTags.SILVERWOOD_LOGS))
			finishInfoEntry(player, silverwoodResearch);
		if(player.getInventory().contains(ArcanaTags.GREATWOOD_LOGS))
			finishInfoEntry(player, greatwoodResearch);
		if(player.getInventory().contains(ArcanaRegistry.PRIMORDIAL_PEARL.getDefaultStack()))
			finishInfoEntry(player, primordialPearlResearch);
	}
	
	public static void checkTick(PlayerEntity player){
		AuraWorld aura = AuraWorld.from(player.world);
		Box nodeBox = new Box(player.getPos().add(6, 6, 6), player.getPos().subtract(6, 6, 6));
		for(Node node : aura.getNodesInBounds(nodeBox)){
			if(node.getType() == NodeTypes.ELDRITCH)
				finishInfoEntry(player, eldritchNodesResearch);
			if(node.getType() == NodeTypes.HUNGRY)
				finishInfoEntry(player, hungryNodesResearch);
			if(node.getTag() != null && node.getTag().getBoolean("in_geode"))
				finishInfoEntry(player, nodalGeodesResearch);
		}
		
		Researcher researcher = Researcher.from(player);
		if(player.getPos().y < player.world.getBottomY() + 20){
			researcher.completePuzzle(Research.getPuzzle(lowestDepthsPuzzle));
			researcher.doSync();
		}
		if(player.getPos().y > player.world.getTopY() - 30){
			researcher.completePuzzle(Research.getPuzzle(highestReachPuzzle));
			researcher.doSync();
		}
		if(AuraChunk.at(player.world, player.getBlockPos()).getFlux() > 40){
			researcher.completePuzzle(Research.getPuzzle(fluxPuzzle));
			researcher.doSync();
		}
		
		// TODO: set bonus addenda should really be in SetBonusStatusEffect::handleArmourSetBonus
		if(player.hasStatusEffect(ArcanaRegistry.ARCANE_AURA)){
			researcher.completeAddendum(Research.getAddendum(arcaniumSetBonusAddendum));
			researcher.doSync();
		}
	}
	
	public static void finishInfoEntry(PlayerEntity player, Identifier entryAndPuzzle){
		// puzzle prevents modified clients from continuing early; force-completing avoids tagging these as root
		var researcher = Researcher.from(player);
		researcher.completePuzzle(Research.getPuzzle(entryAndPuzzle));
		researcher.completeEntry(Research.getEntry(entryAndPuzzle));
		researcher.doSync();
	}
}