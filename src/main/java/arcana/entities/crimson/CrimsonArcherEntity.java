package arcana.entities.crimson;

import net.minecraft.entity.*;
import net.minecraft.entity.ai.RangedAttackMob;
import net.minecraft.entity.ai.goal.BowAttackGoal;
import net.minecraft.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.entity.attribute.DefaultAttributeContainer;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.mob.HostileEntity;
import net.minecraft.entity.projectile.PersistentProjectileEntity;
import net.minecraft.entity.projectile.ProjectileUtil;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.LocalDifficulty;
import net.minecraft.world.ServerWorldAccess;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

public class CrimsonArcherEntity extends CrimsonEntity implements RangedAttackMob{
	
	private final BowAttackGoal<CrimsonArcherEntity> bowAttackGoal = new BowAttackGoal<>(this, 1, 30, 15);
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
		equipStack(EquipmentSlot.MAINHAND, new ItemStack(Items.BOW));
	}
	
	public EntityData initialize(ServerWorldAccess world, LocalDifficulty difficulty, SpawnReason spawnReason, @Nullable EntityData entityData, @Nullable NbtCompound entityNbt){
		EntityData i = super.initialize(world, difficulty, spawnReason, entityData, entityNbt);
		Random rng = world.getRandom();
		initEquipment(rng, difficulty);
		updateEnchantments(random, difficulty);
		updateAttackType();
		return i;
	}
	
	// "if you don't have a bow, don't try to use it"
	public void updateAttackType(){
		if(world != null && !world.isClient){
			goalSelector.remove(meleeAttackGoal);
			goalSelector.remove(bowAttackGoal);
			ItemStack itemStack = getStackInHand(ProjectileUtil.getHandPossiblyHolding(this, Items.BOW));
			if(itemStack.isOf(Items.BOW))
				goalSelector.add(4, bowAttackGoal);
			else
				goalSelector.add(4, meleeAttackGoal);
		}
	}
	
	// attributes
	
	public static DefaultAttributeContainer.Builder createArcherAttributes(){
		return CrimsonEntity.createCrimsonAttributes().add(EntityAttributes.GENERIC_MOVEMENT_SPEED, 0.30);
	}
	
	// behaviour
	
	public void attack(LivingEntity target, float pullProgress){
		ItemStack itemStack = getArrowType(getStackInHand(ProjectileUtil.getHandPossiblyHolding(this, Items.BOW)));
		PersistentProjectileEntity arrow = createArrowProjectile(itemStack, pullProgress);
		double diffX = target.getX() - getX();
		double diffY = target.getBodyY(0.3333333333333333) - arrow.getY();
		double diffZ = target.getZ() - getZ();
		double dist = Math.sqrt(diffX * diffX + diffZ * diffZ);
		arrow.setVelocity(diffX, diffY + dist * 0.2f, diffZ, 1.6f, 14f - world.getDifficulty().getId() * 4);
		playSound(SoundEvents.ENTITY_SKELETON_SHOOT, 1, 1 / (getRandom().nextFloat() * 0.4f + 0.8f));
		world.spawnEntity(arrow);
	}
	
	protected PersistentProjectileEntity createArrowProjectile(ItemStack arrow, float damageModifier){
		return ProjectileUtil.createArrowProjectile(this, arrow, damageModifier);
	}
	
	public void equipStack(EquipmentSlot slot, ItemStack stack){
		super.equipStack(slot, stack);
		if(!world.isClient)
			updateAttackType();
	}
	
	// serialization
	
	public void readCustomDataFromNbt(NbtCompound nbt){
		super.readCustomDataFromNbt(nbt);
		updateAttackType();
	}
}