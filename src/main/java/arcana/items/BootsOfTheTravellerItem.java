package arcana.items;

import net.minecraft.component.type.AttributeModifierSlot;
import net.minecraft.component.type.AttributeModifiersComponent;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.item.ArmorItem;
import net.minecraft.item.ArmorMaterial;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.util.Identifier;

import static arcana.Arcana.arcId;

public class BootsOfTheTravellerItem extends ArmorItem{
	
	private static final Identifier SPEED_MOD_ID = arcId("boots_of_the_traveller/movement_speed");
	private static final Identifier STEP_HEIGHT_MOD_ID = arcId("boots_of_the_traveller/step_height");
	
	public BootsOfTheTravellerItem(RegistryEntry<ArmorMaterial> material, Settings settings){
		super(material, Type.BOOTS, settings);
	}
	
	public static AttributeModifiersComponent createAttributeModifiers(){
		return AttributeModifiersComponent.builder()
				.add(
						EntityAttributes.GENERIC_MOVEMENT_SPEED,
						new EntityAttributeModifier(SPEED_MOD_ID, 0.17f, EntityAttributeModifier.Operation.ADD_MULTIPLIED_BASE),
						AttributeModifierSlot.FEET
				)
				.add(
						EntityAttributes.GENERIC_STEP_HEIGHT,
						new EntityAttributeModifier(STEP_HEIGHT_MOD_ID, 0.4f, EntityAttributeModifier.Operation.ADD_VALUE),
						AttributeModifierSlot.FEET
				)
				.build();
	}
}