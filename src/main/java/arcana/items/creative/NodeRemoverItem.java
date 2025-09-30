package arcana.items.creative;

import arcana.aura.AuraWorld;
import arcana.aura.Node;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.world.World;

import java.util.Optional;

public class NodeRemoverItem extends Item{
	
	public NodeRemoverItem(Settings settings){
		super(settings);
	}
	
	public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand){
		Optional<Node> nodeO = AuraWorld.from(world).raycastNodes(user, true);
		if(nodeO.isPresent()){
			nodeO.get().destroy(false);
			return TypedActionResult.success(user.getStackInHand(hand));
		}
		return super.use(world, user, hand);
	}
}