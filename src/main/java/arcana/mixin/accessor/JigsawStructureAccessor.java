package arcana.mixin.accessor;

import net.minecraft.world.Heightmap;
import net.minecraft.world.gen.heightprovider.HeightProvider;
import net.minecraft.world.gen.structure.JigsawStructure;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.Optional;

@Mixin(JigsawStructure.class)
public interface JigsawStructureAccessor{

	@Accessor("startHeight")
	HeightProvider arcana$getStartHeight();
	
	@Accessor("projectStartToHeightmap")
	Optional<Heightmap.Type> arcana$getProjectStartToHeightmap();
}