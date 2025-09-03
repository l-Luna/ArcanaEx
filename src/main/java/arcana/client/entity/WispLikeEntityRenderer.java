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

import static arcana.Arcana.arcId;

public class WispLikeEntityRenderer<T extends WispLikeEntity> extends EntityRenderer<T>{
	
	private static final Identifier texture = arcId("textures/entity/wisp.png");
	private static final RenderLayer layer = RenderLayer.getEntityTranslucent(texture);
	private static final float ringTime = 21;
	
	private final boolean style;
	
	public WispLikeEntityRenderer(EntityRendererFactory.Context ctx, boolean style){
		super(ctx);
		this.style = style;
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
		
		for(int i = 0; i < 3; i++){
			float localTime = ringTime - (entityTime + i * (ringTime / 3f)) % ringTime;
			float radHere = localTime / ringTime + 1,
			      alphaHere = MathHelper.sin(localTime * MathHelper.PI / ringTime);
			matrices.push();
			matrices.scale(radHere, radHere, radHere);
			quad(vc, matrices, light, -14, -14, 0, 0, 28, 28, alphaHere);
			matrices.pop();
		}
		
		matrices.push();
		matrices.multiply(Vec3f.POSITIVE_Z.getDegreesQuaternion((entityTime * 4) % 90));
		matrices.translate(0, 0, -0.001);
		matrices.scale(1.2f, 1.2f, 1);
		quad(vc, matrices, light, -5, -5, 61, 12, 10, 10, 0.3f);
		matrices.pop();
		
		matrices.push();
		float innerScale = 0.2f * MathHelper.sin(entityTime / 55f) + 0.9f;
		matrices.scale(innerScale, innerScale, 1);
		quad(vc, matrices, light, -4, -4, 61, 0, 8, 8, 1);
		matrices.pop();
		
		matrices.pop();
		super.render(entity, yaw, tickDelta, matrices, vertexConsumers, light);
	}
	
	private static void quad(VertexConsumer vc,
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
	
	private static void vertex(VertexConsumer vc,
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
				.normal(norMat, 1, 0, 0)
				.next();
	}
}