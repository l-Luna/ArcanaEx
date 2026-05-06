package arcana.client.renderers;

import arcana.duck.ProjectedBlockHitResult;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.*;
import org.jetbrains.annotations.NotNull;

public final class PlaneProjectionRenderer{
	
	public static void renderPlaneProjection(WorldRenderContext ctx, HitResult hit){
		if(!(hit instanceof ProjectedBlockHitResult pbhr) || !(ctx.camera().getFocusedEntity() instanceof PlayerEntity pe && !pe.isSpectator()))
			return;
		
		VertexConsumer vc = ctx.consumers().getBuffer(RenderLayer.getLines());
		Vec3d cam = ctx.camera().getPos();
		Matrix4f posMat = ctx.matrixStack().peek().getPositionMatrix();
		Matrix3f normMat = ctx.matrixStack().peek().getNormalMatrix();
		
		Direction side = pbhr.getSide();
		Vec3d bad = side.getDirection() == Direction.AxisDirection.NEGATIVE ? Vec3d.ZERO : new Vec3d(side.getUnitVector());
		Vec3d base = Vec3d.of(pbhr.getBlockPos().offset(side.getOpposite())).add(bad).subtract(cam);
		renderEdge(new Box(base.add(cornerOffset(0, 0, side)), base.add(cornerOffset(0, 1, side))), posMat, normMat, vc);
		renderEdge(new Box(base.add(cornerOffset(1, 0, side)), base.add(cornerOffset(1, 1, side))), posMat, normMat, vc);
		renderEdge(new Box(base.add(cornerOffset(0, 0, side)), base.add(cornerOffset(1, 0, side))), posMat, normMat, vc);
		renderEdge(new Box(base.add(cornerOffset(0, 1, side)), base.add(cornerOffset(1, 1, side))), posMat, normMat, vc);
		
		renderEdge(new Box(base.add(cornerOffset(0, 0, side)), base.add(cornerOffset(1, 1, side))), posMat, normMat, vc);
		renderEdge(new Box(base.add(cornerOffset(0, 0.4f, side)), base.add(cornerOffset(0.6f, 1, side))), posMat, normMat, vc);
		renderEdge(new Box(base.add(cornerOffset(0.4f, 0, side)), base.add(cornerOffset(1, 0.6f, side))), posMat, normMat, vc);
	}
	
	private static @NotNull Vec3d cornerOffset(float u, float v, Direction side){
		int notX = 1 - Math.abs(side.getOffsetX());
		int notY = 1 - Math.abs(side.getOffsetY());
		int notZ = 1 - Math.abs(side.getOffsetZ());
		return new Vec3d(
				notX * (u * notZ + v * notY),
				notY * (u * notX + v * notZ),
				notZ * (u * notY + v * notX)
		);
	}
	
	private static void renderEdge(Box along, Matrix4f posMat, Matrix3f normMat, VertexConsumer vc){
		double xx = along.getXLength();
		double yy = along.getYLength();
		double zz = along.getZLength();
		double n = Math.sqrt(xx * xx + yy * yy + zz * zz);
		xx /= n;
		yy /= n;
		zz /= n;
		vc.vertex(posMat, (float)(along.minX), (float)(along.minY), (float)(along.minZ))
				.color(0, 0, 0, 0.4f)
				.normal(normMat, (float)xx, (float)yy, (float)zz)
				.next();
		vc.vertex(posMat, (float)(along.maxX), (float)(along.maxY), (float)(along.maxZ))
				.color(0, 0, 0, 0.4f)
				.normal(normMat, (float)xx, (float)yy, (float)zz)
				.next();
	}
}