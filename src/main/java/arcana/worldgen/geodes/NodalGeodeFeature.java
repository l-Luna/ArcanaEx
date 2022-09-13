package arcana.worldgen.geodes;

import arcana.components.AuraWorld;
import arcana.nodes.Node;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.gen.feature.ConfiguredFeature;
import net.minecraft.world.gen.feature.Feature;
import net.minecraft.world.gen.feature.GeodeFeature;
import net.minecraft.world.gen.feature.GeodeFeatureConfig;
import net.minecraft.world.gen.feature.util.FeatureContext;

import static arcana.worldgen.SurfaceNodeFeature.randomType;

public class NodalGeodeFeature extends Feature<NodalGeodeFeatureConfig>{
	
	public NodalGeodeFeature(){
		super(NodalGeodeFeatureConfig.CODEC);
	}
	
	public boolean generate(FeatureContext<NodalGeodeFeatureConfig> context){
		// delegate to GeodeFeature, add node on top
		GeodeFeature delegate = new GeodeFeature(GeodeFeatureConfig.CODEC);
		GeodeFeatureConfig geodeConfig = context.getConfig().geodeConfig;
		ConfiguredFeature<GeodeFeatureConfig, ?> confDelegate = new ConfiguredFeature<>(delegate, geodeConfig);
		var pos = context.getOrigin();
		var rng = context.getRandom();
		if(confDelegate.generate(context.getWorld(), context.getGenerator(), rng, pos)){
			AuraWorld aura = AuraWorld.from(context.getWorld());
			int i = geodeConfig.maxGenOffset;
			BlockPos nodePos = pos.add(i / 3, i / 3, i / 3);
			Node toAdd = new Node(randomType(rng), aura.getWorld(), new Vec3d(nodePos.getX() + rng.nextDouble(), nodePos.getY() + rng.nextDouble(), nodePos.getZ() + rng.nextDouble()));
			// TODO: aspect caps
			toAdd.getAspects().add(context.getConfig().primaryAspect, 10);
			aura.getNodes().add(toAdd);
			aura.getWorld().syncComponent(AuraWorld.KEY); // TODO: only sync the new node
			return true;
		}
		return false;
	}
}