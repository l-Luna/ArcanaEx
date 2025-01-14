package arcana.entities.crimson;

import arcana.ArcanaRegistry;
import arcana.components.Researcher;
import arcana.research.BuiltinResearch;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.player.PlayerEntity;

import java.util.Set;

public class CrimsonSpawns{

	public static Set<EntityType<?>> spawnsFor(PlayerEntity player){
		Researcher researcher = Researcher.from(player);
		if(researcher.isPuzzleComplete(BuiltinResearch.bossMilestonePuzzle))
			return Set.of(ArcanaRegistry.CRIMSON_MISSIONARY, ArcanaRegistry.CRIMSON_HEAVY_KNIGHT);
		if(researcher.isPuzzleComplete(BuiltinResearch.infusionMilestonePuzzle))
			return Set.of(ArcanaRegistry.CRIMSON_PROTECTOR, ArcanaRegistry.CRIMSON_MISSIONARY);
		if(researcher.isPuzzleComplete(BuiltinResearch.wandMilestonePuzzle))
			return Set.of(ArcanaRegistry.CRIMSON_ARCHER, ArcanaRegistry.CRIMSON_PROTECTOR);
		return Set.of(ArcanaRegistry.CRIMSON_KNIGHT, ArcanaRegistry.CRIMSON_ARCHER);
	}
}