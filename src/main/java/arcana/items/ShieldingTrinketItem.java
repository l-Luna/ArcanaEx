package arcana.items;

import arcana.legacy_components.RunicShielding;
import com.google.common.collect.Multimap;
import dev.emi.trinkets.api.SlotReference;
import dev.emi.trinkets.api.TrinketItem;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.attribute.EntityAttribute;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.item.ItemStack;

import java.util.UUID;

public class ShieldingTrinketItem extends TrinketItem{
	
	private final int shielding;
	
	public ShieldingTrinketItem(Settings settings, int shielding){
		super(settings);
		this.shielding = shielding;
	}
	
	public Multimap<EntityAttribute, EntityAttributeModifier> getModifiers(ItemStack stack, SlotReference slot, LivingEntity entity, UUID uuid){
		Multimap<EntityAttribute, EntityAttributeModifier> modifiers = super.getModifiers(stack, slot, entity, uuid);
		modifiers.put(RunicShielding.MAX_SHIELDING, new EntityAttributeModifier(uuid, "Runic shielding modifier", shielding, EntityAttributeModifier.Operation.ADDITION));
		return modifiers;
	}
}