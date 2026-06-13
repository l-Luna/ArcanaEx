package arcana.items;

import arcana.legacy_components.Researcher;
import arcana.research.Research;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.stat.Stats;
import net.minecraft.util.Hand;
import net.minecraft.util.Identifier;
import net.minecraft.util.TypedActionResult;
import net.minecraft.world.World;

public class ResearchBookItem extends Item{
	
	private final Identifier bookId;
	
	public ResearchBookItem(Settings settings, Identifier id){
		super(settings);
		bookId = id;
	}
	
	public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand){
		if(world.isClient)
			openBook(bookId);
		else{
			// get root entries out of the way
			Research.streamEntries()
					.filter(x -> x.category().book().id().equals(bookId))
					.filter(x -> x.meta().contains("root"))
					.forEach(entry -> Researcher.from(user).tryAdvance(entry, false));
		}
		user.incrementStat(Stats.USED.getOrCreateStat(this));
		return TypedActionResult.success(user.getStackInHand(hand), world.isClient());
	}
	
	// avoids direct reference without using a proxy interface
	private static void openBook(Identifier bookId){
		try{
			Class.forName("arcana.client.ArcanaClient").getMethod("openBook", Identifier.class).invoke(null, bookId);
		}catch(Exception e){
			e.printStackTrace();
		}
	}
}