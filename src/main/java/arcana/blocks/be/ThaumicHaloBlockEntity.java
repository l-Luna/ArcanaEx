package arcana.blocks.be;

import arcana.ArcanaRegistry;
import arcana.aspects.Aspect;
import arcana.aspects.AspectStack;
import arcana.aspects.Aspects;
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

import java.util.List;

public class ThaumicHaloBlockEntity extends BlockEntity{
	
	public static final int capacity = 50;
	public static final List<Aspect> boostAspects = List.of(
			Aspects.MAGIC,
			Aspects.AURA
	);
	
	@Nullable
	public AspectStack stored;
	
	public ThaumicHaloBlockEntity(BlockPos pos, BlockState state){
		super(ArcanaRegistry.THAUMIC_HALO_BE, pos, state);
	}
	
	public static void tick(World world, BlockPos pos, BlockState state, ThaumicHaloBlockEntity halo){
		if(world.isClient)
			return;
		
		// boost nodes in range
		// if auram is used, increase chance to upgrade & frequency
		// convert small flowers into magical flowers
	}
	
	protected void writeNbt(NbtCompound nbt){
		super.writeNbt(nbt);
		if(stored != null)
			nbt.put("stored", stored.toNbt());
	}
	
	public void readNbt(NbtCompound nbt){
		super.readNbt(nbt);
		if(nbt.contains("stored"))
			stored = AspectStack.fromNbt(nbt.getCompound("stored"));
		else stored = null;
	}
	
	@Nullable
	public AspectStack accept(AspectStack stack, World world, BlockPos pos, Direction from){
		// only accept at all if it's something we can actually use
		if(!boostAspects.contains(stack.type()))
			return stack;
		// standard jar code
		var result = AspectStack.mergeWithCapacity(stored, stack, capacity);
		if(!result.getLeft().equals(stored)){
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
}