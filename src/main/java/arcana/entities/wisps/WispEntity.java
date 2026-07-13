package arcana.entities.wisps;

import arcana.ArcanaRegistry;
import arcana.ArcanaTags;
import arcana.api.ScalpelSlashable;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.goal.ActiveTargetGoal;
import net.minecraft.entity.attribute.DefaultAttributeContainer;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.mob.Angerable;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.tag.DamageTypeTags;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

public class WispEntity extends WispLikeEntity implements Angerable, ScalpelSlashable{
	
	// where the spawning node is
	private BlockPos anchor;
	
	// for angerable
	private int angerTime;
	private @Nullable UUID angryAt;
	
	public WispEntity(EntityType<? extends WispEntity> entityType, World world){
		super(entityType, world);
	}
	
	public WispEntity(World world){
		this(ArcanaRegistry.WISP, world);
	}
	
	protected void initGoals(){
		super.initGoals();
		targetSelector.add(4, new ActiveTargetGoal<>(this, PlayerEntity.class, 10, false, false, this::shouldAngerAt));
	}
	
	public static DefaultAttributeContainer.Builder createDefaultAttributes(){
		return MobEntity.createMobAttributes()
				.add(EntityAttributes.GENERIC_MAX_HEALTH, 16)
				.add(EntityAttributes.GENERIC_FLYING_SPEED, 1)
				.add(EntityAttributes.GENERIC_MOVEMENT_SPEED, 1)
				.add(EntityAttributes.GENERIC_ATTACK_DAMAGE, 3)
				.add(EntityAttributes.GENERIC_FOLLOW_RANGE, 60);
	}
	
	public static DefaultAttributeContainer.Builder createLesserAttributes(){
		return MobEntity.createMobAttributes()
				.add(EntityAttributes.GENERIC_MAX_HEALTH, 8)
				.add(EntityAttributes.GENERIC_FLYING_SPEED, 0.7f)
				.add(EntityAttributes.GENERIC_MOVEMENT_SPEED, 0.7f)
				.add(EntityAttributes.GENERIC_ATTACK_DAMAGE, 2)
				.add(EntityAttributes.GENERIC_FOLLOW_RANGE, 40);
	}
	
	public void setAnchorPos(BlockPos anchor){
		this.anchor = anchor;
	}
	
	public Vec3d anchor(){
		return Vec3d.ofCenter(anchor);
	}
	
	public void tick(){
		if(anchor == null)
			anchor = getBlockPos();
		super.tick();
		if(!getWorld().isClient)
			tickAngerLogic((ServerWorld)getWorld(), true);
	}
	
	public boolean handleAttack(Entity attacker){
		return !((attacker instanceof LivingEntity le && le.getMainHandStack().isIn(ArcanaTags.WISP_ATTACK_WHITELIST)) || attacker instanceof WispLikeEntity);
	}
	
	public boolean canHit(){
		// false by default here, but actually handled in GameRendererMixin
		return false;
	}
	
	public boolean isInvulnerableTo(DamageSource eds){
		if(eds.isIn(DamageTypeTags.BYPASSES_INVULNERABILITY))
			return false;
		// TODO: use custom damage type for reifying damage
		ItemStack stack = eds.getWeaponStack();
		return !(eds.getAttacker() instanceof WispLikeEntity || stack != null && stack.isIn(ArcanaTags.WISP_ATTACK_WHITELIST));
	}
	
	// start items with no velocity
	public @Nullable ItemEntity dropStack(ItemStack stack, float yOffset){
		if(stack.isEmpty())
			return null;
		else if(getWorld().isClient)
			return null;
		else{
			ItemEntity entity = new ItemEntity(getWorld(), getX(), getY() + yOffset, getZ(), stack, 0, 0, 0);
			entity.setToDefaultPickupDelay();
			getWorld().spawnEntity(entity);
			return entity;
		}
	}
	
	public void writeCustomDataToNbt(NbtCompound nbt){
		super.writeCustomDataToNbt(nbt);
		writeAngerToNbt(nbt);
		nbt.putLong("anchor", anchor.asLong());
	}
	
	public void readCustomDataFromNbt(NbtCompound nbt){
		super.readCustomDataFromNbt(nbt);
		readAngerFromNbt(getWorld(), nbt);
		anchor = nbt.contains("anchor") ? BlockPos.fromLong(nbt.getLong("anchor")) : null;
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
		setAngerTime(getWorld().random.nextBetween(20 * 25, 20 * 40));
	}
	
	public void onScalpelSlash(World world, PlayerEntity user, BlockPos pos){
		kill();
	}
}