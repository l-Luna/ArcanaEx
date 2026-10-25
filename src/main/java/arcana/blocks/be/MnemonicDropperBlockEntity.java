package arcana.blocks.be;

import arcana.ArcanaRegistry;
import arcana.screens.MnemonicDropperScreen;
import arcana.util.MnemonicInventoryData;
import net.fabricmc.fabric.api.screenhandler.v1.ExtendedScreenHandlerFactory;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.DispenserBlockEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.SidedInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.screen.PropertyDelegate;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;

import java.util.Arrays;

public class MnemonicDropperBlockEntity extends DispenserBlockEntity implements SidedInventory, ExtendedScreenHandlerFactory<MnemonicInventoryData>{
	
	private int dropIndex = 0;
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
	
	public final MnemonicInventoryData mnemonics = new MnemonicInventoryData();
	
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
	
	protected void writeNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup lookup){
		super.writeNbt(nbt, lookup);
		nbt.putIntArray("drop_counts", dropCounts);
		nbt.putInt("drop_index", dropIndex);
		nbt.put("mnemonics", mnemonics.toNbt(lookup));
	}
	
	public void readNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup lookup){
		super.readNbt(nbt, lookup);
		int[] from = nbt.getIntArray("drop_counts");
		System.arraycopy(from, 0, dropCounts, 0, Math.min(from.length, 9));
		dropIndex = nbt.getInt("drop_index");
		mnemonics.readNbt(nbt.get("mnemonics"), lookup);
	}
	
	public void markDirty(){
		super.markDirty();
		mnemonics.onInventoryChanged(this);
	}
	
	protected ScreenHandler createScreenHandler(int syncId, PlayerInventory playerInventory){
		return new MnemonicDropperScreen.Handler(syncId, playerInventory, this, properties, mnemonics);
	}
	
	// isn't a sided inventory, but needs to implement the interface to have control over canInsert separate to isValid
	
	public int[] getAvailableSlots(Direction side){
		return new int[]{0, 1, 2, 3, 4, 5, 6, 7, 8};
	}
	
	public boolean canInsert(int slot, ItemStack stack, Direction dir){
		return mnemonics.filter(slot, stack);
	}
	
	public boolean canExtract(int slot, ItemStack stack, Direction dir){
		return true;
	}
	
	public MnemonicInventoryData getScreenOpeningData(ServerPlayerEntity player){
		return mnemonics;
	}
}