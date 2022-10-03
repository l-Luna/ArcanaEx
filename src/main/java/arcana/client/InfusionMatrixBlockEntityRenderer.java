package arcana.client;

import arcana.ArcanaRegistry;
import arcana.blocks.InfusionMatrixBlockEntity;
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
import net.minecraft.util.math.Quaternion;
import net.minecraft.util.math.random.Random;

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
		var time = entity.getWorld().getTime();
		var ySpeed = entity.getCurrentRecipe() != null ? 9 : 2.5;
		matrices.translate(0, Math.sin(Math.toRadians((time + tickDelta) * ySpeed)) / 4.5f, 0);
		matrices.translate(.5, .8, .5);
		matrices.multiply(Quaternion.fromEulerXyz(0, (float)Math.toRadians(time + tickDelta), (float)Math.toRadians((time + tickDelta) / 4)));
		matrices.multiply(Quaternion.fromEulerYxz(0, MathHelper.HALF_PI / 2f, MathHelper.HALF_PI / 2f));
		matrices.scale(.8f, .8f, .8f);
		matrices.translate(-.5, -.5, -.5);
		
		BakedModelManager modelManager = MinecraftClient.getInstance().getBakedModelManager();
		BlockRenderManager renderManager = MinecraftClient.getInstance().getBlockRenderManager();
		
		BakedModel model = modelManager.getModel(new ModelIdentifier(arcId("infusion_matrix"), ""));
		VertexConsumer buffer = vertexConsumers.getBuffer(TexturedRenderLayers.getEntitySolid());
		renderManager.getModelRenderer().render(entity.getWorld(), model, state, entity.getPos(), matrices, buffer, false, Random.create(), state.getRenderingSeed(entity.getPos()), overlay);
		
		matrices.pop();
		BlockModelRenderer.disableBrightnessCache();
		
		/*InfusionMatrixBlockEntity.InfusionPhase phase = entity.currentPhase();
		if(phase != null)
			phase.render(entity, matrices, vertexConsumers, tickDelta, entity.getStateForPhase(phase));*/
	}
}