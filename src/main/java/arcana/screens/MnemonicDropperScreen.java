package arcana.screens;

import arcana.ArcanaRegistry;
import arcana.api.RequestableScreenHandler;
import arcana.network.PkScreenRequest;
import arcana.util.InventoryUtil;
import arcana.util.MnemonicInventoryData;
import com.mojang.blaze3d.systems.RenderSystem;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.Drawable;
import net.minecraft.client.gui.Element;
import net.minecraft.client.gui.Selectable;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.client.gui.screen.narration.NarrationMessageBuilder;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.Inventory;
import net.minecraft.inventory.SimpleInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.ArrayPropertyDelegate;
import net.minecraft.screen.Property;
import net.minecraft.screen.PropertyDelegate;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.slot.Slot;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RotationAxis;
import org.jetbrains.annotations.Nullable;

import java.util.List;

import static arcana.Arcana.arcId;

@Environment(EnvType.CLIENT)
public class MnemonicDropperScreen extends HandledScreen<MnemonicDropperScreen.Handler>{
	
	private static final Identifier TEXTURE = arcId("textures/gui/container/mnemonic_dropper.png");
	
	private final PlayerEntity player;
	
	public MnemonicDropperScreen(Handler handler, PlayerInventory pInv, Text title){
		super(handler, pInv, title);
		player = pInv.player;
	}
	
	protected void init(){
		super.init();
		titleX = (backgroundWidth - textRenderer.getWidth(title)) / 2;
		titleY -= 2;
		
		for(int i = 0; i < 9; i++)
			addDrawableChild(new SlotCountGear(i, x + 7 + 18 * i, y + 38));
	}
	
	public void render(DrawContext ctx, int mouseX, int mouseY, float delta){
		renderBackground(ctx, mouseX, mouseY, delta);
		super.render(ctx, mouseX, mouseY, delta);
		drawMouseoverTooltip(ctx, mouseX, mouseY);
		
		boolean cursorEmpty = handler.getCursorStack().isEmpty();
		if(cursorEmpty){
			if(focusedSlot instanceof MnemonicSlot mnSlot)
				mnSlot.drawTooltip(ctx, textRenderer, player.isSpectator(), mouseX, mouseY);
			
			for(Element child : children())
				if(child instanceof SlotCountGear gear && gear.hovered){
					int count = handler.getDropCountProp(gear.index).get();
					String suffix = count == 0 ? "0" : count == 1 ? "1" : "n";
					ctx.drawTooltip(
							textRenderer,
							List.of(
									Text.translatable("container.arcana.mnemonic_dropper.drop_" + suffix, count),
									Text.translatable("container.arcana.mnemonic_dropper.scroll_hint")
							),
							mouseX,
							mouseY);
				}
		}
		
	}
	
	protected void drawBackground(DrawContext ctx, float delta, int mouseX, int mouseY){
		int x = (width - backgroundWidth) / 2;
		int y = (height - backgroundHeight) / 2;
		RenderSystem.setShaderColor(1, 1, 1, 1);
		ctx.drawTexture(TEXTURE, x, y, 0, 0, backgroundWidth, backgroundHeight);
		ctx.drawTexture(TEXTURE, x+12 + handler.getDropIndex()*18, y+32, 176, 0, 8, 5);
	}
	
	protected void drawForeground(DrawContext ctx, int mouseX, int mouseY){
		ctx.drawText(textRenderer, title, titleX, titleY, 0xC0C0C0, false);
	}
	
	protected void drawSlot(DrawContext context, Slot slot){
		if(slot instanceof MnemonicSlot mnSlot)
			mnSlot.drawContent(context, textRenderer);
		super.drawSlot(context, slot);
	}
	
	protected void onMouseClick(Slot slot, int slotId, int button, SlotActionType actionType){
		if(slot instanceof MnemonicSlot mnSlot && !player.isSpectator())
			mnSlot.onClick(handler, handler.mnemonics, actionType);
		super.onMouseClick(slot, slotId, button, actionType);
	}
	
	private class SlotCountGear implements Element, Drawable, Selectable{
		
		private final int index;
		private final int x, y;
		
		private boolean focused, hovered, updated;
		private float currentTurns;
		
		private SlotCountGear(int index, int x, int y){
			this.index = index;
			this.x = x;
			this.y = y;
		}
		
		public void render(DrawContext ctx, int mouseX, int mouseY, float delta){
			hovered = mouseX >= x && mouseX < x + 18 && mouseY >= y && mouseY < y + 18;
			
			int count = handler.getDropCountProp(index).get();
			if(updated)
				currentTurns = MathHelper.lerp(Math.min(delta * 0.45f, 1), currentTurns, count);
			else{
				currentTurns = count;
				updated = true;
			}
			MatrixStack matrices = ctx.getMatrices();
			matrices.push();
			matrices.translate(x + 9, y + 9, 0);
			matrices.multiply(RotationAxis.POSITIVE_Z.rotationDegrees(currentTurns * -90));
			ctx.drawTexture(TEXTURE, -9, -9, count == 0 ? 184 : 202, 0, 18, 18);
			matrices.pop();
			String text = String.valueOf(count);
			int color = count == 0 ? 0x74939B : 0x004B60;
			ctx.drawText(textRenderer, text, x + 9 - textRenderer.getWidth(text)/2, y + 9 - textRenderer.fontHeight/2, color, false);
		}
		
		public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount){
			if(hovered){
				Property prop = handler.getDropCountProp(index);
				int newValue = prop.get();
				if(verticalAmount > 0)
					prop.set(Math.clamp(++newValue, 0, 64));
				else if(verticalAmount < 0)
					prop.set(Math.clamp(--newValue, 0, 64));
				handler.sendContentUpdates();
				new PkScreenRequest(handler, "set_count", index, newValue).sendToServer();
				return true;
			}
			return false;
		}
		
		public void setFocused(boolean focused){
			this.focused = focused;
		}
		
		public boolean isFocused(){
			return focused;
		}
		
		public boolean isMouseOver(double mouseX, double mouseY){
			return hovered;
		}
		
		public SelectionType getType(){
			return isFocused() ? SelectionType.FOCUSED : hovered ? SelectionType.HOVERED : SelectionType.NONE;
		}
		
		public void appendNarrations(NarrationMessageBuilder builder){}
	}
	
	public static class Handler extends ScreenHandler implements RequestableScreenHandler{
		
		private final MnemonicInventoryData mnemonics;
		private final Inventory inventory;
		// [current index, 9 slot counts]
		private final PropertyDelegate properties;
		
		public Handler(int syncId, PlayerInventory pInv, MnemonicInventoryData mnemonics){
			this(syncId, pInv, new SimpleInventory(9), new ArrayPropertyDelegate(10), mnemonics);
			// on the client side, `data` is a copy not attached to any inventory, so attach it to our local copy
			((SimpleInventory)inventory).addListener(mnemonics);
		}
		
		public Handler(int syncId,
		               PlayerInventory pInv,
		               Inventory inventory,
		               PropertyDelegate properties,
		               MnemonicInventoryData mnemonics){
			super(ArcanaRegistry.MNEMONIC_DROPPER_SCREEN_HANDLER, syncId);
			
			this.mnemonics = mnemonics;
			this.inventory = inventory;
			this.inventory.onOpen(pInv.player);
			this.properties = properties;
			addProperties(properties);
			
			for(int i = 0; i < 9; i++)
				addSlot(new MnemonicSlot(inventory, i, 8 + i * 18, 18, mnemonics));
			
			for(int row = 0; row < 3; ++row)
				for(int col = 0; col < 9; col++)
					addSlot(new Slot(pInv, col + row * 9 + 9, 8 + col * 18, 84 + row * 18));
			
			for(int idx = 0; idx < 9; idx++)
				addSlot(new Slot(pInv, idx, 8 + idx * 18, 142));
		}
		
		public Property getDropCountProp(int index){
			return Property.create(properties, index + 1);
		}
		
		public int getDropIndex(){
			return properties.get(0);
		}
		
		public ItemStack quickMove(PlayerEntity player, int index){
			return InventoryUtil.quickMove(this, inventory, index);
		}
		
		public boolean insertItem(ItemStack stack, int startIndex, int endIndex, boolean fromLast){
			return MnemonicSlot.insertItem(this, mnemonics, stack, startIndex, endIndex, fromLast);
		}
		
		public boolean canUse(PlayerEntity player){
			return inventory.canPlayerUse(player);
		}
		
		public void onClosed(PlayerEntity player){
			super.onClosed(player);
			inventory.onClose(player);
		}
		
		public void handleRequest(@Nullable String tag, int index, int data){
			if("set_count".equals(tag) && index >= 0 && index <= 8){
				data = Math.clamp(data, 0, 64);
				properties.set(index + 1, data);
			}
			mnemonics.handleRequest(tag, index);
		}
	}
}