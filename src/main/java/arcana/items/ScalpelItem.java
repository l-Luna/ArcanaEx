package arcana.items;

import arcana.ArcanaRegistry;
import arcana.api.AnimatedUseItem;
import arcana.api.ScalpelSlashable;
import arcana.aura.*;
import arcana.client.particles.CubeParticleEffect;
import arcana.client.particles.CubeParticleStyle;
import arcana.entities.wisps.PureWispEntity;
import arcana.entities.wisps.TaintedWispEntity;
import arcana.entities.wisps.WispEntity;
import arcana.network.PkShakeNode;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.api.EnvironmentInterface;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.component.type.AttributeModifierSlot;
import net.minecraft.component.type.AttributeModifiersComponent;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.ProjectileUtil;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Arm;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.RotationAxis;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.World;

import java.util.Optional;

@EnvironmentInterface(value = EnvType.CLIENT, itf = AnimatedUseItem.class)
public class ScalpelItem extends Item implements AnimatedUseItem{
	
	public enum ScalpelType{
		ROSE,
		SILVER,
		BLACK
	}
	
	public final ScalpelType type;
	
	public ScalpelItem(Settings settings, ScalpelType type){
		super(settings.maxDamage(100));
		this.type = type;
	}
	
	public static AttributeModifiersComponent createAttributeModifiers(ScalpelType type){
		float attackDamage = switch(type){
			case ROSE, SILVER -> 2.5f;
			case BLACK -> 3.5f;
		};
		return AttributeModifiersComponent.builder()
				.add(
						EntityAttributes.GENERIC_ATTACK_DAMAGE,
						new EntityAttributeModifier(BASE_ATTACK_DAMAGE_MODIFIER_ID, attackDamage, EntityAttributeModifier.Operation.ADD_VALUE),
						AttributeModifierSlot.MAINHAND
				)
				.add(
						EntityAttributes.GENERIC_ATTACK_SPEED,
						new EntityAttributeModifier(BASE_ATTACK_SPEED_MODIFIER_ID, -3, EntityAttributeModifier.Operation.ADD_VALUE),
						AttributeModifierSlot.MAINHAND
				)
				.build();
	}
	
	public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand){
		user.setCurrentHand(hand);
		return TypedActionResult.consume(user.getStackInHand(hand));
	}
	
	public void usageTick(World world, LivingEntity user, ItemStack stack, int remainingUseTicks){
		// tick 23 is the actual "hit" frame
		if(remainingUseTicks == 7 && user instanceof PlayerEntity pe){
			Optional<Node> nodeO = AuraWorld.from(world).raycastNodes(user, false);
			Random rng = world.random;
			EquipmentSlot hand = LivingEntity.getSlotForHand(user.getActiveHand());
			if(nodeO.isPresent()){
				if(!world.isClient){
					Node node = nodeO.get();
					NodeType oldNodeType = node.getType();
					if(type == ScalpelType.BLACK)
						node.destroy(true); // TODO
					else{
						new PkShakeNode(node, 40).sendToAllWatching(user);
						boolean degrade = type == ScalpelType.ROSE || rng.nextInt(3) != 0;
						node.damage(degrade, rng);
						int wisps = rng.nextBetween(2, 3);
						for(int i = 0; i < wisps; i++){
							WispEntity wisp
									= oldNodeType == NodeTypes.TAINTED ? new TaintedWispEntity(world)
									: oldNodeType == NodeTypes.PURE ? new PureWispEntity(world)
									: new WispEntity(world);
							wisp.setPosition(node.asVec3d());
							wisp.setAnchorPos(node.asBlockPos());
							wisp.setVelocity(rng.nextFloat() * 2 - 1, rng.nextFloat() * 2 - 1, rng.nextFloat() * 2 - 1);
							world.spawnEntity(wisp);
						}
					}
					stack.damage(1, user, hand);
				}
			}else{
				// TODO: separate block and entity interaction ranges
				double reach = user.getAttributeValue(EntityAttributes.PLAYER_BLOCK_INTERACTION_RANGE);
				double sqReach = reach * reach;
				
				HitResult blockHit = user.raycast(reach, 1, false);
				sqReach = Math.min(sqReach, blockHit.getPos().squaredDistanceTo(user.getEyePos()));
				
				Vec3d look = user.getRotationVec(1).multiply(reach);
				Box box = user.getBoundingBox().stretch(look).expand(1);
				EntityHitResult entityHit = ProjectileUtil.raycast(user, user.getEyePos(), user.getEyePos().add(look), box, ScalpelSlashable.class::isInstance, sqReach);
				
				if(entityHit != null && entityHit.getEntity() instanceof ScalpelSlashable se){
					se.onScalpelSlash(world, pe, entityHit.getEntity().getBlockPos());
					stack.damage(1, user, hand);
				}else if(blockHit instanceof BlockHitResult bhr
						&& bhr.getType() != HitResult.Type.MISS){
					BlockPos pos = bhr.getBlockPos();
					if(WardedChunk.isWarded(world, pos)){
						if(!world.isClient){
							ServerWorld sw = (ServerWorld)world;
							CubeParticleStyle style;
							if(rng.nextInt(3) == 0){
								WardedChunk.setWarded(world, pos, false);
								WardedChunk.sync(world, pos);
								style = CubeParticleStyle.DISAPPEAR;
							}else
								style = CubeParticleStyle.SHAKE;
							sw.spawnParticles(new CubeParticleEffect(ArcanaRegistry.WARDING_EFFECT, style), pos.getX(), pos.getY(), pos.getZ(), 0, 0, 0, 0, 0);
							stack.damage(1, user, hand);
						}
					}else if(world.getBlockState(pos).getBlock() instanceof ScalpelSlashable se){
						se.onScalpelSlash(world, pe, pos);
						stack.damage(1, user, hand);
					}
				}
			}
		}
	}
	
	public ItemStack finishUsing(ItemStack stack, World world, LivingEntity user){
		return stack;
	}
	
	public int getMaxUseTime(ItemStack stack, LivingEntity user){
		return 30;
	}
	
	/*public Multimap<EntityAttribute, EntityAttributeModifier> getAttributeModifiers(EquipmentSlot slot){
		return slot == EquipmentSlot.MAINHAND ? this.attributeModifiers : super.getAttributeModifiers(slot);
	}*/
	
	public boolean postHit(ItemStack stack, LivingEntity target, LivingEntity attacker){
		stack.damage(1, attacker, EquipmentSlot.MAINHAND);
		return true;
	}
	
	@Environment(EnvType.CLIENT)
	public void applyUsingAnimation(MatrixStack matrices, PlayerEntity player, ItemStack stack, float tickDelta, Hand hand, Arm arm){
		matrices.multiply(RotationAxis.POSITIVE_Y.rotation(0.2f));
		float x = (player.getItemUseTime() + tickDelta) / (float)getMaxUseTime(stack, player);
		float of = x < 0.7 ? -x / 3f
				: x <= 0.8 ? 12f * (x - 0.7f) - (0.7f / 3)
				: -6 * (x - 0.8f) + 0.96f;
		matrices.translate(0, 0, -of);
	}
}