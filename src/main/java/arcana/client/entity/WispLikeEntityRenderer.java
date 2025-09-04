package arcana.client.entity;

import arcana.entities.WispLikeEntity;
import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.EntityRenderer;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Matrix3f;
import net.minecraft.util.math.Matrix4f;
import net.minecraft.util.math.Vec3f;
import net.minecraft.util.math.random.Random;

import static arcana.Arcana.arcId;

public class WispLikeEntityRenderer<T extends WispLikeEntity> extends EntityRenderer<T>{
	
	private static final Identifier texture = arcId("textures/entity/wisp.png");
	private static final RenderLayer layer = RenderLayer.getEntityTranslucentEmissive(texture);
	
	private final int rings;
	private final float ringTime;
	private final boolean ringDir;
	private final float ringRad;
	
	public WispLikeEntityRenderer(EntityRendererFactory.Context ctx, int rings, float ringTime, boolean ringDir, float ringRad){
		super(ctx);
		this.rings = rings;
		this.ringTime = ringTime;
		this.ringDir = ringDir;
		this.ringRad = ringRad;
	}
	
	public Identifier getTexture(WispLikeEntity entity){
		return texture;
	}
	
	public void render(T entity, float yaw, float tickDelta, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light){
		float entityTime = entity.age + tickDelta;
		
		matrices.push();
		matrices.translate(0, 0.75f, 0);
		matrices.scale(0.1f, 0.1f, 0.1f);
		matrices.multiply(dispatcher.getRotation());
		matrices.multiply(Vec3f.POSITIVE_Y.getDegreesQuaternion(180));
		VertexConsumer vc = vertexConsumers.getBuffer(layer);
		float deathLerp = entity.deathTime > 0 ? 1 - (entity.deathTime + tickDelta) / 20f : 1;
		
		for(int i = 0; i < rings; i++){
			float localTime = (entityTime + i * (ringTime / rings)) % ringTime;
			if(ringDir)
				localTime = ringTime - localTime;
			float radHere = (ringRad * localTime / ringTime) + ringRad + 5*(1-deathLerp),
			      alphaHere = MathHelper.sin(localTime * MathHelper.PI / ringTime) * deathLerp;
			matrices.push();
			matrices.scale(radHere, radHere, radHere);
			quad(vc, matrices, light, -14, -14, 0, 0, 28, 28, alphaHere);
			matrices.pop();
		}
		
		matrices.push();
		if(entity.hurtTime > 0){
			Random rng = entity.world.random;
			float scale = Math.min(entity.hurtTime, 10) / 10f;
			matrices.translate(scale * rng.nextBetween(-10, 10) / 10f, scale * rng.nextBetween(-10, 10) / 10f, scale * rng.nextBetween(-10, 10) / 10f);
		}
		
		matrices.push();
		matrices.multiply(Vec3f.POSITIVE_Z.getDegreesQuaternion((entityTime * 4) % 90));
		matrices.translate(0, 0, -0.001);
		matrices.scale(1.2f, 1.2f, 1);
		quad(vc, matrices, light, -5, -5, 61, 12, 10, 10, 0.3f * deathLerp);
		matrices.pop();
		
		matrices.push();
		float innerScale = 0.2f * MathHelper.sin(entityTime / 55f) + 0.9f;
		matrices.scale(innerScale, innerScale, 1);
		quad(vc, matrices, light, -4, -4, 61, 0, 8, 8, 1 * deathLerp);
		matrices.pop();
		
		matrices.pop();
		
		matrices.pop();
		super.render(entity, yaw, tickDelta, matrices, vertexConsumers, light);
	}
	
	private void quad(VertexConsumer vc,
	                         MatrixStack matrices,
	                         int light,
	                         float x,
	                         float y,
	                         int texU,
	                         int texV,
	                         int width,
	                         int height,
	                         float alpha){
		MatrixStack.Entry entry = matrices.peek();
		Matrix4f posMat = entry.getPositionMatrix();
		Matrix3f norMat = entry.getNormalMatrix();
		
		vertex(vc, posMat, norMat, light, x, y, texU / 128f, texV / 128f, alpha);
		vertex(vc, posMat, norMat, light, x + width, y, (texU + width) / 128f, texV / 128f, alpha);
		vertex(vc, posMat, norMat, light, x + width, y + height, (texU + width) / 128f, (texV + height) / 128f, alpha);
		vertex(vc, posMat, norMat, light, x, y + height, texU / 128f, (texV + height) / 128f, alpha);
	}
	
	private void vertex(VertexConsumer vc,
	                           Matrix4f posMat,
	                           Matrix3f norMat,
	                           int light,
	                           float x,
	                           float y,
	                           float texU,
	                           float texV,
	                           float alpha){
		vc.vertex(posMat, x, y, 0)
				.color(1, 1, 1, alpha)
				.texture(texU, texV)
				.overlay(OverlayTexture.DEFAULT_UV)
				.light(light)
				.normal(0, 1, 0)
				.next();
	}
}