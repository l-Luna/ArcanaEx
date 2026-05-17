package arcana.blocks.be;

import arcana.ArcanaRegistry;
import arcana.aspects.Aspect;
import arcana.aspects.AspectMap;
import arcana.client.particles.AspectParticleEffect;
import arcana.components.Researcher;
import arcana.recipes.infusion.BakedInfusionRecipe;
import arcana.recipes.infusion.InfusionInventory;
import arcana.recipes.infusion.InfusionRecipe;
import arcana.recipes.infusion.SimpleInfusionRecipe;
import arcana.research.BuiltinResearch;
import arcana.util.NbtUtil;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.Packet;
import net.minecraft.network.listener.ClientPlayPacketListener;
import net.minecraft.network.packet.s2c.play.BlockEntityUpdateS2CPacket;
import net.minecraft.particle.ItemStackParticleEffect;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.random.Random;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Stream;

import static arcana.blocks.be.InfusionMatrixBlockEntity.InfusionState.*;

public class InfusionMatrixBlockEntity extends BlockEntity{
	
	public enum InfusionState{
		IDLE,
		TAKING_ESSENTIA,
		TAKING_ITEMS
	}
	
	private boolean activated = false;
	
	private InfusionRecipe curRecipe;
	private InfusionState curState = IDLE;
	private AspectMap remainingEssentia;
	private List<ItemStack> remainingItems;
	private int instabilityRate = 0;
	private ItemStack result = null;
	private int cooldown = 0;
	private float instability = 0;
	
	private long lastCraftStartEndTime = -1;
	
	// we don't have access to the world or the recipe manager when loading NBT, so we hold it here and deref on first tick
	private Identifier lastRecipe = null;
	
	public InfusionMatrixBlockEntity(BlockPos pos, BlockState state){
		super(ArcanaRegistry.INFUSION_MATRIX_BE, pos, state);
	}
	
	public void tick(){
		assert world != null;
		if(lastRecipe != null){
			curRecipe = (SimpleInfusionRecipe)world.getRecipeManager().get(lastRecipe).orElse(null);
			BlockState state = world.getBlockState(pos);
			world.updateListeners(pos, state, state, Block.NOTIFY_LISTENERS);
			lastRecipe = null;
		}
		
		// check that we're valid and activated
		if(!activated){
			if(curRecipe != null)
				reset();
			return;
		}
		
		if(!structureValid(true)){
			activated = false;
			for(Direction direction : Direction.Type.HORIZONTAL){
				BlockPos pillarPos = pos.down(2).offset(direction).offset(direction.rotateYClockwise());
				if(world.getBlockEntity(pillarPos) instanceof InfusionPillarBlockEntity pillar)
					pillar.setMatrixPosition(null);
			}
			reset();
		}
		
		BlockEntity pedestal = world.getBlockEntity(pos.down(2));
		if(!(pedestal instanceof PedestalBlockEntity pbe) || pbe.getStack().isEmpty()){
			reset();
			return;
		}
		
		if(curRecipe != null){
			markDirty();
			instability += 2 + instabilityRate;
			switch(curState){
				case IDLE -> curState = TAKING_ESSENTIA;
				case TAKING_ESSENTIA -> {
					if(!tickEssentia()){
						curState = TAKING_ITEMS;
						cooldown = 60;
					}
				}
				case TAKING_ITEMS -> {
					if(!tickItems()){
						curState = IDLE;
						finishCrafting(pbe);
					}
				}
			}
		}
	}
	
	private boolean structureValid(boolean strict){
		boolean valid = true;
		BlockPos down = pos.down(2);
		for(Direction direction : Direction.Type.HORIZONTAL){
			BlockPos pillarPos = down.offset(direction).offset(direction.rotateYClockwise());
			if(!(world.getBlockEntity(pillarPos) instanceof InfusionPillarBlockEntity pillar) || (strict && !Objects.equals(pillar.getMatrixPosition(), pos)))
				valid = false;
		}
		
		BlockEntity pedestal = world.getBlockEntity(down);
		if(!(pedestal instanceof PedestalBlockEntity))
			valid = false;
		return valid;
	}
	
	public void activate(PlayerEntity player){
		if(!activated){
			if(structureValid(false)){
				for(Direction direction : Direction.Type.HORIZONTAL){
					BlockPos pillarPos = pos.down(2).offset(direction).offset(direction.rotateYClockwise());
					if(world.getBlockEntity(pillarPos) instanceof InfusionPillarBlockEntity pillar)
						pillar.setMatrixPosition(pos);
				}
				activated = true;
			}
			return;
		}
		
		if(curRecipe != null)
			return;
		// find a valid recipe
		BlockEntity pedestal = world.getBlockEntity(pos.down(2));
		if(pedestal instanceof PedestalBlockEntity pbe && !pbe.getStack().isEmpty()){
			ItemStack centre = pbe.getStack();
			List<ItemStack> outers = outerStacks();
			AspectMap aspects = new AspectMap();
			inRange(world::getBlockEntity)
					.filter(WardedJarBlockEntity.class::isInstance)
					.map(WardedJarBlockEntity.class::cast)
					.map(WardedJarBlockEntity::getStored)
					.filter(Objects::nonNull)
					.forEach(aspects::add);
			
			InfusionInventory inv = new InfusionInventory(centre, outers, aspects);
			world.getRecipeManager().getFirstMatch(SimpleInfusionRecipe.TYPE, inv, world).ifPresent(recipe -> {
				curRecipe = recipe;
				Researcher researcher = Researcher.from(player);
				if(!researcher.isPuzzleComplete(BuiltinResearch.infusionMilestonePuzzle)){
					researcher.completePuzzle(BuiltinResearch.infusionMilestonePuzzle);
					researcher.doSync();
				}
				
				BakedInfusionRecipe requirements = recipe.craftInfusion(inv);
				result = requirements.result();
				remainingEssentia = requirements.aspects();
				remainingItems = requirements.outerStacks();
				instabilityRate = requirements.instability();
				cooldown = 0;
				instability = 0;
				lastCraftStartEndTime = world.getTime();
			});
		}
	}
	
	private void reset(){
		if(curRecipe != null)
			markDirty();
		curState = IDLE;
		remainingEssentia = null;
		remainingItems = null;
		result = null;
		instabilityRate = 0;
		curRecipe = null;
		cooldown = 0;
		instability = 0;
	}
	
	private void finishCrafting(PedestalBlockEntity pedestal){
		pedestal.setStack(result);
		lastCraftStartEndTime = world.getTime();
		reset();
	}
	
	private <T> Stream<@NotNull T> inRange(Function<BlockPos, @Nullable T> getter){
		Stream.Builder<T> builder = Stream.builder();
		for(int y = 0; y < 2; y++)
			for(int x = 0; x < 11; x++)
				for(int z = 0; z < 11; z++){
					BlockPos local = pos.down(2 - y).south(z - 5).east(x - 5);
					T t = getter.apply(local);
					if(t != null)
						builder.add(t);
				}
		return builder.build();
	}
	
	private @NotNull List<ItemStack> outerStacks(){
		return inRange(pos -> {
			if(world.getBlockEntity(pos) instanceof PedestalBlockEntity p && !p.getStack().isEmpty())
				return p.getStack();
			return null;
		}).toList();
	}
	
	public InfusionRecipe getCurrentRecipe(){
		return curRecipe;
	}
	
	public InfusionState getCurrentState(){
		return curState;
	}
	
	public float getInstability(){
		return instability;
	}
	
	public long getLastCraftStartEndTime(){
		return lastCraftStartEndTime;
	}
	
	public boolean isActivated(){
		return activated;
	}
	
	protected void writeNbt(NbtCompound nbt){
		super.writeNbt(nbt);
		nbt.putBoolean("activated", activated);
		
		if(curRecipe != null){
			nbt.putString("currentRecipe", curRecipe.getId().toString());
			nbt.put("remainingEssentia", remainingEssentia.toNbt());
			nbt.put("remainingItems", remainingItems.stream().map(x -> x.writeNbt(new NbtCompound())).collect(NbtUtil.toNbtList()));
			nbt.put("result", result.writeNbt(new NbtCompound()));
			nbt.putInt("instabilityRate", instabilityRate);
			nbt.putString("state", curState.name());
			nbt.putInt("cooldown", cooldown);
			nbt.putFloat("instability", instability);
		}
	}
	
	public void readNbt(NbtCompound nbt){
		super.readNbt(nbt);
		activated = nbt.getBoolean("activated");
		
		if(nbt.contains("currentRecipe") && nbt.contains("state")){
			lastRecipe = new Identifier(nbt.getString("currentRecipe"));
			curState = InfusionState.valueOf(nbt.getString("state"));
			cooldown = nbt.getInt("cooldown");
			instability = nbt.getFloat("instability");
			instabilityRate = nbt.getInt("instabilityRate");
			remainingEssentia = AspectMap.fromNbt(nbt.getCompound("remainingEssentia"));
			remainingItems = NbtUtil.readMutList(nbt, "remainingItems", ItemStack::fromNbt);
			result = ItemStack.fromNbt(nbt.getCompound("result"));
		}
	}
	
	public Packet<ClientPlayPacketListener> toUpdatePacket() {
		return BlockEntityUpdateS2CPacket.create(this);
	}
	
	public NbtCompound toInitialChunkDataNbt() {
		return createNbt();
	}
	
	public boolean tickItems(){
		InfusionRecipe recipe = getCurrentRecipe();
		if(recipe != null && !remainingItems.isEmpty()){
			ItemStack next = remainingItems.get(0);
			Optional<PedestalBlockEntity> matching = inRange(pos1 -> world.getBlockEntity(pos1) instanceof PedestalBlockEntity p ? p : null)
					.filter(x -> next.equals(x.getStack()))
					.findFirst();
			if(matching.isPresent()){
				var pedestal = matching.get();
				var stack = pedestal.getStack();
				var pPos = pedestal.getPos();
				if(world.getTime() % 2 == 0){
					Random rng = world.getRandom();
					double sx = pPos.getX() + rng.nextGaussian() / 8;
					double sy = pPos.getY() + rng.nextGaussian() / 8;
					double sz = pPos.getZ() + rng.nextGaussian() / 8;
					world.addParticle(
							new ItemStackParticleEffect(ArcanaRegistry.INFUSION_ITEM, stack),
							sx + .5, sy + 1.5, sz + .5,
							(pos.getX() - sx) / 10d,
							(pos.getY() - sy - 2) / 10d,
							(pos.getZ() - sz) / 10d
					);
				}
				if(cooldown > 0){
					cooldown--;
					return true;
				}
				remainingItems.remove(stack);
				pedestal.setStack(ItemStack.EMPTY);
				cooldown = 60;
			}
			return true;
		}
		return false;
	}
	
	public boolean tickEssentia(){
		if(world.getTime() % 2 != 0)
			return true;
		InfusionRecipe recipe = getCurrentRecipe();
		if(remainingEssentia != null && !remainingEssentia.isEmpty() && recipe != null){
			Aspect next = remainingEssentia.aspectByIndex(0);
			Optional<WardedJarBlockEntity> first = inRange(world::getBlockEntity)
					.filter(WardedJarBlockEntity.class::isInstance)
					.map(WardedJarBlockEntity.class::cast)
					.filter(x -> x.getStored() != null && x.getStored().type().equals(next))
					.findFirst();
			if(first.isEmpty())
				return true; // we can't find any jars with the aspect we need
			var jar = first.get();
			var jarPos = jar.getPos();
			var xdiff = jarPos.getX() - pos.getX();
			var zdiff = jarPos.getZ() - pos.getZ();
			var xzdiff = Math.sqrt(xdiff * xdiff + zdiff * zdiff);
			world.addParticle(
					new AspectParticleEffect(ArcanaRegistry.ESSENTIA_STREAM, next),
					jarPos.getX() + .5, jarPos.getY() + .5, jarPos.getZ() + .5,
					// EssentiaStreamParticle uses these for x/y rotation
					Math.PI + Math.atan2(xdiff, zdiff),
					Math.PI / 2 - Math.atan2(pos.getY() - jarPos.getY() - 1, xzdiff),
					0
			);
			if(world.getTime() % 4 == 0){
				jar.draw(1);
				remainingEssentia.take(next, 1);
			}
			return true;
		}
		return false;
	}
}