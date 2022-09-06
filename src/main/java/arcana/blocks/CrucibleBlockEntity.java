package arcana.blocks;

import arcana.ArcanaRegistry;
import arcana.ArcanaTags;
import arcana.aspects.AspectMap;
import arcana.aspects.AspectStack;
import arcana.aspects.ItemAspectRegistry;
import arcana.recipes.AlchemyInventory;
import arcana.recipes.AlchemyRecipe;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.Packet;
import net.minecraft.network.listener.ClientPlayPacketListener;
import net.minecraft.network.packet.s2c.play.BlockEntityUpdateS2CPacket;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.math.BlockPos;

import java.util.List;
import java.util.Optional;

import static arcana.blocks.CrucibleBlock.INSIDE;

public class CrucibleBlockEntity extends BlockEntity{
	
	private AspectMap aspects = new AspectMap();
	
	public CrucibleBlockEntity(BlockPos pos, BlockState state){
		super(ArcanaRegistry.CRUCIBLE_BE, pos, state);
	}
	
	public void tick(){
		if(world != null && !world.isClient() && hasWater() && isBoiling()){
			List<ItemEntity> items = world.getEntitiesByClass(ItemEntity.class, INSIDE.getBoundingBox().offset(pos), __ -> true);
			for(ItemEntity item : items){
				ItemStack stack = item.getStack();
				boolean melt = true;
				if(item.getThrower() != null && world.getPlayerByUuid(item.getThrower()) != null){
					PlayerEntity thrower = world.getPlayerByUuid(item.getThrower());
					AlchemyInventory inventory = new AlchemyInventory(this, thrower, stack);
					Optional<AlchemyRecipe> optionalRecipe = world.getRecipeManager().getFirstMatch(AlchemyRecipe.TYPE, inventory, world);
					if(optionalRecipe.isPresent()){
						melt = false;
						AlchemyRecipe recipe = optionalRecipe.get();
						if(stack.getCount() == 1)
							item.remove(Entity.RemovalReason.KILLED);
						else
							stack.decrement(1);
						ItemStack result = recipe.craft(inventory);
						if(!thrower.giveItemStack(result))
							thrower.dropItem(result, false);
						aspects.take(recipe.getAspects());
						markDirty();
						world.updateListeners(pos, world.getBlockState(pos), world.getBlockState(pos), Block.NOTIFY_LISTENERS);
					}
				}
				if(melt){
					List<AspectStack> itemAspects = ItemAspectRegistry.get(stack).asStacks();
					if(itemAspects.size() > 0){
						item.remove(Entity.RemovalReason.KILLED);
						world.playSound(null, pos, SoundEvents.BLOCK_FIRE_EXTINGUISH, SoundCategory.BLOCKS, 1.0F, 1.0F);
						markDirty();
						world.updateListeners(pos, world.getBlockState(pos), world.getBlockState(pos), Block.NOTIFY_LISTENERS);
						for(AspectStack aspectStack : itemAspects)
							aspects.add(aspectStack.type(), aspectStack.amount() * stack.getCount());
					}
				}
			}
		}
	}
	
	public AspectMap getAspects(){
		return aspects;
	}
	
	private boolean hasWater(){
		return getWorld().getBlockState(getPos()).get(CrucibleBlock.FULL);
	}
	
	public void setEmpty(){
		aspects.clear();
		// TODO: flux...
	}
	
	public boolean isBoiling(){
		return world.getBlockState(pos.down()).isIn(ArcanaTags.CRUCIBLE_HEATING_BLOCKS)
		    || world.getFluidState(pos.down()).isIn(ArcanaTags.CRUCIBLE_HEATING_FLUIDS);
	}
	
	protected void writeNbt(NbtCompound nbt){
		nbt.put("aspects", aspects.toNbt());
	}
	
	public void readNbt(NbtCompound nbt){
		aspects = AspectMap.fromNbt(nbt.getCompound("aspects"));
	}
	
	public Packet<ClientPlayPacketListener> toUpdatePacket() {
		return BlockEntityUpdateS2CPacket.create(this);
	}
	
	public NbtCompound toInitialChunkDataNbt() {
		return createNbt();
	}
}