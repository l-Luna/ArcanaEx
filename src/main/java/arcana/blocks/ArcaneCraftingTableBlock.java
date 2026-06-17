package arcana.blocks;

import arcana.screens.ArcaneCraftingScreen;
import com.mojang.serialization.MapCodec;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.screen.NamedScreenHandlerFactory;
import net.minecraft.screen.ScreenHandlerContext;
import net.minecraft.screen.SimpleNamedScreenHandlerFactory;
import net.minecraft.stat.Stats;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

@SuppressWarnings("deprecation")
public class ArcaneCraftingTableBlock extends WaterloggableBlock{
	
	private static final MapCodec<ArcaneCraftingTableBlock> CODEC = createCodec(ArcaneCraftingTableBlock::new);
	private static final Text TITLE = Text.translatable("container.crafting");
	
	public ArcaneCraftingTableBlock(Settings settings){
		super(settings);
	}
	
	protected MapCodec<? extends Block> getCodec(){
		return CODEC;
	}
	
	public ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, BlockHitResult hit){
		if(world.isClient)
			return ActionResult.SUCCESS;
		else{
			player.openHandledScreen(state.createScreenHandlerFactory(world, pos));
			player.incrementStat(Stats.INTERACT_WITH_CRAFTING_TABLE);
			return ActionResult.CONSUME;
		}
	}
	
	public NamedScreenHandlerFactory createScreenHandlerFactory(BlockState state, World world, BlockPos pos) {
		return new SimpleNamedScreenHandlerFactory(
				(syncId, inventory, player) -> new ArcaneCraftingScreen.Handler(syncId, inventory, ScreenHandlerContext.create(world, pos)), TITLE
		);
	}
}