package arcana.client;

import arcana.components.AuraWorld;
import arcana.nodes.Node;
import arcana.nodes.NodeType;
import arcana.nodes.NodeTypes;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.*;
import net.minecraft.util.Identifier;
import net.minecraft.util.JsonHelper;
import net.minecraft.util.math.Quaternion;
import net.minecraft.util.math.Vec3f;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public final class NodeRenderer{
	
	private static final Map<NodeType, Integer> framesByType = new HashMap<>(NodeTypes.NODE_TYPES.size());
	
	@SuppressWarnings("resource") // ???
	public static void renderAll(WorldRenderContext context){
		context.lightmapTextureManager().enable();
		RenderSystem.disableCull();
		RenderSystem.enableBlend();
		RenderSystem.blendFunc(GlStateManager.SrcFactor.SRC_ALPHA, GlStateManager.DstFactor.ONE_MINUS_SRC_ALPHA);
		
		RenderSystem.setShader(GameRenderer::getParticleShader);
		RenderSystem.setShaderColor(1, 1, 1, 1);
		RenderSystem.depthMask(true);
		
		Tessellator tessellator = Tessellator.getInstance();
		BufferBuilder buffer = tessellator.getBuffer();
		
		Camera camera = context.camera();
		
		var nodesByType = context.world()
				.getComponent(AuraWorld.KEY)
				.getNodes()
				.stream()
				.collect(Collectors.groupingBy(Node::getType));
		
		// first pass, visible through blocks
		RenderSystem.disableDepthTest();
		nodesByType.forEach((type, nodes) -> {
			RenderSystem.setShaderTexture(0, loadTexture(type));
			buffer.begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_TEXTURE_COLOR_LIGHT);
			for(Node node : nodes)
				drawNode(camera, node, buffer, 0.12f);
			BufferRenderer.drawWithShader(buffer.end());
		});
		
		// second pass, not obscured
		RenderSystem.enableDepthTest();
		nodesByType.forEach((type, nodes) -> {
			RenderSystem.setShaderTexture(0, loadTexture(type));
			buffer.begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_TEXTURE_COLOR_LIGHT);
			for(Node node : nodes)
				drawNode(camera, node, buffer, 0.85f);
			BufferRenderer.drawWithShader(buffer.end());
		});
		
		RenderSystem.depthMask(true);
		context.lightmapTextureManager().disable();
	}
	
	private static void drawNode(Camera camera, Node n, VertexConsumer cons, float alpha){
		// based on BillboardParticle
		Vec3f[] corners = { new Vec3f(-1, -1, 0), new Vec3f(-1, 1, 0), new Vec3f(1, 1, 0), new Vec3f(1, -1, 0) };
		Quaternion rot = camera.getRotation();
		for(Vec3f corner : corners){
			corner.scale(scale(n));
			corner.rotate(rot);
			corner.add((float)(n.getX() - camera.getPos().x),
			           (float)(n.getY() - camera.getPos().y),
			           (float)(n.getZ() - camera.getPos().z));
		}
		
		cons.vertex(corners[0].getX(), corners[0].getY(), corners[0].getZ())
				.texture(0, v(n, true))
				.color(1, 1, 1, alpha)
				.light(light(n))
				.next();
		cons.vertex(corners[1].getX(), corners[1].getY(), corners[1].getZ())
				.texture(0, v(n, false))
				.color(1, 1, 1, alpha)
				.light(light(n))
				.next();
		cons.vertex(corners[2].getX(), corners[2].getY(), corners[2].getZ())
				.texture(1, v(n, false))
				.color(1, 1, 1, alpha)
				.light(light(n))
				.next();
		cons.vertex(corners[3].getX(), corners[3].getY(), corners[3].getZ())
				.texture(1, v(n, true))
				.color(1, 1, 1, alpha)
				.light(light(n))
				.next();
	}
	
	private static float scale(Node n){
		return 1;
	}
	
	private static float v(Node n, boolean max){
		float f = maxFrames(n.getType());
		return (1 / f) * ((n.getWorld().getTime() / 2 + n.getUuid().hashCode()) % (int)(f) + (max ? 1 : 0));
	}
	
	private static int light(Node n){
		return WorldRenderer.getLightmapCoordinates(n.getWorld(), n.asBlockPos());
	}
	
	private static Identifier loadTexture(NodeType nt){
		if(!framesByType.containsKey(nt))
			loadMeta(nt);
		return getNodeResourceId(nt, ".png");
	}
	
	private static void loadMeta(NodeType nt){
		// this is incredibly stupid
		try{
			try(InputStream stream = MinecraftClient.getInstance().getResourceManager().getResource(getNodeResourceId(nt, ".png.mcmeta")).get().getInputStream()){
				var metaObj = JsonHelper.deserialize(new BufferedReader(new InputStreamReader(stream)));
				framesByType.put(nt, JsonHelper.getArray(JsonHelper.getObject(metaObj, "animation"), "frames").size());
			}
		}catch(IOException e){
			throw new RuntimeException(e);
		}
	}
	
	private static Identifier getNodeResourceId(NodeType nt, String ext){
		return new Identifier(nt.id().getNamespace(), "textures/nodes/" + nt.id().getPath() + ext);
	}
	
	private static int maxFrames(NodeType nt){
		return framesByType.get(nt);
	}
}