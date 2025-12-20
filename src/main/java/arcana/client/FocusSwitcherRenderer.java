package arcana.client;

import arcana.items.WandItem;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntList;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;

import java.util.OptionalInt;

// see https://git.sleeping.town/unascribed-mods/Yttr/src/branch/1.20.1/src/main/java/diy/y2k/yttr/client/render/ui/RifleHUDRenderer.java
public class FocusSwitcherRenderer{
	
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
		
		/*List<ItemStack> storage = new ArrayList<>();
		List<ItemStack> quickAccess = new ArrayList<>();
		var pInv = player.getInventory();
		for(int i = 0; i < pInv.size(); i++){
			ItemStack stack = pInv.getStack(i);
			if(stack.getItem() instanceof FocusItem)
				storage.add(stack);
		}*/
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
			
			if(mc.options.useKey.wasPressed()){
				mc.options.useKey.setPressed(false);
				go(-1, wandStack);
			}
			
			if(mc.options.attackKey.wasPressed()){
				mc.options.attackKey.setPressed(false);
				go(1, wandStack);
			}
		}else{
			openTicks = changedTicks = -1;
			closedTicks++;
		}
	}
	
	private static void go(int dir, ItemStack wandStack){
		// TODO
	}
}