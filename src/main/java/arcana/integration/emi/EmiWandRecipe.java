package arcana.integration.emi;

import arcana.items.Cap;
import arcana.items.Core;
import arcana.items.WandItem;
import dev.emi.emi.api.recipe.EmiPatternCraftingRecipe;
import dev.emi.emi.api.stack.EmiIngredient;
import dev.emi.emi.api.stack.EmiStack;
import dev.emi.emi.api.widget.GeneratedSlotWidget;
import dev.emi.emi.api.widget.SlotWidget;
import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

public final class EmiWandRecipe extends EmiPatternCraftingRecipe{
	
	private static final List<Item> CAPS = Registry.ITEM.stream()
			.filter(Cap.class::isInstance)
			.toList();
	private static final List<Item> CORES = Registry.ITEM.stream()
			.filter(Core.class::isInstance)
			.collect(Collectors.toCollection(ArrayList::new));
	
	static{
		CORES.add(Items.STICK);
	}
	
	public EmiWandRecipe(Identifier id){
		super(
				List.of(
						EmiIngredient.of(CAPS.stream().map(EmiStack::of).collect(Collectors.toList())),
						EmiIngredient.of(CORES.stream().map(EmiStack::of).collect(Collectors.toList()))
				),
				EmiStack.of(WandItem.basicWand()),
				id,
				false
		);
	}
	
	public SlotWidget getInputWidget(int slot, int x, int y){
		if(slot == 0 || slot == 8){
			return new GeneratedSlotWidget(
					rng -> EmiStack.of(getCap(rng)),
					unique,
					x, y);
		}else if(slot == 4){
			return new GeneratedSlotWidget(
					rng -> {
						getCap(rng); // sync up the ingredient and output cores
						return EmiStack.of(getCore(rng));
					},
					unique,
					x, y);
		}else
			return new SlotWidget(EmiStack.EMPTY, x, y);
	}
	
	public SlotWidget getOutputWidget(int x, int y){
		return new GeneratedSlotWidget(
				rng -> EmiStack.of(WandItem.withCapAndCore(getCapAsCap(rng), getCoreAsCore(rng))),
				unique,
				x, y);
	}
	
	private Item getCap(Random rng){
		return CAPS.get(rng.nextInt(CAPS.size()));
	}
	
	private Cap getCapAsCap(Random rng){
		return (Cap)getCap(rng);
	}
	
	private Item getCore(Random rng){
		return CORES.get(rng.nextInt(CORES.size()));
	}
	
	private Core getCoreAsCore(Random rng){
		return Core.asCore(getCore(rng));
	}
}