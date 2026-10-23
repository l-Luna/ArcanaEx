package arcana.blocks.be;

import arcana.ArcanaRegistry;
import arcana.screens.MnemonicDropperScreen;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.DispenserBlockEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.screen.PropertyDelegate;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;

import java.util.Arrays;

public class MnemonicDropperBlockEntity extends DispenserBlockEntity{
	
	private int dropIndex = 8;
	private final int[] dropCounts = new int[9];
	private final PropertyDelegate properties = new PropertyDelegate(){
		public int get(int index){
			if(index == 0)
				return dropIndex;
			return dropCounts[index - 1];
		}
		
		public void set(int index, int value){
			if(index == 0)
				dropIndex = value;
			else
				dropCounts[index - 1] = Math.clamp(value, 0, 64);
			markDirty();
		}
		
		public int size(){
			return dropCounts.length + 1;
		}
	};
	
	public MnemonicDropperBlockEntity(BlockPos pos, BlockState state){
		super(ArcanaRegistry.MNEMONIC_DROPPER_BE, pos, state);
		Arrays.fill(dropCounts, 1);
	}
	
	public void setDropIndex(int dropIndex){
		this.dropIndex = dropIndex;
		markDirty();
	}
	
	public int getDropIndex(){
		return dropIndex;
	}
	
	public int getDropCount(int index){
		return dropCounts[index];
	}
	
	protected Text getContainerName(){
		return Text.translatable("container.arcana.mnemonic_dropper.title");
	}
	
	protected void writeNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup registryLookup){
		super.writeNbt(nbt, registryLookup);
		nbt.putIntArray("drop_counts", dropCounts);
		nbt.putInt("drop_index", dropIndex);
	}
	
	public void readNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup registryLookup){
		super.readNbt(nbt, registryLookup);
		int[] from = nbt.getIntArray("drop_counts");
		System.arraycopy(from, 0, dropCounts, 0, Math.min(from.length, 9));
		dropIndex = nbt.getInt("drop_index");
	}
	
	protected ScreenHandler createScreenHandler(int syncId, PlayerInventory playerInventory){
		return new MnemonicDropperScreen.Handler(syncId, playerInventory, this, properties);
	}
}