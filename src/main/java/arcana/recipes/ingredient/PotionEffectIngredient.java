package arcana.recipes.ingredient;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.fabricmc.fabric.api.recipe.v1.ingredient.CustomIngredient;
import net.fabricmc.fabric.api.recipe.v1.ingredient.CustomIngredientSerializer;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.PotionContentsComponent;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.potion.Potion;
import net.minecraft.registry.Registries;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.registry.entry.RegistryFixedCodec;
import net.minecraft.util.Identifier;

import java.util.List;
import java.util.Optional;

import static arcana.Arcana.arcId;

public record PotionEffectIngredient(RegistryEntry<StatusEffect> effect) implements CustomIngredient{
	
	public static final Identifier TYPE = arcId("potion_effect");
	public static final CustomIngredientSerializer<PotionEffectIngredient> SERIALIZER = new Serializer();
	
	public boolean test(ItemStack stack){
		PotionContentsComponent potionContents = stack.get(DataComponentTypes.POTION_CONTENTS);
		if(potionContents == null)
			return false;
		for(StatusEffectInstance potionEffect : potionContents.getEffects())
			if(potionEffect.getEffectType().equals(effect))
				return true;
		return false;
	}
	
	public List<ItemStack> getMatchingStacks(){
		// prefer to use real potion types when possible, or fall back to custom potions if needed
		RegistryEntry<Potion> potion = null;
		for(RegistryEntry<Potion> possible : Registries.POTION.getIndexedEntries())
			if(possible.value().getEffects().stream().anyMatch(x -> x.getEffectType().equals(effect))){
				potion = possible;
				break;
			}
		if(potion != null)
			return List.of(PotionContentsComponent.createStack(Items.POTION, potion));
		
		PotionContentsComponent customContents = new PotionContentsComponent(
				Optional.empty(),
				Optional.of(0xFFFFFF),
				List.of(new StatusEffectInstance(effect, 200))
		);
		ItemStack potionStack = new ItemStack(Items.POTION);
		potionStack.set(DataComponentTypes.POTION_CONTENTS, customContents);
		return List.of(potionStack);
	}
	
	public boolean requiresTesting(){
		// no point in pregenerating all possible valid choices
		return true;
	}
	
	public CustomIngredientSerializer<?> getSerializer(){
		return SERIALIZER;
	}
	
	private static final class Serializer implements CustomIngredientSerializer<PotionEffectIngredient>{
		
		private static final MapCodec<PotionEffectIngredient> CODEC = RecordCodecBuilder.mapCodec(i -> i.group(
				RegistryFixedCodec.of(RegistryKeys.STATUS_EFFECT).fieldOf("effect").forGetter(PotionEffectIngredient::effect)
		).apply(i, PotionEffectIngredient::new));
		
		private static final PacketCodec<RegistryByteBuf, PotionEffectIngredient> PACKET_CODEC = PacketCodecs.registryEntry(RegistryKeys.STATUS_EFFECT).xmap(PotionEffectIngredient::new, PotionEffectIngredient::effect);
		
		public Identifier getIdentifier(){
			return TYPE;
		}
		
		public MapCodec<PotionEffectIngredient> getCodec(boolean allowEmpty){
			return CODEC;
		}
		
		public PacketCodec<RegistryByteBuf, PotionEffectIngredient> getPacketCodec(){
			return PACKET_CODEC;
		}
	}
}