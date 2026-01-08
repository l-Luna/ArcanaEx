package arcana.mixin;

import arcana.duck.ArcanaItem;
import arcana.items.ArcanaItemSettings;
import arcana.items.FragileComponent;
import net.minecraft.item.Item;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Item.class)
public class ItemMixin implements ArcanaItem{
	
	@Unique
	@Nullable
	private FragileComponent fragileComponent;
	
	@Inject(method = "<init>", at = @At("TAIL"))
	void init(Item.Settings settings, CallbackInfo ci){
		if(settings instanceof ArcanaItemSettings ais)
			fragileComponent = ais.getFragileComponent();
	}
	
	public @Nullable FragileComponent arcana$getFragileComponent(){
		return fragileComponent;
	}
}