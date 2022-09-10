package arcana.blocks;

import arcana.components.KdItem;
import net.minecraft.block.BlockState;
import net.minecraft.block.DispenserBlock;
import net.minecraft.block.dispenser.DispenserBehavior;
import net.minecraft.block.dispenser.ItemDispenserBehavior;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.DispenserBlockEntity;
import net.minecraft.block.entity.HopperBlockEntity;
import net.minecraft.entity.ItemEntity;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.*;
import net.minecraft.world.World;
import net.minecraft.world.WorldEvents;

public class KnowledgeableDropperBlock extends DispenserBlock{
	
	private static final DispenserBehavior BEHAVIOR = new Behaviour();
	
	public KnowledgeableDropperBlock(Settings settings){
		super(settings);
	}
	
	// from DropperBlock
	protected void dispense(ServerWorld world, BlockPos pos){
		BlockPointerImpl pointer = new BlockPointerImpl(world, pos);
		DispenserBlockEntity be = pointer.getBlockEntity();
		int i = be.chooseNonEmptySlot(world.random);
		if(i < 0)
			world.syncWorldEvent(WorldEvents.DISPENSER_FAILS, pos, 0);
		else{
			ItemStack stack = be.getStack(i);
			if(!stack.isEmpty()){
				Direction direction = world.getBlockState(pos).get(FACING);
				Inventory inventory = HopperBlockEntity.getInventoryAt(world, pos.offset(direction));
				ItemStack itemStack2;
				if(inventory == null)
					itemStack2 = BEHAVIOR.dispense(pointer, stack);
				else{
					itemStack2 = HopperBlockEntity.transfer(be, inventory, stack.copy().split(1), direction.getOpposite());
					if(itemStack2.isEmpty()){
						itemStack2 = stack.copy();
						itemStack2.decrement(1);
					}else
						itemStack2 = stack.copy();
				}
				
				be.setStack(i, itemStack2);
			}
		}
	}
	
	protected DispenserBehavior getBehaviorForItem(ItemStack stack){
		return BEHAVIOR;
	}
	
	public BlockEntity createBlockEntity(BlockPos pos, BlockState state){
		return new KnowledgeableDropperBlockEntity(pos, state);
	}
	
	public static class Behaviour extends ItemDispenserBehavior{
		
		// from ItemDispenserBehavior
		// reduce variance & attach source position
		protected ItemStack dispenseSilently(BlockPointer pointer, ItemStack stack){
			Direction direction = pointer.getBlockState().get(DispenserBlock.FACING);
			Position position = DispenserBlock.getOutputLocation(pointer);
			ItemStack itemStack = stack.split(1);
			spawnItem(pointer, itemStack, 6, direction, position);
			return stack;
		}
		
		public static void spawnItem(BlockPointer from, ItemStack stack, int speed, Direction side, Position pos){
			World world = from.getWorld();
			double x = pos.getX();
			double y = pos.getY();
			double z = pos.getZ();
			if(side.getAxis() == Direction.Axis.Y)
				y -= 0.125;
			else
				y -= 0.15625;
			
			ItemEntity entity = new ItemEntity(world, x, y, z, stack);
			double rand = world.random.nextDouble() * 0.07 + 0.1;
			entity.setVelocity(
					world.random.nextTriangular((double)side.getOffsetX() * rand, 0.005 * (double)speed),
					world.random.nextTriangular(0.2, 0.017 * (double)speed),
					world.random.nextTriangular((double)side.getOffsetZ() * rand, 0.005 * (double)speed)
			);
			KdItem.setSource(entity, from.getPos());
			world.spawnEntity(entity);
		}
	}
}