package arcana.items.foci;

import arcana.ArcanaRegistry;
import arcana.aspects.Aspect;
import arcana.aspects.AspectMap;
import arcana.aspects.Aspects;
import arcana.items.FocusItem;
import net.minecraft.entity.effect.StatusEffectInstance;

public class CrystalCapacitorFocusItem extends FocusItem{
	
	public CrystalCapacitorFocusItem(Settings settings){
		super(settings);
	}
	
	public boolean isContinuous(){
		return true;
	}
	
	public void tickContinuousCast(ContinuousCastContext ccc){
		if(ccc.castTime >= 30){
			ccc.stop();
			AspectMap recharge = new AspectMap();
			for(Aspect primal : Aspects.primals)
				recharge.add(primal, ccc.user.world.random.nextBetween(5, 8));
			ccc.recharge(recharge);
			ccc.user.addStatusEffect(new StatusEffectInstance(ArcanaRegistry.ARCANE_DISCHARGE, 30, 0, true, true));
			ccc.focus.damage(1, ccc.user, p -> p.sendToolBreakStatus(ccc.user.getActiveHand()));
		}
	}
}