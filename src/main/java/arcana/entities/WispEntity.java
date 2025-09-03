package arcana.entities;

import net.minecraft.entity.EntityType;
import net.minecraft.entity.attribute.DefaultAttributeContainer;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.mob.Angerable;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

public class WispEntity extends WispLikeEntity implements Angerable{
	
	// where the spawning node is
	private BlockPos anchor;
	
	// for angerable
	private int angerTime;
	private @Nullable UUID angryAt;
	
	public WispEntity(EntityType<? extends WispLikeEntity> entityType, World world){
		super(entityType, world);
	}
	
	public static DefaultAttributeContainer.Builder createDefaultAttributes(){
		return MobEntity.createMobAttributes()
				.add(EntityAttributes.GENERIC_MAX_HEALTH, 20)
				.add(EntityAttributes.GENERIC_FLYING_SPEED, 1)
				.add(EntityAttributes.GENERIC_MOVEMENT_SPEED, 1)
				.add(EntityAttributes.GENERIC_ATTACK_DAMAGE, 4)
				.add(EntityAttributes.GENERIC_FOLLOW_RANGE, 60);
	}
	
	public void setAnchorPos(BlockPos anchor){
		this.anchor = anchor;
	}
	
	Vec3d anchor(){
		return anchor != null ? Vec3d.ofCenter(anchor) : getPos();
	}
	
	public void writeCustomDataToNbt(NbtCompound nbt){
		super.writeCustomDataToNbt(nbt);
		writeAngerToNbt(nbt);
	}
	
	public void readCustomDataFromNbt(NbtCompound nbt){
		super.readCustomDataFromNbt(nbt);
		readAngerFromNbt(world, nbt);
	}
	
	// for angerable
	
	public int getAngerTime(){
		return angerTime;
	}
	
	public void setAngerTime(int angerTime){
		this.angerTime = angerTime;
	}
	
	public @Nullable UUID getAngryAt(){
		return angryAt;
	}
	
	public void setAngryAt(@Nullable UUID angryAt){
		this.angryAt = angryAt;
	}
	
	public void chooseRandomAngerTime(){
		setAngerTime(world.random.nextBetween(20 * 25, 20 * 40));
	}
}