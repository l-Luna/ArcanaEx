package arcana.items.foci;

import arcana.items.FocusItem;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.ProjectileUtil;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.stat.Stats;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.RaycastContext;
import net.minecraft.world.World;

public class FetchFocusItem extends FocusItem{
	
	public FetchFocusItem(Settings settings){
		super(settings);
	}
	
	public boolean isContinuous(){
		return true;
	}
	
	public void tickContinuousCast(ContinuousCastContext ccc){
		PlayerEntity user = ccc.user;
		World world = user.world;
		Vec3d from = user.getEyePos();
		Vec3d to = from.add(user.getRotationVector().multiply(40));
		BlockHitResult blockRaycast = world.raycast(new RaycastContext(from, to, RaycastContext.ShapeType.COLLIDER, RaycastContext.FluidHandling.NONE, user));
		// TODO: inflate item entity hitboxes
		EntityHitResult entityRaycast = ProjectileUtil.raycast(user, from, to, new Box(from, to), x -> (x instanceof LivingEntity && !(x instanceof PlayerEntity)) || x instanceof ItemEntity, 40*40);
		
		if(blockRaycast != null && entityRaycast != null){
			if(blockRaycast.getPos().squaredDistanceTo(from) > entityRaycast.getPos().squaredDistanceTo(from))
				blockRaycast = null;
			else
				entityRaycast = null;
		}
		if(entityRaycast != null){
			Entity target = entityRaycast.getEntity();
			if(target instanceof ItemEntity ie)
				collect(user, ie);
			else if(target instanceof LivingEntity le){
				boolean sneaking = user.isSneaking();
				if(sneaking && !(le instanceof PlayerEntity) && world.getTime() % 5 == 0 && world.random.nextInt(8) == 0){
					var slots = EquipmentSlot.values();
					var slot = slots[world.random.nextInt(slots.length)];
					ItemStack equipped = le.getEquippedStack(slot);
					if(!equipped.isEmpty()){
						le.equipStack(slot, ItemStack.EMPTY);
						user.giveItemStack(equipped);
						// TODO: animate items being taken
						// (note that sendPickup only sends the ID of an item entity, which doesn't exist in this case)
					}
				}
				le.setVelocity(le.getVelocity().add(user.getPos().subtract(le.getPos()).normalize().multiply(sneaking ? 0.05 : 0.1)));
			}
		}
		if(blockRaycast != null){
			// TODO: harvest crops
		}
	}
	
	// from ItemEntity.onPlayerCollision
	private static void collect(PlayerEntity player, ItemEntity entity){
		if(!player.world.isClient){
			ItemStack stack = entity.getStack();
			Item oldItem = stack.getItem();
			int oldCount = stack.getCount();
			if(player.getInventory().insertStack(stack)){
				player.sendPickup(entity, oldCount);
				if(stack.isEmpty()){
					entity.discard();
					stack.setCount(oldCount);
				}
				
				player.increaseStat(Stats.PICKED_UP.getOrCreateStat(oldItem), oldCount);
				player.triggerItemPickedUpByEntityCriteria(entity);
			}
		}
	}
}