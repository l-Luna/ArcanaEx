package arcana.items;

import arcana.ArcanaRegistry;
import arcana.util.SearchUtil;
import dev.emi.trinkets.api.SlotReference;
import dev.emi.trinkets.api.TrinketItem;
import net.minecraft.block.BlockState;
import net.minecraft.entity.LivingEntity;
import net.minecraft.fluid.Fluids;
import net.minecraft.item.ItemStack;
import net.minecraft.state.property.Properties;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class LamplightTrinketItem extends TrinketItem{
	
	public LamplightTrinketItem(Settings settings){
		super(settings);
	}
	
	public void tick(ItemStack stack, SlotReference slot, LivingEntity entity){
		if(entity.isSpectator())
			return;
		super.tick(stack, slot, entity);
		World world = entity.getWorld();
		if(world.isClient)
			return;
		BlockPos userPos = BlockPos.ofFloored(entity.getEyePos());
		if(world.getTime() % 20 == 10)
			proc(world, userPos);
		if(world.getTime() % 20 * 3 == 30)
			SearchUtil.randomSearch(world, userPos, 6, 10, (pos, __) -> proc(world, pos));
	}
	
	private static boolean proc(World world, BlockPos userPos){
		BlockState there = world.getBlockState(userPos);
		if((there.isAir() || there.isReplaceable()) && world.getLightLevel(userPos) < 8){
			boolean wet = world.getFluidState(userPos).isOf(Fluids.WATER);
			world.setBlockState(userPos, ArcanaRegistry.TEMPORARY_LIGHT_BLOCK.getDefaultState().with(Properties.WATERLOGGED, wet));
			return true;
		}
		return false;
	}
}