package arcana.recipes.ingredient;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.fabricmc.fabric.api.recipe.v1.ingredient.CustomIngredient;
import net.fabricmc.fabric.api.recipe.v1.ingredient.CustomIngredientSerializer;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.ItemEnchantmentsComponent;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.registry.entry.RegistryFixedCodec;
import net.minecraft.util.Identifier;

import java.util.List;

import static arcana.Arcana.arcId;

public record EnchantedBookIngredient(RegistryEntry<Enchantment> enchantment, int level) implements CustomIngredient{
	
	public static final Identifier TYPE = arcId("enchanted_book");
	public static final CustomIngredientSerializer<EnchantedBookIngredient> SERIALIZER = new Serializer();
	
	public boolean test(ItemStack stack){
		return stack.isOf(Items.ENCHANTED_BOOK) &&
				stack.getOrDefault(DataComponentTypes.STORED_ENCHANTMENTS, ItemEnchantmentsComponent.DEFAULT).getLevel(enchantment) >= level;
	}
	
	public List<ItemStack> getMatchingStacks(){
		ItemStack book = new ItemStack(Items.ENCHANTED_BOOK);
		EnchantmentHelper.apply(book, builder -> builder.add(enchantment, level));
		return List.of(book);
	}
	
	public boolean requiresTesting(){
		// no point in pregenerating all possible valid choices
		return true;
	}
	
	public CustomIngredientSerializer<?> getSerializer(){
		return SERIALIZER;
	}
	
	private static final class Serializer implements CustomIngredientSerializer<EnchantedBookIngredient>{
		
		private static final MapCodec<EnchantedBookIngredient> CODEC = RecordCodecBuilder.mapCodec(i -> i.group(
				RegistryFixedCodec.of(RegistryKeys.ENCHANTMENT).fieldOf("enchantment").forGetter(EnchantedBookIngredient::enchantment),
				Codec.INT.optionalFieldOf("level", 1).forGetter(EnchantedBookIngredient::level)
		).apply(i, EnchantedBookIngredient::new));
		
		private static final PacketCodec<RegistryByteBuf, EnchantedBookIngredient> PACKET_CODEC = PacketCodec.tuple(
				PacketCodecs.registryEntry(RegistryKeys.ENCHANTMENT), EnchantedBookIngredient::enchantment,
				PacketCodecs.VAR_INT, EnchantedBookIngredient::level,
				EnchantedBookIngredient::new
		);
		
		public Identifier getIdentifier(){
			return TYPE;
		}
		
		public MapCodec<EnchantedBookIngredient> getCodec(boolean allowEmpty){
			return CODEC;
		}
		
		public PacketCodec<RegistryByteBuf, EnchantedBookIngredient> getPacketCodec(){
			return PACKET_CODEC;
		}
	}
}