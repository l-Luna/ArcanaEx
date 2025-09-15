package arcana.items;

import arcana.aura.AuraWorld;
import arcana.aura.Node;
import arcana.entities.WispEntity;
import arcana.network.PkShakeNode;
import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.Multimap;
import com.jamieswhiteshirt.reachentityattributes.ReachEntityAttributes;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.api.EnvironmentInterface;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.attribute.EntityAttribute;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.ProjectileUtil;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Arm;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.Vec3f;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.World;

import java.util.Optional;

@EnvironmentInterface(value = EnvType.CLIENT, itf = PosableItem.class)
public class ScalpelItem extends Item implements PosableItem{
	
	public enum ScalpelType{
		ROSE,
		SILVER,
		BLACK
	}
	
	public final ScalpelType type;
	
	private final Multimap<EntityAttribute, EntityAttributeModifier> attributeModifiers;
	
	public ScalpelItem(Settings settings, ScalpelType type){
		super(settings.maxDamageIfAbsent(100));
		this.type = type;
		
		float attackDamage = switch(type){
			case ROSE, SILVER -> 2.5f;
			case BLACK -> 3.5f;
		};
		float attackSpeed = -3;
		ImmutableMultimap.Builder<EntityAttribute, EntityAttributeModifier> builder = ImmutableMultimap.builder();
		builder.put(
				EntityAttributes.GENERIC_ATTACK_DAMAGE,
				new EntityAttributeModifier(ATTACK_DAMAGE_MODIFIER_ID, "Weapon modifier", attackDamage, EntityAttributeModifier.Operation.ADDITION)
		);
		builder.put(
				EntityAttributes.GENERIC_ATTACK_SPEED,
				new EntityAttributeModifier(ATTACK_SPEED_MODIFIER_ID, "Weapon modifier", attackSpeed, EntityAttributeModifier.Operation.ADDITION)
		);
		this.attributeModifiers = builder.build();
	}
	
	public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand){
		user.setCurrentHand(hand);
		return TypedActionResult.consume(user.getStackInHand(hand));
	}
	
	public void usageTick(World world, LivingEntity user, ItemStack stack, int remainingUseTicks){
		// tick 24 is the actual "hit" frame
		if(remainingUseTicks == 6){
			if(!world.isClient){
				Optional<Node> nodeO = AuraWorld.from(world).raycastNodes(user, false);
				if(nodeO.isPresent()){
					Node node = nodeO.get();
					if(type == ScalpelType.BLACK)
						node.destroy(true); // TODO
					else{
						Random rng = world.random;
						new PkShakeNode(node, 40).sendToAllWatching(user);
						boolean degrade = type == ScalpelType.ROSE || rng.nextInt(3) != 0;
						node.damage(degrade, rng);
						int wisps = rng.nextBetween(2, 3);
						for(int i = 0; i < wisps; i++){
							WispEntity wisp = new WispEntity(world);
							wisp.setPosition(node.asVec3d());
							wisp.setAnchorPos(node.asBlockPos());
							wisp.setVelocity(rng.nextFloat() * 2 - 1, rng.nextFloat() * 2 - 1, rng.nextFloat() * 2 - 1);
							world.spawnEntity(wisp);
						}
					}
					stack.damage(1, user, e -> e.sendToolBreakStatus(e.getActiveHand()));
				}else{
					double reach = ReachEntityAttributes.getReachDistance(user, 4.5);
					Vec3d look = user.getRotationVec(1).multiply(reach);
					Box box = user.getBoundingBox().stretch(look).expand(1);
					EntityHitResult hit = ProjectileUtil.raycast(user, user.getEyePos(), user.getEyePos().add(look), box, WispEntity.class::isInstance, reach*reach);
					if(hit != null)
						hit.getEntity().kill();
				}
			}
		}
	}
	
	public ItemStack finishUsing(ItemStack stack, World world, LivingEntity user){
		return stack;
	}
	
	public int getMaxUseTime(ItemStack stack){
		return 30;
	}
	
	public Multimap<EntityAttribute, EntityAttributeModifier> getAttributeModifiers(EquipmentSlot slot){
		return slot == EquipmentSlot.MAINHAND ? this.attributeModifiers : super.getAttributeModifiers(slot);
	}
	
	public boolean postHit(ItemStack stack, LivingEntity target, LivingEntity attacker){
		stack.damage(1, attacker, e -> e.sendEquipmentBreakStatus(EquipmentSlot.MAINHAND));
		return true;
	}
	
	@Environment(EnvType.CLIENT)
	public void applyPose(MatrixStack matrices, PlayerEntity player, ItemStack stack, float tickDelta, Hand hand, Arm arm){
		matrices.multiply(Vec3f.POSITIVE_Y.getRadialQuaternion(0.2f));
		float x = (player.getItemUseTime() + tickDelta) / (float)getMaxUseTime(stack);
		float of = x < 0.7 ? -x / 3f
				: x <= 0.8 ? 12f * (x - 0.7f) - (0.7f / 3)
				: -6 * (x - 0.8f) + 0.96f;
		matrices.translate(0, 0, -of);
	}
}