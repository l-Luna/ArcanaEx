package arcana.mixin;

import arcana.ArcanaRegistry;
import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.PlantBlock;
import net.minecraft.registry.tag.BlockTags;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.BlockView;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(PlantBlock.class)
public class PlantBlockMixin{

	@ModifyReturnValue(method = "canPlantOnTop", at = @At("RETURN"))
	boolean canPlantOn(boolean original, BlockState floor, BlockView world, BlockPos pos){
		return original || (floor.isOf(ArcanaRegistry.STONE_VASE) && ((Block)(Object)this).getRegistryEntry().isIn(BlockTags.SMALL_FLOWERS));
	}
}