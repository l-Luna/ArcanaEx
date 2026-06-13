package arcana.screens;

import arcana.ArcanaRegistry;
import arcana.api.ContextCraftedItem;
import arcana.aspects.Aspect;
import arcana.aspects.AspectMap;
import arcana.aspects.Aspects;
import arcana.aspects.ScaledAspectMap;
import arcana.client.AspectRenderHelper;
import arcana.items.WandItem;
import arcana.recipes.arcane_crafting.ArcaneCraftingRecipe;
import arcana.recipes.arcane_crafting.ShapedArcaneCraftingRecipe;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.CraftingInventory;
import net.minecraft.inventory.CraftingResultInventory;
import net.minecraft.inventory.Inventory;
import net.minecraft.inventory.SimpleInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.network.packet.s2c.play.ScreenHandlerSlotUpdateS2CPacket;
import net.minecraft.recipe.RecipeEntry;
import net.minecraft.recipe.RecipeManager;
import net.minecraft.recipe.RecipeMatcher;
import net.minecraft.recipe.RecipeType;
import net.minecraft.recipe.book.RecipeBookCategory;
import net.minecraft.recipe.input.CraftingRecipeInput;
import net.minecraft.screen.AbstractRecipeScreenHandler;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.ScreenHandlerContext;
import net.minecraft.screen.slot.CraftingResultSlot;
import net.minecraft.screen.slot.Slot;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.util.math.Vec2f;
import net.minecraft.world.World;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;

import static arcana.Arcana.arcId;

public class ArcaneCraftingScreen extends HandledScreen<ArcaneCraftingScreen.Handler>{
	
	private static final Identifier texture = arcId("textures/gui/container/arcane_crafting_table.png");
	
	private static final Map<Aspect, Vec2f> aspectPositions = Map.of(
			Aspects.AIR, new Vec2f(65, 15),
			Aspects.FIRE, new Vec2f(22, 39),
			Aspects.WATER, new Vec2f(108, 39),
			Aspects.EARTH, new Vec2f(22, 89),
			Aspects.ORDER, new Vec2f(108, 89),
			Aspects.ENTROPY, new Vec2f(65, 113)
	);
	
	public ArcaneCraftingScreen(Handler handler, PlayerInventory inventory, Text title){
		super(handler, inventory, title);
	}
	
	protected void init(){
		backgroundWidth = 187;
		backgroundHeight = 233;
		super.init();
		titleX = 10;
		titleY = -5;
	}
	
	protected void drawBackground(DrawContext context, float delta, int mouseX, int mouseY){
		RenderSystem.setShader(GameRenderer::getPositionTexProgram);
		RenderSystem.setShaderColor(1, 1, 1, 1);
		context.drawTexture(texture, x, y, 0, 0, backgroundWidth, backgroundHeight);
		
		// draw required aspects for recipe
		ClientWorld world = MinecraftClient.getInstance().world;
		ItemStack wand = handler.wand.getStack(0);
		world.getRecipeManager().getFirstMatch(ShapedArcaneCraftingRecipe.TYPE, handler.inv.createRecipeInput(), world).ifPresent(recipe -> {
			ScaledAspectMap stored = wand.getItem() instanceof WandItem ? WandItem.aspectsFrom(wand) : new ScaledAspectMap(new AspectMap(), 1);
			for(Aspect aspect : recipe.value().aspects().aspectSet()){
				int amount = recipe.value().aspects().get(aspect);
				amount *= WandItem.costMultiplier(aspect, wand, client.player);
				boolean blink = !stored.contains(aspect, amount);
				context.getMatrices().push();
				context.getMatrices().translate(x, y, 1);
				int x = (int)aspectPositions.get(aspect).x;
				int y = (int)aspectPositions.get(aspect).y;
				float alpha = blink ? (float)Math.abs(Math.sin((world.getTime() + delta) / 4.5f)) * 0.6f + 0.4f : 1;
				AspectRenderHelper.renderAspect(aspect, context, x, y, 0, 1, 1, 1, alpha);
				AspectRenderHelper.renderAspectStackOverlay(amount, context, MinecraftClient.getInstance().textRenderer, x, y, 0);
				context.getMatrices().pop();
			}
		});
	}
	
	public void render(DrawContext matrices, int mouseX, int mouseY, float delta) {
		renderBackground(matrices, mouseX, mouseY, delta);
		super.render(matrices, mouseX, mouseY, delta);
		drawMouseoverTooltip(matrices, mouseX, mouseY);
	}
	
	protected void drawForeground(DrawContext matrices, int mouseX, int mouseY){
		// no-op - don't draw label
	}
	
	public static class Handler extends AbstractRecipeScreenHandler<CraftingRecipeInput, ArcaneCraftingRecipe>{
		
		final CraftingInventory inv = new CraftingInventory(this, 3, 3);
		final Inventory wand = new SimpleInventory(1){
			public void markDirty(){
				super.markDirty();
				onContentChanged(wand);
			}
		};
		private final CraftingResultInventory result = new CraftingResultInventory();
		private final ScreenHandlerContext context;
		private final PlayerEntity player;
		
		public Handler(int syncId, PlayerInventory playerInventory){
			this(syncId, playerInventory, ScreenHandlerContext.EMPTY);
		}
		
		public Handler(int syncId, PlayerInventory inv, ScreenHandlerContext ctx){
			super(ArcanaRegistry.ARCANE_CRAFTING_SCREEN_HANDLER, syncId);
			context = ctx;
			player = inv.player;
			
			addSlot(new ArcaneCraftingResultSlot(player, this.inv, result, 0, 160, 64));
			
			// crafting slots
			for(int i = 0; i < 3; ++i)
				for(int j = 0; j < 3; ++j)
					addSlot(new Slot(this.inv, j + i * 3, 42 + j * 23, 41 + i * 23));
			
			addSlot(new Slot(wand, 0, 160, 18){
				public boolean canInsert(ItemStack stack){
					return stack.getItem() instanceof WandItem;
				}
			});
			
			// player inventory
			for(int i = 0; i < 3; ++i)
				for(int j = 0; j < 9; ++j)
					addSlot(new Slot(inv, j + i * 9 + 9, 16 + j * 18, 151 + i * 18));
			
			// player hotbar
			for(int i = 0; i < 9; ++i)
				addSlot(new Slot(inv, i, 16 + i * 18, 209));
		}
		
		protected static void updateResult(
				ScreenHandler handler, World world, PlayerEntity player, CraftingInventory craftInv, CraftingResultInventory result, Inventory wandInv){
			if(!world.isClient){
				ServerPlayerEntity serverPlayer = (ServerPlayerEntity)player;
				var itemStack = new AtomicReference<>(ItemStack.EMPTY);
				RecipeManager manager = world.getServer().getRecipeManager();
				// this is what if/else will look like in 2024
				CraftingRecipeInput input = craftInv.createRecipeInput();
				manager.getFirstMatch(ShapedArcaneCraftingRecipe.TYPE, input, world).ifPresentOrElse(recipe -> {
					// the pattern matches, but the aspects might not
					// ArcaneCraftingScreen will display the missing aspects for us
					ItemStack wandStack = wandInv.getStack(0);
					if(wandStack.getItem() instanceof WandItem){
						ScaledAspectMap stored = WandItem.aspectsFrom(wandStack);
						AspectMap toTake = recipe.value().aspects().copy();
						toTake.multiply(aspect -> WandItem.costMultiplier(aspect, wandStack, player));
						if(stored.contains(toTake) && result.shouldCraftRecipe(world, serverPlayer, recipe))
							itemStack.set(recipe.value().craft(input, player.getWorld().getRegistryManager()));
					}
				}, () -> manager.getFirstMatch(RecipeType.CRAFTING, input, world).ifPresent(recipe -> {
					if(result.shouldCraftRecipe(world, serverPlayer, recipe))
						itemStack.set(recipe.value().craft(input, player.getWorld().getRegistryManager()));
				}));
				
				result.setStack(0, itemStack.get());
				handler.setPreviousTrackedSlot(0, itemStack.get());
				serverPlayer.networkHandler.sendPacket(new ScreenHandlerSlotUpdateS2CPacket(handler.syncId, handler.nextRevision(), 0, itemStack.get()));
			}
		}
		
		@Override
		public void onContentChanged(Inventory inventory){
			context.run((world, pos) -> updateResult(this, world, player, inv, result, wand));
		}
		
		public void populateRecipeFinder(RecipeMatcher finder){
			inv.provideRecipeInputs(finder);
		}
		
		public void clearCraftingSlots(){
			inv.clear();
			result.clear();
			wand.clear();
		}
		
		public boolean matches(RecipeEntry<ArcaneCraftingRecipe> recipe){
			return recipe.value().matches(inv.createRecipeInput(), player.getWorld());
		}
		
		public int getCraftingResultSlotIndex(){
			return 0;
		}
		
		public int getCraftingWidth(){
			return 3;
		}
		
		public int getCraftingHeight(){
			return 3;
		}
		
		public int getCraftingSlotCount(){
			return 11;
		}
		
		public RecipeBookCategory getCategory(){
			return RecipeBookCategory.CRAFTING;
		}
		
		public boolean canInsertIntoSlot(int index){
			return index != getCraftingResultSlotIndex();
		}
		
		public boolean canInsertIntoSlot(ItemStack stack, Slot slot){
			return slot.inventory != result && super.canInsertIntoSlot(stack, slot);
		}
		
		public ItemStack quickMove(PlayerEntity player, int index){
			ItemStack itemStack = ItemStack.EMPTY;
			Slot slot = slots.get(index);
			if(slot.hasStack()){
				ItemStack itemStack2 = slot.getStack();
				itemStack = itemStack2.copy();
				if(index == 0){
					// shift click from crafting
					context.run((world, pos) -> {
						Item item = itemStack2.getItem();
						item.onCraftByPlayer(itemStack2, world, player);
						if(item instanceof ContextCraftedItem cci)
							cci.onCraft(itemStack2, inv, world, player);
					});
					if(!this.insertItem(itemStack2, 11, 47, true))
						return ItemStack.EMPTY;
					
					slot.onQuickTransfer(itemStack2, itemStack);
				}else if(index >= 11 && index < 47){
					// try to place wands in the wand slot first
					boolean isWand = itemStack2.getItem() instanceof WandItem;
					if(!insertItem(itemStack2, 1, 11, isWand)){
						if(index < 38){
							if(!insertItem(itemStack2, 38, 47, false))
								return ItemStack.EMPTY;
						}else if(!insertItem(itemStack2, 11, 38, false))
							return ItemStack.EMPTY;
					}
				}else if(!insertItem(itemStack2, 11, 47, false))
					return ItemStack.EMPTY;
				
				if(itemStack2.isEmpty())
					slot.setStack(ItemStack.EMPTY);
				else
					slot.markDirty();
				
				if(itemStack2.getCount() == itemStack.getCount())
					return ItemStack.EMPTY;
				
				slot.onTakeItem(player, itemStack2);
				if(index == 0)
					player.dropItem(itemStack2, false);
			}
			
			return itemStack;
		}
		
		public boolean canUse(PlayerEntity player){
			return canUse(context, player, ArcanaRegistry.ARCANE_CRAFTING_TABLE);
		}
		
		public void onClosed(PlayerEntity player){
			super.onClosed(player);
			context.run((world, pos) -> dropInventory(player, inv));
			context.run((world, pos) -> dropInventory(player, wand));
		}
		
		public /* non-static */ class ArcaneCraftingResultSlot extends CraftingResultSlot{
			
			public ArcaneCraftingResultSlot(PlayerEntity player, CraftingInventory input, Inventory inventory, int index, int x, int y){
				super(player, input, inventory, index, x, y);
			}
			
			public void onTakeItem(PlayerEntity player, ItemStack stack){
				World world = player.getWorld();
				RecipeManager manager = world.getRecipeManager();
				Optional<RecipeEntry<ArcaneCraftingRecipe>> arcaneCrafting = manager.getFirstMatch(ShapedArcaneCraftingRecipe.TYPE, inv.createRecipeInput(), world);
				arcaneCrafting.ifPresent(recipe -> {
					// also take required aspects
					ItemStack wandStack = wand.getStack(0);
					if(wandStack.getItem() instanceof WandItem){
						// already checked to see if it contains enough
						AspectMap toTake = recipe.value().aspects().copy();
						toTake.multiply(aspect -> WandItem.costMultiplier(aspect, wandStack, player));
						WandItem.updateAspects(wandStack, map -> map.take(toTake));
					}
				});
				// take aspects before taking items
				// need to allow for *arcane* crafting too
				this.onCrafted(stack);
				if(stack.getItem() instanceof ContextCraftedItem cci)
					cci.onCraft(stack, inv, player.getWorld(), player);
				DefaultedList<ItemStack> remains;
				if(arcaneCrafting.isPresent())
					remains = player.getWorld().getRecipeManager().getRemainingStacks(ShapedArcaneCraftingRecipe.TYPE, inv.createRecipeInput(), player.getWorld());
				else
					remains = player.getWorld().getRecipeManager().getRemainingStacks(RecipeType.CRAFTING, inv.createRecipeInput(), player.getWorld());
				
				for(int i = 0; i < remains.size(); ++i){
					ItemStack inputStack = inv.getStack(i);
					ItemStack remainingStack = remains.get(i);
					if(!inputStack.isEmpty()){
						inv.removeStack(i, 1);
						inputStack = inv.getStack(i);
					}
					
					if(!remainingStack.isEmpty()){
						if(inputStack.isEmpty())
							inv.setStack(i, remainingStack);
						else if(ItemStack.areItemsAndComponentsEqual(inputStack, remainingStack)){
							remainingStack.increment(inputStack.getCount());
							inv.setStack(i, remainingStack);
						}else if(!player.getInventory().insertStack(remainingStack))
							player.dropItem(remainingStack, false);
					}
				}
			}
		}
	}
}