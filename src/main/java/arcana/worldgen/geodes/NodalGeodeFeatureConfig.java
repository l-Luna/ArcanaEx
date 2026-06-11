package arcana.worldgen.geodes;

import arcana.aspects.Aspect;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.world.gen.feature.FeatureConfig;
import net.minecraft.world.gen.feature.GeodeFeatureConfig;

public class NodalGeodeFeatureConfig implements FeatureConfig{

	public static final Codec<NodalGeodeFeatureConfig> CODEC = RecordCodecBuilder.create(
			i -> i.group(
					GeodeFeatureConfig.CODEC.fieldOf("geode_config").forGetter(x -> x.geodeConfig),
					Aspect.CODEC.fieldOf("primary_aspect").forGetter(x -> x.primaryAspect)
			).apply(i, NodalGeodeFeatureConfig::new)
	);
	
	public final GeodeFeatureConfig geodeConfig;
	public final Aspect primaryAspect;
	
	public NodalGeodeFeatureConfig(GeodeFeatureConfig config, Aspect primaryAspect){
		geodeConfig = config;
		this.primaryAspect = primaryAspect;
	}
}