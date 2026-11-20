package arcana.client.ber;

import arcana.aspects.AspectStack;
import arcana.items.components.ArcanaDataComponents;
import net.fabricmc.fabric.api.client.rendering.v1.BuiltinItemRendererRegistry;
import net.minecraft.block.BlockState;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.model.json.ModelTransformationMode;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.ItemStack;

// inspired by https://github.com/miyucomics/hexical/blob/main/src/client/java/miyucomics/hexical/feature/media_jar/MediaJarItemRenderer.kt
public class WardedJarItemRenderer implements BuiltinItemRendererRegistry.DynamicItemRenderer{
	
	private final BlockState renderState;
	private final int capacity;
	
	public WardedJarItemRenderer(BlockState state, int capacity){
		renderState = state;
		this.capacity = capacity;
	}
	
	public void render(ItemStack stack, ModelTransformationMode mode, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, int overlay){
		matrices.push();
		MinecraftClient.getInstance().getBlockRenderManager().renderBlockAsEntity(renderState, matrices, vertexConsumers, light, overlay);
		AspectStack aspectStack = stack.get(ArcanaDataComponents.STORED_SINGLE_ASPECT);
		if(aspectStack != null)
			WardedJarBlockEntityRenderer.renderInner(matrices, vertexConsumers, light, aspectStack, capacity);
		matrices.pop();
	}
}