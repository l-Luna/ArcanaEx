package arcana.items;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Hand;
import net.minecraft.util.Identifier;
import net.minecraft.util.TypedActionResult;
import net.minecraft.world.World;

public class DirectResearchEntryItem extends Item{
	
	private final Identifier bookId;
	
	public DirectResearchEntryItem(Settings settings, Identifier id){
		super(settings);
		bookId = id;
	}
	
	public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand){
		if(world.isClient)
			openEntry(bookId);
		return TypedActionResult.success(user.getStackInHand(hand), world.isClient());
	}
	
	// avoids direct reference without using a proxy interface
	public static void openEntry(Identifier entryId){
		try{
			Class.forName("arcana.client.ArcanaClient").getMethod("openEntry", Identifier.class).invoke(null, entryId);
		}catch(Exception e){
			e.printStackTrace();
		}
	}
}