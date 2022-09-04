package arcana.network;

import arcana.Networking;
import arcana.ReflectivelyUtilized;
import arcana.items.FocusItem;
import arcana.items.WandItem;
import com.unascribed.lib39.tunnel.api.C2SMessage;
import com.unascribed.lib39.tunnel.api.NetworkContext;
import com.unascribed.lib39.tunnel.api.annotation.field.MarshalledAs;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Hand;

import java.util.ArrayList;
import java.util.List;

public class PkSwapFocus extends C2SMessage{
	
	Hand hand;
	
	@MarshalledAs("int32") // would be varint, but -1 is valid
	int focusIdx;
	
	@ReflectivelyUtilized
	public PkSwapFocus(NetworkContext ctx){
		super(ctx);
	}
	
	public PkSwapFocus(Hand hand, int focusIdx){
		super(Networking.arcCtx);
		this.hand = hand;
		this.focusIdx = focusIdx;
	}
	
	protected void handle(ServerPlayerEntity player){
		ItemStack wandStack = player.getStackInHand(hand);
		if(wandStack.getItem() instanceof WandItem){
			var currentFocus = WandItem.focusFrom(wandStack);
			if(focusIdx >= 0){
				List<ItemStack> foci = getFoci(player);
				if(focusIdx < foci.size()){
					player.giveItemStack(currentFocus);
					ItemStack focus = foci.get(focusIdx);
					WandItem.putFocus(wandStack, focus);
					player.getInventory().removeOne(focus);
				}
			}else if(!currentFocus.isEmpty()){
				player.giveItemStack(currentFocus);
				WandItem.putFocus(wandStack, ItemStack.EMPTY);
			}
		}
	}
	
	public static List<ItemStack> getFoci(PlayerEntity player){
		//TODO: focus pouch?
		List<ItemStack> foci = new ArrayList<>();
		PlayerInventory inventory = player.getInventory();
		for(int i = 0; i < inventory.size(); i++){
			ItemStack stack = inventory.getStack(i);
			if(stack.getItem() instanceof FocusItem)
				foci.add(stack);
		}
		return foci;
	}
}