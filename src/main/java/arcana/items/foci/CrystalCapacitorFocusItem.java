package arcana.items.foci;

import arcana.ArcanaRegistry;
import arcana.aspects.Aspect;
import arcana.aspects.AspectMap;
import arcana.aspects.Aspects;
import arcana.items.FocusItem;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.sound.SoundEvents;
import org.jetbrains.annotations.Nullable;

public class CrystalCapacitorFocusItem extends FocusItem{
	
	public CrystalCapacitorFocusItem(Settings settings){
		super(settings);
	}
	
	public AspectMap deciCastCost(@Nullable ItemStack wand, ItemStack focus, PlayerEntity user){
		return new AspectMap();
	}
	
	public boolean isContinuous(){
		return true;
	}
	
	public void tickContinuousCast(ContinuousCastContext ccc){
		if(ccc.castTime >= 30){
			ccc.stop();
			AspectMap recharge = new AspectMap();
			for(Aspect primal : Aspects.primals)
				recharge.add(primal, ccc.user.getWorld().random.nextBetween(50, 88));
			ccc.user.playSound(SoundEvents.BLOCK_AMETHYST_BLOCK_BREAK, 0.7f, 1.2f);
			ccc.rechargeDeci(recharge);
			ccc.user.addStatusEffect(new StatusEffectInstance(ArcanaRegistry.ARCANE_DISCHARGE.entry(), 30, 0, true, true));
			ccc.focus.damage(1, ccc.user, LivingEntity.getSlotForHand(ccc.user.getActiveHand()));
		}
	}
}