package arcana.entities.locomotive;

import arcana.blocks.SymbolBlock;
import net.minecraft.block.BlockState;
import net.minecraft.state.property.Properties;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;

import java.util.ArrayList;
import java.util.List;

public record Symbol(String name, boolean continuous, SymbolAction action){
	
	public interface SymbolAction{
		boolean consider(World world, SuspensionEngineEntity carriage, BlockState symbolState, BlockPos symbolPos);
	}
	
	public static Symbol BLANK = new Symbol("blank", false, (world, carriage, symbolState, symbolPos) -> true);
	// tell isn't actually a symbol
	
	public static Symbol HOLD = new Symbol("hold", true, (world, carriage, symbolState, symbolPos) -> false);
	// TODO
	public static Symbol FUEL = new Symbol("fuel", true, (world, carriage, symbolState, symbolPos) -> false);
	public static Symbol LOAD = new Symbol("load", true, (world, carriage, symbolState, symbolPos) -> false);
	public static Symbol UNLOAD = new Symbol("unload", true, (world, carriage, symbolState, symbolPos) -> false);
	
	public static Symbol FOLLOW_ME = new Symbol("follow_me", false, (world, carriage, symbolState, symbolPos) -> {
		if(symbolState.getProperties().contains(Properties.HORIZONTAL_FACING)){
			Direction target = symbolState.get(Properties.HORIZONTAL_FACING);
			if(target != carriage.getDirection().getOpposite())
				carriage.setDirection(target);
		}
		return true;
	});
	public static Symbol TURN_LEFT = new Symbol("turn_left", false, (world, carriage, symbolState, symbolPos) -> {
		carriage.setDirection(carriage.getDirection().rotateYCounterclockwise());
		return true;
	});
	public static Symbol TURN_RIGHT = new Symbol("turn_right", false, (world, carriage, symbolState, symbolPos) -> {
		carriage.setDirection(carriage.getDirection().rotateYClockwise());
		return true;
	});
	public static Symbol SPLIT = new Symbol("split", false, (world, carriage, symbolState, symbolPos) -> {
		if(world.isClient)
			return true;
		Direction d = carriage.getDirection();
		carriage.setDirection(world.getRandom().nextBoolean() ? d.rotateYCounterclockwise() : d.rotateYClockwise());
		return true;
	});
	
	// TODO
	public static Symbol DRIFT = new Symbol("drift", false, (world, carriage, symbolState, symbolPos) -> true);
	public static Symbol SYNCHRONIZE = new Symbol("synchronize", false, (world, carriage, symbolState, symbolPos) -> true);
	
	public static Symbol GRAB = new Symbol("grab", false, (world, carriage, symbolState, symbolPos) -> true);
	public static Symbol EJECT = new Symbol("eject", false, (world, carriage, symbolState, symbolPos) -> true);
	
	public static List<Symbol> all = List.of(
			BLANK,
			FOLLOW_ME, TURN_LEFT, TURN_RIGHT, SPLIT,
			HOLD, FUEL, LOAD, UNLOAD,
			DRIFT, SYNCHRONIZE,
			GRAB, EJECT
	);
	
	public static final List<SymbolBlock> blocks = new ArrayList<>();
}