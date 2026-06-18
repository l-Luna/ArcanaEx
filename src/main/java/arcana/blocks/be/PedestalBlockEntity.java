package arcana.blocks.be;

import arcana.ArcanaRegistry;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.listener.ClientPlayPacketListener;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.s2c.play.BlockEntityUpdateS2CPacket;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.util.math.BlockPos;

public class PedestalBlockEntity extends BlockEntity{
	
	private ItemStack stack = ItemStack.EMPTY;
	
	public PedestalBlockEntity(BlockPos pos, BlockState state){
		super(ArcanaRegistry.PEDESTAL_BE, pos, state);
	}
	
	public ItemStack getStack(){
		return stack;
	}
	
	public void setStack(ItemStack stack){
		this.stack = stack;
		markDirty();
	}
	
	protected void writeNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup registryLookup){
		super.writeNbt(nbt, registryLookup);
		nbt.put("stack", stack.encodeAllowEmpty(registryLookup));
	}
	
	public void readNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup registryLookup){
		super.readNbt(nbt, registryLookup);
		stack = ItemStack.fromNbtOrEmpty(registryLookup, nbt.getCompound("stack"));
	}
	
	public Packet<ClientPlayPacketListener> toUpdatePacket(){
		return BlockEntityUpdateS2CPacket.create(this);
	}
	
	public NbtCompound toInitialChunkDataNbt(RegistryWrapper.WrapperLookup registryLookup){
		return createNbt(registryLookup);
	}
}