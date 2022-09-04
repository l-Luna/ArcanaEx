package arcana.screens;

import arcana.ArcanaRegistry;
import arcana.aspects.AspectMap;
import arcana.items.WandItem;
import arcana.recipes.ShapedArcaneCraftingRecipe;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.CraftingInventory;
import net.minecraft.inventory.CraftingResultInventory;
import net.minecraft.inventory.Inventory;
import net.minecraft.inventory.SimpleInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.network.packet.s2c.play.ScreenHandlerSlotUpdateS2CPacket;
import net.minecraft.recipe.Recipe;
import net.minecraft.recipe.RecipeManager;
import net.minecraft.recipe.RecipeMatcher;
import net.minecraft.recipe.RecipeType;
import net.minecraft.recipe.book.RecipeBookCategory;
import net.minecraft.screen.AbstractRecipeScreenHandler;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.ScreenHandlerContext;
import net.minecraft.screen.slot.CraftingResultSlot;
import net.minecraft.screen.slot.Slot;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.world.World;

import java.util.concurrent.atomic.AtomicReference;

public class ArcaneCraftingScreenHandler extends AbstractRecipeScreenHandler<CraftingInventory>{
	
	final CraftingInventory input = new CraftingInventory(this, 3, 3);
	final Inventory wand = new SimpleInventory(1){
		public void markDirty(){
			super.markDirty();
			onContentChanged(wand);
		}
	};
	private final CraftingResultInventory result = new CraftingResultInventory();
	private final ScreenHandlerContext context;
	private final PlayerEntity player;
	
	public ArcaneCraftingScreenHandler(int syncId, PlayerInventory playerInventory){
		this(syncId, playerInventory, ScreenHandlerContext.EMPTY);
	}
	
	public ArcaneCraftingScreenHandler(int syncId, PlayerInventory inv, ScreenHandlerContext ctx){
		super(ArcanaRegistry.ARCANE_CRAFTING_SCREEN_HANDLER, syncId);
		context = ctx;
		player = inv.player;
		
		addSlot(new ArcaneCraftingResultSlot(player, input, result, 0, 160, 64));
		
		// crafting slots
		for(int i = 0; i < 3; ++i)
			for(int j = 0; j < 3; ++j)
				addSlot(new Slot(input, j + i * 3, 42 + j * 23, 41 + i * 23));
		
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
			manager.getFirstMatch(ShapedArcaneCraftingRecipe.TYPE, craftInv, world).ifPresentOrElse(recipe -> {
				// the pattern matches, but the aspects might not
				// ArcaneCraftingScreen will display the missing aspects for us
				ItemStack wandStack = wandInv.getStack(0);
				if(wandStack.getItem() instanceof WandItem){
					AspectMap stored = WandItem.aspectsFrom(wandStack);
					if(stored.contains(recipe.aspects()) && result.shouldCraftRecipe(world, serverPlayer, recipe))
						itemStack.set(recipe.craft(craftInv));
				}
			}, () -> manager.getFirstMatch(RecipeType.CRAFTING, craftInv, world).ifPresent(recipe -> {
				if(result.shouldCraftRecipe(world, serverPlayer, recipe))
					itemStack.set(recipe.craft(craftInv));
			}));
			
			result.setStack(0, itemStack.get());
			handler.setPreviousTrackedSlot(0, itemStack.get());
			serverPlayer.networkHandler.sendPacket(new ScreenHandlerSlotUpdateS2CPacket(handler.syncId, handler.nextRevision(), 0, itemStack.get()));
		}
	}
	
	@Override
	public void onContentChanged(Inventory inventory){
		context.run((world, pos) -> updateResult(this, world, player, input, result, wand));
	}
	
	public void populateRecipeFinder(RecipeMatcher finder){
		input.provideRecipeInputs(finder);
	}
	
	public void clearCraftingSlots(){
		input.clear();
		result.clear();
		wand.clear();
	}
	
	public boolean matches(Recipe<? super CraftingInventory> recipe){
		return recipe.matches(input, player.world);
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
	
	public ItemStack transferSlot(PlayerEntity player, int index){
		ItemStack itemStack = ItemStack.EMPTY;
		Slot slot = slots.get(index);
		if(slot.hasStack()){
			ItemStack itemStack2 = slot.getStack();
			itemStack = itemStack2.copy();
			if(index == 0){
				// shift click from crafting
				context.run((world, pos) -> itemStack2.getItem().onCraft(itemStack2, world, player));
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
	
	public void close(PlayerEntity player){
		super.close(player);
		context.run((world, pos) -> dropInventory(player, input));
		context.run((world, pos) -> dropInventory(player, wand));
	}
	
	public /* non-static */ class ArcaneCraftingResultSlot extends CraftingResultSlot{
		
		public ArcaneCraftingResultSlot(PlayerEntity player, CraftingInventory input, Inventory inventory, int index, int x, int y){
			super(player, input, inventory, index, x, y);
		}
		
		public void onTakeItem(PlayerEntity player, ItemStack stack){
			World world = player.world;
			RecipeManager manager = world.getRecipeManager();
			var arcaneCrafting = manager.getFirstMatch(ShapedArcaneCraftingRecipe.TYPE, input, world);
			arcaneCrafting.ifPresent(recipe -> {
				// also take required aspects
				ItemStack wandStack = wand.getStack(0);
				if(wandStack.getItem() instanceof WandItem){
					// already checked to see if it contains enough
					WandItem.updateAspects(wandStack, map -> map.take(recipe.aspects()));
				}
			});
			// take aspects before taking items
			// need to allow for *arcane* crafting too
			this.onCrafted(stack);
			DefaultedList<ItemStack> remains;
			if(arcaneCrafting.isPresent())
				remains = player.world.getRecipeManager().getRemainingStacks(ShapedArcaneCraftingRecipe.TYPE, input, player.world);
			else
				remains = player.world.getRecipeManager().getRemainingStacks(RecipeType.CRAFTING, input, player.world);
			
			for(int i = 0; i < remains.size(); ++i){
				ItemStack inputStack = input.getStack(i);
				ItemStack remainingStack = remains.get(i);
				if(!inputStack.isEmpty()){
					input.removeStack(i, 1);
					inputStack = input.getStack(i);
				}
				
				if(!remainingStack.isEmpty()){
					if(inputStack.isEmpty())
						input.setStack(i, remainingStack);
					else if(ItemStack.areItemsEqualIgnoreDamage(inputStack, remainingStack) && ItemStack.areNbtEqual(inputStack, remainingStack)){
						remainingStack.increment(inputStack.getCount());
						input.setStack(i, remainingStack);
					}else if(!player.getInventory().insertStack(remainingStack))
						player.dropItem(remainingStack, false);
				}
			}
		}
	}
}