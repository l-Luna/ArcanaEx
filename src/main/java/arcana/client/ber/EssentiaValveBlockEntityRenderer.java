package arcana.client.ber;

import arcana.ArcanaRegistry;
import arcana.blocks.tubes.EssentiaValveBlockEntity;
import net.minecraft.block.BlockState;
import net.minecraft.block.ConnectingBlock;
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
import net.minecraft.util.Identifier;
import net.minecraft.util.math.RotationAxis;
import net.minecraft.util.math.random.Random;

import static arcana.Arcana.arcId;

public class EssentiaValveBlockEntityRenderer implements BlockEntityRenderer<EssentiaValveBlockEntity>{
	
	public static final Identifier GEAR_TEX = arcId("block/essentia_valve");
	
	public void render(EssentiaValveBlockEntity be,
	                   float tickDelta,
	                   MatrixStack matrices,
	                   VertexConsumerProvider vertexConsumers,
	                   int light,
	                   int overlay){
		BlockModelRenderer.enableBrightnessCache();
		matrices.push();
		
		// apply everything to the centre of the model
		matrices.translate(0.5, 0.5, 0.5);
		
		// rotate to pick an empty side
		BlockState state = be.getWorld().getBlockState(be.getPos());
		if(state.isOf(ArcanaRegistry.ESSENTIA_VALVE) && state.get(ConnectingBlock.UP))
			if(!state.get(ConnectingBlock.NORTH))
				matrices.multiply(RotationAxis.NEGATIVE_X.rotationDegrees(90));
			else if(!state.get(ConnectingBlock.EAST))
				matrices.multiply(RotationAxis.NEGATIVE_Z.rotationDegrees(90));
			else if(!state.get(ConnectingBlock.SOUTH))
				matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(90));
			else if(!state.get(ConnectingBlock.WEST))
				matrices.multiply(RotationAxis.POSITIVE_Z.rotationDegrees(90));
			else if(!state.get(ConnectingBlock.DOWN))
				matrices.multiply(RotationAxis.NEGATIVE_X.rotationDegrees(180));
		
		// set base gear height
		matrices.translate(0, 0.2, 0);
		
		// modify height & rotation based on state
		if(be.enabled()){
			// display higher up
			// if lastChangedTick is less than 20 different from the current tick, transition
			float tickDiff = Math.min(10, (be.getWorld().getTime() + tickDelta) - be.lastChangedTick);
			float heightDiff = (tickDiff / 10) * .06f;
			float rotationDiff = (tickDiff / 10) * 135;
			matrices.translate(0, heightDiff, 0);
			matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(rotationDiff + 45));
		}else{
			float tickDiff = Math.min(10, (be.getWorld().getTime() + tickDelta) - be.lastChangedTick);
			float heightDiff = (1 - (tickDiff / 10)) * .06f;
			float rotationDiff = (1 - (tickDiff / 10)) * 135;
			matrices.translate(0, heightDiff, 0);
			matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(rotationDiff + 45));
		}
		
		// shrink gear model
		matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(90));
		matrices.scale(0.7f, 0.7f, 0.7f);
		
		// un-center
		matrices.translate(-0.5, -0.5, -0.5);
		
		BakedModelManager modelManager = MinecraftClient.getInstance().getBakedModelManager();
		BlockRenderManager renderManager = MinecraftClient.getInstance().getBlockRenderManager();
		
		BakedModel model = modelManager.getModel(new ModelIdentifier(arcId("essentia_valve"), "inventory"));
		VertexConsumer buffer = vertexConsumers.getBuffer(TexturedRenderLayers.getEntityCutout());
		renderManager.getModelRenderer().render(be.getWorld(), model, state, be.getPos(), matrices, buffer, false, Random.create(), state.getRenderingSeed(be.getPos()), overlay);
		
		matrices.pop();
		BlockModelRenderer.disableBrightnessCache();
	}
}