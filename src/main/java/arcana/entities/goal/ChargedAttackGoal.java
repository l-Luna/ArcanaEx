package arcana.entities.goal;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.goal.Goal;
import net.minecraft.entity.mob.HostileEntity;
import net.minecraft.entity.projectile.ProjectileUtil;
import net.minecraft.item.Items;
import net.minecraft.util.TimeHelper;
import net.minecraft.util.math.intprovider.UniformIntProvider;

import java.util.EnumSet;

// generic adaptation of CrossbowAttackGoal
public class ChargedAttackGoal<E extends HostileEntity> extends Goal{
	
	public enum Stage{
		UNCHARGED,
		CHARGING,
		CHARGED,
		READY_TO_ATTACK;
	}
	
	public static final UniformIntProvider COOLDOWN_RANGE = TimeHelper.betweenSeconds(1, 2);
	
	private final E actor;
	private Stage stage = Stage.UNCHARGED;
	private final double speed;
	private final float squaredRange;
	private int seeingTargetTicker;
	private int chargedTicksLeft;
	private int cooldown;
	
	private final ChargedAttack<E> attack;
	private int chargeTime;
	
	public ChargedAttackGoal(E actor, double speed, float range, ChargedAttack<E> attack){
		this.actor = actor;
		this.speed = speed;
		squaredRange = range*range;
		this.attack = attack;
		setControls(EnumSet.of(Control.MOVE, Control.LOOK));
	}
	
	public boolean canStart(){
		return hasAliveTarget() && canEntityAttack();
	}
	
	private boolean canEntityAttack(){
		return attack.canUse(actor);
	}
	
	public boolean shouldContinue(){
		return hasAliveTarget() && (canStart() || !actor.getNavigation().isIdle()) && canEntityAttack();
	}
	
	private boolean hasAliveTarget(){
		return actor.getTarget() != null && actor.getTarget().isAlive();
	}
	
	public void stop(){
		super.stop();
		actor.setAttacking(false);
		actor.setTarget(null);
		seeingTargetTicker = 0;
		attack.cancel(actor);
		stage = Stage.UNCHARGED;
	}
	
	public boolean shouldRunEveryTick(){
		return true;
	}
	
	public void tick(){
		LivingEntity target = actor.getTarget();
		if(target != null){
			boolean canSee = actor.getVisibilityCache().canSee(target);
			boolean hasSeen = seeingTargetTicker > 0;
			if(canSee != hasSeen)
				seeingTargetTicker = 0;
			
			if(canSee)
				seeingTargetTicker++;
			else
				seeingTargetTicker--;
			
			double dist = actor.squaredDistanceTo(target);
			boolean justNoticed = (dist > squaredRange || seeingTargetTicker < 5) && chargedTicksLeft == 0;
			if(justNoticed){
				cooldown--;
				if(cooldown <= 0){
					actor.getNavigation().startMovingTo(target, isUncharged() ? speed : speed * 0.5);
					cooldown = COOLDOWN_RANGE.get(actor.getRandom());
				}
			}else{
				cooldown = 0;
				actor.getNavigation().stop();
			}
			
			actor.getLookControl().lookAt(target, 30, 30);
			if(stage == Stage.UNCHARGED){
				if(!justNoticed){
					actor.setCurrentHand(ProjectileUtil.getHandPossiblyHolding(actor, Items.CROSSBOW));
					stage = Stage.CHARGING;
					attack.begin(actor);
				}
			}else if(stage == Stage.CHARGING){
				chargeTime++;
				// TODO: cancelling
				/*if(!actor.isUsingItem())
					stage = Stage.UNCHARGED;*/
				
				if(attack.hasFinishedCharging(actor, chargeTime)){
					actor.stopUsingItem();
					stage = Stage.CHARGED;
					chargedTicksLeft = 20 + actor.getRandom().nextInt(20);
					attack.finishCharging(actor);
				}
			}else if(stage == Stage.CHARGED){
				chargedTicksLeft--;
				if(chargedTicksLeft == 0)
					stage = Stage.READY_TO_ATTACK;
			}else if(stage == Stage.READY_TO_ATTACK && canSee){
				attack.shootAt(actor, target, 1);
				stage = Stage.UNCHARGED;
			}
		}
	}
	
	private boolean isUncharged(){
		return stage == Stage.UNCHARGED;
	}
}