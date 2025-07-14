package arcana.worldgen.geodes;

import arcana.aspects.Aspect;
import arcana.aspects.AspectMap;
import arcana.aspects.Aspects;
import arcana.aura.AuraWorld;
import arcana.aura.Node;
import arcana.aura.NodeType;
import arcana.aura.NodeTypes;
import net.minecraft.util.Util;
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
			// create node
			NodeType type = randomType(rng);
			Node toAdd = new Node(type, aura.getWorld(), new Vec3d(nodePos.getX() + rng.nextDouble(), nodePos.getY() + rng.nextDouble(), nodePos.getZ() + rng.nextDouble()), rng);
			toAdd.getOrCreateTag().putBoolean("in_geode", true);
			// setup aspect cap
			// contain greater-than-maximum of this primal,
			//   then 1-2 other primals (possibly repeating this one),
			//   then 1/4 chance of a random compound
			Aspect aspect = context.getConfig().primaryAspect;
			AspectMap cap = toAdd.getAspectCap();
			cap.clear();
			cap.add(aspect, type.aspectCap() + 15);
			cap.add(Util.getRandom(Aspects.primals, rng), rng.nextBetween(type.aspectCap() / 4, type.aspectCap() / 2));
			if(rng.nextInt(3) == 0)
				cap.add(Util.getRandom(Aspects.primals, rng), rng.nextBetween(type.aspectCap() / 4, type.aspectCap() / 2));
			if(rng.nextInt(4) == 0)
				cap.add(Util.getRandom(Aspects.getCompoundAspects(), rng), rng.nextBetween(type.aspectCap() / 4, type.aspectCap() / 2));
			// initial aspect content
			AspectMap content = toAdd.getAspects();
			content.clear();
			content.add(cap);
			// update chunk
			aura.addNode(toAdd);
			if(type == NodeTypes.TAINTED)
				aura.getOrCreateChunk(nodePos).incrementFlux(rng.nextBetween(7, 12), null);
			return true;
		}
		return false;
	}
}