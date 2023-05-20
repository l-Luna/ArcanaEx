package arcana.items;

import arcana.ArcanaRegistry;
import arcana.aspects.Aspect;
import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.Multimap;
import com.jamieswhiteshirt.reachentityattributes.ReachEntityAttributes;
import dev.emi.trinkets.api.SlotReference;
import dev.emi.trinkets.api.TrinketItem;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.attribute.EntityAttribute;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.UUID;

import static net.minecraft.entity.attribute.EntityAttributeModifier.Operation.ADDITION;

public class RingItem extends TrinketItem implements VisDiscountingItem{
	
	// 1 base defense
	private static final UUID defenseUuid = UUID.fromString("c5804818-530d-11ed-bdc3-0242ac120002");
	private static final UUID projectingUuid = UUID.fromString("6c2b1412-530f-11ed-bdc3-0242ac120002");
	
	private static final Multimap<EntityAttribute, EntityAttributeModifier> modifiers = ImmutableMultimap.of(EntityAttributes.GENERIC_ARMOR, new EntityAttributeModifier(defenseUuid, "Ring modifier", 1, ADDITION));
	
	private final int maxProjecting, percentOff;
	
	public RingItem(Settings settings, int maxProjecting, int percentOff){
		super(settings);
		this.maxProjecting = maxProjecting;
		this.percentOff = percentOff;
	}
	
	public Multimap<EntityAttribute, EntityAttributeModifier> getModifiers(ItemStack stack, SlotReference slot, LivingEntity entity, UUID uuid){
		ImmutableMultimap.Builder<EntityAttribute, EntityAttributeModifier> builder = ImmutableMultimap.builder();
		builder.putAll(super.getModifiers(stack, slot, entity, uuid));
		builder.putAll(modifiers);
		int projectingLevel = EnchantmentHelper.getLevel(ArcanaRegistry.PROJECTING, stack);
		if(projectingLevel > 0)
			builder.put(ReachEntityAttributes.REACH, new EntityAttributeModifier(projectingUuid, "Projecting modifier", Math.min(projectingLevel, maxProjecting), ADDITION));
		return builder.build();
	}
	
	public int percentOff(ItemStack stack, Aspect aspect, PlayerEntity player){
		return percentOff;
	}
	
	public void appendTooltip(ItemStack stack, @Nullable World world, List<Text> tooltip, TooltipContext context){
		if(percentOff != 0)
			tooltip.add(Text.translatable("tooltip.arcana.wand.discount.all", percentOff));
	}
}