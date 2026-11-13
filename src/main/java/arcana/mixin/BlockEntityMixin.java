package arcana.mixin;

import arcana.duck.ArcanaBlockEntity;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.inventory.Inventory;
import net.minecraft.inventory.InventoryChangedListener;
import net.minecraft.util.math.BlockPos;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.ArrayList;
import java.util.List;

@Mixin(BlockEntity.class)
public class BlockEntityMixin implements ArcanaBlockEntity{
	
	@Unique
	private List<InventoryChangedListener> listeners;
	
	@Inject(method = "<init>", at = @At("TAIL"))
	private void init(BlockEntityType type, BlockPos pos, BlockState state, CallbackInfo ci){
		if(this instanceof Inventory)
			listeners = new ArrayList<>();
	}
	
	@Inject(method = "markDirty()V", at = @At("TAIL"))
	void markDirty(CallbackInfo ci){
		if(listeners != null)
			for(InventoryChangedListener listener : listeners)
				listener.onInventoryChanged((Inventory)this);
	}
	
	public void arcana$addListener(InventoryChangedListener listener){
		if(listeners != null)
			listeners.add(listener);
	}
}