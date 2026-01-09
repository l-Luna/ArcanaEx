package arcana.network;

import arcana.ReflectivelyUtilized;
import arcana.items.FocusItem;
import arcana.items.FocusPouchItem;
import arcana.items.WandItem;
import com.unascribed.lib39.tunnel.api.C2SMessage;
import com.unascribed.lib39.tunnel.api.NetworkContext;
import com.unascribed.lib39.tunnel.api.annotation.field.MarshalledAs;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Hand;

public class PkSwapFocus extends C2SMessage{
	
	Hand hand;
	
	// would be varint, but -1 is valid
	@MarshalledAs("int32")
	int focusIdx;
	@MarshalledAs("int32")
	int pouchIdx;
	
	@ReflectivelyUtilized
	public PkSwapFocus(NetworkContext ctx){
		super(ctx);
	}
	
	public PkSwapFocus(Hand hand, int focusIdx, int pouchIdx){
		super(Networking.context);
		this.hand = hand;
		this.focusIdx = focusIdx;
		this.pouchIdx = pouchIdx;
	}
	
	protected void handle(ServerPlayerEntity player){
		ItemStack wandStack = player.getStackInHand(hand);
		if(wandStack.getItem() instanceof WandItem){
			Inventory target = player.getInventory();
			if(pouchIdx >= 0){
				ItemStack pouchStack = player.getInventory().getStack(pouchIdx);
				if(pouchStack.getItem() instanceof FocusPouchItem)
					target = FocusPouchItem.inventoryFrom(pouchStack);
			}
			insertStack(WandItem.focusFrom(wandStack), target, player);
			WandItem.putFocus(wandStack, ItemStack.EMPTY);
			if(focusIdx >= 0){
				ItemStack candFocusStack = target.getStack(focusIdx);
				if(candFocusStack.getItem() instanceof FocusItem){
					WandItem.putFocus(wandStack, candFocusStack);
					target.setStack(focusIdx, ItemStack.EMPTY);
				}
			}
		}
	}
	
	private static void insertStack(ItemStack stack, Inventory inventory, PlayerEntity player){
		for(int i = 0; i < inventory.size(); i++)
			if(inventory.getStack(i).isEmpty()){
				inventory.setStack(i, stack);
				return;
			}
		player.giveItemStack(stack);
	}
}