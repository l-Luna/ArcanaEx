package arcana.items;

import arcana.ArcanaRegistry;
import arcana.components.MagicMirrorQueue;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.StackReference;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.screen.slot.Slot;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.ClickType;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class PersonalMagicMirrorItem extends Item{
	
	public PersonalMagicMirrorItem(Settings settings){
		super(settings);
	}
	
	public void appendTooltip(ItemStack stack, @Nullable World world, List<Text> tooltip, TooltipContext context){
		super.appendTooltip(stack, world, tooltip, context);
		tooltip.add(Text.translatable("tooltip.arcana.personal_magic_mirror." + (getTargetPos(stack) != null ? "" : "un") + "bound").formatted(Formatting.GRAY));
	}
	
	public ActionResult useOnBlock(ItemUsageContext ctx){
		BlockPos pos = ctx.getBlockPos();
		if(ctx.getWorld().getBlockState(pos).getBlock() == ArcanaRegistry.MAGIC_MIRROR){
			setTargetPos(ctx.getStack(), pos);
			return ActionResult.CONSUME;
		}
		return super.useOnBlock(ctx);
	}
	
	public boolean onClicked(ItemStack stack, ItemStack otherStack, Slot slot, ClickType clickType, PlayerEntity player, StackReference ref){
		if(player.world.isClient)
			return false; // TODO: creative inventory
		if(clickType == ClickType.RIGHT && !otherStack.isEmpty()){
			BlockPos target = getTargetPos(stack);
			if(target != null){
				MagicMirrorQueue.from(player.world).push(target, otherStack);
				ref.set(ItemStack.EMPTY);
				return true;
			}
		}
		return super.onClicked(stack, otherStack, slot, clickType, player, ref);
	}
	
	//
	
	@Nullable
	private static BlockPos getTargetPos(ItemStack mirrorStack){
		NbtCompound nbt = mirrorStack.getNbt();
		return nbt != null && nbt.contains("target") ? BlockPos.fromLong(nbt.getLong("target")) : null;
	}
	
	private static void setTargetPos(ItemStack mirrorStack, BlockPos target){
		mirrorStack.getOrCreateNbt().putLong("target", target.asLong());
	}
}