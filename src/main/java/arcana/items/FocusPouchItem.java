package arcana.items;

import arcana.screens.FocusPouchScreen;
import arcana.util.ArrayInventory;
import net.fabricmc.fabric.api.item.v1.FabricItem;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtElement;
import net.minecraft.screen.SimpleNamedScreenHandlerFactory;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.world.World;

public class FocusPouchItem extends Item implements FabricItem{
	
	public FocusPouchItem(Settings settings){
		super(settings);
	}
	
	public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand){
		ItemStack stack = user.getStackInHand(hand);
		if(!world.isClient)
			user.openHandledScreen(new SimpleNamedScreenHandlerFactory(
					(syncId, inv, player) -> new FocusPouchScreen.Handler(syncId, inv, inventoryFrom(stack)),
					stack.getName()
			));
		return new TypedActionResult<>(ActionResult.SUCCESS, stack);
	}
	
	public ArrayInventory inventoryFrom(ItemStack stack){
		ArrayInventory inventory = new ArrayInventory(9*3);
		inventory.readNbtList(stack.getOrCreateNbt().getList("Items", NbtElement.COMPOUND_TYPE));
		inventory.addListener(i -> setInventory(stack, (ArrayInventory)i));
		return inventory;
	}
	
	public void setInventory(ItemStack stack, ArrayInventory inventory){
		stack.getOrCreateNbt().put("Items", inventory.toNbtList());
	}
	
	public boolean allowNbtUpdateAnimation(PlayerEntity player, Hand hand, ItemStack oldStack, ItemStack newStack){
		return false;
	}
}