package arcana.client.ber;

import arcana.aspects.AspectMap;
import arcana.aspects.AspectStack;
import arcana.aspects.Aspects;
import arcana.blocks.be.MysticMistBlockEntity;
import arcana.client.ArcanaClient;
import arcana.client.AspectRenderHelper;
import arcana.client.RenderHelper;
import arcana.items.GogglesOfRevealingItem;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.*;
import net.minecraft.client.render.block.entity.BlockEntityRenderer;
import net.minecraft.client.texture.Sprite;
import net.minecraft.client.texture.SpriteAtlasTexture;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RotationAxis;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.noise.PerlinNoiseSampler;
import net.minecraft.util.math.random.LocalRandom;
import org.joml.Vector3f;

public class MysticMistBlockEntityRenderer implements BlockEntityRenderer<MysticMistBlockEntity>{
	
	public static final Identifier RAIN = Identifier.of("environment/rain");
	public static final Identifier SNOW = Identifier.of("environment/snow");
	
	public void render(MysticMistBlockEntity entity,
	                   float tickDelta,
	                   MatrixStack matrices,
	                   VertexConsumerProvider vcp,
	                   int light,
	                   int overlay){
		AspectStack stack = entity.stored;
		if(stack == null)
			return;
		
		var player = MinecraftClient.getInstance().player;
		if(GogglesOfRevealingItem.hasRevealing(player))
			AspectRenderHelper.renderAspectsInWorld(matrices, player, AspectMap.fromAspectStack(stack), entity.getPos(), new Vector3f(0, 1.8f, 0));
		
		// heat effect
		if(stack.type().equals(Aspects.FIRE)){
			// TODO
			return;
		}
		// aurora effect
		if(stack.type().equals(Aspects.AURA)){
			// TODO
			return;
		}
		
		// water, energy, ice
		boolean isRain = stack.type().equals(Aspects.WATER);
		boolean isThunder = stack.type().equals(Aspects.ENERGY);
		
		final int lim = MysticMistBlockEntity.radius * 2;
		
		matrices.push();
		matrices.translate(-lim/2f, entity.vspace - 1.5, -lim/2f);
		
		double time = entity.getWorld().getTime() + tickDelta;
		var diff = -(long)(time / 64);
		float offset = (float)((time / 64) % 1);
		LocalRandom rng = new LocalRandom(entity.hashCode());
		PerlinNoiseSampler p = new PerlinNoiseSampler(rng);
		
		var atlas = MinecraftClient.getInstance().getSpriteAtlas(SpriteAtlasTexture.BLOCK_ATLAS_TEXTURE);
		RenderSystem.enableBlend();
		RenderSystem.defaultBlendFunc();
		RenderSystem.enableDepthTest();
		RenderSystem.setShader(GameRenderer::getPositionColorTexLightmapProgram);
		RenderSystem.setShaderColor(1, 1, 1, 1);
		if(isThunder)
			RenderSystem.setShaderColor(0.8f, 0.8f, 0.8f, 1);
		RenderSystem.setShaderTexture(0, SpriteAtlasTexture.BLOCK_ATLAS_TEXTURE);
		BufferBuilder vc = Tessellator.getInstance().begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_COLOR_TEXTURE_LIGHT_NORMAL);
		
		Sprite whiteSprite = atlas.apply(ArcanaClient.WHITE_TEX);
		for(int x = 0; x < lim; x++)
			for(int z = 0; z < lim; z++){
				if(p.sample((x + diff) * .3, 0, (z + diff) * .3) >= 0.13){
					float opacity = 1;
					if(x == 0 || z == 0)
						opacity = offset;
					else if(x == lim - 1 || z == lim - 1)
						opacity = 1 - offset;
					
					// clouds
					RenderHelper.colCuboid(vc, matrices, ((int)(opacity * 255) << 24) + 0x00EEEEFE, new Vec3d(x + offset, 1.2f, z + offset), 1, 0.35f, 1, whiteSprite, false);
				}
			}
		
		RenderSystem.setShaderColor(1, 1, 1, 1);
		Sprite fallSprite = atlas.apply(isRain || isThunder ? RAIN : SNOW);
		
		float texMinU = fallSprite.getMinU();
		float texMaxU = MathHelper.lerp(1, fallSprite.getMinU(), fallSprite.getMaxU());
		float texMinV = fallSprite.getMinV();
		float texMaxV = MathHelper.lerp(1, fallSprite.getMinV(), fallSprite.getMaxV());
		
		final float sqrt2 = MathHelper.SQUARE_ROOT_OF_TWO;
		final float fallrate = isRain ? 32 : isThunder ? 16 : 256;
		
		for(int x = 0; x < lim; x++)
			for(int z = 0; z < lim; z++){
				if(p.sample((x + diff) * .3, 0, (z + diff) * .3) >= 0.13){
					// need two parts to animate it on an atlas
					for(int part = 0; part < 2; part++){
						float lOffset = (float)(((time / fallrate) % 1) + Math.abs(p.sample((x + diff), 0, (z + diff)))) % 1;
						var itv = MathHelper.lerp(1 - lOffset, texMinV, texMaxV);
						float startY = (part == 0) ? 0 : lOffset,
						      endY   = (part == 0) ? lOffset : 1;
						float minV = (part == 0) ? itv : texMinV,
						      maxV = (part == 0) ? texMaxV : itv;
						// X-shape
						for(int direction = 0; direction < 2; direction++){
							matrices.push();
							matrices.translate(x + offset, 1.2f, z + offset);
							matrices.translate(1, 0, 0);
							if(direction == 1)
								matrices.translate(0, 0, 1);
							matrices.scale(4 * sqrt2, 4 * sqrt2, 4 * sqrt2);
							matrices.multiply(RotationAxis.POSITIVE_Y.rotation(MathHelper.HALF_PI / 2f));
							matrices.multiply(RotationAxis.POSITIVE_Z.rotation(3 * MathHelper.HALF_PI));
							
							if(direction == 1)
								matrices.multiply(RotationAxis.POSITIVE_X.rotation(MathHelper.HALF_PI));
							
							// and rain
							var mat = matrices.peek().getPositionMatrix();
							
							// forward
							vc.vertex(mat, endY, 0, 0)
									.color(0xFFFFFFFF)
									.texture(texMinU, maxV)
									.light(LightmapTextureManager.MAX_LIGHT_COORDINATE)
									.normal(1, 0, 0);
							vc.vertex(mat, endY, -1 / 4f, 0)
									.color(0xFFFFFFFF)
									.texture(texMaxU, maxV)
									.light(LightmapTextureManager.MAX_LIGHT_COORDINATE)
									.normal(1, 0, 0);
							vc.vertex(mat, startY, -1 / 4f, 0)
									.color(0xFFFFFFFF)
									.texture(texMaxU, minV)
									.light(LightmapTextureManager.MAX_LIGHT_COORDINATE)
									.normal(1, 0, 0);
							vc.vertex(mat, startY, 0, 0)
									.color(0xFFFFFFFF)
									.texture(texMinU, minV)
									.light(LightmapTextureManager.MAX_LIGHT_COORDINATE)
									.normal(1, 0, 0);
							
							// and back
							vc.vertex(mat, startY, 0, 0)
									.color(0xFFFFFFFF)
									.texture(texMinU, minV)
									.light(LightmapTextureManager.MAX_LIGHT_COORDINATE)
									.normal(1, 0, 0);
							vc.vertex(mat, startY, -1 / 4f, 0)
									.color(0xFFFFFFFF)
									.texture(texMaxU, minV)
									.light(LightmapTextureManager.MAX_LIGHT_COORDINATE)
									.normal(1, 0, 0);
							vc.vertex(mat, endY, -1 / 4f, 0)
									.color(0xFFFFFFFF)
									.texture(texMaxU, maxV)
									.light(LightmapTextureManager.MAX_LIGHT_COORDINATE)
									.normal(1, 0, 0);
							vc.vertex(mat, endY, 0, 0)
									.color(0xFFFFFFFF)
									.texture(texMinU, maxV)
									.light(LightmapTextureManager.MAX_LIGHT_COORDINATE)
									.normal(1, 0, 0);
							
							matrices.pop();
						}
					}
				}
			}
		matrices.pop();
		 RenderHelper.drawBuffer(vc);
		RenderSystem.disableBlend();
	}
	
	public boolean rendersOutsideBoundingBox(MysticMistBlockEntity blockEntity){
		return true;
	}
}