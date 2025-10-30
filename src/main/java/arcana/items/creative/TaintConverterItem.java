package arcana.items.creative;

import arcana.aura.Taint;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class TaintConverterItem extends Item{
	
	private final boolean fwd;
	
	public TaintConverterItem(Settings settings, boolean fwd){
		super(settings);
		this.fwd = fwd;
	}
	
	public ActionResult useOnBlock(ItemUsageContext context){
		BlockPos here = context.getBlockPos();
		World world = context.getWorld();
		PlayerEntity player = context.getPlayer();
		ItemStack stack = context.getStack();
		if(player != null && player.isSneaking())
			cycleRadius(stack, player);
		else{
			int r = radiusFor(stack);
			for(BlockPos pos : BlockPos.iterate(here.add(-r, -r, -r), here.add(r, r, r)))
				if(fwd)
					Taint.taintBlock(world, pos);
				else
					Taint.untaintBlock(world, pos);
		}
		return ActionResult.SUCCESS;
	}
	
	public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand){
		if(user.isSneaking()){
			ItemStack stack = user.getStackInHand(hand);
			cycleRadius(stack, user);
			return TypedActionResult.success(stack);
		}
		return super.use(world, user, hand);
	}
	
	private static void cycleRadius(ItemStack stack, PlayerEntity player){
		int nr = (radiusFor(stack) % 8) + 1;
		setRadiusFor(stack, nr);
		player.sendMessage(Text.translatable("tooltip.arcana.radius", nr), true);
	}
	
	private static int radiusFor(ItemStack stack){
		NbtCompound nbt = stack.getNbt();
		return nbt == null ? 1 : nbt.getInt("radius");
	}
	
	private static void setRadiusFor(ItemStack stack, int radius){
		stack.getOrCreateNbt().putInt("radius", radius);
	}
	
	public void appendTooltip(ItemStack stack, @Nullable World world, List<Text> tooltip, TooltipContext context){
		super.appendTooltip(stack, world, tooltip, context);
		tooltip.add(Text.translatable("tooltip.arcana.radius", radiusFor(stack)));
	}
}