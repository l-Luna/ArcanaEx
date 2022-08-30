package arcana.research.requirements;

import arcana.research.Requirement;
import arcana.util.NbtUtil;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.tag.TagKey;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;

import java.util.Map;

import static arcana.Arcana.arcId;

public class ItemTagRequirement extends Requirement{
	
	public static final Identifier TYPE = arcId("tag");
	
	private final TagKey<Item> tag;
	
	public ItemTagRequirement(Identifier tagId){
		tag = TagKey.of(Registry.ITEM_KEY, tagId);
	}
	
	public TagKey<Item> getTag(){
		return tag;
	}
	
	public boolean satisfiedBy(PlayerEntity player){
		return player.getInventory().remove(x -> x.isIn(tag), 0, player.playerScreenHandler.getCraftingInput()) >= (getAmount() == 0 ? 1 : getAmount());
	}
	
	public void takeFrom(PlayerEntity player){
		player.getInventory().remove(x -> x.isIn(tag), getAmount(), player.playerScreenHandler.getCraftingInput());
	}
	
	public Identifier type(){
		return TYPE;
	}
	
	public NbtCompound data(){
		return NbtUtil.from(Map.of("tag", tag.id()));
	}
}