package arcana.aura;

import arcana.aspects.Aspect;
import arcana.aspects.AspectMap;
import arcana.aspects.Aspects;
import com.mojang.serialization.Codec;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.Util;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.World;

import java.util.List;
import java.util.function.BiConsumer;

public record NodeType(Identifier id, int rechargeTime, int aspectCap, BiConsumer<Node, World> ticker){
	
	public static final Codec<NodeType> CODEC = Identifier.CODEC.xmap(NodeTypes::byName, NodeType::id);
	
	public AspectMap randomCap(Random rng){
		// at least 1 aspect at full capacity; 2 at half-full; 3 at 0-half
		// with a 1/7 chance of an extra non-primal aspect
		int cap = aspectCap();
		AspectMap aspectCap = new AspectMap();
		List<Aspect> primals = Util.copyShuffled(Aspects.primals.stream(), rng);
		aspectCap.add(primals.get(0), cap);
		aspectCap.add(primals.get(1), rng.nextBetween(cap/2, cap));
		aspectCap.add(primals.get(2), rng.nextBetween(cap/2, cap));
		aspectCap.add(primals.get(3), rng.nextBetween(0, cap/2));
		aspectCap.add(primals.get(4), rng.nextBetween(0, cap/2));
		aspectCap.add(primals.get(5), rng.nextBetween(0, cap/2));
		if(rng.nextInt(7) == 0)
			aspectCap.add(Util.getRandom(Aspects.aspects.values().stream().toList(), rng), rng.nextBetween(cap/2, cap));
		return aspectCap;
	}
	
	public Text name(){
		return Text.translatable("arcana.node." + id().getPath());
	}
}