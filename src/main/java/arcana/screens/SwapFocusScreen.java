package arcana.screens;

import arcana.items.WandItem;
import arcana.network.PkSwapFocus;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawableHelper;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import net.minecraft.util.Hand;

import java.util.List;
import java.util.Random;

import static arcana.network.PkSwapFocus.getFoci;

public class SwapFocusScreen extends Screen{
	
	private static final Random random = new Random();
	public static KeyBinding swapFocus;
	
	private final Hand hand;
	private boolean hasClicked = false;
	
	protected SwapFocusScreen(Hand hand){
		super(Text.literal(""));
		this.hand = hand;
	}
	
	public void render(MatrixStack matrices, int mouseX, int mouseY, float delta){
		// who needs sprites when you have squares
		ItemStack wandStack = client.player.getStackInHand(hand);
		if(wandStack.getItem() instanceof WandItem){
			ItemStack currentFocus = WandItem.focusFrom(wandStack);
			if(!currentFocus.isEmpty())
				client.getItemRenderer().renderInGui(currentFocus, width / 2 - 8, height / 2 - 8);
			List<ItemStack> foci = getFoci(client.player);
			int size = foci.size();
			int distance = size * 5 + 28;
			int particleCount = size * 12 + 16;
			for(int i = 0; i < particleCount; i++){
				random.setSeed(i);
				float v = (float)Math.toRadians((i + (client.world.getTime() + delta) / (5f + random.nextInt(5) - 2)) * (360f / (particleCount)));
				int red = random.nextInt(128) + 127, green = random.nextInt(128) + 127, blue = random.nextInt(128) + 127;
				int colour = 0x6F000000 | (red << 16) | (green << 8) | blue;
				int thisDist = distance + random.nextInt(21) - 10;
				int x = (int)(Math.cos(v) * (thisDist + 4)) - 4 + width / 2;
				int y = (int)(Math.sin(v) * (thisDist + 4)) - 4 + height / 2;
				DrawableHelper.fillGradient(matrices, x, y, x + 8, y + 8, colour, colour, 0);
			}
			for(int i = 0; i < size; i++){
				ItemStack focus = foci.get(i);
				var v = (float)Math.toRadians(i * (360f / size));
				int x = (int)(Math.cos(v) * distance) - 8 + width / 2;
				int y = (int)(Math.sin(v) * distance) - 8 + height / 2;
				client.getItemRenderer().renderInGui(focus, x, y);
			}
		}
	}
	
	public boolean mouseClicked(double mouseX, double mouseY, int button){
		return select(mouseX, mouseY, true);
	}
	
	public boolean keyReleased(int keyCode, int scanCode, int modifiers){
		if(!swapFocus.isPressed()){
			double mx = client.mouse.getX() * client.getWindow().getScaledWidth() / client.getWindow().getWidth();
			double my = client.mouse.getY() * client.getWindow().getScaledHeight() / client.getWindow().getHeight();
			
			if(!hasClicked) // don't select foci if one has already been selected
				select(mx, my, false);
			
			client.setScreen(null);
			
			return true;
		}
		return false;
	}
	
	private boolean select(double mouseX, double mouseY, boolean clicked){
		// don't auto-select on close if one was selected
		hasClicked = true;
		
		// find the nearest focus
		// click in the middle to remove current focus
		if(mouseX >= (width / 2f) - 8 && mouseX < (width / 2f) + 8 && mouseY >= (height / 2f) - 8 && mouseY < (height / 2f) + 8){
			// just closing the screen in the middle does nothing
			if(!clicked)
				return false;
			// swap focus to -1 = none
			new PkSwapFocus(hand, -1).sendToServer();
			return true;
		}
		
		List<ItemStack> foci = getFoci(client.player);
		int size = foci.size();
		// pick based on angles
		if(size > 0){
			double angle = Math.toDegrees(Math.atan2(mouseY - height / 2d, mouseX - width / 2d)) + (180d / size);
			angle = angle % 360;
			angle = angle < 0 ? 360 + angle : angle;
			int item = (int)(angle / (360d / size));
			new PkSwapFocus(hand, item).sendToServer();
			return true;
		}
		return false;
	}
	
	public boolean shouldPause(){
		return false;
	}
	
	public static void tryOpen(MinecraftClient client){
		while(swapFocus.wasPressed())
			if(client.currentScreen == null && client.player != null)
				for(Hand hand : Hand.values())
					if(client.player.getStackInHand(hand).getItem() instanceof WandItem)
						client.setScreen(new SwapFocusScreen(hand));
	}
}