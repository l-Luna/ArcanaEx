package arcana.blocks;

import arcana.ArcanaRegistry;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.Packet;
import net.minecraft.network.listener.ClientPlayPacketListener;
import net.minecraft.network.packet.s2c.play.BlockEntityUpdateS2CPacket;
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
	}
	
	protected void writeNbt(NbtCompound nbt){
		super.writeNbt(nbt);
		nbt.put("stack", stack.writeNbt(new NbtCompound()));
	}
	
	public void readNbt(NbtCompound nbt){
		super.readNbt(nbt);
		stack = ItemStack.fromNbt(nbt.getCompound("stack"));
	}
	
	public Packet<ClientPlayPacketListener> toUpdatePacket() {
		return BlockEntityUpdateS2CPacket.create(this);
	}
	
	public NbtCompound toInitialChunkDataNbt() {
		return createNbt();
	}
}