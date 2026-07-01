package arcana.items;

import arcana.items.components.ArcanaDataComponents;
import arcana.screens.FocusPouchScreen;
import arcana.util.ArrayInventory;
import net.fabricmc.fabric.api.item.v1.FabricItem;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.SimpleNamedScreenHandlerFactory;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.world.World;

public class FocusPouchItem extends Item implements FabricItem{
	
	public FocusPouchItem(Item.Settings settings){
		super(settings.component(ArcanaDataComponents.FOCUS_POUCH_INVENTORY, new ArrayInventory(27)));
	}
	
	public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand){
		ItemStack stack = user.getStackInHand(hand);
		if(!world.isClient)
			user.openHandledScreen(new SimpleNamedScreenHandlerFactory(
					(syncId, inv, player) -> new FocusPouchScreen.Handler(syncId, inv, inventoryFrom(stack), stack),
					stack.getName()
			));
		return new TypedActionResult<>(ActionResult.SUCCESS, stack);
	}
	
	public static ArrayInventory inventoryFrom(ItemStack stack){
		return stack.get(ArcanaDataComponents.FOCUS_POUCH_INVENTORY).copy();
	}
	
	public static void setInventory(ItemStack stack, ArrayInventory inventory){
		stack.set(ArcanaDataComponents.FOCUS_POUCH_INVENTORY, inventory);
	}
	
	public boolean allowComponentsUpdateAnimation(PlayerEntity player, Hand hand, ItemStack oldStack, ItemStack newStack){
		return false;
	}
}