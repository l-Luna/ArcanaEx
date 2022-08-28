package arcana.screens;

import arcana.ArcanaRegistry;
import arcana.screens.ArcaneCraftingInventory.ArcaneCraftingResultInventory;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.CraftingInventory;
import net.minecraft.inventory.CraftingResultInventory;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.network.packet.s2c.play.ScreenHandlerSlotUpdateS2CPacket;
import net.minecraft.recipe.CraftingRecipe;
import net.minecraft.recipe.Recipe;
import net.minecraft.recipe.RecipeMatcher;
import net.minecraft.recipe.RecipeType;
import net.minecraft.recipe.book.RecipeBookCategory;
import net.minecraft.screen.AbstractRecipeScreenHandler;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.ScreenHandlerContext;
import net.minecraft.screen.slot.CraftingResultSlot;
import net.minecraft.screen.slot.Slot;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.world.World;

import java.util.Optional;

public class ArcaneCraftingScreenHandler extends AbstractRecipeScreenHandler<ArcaneCraftingInventory>{
	
	private final ArcaneCraftingInventory input = new ArcaneCraftingInventory(this, 3, 3);
	private final ArcaneCraftingResultInventory result = new ArcaneCraftingResultInventory();
	private final ScreenHandlerContext context;
	private final PlayerEntity player;
	
	public ArcaneCraftingScreenHandler(int syncId, PlayerInventory playerInventory){
		this(syncId, playerInventory, ScreenHandlerContext.EMPTY);
	}
	
	public ArcaneCraftingScreenHandler(int syncId, PlayerInventory inv, ScreenHandlerContext ctx){
		super(ArcanaRegistry.ARCANE_CRAFTING_SCREEN_HANDLER, syncId);
		context = ctx;
		player = inv.player;
		
		addSlot(new CraftingResultSlot(player, input, result, 0, 160, 64));
		
		// crafting slots
		for(int i = 0; i < 3; ++i)
			for(int j = 0; j < 3; ++j)
				addSlot(new Slot(input, j + i * 3, 42 + j * 23, 41 + i * 23));
		
		// player inventory
		for(int i = 0; i < 3; ++i)
			for(int j = 0; j < 9; ++j)
				addSlot(new Slot(inv, j + i * 9 + 9, 16 + j * 18, 151 + i * 18));
		
		// player hotbar
		for(int i = 0; i < 9; ++i)
			addSlot(new Slot(inv, i, 16 + i * 18, 209));
	}
	
	protected static void updateResult(
			ScreenHandler handler, World world, PlayerEntity player, CraftingInventory craftingInventory, CraftingResultInventory result){
		if(!world.isClient){
			ServerPlayerEntity serverPlayerEntity = (ServerPlayerEntity)player;
			ItemStack itemStack = ItemStack.EMPTY;
			Optional<CraftingRecipe> optional = world.getServer().getRecipeManager().getFirstMatch(RecipeType.CRAFTING, craftingInventory, world);
			if(optional.isPresent()){
				CraftingRecipe craftingRecipe = optional.get();
				if(result.shouldCraftRecipe(world, serverPlayerEntity, craftingRecipe))
					itemStack = craftingRecipe.craft(craftingInventory);
			}
			
			result.setStack(0, itemStack);
			handler.setPreviousTrackedSlot(0, itemStack);
			serverPlayerEntity.networkHandler.sendPacket(new ScreenHandlerSlotUpdateS2CPacket(handler.syncId, handler.nextRevision(), 0, itemStack));
		}
	}
	
	@Override
	public void onContentChanged(Inventory inventory){
		this.context.run((world, pos) -> updateResult(this, world, player, input, result));
	}
	
	public void populateRecipeFinder(RecipeMatcher finder){
		input.provideRecipeInputs(finder);
	}
	
	public void clearCraftingSlots(){
		input.clear();
		result.clear();
	}
	
	public boolean matches(Recipe<? super ArcaneCraftingInventory> recipe){
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
		return 10;
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
		if(slot != null && slot.hasStack()){
			itemStack = slot.getStack().copy();
			if(index == 0){
				// shift click from crafting
				context.run((world, pos) -> slot.getStack().getItem().onCraft(slot.getStack(), world, player));
				if(!this.insertItem(slot.getStack(), 10, 46, true))
					return ItemStack.EMPTY;
				
				slot.onQuickTransfer(slot.getStack(), itemStack);
			}else if(index >= 10 && index < 46){
				if(!insertItem(slot.getStack(), 1, 10, false)){
					if(index < 37){
						if(!insertItem(slot.getStack(), 37, 46, false))
							return ItemStack.EMPTY;
					}else if(!insertItem(slot.getStack(), 10, 37, false))
						return ItemStack.EMPTY;
				}
			}else if(!insertItem(slot.getStack(), 10, 46, false))
				return ItemStack.EMPTY;
			
			if(slot.getStack().isEmpty())
				slot.setStack(ItemStack.EMPTY);
			else
				slot.markDirty();
			
			if(slot.getStack().getCount() == itemStack.getCount())
				return ItemStack.EMPTY;
			
			slot.onTakeItem(player, slot.getStack());
			if(index == 0)
				player.dropItem(slot.getStack(), false);
		}
		
		return itemStack;
	}
	
	public boolean canUse(PlayerEntity player){
		return canUse(context, player, ArcanaRegistry.ARCANE_CRAFTING_TABLE);
	}
	
	public void close(PlayerEntity player){
		super.close(player);
		context.run((world, pos) -> this.dropInventory(player, this.input));
	}
}
