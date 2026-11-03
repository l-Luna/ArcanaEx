package arcana.blocks.be;

import arcana.ArcanaRegistry;
import arcana.util.ArrayInventory;
import arcana.util.SidedArrayInventory;
import net.minecraft.block.BlockState;
import net.minecraft.block.InventoryProvider;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.inventory.SidedInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.listener.ClientPlayPacketListener;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.s2c.play.BlockEntityUpdateS2CPacket;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.WorldAccess;

public class PedestalBlockEntity extends BlockEntity implements InventoryProvider{
	
	private final SidedArrayInventory inventory = new SidedArrayInventory(1)
			.withDefaultSidedSlots(0);
	
	public PedestalBlockEntity(BlockPos pos, BlockState state){
		super(ArcanaRegistry.PEDESTAL_BE, pos, state);
	}
	
	public ItemStack getStack(){
		return inventory.getStack(0);
	}
	
	public void setStack(ItemStack stack){
		inventory.setStack(0, stack);
		markDirty();
	}
	
	protected void writeNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup lookup){
		super.writeNbt(nbt, lookup);
		nbt.put("stack", getStack().encodeAllowEmpty(lookup));
	}
	
	public void readNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup lookup){
		super.readNbt(nbt, lookup);
		setStack(ItemStack.fromNbtOrEmpty(lookup, nbt.getCompound("stack")));
	}
	
	public Packet<ClientPlayPacketListener> toUpdatePacket(){
		return BlockEntityUpdateS2CPacket.create(this);
	}
	
	public NbtCompound toInitialChunkDataNbt(RegistryWrapper.WrapperLookup registryLookup){
		return createNbt(registryLookup);
	}
	
	public ArrayInventory getInventory(){
		return inventory;
	}
	
	public SidedInventory getInventory(BlockState state, WorldAccess world, BlockPos pos){
		return inventory;
	}
}