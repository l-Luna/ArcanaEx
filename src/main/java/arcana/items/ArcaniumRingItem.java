package arcana.items;

import arcana.ArcanaRegistry;
import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.Multimap;
import com.jamieswhiteshirt.reachentityattributes.ReachEntityAttributes;
import dev.emi.trinkets.api.SlotReference;
import dev.emi.trinkets.api.TrinketItem;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.attribute.EntityAttribute;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.item.ItemStack;

import java.util.UUID;

import static net.minecraft.entity.attribute.EntityAttributeModifier.Operation.ADDITION;

public class ArcaniumRingItem extends TrinketItem{
	
	// 1 base defense
	private static Multimap<EntityAttribute, EntityAttributeModifier> modifiers;
	private static final UUID defenseUuid = UUID.fromString("c5804818-530d-11ed-bdc3-0242ac120002");
	private static final UUID projectingUuid = UUID.fromString("6c2b1412-530f-11ed-bdc3-0242ac120002");
	
	public ArcaniumRingItem(Settings settings){
		super(settings);
		modifiers = ImmutableMultimap.of(EntityAttributes.GENERIC_ARMOR, new EntityAttributeModifier(defenseUuid, "Ring modifier", 1, ADDITION));
	}
	
	public Multimap<EntityAttribute, EntityAttributeModifier> getModifiers(ItemStack stack, SlotReference slot, LivingEntity entity, UUID uuid){
		ImmutableMultimap.Builder<EntityAttribute, EntityAttributeModifier> builder = ImmutableMultimap.builder();
		builder.putAll(super.getModifiers(stack, slot, entity, uuid));
		builder.putAll(modifiers);
		int projectingLevel = EnchantmentHelper.getLevel(ArcanaRegistry.PROJECTING, stack);
		if(projectingLevel > 0){
			builder.put(ReachEntityAttributes.REACH, new EntityAttributeModifier(projectingUuid, "Projecting modifier", projectingLevel, ADDITION));
		}
		return builder.build();
	}
}