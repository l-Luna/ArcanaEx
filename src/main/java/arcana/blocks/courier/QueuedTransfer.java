package arcana.blocks.courier;

import net.fabricmc.fabric.api.transfer.v1.storage.TransferVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.base.ResourceAmount;

public record QueuedTransfer<T extends TransferVariant<?>>(
		SymbolInstance source, // for syncing
		SymbolStorage.ResourceHandle<T> sourceHandle,
		int sourceHandleIdx, // for syncing
		SymbolInstance sink,
		ResourceAmount<T> stack
){
	
	public boolean sourceValid(){
		return sourceHandle.valid() && sourceHandle.getStack().resource().equals(stack.resource()) && sourceHandle.getStack().amount() <= stack.amount();
	}
}