package arcana.duck;

import net.minecraft.util.math.Direction;

// for ensuring that only one arcane levitator pushes an entity at a time
public interface ArcanaPushableEntity{

	boolean arcana$wasPushed(Direction direction);
	void arcana$setWasPushed(Direction direction, boolean value);
}