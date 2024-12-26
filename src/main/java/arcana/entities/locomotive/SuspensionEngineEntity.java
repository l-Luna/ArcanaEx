package arcana.entities.locomotive;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.entity.vehicle.BoatEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.Packet;
import net.minecraft.network.packet.s2c.play.EntitySpawnS2CPacket;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;
import software.bernie.geckolib3.core.IAnimatable;
import software.bernie.geckolib3.core.PlayState;
import software.bernie.geckolib3.core.builder.AnimationBuilder;
import software.bernie.geckolib3.core.controller.AnimationController;
import software.bernie.geckolib3.core.manager.AnimationData;
import software.bernie.geckolib3.core.manager.AnimationFactory;
import software.bernie.geckolib3.util.GeckoLibUtil;

public class SuspensionEngineEntity extends Entity implements IAnimatable{
	
	private static final TrackedData<Direction>
			direction = DataTracker.registerData(SuspensionEngineEntity.class, TrackedDataHandlerRegistry.FACING),
			targetDirection = DataTracker.registerData(SuspensionEngineEntity.class, TrackedDataHandlerRegistry.FACING);
	private static final TrackedData<BlockPos>
			location = DataTracker.registerData(SuspensionEngineEntity.class, TrackedDataHandlerRegistry.BLOCK_POS),
			targetLocation = DataTracker.registerData(SuspensionEngineEntity.class, TrackedDataHandlerRegistry.BLOCK_POS);
	private static final TrackedData<Float> movementTimer = DataTracker.registerData(SuspensionEngineEntity.class, TrackedDataHandlerRegistry.FLOAT);
	
	protected static final AnimationBuilder floatAnim = new AnimationBuilder().addAnimation("engine.float");
	private final AnimationFactory animFactory = GeckoLibUtil.createFactory(this);
	
	public SuspensionEngineEntity(EntityType<?> type, World world){
		super(type, world);
	}
	
	// behaviour
	
	public void tick(){
		super.tick();
		
		// fix invalid states
		if(getDirection().getAxis().isVertical()){
			setDirection(Direction.NORTH);
			setTargetDirection(Direction.NORTH);
		}
		// TODO: properly initialize `here`; it's possible that a faster train might want this further out anyways?
		if(!getBlockPos().isWithinDistance(getLocation(), 4)){
			setLocation(getBlockPos());
			setTargetLocation(getBlockPos());
		}
		
		// look the way you travel
		setYaw(getDirection().getRotationQuaternion().toEulerXyzDegrees().getY() + 180);
		
		// decide on target location
		setTargetLocation(getLocation().offset(getTargetDirection()));
		
		// process symbols
		
		/*
		// move with speed
		move(MovementType.SELF, getVelocity());
		
		// accelerate towards the right direction
		Vec3d targetVelocity = new Vec3d(getDirection().getUnitVector()).multiply(0.4);
		
		// stop against blocks
		boolean cont = world.isSpaceEmpty(this, getBoundingBox().offset(new BlockPos(getDirection().getVector())));
		
		// process symbols - they might ask us to stop
		BlockPos pos = getBlockPos();
		BlockState stateHere = world.getBlockState(pos);
		if(stateHere.getBlock() instanceof SymbolBlock sb && !world.isReceivingRedstonePower(pos))
			cont &= sb.getSymbol().action().consider(world, this, stateHere, pos);
		
		if(cont){
			// accelerate slowly in the direction of travel, decelerate quickly in other directions
			// for(comp c) if t_c != 0 lerp(0.02) else lerp(0.5) ???
			setVelocity(getVelocity().lerp(targetVelocity, 0.5));
		}else{
			// slow down quickly
			setVelocity(getVelocity().lerp(Vec3d.ZERO, 0.4));
		}*/
	}
	
	public boolean collidesWith(Entity other) {
		return BoatEntity.canCollide(this, other);
	}
	
	/*public boolean isPushable() {
		return true;
	}*/
	
	public boolean canHit(){
		return true;
	}
	
	// attributes
	
	public Direction getDirection(){
		return dataTracker.get(direction);
	}
	
	public void setDirection(Direction to){
		dataTracker.set(direction, to);
	}
	
	public BlockPos getLocation(){
		return dataTracker.get(location);
	}
	
	public void setLocation(BlockPos to){
		dataTracker.set(location, to);
	}
	
	public Direction getTargetDirection(){
		return dataTracker.get(targetDirection);
	}
	
	public void setTargetDirection(Direction to){
		dataTracker.set(targetDirection, to);
	}
	
	public BlockPos getTargetLocation(){
		return dataTracker.get(targetLocation);
	}
	
	public void setTargetLocation(BlockPos to){
		dataTracker.set(targetLocation, to);
	}
	
	public float getMovementTimer(){
		return dataTracker.get(movementTimer);
	}
	
	public void setMovementTimer(float to){
		dataTracker.set(movementTimer, to);
	}
	
	protected void writeCustomDataToNbt(NbtCompound nbt){
		nbt.putInt("direction", getDirection().getId());
		nbt.putInt("targetDirection", getTargetDirection().getId());
		BlockPos location = getLocation();
		nbt.putInt("locationX", location.getX());
		nbt.putInt("locationY", location.getY());
		nbt.putInt("locationZ", location.getZ());
		BlockPos targetLocation = getTargetLocation();
		nbt.putInt("targetLocationX", targetLocation.getX());
		nbt.putInt("targetLocationY", targetLocation.getY());
		nbt.putInt("targetLocationZ", targetLocation.getZ());
		nbt.putFloat("movementTimer", getMovementTimer());
	}
	
	protected void readCustomDataFromNbt(NbtCompound nbt){
		setDirection(Direction.byId(nbt.getInt("direction")));
		setTargetDirection(Direction.byId(nbt.getInt("targetDirection")));
		setLocation(new BlockPos(nbt.getInt("locationX"), nbt.getInt("locationY"), nbt.getInt("locationZ")));
		setTargetLocation(new BlockPos(nbt.getInt("targetLocationX"), nbt.getInt("targetLocation"), nbt.getInt("targetLocationZ")));
		setMovementTimer(nbt.getFloat("movementTimer"));
	}
	
	protected void initDataTracker(){
		dataTracker.startTracking(direction, Direction.NORTH);
		dataTracker.startTracking(targetDirection, Direction.NORTH);
		dataTracker.startTracking(location, BlockPos.ORIGIN);
		dataTracker.startTracking(targetLocation, BlockPos.ORIGIN);
		dataTracker.startTracking(movementTimer, 0f);
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