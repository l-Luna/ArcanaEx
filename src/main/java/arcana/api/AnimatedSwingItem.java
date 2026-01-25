package arcana.api;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Arm;
import net.minecraft.util.Hand;

@Environment(EnvType.CLIENT)
public interface AnimatedSwingItem{

	@Environment(EnvType.CLIENT)
	boolean applySwingAnimation(MatrixStack matrices, PlayerEntity player, ItemStack stack, float tickDelta, float swingProgress, float equipProgress, Hand hand, Arm arm);
}