package arcana.items;

import arcana.legacy_components.Researcher;
import arcana.research.Research;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.world.World;

import static arcana.Arcana.arcId;

public class CheatersArcanumItem extends ResearchBookItem{
	
	public CheatersArcanumItem(Settings settings){
		super(settings, arcId("arcanum"));
	}
	
	public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand){
		if(!world.isClient){ // a waiver of responsibility
			Researcher from = Researcher.from(user);
			Research.streamEntries().forEach(from::completeEntry);
			Research.streamAddenda().forEach(from::completeAddendum);
			from.doSync();
		}
		return super.use(world, user, hand);
	}
}