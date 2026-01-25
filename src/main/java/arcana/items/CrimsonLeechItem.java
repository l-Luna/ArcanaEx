package arcana.items;

import arcana.ArcanaRegistry;
import arcana.api.AnimatedSwingItem;
import arcana.api.WarpingItem;
import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.Multimap;
import com.jamieswhiteshirt.reachentityattributes.ReachEntityAttributes;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.api.EnvironmentInterface;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityGroup;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.attribute.EntityAttribute;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Vanishable;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.Arm;
import net.minecraft.util.Hand;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Quaternion;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.UUID;

import static net.minecraft.entity.attribute.EntityAttributeModifier.Operation.ADDITION;

@EnvironmentInterface(value = EnvType.CLIENT, itf = AnimatedSwingItem.class)
public class CrimsonLeechItem extends Item implements Vanishable, AnimatedSwingItem, WarpingItem{
	
	private static final UUID reachUuid = UUID.fromString("708e5db3-f04c-440c-a0af-d7295835d99a");
	
	private final Multimap<EntityAttribute, EntityAttributeModifier> attributeModifiers;
	
	public CrimsonLeechItem(Settings settings){
		super(settings);
		
		float attackDamage = 6.5f;
		ImmutableMultimap.Builder<EntityAttribute, EntityAttributeModifier> builder = ImmutableMultimap.builder();
		builder.put(
				EntityAttributes.GENERIC_ATTACK_DAMAGE,
				new EntityAttributeModifier(ATTACK_DAMAGE_MODIFIER_ID, "Weapon modifier", attackDamage, EntityAttributeModifier.Operation.ADDITION)
		);
		builder.put(
				EntityAttributes.GENERIC_ATTACK_SPEED,
				new EntityAttributeModifier(ATTACK_SPEED_MODIFIER_ID, "Weapon modifier", -2.8f, EntityAttributeModifier.Operation.ADDITION)
		);
		builder.put(
				ReachEntityAttributes.ATTACK_RANGE,
				new EntityAttributeModifier(reachUuid, "Weapon modifier", 4, ADDITION)
		);
		attributeModifiers = builder.build();
	}
	
	public Multimap<EntityAttribute, EntityAttributeModifier> getAttributeModifiers(EquipmentSlot slot){
		return slot == EquipmentSlot.MAINHAND ? attributeModifiers : super.getAttributeModifiers(slot);
	}
	
	public int getEnchantability(){
		return 1;
	}
	
	public int warping(ItemStack stack, PlayerEntity player){
		return 2;
	}
	
	public static void handleEntityDeath(ServerWorld world, Entity killerEntity, LivingEntity killed){
		if(killerEntity instanceof LivingEntity killer
				&& killer.getStackInHand(Hand.MAIN_HAND).isOf(ArcanaRegistry.CRIMSON_LEECH)
				&& killed.getGroup() != EntityGroup.UNDEAD){
			killer.heal(world.random.nextBetween(1, 4));
			world.playSound(null, killed.getX(), killed.getY(), killed.getZ(), SoundEvents.ENTITY_CAT_HISS, SoundCategory.HOSTILE, 0.5f, 0.5f, 0);
			// particles...
		}
	}
	
	@Environment(EnvType.CLIENT)
	public void appendTooltip(ItemStack stack, @Nullable World world, List<Text> tooltip, TooltipContext context){
		super.appendTooltip(stack, world, tooltip, context);
		tooltip.add(ArcanaRegistry.WARPING.getName(2));
	}
	
	@Environment(EnvType.CLIENT)
	public boolean applySwingAnimation(MatrixStack matrices, PlayerEntity player, ItemStack stack, float tickDelta, float swingProgress, float equipProgress, Hand hand, Arm arm){
		if(player.preferredHand != hand)
			return false;
		// TODO: cleanup
		// undo some builtin offsets
		int off = arm == Arm.RIGHT ? 1 : -1;
		
		float x = -0.4F * MathHelper.sin(MathHelper.sqrt(swingProgress) * MathHelper.PI);
		float y = 0.2F * MathHelper.sin(MathHelper.sqrt(swingProgress) * (MathHelper.PI * 2));
		float z = -0.2F * MathHelper.sin(swingProgress * MathHelper.PI);
		matrices.translate(-off * x, -y, -z);
		if(player.handSwinging){
			matrices.translate(0, equipProgress * 0.6F, 0);
			matrices.translate(0, 0.4, -0.4 * (1-swingProgress) * (swingProgress) - 0.8);
			matrices.scale(1, 0.9f - 0.2f * swingProgress, 1);
			float a = arm == Arm.RIGHT ? 0.9f : 0;
			matrices.multiply(Quaternion.fromEulerXyz(-MathHelper.HALF_PI * (a + 0.1f * swingProgress), 0, 0));
		}
		
		return true;
	}
}