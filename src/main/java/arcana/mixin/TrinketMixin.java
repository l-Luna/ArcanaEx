package arcana.mixin;

import arcana.ArcanaRegistry;
import arcana.enchantments.ProjectingEnchantment;
import com.google.common.collect.Multimap;
import com.jamieswhiteshirt.reachentityattributes.ReachEntityAttributes;
import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import dev.emi.trinkets.api.SlotReference;
import dev.emi.trinkets.api.Trinket;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.attribute.EntityAttribute;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import java.util.UUID;

import static net.minecraft.entity.attribute.EntityAttributeModifier.Operation.ADDITION;

@Mixin(Trinket.class)
public interface TrinketMixin{

	@ModifyReturnValue(method = "getModifiers", at = @At("RETURN"))
	default Multimap<EntityAttribute, EntityAttributeModifier> addModifiers(Multimap<EntityAttribute, EntityAttributeModifier> original,
	                                                                        ItemStack stack,
	                                                                        SlotReference slot,
	                                                                        LivingEntity entity,
	                                                                        UUID uuid){
		int maxProjecting = ProjectingEnchantment.maxLevelFor(stack);
		if(maxProjecting > 0){
			int projectingLevel = EnchantmentHelper.getLevel(ArcanaRegistry.PROJECTING, stack);
			if(projectingLevel > 0)
				// TODO: what if original stack already uses `uuid` for reach?
				original.put(ReachEntityAttributes.REACH, new EntityAttributeModifier(uuid, "Projecting modifier", Math.min(projectingLevel, maxProjecting), ADDITION));
		}
		return original;
	}
}