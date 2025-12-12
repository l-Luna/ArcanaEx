package arcana.screens;

import arcana.ArcanaRegistry;
import arcana.blocks.ResearchTableBlock;
import arcana.blocks.be.ResearchTableBlockEntity;
import arcana.client.research.PuzzleRenderer;
import arcana.items.ResearchNotesItem;
import arcana.research.Puzzle;
import arcana.research.Research;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.block.BlockState;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.Inventory;
import net.minecraft.inventory.SimpleInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.ScreenHandlerContext;
import net.minecraft.screen.slot.Slot;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.Pair;

import static arcana.Arcana.arcId;

public class ResearchTableScreen extends HandledScreen<ResearchTableScreen.Handler>{
	
	private static final Identifier texture = arcId("textures/gui/container/research_table.png");
	
	public static final int bgWidth = 338, bgHeight = 241;
	
	public ResearchTableScreen(Handler handler, PlayerInventory inventory, Text title){
		super(handler, inventory, title);
	}
	
	protected void init(){
		backgroundWidth = 338;
		backgroundHeight = 241;
		super.init();
		titleY = -100;
	}
	
	protected void drawBackground(MatrixStack matrices, float delta, int mouseX, int mouseY){
		RenderSystem.setShaderTexture(0, texture);
		drawRtTexture(matrices, x, y, 0, 0, 0, backgroundWidth, backgroundHeight);
	}
	
	public void render(MatrixStack matrices, int mouseX, int mouseY, float delta){
		renderBackground(matrices);
		super.render(matrices, mouseX, mouseY, delta);
		// don't draw item tooltips if e.g. an aspect is selected
		var notes = handler.slots.get(37).getStack();
		var nbt = notes.getNbt();
		if(!notes.isEmpty() && nbt != null && nbt.contains("puzzle_id")){
			Puzzle puzzle = Research.getPuzzle(new Identifier(nbt.getString("puzzle_id")));
			var renderer = PuzzleRenderer.get(puzzle);
			if(renderer == null || renderer.drawItemTooltips())
				drawMouseoverTooltip(matrices, mouseX, mouseY);
		}else
			drawMouseoverTooltip(matrices, mouseX, mouseY);
	}
	
	public static void drawRtTexture(MatrixStack matrices, int x, int y, int z, float u, float v, int width, int height){
		drawTexture(matrices, x, y, z, u, v, width, height, 338, 338);
	}
	
	protected void drawForeground(MatrixStack matrices, int mouseX, int mouseY){
		// don't draw label
		// do draw selected aspect over items
		matrices.push();
		matrices.translate(-x, -y, 0);
		var notes = handler.slots.get(37).getStack();
		var nbt = notes.getNbt();
		if(!notes.isEmpty() && nbt != null && nbt.contains("puzzle_id")){
			Puzzle puzzle = Research.getPuzzle(new Identifier(nbt.getString("puzzle_id")));
			var renderer = PuzzleRenderer.get(puzzle);
			if(renderer != null){
				var data = nbt.getCompound("puzzle_data");
				if(notes.getItem() == ArcanaRegistry.RESEARCH_NOTES){
					renderer.render(matrices, puzzle, data, width, height, mouseX, mouseY);
					renderer.renderAfter(matrices, puzzle, data, width, height, mouseX, mouseY);
				}else if(notes.getItem() == ArcanaRegistry.COMPLETE_RESEARCH_NOTES)
					renderer.renderComplete(matrices, puzzle, data, width, height, mouseX, mouseY);
			}
		}
		matrices.pop();
	}
	
	public boolean mouseClicked(double mouseX, double mouseY, int button){
		super.mouseClicked(mouseX, mouseY, button);
		
		var notes = handler.slots.get(37).getStack();
		var nbt = notes.getNbt();
		if(!notes.isEmpty() && nbt != null && nbt.contains("puzzle_id") && notes.getItem() == ArcanaRegistry.RESEARCH_NOTES){
			Puzzle puzzle = Research.getPuzzle(new Identifier(nbt.getString("puzzle_id")));
			var renderer = PuzzleRenderer.get(puzzle);
			if(renderer != null)
				return renderer.onClick(button, puzzle, nbt.getCompound("puzzle_data"), width, height, (int)mouseX, (int)mouseY);
		}
		
		return false;
	}
	
	public void close(){
		var notes = handler.slots.get(37).getStack();
		var nbt = notes.getNbt();
		if(!notes.isEmpty() && nbt != null && nbt.contains("puzzle_id")){
			Puzzle puzzle = Research.getPuzzle(new Identifier(nbt.getString("puzzle_id")));
			var renderer = PuzzleRenderer.get(puzzle);
			if(renderer != null)
				renderer.onClose();
		}
		super.close();
	}
	
	public static class Handler extends ScreenHandler{
		
		private final Inventory inventory;
		
		public Handler(int syncId, PlayerInventory playerInv){
			this(syncId, playerInv, ScreenHandlerContext.EMPTY);
		}
		
		public Handler(int syncId, PlayerInventory inv, ScreenHandlerContext ctx){
			super(ArcanaRegistry.RESEARCH_TABLE_SCREEN_HANDLER, syncId);
			
			// player inventory
			for(int i = 0; i < 3; ++i)
				for(int j = 0; j < 9; ++j)
					addSlot(new Slot(inv, j + i * 9 + 9, 119 + j * 18, 181 + i * 18));
			
			// compact hotbar
			for(int i = 0; i < 3; i++)
				for(int j = 0; j < 3; j++)
					addSlot(new Slot(inv, j + i * 3, 59 + j * 18, 181 + i * 18));
			
			Pair<SimpleInventory, SimpleInventory> tableInv = ctx.get((world, pos) -> {
				BlockState state = world.getBlockState(pos);
				if(!state.get(ResearchTableBlock.left))
					pos = pos.offset(state.get(ResearchTableBlock.facing));
				ResearchTableBlockEntity entity = (ResearchTableBlockEntity)world.getBlockEntity(pos);
				return new Pair<>(entity.scribingTools, entity.note);
			}).orElse(new Pair<>(new SimpleInventory(1), new SimpleInventory(1)));
			
			addSlot(new Slot(tableInv.getLeft(), 0, 74, 10){
				public boolean canInsert(ItemStack stack){
					return stack.getItem() == ArcanaRegistry.SCRIBING_TOOLS;
				}
			});
			addSlot(new Slot(tableInv.getRight(), 0, 92, 10){
				public boolean canInsert(ItemStack stack){
					return stack.getItem() instanceof ResearchNotesItem;
				}
			});
			
			inventory = tableInv.getLeft();
		}
		
		public ItemStack transferSlot(PlayerEntity player, int index){
			ItemStack itemStack = ItemStack.EMPTY;
			Slot slot = slots.get(index);
			if(slot != null && slot.hasStack()){
				ItemStack itemStack2 = slot.getStack();
				itemStack = itemStack2.copy();
				if(index >= 36){
					if(!insertItem(itemStack2, 0, 36, true))
						return ItemStack.EMPTY;
				}else if(!insertItem(itemStack2, 36, 38, true))
					return ItemStack.EMPTY;
				
				if(itemStack2.isEmpty())
					slot.setStack(ItemStack.EMPTY);
				else
					slot.markDirty();
				
				if(itemStack2.getCount() == itemStack.getCount())
					return ItemStack.EMPTY;
				
				slot.onTakeItem(player, itemStack2);
			}
			
			return itemStack;
		}
		
		public boolean canUse(PlayerEntity player){
			return inventory.canPlayerUse(player);
		}
	}
}