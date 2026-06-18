package arcana.items.components;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.util.Identifier;

import java.util.*;

// like a skimmed down Researcher
public record ResearchCompletionComponent(
		Map<Identifier, Integer> stages,
		Set<Identifier> puzzles
){

	public static final ResearchCompletionComponent DEFAULT = new ResearchCompletionComponent(Map.of(), Set.of());
	
	public static final Codec<ResearchCompletionComponent> CODEC = RecordCodecBuilder.create(i -> i.group(
			Codec.unboundedMap(Identifier.CODEC, Codec.INT).fieldOf("stages").forGetter(ResearchCompletionComponent::stages),
			Codec.list(Identifier.CODEC).<Set<Identifier>>xmap(HashSet::new, ArrayList::new).fieldOf("puzzles").forGetter(ResearchCompletionComponent::puzzles)
	).apply(i, ResearchCompletionComponent::new));
	
	public ResearchCompletionComponent mutableCopy(){
		return new ResearchCompletionComponent(new HashMap<>(stages), new HashSet<>(puzzles));
	}
}