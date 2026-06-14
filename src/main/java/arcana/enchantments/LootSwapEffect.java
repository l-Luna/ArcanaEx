package arcana.enchantments;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.enchantment.EnchantmentLevelBasedValue;
import net.minecraft.util.Identifier;

public record LootSwapEffect(EnchantmentLevelBasedValue chance, Identifier mapping){

	public static final Codec<LootSwapEffect> CODEC = RecordCodecBuilder.create(i -> i.group(
			EnchantmentLevelBasedValue.CODEC.fieldOf("chance").forGetter(LootSwapEffect::chance),
			Identifier.CODEC.fieldOf("mapping").forGetter(LootSwapEffect::mapping)
	).apply(i, LootSwapEffect::new));
}