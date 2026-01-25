package arcana.items;

import arcana.ArcanaRegistry;
import arcana.api.WarpingItem;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class TwinHeartbeatRingItem extends RingItem implements WarpingItem{
	
	public TwinHeartbeatRingItem(Settings settings){
		super(settings, 0, 0);
	}
	
	public int warping(ItemStack stack, PlayerEntity player){
		return 2;
	}
	
	public void appendTooltip(ItemStack stack, @Nullable World world, List<Text> tooltip, TooltipContext context){
		super.appendTooltip(stack, world, tooltip, context);
		tooltip.add(ArcanaRegistry.WARPING.getName(2));
	}
}