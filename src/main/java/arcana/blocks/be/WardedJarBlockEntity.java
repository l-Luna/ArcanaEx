package arcana.blocks.be;

import arcana.ArcanaRegistry;
import arcana.api.AspectIo;
import arcana.aspects.AspectStack;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.Packet;
import net.minecraft.network.listener.ClientPlayPacketListener;
import net.minecraft.network.packet.s2c.play.BlockEntityUpdateS2CPacket;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

public class WardedJarBlockEntity extends BlockEntity implements AspectIo{
	
	private static final int capacity = 100;
	
	private final boolean isVoidJar;
	
	@Nullable
	private AspectStack stored;
	
	public WardedJarBlockEntity(BlockPos pos, BlockState state, boolean isVoidJar){
		super(isVoidJar ? ArcanaRegistry.VOID_JAR_BE : ArcanaRegistry.WARDED_JAR_BE, pos, state);
		this.isVoidJar = isVoidJar;
	}
	
	public void readNbt(NbtCompound nbt){
		super.readNbt(nbt);
		if(nbt.contains("stored"))
			stored = AspectStack.fromNbt(nbt.getCompound("stored"));
		else stored = null;
	}
	
	protected void writeNbt(NbtCompound nbt){
		super.writeNbt(nbt);
		if(stored != null)
			nbt.put("stored", stored.toNbt());
	}
	
	public AspectStack accept(AspectStack stack, World world, BlockPos pos, Direction from){
		var result = AspectStack.mergeWithCapacity(stored, stack, capacity);
		if(!result.getLeft().equals(stored)){
			stored = result.getLeft();
			markDirty();
		}
		if(isVoidJar){
			// TODO: produce flux
			return null;
		}
		return result.getRight();
	}
	
	public @Nullable AspectStack draw(int max, World world, BlockPos pos, Direction from){
		return draw(max);
	}
	
	public @Nullable AspectStack draw(int max){
		var result = AspectStack.draw(stored, max);
		if(!Objects.equals(result.getLeft(), stored)){
			stored = result.getLeft();
			markDirty();
		}
		return result.getRight();
	}
	
	public Packet<ClientPlayPacketListener> toUpdatePacket(){
		return BlockEntityUpdateS2CPacket.create(this);
	}
	
	public NbtCompound toInitialChunkDataNbt(){
		return createNbt();
	}
	
	public void markDirty(){
		super.markDirty();
		if(world instanceof ServerWorld sw)
			sw.getChunkManager().markForUpdate(pos);
	}
	
	@Nullable
	public AspectStack getStored(){
		return stored;
	}
}