package arcana.client.renderers;

import arcana.client.ArcanaClient;
import arcana.client.RenderHelper;
import arcana.items.components.ArcanaDataComponents;
import arcana.items.components.FollowMeTargetComponent;
import com.mojang.blaze3d.systems.RenderSystem;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.render.*;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.ChunkSectionPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.Heightmap;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.ChunkStatus;
import org.joml.Vector3f;

import java.lang.ref.WeakReference;

import static arcana.Arcana.arcId;

public class FollowMeTargetRenderer{
	
	public static final Identifier TARGET_TEXTURE = arcId("textures/misc/target.png"),
									BREADCRUMB_TEXTURE = arcId("textures/misc/breadcrumb.png");
	
	private static final int BREADCRUMB_DOTS = 5;
	private static final float BREADCRUMB_SPACING = 3;
	
	// temporary animation data
	private static WeakReference<FollowMeTargetComponent> lastTarget = null;
	private static double terrainSnappedY;
	
	public static void render(WorldRenderContext context){
		MinecraftClient client = MinecraftClient.getInstance();
		ClientPlayerEntity player = client.player;
		World world = context.world();
		if(player == null)
			return;
		FollowMeTargetComponent target = player.getAttached(ArcanaDataComponents.FOLLOW_ME_TARGET);
		if(target == null)
			return;
		if(!world.getRegistryKey().getValue().equals(target.world()))
			return;
		
		// reset temp data when a new target is chosen
		if(lastTarget == null || lastTarget.get() != target){
			lastTarget = new WeakReference<>(target);
			terrainSnappedY = -1;
		}
		
		float delta = context.tickCounter().getTickDelta(true);
		float timeAlive = (float)Math.min(context.world().getTime() - target.timeCreated(), 20 * 20) + delta;
		float animLerp = Math.min(timeAlive, 40) / 40f;
		animLerp *= animLerp * animLerp;
		
		Vec3d cameraPos = context.camera().getPos();
		Vec3d endPos = target.end();
		// snap to heightmap when available
		if(target.snapToHeightmap().isPresent()){
			// use sea level if heightmap isn't present, still inaccurate but better than underground
			double surfaceY = world.getSeaLevel();
			if(terrainSnappedY != -1)
				surfaceY = terrainSnappedY;
			else{
				// explicitly check for full chunks, since the chunk is already "partially generated" but missing actual terrain
				Chunk chunk = world.getChunk(ChunkSectionPos.getSectionCoord((int)endPos.x), ChunkSectionPos.getSectionCoord((int)endPos.z), ChunkStatus.FULL, false);
				if(chunk != null){
					// only use client heightmaps
					Heightmap.Type heightmap = switch(target.snapToHeightmap().get()){
						case WORLD_SURFACE_WG -> Heightmap.Type.WORLD_SURFACE;
						default -> Heightmap.Type.MOTION_BLOCKING;
					};
					terrainSnappedY = surfaceY = chunk.sampleHeightmap(heightmap, (int)endPos.x, (int)endPos.y);
				}
			}
			endPos = new Vec3d(endPos.x, surfaceY, endPos.z);
		}
		Vec3d markerPos = target.start().lerp(endPos, animLerp);
		
		// keep the marker within 30 blocks for rendering, but use the full distance for other calculations
		Vec3d realMarkerPos = markerPos;
		if(!markerPos.isInRange(cameraPos, 30))
			markerPos = cameraPos.add(markerPos.subtract(cameraPos).normalize().multiply(30));
		Tessellator tessellator = Tessellator.getInstance();
		context.lightmapTextureManager().enable();
		RenderSystem.enableBlend();
		RenderSystem.setShader(GameRenderer::getParticleProgram);
		RenderSystem.setShaderColor(1, 1, 1, 1);
		RenderSystem.disableDepthTest();
		
		// breadcrumbs, closest N points along the line
		RenderSystem.setShaderTexture(0, BREADCRUMB_TEXTURE);
		BufferBuilder buffer = tessellator.begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_TEXTURE_COLOR_LIGHT);
		
		Vec3d line = realMarkerPos.subtract(target.start());
		float lineLength = (float)line.length();
		Vec3d direction = line.normalize();
		Vec3d relativePos = cameraPos.subtract(target.start());
		float lengthAlong = (float)direction.dotProduct(relativePos);
		float rndLengthAlong = Math.round(lengthAlong / BREADCRUMB_SPACING) * BREADCRUMB_SPACING;
		for(int i = -BREADCRUMB_DOTS; i <= BREADCRUMB_DOTS; i++){
			float pointAlong = rndLengthAlong + i * BREADCRUMB_SPACING;
			if(pointAlong < 0 || pointAlong + 1 > lineLength)
				continue;
			Vec3d along = target.start().add(direction.multiply(pointAlong));
			float alpha = 1 - Math.abs(lengthAlong - pointAlong) / (BREADCRUMB_DOTS * BREADCRUMB_SPACING);
			NodeRenderer.drawQuad(context.camera(), along, new Vector3f(-0.5f, -0.5f, -0.5f), buffer, alpha * 0.8f, 1, 0, 1, 1, LightmapTextureManager.MAX_LIGHT_COORDINATE);
		}
		
		RenderHelper.drawBuffer(buffer);
		
		// end marker
		RenderSystem.setShaderTexture(0, TARGET_TEXTURE);
		buffer = tessellator.begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_TEXTURE_COLOR_LIGHT);
		for(int i = 0; i < 3; i++)
			NodeRenderer.drawQuad(context.camera(), markerPos, new Vector3f(-0.5f + ArcanaClient.osc2(3, i / 3f) * 0.06f, -0.5f + ArcanaClient.osc2(3, 0.25f + i / 3f) * 0.06f, -0.5f), buffer, 0.45f / (i + 1) + 0.2f, 1, 0, 1, 1, LightmapTextureManager.MAX_LIGHT_COORDINATE);
		RenderHelper.drawBuffer(buffer);
		RenderSystem.enableDepthTest();
	}
}