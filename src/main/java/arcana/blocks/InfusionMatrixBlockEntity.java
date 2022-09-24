package arcana.blocks;

import arcana.ArcanaRegistry;
import arcana.aspects.AspectMap;
import arcana.recipes.InfusionInventory;
import arcana.recipes.InfusionRecipe;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.util.math.BlockPos;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Stream;

public class InfusionMatrixBlockEntity extends BlockEntity{
	
	public static final List<InfusionPhase> phases = List.of(
			new ItemPhase(),
			new EssentiaPhase()
	);
	
	private InfusionRecipe crafting;
	private Map<InfusionPhase, NbtCompound> states = null;
	private int curPhase = -1;
	private InfusionState curState = null;
	
	public InfusionMatrixBlockEntity(BlockPos pos, BlockState state){
		super(ArcanaRegistry.INFUSION_MATRIX_BE, pos, state);
	}
	
	protected void tick(){
		BlockEntity pedestal = world.getBlockEntity(pos.down(2));
		if(!(pedestal instanceof PedestalBlockEntity pbe) || pbe.getStack().isEmpty())
			reset();
		if(crafting != null){
			// setup
			if(curPhase == -1){
				curPhase = 0;
				states = new HashMap<>(phases.size());
			}
			if(phases.size() > curPhase){
				var phase = phases.get(curPhase);
				curState = phase.tick(this, states.computeIfAbsent(phase, __ -> new NbtCompound()));
				if(curState == InfusionState.finished)
					curPhase++;
				else if(curState == InfusionState.stalling){
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
	
	public void activate(){
		// TODO: setup infusion pillars...
		
		if(crafting != null)
			return;
		// find a valid recipe
		BlockEntity pedestal = world.getBlockEntity(pos.down(2));
		if(pedestal instanceof PedestalBlockEntity pbe && !pbe.getStack().isEmpty()){
			ItemStack centre = pbe.getStack();
			List<ItemStack> outers = inRange(pos -> {
				if(world.getBlockEntity(pos) instanceof PedestalBlockEntity p && !p.getStack().isEmpty())
					return p.getStack();
				return null;
			}).toList();
			AspectMap aspects = new AspectMap();
			
			InfusionInventory inv = new InfusionInventory(centre, outers, aspects);
			world.getRecipeManager().getFirstMatch(InfusionRecipe.TYPE, inv, world).ifPresent(recipe -> {
				crafting = recipe;
			});
		}
	}
	
	private void reset(){
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
	
	public InfusionRecipe getCrafting(){
		return crafting;
	}
	
	public interface InfusionPhase{
		
		@NotNull
		InfusionState tick(InfusionMatrixBlockEntity be, NbtCompound tag);
		
		@Environment(EnvType.CLIENT)
		void render(InfusionMatrixBlockEntity be, MatrixStack matrices, VertexConsumerProvider vcp, float tickDelta);
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
			return InfusionState.finished;
		}
		
		@Environment(EnvType.CLIENT)
		public void render(InfusionMatrixBlockEntity be, MatrixStack matrices, VertexConsumerProvider vcp, float tickDelta){
		
		}
	}
	
	public static class EssentiaPhase implements InfusionPhase{
		
		public @NotNull InfusionState tick(InfusionMatrixBlockEntity be, NbtCompound tag){
			return InfusionState.finished;
		}
		
		@Environment(EnvType.CLIENT)
		public void render(InfusionMatrixBlockEntity be, MatrixStack matrices, VertexConsumerProvider vcp, float tickDelta){
		
		}
	}
}