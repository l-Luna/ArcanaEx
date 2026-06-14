package arcana.enchantments;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.item.Item;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.tag.TagKey;

import java.util.List;

public record DynamicMaxLevelsEffect(List<TagKey<Item>> maxLevelTags){

	public static final Codec<DynamicMaxLevelsEffect> CODEC = RecordCodecBuilder.create(i -> i.group(
			TagKey.codec(RegistryKeys.ITEM).listOf().fieldOf("max_level_tags").forGetter(DynamicMaxLevelsEffect::maxLevelTags)
	).apply(i, DynamicMaxLevelsEffect::new));
}