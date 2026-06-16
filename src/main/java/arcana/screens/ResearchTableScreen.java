package arcana.screens;

import arcana.ArcanaRegistry;
import arcana.blocks.ResearchTableBlock;
import arcana.blocks.be.ResearchTableBlockEntity;
import arcana.client.research.PuzzleRenderer;
import arcana.items.ResearchNotesItem;
import arcana.items.components.ArcanaItemComponentTypes;
import arcana.research.Puzzle;
import arcana.research.Research;
import net.minecraft.block.BlockState;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.Inventory;
import net.minecraft.inventory.SimpleInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
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
	
	protected void drawBackground(DrawContext ctx, float delta, int mouseX, int mouseY){
		drawRtTexture(ctx, texture, x, y, 0, 0, 0, backgroundWidth, backgroundHeight);
	}
	
	public void render(DrawContext ctx, int mouseX, int mouseY, float delta){
		renderBackground(ctx, mouseX, mouseY, delta);
		super.render(ctx, mouseX, mouseY, delta);
		// don't draw item tooltips if e.g. an aspect is selected
		ItemStack notes = handler.slots.get(37).getStack();
		if(!notes.isEmpty()){
			Puzzle puzzle = Research.getPuzzle(notes.get(ArcanaItemComponentTypes.RESEARCH_NOTE_PUZZLE_ID));
			var renderer = PuzzleRenderer.get(puzzle);
			if(renderer == null || renderer.drawItemTooltips())
				drawMouseoverTooltip(ctx, mouseX, mouseY);
		}else
			drawMouseoverTooltip(ctx, mouseX, mouseY);
	}
	
	public static void drawRtTexture(DrawContext ctx, Identifier tex, int x, int y, int z, float u, float v, int width, int height){
		ctx.drawTexture(tex, x, y, z, u, v, width, height, 338, 338);
	}
	
	protected void drawForeground(DrawContext ctx, int mouseX, int mouseY){
		MatrixStack matrices = ctx.getMatrices();
		// don't draw label
		// do draw selected aspect over items
		matrices.push();
		matrices.translate(-x, -y, 0);
		ItemStack notes = handler.slots.get(37).getStack();
		if(!notes.isEmpty()){
			Puzzle puzzle = Research.getPuzzle(notes.get(ArcanaItemComponentTypes.RESEARCH_NOTE_PUZZLE_ID));
			PuzzleRenderer<Puzzle> renderer = PuzzleRenderer.get(puzzle);
			if(renderer != null){
				NbtCompound data = notes.get(ArcanaItemComponentTypes.RESEARCH_NOTE_PUZZLE_DATA);
				if(notes.getItem() == ArcanaRegistry.RESEARCH_NOTES){
					renderer.render(ctx, puzzle, data, width, height, mouseX, mouseY);
					renderer.renderAfter(ctx, puzzle, data, width, height, mouseX, mouseY);
				}else if(notes.getItem() == ArcanaRegistry.COMPLETE_RESEARCH_NOTES)
					renderer.renderComplete(ctx, puzzle, data, width, height, mouseX, mouseY);
			}
		}
		matrices.pop();
	}
	
	public boolean mouseClicked(double mouseX, double mouseY, int button){
		super.mouseClicked(mouseX, mouseY, button);
		
		ItemStack notes = handler.slots.get(37).getStack();
		if(!notes.isEmpty() && notes.getItem() == ArcanaRegistry.RESEARCH_NOTES){
			Puzzle puzzle = Research.getPuzzle(notes.get(ArcanaItemComponentTypes.RESEARCH_NOTE_PUZZLE_ID));
			var renderer = PuzzleRenderer.get(puzzle);
			if(renderer != null)
				return renderer.onClick(button, puzzle, notes.get(ArcanaItemComponentTypes.RESEARCH_NOTE_PUZZLE_DATA), width, height, (int)mouseX, (int)mouseY);
		}
		
		return false;
	}
	
	public void close(){
		ItemStack notes = handler.slots.get(37).getStack();
		if(!notes.isEmpty()){
			Puzzle puzzle = Research.getPuzzle(notes.get(ArcanaItemComponentTypes.RESEARCH_NOTE_PUZZLE_ID));
			PuzzleRenderer<Puzzle> renderer = PuzzleRenderer.get(puzzle);
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
		
		public ItemStack quickMove(PlayerEntity player, int index){
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