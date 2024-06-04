package arcana.client.ber;

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
import net.minecraft.util.math.Vec3f;
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
		// this doesn't work at all and i'm massively overcomplicating it i'm sorry
		
		BlockModelRenderer.enableBrightnessCache();
		matrices.push();
		//matrices.translate(.5, .5, .5);
		// rotate to pick an empty side
		BlockState state = be.getWorld().getBlockState(be.getPos());
		//matrices.multiply(Vec3f.POSITIVE_X.getDegreesQuaternion(90));
		if(state.get(ConnectingBlock.UP))
			if(!state.get(ConnectingBlock.NORTH))
				matrices.multiply(Vec3f.NEGATIVE_X.getDegreesQuaternion(90));
			else if(!state.get(ConnectingBlock.EAST))
				matrices.multiply(Vec3f.NEGATIVE_Z.getDegreesQuaternion(90));
			else if(!state.get(ConnectingBlock.SOUTH))
				matrices.multiply(Vec3f.POSITIVE_X.getDegreesQuaternion(90));
			else if(!state.get(ConnectingBlock.WEST))
				matrices.multiply(Vec3f.POSITIVE_Z.getDegreesQuaternion(90));
			else if(!state.get(ConnectingBlock.DOWN))
				matrices.multiply(Vec3f.NEGATIVE_X.getDegreesQuaternion(180));
		// set base gear height
		matrices.translate(1, 1, 0);
		// modify height & rotation based on state
		if(be.enabled()){
			// display higher up
			// if lastChangedTick is less than 20 different from the current tick, transition
			float tickDiff = Math.min(10, (be.getWorld().getTime() + tickDelta) - be.lastChangedTick);
			float heightDiff = (tickDiff / 10) * .07f;
			float rotationDiff = (tickDiff / 10) * 135;
			//matrices.translate(0, heightDiff, 0);
			matrices.multiply(Vec3f.POSITIVE_Z.getDegreesQuaternion(rotationDiff + 45));
		}else{
			float tickDiff = Math.min(10, (be.getWorld().getTime() + tickDelta) - be.lastChangedTick);
			float heightDiff = (1 - (tickDiff / 10)) * .07f;
			float rotationDiff = (1 - (tickDiff / 10)) * 135;
			//matrices.translate(0, heightDiff, 0);
			matrices.multiply(Vec3f.POSITIVE_Z.getDegreesQuaternion(rotationDiff + 45));
		}
		//matrices.scale(0.5f, 0.5f, 0.5f);
		
		BakedModelManager modelManager = MinecraftClient.getInstance().getBakedModelManager();
		BlockRenderManager renderManager = MinecraftClient.getInstance().getBlockRenderManager();
		
		BakedModel model = modelManager.getModel(new ModelIdentifier(arcId("essentia_valve"), "inventory"));
		VertexConsumer buffer = vertexConsumers.getBuffer(TexturedRenderLayers.getEntityCutout());
		renderManager.getModelRenderer().render(be.getWorld(), model, state, be.getPos(), matrices, buffer, false, Random.create(), state.getRenderingSeed(be.getPos()), overlay);
		
		matrices.pop();
		BlockModelRenderer.disableBrightnessCache();
	}
}