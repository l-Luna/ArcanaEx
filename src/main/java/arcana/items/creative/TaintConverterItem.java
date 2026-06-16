package arcana.items.creative;

import arcana.ArcanaRegistry;
import arcana.aura.InfestedChunk;
import arcana.aura.Taint;
import arcana.client.particles.CubeParticleEffect;
import arcana.client.particles.CubeParticleStyle;
import arcana.items.components.ArcanaItemComponentTypes;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.item.tooltip.TooltipType;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class TaintConverterItem extends Item{
	
	private final boolean fwd, physical;
	
	public TaintConverterItem(Settings settings, boolean fwd, boolean physical){
		super(settings);
		this.fwd = fwd;
		this.physical = physical;
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
			for(BlockPos pos : BlockPos.iterate(here.add(-r, -r, -r), here.add(r, r, r))){
				if(!physical){
					if(!world.isAir(pos) && InfestedChunk.setInfested(world, pos, fwd))
						world.addParticle(new CubeParticleEffect(ArcanaRegistry.INFESTED_EFFECT, fwd ? CubeParticleStyle.APPEAR : CubeParticleStyle.DISAPPEAR), pos.getX(), pos.getY(), pos.getZ(), 0, 0, 0);
				}else if(fwd)
					Taint.taintBlock(world, pos);
				else
					Taint.untaintBlock(world, pos);
			}
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
		return stack.getOrDefault(ArcanaItemComponentTypes.RADIUS, 1);
	}
	
	private static void setRadiusFor(ItemStack stack, int radius){
		stack.set(ArcanaItemComponentTypes.RADIUS, radius);
	}
	
	public void appendTooltip(ItemStack stack, @Nullable TooltipContext ctx, List<Text> tooltip, TooltipType type){
		super.appendTooltip(stack, ctx, tooltip, type);
		tooltip.add(Text.translatable("tooltip.arcana.radius", radiusFor(stack)));
	}
}