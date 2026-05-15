package arcana.api;

import arcana.aspects.Aspect;
import arcana.aspects.AspectMap;
import arcana.aspects.ScaledAspectMap;
import arcana.items.WandItem;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.util.ActionResult;
import org.jetbrains.annotations.Nullable;

/**
 * @see arcana.items.FocusItem
 */
public interface Focus{
	
	// TODO: could be enforced better?
	
	default ScaledAspectMap castCost(@Nullable ItemStack wand, ItemStack focus, PlayerEntity user){
		return new ScaledAspectMap(deciCastCost(wand, focus, user), 0.1f);
	}
	
	AspectMap deciCastCost(@Nullable ItemStack wand, ItemStack focus, PlayerEntity user);
	
	default ActionResult castOnBlock(ItemUsageContext ctx){
		return ActionResult.PASS;
	}
	
	default ActionResult castOnEntity(ItemStack wand, ItemStack focus, PlayerEntity user, LivingEntity target){
		return ActionResult.PASS;
	}
	
	default boolean isContinuous(){
		return false;
	}
	
	default void startContinuousCast(ContinuousCastContext ccc){}
	default void tickContinuousCast(ContinuousCastContext ccc){}
	default void endContinuousCast(ContinuousCastContext ccc){}
	
	final class ContinuousCastContext{
		public final ItemStack wand;
		public final ItemStack focus;
		public final PlayerEntity user;
		public final NbtCompound state;
		public final int castTime;
		
		private boolean stopping = false;
		
		public ContinuousCastContext(ItemStack wand, ItemStack focus, PlayerEntity user, NbtCompound state, int castTime){
			this.wand = wand;
			this.focus = focus;
			this.user = user;
			this.state = state;
			this.castTime = castTime;
		}
		
		public boolean requestDrainDeci(AspectMap required){
			return requestDrain(new ScaledAspectMap(required, 0.1f));
		}
		
		public boolean requestDrain(ScaledAspectMap required){
			if(WandItem.aspectsFrom(wand).contains(required)){
				WandItem.updateAspects(wand, stored -> stored.take(required));
				return true;
			}
			return false;
		}
		
		public void rechargeDeci(AspectMap added){
			recharge(new ScaledAspectMap(added, 0.1f));
		}
		
		public void recharge(ScaledAspectMap added){
			WandItem.updateAspects(wand, stored -> {
				for(Aspect aspect : added.underlying().aspectSet())
					stored.addCapped(aspect, added.get(aspect), WandItem.capacity(wand));
			});
		}
		
		public void stop(){
			stopping = true;
		}
		
		public boolean isStopping(){
			return stopping;
		}
	}
}