package arcana.client.ber;

import arcana.ArcanaRegistry;
import arcana.blocks.be.InfusionMatrixBlockEntity;
import arcana.blocks.be.InfusionPillarBlockEntity;
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
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Quaternion;
import net.minecraft.util.math.Vec3f;
import net.minecraft.util.math.random.LocalRandom;
import net.minecraft.util.math.random.Random;

import static arcana.Arcana.arcId;

public class InfusionPillarBlockEntityRenderer implements BlockEntityRenderer<InfusionPillarBlockEntity>{
	
	public static final ModelIdentifier BASE_ID = new ModelIdentifier(arcId("infusion_pillar_base"), "");
	public static final ModelIdentifier UPPER_ID = new ModelIdentifier(arcId("infusion_pillar_upper"), "");
	public static final ModelIdentifier PEAK_ID = new ModelIdentifier(arcId("infusion_pillar_peak"), "");
	
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
		
		BakedModel base = modelManager.getModel(BASE_ID);
		BakedModel upper = modelManager.getModel(UPPER_ID);
		BakedModel peak = modelManager.getModel(PEAK_ID);
		
		VertexConsumer buffer = vertexConsumers.getBuffer(TexturedRenderLayers.getEntitySolid());
		renderManager.getModelRenderer().render(entity.getWorld(), base, state, entity.getPos(), matrices, buffer, false, Random.create(), state.getRenderingSeed(entity.getPos()), overlay);
		
		float rotSideways = 0, rotVertical = 0, rotUpper = 90;
		BlockPos matrixPos = entity.getMatrixPosition();
		if(matrixPos != null && entity.getWorld().getBlockEntity(matrixPos) instanceof InfusionMatrixBlockEntity matrixBe){
			BlockPos relMatrixPos = matrixPos.subtract(entity.getPos());
			rotUpper = 25;
			rotSideways = (float)(Math.toDegrees(MathHelper.atan2(relMatrixPos.getX(), relMatrixPos.getZ())) + 180);
			
			if(matrixBe.getCurrentRecipe() != null){
				float scale = matrixBe.getInstability() / 10000;
				final int interval = 5;
				// pick target locations every `interval` ticks
				long time = entity.getWorld().getTime();
				Random prevRng = new LocalRandom(entity.hashCode() + time / interval - 1);
				Random rng = new LocalRandom(entity.hashCode() + time / interval);
				float between = (time % interval + tickDelta) / interval;
				between *= between * between;
				prevRng.nextFloat(); rng.nextFloat();
				rotSideways += (MathHelper.lerp(between, prevRng.nextFloat(), rng.nextFloat()) - 0.5f) * 2 * scale;
				rotVertical += (MathHelper.lerp(between, prevRng.nextFloat(), rng.nextFloat()) - 0.5f) * 5 * scale;
				rotUpper += (MathHelper.lerp(between, prevRng.nextFloat(), rng.nextFloat()) - 0.5f) * 9 * scale;
			}
		}
		
		matrices.push();
		matrices.translate(0, .55, 0);
		// rotate around the centre
		matrices.translate(.5, 0, .5);
		// and the block's rotation
		matrices.multiply(Quaternion.fromEulerXyzDegrees(new Vec3f(0, rotSideways, 0)));
		matrices.multiply(Quaternion.fromEulerXyzDegrees(new Vec3f(rotVertical, 0, 0)));
		matrices.translate(-.5, 0, -.5);
		matrices.translate(-.03, 0, .1);
		renderManager.getModelRenderer().render(entity.getWorld(), upper, state, entity.getPos(), matrices, buffer, false, Random.create(), state.getRenderingSeed(entity.getPos()), overlay);
		
		// place and rotate top part correctly
		matrices.translate(0, 17 / 16f, 5.5f / 16f);
		matrices.translate(0, 0, 7 / 16f);
		matrices.multiply(Quaternion.fromEulerXyzDegrees(new Vec3f(rotUpper, 0, 0)));
		matrices.translate(0, 0, -7 / 16f);
		
		renderManager.getModelRenderer().render(entity.getWorld(), peak, state, entity.getPos(), matrices, buffer, false, Random.create(), state.getRenderingSeed(entity.getPos()), overlay);
		matrices.pop();
		
		matrices.pop();
		BlockModelRenderer.disableBrightnessCache();
	}
}