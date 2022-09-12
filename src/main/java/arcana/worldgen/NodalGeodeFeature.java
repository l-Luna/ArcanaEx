package arcana.worldgen;

import arcana.aspects.Aspect;
import com.mojang.serialization.Codec;
import net.minecraft.world.gen.feature.GeodeFeature;
import net.minecraft.world.gen.feature.GeodeFeatureConfig;
import net.minecraft.world.gen.feature.util.FeatureContext;

public class NodalGeodeFeature extends GeodeFeature{
	
	// who needs NodalGeodeFeatureConfig anyways?
	private final Aspect primary;
	
	public NodalGeodeFeature(Codec<GeodeFeatureConfig> codec, Aspect primary){
		super(codec);
		this.primary = primary;
	}
	
	public boolean generate(FeatureContext<GeodeFeatureConfig> context){
		if(super.generate(context)){
			context.getWorld();
			return true;
		}
		return false;
	}
}