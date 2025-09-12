package arcana.items;

import arcana.aspects.AspectMap;
import arcana.aspects.AspectStack;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Formatting;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class FocusItem extends Item{
	
	public FocusItem(Settings settings){
		super(settings);
	}
	
	public Text nameForTooltip(ItemStack focusStack){
		return Text.translatable(getTranslationKey(focusStack)).formatted(Formatting.AQUA);
	}
	
	@Environment(EnvType.CLIENT)
	public void appendTooltip(ItemStack stack, @Nullable World world, List<Text> tooltip, TooltipContext context){
		tooltip.add(WandItem.costText(castCost(stack, null, MinecraftClient.getInstance().player)));
	}
	
	// TODO: split into Focus interface, like Cap/Core?
	public AspectMap castCost(@Nullable ItemStack wand, ItemStack focus, PlayerEntity user){
		return new AspectMap();
	}
	
	public ActionResult castOnBlock(ItemUsageContext ctx){
		return ActionResult.PASS;
	}
	
	public ActionResult castOnEntity(ItemStack wand, ItemStack focus, PlayerEntity user, LivingEntity target){
		return ActionResult.PASS;
	}
	
	public boolean isContinuous(){
		return false;
	}
	
	public void startContinuousCast(ContinuousCastContext ccc){}
	public void tickContinuousCast(ContinuousCastContext ccc){}
	public void endContinuousCast(ContinuousCastContext ccc){}
	
	public static final class ContinuousCastContext{
		public final ItemStack wand;
		public final ItemStack focus;
		public final PlayerEntity user;
		public final NbtCompound state;
		public final int castTime;
		
		private boolean stopping = false;
		
		public ContinuousCastContext(ItemStack wand, ItemStack focus, PlayerEntity user, NbtCompound state, int castTime){
			this.wand = wand;
			this.focus = focus;
			this.user = user;
			this.state = state;
			this.castTime = castTime;
		}
		
		public boolean requestDrain(AspectMap required){
			if(WandItem.aspectsFrom(wand).contains(required)){
				WandItem.updateAspects(wand, stored -> stored.take(required));
				return true;
			}
			return false;
		}
		
		public void recharge(AspectMap added){
			WandItem.updateAspects(wand, stored -> {
				for(AspectStack stack : added.asStacks())
					stored.addCapped(stack, WandItem.capacity(wand));
			});
		}
		
		public void stop(){
			stopping = true;
		}
		
		public boolean isStopping(){
			return stopping;
		}
	}
}