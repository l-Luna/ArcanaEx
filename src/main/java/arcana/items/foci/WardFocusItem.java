package arcana.items.foci;

import arcana.ArcanaRegistry;
import arcana.aspects.AspectMap;
import arcana.aspects.AspectStack;
import arcana.aspects.Aspects;
import arcana.aura.WardedChunk;
import arcana.client.particles.CubeParticleEffect;
import arcana.client.particles.CubeParticleStyle;
import arcana.items.FocusItem;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.util.ActionResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

public class WardFocusItem extends FocusItem{
	
	public WardFocusItem(Settings settings){
		super(settings);
	}
	
	public AspectMap deciCastCost(@Nullable ItemStack wand, ItemStack focus, PlayerEntity user){
		return AspectMap.fromAspectStacks(new AspectStack(Aspects.ORDER, 65), new AspectStack(Aspects.ORDER, 45));
	}
	
	public ActionResult castOnBlock(ItemUsageContext ctx){
		BlockPos pos = ctx.getBlockPos();
		World w = ctx.getWorld();
		boolean willWard = !WardedChunk.isWarded(w, pos);
		WardedChunk.setWarded(w, pos, willWard);
		w.addParticle(new CubeParticleEffect(ArcanaRegistry.WARDING_EFFECT, willWard ? CubeParticleStyle.APPEAR : CubeParticleStyle.DISAPPEAR), pos.getX(), pos.getY(), pos.getZ(), 0, 0, 0);
		return ActionResult.SUCCESS;
	}
}