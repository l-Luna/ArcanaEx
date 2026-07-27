package arcana.entities;

import arcana.ArcanaRegistry;
import arcana.entities.goal.ChargedAttack;
import arcana.entities.goal.ChargedAttackGoal;
import arcana.items.WandItem;
import arcana.util.MathUtil;
import net.minecraft.entity.*;
import net.minecraft.entity.ai.goal.*;
import net.minecraft.entity.attribute.DefaultAttributeContainer;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.mob.HostileEntity;
import net.minecraft.entity.passive.IronGolemEntity;
import net.minecraft.entity.passive.MerchantEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Hand;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.LocalDifficulty;
import net.minecraft.world.ServerWorldAccess;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;
import software.bernie.geckolib.animatable.GeoEntity;
import software.bernie.geckolib.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.animation.AnimatableManager;
import software.bernie.geckolib.animation.AnimationController;
import software.bernie.geckolib.animation.PlayState;
import software.bernie.geckolib.animation.RawAnimation;
import software.bernie.geckolib.util.GeckoLibUtil;

import java.util.UUID;

public class ZombieThaumaturgeEntity extends HostileEntity implements GeoEntity{
	
	private static final RawAnimation IDLE_ANIM = RawAnimation.begin().thenPlay("idle");
	private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);
	
	private static final FlameOrbAttack FLAME_ORB_ATTACK = new FlameOrbAttack();
	
	private UUID curProjectileId = null;
	
	public ZombieThaumaturgeEntity(EntityType<? extends HostileEntity> entityType, World world){
		super(entityType, world);
	}
	
	//
	
	public EntityData initialize(ServerWorldAccess world, LocalDifficulty difficulty, SpawnReason spawnReason, EntityData entityData){
		EntityData i = super.initialize(world, difficulty, spawnReason, entityData);
		initEquipment(world.getRandom(), difficulty);
		updateEnchantments(world, random, difficulty);
		setEquipmentDropChance(EquipmentSlot.MAINHAND, 0);
		return i;
	}
	
	protected void initEquipment(Random random, LocalDifficulty localDifficulty){
		super.initEquipment(random, localDifficulty);
		equipStack(EquipmentSlot.MAINHAND, WandItem.basicWand());
	}
	
	protected void initGoals(){
		goalSelector.add(2, new AvoidSunlightGoal(this));
		goalSelector.add(3, new EscapeSunlightGoal(this, 1));
		goalSelector.add(4, new ChargedAttackGoal<>(this, 1, 8, FLAME_ORB_ATTACK));
		goalSelector.add(5, new MeleeAttackGoal(this, 1, false));
		goalSelector.add(6, new MoveThroughVillageGoal(this, 1, true, 4, () -> false));
		goalSelector.add(7, new WanderAroundFarGoal(this, 1));
		goalSelector.add(8, new LookAtEntityGoal(this, PlayerEntity.class, 8));
		goalSelector.add(8, new LookAroundGoal(this));
		
		targetSelector.add(2, new ActiveTargetGoal<>(this, PlayerEntity.class, true));
		targetSelector.add(3, new ActiveTargetGoal<>(this, MerchantEntity.class, false));
		targetSelector.add(3, new ActiveTargetGoal<>(this, IronGolemEntity.class, true));
	}
	
	public static DefaultAttributeContainer.Builder createAttributes(){
		return HostileEntity.createHostileAttributes()
				.add(EntityAttributes.GENERIC_MOVEMENT_SPEED, 0.2f)
				.add(EntityAttributes.GENERIC_ATTACK_DAMAGE, 3)
				.add(EntityAttributes.GENERIC_ARMOR, 2)
				.add(EntityAttributes.GENERIC_FOLLOW_RANGE, 36);
	}
	
	@Nullable
	public Entity getCurProjectile(){
		return curProjectileId != null && getEntityWorld() instanceof ServerWorld sw ? sw.getEntity(curProjectileId) : null;
	}
	
	public void setCurProjectile(@Nullable Entity entity){
		curProjectileId = entity == null ? null : entity.getUuid();
	}
	
	//
	
	public void tickMovement(){
		if(isAlive() && isAffectedByDaylight()){
			ItemStack helmet = getEquippedStack(EquipmentSlot.HEAD);
			if(helmet.isEmpty())
				setOnFireFor(8);
			else if(helmet.isDamageable()){
				Item item = helmet.getItem();
				helmet.setDamage(helmet.getDamage() + random.nextInt(2));
				if(helmet.getDamage() >= helmet.getMaxDamage()){
					sendEquipmentBreakStatus(item, EquipmentSlot.HEAD);
					equipStack(EquipmentSlot.HEAD, ItemStack.EMPTY);
				}
			}
		}
		super.tickMovement();
	}
	
	//
	
	public void registerControllers(AnimatableManager.ControllerRegistrar controllers){
		controllers.add(new AnimationController<>(this, "idle", 20, event -> {
			event.getController().setAnimation(IDLE_ANIM);
			return PlayState.CONTINUE;
		}));
	}
	
	public AnimatableInstanceCache getAnimatableInstanceCache(){
		return cache;
	}
	
	//
	
	public void readCustomDataFromNbt(NbtCompound nbt){
		super.readCustomDataFromNbt(nbt);
		curProjectileId = nbt.containsUuid("cur_projectile_id") ? nbt.getUuid("cur_projectile_id") : null;
	}
	
	public void writeCustomDataToNbt(NbtCompound nbt){
		super.writeCustomDataToNbt(nbt);
		if(curProjectileId != null)
			nbt.putUuid("cur_projectile_id", curProjectileId);
	}
	
	//
	
	private static class FlameOrbAttack implements ChargedAttack<ZombieThaumaturgeEntity>{
		
		public boolean canUse(ZombieThaumaturgeEntity entity){
			return entity.getMainHandStack().isOf(ArcanaRegistry.WAND);
		}
		
		public void begin(ZombieThaumaturgeEntity entity){
			entity.setCurrentHand(Hand.MAIN_HAND);
			// only create a new orb if there isn't already one (e.g. if reopening a world that already had an attack in progress)
			if(!(entity.getCurProjectile() instanceof MagicOrbEntity)){
				World w = entity.getEntityWorld();
				MagicOrbEntity orb = new FlameOrbEntity(ArcanaRegistry.FLAME_ORB, w);
				orb.setOwner(entity);
				orb.setPosition(MathUtil.hoverPosition(entity));
				w.spawnEntity(orb);
				entity.setCurProjectile(orb);
			}
		}
		
		public boolean hasFinishedCharging(ZombieThaumaturgeEntity entity, int chargedTicks){
			Entity projectile = entity.getCurProjectile();
			return projectile instanceof MagicOrbEntity orb && orb.getSize() >= 1;
		}
		
		public void finishCharging(ZombieThaumaturgeEntity entity){
			// particle effects...
		}
		
		public void shootAt(ZombieThaumaturgeEntity entity, LivingEntity target, float time){
			if(entity.getCurProjectile() instanceof MagicOrbEntity orb)
				orb.release(target);
			entity.setCurProjectile(null);
		}
		
		public void cancel(ZombieThaumaturgeEntity entity){
			ChargedAttack.super.cancel(entity);
			if(entity.getCurProjectile() instanceof MagicOrbEntity orb)
				orb.burst();
			entity.setCurProjectile(null);
		}
	}
}