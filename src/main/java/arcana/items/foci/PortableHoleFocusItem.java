package arcana.items.foci;

import arcana.aspects.AspectMap;
import arcana.aspects.AspectStack;
import arcana.aspects.Aspects;
import arcana.items.FocusItem;
import arcana.items.WandItem;
import com.google.common.collect.Lists;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.util.ActionResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction.Axis;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class PortableHoleFocusItem extends FocusItem{
	
	public PortableHoleFocusItem(Settings settings){
		super(settings);
	}
	
	public AspectMap deciCastCost(@Nullable ItemStack wand, ItemStack focus, PlayerEntity user){
		return AspectMap.fromAspectStacks(new AspectStack(Aspects.ENTROPY, 100), new AspectStack(Aspects.ORDER, 55));
	}
	
	public ActionResult castOnBlock(ItemUsageContext ctx){
		ItemStack wandStack = ctx.getStack();
		// requires better than an iron cap
		int complexity = WandItem.focusComplexity(wandStack);
		int difficulty = WandItem.focusStrength(wandStack, ctx.getPlayer());
		if(complexity < 12)
			return ActionResult.FAIL;
		// thanks una
		World world = ctx.getWorld();
		int distance = 12 + Math.round(0.14f * difficulty);
		List<Axis> axes = List.of(Axis.values());
		Axis fwd = ctx.getSide().getAxis();
		Axis axisX = axes.stream().filter(a -> a != fwd).findFirst().get();
		Axis axisY = Lists.reverse(axes).stream().filter(a -> a != fwd).findFirst().get();
		for(int z = -1; z < distance; z++){
			BlockPos pos = ctx.getBlockPos().offset(ctx.getSide().getOpposite(), z);
			boolean didPhase = false;
			
			for(int x = -1; x <= 1; x++){
				for(int y = -1; y <= 1; y++){
					BlockPos local = pos.offset(axisX, x).offset(axisY, y);
					BlockState state = world.getBlockState(local);
					if(!state.isAir())
						didPhase = true;
					if(state.getHardness(world, local) != -1)
						world.phaseBlock(local, 100, 20 * z);
				}
			}
			
			if(z >= 0 && !didPhase)
				break;
		}
		return ActionResult.SUCCESS;
	}
}