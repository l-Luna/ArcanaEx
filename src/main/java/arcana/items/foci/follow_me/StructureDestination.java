package arcana.items.foci.follow_me;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.gen.structure.Structure;
import org.jetbrains.annotations.Nullable;

public record StructureDestination(TagKey<Structure> structureTag) implements Destination{
	
	public static final MapCodec<StructureDestination> CODEC = RecordCodecBuilder.mapCodec(i -> i.group(
			TagKey.codec(RegistryKeys.STRUCTURE).fieldOf("structure_tag").forGetter(x -> x.structureTag)
	).apply(i, StructureDestination::new));
	
	
	public MapCodec<? extends Destination> getCodec(){
		return CODEC;
	}
	
	public @Nullable BlockPos getLocation(ItemStack input, ServerWorld world, BlockPos origin){
		return world.locateStructure(structureTag, origin, 100, false);
	}
	
	public boolean isDynamic(){
		return false;
	}
}