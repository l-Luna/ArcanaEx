package arcana.client.renderers;

import arcana.items.WandItem;
import arcana.network.PkSwapFocus;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.RenderTickCounter;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.StackReference;
import net.minecraft.item.ItemStack;

import java.util.ArrayList;
import java.util.List;

// see https://git.sleeping.town/unascribed-mods/Yttr/src/branch/1.20.1/src/main/java/diy/y2k/yttr/client/render/ui/RifleHUDRenderer.java
public final class FocusSwitcherRenderer{
	
	private static final int openAnimationTime = 9, closeAnimationTime = 10, swapAnimationTime = 5;
	
	private static int openTicks = -1, closedTicks = -1;
	private static boolean inStorage = true;
	
	// TODO: combine with HudRenderer, or otherwise adjust for wand HUD position
	public static void renderHud(DrawContext matrices, RenderTickCounter delta){
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
		
		List<StackReference> storage = new ArrayList<>();
		List<StackReference> quickAccess = new ArrayList<>();
		PkSwapFocus.gatherFoci(player, storage, quickAccess);
		
		int storageY = 30;
		float introEase = Math.min(1, (openTicks + delta) / openAnimationTime);
		introEase = 1 - (1 - introEase) * (1 - introEase) * (1 - introEase);
		introEase = introEase > 0.98 ? 1 : introEase;
		float spacing = introEase * 20;
		if(!quickAccess.isEmpty()){
			storageY += 20;
			for(int i = 0; i < quickAccess.size(); i++)
				mc.getItemRenderer().renderInGui(quickAccess.get(i).get(), (int)(i * spacing + 60 + 10*introEase), 30);
		}
		for(int i = 0; i < storage.size(); i++)
			mc.getItemRenderer().renderInGui(storage.get(i).get(), (int)(i * spacing + 60 + 10*introEase), storageY);
	}
	
	public static void tick(MinecraftClient client){
		MinecraftClient mc = MinecraftClient.getInstance();
		PlayerEntity player = mc.player;
		if(player == null)
			return;
		ItemStack wandStack;
		ItemStack mainHand = player.getMainHandStack(), offHand = player.getOffHandStack();
		if((!((wandStack = mainHand).getItem() instanceof WandItem) && !((wandStack = offHand).getItem() instanceof WandItem))){
			openTicks = closedTicks = -1;
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
				new PkSwapFocus(false).sendToServer();
				PkSwapFocus.go(player, false, wandStack);
			}
			
			if(mc.options.attackKey.wasPressed()){
				mc.options.attackKey.setPressed(false);
				new PkSwapFocus(true).sendToServer();
				PkSwapFocus.go(player, true, wandStack);
			}
			
			if(mc.options.sneakKey.wasPressed()){
				mc.options.sneakKey.setPressed(false);
				inStorage = !inStorage;
			}
		}else{
			openTicks = -1;
			closedTicks++;
			inStorage = false;
		}
	}
}