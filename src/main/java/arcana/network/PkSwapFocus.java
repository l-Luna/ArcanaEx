package arcana.network;

import arcana.ReflectivelyUtilized;
import arcana.items.FocusItem;
import arcana.items.FocusPouchItem;
import arcana.items.WandItem;
import arcana.util.ArrayInventory;
import com.unascribed.lib39.tunnel.api.C2SMessage;
import com.unascribed.lib39.tunnel.api.NetworkContext;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.StackReference;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;

import java.util.ArrayList;
import java.util.List;

public class PkSwapFocus extends C2SMessage{
	
	boolean fwd;
	
	@ReflectivelyUtilized
	public PkSwapFocus(NetworkContext ctx){
		super(ctx);
	}
	
	public PkSwapFocus(boolean fwd){
		super(Networking.context);
		this.fwd = fwd;
	}
	
	protected void handle(ServerPlayerEntity player){
		ItemStack wandStack;
		ItemStack mainHand = player.getMainHandStack(), offHand = player.getOffHandStack();
		if((!((wandStack = mainHand).getItem() instanceof WandItem) && !((wandStack = offHand).getItem() instanceof WandItem)))
			return;
		
		go(player, fwd, wandStack);
	}
	
	public static void go(PlayerEntity player, boolean fwd, ItemStack wandStack){
		List<StackReference> storage = new ArrayList<>();
		List<StackReference> quickAccess = new ArrayList<>();
		gatherFoci(player, storage, quickAccess);
		if(!quickAccess.isEmpty()){
			// TODO: focus pouch hotbar swapping
		}else if(!storage.isEmpty()){
			// TODO: and this is kind of silly
			ItemStack old = WandItem.focusFrom(wandStack);
			int last = storage.size() - 1;
			if(fwd){
				WandItem.putFocus(wandStack, storage.get(0).get());
				for(int i = 0; i < last; i++)
					storage.get(i).set(storage.get(i + 1).get());
				storage.get(last).set(old);
			}else{
				WandItem.putFocus(wandStack, storage.get(last).get());
				for(int i = last; i > 0; i--)
					storage.get(i).set(storage.get(i - 1).get());
				storage.get(0).set(old);
			}
		}
	}
	
	public static void gatherFoci(PlayerEntity player, List<StackReference> storage, List<StackReference> quickAccess){
		PlayerInventory playerInv = player.getInventory();
		for(int i = 0; i < playerInv.size(); i++){
			ItemStack stack = playerInv.getStack(i);
			if(stack.getItem() instanceof FocusItem)
				storage.add(StackReference.of(playerInv, i));
			else if(stack.getItem() instanceof FocusPouchItem){
				ArrayInventory pouchInv = FocusPouchItem.inventoryFrom(stack);
				for(int j = 0; j < pouchInv.size(); j++){
					int finalIdx = j;
					if(!pouchInv.getStack(j).isEmpty()){
						List<StackReference> target = j < 9 ? quickAccess : storage;
						// TODO: generify? `ItemStackInventorySlotReference` is a bit wordy
						target.add(new StackReference(){
							public ItemStack get(){
								return pouchInv.getStack(finalIdx);
							}
							
							public boolean set(ItemStack newStack){
								pouchInv.setStack(finalIdx, newStack);
								FocusPouchItem.setInventory(stack, pouchInv);
								return true;
							}
						});
					}
				}
			}
		}
	}
}