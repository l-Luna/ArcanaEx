package arcana.blocks.be;

import arcana.ArcanaRegistry;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.Packet;
import net.minecraft.network.listener.ClientPlayPacketListener;
import net.minecraft.network.packet.s2c.play.BlockEntityUpdateS2CPacket;
import net.minecraft.util.math.BlockPos;
import org.jetbrains.annotations.Nullable;

public class InfusionPillarBlockEntity extends BlockEntity{
	
	private @Nullable BlockPos relativeMatrixPosition = null;
	
	public InfusionPillarBlockEntity(BlockPos pos, BlockState state){
		super(ArcanaRegistry.INFUSION_PILLAR_BE, pos, state);
	}
	
	public @Nullable BlockPos getMatrixPosition(){
		return relativeMatrixPosition != null ? pos.add(relativeMatrixPosition) : null;
	}
	
	public void setMatrixPosition(@Nullable BlockPos matrixPos){
		relativeMatrixPosition = matrixPos != null ? matrixPos.subtract(pos) : null;
		markDirty();
	}
	
	protected void writeNbt(NbtCompound nbt){
		super.writeNbt(nbt);
		nbt.putBoolean("active", relativeMatrixPosition != null);
		if(relativeMatrixPosition != null)
			nbt.putLong("relativeMatrixPosition", relativeMatrixPosition.asLong());
	}
	
	public void readNbt(NbtCompound nbt){
		super.readNbt(nbt);
		if(nbt.getBoolean("active"))
			relativeMatrixPosition = BlockPos.fromLong(nbt.getLong("relativeMatrixPosition"));
		else
			relativeMatrixPosition = null;
	}
	
	public Packet<ClientPlayPacketListener> toUpdatePacket() {
		return BlockEntityUpdateS2CPacket.create(this);
	}
	
	public NbtCompound toInitialChunkDataNbt() {
		return createNbt();
	}
}