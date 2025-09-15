package arcana.items;

import arcana.ArcanaRegistry;
import arcana.aura.AuraWorld;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.world.World;

import static arcana.Arcana.arcId;

public class ScribbledNotesItem extends Item{
	
	public ScribbledNotesItem(Settings settings){
		super(settings);
	}
	
	public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand){
		ItemStack stack = user.getStackInHand(hand);
		if(AuraWorld.from(world).raycastNodes(user, false).isPresent()){
			ItemStack newStack = new ItemStack(ArcanaRegistry.ARCANUM);
			user.setStackInHand(hand, newStack);
			return TypedActionResult.success(newStack);
		}else{
			DirectResearchEntryItem.openEntry(arcId("scribbled_notes"));
			return TypedActionResult.success(stack);
		}
	}
}