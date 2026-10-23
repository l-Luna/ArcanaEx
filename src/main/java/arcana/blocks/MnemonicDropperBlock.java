package arcana.blocks;

import arcana.ArcanaRegistry;
import arcana.blocks.be.MnemonicDropperBlockEntity;
import com.mojang.serialization.MapCodec;
import net.minecraft.block.BlockState;
import net.minecraft.block.DispenserBlock;
import net.minecraft.block.dispenser.DispenserBehavior;
import net.minecraft.block.dispenser.ItemDispenserBehavior;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.HopperBlockEntity;
import net.minecraft.entity.ItemEntity;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPointer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Position;
import net.minecraft.world.World;
import net.minecraft.world.WorldEvents;

public class MnemonicDropperBlock extends DispenserBlock{
	
	private static final MapCodec<MnemonicDropperBlock> CODEC = createCodec(MnemonicDropperBlock::new);
	
	private static final DispenserBehavior BEHAVIOR = new Behaviour();
	
	public MnemonicDropperBlock(Settings settings){
		super(settings);
	}
	
	public MapCodec<? extends DispenserBlock> getCodec(){
		return CODEC;
	}
	
	// from DropperBlock
	protected void dispense(ServerWorld world, BlockState state, BlockPos pos){
		MnemonicDropperBlockEntity be = world.getBlockEntity(pos, ArcanaRegistry.MNEMONIC_DROPPER_BE).orElseThrow();
		BlockPointer pointer = new BlockPointer(world, pos, state, be);
		int oldIdx = be.getDropIndex();
		int idx = be.getDropIndex();
		int count = be.getDropCount(idx);
		// skip to the first non-0 slot, fail if revisiting original slot
		while(count == 0){
			idx = (idx + 1) % 9;
			if(idx == oldIdx){
				world.syncWorldEvent(WorldEvents.DISPENSER_FAILS, pos, 0);
				return;
			}
			count = be.getDropCount(idx);
		}
		// ensure that the right count is used when dispensing
		be.setDropIndex(idx);
		ItemStack stack = be.getStack(idx);
		// then attempt to dispense
		if(!stack.isEmpty() && stack.getCount() >= count){
			Direction direction = world.getBlockState(pos).get(FACING);
			Inventory inventory = HopperBlockEntity.getInventoryAt(world, pos.offset(direction));
			ItemStack leftover;
			if(inventory == null)
				leftover = BEHAVIOR.dispense(pointer, stack);
			else{
				// only attempt to transfer up to `count`
				ItemStack overflow = HopperBlockEntity.transfer(be, inventory, stack.copy().split(count), direction.getOpposite());
				// subtract the amount that was *successfully* transferred from the total
				int transferred = count - overflow.getCount();
				leftover = stack.copy();
				leftover.decrement(transferred);
			}
			
			// move to next slot after dispensing, try to move to a full one
			do{
				be.setDropIndex((be.getDropIndex() + 1) % 9);
			}while(be.getDropCount(be.getDropIndex()) == 0);
			be.setStack(idx, leftover);
		}else
			world.syncWorldEvent(WorldEvents.DISPENSER_FAILS, pos, 0);
	}
	
	protected DispenserBehavior getBehaviorForItem(World world, ItemStack stack){
		return BEHAVIOR;
	}
	
	public BlockEntity createBlockEntity(BlockPos pos, BlockState state){
		return new MnemonicDropperBlockEntity(pos, state);
	}
	
	public static class Behaviour extends ItemDispenserBehavior{
		
		// from ItemDispenserBehavior
		protected ItemStack dispenseSilently(BlockPointer pointer, ItemStack stack){
			Direction direction = pointer.state().get(DispenserBlock.FACING);
			MnemonicDropperBlockEntity be = (MnemonicDropperBlockEntity)pointer.blockEntity();
			ItemStack split = stack.split(be.getDropCount(be.getDropIndex()));
			spawnItem(pointer, split, 6, direction, DispenserBlock.getOutputLocation(pointer));
			return stack;
		}
		
		public static void spawnItem(BlockPointer from, ItemStack stack, int speed, Direction side, Position pos){
			World world = from.world();
			double x = pos.getX();
			double y = pos.getY();
			double z = pos.getZ();
			if(side.getAxis() == Direction.Axis.Y)
				y -= 0.125;
			else
				y -= 0.15625;
			
			ItemEntity entity = new ItemEntity(world, x, y, z, stack);
			double base = 0.12;
			entity.setVelocity(
					world.random.nextTriangular((double)side.getOffsetX() * base, 0.003 * (double)speed),
					world.random.nextTriangular(0.2, 0.008 * (double)speed),
					world.random.nextTriangular((double)side.getOffsetZ() * base, 0.003 * (double)speed)
			);
			world.spawnEntity(entity);
		}
	}
}