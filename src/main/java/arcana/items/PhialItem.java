package arcana.items;

import arcana.aspects.Aspect;
import arcana.aspects.AspectIo;
import arcana.aspects.AspectStack;
import arcana.aspects.Aspects;
import net.minecraft.block.Block;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUsage;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.stat.Stats;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.world.event.GameEvent;
import org.jetbrains.annotations.Nullable;

public class PhialItem extends Item{
	
	@Nullable("if empty")
	private final Aspect aspect;
	
	public PhialItem(Settings settings, @Nullable Aspect aspect){
		super(settings);
		this.aspect = aspect;
	}
	
	public ActionResult useOnBlock(ItemUsageContext ctx){
		var world = ctx.getWorld();
		var user = ctx.getPlayer();
		var pos = ctx.getBlockPos();
		var item = ctx.getStack();
		Block at = world.getBlockState(pos).getBlock();
		if(at instanceof AspectIo io){
			if(aspect == null){
				var stack = io.draw(8, world, pos, null);
				if(stack != null && stack.amount() >= 8){
					world.playSound(user, user.getX(), user.getY(), user.getZ(), SoundEvents.ITEM_BOTTLE_FILL, SoundCategory.NEUTRAL, 1.0F, 1.0F);
					world.emitGameEvent(user, GameEvent.FLUID_PICKUP, pos);
					user.setStackInHand(ctx.getHand(), fill(item, user, new ItemStack(Aspects.phials.get(stack.type()))));
					return ActionResult.SUCCESS;
				}
			}else{
				if(io.accept(new AspectStack(aspect, 8), world, pos, null)){
					if(!user.isCreative())
						item.decrement(1);
					return ActionResult.SUCCESS;
				}
			}
		}
		return super.useOnBlock(ctx);
	}
	
	protected ItemStack fill(ItemStack stack, PlayerEntity player, ItemStack output){
		player.incrementStat(Stats.USED.getOrCreateStat(this));
		return ItemUsage.exchangeStack(stack, player, output, false);
	}
	
	public @Nullable Aspect getAspect(){
		return aspect;
	}
	
	public Text getName(ItemStack stack){
		return getName();
	}
	
	public Text getName(){
		return aspect != null ? Text.translatable("item.arcana.phial", aspect.name()) : Text.translatable("item.arcana.empty_phial");
	}
}