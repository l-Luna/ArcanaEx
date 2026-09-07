package arcana.blocks.be;

import arcana.ArcanaRegistry;
import arcana.ArcanaSounds;
import arcana.blocks.MagicMirrorBlock;
import arcana.cca_components.MagicMirrorQueue;
import arcana.items.components.ArcanaDataComponents;
import arcana.util.MathUtil;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.HopperBlockEntity;
import net.minecraft.component.ComponentMap;
import net.minecraft.entity.ItemEntity;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;

import java.util.UUID;

public class MagicMirrorBlockEntity extends BlockEntity{
	
	private UUID tag, id;
	
	public MagicMirrorBlockEntity(BlockPos pos, BlockState state){
		super(ArcanaRegistry.MAGIC_MIRROR_BE, pos, state);
	}
	
	//
	
	public void tick(World world, BlockPos pos, BlockState state){
		if(world.isClient)
			return;
		if(id == null){
			id = MathUtil.randomUuid(world.random);
			markDirty();
		}
		ItemStack next = MagicMirrorQueue.from(world).pull(tag, id);
		if(next == null)
			return;
		
		Direction facing = state.get(MagicMirrorBlock.FACING);
		// if directly above an inventory, try to insert directly
		Inventory target = HopperBlockEntity.getInventoryAt(world, pos.down());
		if(target != null)
			next = HopperBlockEntity.transfer(null, target, next, null);
		// then drop the excess
		if(!next.isEmpty()){
			ItemEntity entity = new ItemEntity(world, pos.getX() + .5 - facing.getOffsetX() * 0.4, pos.getY() + .5, pos.getZ() + .5 - facing.getOffsetZ() * 0.4, next.copy());
			entity.setVelocity(facing.getOffsetX() * 0.2, 0, facing.getOffsetZ() * 0.2);
			world.spawnEntity(entity);
		}
		playMirrorEffect((ServerWorld)world, 0.5f);
	}
	
	public void pushItem(ItemStack stack){
		World world = getWorld();
		MagicMirrorQueue.from(world).push(getTag(), getId(), stack);
		// TODO: VFX
		if(world instanceof ServerWorld sw)
			playMirrorEffect(sw, 1.1f);
	}
	
	public UUID getTag(){
		return tag;
	}
	
	public void setTag(UUID tag){
		this.tag = tag;
		markDirty();
	}
	
	public UUID getId(){
		return id;
	}
	
	//
	
	protected void writeNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup registryLookup){
		super.writeNbt(nbt, registryLookup);
		nbt.putUuid("tag", tag);
		if(id != null)
			nbt.putUuid("m_id", id);
	}
	
	public void readNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup registryLookup){
		super.readNbt(nbt, registryLookup);
		tag = nbt.getUuid("tag");
		if(nbt.containsUuid("m_id"))
			id = nbt.getUuid("m_id");
	}
	
	protected void readComponents(ComponentsAccess components){
		super.readComponents(components);
		setTag(components.get(ArcanaDataComponents.MAGIC_MIRROR_TAG));
	}
	
	protected void addComponents(ComponentMap.Builder componentMapBuilder){
		super.addComponents(componentMapBuilder);
		componentMapBuilder.add(ArcanaDataComponents.MAGIC_MIRROR_TAG, getTag());
	}
	
	public void removeFromCopiedStackNbt(NbtCompound nbt){
		super.removeFromCopiedStackNbt(nbt);
		nbt.remove("tag");
		nbt.remove("m_id");
	}
	
	private void playMirrorEffect(ServerWorld sw, float pitch){
		sw.playSound(null, pos.getX(), pos.getY(), pos.getZ(), ArcanaSounds.MIRROR_ACCEPT.entry(), SoundCategory.BLOCKS, 0.12f, pitch + sw.random.nextFloat()*0.1f, sw.random.nextLong());
	}
}