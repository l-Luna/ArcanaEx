package arcana.blocks.courier;

import net.fabricmc.fabric.api.transfer.v1.storage.TransferVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.base.ResourceAmount;

public record QueuedTransfer<T extends TransferVariant<?>>(
		SymbolStorage.ResourceHandle<T> source,
		SymbolInstance sink,
		ResourceAmount<T> stack
){
	
	public boolean sourceValid(){
		return source.valid() && source.getStack().resource().equals(stack.resource()) && source.getStack().amount() <= stack.amount();
	}
}