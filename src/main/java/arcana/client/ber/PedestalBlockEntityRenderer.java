package arcana.client.ber;

import arcana.blocks.be.PedestalBlockEntity;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.block.entity.BlockEntityRenderer;
import net.minecraft.client.render.block.entity.BlockEntityRendererFactory;
import net.minecraft.client.render.item.ItemRenderer;
import net.minecraft.client.render.model.json.ModelTransformationMode;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.RotationAxis;

public class PedestalBlockEntityRenderer implements BlockEntityRenderer<PedestalBlockEntity>{
	
	private final ItemRenderer renderer;
	
	public PedestalBlockEntityRenderer(BlockEntityRendererFactory.Context ctx){
		renderer = ctx.getItemRenderer();
	}
	
	public void render(PedestalBlockEntity pedestal,
	                   float tickDelta,
	                   MatrixStack matrices,
	                   VertexConsumerProvider vertexConsumers,
	                   int light,
	                   int overlay){
		ItemStack toDisplay = pedestal.getStack();
		if(!toDisplay.isEmpty()){
			double rot = ((pedestal.getWorld().getTime() % 360) + tickDelta) % 360;
			double height = rot * 2;
			matrices.push();
			matrices.translate(.5, 1.3, .5);
			matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees((float)rot));
			matrices.translate(0, Math.sin(Math.toRadians(height)) / 12f, 0);
			if(toDisplay.getItem() instanceof BlockItem){
				matrices.translate(0, -.25, 0);
				matrices.scale(1.4f, 1.4f, 1.4f);
			}
			matrices.scale(1.1f, 1.1f, 1.1f);
			renderer.renderItem(
					toDisplay,
					ModelTransformationMode.GROUND,
					light,
					overlay,
					matrices,
					vertexConsumers,
					pedestal.getWorld(),
					0
			);
			matrices.pop();
		}
	}
}