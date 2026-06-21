package arcana.mixin.enchantments;

import arcana.Arcana;
import com.google.common.collect.Multimap;
import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import dev.emi.trinkets.api.SlotReference;
import dev.emi.trinkets.api.Trinket;
import net.minecraft.component.EnchantmentEffectComponentTypes;
import net.minecraft.component.type.ItemEnchantmentsComponent;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.enchantment.effect.AttributeEnchantmentEffect;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.attribute.EntityAttribute;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import java.util.List;
import java.util.Optional;

@Mixin(Trinket.class)
public interface TrinketMixin{
	
	@ModifyReturnValue(method = "getModifiers", at = @At("RETURN"))
	default Multimap<RegistryEntry<EntityAttribute>, EntityAttributeModifier> addModifiers(
			Multimap<RegistryEntry<EntityAttribute>, EntityAttributeModifier> original,
			ItemStack stack,
			SlotReference slot,
			LivingEntity entity,
			Identifier slotIdentifier){
		// trinkets doesn't seem to apply trinket enchantment attribute effects, so...
		ItemEnchantmentsComponent enchantments = EnchantmentHelper.getEnchantments(stack);
		for(RegistryEntry<Enchantment> enchantment : enchantments.getEnchantments()){
			Optional<RegistryKey<Enchantment>> key = enchantment.getKey();
			if(key.isPresent() && key.get().getValue().getNamespace().equals(Arcana.MODID)){
				List<AttributeEnchantmentEffect> effects = enchantment.value().effects().get(EnchantmentEffectComponentTypes.ATTRIBUTES);
				if(effects != null)
					for(AttributeEnchantmentEffect effect : effects)
						original.put(effect.attribute(), effect.createAttributeModifier(enchantments.getLevel(enchantment), slotIdentifier::toUnderscoreSeparatedString));
			}
		}
		return original;
	}
}