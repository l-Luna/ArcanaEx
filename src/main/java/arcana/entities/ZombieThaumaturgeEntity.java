package arcana.entities;

import arcana.items.WandItem;
import net.minecraft.entity.EntityData;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.SpawnReason;
import net.minecraft.entity.ai.goal.*;
import net.minecraft.entity.attribute.DefaultAttributeContainer;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.mob.HostileEntity;
import net.minecraft.entity.passive.IronGolemEntity;
import net.minecraft.entity.passive.MerchantEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.LocalDifficulty;
import net.minecraft.world.ServerWorldAccess;
import net.minecraft.world.World;
import software.bernie.geckolib.animatable.GeoEntity;
import software.bernie.geckolib.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.animation.AnimatableManager;
import software.bernie.geckolib.animation.AnimationController;
import software.bernie.geckolib.animation.PlayState;
import software.bernie.geckolib.animation.RawAnimation;
import software.bernie.geckolib.util.GeckoLibUtil;

public class ZombieThaumaturgeEntity extends HostileEntity implements GeoEntity{
	
	private static final RawAnimation IDLE_ANIM = RawAnimation.begin().thenPlay("idle");
	private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);
	
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
		goalSelector.add(4, new MeleeAttackGoal(this, 1, false));
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
}