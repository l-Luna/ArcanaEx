package arcana.mixin;

import arcana.duck.ArcanaPushableEntity;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.Direction;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.EnumSet;

@Mixin(Entity.class)
public class EntityMixin implements ArcanaPushableEntity{
	
	@Unique
	private final EnumSet<Direction> pushedDirections = EnumSet.noneOf(Direction.class);
	
	public boolean arcana$wasPushed(Direction direction){
		return pushedDirections.contains(direction);
	}
	
	public void arcana$setWasPushed(Direction direction, boolean value){
		if(value)
			pushedDirections.add(direction);
		else
			pushedDirections.remove(direction);
	}
	
	@Inject(method = "baseTick", at = @At("HEAD"))
	void baseTick(CallbackInfo ci){
		pushedDirections.clear();
	}
}