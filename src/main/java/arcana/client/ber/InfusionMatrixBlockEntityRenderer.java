package arcana.client.ber;

import arcana.ArcanaRegistry;
import arcana.blocks.be.InfusionMatrixBlockEntity;
import arcana.client.ArcanaClient;
import net.minecraft.block.BlockState;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.TexturedRenderLayers;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.block.BlockModelRenderer;
import net.minecraft.client.render.block.BlockRenderManager;
import net.minecraft.client.render.block.entity.BlockEntityRenderer;
import net.minecraft.client.render.model.BakedModel;
import net.minecraft.client.render.model.BakedModelManager;
import net.minecraft.client.util.ModelIdentifier;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.random.Random;
import org.joml.Quaternionf;

import static arcana.Arcana.arcId;

public class InfusionMatrixBlockEntityRenderer implements BlockEntityRenderer<InfusionMatrixBlockEntity>{
	
	public void render(InfusionMatrixBlockEntity entity,
	                   float tickDelta,
	                   MatrixStack matrices,
	                   VertexConsumerProvider vertexConsumers,
	                   int light,
	                   int overlay){
		BlockState state = entity.getWorld().getBlockState(entity.getPos());
		if(!state.isOf(ArcanaRegistry.INFUSION_MATRIX))
			return;
		
		BlockModelRenderer.enableBrightnessCache();
		matrices.push();
		boolean crafting = entity.getCurrentRecipe() != null;
		if(entity.isActivated()){
			long time = entity.getWorld().getTime();
			long eventTime = entity.getLastCraftStartEndTime();
			float transDelta = eventTime == -1 || time - eventTime >= 15 ? 1 : ((float)(time - eventTime) + tickDelta) / 15f;
			
			double normalY = ArcanaClient.osc(40) / 4.5f;
			double craftingY = ArcanaClient.osc(25) / 3.5f;
			double lerpedY = crafting ? MathHelper.lerp(transDelta, normalY, craftingY) : MathHelper.lerp(transDelta, craftingY, normalY);
			
			matrices.translate(0, lerpedY, 0);
			matrices.translate(.5, .8, .5);
			matrices.multiply(new Quaternionf().rotateXYZ(0, (float)Math.toRadians(time + tickDelta), (float)Math.toRadians((time + tickDelta) / 4)));
			matrices.multiply(new Quaternionf().rotateXYZ(MathHelper.HALF_PI / 2f, 0, MathHelper.HALF_PI / 2f));
		}else
			matrices.translate(.5, .5, .5);
		matrices.scale(.8f, .8f, .8f);
		matrices.translate(-.5, -.5, -.5);
		
		BakedModelManager modelManager = MinecraftClient.getInstance().getBakedModelManager();
		BlockRenderManager renderManager = MinecraftClient.getInstance().getBlockRenderManager();
		
		BakedModel model = crafting ? modelManager.getModel(arcId("block/infusion_matrix_active")) : modelManager.getModel(new ModelIdentifier(arcId("infusion_matrix"), ""));
		VertexConsumer buffer = vertexConsumers.getBuffer(TexturedRenderLayers.getEntitySolid());
		renderManager.getModelRenderer().render(entity.getWorld(), model, state, entity.getPos(), matrices, buffer, false, Random.create(), state.getRenderingSeed(entity.getPos()), overlay);
		
		matrices.pop();
		BlockModelRenderer.disableBrightnessCache();
	}
}