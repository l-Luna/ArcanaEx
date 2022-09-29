package arcana.blocks;

import arcana.ArcanaRegistry;
import arcana.aspects.AspectIo;
import arcana.aspects.AspectStack;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.Packet;
import net.minecraft.network.listener.ClientPlayPacketListener;
import net.minecraft.network.packet.s2c.play.BlockEntityUpdateS2CPacket;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

public class WardedJarBlockEntity extends BlockEntity implements AspectIo{
	
	private static final int capacity = 100;
	
	@Nullable
	private AspectStack stored;
	
	public WardedJarBlockEntity(BlockPos pos, BlockState state){
		super(ArcanaRegistry.WARDED_JAR_BE, pos, state);
	}
	
	public void readNbt(NbtCompound nbt){
		super.readNbt(nbt);
		if(nbt.contains("stored"))
			stored = AspectStack.fromNbt(nbt.getCompound("stored"));
	}
	
	protected void writeNbt(NbtCompound nbt){
		super.writeNbt(nbt);
		if(stored != null)
			nbt.put("stored", stored.toNbt());
	}
	
	public boolean accept(AspectStack stack, World world, BlockPos pos, Direction from){
		if(stored != null && stack.type() != stored.type())
			return false;
		var current = stored == null ? 0 : stored.amount();
		int capacityLeft = capacity - current;
		if(capacityLeft > 0){
			int newAmount = current + stack.amount();
			stored = new AspectStack(stack.type(), newAmount);
			markDirty();
			return true;
		}
		return false;
	}
	
	public @Nullable AspectStack draw(int max, World world, BlockPos pos, Direction from){
		if(stored != null && stored.amount() > 0){
			var actual = Math.min(max, stored.amount());
			var ret = new AspectStack(stored.type(), actual);
			stored = new AspectStack(stored.type(), stored.amount() - actual);
			if(stored.amount() <= 0)
				stored = null;
			markDirty();
			return ret;
		}
		return null;
	}
	
	public Packet<ClientPlayPacketListener> toUpdatePacket(){
		return BlockEntityUpdateS2CPacket.create(this);
	}
	
	public NbtCompound toInitialChunkDataNbt(){
		return createNbt();
	}
	
	@Nullable
	public AspectStack getStored(){
		return stored;
	}
}