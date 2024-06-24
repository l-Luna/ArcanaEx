package arcana.mixin;

import arcana.blocks.WardedCampfireBlock;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.TargetPredicate;
import net.minecraft.entity.ai.goal.ActiveTargetGoal;
import net.minecraft.entity.mob.MobEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.function.Predicate;

@Mixin(ActiveTargetGoal.class)
public class ActiveTargetGoalMixin{
	
	@Shadow protected TargetPredicate targetPredicate;
	
	@Inject(method = "<init>(Lnet/minecraft/entity/mob/MobEntity;Ljava/lang/Class;IZZLjava/util/function/Predicate;)V",
	        at = @At("TAIL"))
	private void adjustTargetPredicate(MobEntity _m,
	                                   Class<?> _tc,
	                                   int _rc,
	                                   boolean _cv,
	                                   boolean _ccn,
	                                   Predicate<LivingEntity> tp,
	                                   CallbackInfo ci){
		Predicate<LivingEntity> prot = ActiveTargetGoalMixin::isNotWarded;
		if(tp == null)
			tp = prot;
		else tp = tp.and(prot);
		targetPredicate.setPredicate(tp);
	}
	
	@Unique
	private static boolean isNotWarded(LivingEntity e){
		return e == null || !WardedCampfireBlock.isProtected(e);
	}
}