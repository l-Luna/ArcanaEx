package arcana.items.foci.follow_me;

import arcana.mixin.accessor.JigsawStructureAccessor;
import arcana.util.InertRegistryCodecs;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.entry.RegistryEntryList;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Pair;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.random.CheckedRandom;
import net.minecraft.util.math.random.ChunkRandom;
import net.minecraft.world.Heightmap;
import net.minecraft.world.gen.HeightContext;
import net.minecraft.world.gen.chunk.ChunkGenerator;
import net.minecraft.world.gen.structure.JigsawStructure;
import net.minecraft.world.gen.structure.Structure;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

public record StructureDestination(RegistryEntryList<Structure> structure) implements Destination{
	
	public static final MapCodec<StructureDestination> CODEC = RecordCodecBuilder.mapCodec(i -> i.group(
			InertRegistryCodecs.create(RegistryKeys.STRUCTURE).fieldOf("structure").forGetter(x -> x.structure)
	).apply(i, StructureDestination::new));
	
	public MapCodec<? extends Destination> getCodec(){
		return CODEC;
	}
	
	public net.minecraft.util.@Nullable Pair<BlockPos, Optional<Heightmap.Type>> getLocation(ItemStack input, ServerWorld world, BlockPos origin, PlayerEntity caster){
		ChunkGenerator gen = world.getChunkManager().getChunkGenerator();
		var located = gen.locateStructure(world, structure, origin, 1000, false);
		if(located == null)
			return null;
		int yOffset = 0;
		Structure structureType = located.getSecond().value();
		Optional<Heightmap.Type> heightmap = Optional.empty();
		if(structureType instanceof JigsawStructure js){
			JigsawStructureAccessor acc = (JigsawStructureAccessor)(Object)js;
			heightmap = acc.arcana$getProjectStartToHeightmap();
			if(acc.arcana$getProjectStartToHeightmap().isEmpty())
				yOffset = acc.arcana$getStartHeight().get(createChunkRandom(world, located.getFirst()), new HeightContext(gen, world));
		}
		return new Pair<>(located.getFirst().add(0, yOffset, 0), heightmap);
	}
	
	private static ChunkRandom createChunkRandom(ServerWorld world, BlockPos pos){
		ChunkPos chunkPos = new ChunkPos(pos);
		ChunkRandom chunkRandom = new ChunkRandom(new CheckedRandom(0));
		chunkRandom.setCarverSeed(world.getSeed(), chunkPos.x, chunkPos.z);
		return chunkRandom;
	}
}