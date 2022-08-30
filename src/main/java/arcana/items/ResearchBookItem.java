package arcana.items;

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
		ItemStack stack = user.getStackInHand(hand);
		openBook(bookId);
		user.incrementStat(Stats.USED.getOrCreateStat(this));
		return TypedActionResult.success(stack, world.isClient());
	}
	
	// "just use packets" no
	private static void openBook(Identifier bookId){
		try{
			Class.forName("arcana.client.ArcanaClient").getMethod("openBook", Identifier.class).invoke(null, bookId);
		}catch(Exception e){
			e.printStackTrace();
		}
	}
}