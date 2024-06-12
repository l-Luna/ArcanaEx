package arcana.entities.locomotive;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.MovementType;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.entity.vehicle.BoatEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.Packet;
import net.minecraft.network.packet.s2c.play.EntitySpawnS2CPacket;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import software.bernie.geckolib3.core.IAnimatable;
import software.bernie.geckolib3.core.PlayState;
import software.bernie.geckolib3.core.builder.AnimationBuilder;
import software.bernie.geckolib3.core.controller.AnimationController;
import software.bernie.geckolib3.core.manager.AnimationData;
import software.bernie.geckolib3.core.manager.AnimationFactory;
import software.bernie.geckolib3.util.GeckoLibUtil;

public class SuspensionEngineEntity extends Entity implements IAnimatable{
	
	private static final TrackedData<Direction> direction = DataTracker.registerData(SuspensionEngineEntity.class, TrackedDataHandlerRegistry.FACING);
	
	protected static final AnimationBuilder floatAnim = new AnimationBuilder().addAnimation("engine.float");
	private final AnimationFactory animFactory = GeckoLibUtil.createFactory(this);
	
	public SuspensionEngineEntity(EntityType<?> type, World world){
		super(type, world);
	}
	
	// behaviour
	
	public void tick(){
		super.tick();
		
		// why does it always start down??
		if(getDirection().getAxis().isVertical())
			setDirection(Direction.NORTH);
		
		// look the way you travel
		setYaw(getDirection().getRotationQuaternion().toEulerXyzDegrees().getY() + 180);
		
		// move with speed
		move(MovementType.SELF, getVelocity());
		
		// accelerate towards the right direction
		Vec3d targetVelocity = new Vec3d(getDirection().getUnitVector()).multiply(0.4);
		
		// speed up slowly, stop against blocks
		boolean empty = world.isSpaceEmpty(this, getBoundingBox().offset(new BlockPos(getDirection().getVector())));
		setVelocity(empty ? getVelocity().lerp(targetVelocity, 0.02) : getVelocity().lerp(Vec3d.ZERO, 0.3));
	}
	
	public boolean collidesWith(Entity other) {
		return BoatEntity.canCollide(this, other);
	}
	
	public boolean isPushable() {
		return true;
	}
	
	
	// attributes
	
	public Direction getDirection(){
		return dataTracker.get(direction);
	}
	
	public void setDirection(Direction to){
		dataTracker.set(direction, to);
	}
	
	protected void writeCustomDataToNbt(NbtCompound nbt){
		if(nbt.contains("direction"))
			nbt.putInt("direction", getDirection().getId());
	}
	
	protected void readCustomDataFromNbt(NbtCompound nbt){
		setDirection(Direction.byId(nbt.getInt("direction")));
	}
	
	protected void initDataTracker(){
		dataTracker.startTracking(direction, Direction.NORTH);
	}
	
	// networking
	
	public Packet<?> createSpawnPacket(){
		return new EntitySpawnS2CPacket(this);
	}
	
	// animation
	
	public void registerControllers(AnimationData data){
		data.addAnimationController(new AnimationController<>(this, "float", 20, event -> {
			event.getController().setAnimation(floatAnim);
			return PlayState.CONTINUE;
		}));
	}
	
	public AnimationFactory getFactory(){
		return animFactory;
	}
}