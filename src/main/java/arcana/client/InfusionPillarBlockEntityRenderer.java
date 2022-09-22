package arcana.client;

import arcana.ArcanaRegistry;
import arcana.blocks.InfusionPillarBlock;
import arcana.blocks.InfusionPillarBlockEntity;
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
import net.minecraft.util.math.Quaternion;
import net.minecraft.util.math.Vec3f;
import net.minecraft.util.math.random.Random;

import static arcana.Arcana.arcId;

public class InfusionPillarBlockEntityRenderer implements BlockEntityRenderer<InfusionPillarBlockEntity>{
	
	public void render(InfusionPillarBlockEntity entity,
	                   float tickDelta,
	                   MatrixStack matrices,
	                   VertexConsumerProvider vertexConsumers,
	                   int light,
	                   int overlay){
		BlockState state = entity.getWorld().getBlockState(entity.getPos());
		if(!state.isOf(ArcanaRegistry.INFUSION_PILLAR))
			return;
		
		BlockModelRenderer.enableBrightnessCache();
		matrices.push();
		
		BakedModelManager modelManager = MinecraftClient.getInstance().getBakedModelManager();
		BlockRenderManager renderManager = MinecraftClient.getInstance().getBlockRenderManager();
		
		BakedModel base = modelManager.getModel(new ModelIdentifier(arcId("infusion_pillar_base"), ""));
		BakedModel upper = modelManager.getModel(new ModelIdentifier(arcId("infusion_pillar_upper"), ""));
		
		VertexConsumer buffer = vertexConsumers.getBuffer(TexturedRenderLayers.getEntitySolid());
		renderManager.getModelRenderer().render(entity.getWorld(), base, state, entity.getPos(), matrices, buffer, false, Random.create(), state.getRenderingSeed(entity.getPos()), overlay);
		
		matrices.push();
		matrices.translate(0, .55, 0);
		// rotate around the centre
		matrices.translate(.5, 0, .5);
		matrices.multiply(Quaternion.fromEulerYxz((float)(Math.PI / 4), 0, 0));
		// and the block's rotation
		matrices.multiply(Quaternion.fromEulerXyzDegrees(new Vec3f(0, state.get(InfusionPillarBlock.facing).asRotation(), 0)));
		matrices.translate(-.5, 0, -.5);
		matrices.translate(-.03, 0, .1);
		renderManager.getModelRenderer().render(entity.getWorld(), upper, state, entity.getPos(), matrices, buffer, false, Random.create(), state.getRenderingSeed(entity.getPos()), overlay);
		matrices.pop();
		
		matrices.pop();
		BlockModelRenderer.disableBrightnessCache();
	}
}