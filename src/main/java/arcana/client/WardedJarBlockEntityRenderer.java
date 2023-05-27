package arcana.client;

import arcana.aspects.AspectStack;
import arcana.blocks.WardedJarBlockEntity;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.block.entity.BlockEntityRenderer;
import net.minecraft.client.texture.SpriteAtlasTexture;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;

import static arcana.Arcana.arcId;

public class WardedJarBlockEntityRenderer implements BlockEntityRenderer<WardedJarBlockEntity>{
	
	public static final Identifier topTexture = arcId("block/jar_fluid/top");
	public static final Identifier sideTexture = arcId("block/jar_fluid/side");
	public static final Identifier bottomTexture = arcId("block/jar_fluid/bottom");
	
	public void render(WardedJarBlockEntity entity,
	                   float tickDelta,
	                   MatrixStack matrices,
	                   VertexConsumerProvider vcs,
	                   int light,
	                   int overlay){
		AspectStack stack = entity.getStored();
		if(stack != null){
			var sprites = MinecraftClient.getInstance().getSpriteAtlas(SpriteAtlasTexture.BLOCK_ATLAS_TEXTURE);
			var top = sprites.apply(topTexture);
			var side = sprites.apply(sideTexture);
			var bottom = sprites.apply(bottomTexture);
			
			VertexConsumer cons = vcs.getBuffer(RenderLayer.getSolid());
			int colour = stack.type().colour() + 0xFF000000;
			float height = stack.amount() / 100f;
			
			matrices.push();
			matrices.translate(.25, .075, .25);
			matrices.scale(.5f, .6f * height, .5f);
			
			// top
			vertex(cons, matrices, colour, 0, 1, 1, top.getMinU(), top.getMaxV(), light);
			vertex(cons, matrices, colour, 1, 1, 1, top.getMaxU(), top.getMaxV(), light);
			vertex(cons, matrices, colour, 1, 1, 0, top.getMaxU(), top.getMinV(), light);
			vertex(cons, matrices, colour, 0, 1, 0, top.getMinU(), top.getMinV(), light);
			
			// bottom
			vertex(cons, matrices, colour, 0, 0, 0, bottom.getMinU(), bottom.getMinV(), light);
			vertex(cons, matrices, colour, 1, 0, 0, bottom.getMaxU(), bottom.getMinV(), light);
			vertex(cons, matrices, colour, 1, 0, 1, bottom.getMaxU(), bottom.getMaxV(), light);
			vertex(cons, matrices, colour, 0, 0, 1, bottom.getMinU(), bottom.getMaxV(), light);
			
			var sideMinV = side.getMaxV() - height * (side.getMaxV() - side.getMinV());
			
			// east (+X) face
			vertex(cons, matrices, colour, 1, 1, 0, side.getMinU(), sideMinV, light);
			vertex(cons, matrices, colour, 1, 1, 1, side.getMaxU(), sideMinV, light);
			vertex(cons, matrices, colour, 1, 0, 1, side.getMaxU(), side.getMaxV(), light);
			vertex(cons, matrices, colour, 1, 0, 0, side.getMinU(), side.getMaxV(), light);
			
			// west (-X) face
			vertex(cons, matrices, colour, 0, 1, 0, side.getMinU(), sideMinV, light);
			vertex(cons, matrices, colour, 0, 0, 0, side.getMinU(), side.getMaxV(), light);
			vertex(cons, matrices, colour, 0, 0, 1, side.getMaxU(), side.getMaxV(), light);
			vertex(cons, matrices, colour, 0, 1, 1, side.getMaxU(), sideMinV, light);
			
			// north (-Z) face
			vertex(cons, matrices, colour, 1, 0, 0, side.getMaxU(), side.getMaxV(), light);
			vertex(cons, matrices, colour, 0, 0, 0, side.getMinU(), side.getMaxV(), light);
			vertex(cons, matrices, colour, 0, 1, 0, side.getMinU(), sideMinV, light);
			vertex(cons, matrices, colour, 1, 1, 0, side.getMaxU(), sideMinV, light);
			
			// south (+Z) face
			vertex(cons, matrices, colour, 0, 0, 1, side.getMinU(), side.getMaxV(), light);
			vertex(cons, matrices, colour, 1, 0, 1, side.getMaxU(), side.getMaxV(), light);
			vertex(cons, matrices, colour, 1, 1, 1, side.getMaxU(), sideMinV, light);
			vertex(cons, matrices, colour, 0, 1, 1, side.getMinU(), sideMinV, light);
			
			matrices.pop();
		}
	}
	
	private void vertex(VertexConsumer cons, MatrixStack ms, int colour, float x, float y, float z, float u, float v, int light){
		cons.vertex(ms.peek().getPositionMatrix(), x, y, z)
				.color(colour)
				.texture(u, v)
				.light(light)
				.normal(1, 0, 0)
				.next();
	}
}