package arcana.items.components;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.registry.entry.RegistryFixedCodec;

import java.util.Optional;

public record FragileComponent(int colour, Optional<RegistryEntry<StatusEffect>> effect){
	
	public static final Codec<FragileComponent> CODEC = RecordCodecBuilder.create(i -> i.group(
			Codec.INT.fieldOf("color").forGetter(FragileComponent::colour),
			RegistryFixedCodec.of(RegistryKeys.STATUS_EFFECT).optionalFieldOf("effect").forGetter(FragileComponent::effect)
	).apply(i, FragileComponent::new));
	
	public FragileComponent(int colour){
		this(colour, Optional.empty());
	}
	
	public FragileComponent(int colour, RegistryEntry<StatusEffect> effect){
		this(colour, Optional.of(effect));
	}
}