package arcana.client.ber;

import arcana.blocks.courier.CourierBlock;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.block.entity.BlockEntityRenderer;
import net.minecraft.client.util.math.MatrixStack;

public class CourierBlockEntityRenderer implements BlockEntityRenderer<CourierBlock.BE>{
	
	public void render(CourierBlock.BE entity,
	                   float tickDelta,
	                   MatrixStack matrices,
	                   VertexConsumerProvider vertexConsumers,
	                   int light,
	                   int overlay){
		// get model...
		// get last/next pos...
		// get lerp...
		switch(entity.getState()){
			case SLEEP -> {
			}
			case SPIN_UP -> {
			}
			case MOVE_TO_SOURCE -> {
			}
			case PICKUP -> {
			}
			case MOVE_TO_SINK -> {
			}
			case DROP -> {
			}
		}
	}
}