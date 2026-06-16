package arcana.research.requirements;

import arcana.research.Requirement;
import arcana.util.NbtUtil;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;

import java.util.Map;

import static arcana.Arcana.arcId;

// TODO: merge w/ TagRequirement and just use Ingredient
public class ItemRequirement extends Requirement{
	
	public static final Identifier TYPE = arcId("item");
	
	private final Item item;
	
	public ItemRequirement(Item item){
		this.item = item;
	}
	
	public boolean satisfiedBy(PlayerEntity player){
		return player.getInventory().remove(x -> x.getItem().equals(item), 0, player.playerScreenHandler.getCraftingInput()) >= (getAmount() == 0 ? 1 : getAmount());
	}
	
	public void takeFrom(PlayerEntity player){
		player.getInventory().remove(x -> x.getItem().equals(item), getAmount(), player.playerScreenHandler.getCraftingInput());
	}
	
	public Identifier type(){
		return TYPE;
	}
	
	public NbtCompound data(){
		return NbtUtil.from(Map.of(
				"item", Registries.ITEM.getId(item)
		));
	}
	
	public Item getItem(){
		return item;
	}
}