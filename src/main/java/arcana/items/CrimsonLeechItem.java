package arcana.items;

import arcana.ArcanaRegistry;
import arcana.ArcanaTags;
import arcana.api.AnimatedSwingItem;
import arcana.api.WarpingItem;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.api.EnvironmentInterface;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.component.type.AttributeModifierSlot;
import net.minecraft.component.type.AttributeModifiersComponent;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.tooltip.TooltipType;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.Arm;
import net.minecraft.util.Hand;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RotationAxis;
import org.jetbrains.annotations.Nullable;

import java.util.List;

import static arcana.Arcana.arcId;

@EnvironmentInterface(value = EnvType.CLIENT, itf = AnimatedSwingItem.class)
public class CrimsonLeechItem extends Item implements AnimatedSwingItem, WarpingItem{
	
	public CrimsonLeechItem(Settings settings){
		super(settings);
	}
	
	public static AttributeModifiersComponent createAttributeModifiers(){
		return AttributeModifiersComponent.builder()
				.add(
						EntityAttributes.GENERIC_ATTACK_DAMAGE,
						new EntityAttributeModifier(BASE_ATTACK_DAMAGE_MODIFIER_ID, 6.5f, EntityAttributeModifier.Operation.ADD_VALUE),
						AttributeModifierSlot.MAINHAND
				)
				.add(
						EntityAttributes.GENERIC_ATTACK_SPEED,
						new EntityAttributeModifier(BASE_ATTACK_SPEED_MODIFIER_ID, -2.8f, EntityAttributeModifier.Operation.ADD_VALUE),
						AttributeModifierSlot.MAINHAND
				)
				.add(
						EntityAttributes.PLAYER_ENTITY_INTERACTION_RANGE,
						new EntityAttributeModifier(arcId("attack_reach"), 4, EntityAttributeModifier.Operation.ADD_VALUE),
						AttributeModifierSlot.MAINHAND
				)
				.build();
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
				&& !killed.getType().isIn(ArcanaTags.CANNOT_STEAL_LIFE_FROM)){
			killer.heal(world.random.nextBetween(1, 4));
			// TODO: custom lifesteal sound, vfx
			world.playSound(null, killed.getX(), killed.getY(), killed.getZ(), SoundEvents.ENTITY_CAT_HISS, SoundCategory.HOSTILE, 0.5f, 0.5f, 0);
		}
	}
	
	@Environment(EnvType.CLIENT)
	public void appendTooltip(ItemStack stack, @Nullable TooltipContext ctx, List<Text> tooltip, TooltipType type){
		super.appendTooltip(stack, ctx, tooltip, type);
		tooltip.add(WarpingItem.warpingTooltip(2));
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
			matrices.translate(0, 0.4, -0.4 * (1 - swingProgress) * (swingProgress) - 0.8);
			matrices.scale(1, 0.9f - 0.2f * swingProgress, 1);
			float a = arm == Arm.RIGHT ? 0.9f : 0;
			matrices.multiply(RotationAxis.POSITIVE_X.rotation(-MathHelper.HALF_PI * (a + 0.1f * swingProgress)));
		}
		
		return true;
	}
}