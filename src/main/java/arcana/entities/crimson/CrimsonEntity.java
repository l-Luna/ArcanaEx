package arcana.entities.crimson;

import arcana.ArcanaRegistry;
import net.minecraft.entity.EntityDimensions;
import net.minecraft.entity.EntityGroup;
import net.minecraft.entity.EntityPose;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.ai.goal.*;
import net.minecraft.entity.attribute.DefaultAttributeContainer;
import net.minecraft.entity.mob.HostileEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Items;
import net.minecraft.item.RangedWeaponItem;
import net.minecraft.world.World;
import software.bernie.geckolib3.core.IAnimatable;
import software.bernie.geckolib3.core.PlayState;
import software.bernie.geckolib3.core.builder.AnimationBuilder;
import software.bernie.geckolib3.core.controller.AnimationController;
import software.bernie.geckolib3.core.manager.AnimationData;
import software.bernie.geckolib3.core.manager.AnimationFactory;
import software.bernie.geckolib3.util.GeckoLibUtil;

public class CrimsonEntity extends HostileEntity implements IAnimatable{
	
	protected static final AnimationBuilder idleAnim = new AnimationBuilder().addAnimation("idle");
	private final AnimationFactory animFactory = GeckoLibUtil.createFactory(this);
	
	public CrimsonEntity(EntityType<? extends HostileEntity> entityType, World world){
		super(entityType, world);
	}
	
	// setup
	
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
		return HostileEntity.createHostileAttributes() /* .add(...) */;
	}
	
	public EntityGroup getGroup(){
		return ArcanaRegistry.CRIMSON_GROUP;
	}
	
	public boolean canUseRangedWeapon(RangedWeaponItem weapon){
		return weapon == Items.BOW;
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