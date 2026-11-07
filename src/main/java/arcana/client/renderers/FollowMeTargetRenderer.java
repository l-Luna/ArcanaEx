package arcana.client.renderers;

import arcana.client.RenderHelper;
import arcana.items.components.ArcanaDataComponents;
import com.mojang.blaze3d.systems.RenderSystem;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.render.*;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import org.joml.Vector3f;

import static arcana.Arcana.arcId;

public class FollowMeTargetRenderer{
	
	public static final Identifier TARGET_TEXTURE = arcId("textures/misc/target.png");
	
	public static void render(WorldRenderContext context){
		MinecraftClient client = MinecraftClient.getInstance();
		ClientPlayerEntity player = client.player;
		if(player == null)
			return;
		BlockPos target = player.getAttached(ArcanaDataComponents.FOLLOW_ME_TARGET);
		if(target == null)
			return;
		
		Vec3d cameraPos = context.camera().getPos();
		Vec3d markerPos;
		if(target.toCenterPos().isInRange(cameraPos, 20))
			markerPos = target.toCenterPos();
		else{
			Vec3d direction = target.toCenterPos().subtract(cameraPos).normalize();
			markerPos = cameraPos.add(direction.multiply(20));
		}
		Tessellator tessellator = Tessellator.getInstance();
		context.lightmapTextureManager().enable();
		RenderSystem.setShader(GameRenderer::getParticleProgram);
		RenderSystem.setShaderTexture(0, TARGET_TEXTURE);
		RenderSystem.setShaderColor(1, 1, 1, 1);
		RenderSystem.disableDepthTest();
		BufferBuilder buffer = tessellator.begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_TEXTURE_COLOR_LIGHT);
		NodeRenderer.drawQuad(context.camera(), markerPos, new Vector3f(), buffer, 1, 1, 0, 1, 1, LightmapTextureManager.MAX_LIGHT_COORDINATE);
		RenderHelper.drawBuffer(buffer);
		RenderSystem.enableDepthTest();
	}
}