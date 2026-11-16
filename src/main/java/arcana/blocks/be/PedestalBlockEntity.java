package arcana.blocks.be;

import arcana.ArcanaRegistry;
import arcana.util.SidedArrayInventory;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.inventory.SidedInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.listener.ClientPlayPacketListener;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.s2c.play.BlockEntityUpdateS2CPacket;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;

public class PedestalBlockEntity extends BlockEntity{
	
	private final SidedArrayInventory inventory = new SidedArrayInventory(1)
			.withDefaultSidedSlots(0)
			.withListener(__ -> markDirtyAndSync());
	
	public PedestalBlockEntity(BlockPos pos, BlockState state){
		super(ArcanaRegistry.PEDESTAL_BE, pos, state);
	}
	
	public ItemStack getStack(){
		return inventory.getStack(0);
	}
	
	public void setStack(ItemStack stack){
		inventory.setStack(0, stack);
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
	
	private void markDirtyAndSync(){
		markDirty();
		if(world instanceof ServerWorld sw)
			sw.getChunkManager().markForUpdate(pos);
	}
	
	public SidedInventory getInventory(){
		return inventory;
	}
}