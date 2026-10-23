package arcana.blocks.be;

import arcana.ArcanaRegistry;
import arcana.ArcanaTags;
import arcana.aspects.Aspect;
import arcana.aspects.AspectMap;
import arcana.aspects.AspectStack;
import arcana.aspects.ItemAspectRegistry;
import arcana.aura.AuraWorld;
import arcana.aura.FluxOrigin;
import arcana.blocks.CrucibleBlock;
import arcana.cca_components.Researcher;
import arcana.recipes.alchemy.AlchemyInput;
import arcana.recipes.alchemy.AlchemyRecipe;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.listener.ClientPlayPacketListener;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.s2c.play.BlockEntityUpdateS2CPacket;
import net.minecraft.recipe.RecipeEntry;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.Util;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.random.Random;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static arcana.blocks.CrucibleBlock.INSIDE;

public class CrucibleBlockEntity extends BlockEntity{
	
	private static final int MAX_CAPACITY = 300, SOFT_MAX_CAPACITY = 150;
	
	private AspectMap aspects = new AspectMap();
	
	public CrucibleBlockEntity(BlockPos pos, BlockState state){
		super(ArcanaRegistry.CRUCIBLE_BE, pos, state);
	}
	
	public void tick(){
		if(world != null && !world.isClient()){
			tickOverflow();
			if(!isBubbling())
				return;
			
			List<ItemEntity> items = world.getEntitiesByClass(ItemEntity.class, INSIDE.getBoundingBox().offset(pos), __ -> true);
			for(ItemEntity item : items){
				ItemStack stack = item.getStack();
				boolean melt = true;
				if(item.getOwner() instanceof PlayerEntity thrower){
					AlchemyInput inventory = new AlchemyInput(this, stack, Researcher.from(thrower).getAllResearch());
					Optional<RecipeEntry<AlchemyRecipe>> optionalRecipe = world.getRecipeManager().getFirstMatch(AlchemyRecipe.TYPE, inventory, world);
					if(optionalRecipe.isPresent()){
						melt = false;
						AlchemyRecipe recipe = optionalRecipe.get().value();
						ItemStack result = recipe.craft(inventory, world.getRegistryManager());
						aspects.take(recipe.getConsumedAspects(inventory));
						if(stack.getCount() == 1)
							item.remove(Entity.RemovalReason.KILLED);
						else
							stack.decrement(1);
						if(!thrower.giveItemStack(result))
							thrower.dropItem(result, false);
						markDirtyAndSync();
					}
				}
				if(melt && !stack.isIn(ArcanaTags.CRUCIBLE_REAGENT_BLACKLIST)){
					AspectMap itemAspects = ItemAspectRegistry.get(stack);
					if(!itemAspects.isEmpty()){
						onItemMelted(stack, itemAspects);
						item.remove(Entity.RemovalReason.KILLED);
						world.playSound(null, pos, SoundEvents.BLOCK_FIRE_EXTINGUISH, SoundCategory.BLOCKS, 1.0F, 1.0F);
						markDirtyAndSync();
					}
				}
			}
		}
	}
	
	private void onItemMelted(ItemStack stack, AspectMap itemAspects){
		for(AspectStack aspectStack : itemAspects)
			aspects.add(aspectStack.type(), aspectStack.amount() * stack.getCount());
		if(aspects.total() > MAX_CAPACITY)
			removeAndEmitFlux(aspects.total() - MAX_CAPACITY);
		if(aspects.total() > SOFT_MAX_CAPACITY)
			removeAndEmitFlux(Math.floorDiv(aspects.total() - SOFT_MAX_CAPACITY, 8));
	}
	
	private void tickOverflow(){
		if(aspects.total() > SOFT_MAX_CAPACITY && world.getTime() % 3 == 0)
			removeAndEmitFlux(1);
	}
	
	private void removeAndEmitFlux(int amount){
		AuraWorld.from(world).incrementFlux(amount, FluxOrigin.CRUCIBLE_BOILOFF, pos);
		spawnFluxBubble();
		
		Random rng = world.getRandom();
		List<Aspect> types = new ArrayList<>(aspects.aspectSet());
		Util.shuffle(types, rng);
		int total = aspects.total();
		for(Aspect type : types){
			float frac = (float)amount/total;
			int aspAmount = aspects.get(type);
			int sub = (int)Math.min(amount, Math.ceil(aspAmount * frac));
			aspects.take(type, sub);
			amount -= sub;
			if(amount <= 0)
				break;
			total -= aspAmount;
		}
		
		markDirtyAndSync();
	}
	
	private void spawnFluxBubble(){
		Random rand = world.random;
		((ServerWorld)world).spawnParticles(ArcanaRegistry.FLUX_BUBBLE,
				pos.getX() + .125 + rand.nextFloat() * .75f,
				pos.getY() + .8125f,
				pos.getZ() + .125 + rand.nextFloat() * .75f,
				0, 0, 0.02, 0, 1);
	}
	
	protected void markDirtyAndSync(){
		markDirty();
		world.updateListeners(pos, world.getBlockState(pos), world.getBlockState(pos), Block.NOTIFY_LISTENERS);
	}
	
	public AspectMap getAspects(){
		return aspects;
	}
	
	public boolean isBubbling(){
		return getWorld().getBlockState(getPos()).get(CrucibleBlock.FULL)
				&& (world.getBlockState(pos.down()).isIn(ArcanaTags.CRUCIBLE_HEATING_BLOCKS)
				|| world.getFluidState(pos.down()).isIn(ArcanaTags.CRUCIBLE_HEATING_FLUIDS));
	}
	
	public void setEmpty(){
		int aspectTotal = aspects.total();
		if(aspectTotal > 0)
			// TODO(balance): adjust the numbers here
			AuraWorld.from(world).incrementFlux(aspectTotal / 2f, FluxOrigin.CRUCIBLE_EMPTYING, pos);
		aspects.clear();
	}
	
	protected void writeNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup lookup){
		nbt.put("aspects", aspects.toNbt());
	}
	
	public void readNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup lookup){
		aspects = AspectMap.fromNbt(nbt.getCompound("aspects"));
	}
	
	public Packet<ClientPlayPacketListener> toUpdatePacket(){
		return BlockEntityUpdateS2CPacket.create(this);
	}
	
	public NbtCompound toInitialChunkDataNbt(RegistryWrapper.WrapperLookup registryLookup){
		return createNbt(registryLookup);
	}
}