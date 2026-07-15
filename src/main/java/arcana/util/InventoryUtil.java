package arcana.util;

import dev.emi.trinkets.api.TrinketComponent;
import dev.emi.trinkets.api.TrinketsApi;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.recipe.input.RecipeInput;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.slot.Slot;
import net.minecraft.util.Pair;

import java.util.Optional;
import java.util.function.Function;
import java.util.stream.IntStream;
import java.util.stream.Stream;

public final class InventoryUtil{
	
	public static Stream<ItemStack> streamInventory(Inventory i){
		return IntStream.range(0, i.size()).mapToObj(i::getStack);
	}
	
	public static Stream<ItemStack> streamInventory(RecipeInput i){
		return IntStream.range(0, i.getSize()).mapToObj(i::getStackInSlot);
	}
	
	public static Stream<ItemStack> streamAllItems(PlayerEntity player){
		return Stream.concat(
				streamInventory(player.getInventory()),
				TrinketsApi.getTrinketComponent(player).stream().flatMap(x -> x.getAllEquipped().stream()).map(Pair::getRight)
		);
	}
	
	public static boolean hasTrinket(LivingEntity entity, Item item){
		TrinketComponent trinkets = TrinketsApi.getTrinketComponent(entity).orElse(null);
		return trinkets != null && trinkets.isEquipped(item);
	}
	
	public static <T> Optional<T> fromFirstTrinket(LivingEntity entity, Function<ItemStack, T> f){
		return TrinketsApi.getTrinketComponent(entity)
				.flatMap(it -> it.getAllEquipped().stream()
						.map(Pair::getRight)
						.flatMap(x -> Stream.ofNullable(f.apply(x)))
						.findFirst());
	}
	
	public static ItemStack quickMove(ScreenHandler self, Inventory inventory, int index){
		ItemStack rem = ItemStack.EMPTY;
		Slot slot = self.slots.get(index);
		if(slot.hasStack()){
			ItemStack there = slot.getStack();
			rem = there.copy();
			if(index < inventory.size()){
				if(!self.insertItem(there, inventory.size(), self.slots.size(), true))
					return ItemStack.EMPTY;
			}else if(!self.insertItem(there, 0, inventory.size(), false))
				return ItemStack.EMPTY;
			
			if(there.isEmpty())
				slot.setStack(ItemStack.EMPTY);
			else
				slot.markDirty();
		}
		
		return rem;
	}
}