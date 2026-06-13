package arcana.research.requirements;

import arcana.recipes.XIngredient;
import arcana.research.Requirement;
import arcana.util.NbtUtil;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.NotNull;

import java.util.Map;

import static arcana.Arcana.arcId;

public class ItemRequirement extends Requirement{
	
	public static final Identifier TYPE = arcId("item");
	
	private final Item item;
	
	@NotNull
	private final XIngredient.StackMatcher matcher;
	
	public ItemRequirement(Item item, @NotNull XIngredient.StackMatcher matcher){
		this.item = item;
		this.matcher = matcher;
	}
	
	public boolean satisfiedBy(PlayerEntity player){
		return player.getInventory().remove(x -> x.getItem().equals(item) && matcher.test(x), 0, player.playerScreenHandler.getCraftingInput()) >= (getAmount() == 0 ? 1 : getAmount());
	}
	
	public void takeFrom(PlayerEntity player){
		player.getInventory().remove(x -> x.getItem().equals(item), getAmount(), player.playerScreenHandler.getCraftingInput());
	}
	
	public Identifier type(){
		return TYPE;
	}
	
	public NbtCompound data(){
		return NbtUtil.from(Map.of(
				"item", Registries.ITEM.getId(item),
				"matcher", matcher.asString()
		));
	}
	
	public Item getItem(){
		return item;
	}
	
	public @NotNull XIngredient.StackMatcher getMatcher(){
		return matcher;
	}
}