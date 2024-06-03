package arcana.blocks.be;

import arcana.ArcanaRegistry;
import arcana.aspects.AspectIo;
import arcana.aspects.AspectSpeck;
import arcana.aspects.AspectStack;
import arcana.blocks.EssentiaPumpBlock;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.inventory.SimpleInventory;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;

public class EssentiaPumpBlockEntity extends EssentiaTubeBlockEntity{
	
	private int timer = 15;
	// essentia crystal filter
	public SimpleInventory inventory = new SimpleInventory(1);
	
	public EssentiaPumpBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state){
		super(type, pos, state);
	}
	
	public EssentiaPumpBlockEntity(BlockPos pos, BlockState state){
		super(ArcanaRegistry.ESSENTIA_PUMP_BE, pos, state);
	}
	
	public static void tick(World world, BlockPos pos, BlockState state, EssentiaPumpBlockEntity pump){
		EssentiaTubeBlockEntity.tick(world, pos, state, pump);
		if(world.isReceivingRedstonePower(pos))
			return;
		pump.timer--;
		if(pump.timer <= 0){
			pump.timer = 15;
			// IN ADDITION! we also want to create specks of our own
			Direction towards = state.get(EssentiaPumpBlock.facing);
			Direction from = towards.getOpposite();
			if(world.getBlockState(pos.offset(from)).getBlock() instanceof AspectIo aio){
				AspectStack drawn = aio.draw(5, world, pos.offset(from), towards);
				if(drawn != null){
					AspectSpeck speck = new AspectSpeck(drawn, .5f, towards, 0);
					pump.insert(speck);
				}
			}
		}
	}
	
	protected void writeNbt(NbtCompound nbt){
		super.writeNbt(nbt);
		nbt.putInt("timer", timer);
		nbt.put("inventory", inventory.toNbtList());
	}
	
	public void readNbt(NbtCompound nbt){
		super.readNbt(nbt);
		timer = nbt.getInt("timer");
		inventory.readNbtList(nbt.getList("inventory", NbtElement.COMPOUND_TYPE));
	}
}