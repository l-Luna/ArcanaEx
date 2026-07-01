package arcana.items;

import arcana.aspects.AspectMap;
import arcana.aspects.ItemAspectRegistry;
import arcana.items.components.ArcanaDataComponents;
import arcana.items.components.HoldingJugContentsComponent;
import net.fabricmc.fabric.api.item.v1.FabricItem;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.StackReference;
import net.minecraft.item.*;
import net.minecraft.item.tooltip.TooltipData;
import net.minecraft.screen.slot.Slot;
import net.minecraft.util.ActionResult;
import net.minecraft.util.ClickType;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.RaycastContext;
import net.minecraft.world.World;
import net.minecraft.world.event.GameEvent;

import java.util.List;
import java.util.Optional;

public class HoldingJugItem extends Item implements FabricItem{
	
	public static final int MAX_ASPECT_TYPES = 4;
	public static final int MAX_CAPACITY = 2048;
	public static final int MAX_ITEM_TYPES = 25;
	
	public HoldingJugItem(Item.Settings settings){
		super(settings.component(ArcanaDataComponents.HOLDING_JUG_CONTENTS, HoldingJugContentsComponent.DEFAULT));
	}
	
	public boolean allowComponentsUpdateAnimation(PlayerEntity player, Hand hand, ItemStack oldStack, ItemStack newStack){
		return false;
	}
	
	// item storage
	
	public Optional<TooltipData> getTooltipData(ItemStack stack){
		return Optional.ofNullable(stack.get(ArcanaDataComponents.HOLDING_JUG_CONTENTS));
	}
	
	public boolean onClicked(ItemStack stack, ItemStack otherStack, Slot slot, ClickType clickType, PlayerEntity player, StackReference cursor){
		if(clickType != ClickType.RIGHT)
			return false;
		HoldingJugContentsComponent contents = stack.getOrDefault(ArcanaDataComponents.HOLDING_JUG_CONTENTS, HoldingJugContentsComponent.DEFAULT);
		ItemStack cursorStack = cursor.get();
		if(cursorStack.isEmpty()){
			// attempt to take an item out
			if(!contents.stacks().isEmpty()){
				cursor.set(contents.stacks().getFirst());
				stack.set(ArcanaDataComponents.HOLDING_JUG_CONTENTS, contents.withoutFirst());
				return true;
			}
		}else{
			// attempt to put a stack in; check stack count first, then aspect totals
			if(!cursorStack.getItem().canBeNested())
				return false;
			if(contents.stacks().size() >= MAX_ITEM_TYPES)
				return false;
			AspectMap itemAspects = ItemAspectRegistry.get(cursorStack);
			if(itemAspects.isEmpty())
				return false;
			AspectMap held = contents.aspects().copy();
			held.add(itemAspects);
			if(held.aspectSet().size() > MAX_ASPECT_TYPES || held.total() > MAX_CAPACITY)
				return false;
			stack.set(ArcanaDataComponents.HOLDING_JUG_CONTENTS, contents.withFirst(cursorStack));
			cursor.set(ItemStack.EMPTY);
			return true;
		}
		return super.onClicked(stack, otherStack, slot, clickType, player, cursor);
	}
	
	public boolean canBeNested(){
		return false;
	}
	
	// pouring items out
	
	public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand){
		user.setCurrentHand(hand);
		return TypedActionResult.consume(user.getStackInHand(hand));
	}
	
	public void usageTick(World world, LivingEntity user, ItemStack stack, int remainingUseTicks){
		if(!world.isClient && user instanceof PlayerEntity player && world.getTime() % 15 == 0){
			HoldingJugContentsComponent contents = stack.getOrDefault(ArcanaDataComponents.HOLDING_JUG_CONTENTS, HoldingJugContentsComponent.DEFAULT);
			List<ItemStack> stacks = contents.stacks();
			if(!stacks.isEmpty()){
				// find the target location
				HitResult raycast = raycast(world, player, RaycastContext.FluidHandling.NONE);
				if(raycast.getType() == HitResult.Type.MISS)
					return;
				Vec3d location = raycast.getPos();
				// pick a random item to pour
				int index = world.random.nextInt(stacks.size());
				ItemStack there = stacks.get(index);
				// either drop it or place it
				if(there.getItem() instanceof BlockItem block){
					Direction face = raycast instanceof BlockHitResult bhr ? bhr.getSide() : Direction.UP;
					ItemPlacementContext ctx = new AutomaticItemPlacementContext(world, BlockPos.ofFloored(location), player.getHorizontalFacing(), there, face);
					ActionResult place = block.place(ctx);
					if(!place.isAccepted()){
						// failed to place the block, don't consume item
						return;
					}
				}else{
					ItemEntity spawned = new ItemEntity(world, location.x, location.y, location.z, there.copyWithCount(1));
					world.spawnEntity(spawned);
					world.emitGameEvent(spawned, GameEvent.ENTITY_PLACE, location);
				}
				// remove it from the jug
				if(there.getCount() == 1)
					contents = contents.without(index);
				else
					contents = contents.without(index).with(index, there.copyWithCount(there.getCount() - 1));
				stack.set(ArcanaDataComponents.HOLDING_JUG_CONTENTS, contents);
			}else
				user.stopUsingItem();
		}
	}
	
	public int getMaxUseTime(ItemStack stack, LivingEntity user){
		return 72000;
	}
}