package arcana.client.renderers;

import arcana.items.FocusItem;
import arcana.items.FocusPouchItem;
import arcana.items.WandItem;
import arcana.util.ArrayInventory;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntList;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;

import java.util.ArrayList;
import java.util.List;
import java.util.OptionalInt;

// see https://git.sleeping.town/unascribed-mods/Yttr/src/branch/1.20.1/src/main/java/diy/y2k/yttr/client/render/ui/RifleHUDRenderer.java
public final class FocusSwitcherRenderer{
	
	private static final int openAnimationTime = 10, closeAnimationTime = 10, swapAnimationTime = 5;
	
	private static int openTicks = -1, closedTicks = -1, changedTicks = -1, changeDir = 0;
	private static boolean inStorage = true;
	
	public static void renderHud(MatrixStack matrices, float delta){
		MinecraftClient mc = MinecraftClient.getInstance();
		PlayerEntity player = mc.player;
		if(player == null)
			return;
		ItemStack wandStack;
		ItemStack mainHand = player.getMainHandStack(), offHand = player.getOffHandStack();
		if((!((wandStack = mainHand).getItem() instanceof WandItem) && !((wandStack = offHand).getItem() instanceof WandItem)))
			return;
		
		if(openTicks == -1)
			return;
		
		IntList focusIndices = new IntArrayList();
		OptionalInt pouchIndex = OptionalInt.empty();
		
		List<ItemStack> storage = new ArrayList<>();
		List<ItemStack> quickAccess = new ArrayList<>();
		gatherFoci(player, storage, quickAccess);
		
		int storageY = 30;
		float introEase = Math.min(1, (openTicks + delta) / 9);
		introEase = 1 - (1 - introEase) * (1 - introEase) * (1 - introEase);
		introEase = introEase > 0.98 ? 1 : introEase;
		float spacing = introEase * 20;
		if(!quickAccess.isEmpty()){
			storageY += 20;
			for(int i = 0; i < quickAccess.size(); i++)
				mc.getItemRenderer().renderInGui(quickAccess.get(i), (int)(i * spacing + 60 + 10*introEase), 30);
		}
		for(int i = 0; i < storage.size(); i++)
			mc.getItemRenderer().renderInGui(storage.get(i), (int)(i * spacing + 60 + 10*introEase), storageY);
	}
	
	public static void tick(MinecraftClient client){
		MinecraftClient mc = MinecraftClient.getInstance();
		PlayerEntity player = mc.player;
		if(player == null)
			return;
		ItemStack wandStack;
		ItemStack mainHand = player.getMainHandStack(), offHand = player.getOffHandStack();
		if((!((wandStack = mainHand).getItem() instanceof WandItem) && !((wandStack = offHand).getItem() instanceof WandItem))){
			openTicks = closedTicks = changedTicks = -1;
			changeDir = 0;
			return;
		}
		
		if(mc.options.swapHandsKey.isPressed() || mc.options.swapHandsKey.wasPressed()){
			closedTicks = -1;
			openTicks++;
			// drain `timesPressed`
			//noinspection StatementWithEmptyBody
			while(mc.options.swapHandsKey.wasPressed()){}
			
			// TODO: scrolling
			if(mc.options.useKey.wasPressed()){
				mc.options.useKey.setPressed(false);
				go(player, -1, wandStack);
			}
			
			if(mc.options.attackKey.wasPressed()){
				mc.options.attackKey.setPressed(false);
				go(player, 1, wandStack);
			}
			
			if(mc.options.sneakKey.wasPressed()){
				mc.options.sneakKey.setPressed(false);
				inStorage = !inStorage;
			}
		}else{
			openTicks = changedTicks = -1;
			closedTicks++;
			inStorage = false;
		}
	}
	
	private static void go(PlayerEntity player, int dir, ItemStack wandStack){
		// TODO
		List<ItemStack> storage = new ArrayList<>();
		List<ItemStack> quickAccess = new ArrayList<>();
		gatherFoci(player, storage, quickAccess);
		
	}
	
	private static void gatherFoci(PlayerEntity player, List<ItemStack> storage, List<ItemStack> quickAccess){
		PlayerInventory playerInv = player.getInventory();
		for(int i = 0; i < playerInv.size(); i++){
			ItemStack stack = playerInv.getStack(i);
			if(stack.getItem() instanceof FocusItem)
				storage.add(stack);
			else if(stack.getItem() instanceof FocusPouchItem){
				ArrayInventory pouchInv = FocusPouchItem.inventoryFrom(stack);
				for(int j = 0; j < pouchInv.size(); j++)
					if(!pouchInv.getStack(j).isEmpty())
						(j < 9 ? quickAccess : storage).add(pouchInv.getStack(j));
			}
		}
	}
}