package arcana.worldgen.underground;

import com.mojang.serialization.MapCodec;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.gen.feature.FeaturePlacementContext;
import net.minecraft.world.gen.placementmodifier.AbstractConditionalPlacementModifier;
import net.minecraft.world.gen.placementmodifier.PlacementModifierType;

public class ExposedPlacementModifier extends AbstractConditionalPlacementModifier{
	
	private static final MapCodec<ExposedPlacementModifier> CODEC = MapCodec.unit(new ExposedPlacementModifier());
	public static PlacementModifierType<ExposedPlacementModifier> TYPE = () -> CODEC;
	
	protected boolean shouldPlace(FeaturePlacementContext context, Random random, BlockPos pos){
		for(Direction d : Direction.values())
			if(context.getBlockState(pos.offset(d)).isAir())
				return true;
		return false;
	}
	
	public PlacementModifierType<?> getType(){
		return TYPE;
	}
}