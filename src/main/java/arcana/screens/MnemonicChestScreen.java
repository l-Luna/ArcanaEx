package arcana.screens;

import arcana.ArcanaRegistry;
import arcana.api.RequestableScreenHandler;
import arcana.util.ArrayInventory;
import arcana.util.InventoryUtil;
import arcana.util.MnemonicInventoryData;
import com.mojang.blaze3d.systems.RenderSystem;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.slot.Slot;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;

import static arcana.Arcana.arcId;

@Environment(EnvType.CLIENT)
public class MnemonicChestScreen extends HandledScreen<MnemonicChestScreen.Handler>{
	
	private static final Identifier TEXTURE = arcId("textures/gui/container/mnemonic_chest.png");
	
	private final PlayerEntity player;
	
	public MnemonicChestScreen(Handler handler, PlayerInventory pInv, Text title){
		super(handler, pInv, title);
		player = pInv.player;
	}
	
	protected void init(){
		super.init();
		backgroundHeight = 169;
		
		titleX = (backgroundWidth - textRenderer.getWidth(title)) / 2;
		titleY -= 2;
		y = (height - backgroundHeight) / 2;
	}
	
	public void render(DrawContext ctx, int mouseX, int mouseY, float delta){
		renderBackground(ctx, mouseX, mouseY, delta);
		super.render(ctx, mouseX, mouseY, delta);
		drawMouseoverTooltip(ctx, mouseX, mouseY);
		
		if(focusedSlot instanceof MnemonicSlot mnSlot && handler.getCursorStack().isEmpty())
			mnSlot.drawTooltip(ctx, textRenderer, player.isSpectator(), mouseX, mouseY);
	}
	
	protected void drawBackground(DrawContext ctx, float delta, int mouseX, int mouseY){
		int x = (width - backgroundWidth) / 2;
		int y = (height - backgroundHeight) / 2;
		RenderSystem.setShaderColor(1, 1, 1, 1);
		ctx.drawTexture(TEXTURE, x, y, 0, 0, backgroundWidth, backgroundHeight);
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
	
	public static class Handler extends ScreenHandler implements RequestableScreenHandler{
		
		private final MnemonicInventoryData mnemonics;
		private final ArrayInventory inventory;
		
		public Handler(int syncId, PlayerInventory pInv, MnemonicInventoryData data){
			this(syncId, pInv, new ArrayInventory(27), data);
			// on the client side, `data` is a copy not attached to any inventory, so attach it to our local copy
			inventory.addListener(data);
		}
		
		public Handler(int syncId, PlayerInventory pInv, ArrayInventory inventory, MnemonicInventoryData mnemonics){
			super(ArcanaRegistry.MNEMONIC_CHEST_SCREEN_HANDLER, syncId);
			this.inventory = inventory;
			this.mnemonics = mnemonics;
			
			for(int row = 0; row < 3; ++row)
				for(int col = 0; col < 9; col++)
					addSlot(new MnemonicSlot(inventory, col + row * 9, 8 + col * 18, 19 + row * 18, mnemonics));
			
			for(int row = 0; row < 3; ++row)
				for(int col = 0; col < 9; col++)
					addSlot(new Slot(pInv, col + row * 9 + 9, 8 + col * 18, 87 + row * 18));
			
			for(int idx = 0; idx < 9; idx++)
				addSlot(new Slot(pInv, idx, 8 + idx * 18, 145));
		}
		
		public ItemStack quickMove(PlayerEntity player, int slot){
			return InventoryUtil.quickMove(this, inventory, slot);
		}
		
		public boolean insertItem(ItemStack stack, int startIndex, int endIndex, boolean fromLast){
			return MnemonicSlot.insertItem(this, mnemonics, stack, startIndex, endIndex, fromLast);
		}
		
		public boolean canUse(PlayerEntity player){
			return true;
		}
		
		public void handleRequest(@Nullable String tag, int index, int data){
			mnemonics.handleRequest(tag, index);
		}
	}
}