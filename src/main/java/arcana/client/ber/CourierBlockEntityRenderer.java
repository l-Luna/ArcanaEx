package arcana.client.ber;

import arcana.blocks.courier.CourierBlock;
import arcana.blocks.courier.QueuedTransfer;
import arcana.blocks.courier.SymbolConfig;
import arcana.blocks.courier.SymbolInstance;
import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.block.entity.BlockEntityRenderer;
import net.minecraft.client.render.item.ItemRenderer;
import net.minecraft.client.render.model.json.ModelTransformationMode;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.ItemStack;
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
		Vec3d cPos = pos.toCenterPos();
		matrices.push();
		// get model...
		// get last/next pos...
		// get lerp...
		float stateLerp = Math.clamp((entity.getStateTicks() + tickDelta) / 14f, 0, 1) /*CourierBlock.STATE_TIME*/;
		stateLerp *= stateLerp;
		QueuedTransfer<?> transfer = entity.getCurrentTransfer();
		switch(entity.getState()){
			case SLEEP -> {
			}
			case SPIN_UP -> {
			}
			case MOVE_TO_SOURCE -> {
			}
			case PICKUP -> {
				if(transfer != null && transfer.stack().resource() instanceof ItemVariant iv){
					ItemStack stack = iv.toStack((int)transfer.stack().amount());
					
					Vec3d sourcePos = transfer.source().pos.toCenterPos().subtract(cPos);
					matrices.translate(0.5f, 0.5f + stateLerp, 0.5f);
					matrices.translate(sourcePos.x, sourcePos.y, sourcePos.z);
					
					ItemRenderer renderer = MinecraftClient.getInstance().getItemRenderer();
					renderer.renderItem(
							stack,
							ModelTransformationMode.GROUND,
							light,
							overlay,
							matrices,
							vertexConsumers,
							entity.getWorld(),
							0
					);
				}
			}
			case MOVE_TO_SINK -> {
				if(transfer != null && transfer.stack().resource() instanceof ItemVariant iv){
					ItemStack stack = iv.toStack((int)transfer.stack().amount());
					
					Vec3d lerpPos = transfer.source().pos.toCenterPos().lerp(transfer.sink().pos.toCenterPos(), stateLerp).subtract(cPos);
					matrices.translate(0.5f, 1.5f, 0.5f);
					matrices.translate(lerpPos.x, lerpPos.y, lerpPos.z);
					
					ItemRenderer renderer = MinecraftClient.getInstance().getItemRenderer();
					renderer.renderItem(
							stack,
							ModelTransformationMode.GROUND,
							light,
							overlay,
							matrices,
							vertexConsumers,
							entity.getWorld(),
							0
					);
				}
			}
			case DROP -> {
				if(transfer != null && transfer.stack().resource() instanceof ItemVariant iv){
					ItemStack stack = iv.toStack((int)transfer.stack().amount());
					
					Vec3d sinkPos = transfer.sink().pos.toCenterPos().subtract(cPos);
					matrices.translate(0.5f, 1.5f - stateLerp, 0.5f);
					matrices.translate(sinkPos.x, sinkPos.y, sinkPos.z);
					
					ItemRenderer renderer = MinecraftClient.getInstance().getItemRenderer();
					renderer.renderItem(
							stack,
							ModelTransformationMode.GROUND,
							light,
							overlay,
							matrices,
							vertexConsumers,
							entity.getWorld(),
							0
					);
					
				}
			}
		}
		matrices.pop();
		
		// draw connections to symbols
		matrices.push();
		matrices.translate(0.5f, 0.5f, 0.5f);
		MatrixStack.Entry entry = matrices.peek();
		VertexConsumer buffer = vertexConsumers.getBuffer(RenderLayer.getLines());
		for(SymbolInstance symbol : entity.getSymbols()){
			BlockPos symPos = symbol.pos;
			boolean source = symbol.config.direction == SymbolConfig.SymbolDirection.SOURCE;
			Vec3d from = symPos.toCenterPos(), to = pos.toCenterPos();
			Vec3d diff = from.subtract(to);
			Vec3d normal = diff.normalize();
			buffer.vertex(entry, 0, 0, 0)
					.color(source ? 1f : 0.1f, 1f, source ? 0.1f : 1f, 1f)
					.normal(entry, (float)normal.x, (float)normal.y, (float)normal.z);
			buffer.vertex(entry, diff.toVector3f())
					.color(1f, 1f, 1f, 1f)
					.normal(entry, (float)normal.x, (float)normal.y, (float)normal.z);
		}
		matrices.pop();
	}
}