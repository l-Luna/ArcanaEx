package arcana.screens;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.ScreenHandlerType;
import org.jetbrains.annotations.Nullable;

public class ArcaneFurnaceScreenHandler extends ScreenHandler{
	
	protected ArcaneFurnaceScreenHandler(@Nullable ScreenHandlerType<?> type, int syncId){
		super(type, syncId);
	}
	
	public ItemStack transferSlot(PlayerEntity player, int index){
		return null;
	}
	
	public boolean canUse(PlayerEntity player){
		return false;
	}
}