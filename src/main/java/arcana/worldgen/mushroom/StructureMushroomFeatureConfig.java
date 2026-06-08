package arcana.worldgen.mushroom;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.intprovider.IntProvider;
import net.minecraft.world.gen.feature.FeatureConfig;
import net.minecraft.world.gen.stateprovider.BlockStateProvider;

public record StructureMushroomFeatureConfig(
		BlockStateProvider stemProvider,
		Identifier capStructureId,
		IntProvider stemHeight
) implements FeatureConfig{
	
	public static final Codec<StructureMushroomFeatureConfig> CODEC = RecordCodecBuilder.create(
			i -> i.group(
					BlockStateProvider.TYPE_CODEC.fieldOf("stem_provider").forGetter(x -> x.stemProvider),
					Identifier.CODEC.fieldOf("cap_structure_id").forGetter(x -> x.capStructureId),
					IntProvider.POSITIVE_CODEC.fieldOf("stem_height").forGetter(x -> x.stemHeight)
			).apply(i, StructureMushroomFeatureConfig::new)
	);
}