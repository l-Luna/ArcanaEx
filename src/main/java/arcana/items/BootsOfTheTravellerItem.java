package arcana.items;

import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.Multimap;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.attribute.EntityAttribute;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.item.ArmorItem;
import net.minecraft.item.ArmorMaterial;

import java.util.UUID;

public class BootsOfTheTravellerItem extends ArmorItem{
	
	private static final UUID speedModifierId = UUID.fromString("2d06d39c-4726-11ed-b878-0242ac120002");
	
	public BootsOfTheTravellerItem(ArmorMaterial material, Settings settings){
		super(material, EquipmentSlot.FEET, settings);
	}
	
	public Multimap<EntityAttribute, EntityAttributeModifier> getAttributeModifiers(EquipmentSlot slot){
		return slot == this.slot ? applySpeedEffect() : super.getAttributeModifiers(slot);
	}
	
	private ImmutableMultimap<EntityAttribute, EntityAttributeModifier> applySpeedEffect(){
		ImmutableMultimap.Builder<EntityAttribute, EntityAttributeModifier> builder = ImmutableMultimap.builder();
		builder.putAll(attributeModifiers);
		builder.put(
				EntityAttributes.GENERIC_MOVEMENT_SPEED,
				new EntityAttributeModifier(speedModifierId, "Speed boost", getSpeedBoost(), EntityAttributeModifier.Operation.MULTIPLY_TOTAL)
		);
		return builder.build();
		// jump boosting is handled by LivingEntityMixin
	}
	
	protected double getSpeedBoost(){
		return 0.16;
	}
}