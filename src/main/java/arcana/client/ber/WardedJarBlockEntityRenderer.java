package arcana.client.ber;

import arcana.aspects.AspectMap;
import arcana.aspects.AspectStack;
import arcana.blocks.be.WardedJarBlockEntity;
import arcana.client.AspectRenderHelper;
import arcana.items.GogglesOfRevealingItem;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.block.entity.BlockEntityRenderer;
import net.minecraft.client.texture.Sprite;
import net.minecraft.client.texture.SpriteAtlasTexture;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;
import org.joml.Vector3f;

import java.util.function.Function;

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
			// render liquid contents
			Function<Identifier, Sprite> sprites = MinecraftClient.getInstance().getSpriteAtlas(SpriteAtlasTexture.BLOCK_ATLAS_TEXTURE);
			Sprite top = sprites.apply(topTexture);
			Sprite side = sprites.apply(sideTexture);
			Sprite bottom = sprites.apply(bottomTexture);
			
			VertexConsumer cons = vcs.getBuffer(RenderLayer.getSolid());
			int colour = stack.type().colour() + 0xFF000000;
			float height = stack.amount() / 100f;
			
			matrices.push();
			matrices.translate(.25, .075, .25);
			matrices.scale(.5f, .6f * height, .5f);
			
			// top
			vertex(cons, matrices, colour, 0, 1, 1, top, 0, 0.75f, light);
			vertex(cons, matrices, colour, 1, 1, 1, top, 0.75f, 0.75f, light);
			vertex(cons, matrices, colour, 1, 1, 0, top, 0.75f, 0, light);
			vertex(cons, matrices, colour, 0, 1, 0, top, 0, 0, light);
			
			// bottom
			vertex(cons, matrices, colour, 0, 0, 0, bottom, 0, 0, light);
			vertex(cons, matrices, colour, 1, 0, 0, bottom, 0.75f, 0, light);
			vertex(cons, matrices, colour, 1, 0, 1, bottom, 0.75f, 0.75f, light);
			vertex(cons, matrices, colour, 0, 0, 1, bottom, 0, 0.75f, light);
			
			float heightU = 1-height;
			
			// east (+X) face
			vertex(cons, matrices, colour, 1, 1, 0, side, 0, heightU, light);
			vertex(cons, matrices, colour, 1, 1, 1, side, 0.75f, heightU, light);
			vertex(cons, matrices, colour, 1, 0, 1, side, 0.75f, 1, light);
			vertex(cons, matrices, colour, 1, 0, 0, side, 0, 1, light);
			
			// west (-X) face
			vertex(cons, matrices, colour, 0, 1, 0, side, 0, heightU, light);
			vertex(cons, matrices, colour, 0, 0, 0, side, 0, 1, light);
			vertex(cons, matrices, colour, 0, 0, 1, side, 0.75f, 1, light);
			vertex(cons, matrices, colour, 0, 1, 1, side, 0.75f, heightU, light);
			
			// north (-Z) face
			vertex(cons, matrices, colour, 1, 0, 0, side, 0.75f, 1, light);
			vertex(cons, matrices, colour, 0, 0, 0, side, 0, 1, light);
			vertex(cons, matrices, colour, 0, 1, 0, side, 0, heightU, light);
			vertex(cons, matrices, colour, 1, 1, 0, side, 0.75f, heightU, light);
			
			// south (+Z) face
			vertex(cons, matrices, colour, 0, 0, 1, side, 0, 1, light);
			vertex(cons, matrices, colour, 1, 0, 1, side, 0.75f, 1, light);
			vertex(cons, matrices, colour, 1, 1, 1, side, 0.75f, heightU, light);
			vertex(cons, matrices, colour, 0, 1, 1, side, 0, heightU, light);
			
			matrices.pop();
			
			// render pop-out aspect
			ClientPlayerEntity player = MinecraftClient.getInstance().player;
			if(!GogglesOfRevealingItem.hasRevealing(player))
				return;
			AspectRenderHelper.renderAspectsInWorld(matrices, player, AspectMap.fromAspectStack(stack), entity.getPos(), new Vector3f(0, 0.7f, -0.6f));
		}
	}
	
	private void vertex(VertexConsumer cons, MatrixStack ms, int colour, float x, float y, float z, Sprite sprite, float uFrac, float vFrac, int light){
		cons.vertex(ms.peek().getPositionMatrix(), x, y, z)
				.color(colour)
				.texture(MathHelper.lerp(uFrac, sprite.getMinU(), sprite.getMaxU()), MathHelper.lerp(vFrac, sprite.getMinV(), sprite.getMaxV()))
				.light(light)
				.normal(1, 0, 0);
	}
}