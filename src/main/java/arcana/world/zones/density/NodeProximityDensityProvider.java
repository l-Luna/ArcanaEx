package arcana.world.zones.density;

import arcana.util.Ease;
import arcana.world.AuraWorld;
import arcana.world.NodeType;
import arcana.world.zones.DensityProvider;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

import java.util.Comparator;

public record NodeProximityDensityProvider(NodeType nodeType, float maxDistance, Ease ease) implements DensityProvider{
	
	public static MapCodec<NodeProximityDensityProvider> CODEC = RecordCodecBuilder.mapCodec(i -> i.group(
			NodeType.CODEC.fieldOf("node_type").forGetter(NodeProximityDensityProvider::nodeType),
			Codec.FLOAT.fieldOf("max_distance").forGetter(NodeProximityDensityProvider::maxDistance),
			Ease.CODEC.optionalFieldOf("ease", Ease.LINEAR).forGetter(NodeProximityDensityProvider::ease)
	).apply(i, NodeProximityDensityProvider::new));
	
	public float densityAt(World world, Vec3d position){
		return AuraWorld.from(world).getNodesInBounds(new Box(position, position).expand(maxDistance)).stream()
				.filter(x -> x.asVec3d().squaredDistanceTo(position) < maxDistance * maxDistance)
				.filter(x -> x.getType() == nodeType)
				.min(Comparator.comparingDouble(x -> x.asVec3d().squaredDistanceTo(position)))
				.map(value -> ease.apply(1 - (float)(value.asVec3d().distanceTo(position) / maxDistance)))
				.orElse(0f);
	}
	
	public MapCodec<? extends DensityProvider> getCodec(){
		return CODEC;
	}
}