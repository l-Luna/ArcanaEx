package arcana.blocks.be;

import arcana.ArcanaRegistry;
import arcana.screens.DistilleryPathfinderScreenHandler;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.SimpleInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.Packet;
import net.minecraft.network.listener.ClientPlayPacketListener;
import net.minecraft.network.packet.s2c.play.BlockEntityUpdateS2CPacket;
import net.minecraft.screen.NamedScreenHandlerFactory;
import net.minecraft.screen.PropertyDelegate;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class DistilleryPathfinderBlockEntity extends BlockEntity implements NamedScreenHandlerFactory{
	
	private static final Text title = Text.translatable("block.arcana.distillery_pathfinder");
	
	public SimpleInventory material = new SimpleInventory(1), fuel = new SimpleInventory(1);
	public int burnTime, maxBurnTime, progress;
	
	public final PropertyDelegate propertyDelegate = new PropertyDelegate(){
		// [burn time, max burn time, progress]
		public int get(int index){
			return switch(index){
				case 0 -> burnTime;
				case 1 -> maxBurnTime;
				case 2 -> progress;
				default -> -1;
			};
		}
		
		public void set(int index, int value){
			switch(index){
				case 0 -> burnTime = index;
				case 1 -> maxBurnTime = value;
				case 2 -> progress = value;
			}
		}
		
		public int size(){
			return 3;
		}
	};
	
	public DistilleryPathfinderBlockEntity(BlockPos pos, BlockState state){
		super(ArcanaRegistry.DISTILLERY_PATHFINDER_BE, pos, state);
		material.addListener(__ -> markDirty());
		fuel.addListener(__ -> markDirty());
	}
	
	protected void writeNbt(NbtCompound nbt){
		super.writeNbt(nbt);
		nbt.put("material", material.getStack(0).writeNbt(new NbtCompound()));
		nbt.put("fuel", fuel.getStack(0).writeNbt(new NbtCompound()));
		
		nbt.putInt("burnTime", burnTime);
		nbt.putInt("maxBurnTime", maxBurnTime);
		nbt.putInt("progress", progress);
	}
	
	public void readNbt(NbtCompound nbt){
		super.readNbt(nbt);
		material.setStack(0, ItemStack.fromNbt(nbt.getCompound("material")));
		fuel.setStack(0, ItemStack.fromNbt(nbt.getCompound("fuel")));
		
		burnTime = nbt.getInt("burnTime");
		maxBurnTime = nbt.getInt("maxBurnTime");
		progress = nbt.getInt("progress");
	}
	
	public void tick(World world, BlockPos pos, BlockState state){
		progress++;
		
		// now explode!
		if(progress > 13 * 20){
			world.setBlockState(pos, ArcanaRegistry.TAINT_GOO.getDefaultState());
		}
	}
	
	public Text getDisplayName(){
		return title;
	}
	
	public ScreenHandler createMenu(int syncId, PlayerInventory inv, PlayerEntity player){
		return new DistilleryPathfinderScreenHandler(syncId, inv, material, fuel, propertyDelegate);
	}
	
	public void markDirty(){
		super.markDirty();
		if(world instanceof ServerWorld sw)
			sw.getChunkManager().markForUpdate(pos);
	}
	
	public Packet<ClientPlayPacketListener> toUpdatePacket(){
		return BlockEntityUpdateS2CPacket.create(this);
	}
	
	public NbtCompound toInitialChunkDataNbt(){
		return createNbt();
	}
}