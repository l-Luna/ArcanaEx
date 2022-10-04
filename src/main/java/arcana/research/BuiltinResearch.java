package arcana.research;

import arcana.ArcanaRegistry;
import arcana.ArcanaTags;
import arcana.components.AuraWorld;
import arcana.components.Researcher;
import arcana.nodes.Node;
import arcana.nodes.NodeTypes;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Box;

import static arcana.Arcana.arcId;

public final class BuiltinResearch{

	public static final Identifier fluxPuzzle = arcId("flux_build_research");
	
	public static final Identifier silverwoodResearch = arcId("silverwood_trees");
	public static final Identifier greatwoodResearch = arcId("greatwood_trees");
	public static final Identifier primordialPearlResearch = arcId("primordial_pearl");
	public static final Identifier hungryNodesResearch = arcId("hungry_nodes");
	public static final Identifier eldritchNodesResearch = arcId("eldritch_nodes");
	public static final Identifier nodalGeodesResearch = arcId("nodal_geodes");
	
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
	}
	
	public static void finishInfoEntry(PlayerEntity player, Identifier entryAndPuzzle){
		// puzzle prevents modified clients from continuing early; force-completing avoids tagging these as root
		var researcher = Researcher.from(player);
		researcher.completePuzzle(Research.getPuzzle(entryAndPuzzle));
		researcher.completeEntry(Research.getEntry(entryAndPuzzle));
		researcher.doSync();
	}
}