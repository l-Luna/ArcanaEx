package arcana.blocks;

import arcana.ArcanaRegistry;
import arcana.ArcanaTags;
import arcana.aspects.AspectMap;
import arcana.aspects.AspectStack;
import arcana.aspects.ItemAspectRegistry;
import arcana.components.KdItem;
import arcana.components.Researcher;
import arcana.items.TomeOfSharingItem;
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
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;

import java.util.List;
import java.util.Map;
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
				Map<Identifier, Integer> research = null;
				PlayerEntity thrower = null;
				if(item.getThrower() != null){
					thrower = world.getPlayerByUuid(item.getThrower());
					if(thrower != null)
						research = Researcher.from(thrower).getAllResearch();
				}else{
					var source = KdItem.getSource(item);
					if(source != null){
						BlockEntity be = world.getBlockEntity(source);
						if(be instanceof KnowledgeableDropperBlockEntity kdbe)
							// getBoundResearch is empty for the empty item stack
							research = TomeOfSharingItem.getBoundResearch(kdbe.getTomeSlot().getStack(0));
					}
				}
				if(research != null){
					AlchemyInventory inventory = new AlchemyInventory(this, stack, research);
					Optional<AlchemyRecipe> optionalRecipe = world.getRecipeManager().getFirstMatch(AlchemyRecipe.TYPE, inventory, world);
					if(optionalRecipe.isPresent()){
						melt = false;
						AlchemyRecipe recipe = optionalRecipe.get();
						if(stack.getCount() == 1)
							item.remove(Entity.RemovalReason.KILLED);
						else
							stack.decrement(1);
						ItemStack result = recipe.craft(inventory);
						if(thrower != null){
							if(!thrower.giveItemStack(result))
								thrower.dropItem(result, false);
						}else drop: {
							ItemEntity product = new ItemEntity(world, pos.getX() + .5, pos.getY() + 1, pos.getZ() + .5, result);
							var rng = world.random;
							for(Direction value : Direction.shuffle(rng))
								if(value.getAxis().isHorizontal()){
									var offset = pos.up().offset(value);
									if(!world.getBlockState(offset).isSolidBlock(world, offset)){
										product.setVelocity(value.getOffsetX() * 0.1, 0.2, value.getOffsetZ() * 0.1);
										world.spawnEntity(product);
										break drop;
									}
								}
							
							product.setVelocity(rng.nextGaussian() / 10, 0.2, rng.nextGaussian() / 10);
							world.spawnEntity(product);
						}
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