package arcana.entities.crimson;

import net.minecraft.entity.EntityData;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.SpawnReason;
import net.minecraft.entity.ai.goal.*;
import net.minecraft.entity.attribute.DefaultAttributeContainer;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.mob.HostileEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.BowItem;
import net.minecraft.item.RangedWeaponItem;
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

public class CrimsonEntity extends HostileEntity implements GeoEntity{
	
	private static final RawAnimation IDLE_ANIM = RawAnimation.begin().thenPlay("idle");
	private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);
	
	public CrimsonEntity(EntityType<? extends HostileEntity> entityType, World world){
		super(entityType, world);
	}
	
	// setup
	
	public EntityData initialize(ServerWorldAccess world, LocalDifficulty difficulty, SpawnReason spawnReason, @Nullable EntityData entityData){
		EntityData i = super.initialize(world, difficulty, spawnReason, entityData);
		initEquipment(world.getRandom(), difficulty);
		updateEnchantments(world, random, difficulty);
		setLeftHanded(true);
		setEquipmentDropChance(EquipmentSlot.MAINHAND, 0.25f);
		return i;
	}
	
	protected void initGoals(){
		super.initGoals();
		goalSelector.add(5, new WanderAroundFarGoal(this, 1));
		goalSelector.add(6, new LookAtEntityGoal(this, PlayerEntity.class, 8));
		goalSelector.add(6, new LookAroundGoal(this));
		targetSelector.add(1, new RevengeGoal(this));
		targetSelector.add(2, new ActiveTargetGoal<>(this, PlayerEntity.class, true));
	}
	
	// attributes
	
	public static DefaultAttributeContainer.Builder createCrimsonAttributes(){
		return HostileEntity.createHostileAttributes()
				.add(EntityAttributes.GENERIC_ARMOR, 4)
				.add(EntityAttributes.GENERIC_FOLLOW_RANGE, 36);
	}
	
	public boolean canUseRangedWeapon(RangedWeaponItem weapon){
		return weapon instanceof BowItem;
	}
	
	public double getHeightOffset(){
		return -0.6;
	}
	
	// animation - not really used, geckolib is used here primarily for models
	
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