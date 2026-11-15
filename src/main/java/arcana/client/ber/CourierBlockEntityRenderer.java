package arcana.client.ber;

import arcana.blocks.courier.CourierBlock;
import arcana.blocks.courier.SymbolConfig;
import arcana.blocks.courier.SymbolInstance;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.block.entity.BlockEntityRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;

public class CourierBlockEntityRenderer implements BlockEntityRenderer<CourierBlock.BE>{
	
	public void render(CourierBlock.BE entity,
	                   float tickDelta,
	                   MatrixStack matrices,
	                   VertexConsumerProvider vertexConsumers,
	                   int light,
	                   int overlay){
		BlockPos pos = entity.getPos();
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
		
		// draw connections to symbols
		MatrixStack.Entry entry = matrices.peek();
		VertexConsumer buffer = vertexConsumers.getBuffer(RenderLayer.getLines());
		for(SymbolInstance symbol : entity.getSymbols()){
			BlockPos symPos = symbol.pos;
			boolean source = symbol.config.direction == SymbolConfig.SymbolDirection.SOURCE;
			Vec3d from = symPos.toCenterPos(), to = pos.toCenterPos();
			Vec3d normal = to.subtract(from).normalize();
			buffer.vertex(from.toVector3f())
					.color(source ? 1f : 0.1f, 1f, source ? 0.1f : 1f, 1f)
					.normal(entry, (float)normal.x, (float)normal.y, (float)normal.z);
			buffer.vertex(to.toVector3f())
					.color(1f, 1f, 1f, 1f)
					.normal(entry, (float)normal.x, (float)normal.y, (float)normal.z);
		}
	}
}