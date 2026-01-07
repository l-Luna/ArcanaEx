package arcana.mixin;

import arcana.ArcanaRegistry;
import arcana.aspects.ItemAspectRegistry;
import arcana.aspects.ItemAspectsTooltipData;
import arcana.components.RunicShielding;
import arcana.enchantments.RunicShieldingEnchantment;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import net.minecraft.client.item.TooltipData;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.attribute.EntityAttribute;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.item.ArmorItem;
import net.minecraft.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Optional;

@Mixin(ItemStack.class)
public class ItemStackMixin{
	
	// TODO: reimplement without clobbering tooltips
	@Inject(method = "getTooltipData", at = @At("RETURN"), cancellable = true)
	private void applyAspectsTooltipData(CallbackInfoReturnable<Optional<TooltipData>> cir){
		var aspects = ItemAspectRegistry.get((ItemStack)(Object)this);
		if(!aspects.isEmpty())
			cir.setReturnValue(Optional.of(new ItemAspectsTooltipData(aspects.asStacks(), cir.getReturnValue().orElse(null))));
	}
	
	@ModifyExpressionValue(method = "getAttributeModifiers", at = @At(value = "INVOKE", target = "Lnet/minecraft/item/Item;getAttributeModifiers(Lnet/minecraft/entity/EquipmentSlot;)Lcom/google/common/collect/Multimap;"))
	private Multimap<EntityAttribute, EntityAttributeModifier> getAttributeModifiers(Multimap<EntityAttribute, EntityAttributeModifier> original, EquipmentSlot slot){
		ItemStack self = (ItemStack)(Object)this;
		if(self.getItem() instanceof ArmorItem armor && armor.getSlotType() == slot){
			int shieldingLevel = EnchantmentHelper.getLevel(ArcanaRegistry.RUNIC_SHIELDING, self);
			if(shieldingLevel > 0){
				var ret = HashMultimap.create(original);
				ret.put(RunicShielding.MAX_SHIELDING, new EntityAttributeModifier(RunicShieldingEnchantment.MODIFIER_UUIDS[slot.getEntitySlotId()], "Runic shielding enchantment bonus", shieldingLevel / 2d, EntityAttributeModifier.Operation.ADDITION));
				return ret;
			}
		}
		return original;
	}
}