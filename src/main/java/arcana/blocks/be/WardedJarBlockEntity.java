package arcana.blocks.be;

import arcana.ArcanaRegistry;
import arcana.api.AspectIo;
import arcana.aspects.AspectStack;
import arcana.items.components.ArcanaDataComponents;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.component.ComponentMap;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.listener.ClientPlayPacketListener;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.s2c.play.BlockEntityUpdateS2CPacket;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

public class WardedJarBlockEntity extends BlockEntity implements AspectIo{
	
	@Nullable
	private AspectStack stored;
	
	public WardedJarBlockEntity(BlockEntityType<? extends WardedJarBlockEntity> type, BlockPos pos, BlockState state){
		super(type, pos, state);
	}
	
	public WardedJarBlockEntity(BlockPos pos, BlockState state){
		this(ArcanaRegistry.WARDED_JAR_BE, pos, state);
	}
	
	public int getCapacity(){
		return 128;
	}
	
	public void readNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup registryLookup){
		super.readNbt(nbt, registryLookup);
		stored = null;
		if(nbt.contains("stored"))
			stored = AspectStack.fromNbt(nbt.getCompound("stored"));
	}
	
	protected void writeNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup registryLookup){
		super.writeNbt(nbt, registryLookup);
		if(stored != null)
			nbt.put("stored", stored.toNbt());
		else{
			// empty NBT tags don't get synced, so add dummy data
			// actual saving/loading just uses the presence of `stored`
			nbt.putBoolean("empty", true);
		}
	}
	
	public AspectStack accept(AspectStack stack, World world, BlockPos pos, Direction from){
		var result = AspectStack.mergeWithCapacity(stored, stack, getCapacity());
		if(!result.getLeft().equals(stored)){
			stored = result.getLeft();
			markDirty();
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
	
	public NbtCompound toInitialChunkDataNbt(RegistryWrapper.WrapperLookup registryLookup){
		return createNbt(registryLookup);
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
	
	protected void readComponents(ComponentsAccess components){
		super.readComponents(components);
		stored = components.get(ArcanaDataComponents.STORED_SINGLE_ASPECT);
	}
	
	protected void addComponents(ComponentMap.Builder componentMapBuilder){
		super.addComponents(componentMapBuilder);
		componentMapBuilder.add(ArcanaDataComponents.STORED_SINGLE_ASPECT, stored);
	}
	
	public void removeFromCopiedStackNbt(NbtCompound nbt){
		super.removeFromCopiedStackNbt(nbt);
		nbt.remove("stored");
	}
}