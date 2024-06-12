package arcana.entities.locomotive;

import net.minecraft.block.BlockState;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;

public record LocomotiveSymbol(String name, boolean continuous, SymbolAction action){
	
	public interface SymbolAction{
		boolean consider(ServerWorld world, SuspensionEngineEntity carriage, BlockState symbolState, BlockPos symbolPos);
	}
	
	public static LocomotiveSymbol HOLD = new LocomotiveSymbol("hold", true, (world, carriage, symbolState, symbolPos) ->
			world.isReceivingRedstonePower(symbolPos));
	public static LocomotiveSymbol TURN_LEFT = new LocomotiveSymbol("turn_left", false, (world, carriage, symbolState, symbolPos) -> {
		carriage.setDirection(carriage.getDirection().rotateYCounterclockwise());
		return true;
	});
}