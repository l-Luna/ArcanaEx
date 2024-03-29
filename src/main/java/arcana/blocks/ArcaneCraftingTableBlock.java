package arcana.blocks;

import arcana.screens.ArcaneCraftingScreenHandler;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.screen.NamedScreenHandlerFactory;
import net.minecraft.screen.ScreenHandlerContext;
import net.minecraft.screen.SimpleNamedScreenHandlerFactory;
import net.minecraft.stat.Stats;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

@SuppressWarnings("deprecation")
public class ArcaneCraftingTableBlock extends WaterloggableBlock{
	
	private static final Text title = Text.translatable("container.crafting");
	
	public ArcaneCraftingTableBlock(Settings settings){
		super(settings);
	}
	
	public ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockHitResult hit){
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
				(syncId, inventory, player) -> new ArcaneCraftingScreenHandler(syncId, inventory, ScreenHandlerContext.create(world, pos)), title
		);
	}
}