package arcana.blocks;

import arcana.ArcanaRegistry;
import arcana.aspects.Aspect;
import arcana.aspects.AspectMap;
import arcana.aspects.AspectStack;
import arcana.client.particles.AspectParticleEffect;
import arcana.recipes.InfusionInventory;
import arcana.recipes.InfusionRecipe;
import arcana.util.StreamUtil;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.network.Packet;
import net.minecraft.network.listener.ClientPlayPacketListener;
import net.minecraft.network.packet.s2c.play.BlockEntityUpdateS2CPacket;
import net.minecraft.particle.ItemStackParticleEffect;
import net.minecraft.recipe.Ingredient;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.random.Random;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static arcana.blocks.InfusionMatrixBlockEntity.InfusionState.*;

public class InfusionMatrixBlockEntity extends BlockEntity{
	
	public static final List<InfusionPhase> phases = List.of(
			new EssentiaPhase(),
			new ItemPhase()
	);
	
	private boolean activated = false;
	
	private InfusionRecipe crafting;
	private Map<InfusionPhase, NbtCompound> states = null;
	private int curPhase = -1;
	private InfusionState curState = null;
	
	// we don't have access to the world or the recipe manager when loading NBT, so we hold it here and deref on first tick
	private Identifier lastRecipe = null;
	
	public InfusionMatrixBlockEntity(BlockPos pos, BlockState state){
		super(ArcanaRegistry.INFUSION_MATRIX_BE, pos, state);
	}
	
	protected void tick(){
		assert world != null;
		if(lastRecipe != null){
			crafting = (InfusionRecipe)world.getRecipeManager().get(lastRecipe).orElse(null);
			var state = world.getBlockState(pos);
			world.updateListeners(pos, state, state, Block.NOTIFY_LISTENERS);
			lastRecipe = null;
		}
		
		// check that we're valid and activated
		if(!activated){
			if(crafting != null)
				reset();
			return;
		}
		
		if(!checkValid()){
			activated = false;
			reset();
		}
		
		BlockEntity pedestal = world.getBlockEntity(pos.down(2));
		if(pedestal instanceof PedestalBlockEntity pbe && pbe.getStack().isEmpty())
			reset();
		
		if(crafting != null){
			markDirty();
			// setup
			if(curPhase == -1){
				curPhase = 0;
				states = new HashMap<>(phases.size());
			}
			if(phases.size() > curPhase){
				var phase = currentPhase();
				curState = phase.tick(this, states.computeIfAbsent(phase, __ -> new NbtCompound()));
				if(curState == finished){
					curPhase++;
					curState = working;
				}else if(curState == stalling){
					// TODO: add instability here
				}
			}else{
				// completed
				assert pedestal instanceof PedestalBlockEntity;
				// TODO: use craft() in activate() to calculate/preserve enchantment levels, durability...
				((PedestalBlockEntity)pedestal).setStack(crafting.getOutput());
				reset();
			}
		}
	}
	
	private boolean checkValid(){
		boolean valid = true;
		var down = pos.down(2);
		for(Direction direction : Direction.Type.HORIZONTAL)
			if(!(world.getBlockEntity(down.offset(direction).offset(direction.rotateYClockwise())) instanceof InfusionPillarBlockEntity))
				valid = false;
		
		BlockEntity pedestal = world.getBlockEntity(down);
		if(!(pedestal instanceof PedestalBlockEntity))
			valid = false;
		return valid;
	}
	
	public void activate(){
		if(!activated){
			if(checkValid()){
				// TODO: setup infusion pillars...
				activated = true;
			}
			return;
		}
		
		if(crafting != null)
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
			world.getRecipeManager().getFirstMatch(InfusionRecipe.TYPE, inv, world).ifPresent(recipe -> crafting = recipe);
		}
	}
	
	private void reset(){
		if(crafting != null)
			markDirty();
		curPhase = -1;
		curState = null;
		states = null;
		crafting = null;
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
	
	public InfusionPhase currentPhase(){
		return curPhase == -1 || curPhase >= phases.size() ? null : phases.get(curPhase);
	}
	
	public InfusionRecipe getCurrentRecipe(){
		return crafting;
	}
	
	// for InfusionMatrixBlockEntityRenderer
	/*public NbtCompound getStateForPhase(InfusionPhase phase){
		return states != null ? states.getOrDefault(phase, new NbtCompound()) : new NbtCompound();
	}*/
	
	public boolean isActivated(){
		return activated;
	}
	
	protected void writeNbt(NbtCompound nbt){
		super.writeNbt(nbt);
		nbt.putBoolean("activated", activated);
		
		if(crafting != null){
			nbt.putString("currentRecipe", crafting.getId().toString());
			nbt.putInt("currentPhase", curPhase);
			
			NbtCompound phasesTag = new NbtCompound();
			if(states != null)
				states.forEach((phase, compound) -> phasesTag.put(String.valueOf(phases.indexOf(phase)), compound));
			nbt.put("phases", phasesTag);
		}
	}
	
	public void readNbt(NbtCompound nbt){
		super.readNbt(nbt);
		activated = nbt.getBoolean("activated");
		
		if(nbt.contains("currentRecipe")){
			lastRecipe = (new Identifier(nbt.getString("currentRecipe")));
			curPhase = nbt.getInt("currentPhase");
			
			NbtCompound phasesTag = nbt.getCompound("phases");
			states = new HashMap<>(phases.size());
			for(String key : phasesTag.getKeys())
				try{
					states.put(phases.get(Integer.parseInt(key)), phasesTag.getCompound(key));
				}catch(NumberFormatException ignored){}
		}
	}
	
	public Packet<ClientPlayPacketListener> toUpdatePacket() {
		return BlockEntityUpdateS2CPacket.create(this);
	}
	
	public NbtCompound toInitialChunkDataNbt() {
		return createNbt();
	}
	
	public interface InfusionPhase{
		
		@NotNull
		InfusionState tick(InfusionMatrixBlockEntity be, NbtCompound tag);
		
		/*@Environment(EnvType.CLIENT)
		void render(InfusionMatrixBlockEntity be,
		            MatrixStack matrices,
		            VertexConsumerProvider vcp,
		            float tickDelta,
		            NbtCompound tag);*/
	}
	
	public enum InfusionState{
		
		/** Still accepting items/resources and progressing normally. */
		working,
		/** Completed the phase and should move on, or completed the infusion. */
		finished,
		/** Missing items or resources, cannot continue and producing instability. */
		stalling
	}
	
	public static class ItemPhase implements InfusionPhase{
		
		public @NotNull InfusionState tick(InfusionMatrixBlockEntity be, NbtCompound tag){
			if(!tag.contains("cooldown", NbtElement.INT_TYPE)){ // initial pulling
				tag.putInt("cooldown", 60);
				return working;
			}
			InfusionRecipe recipe = be.getCurrentRecipe();
			if(recipe != null){
				Ingredient next = nextIngredient(tag, recipe);
				if(next != null){
					var matching = be
							.inRange(pos1 -> be.world.getBlockEntity(pos1) instanceof PedestalBlockEntity p ? p : null)
							.filter(x -> next.test(x.getStack()))
							.findFirst();
					if(matching.isPresent()){
						var pedestal = matching.get();
						var stack = pedestal.getStack();
						var pPos = pedestal.getPos();
						if(be.world.getTime() % 2 == 0){
							Random rng = be.world.getRandom();
							var sx = pPos.getX() + rng.nextGaussian() / 8;
							var sy = pPos.getY() + rng.nextGaussian() / 8;
							var sz = pPos.getZ() + rng.nextGaussian() / 8;
							be.world.addParticle(
									new ItemStackParticleEffect(ArcanaRegistry.INFUSION_ITEM, stack),
									sx + .5, sy + 1.5, sz + .5,
									(be.pos.getX() - sx) / 10d,
									(be.pos.getY() - sy - 2) / 10d,
									(be.pos.getZ() - sz) / 10d
							);
						}
						if(tag.getInt("cooldown") > 0){
							tag.putInt("cooldown", tag.getInt("cooldown") - 1);
							return working;
						}
						var list = tag.getList("absorbed", NbtElement.COMPOUND_TYPE);
						list.add(stack.writeNbt(new NbtCompound()));
						tag.put("absorbed", list);
						pedestal.setStack(ItemStack.EMPTY);
						tag.putInt("cooldown", 60);
						return working;
					}else{
						return stalling;
					}
				}else
					return finished;
			}
			return finished;
		}
		
		@Nullable
		private static Ingredient nextIngredient(NbtCompound tag, InfusionRecipe recipe){
			List<ItemStack> absorbed = StreamUtil.streamAndApply(
					tag.getList("absorbed", NbtElement.COMPOUND_TYPE),
					NbtCompound.class,
					ItemStack::fromNbt
			).collect(Collectors.toCollection(ArrayList::new));
			// similar to recipe matching
			Ingredient next = null;
			ingredients:
			for(Ingredient ingredient : recipe.outerIngredients()){
				for(int i = 0; i < absorbed.size(); i++)
					if(ingredient.test(absorbed.get(i))){
						absorbed.remove(i);
						continue ingredients;
					}
				next = ingredient;
				break;
			}
			return next;
		}
	}
	
	public static class EssentiaPhase implements InfusionPhase{
		
		public @NotNull InfusionState tick(InfusionMatrixBlockEntity be, NbtCompound tag){
			if(be.world.getTime() % 2 != 0)
				return working;
			InfusionRecipe recipe = be.getCurrentRecipe();
			if(recipe != null){
				Aspect next = null;
				for(AspectStack stack : recipe.aspects())
					if(tag.getInt(stack.type().id().toString()) < stack.amount()){
						next = stack.type();
						break;
					}
				if(next == null)
					return finished; // nothing else to absorb
				Aspect tmpNext = next;
				Optional<WardedJarBlockEntity> first = be.inRange(be.world::getBlockEntity)
						.filter(WardedJarBlockEntity.class::isInstance)
						.map(WardedJarBlockEntity.class::cast)
						.filter(x -> x.getStored() != null && x.getStored().type().equals(tmpNext))
						.findFirst();
				if(first.isEmpty())
					return stalling; // we can't find any jars with the aspect we need
				var jar = first.get();
				var jarPos = jar.getPos();
				var xdiff = jarPos.getX() - be.pos.getX();
				var zdiff = jarPos.getZ() - be.pos.getZ();
				var xzdiff = Math.sqrt(xdiff * xdiff + zdiff * zdiff);
				be.world.addParticle(
						new AspectParticleEffect(ArcanaRegistry.ESSENTIA_STREAM, next),
						jarPos.getX() + .5, jarPos.getY() + .5, jarPos.getZ() + .5,
						// EssentiaStreamParticle uses these for x/y rotation
						Math.PI + Math.atan2(xdiff, zdiff),
						Math.PI / 2 - Math.atan2(be.pos.getY() - jarPos.getY() - 1, xzdiff),
						0
				);
				if(be.world.getTime() % 4 == 0){
					jar.draw(1);
					tag.putInt(next.id().toString(), tag.getInt(next.id().toString()) + 1);
				}
				return working;
			}
			return finished;
		}
	}
}