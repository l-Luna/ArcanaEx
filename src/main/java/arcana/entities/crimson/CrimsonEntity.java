package arcana.entities.crimson;

import arcana.ArcanaRegistry;
import net.minecraft.entity.*;
import net.minecraft.entity.ai.goal.*;
import net.minecraft.entity.attribute.DefaultAttributeContainer;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.mob.HostileEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.BowItem;
import net.minecraft.item.RangedWeaponItem;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.world.LocalDifficulty;
import net.minecraft.world.ServerWorldAccess;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;
import software.bernie.geckolib.util.GeckoLibUtil;

public class CrimsonEntity extends HostileEntity implements IAnimatable{
	
	protected static final AnimationBuilder idleAnim = new AnimationBuilder().addAnimation("idle");
	private final AnimationFactory animFactory = GeckoLibUtil.createFactory(this);
	
	public CrimsonEntity(EntityType<? extends HostileEntity> entityType, World world){
		super(entityType, world);
	}
	
	// setup
	
	public EntityData initialize(ServerWorldAccess world, LocalDifficulty difficulty, SpawnReason spawnReason, @Nullable EntityData entityData, @Nullable NbtCompound entityNbt){
		EntityData i = super.initialize(world, difficulty, spawnReason, entityData, entityNbt);
		initEquipment(world.getRandom(), difficulty);
		updateEnchantments(random, difficulty);
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
	
	public EntityGroup getGroup(){
		return ArcanaRegistry.CRIMSON_GROUP;
	}
	
	public boolean canUseRangedWeapon(RangedWeaponItem weapon){
		return weapon instanceof BowItem;
	}
	
	protected float getActiveEyeHeight(EntityPose pose, EntityDimensions dimensions){
		return 1.74f;
	}
	
	public double getHeightOffset(){
		return -0.6;
	}
	
	// animation - not really used, geckolib is used here primarily for models
	
	public void registerControllers(AnimationData data){
		data.addAnimationController(new AnimationController<>(this, "idle", 20, event -> {
			event.getController().setAnimation(idleAnim);
			return PlayState.CONTINUE;
		}));
	}
	
	public AnimationFactory getFactory(){
		return animFactory;
	}
}