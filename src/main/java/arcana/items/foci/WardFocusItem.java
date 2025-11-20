package arcana.items.foci;

import arcana.ArcanaRegistry;
import arcana.aura.WardedChunk;
import arcana.items.FocusItem;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.util.ActionResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class WardFocusItem extends FocusItem{
	
	public WardFocusItem(Settings settings){
		super(settings);
	}
	
	public ActionResult castOnBlock(ItemUsageContext ctx){
		BlockPos pos = ctx.getBlockPos();
		World w = ctx.getWorld();
		boolean willWard = !WardedChunk.isWarded(w, pos);
		WardedChunk.setWarded(w, pos, willWard);
		if(willWard)
			w.addParticle(ArcanaRegistry.WARDING_EFFECT, pos.getX(), pos.getY(), pos.getZ(), 0, 0, 0);
		return ActionResult.SUCCESS;
	}
}