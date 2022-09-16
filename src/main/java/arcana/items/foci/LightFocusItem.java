package arcana.items.foci;

import arcana.ArcanaRegistry;
import arcana.aspects.AspectMap;
import arcana.aspects.AspectStack;
import arcana.aspects.Aspects;
import arcana.items.FocusItem;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.fluid.Fluids;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.util.ActionResult;
import net.minecraft.util.math.BlockPos;
import org.jetbrains.annotations.Nullable;

import java.util.List;

import static arcana.blocks.WaterloggableBlock.waterlogged;

public class LightFocusItem extends FocusItem{
	
	public LightFocusItem(Settings settings){
		super(settings);
	}
	
	public AspectMap castCost(@Nullable ItemStack wand, ItemStack focus, PlayerEntity user){
		return AspectMap.fromAspectStacks(List.of(new AspectStack(Aspects.AIR, 3), new AspectStack(Aspects.FIRE, 3)));
	}
	
	public ActionResult castOnBlock(ItemUsageContext ctx){
		BlockPos toSet = ctx.getBlockPos().offset(ctx.getSide());
		BlockState there = ctx.getWorld().getBlockState(toSet);
		if(there.isAir() || there.getMaterial().isReplaceable()){
			boolean wet = ctx.getWorld().getFluidState(toSet).isOf(Fluids.WATER);
			ctx.getWorld().setBlockState(toSet, ArcanaRegistry.LIGHT_BLOCK.getDefaultState().with(waterlogged, wet));
			return ActionResult.SUCCESS;
		}
		return super.castOnBlock(ctx);
	}
}