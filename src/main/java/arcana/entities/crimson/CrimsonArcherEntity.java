package arcana.entities.crimson;

import arcana.ArcanaRegistry;
import arcana.ArcanaTags;
import arcana.cca_components.CaArrow;
import arcana.items.CrimsonLongbowItem;
import net.minecraft.block.BlockState;
import net.minecraft.entity.*;
import net.minecraft.entity.ai.RangedAttackMob;
import net.minecraft.entity.ai.goal.BowAttackGoal;
import net.minecraft.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.entity.attribute.DefaultAttributeContainer;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.mob.HostileEntity;
import net.minecraft.entity.projectile.ArrowEntity;
import net.minecraft.entity.projectile.PersistentProjectileEntity;
import net.minecraft.entity.projectile.ProjectileUtil;
import net.minecraft.item.BowItem;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.random.Random;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.world.*;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class CrimsonArcherEntity extends CrimsonEntity implements RangedAttackMob{
	
	private final BowAttackGoal<CrimsonArcherEntity> bowAttackGoal = new AnyBowAttackGoal<>(this, 1, 30, 15);
	private final MeleeAttackGoal meleeAttackGoal = new MeleeAttackGoal(this, 1.3, false){
		public void start(){
			super.start();
			setAttacking(true);
		}
		
		public void stop(){
			super.stop();
			setAttacking(false);
		}
	};
	
	public CrimsonArcherEntity(EntityType<? extends HostileEntity> entityType, World world){
		super(entityType, world);
		updateAttackType();
	}
	
	// setup
	
	protected void initEquipment(Random random, LocalDifficulty localDifficulty){
		super.initEquipment(random, localDifficulty);
		equipStack(EquipmentSlot.MAINHAND, new ItemStack(ArcanaRegistry.CRIMSON_LONGBOW));
	}
	
	public EntityData initialize(ServerWorldAccess world, LocalDifficulty difficulty, SpawnReason spawnReason, @Nullable EntityData entityData){
		EntityData i = super.initialize(world, difficulty, spawnReason, entityData);
		updateAttackType();
		return i;
	}
	
	// "if you don't have a bow, don't try to use it"
	public void updateAttackType(){
		if(getWorld() != null && !getWorld().isClient){
			goalSelector.remove(meleeAttackGoal);
			goalSelector.remove(bowAttackGoal);
			ItemStack itemStack = getStackInHand(getBowHand());
			if(itemStack.getItem() instanceof BowItem)
				goalSelector.add(4, bowAttackGoal);
			else
				goalSelector.add(4, meleeAttackGoal);
		}
	}
	
	// attributes
	
	public static DefaultAttributeContainer.Builder createArcherAttributes(){
		return CrimsonEntity.createCrimsonAttributes().add(EntityAttributes.GENERIC_MOVEMENT_SPEED, 0.3);
	}
	
	// behaviour
	
	public void shootAt(LivingEntity target, float pullProgress){
		Hand hand = getBowHand();
		ItemStack bowStack = getStackInHand(hand);
		ItemStack arrowStack = getProjectileType(bowStack);
		PersistentProjectileEntity arrow = createArrowProjectile(arrowStack, pullProgress, bowStack);
		double diffX = target.getX() - getX();
		double diffY = target.getBodyY(0.3333333333333333) - arrow.getY();
		double diffZ = target.getZ() - getZ();
		double dist = Math.sqrt(diffX * diffX + diffZ * diffZ);
		arrow.setVelocity(diffX, diffY + dist * 0.2f, diffZ, 1.6f, 14f - getWorld().getDifficulty().getId() * 4);
		if(arrow instanceof ArrowEntity ae && bowStack.getItem() instanceof CrimsonLongbowItem)
			CaArrow.setProjected(ae, true);
		playSound(SoundEvents.ENTITY_SKELETON_SHOOT, 1, 1 / (getRandom().nextFloat() * .4f + .8f));
		getWorld().spawnEntity(arrow);
	}
	
	private @NotNull Hand getBowHand(){
		return getMainHandStack().getItem() instanceof BowItem ? Hand.MAIN_HAND : Hand.OFF_HAND;
	}
	
	protected PersistentProjectileEntity createArrowProjectile(ItemStack arrow, float damageModifier, ItemStack shotFrom){
		return ProjectileUtil.createArrowProjectile(this, arrow, damageModifier, shotFrom);
	}
	
	public void equipStack(EquipmentSlot slot, ItemStack stack){
		super.equipStack(slot, stack);
		if(!getWorld().isClient)
			updateAttackType();
	}
	
	// target enemies through thin blocks when using the crimson longbow
	public boolean canSee(Entity entity){
		boolean canShootThrough = getMainHandStack().getItem() instanceof CrimsonLongbowItem
		                       || getOffHandStack().getItem() instanceof CrimsonLongbowItem;
		if(canShootThrough){
			Vec3d src = new Vec3d(getX(), getEyeY(), getZ());
			Vec3d dst = new Vec3d(entity.getX(), entity.getEyeY(), entity.getZ());
			if(entity.getWorld() != getWorld() || src.distanceTo(dst) > 128)
				return false;
			return this.getWorld().raycast(new RaycastContext(src, dst, RaycastContext.ShapeType.COLLIDER, RaycastContext.FluidHandling.NONE, this){
				// allow seeing through thin blocks
				public VoxelShape getBlockShape(BlockState state, BlockView world, BlockPos pos){
					return state.isIn(ArcanaTags.PROJECTED_ARROW_IGNORES) ? VoxelShapes.empty() : super.getBlockShape(state, world, pos);
				}
			}).getType() == HitResult.Type.MISS;
		}
		return super.canSee(entity);
	}
	
	// serialization
	
	public void readCustomDataFromNbt(NbtCompound nbt){
		super.readCustomDataFromNbt(nbt);
		updateAttackType();
	}
	
	// work with custom bows
	
	protected static class AnyBowAttackGoal<T extends HostileEntity & RangedAttackMob> extends BowAttackGoal<T>{
		
		private final double speed;
		
		public AnyBowAttackGoal(T actor, double speed, int attackInterval, float range){
			super(actor, speed, attackInterval, range);
			this.speed = speed;
		}
		
		protected boolean isHoldingBow(){
			return actor.isHolding(x -> x.getItem() instanceof BowItem);
		}
		
		// copy paste to change the final line
		
		public void tick(){
			LivingEntity target = actor.getTarget();
			if(target != null){
				double dist = actor.squaredDistanceTo(target.getX(), target.getY(), target.getZ());
				boolean canSee = actor.getVisibilityCache().canSee(target);
				boolean bl2 = targetSeeingTicker > 0;
				if(canSee != bl2)
					targetSeeingTicker = 0;
				
				if(canSee)
					++targetSeeingTicker;
				else
					--targetSeeingTicker;
				
				if(!(dist > squaredRange) && targetSeeingTicker >= 20){
					actor.getNavigation().stop();
					++combatTicks;
				}else{
					actor.getNavigation().startMovingTo(target, speed);
					combatTicks = -1;
				}
				
				if(combatTicks >= 20){
					if(actor.getRandom().nextFloat() < 0.3){
						movingToLeft = !movingToLeft;
					}
					
					if(actor.getRandom().nextFloat() < 0.3){
						backward = !backward;
					}
					
					combatTicks = 0;
				}
				
				if(combatTicks > -1){
					if(dist > squaredRange * 0.75F)
						backward = false;
					else if(dist < squaredRange * 0.25F)
						backward = true;
					
					actor.getMoveControl().strafeTo(backward ? -0.5F : 0.5F, movingToLeft ? 0.5F : -0.5F);
					actor.lookAtEntity(target, 30.0F, 30.0F);
				}else
					actor.getLookControl().lookAt(target, 30.0F, 30.0F);
				
				if(actor.isUsingItem()){
					if(!canSee && targetSeeingTicker < -60)
						actor.clearActiveItem();
					else if(canSee){
						int i = actor.getItemUseTime();
						if(i >= 20){
							actor.clearActiveItem();
							actor.shootAt(target, BowItem.getPullProgress(i));
							cooldown = attackInterval;
						}
					}
				}else if(--cooldown <= 0 && targetSeeingTicker >= -60)
					actor.setCurrentHand(actor.getMainHandStack().getItem() instanceof BowItem ? Hand.MAIN_HAND : Hand.OFF_HAND);
			}
		}
	}
}