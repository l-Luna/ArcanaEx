package arcana.client;

import arcana.aspects.Aspect;
import arcana.aspects.Aspects;
import arcana.components.AuraWorld;
import arcana.items.GogglesOfRevealingItem;
import arcana.nodes.Node;
import arcana.nodes.NodeType;
import arcana.nodes.NodeTypes;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.*;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.util.Identifier;
import net.minecraft.util.JsonHelper;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Position;
import net.minecraft.util.math.Quaternion;
import net.minecraft.util.math.Vec3f;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;
import java.util.stream.Collectors;

public final class NodeRenderer{
	
	private static final Map<NodeType, Integer> framesByType = new HashMap<>(NodeTypes.NODE_TYPES.size());
	
	private static final Map<Node, Float> lerpView = new WeakHashMap<>();
	
	@SuppressWarnings("resource") // ???
	public static void renderAll(WorldRenderContext context){
		var player = MinecraftClient.getInstance().player;
		boolean hasGoggles = player == null || player.getEquippedStack(EquipmentSlot.HEAD).getItem() instanceof GogglesOfRevealingItem;
		
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
		
		var auraWorld = context.world().getComponent(AuraWorld.KEY);
		List<Node> allNodes = auraWorld.getNodes();
		
		var nodesByType = allNodes
				.stream()
				.collect(Collectors.groupingBy(Node::getType));
		
		// first pass, visible through blocks if you have goggles of revealing
		if(hasGoggles)
			RenderSystem.disableDepthTest();
		nodesByType.forEach((type, nodes) -> {
			RenderSystem.setShaderTexture(0, loadTexture(type));
			buffer.begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_TEXTURE_COLOR_LIGHT);
			for(Node node : nodes)
				if(shouldView(node))
					drawNode(camera, node, buffer, .12f);
			BufferRenderer.drawWithShader(buffer.end());
		});
		
		// second pass, hidden by blocks, requires goggles
		RenderSystem.enableDepthTest();
		if(hasGoggles){
			nodesByType.forEach((type, nodes) -> {
				RenderSystem.setShaderTexture(0, loadTexture(type));
				buffer.begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_TEXTURE_COLOR_LIGHT);
				for(Node node : nodes)
					if(shouldView(node))
						drawNode(camera, node, buffer, .85f);
				BufferRenderer.drawWithShader(buffer.end());
			});
			
			// only render aspects for the one you look at
			var looking = auraWorld.raycast(player.getEyePos(), 6.5, false, player).orElse(null);
			for(Node node : allNodes)
				if(shouldView(node))
					if(node == looking)
						lerpView.put(node, MathHelper.lerp(context.tickDelta() / 5f, lerpView.computeIfAbsent(node, __ -> 0f), 1));
					else
						lerpView.put(node, MathHelper.lerp(context.tickDelta() / 5f, lerpView.computeIfAbsent(node, __ -> 1f), 0));
				else
					lerpView.put(node, 0f);
			
			Aspects.primals.forEach(primal -> {
				RenderSystem.setShaderTexture(0, AspectRenderer.texture(primal));
				buffer.begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_TEXTURE_COLOR_LIGHT);
				for(Node node : allNodes)
					if(shouldView(node))
						drawNodeAspect(camera, node, buffer, primal);
				BufferRenderer.drawWithShader(buffer.end());
			});
			
			for(Node node : allNodes){
				if(shouldView(node)){
					// can't batch non-primals, so avoid these if we can
					for(Aspect aspect : node.getAspects().aspectSet())
						if(!Aspects.primals.contains(aspect)){
							RenderSystem.setShaderTexture(0, AspectRenderer.texture(aspect));
							buffer.begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_TEXTURE_COLOR_LIGHT);
							drawNodeAspect(camera, node, buffer, aspect);
							BufferRenderer.drawWithShader(buffer.end());
						}
				}
			}
			
			for(Node node : allNodes)
				if(shouldView(node))
					for(Aspect aspect : node.getAspects().aspectSet())
						drawNodeAspectCount(camera, node, buffer, aspect);
		}
		
		RenderSystem.depthMask(true);
		context.lightmapTextureManager().disable();
	}
	
	private static boolean shouldView(Node node){
		var client = MinecraftClient.getInstance();
		var maxDist = client.options.getClampedViewDistance() * 14;
		return maxDist * maxDist >= client.player.squaredDistanceTo(node.getX(), node.getY(), node.getZ());
	}
	
	private static void drawNode(Camera camera, Node node, BufferBuilder buffer, float alpha){
		drawQuad(camera, node, Vec3f.ZERO, buffer, alpha, scale(node), v(node, false), v(node, true), light(node));
	}
	
	private static void drawNodeAspect(Camera camera, Node node, BufferBuilder buffer, Aspect aspect){
		if(node.getAspects().size() == 0 || !node.getAspects().contains(aspect))
			return;
		Vec3f offset = Vec3f.POSITIVE_Y.copy();
		offset.scale(1.2f * lerpView.getOrDefault(node, 0f));
		offset.add(0, 0, -0.01f);
		offset.rotate(Quaternion.fromEulerXyz(0, 0, (float)((Math.PI * 2) * (node.getAspects().indexOf(aspect) / (float)node.getAspects().size()))));
		var alpha = (float)(.85 - Math.sqrt(MinecraftClient.getInstance().player.squaredDistanceTo(node.getX(), node.getY(), node.getZ())) / 10);
		alpha *= lerpView.getOrDefault(node, 0f);
		drawQuad(camera, node, offset, buffer, alpha, .35f, 0, 1, light(node));
	}
	
	private static void drawNodeAspectCount(Camera camera, Node node, BufferBuilder buffer, Aspect aspect){
		if(node.getAspects().size() == 0 || !node.getAspects().contains(aspect))
			return;
		
		String amount = node.getAspects().underlying().get(aspect).toString();
		
		double sqrDist = MinecraftClient.getInstance().player.squaredDistanceTo(node.getX(), node.getY(), node.getZ());
		var alpha = (float)(1 - Math.sqrt(sqrDist) / 10);
		alpha *= lerpView.getOrDefault(node, 0f);
		if(alpha < 4 / 255f) // text renderer thinks zero/very low alpha = full alpha but i forgot to say it
			alpha = 4 / 255f;
		var intAlpha = (int)(alpha * 255) << 24;
		
		Vec3f offset = Vec3f.POSITIVE_Y.copy();
		offset.scale(1.2f * lerpView.getOrDefault(node, 0f));
		offset.rotate(Quaternion.fromEulerXyz(0, 0, (float)((Math.PI * 2) * (node.getAspects().indexOf(aspect) / (float)node.getAspects().size()))));
		
		var stack = RenderSystem.getModelViewStack();
		stack.push();
		stack.multiply(camera.getRotation());
		stack.translate(-node.getX(), node.getY(), -node.getZ());
		stack.translate(camera.getPos().x, -camera.getPos().y, camera.getPos().z);
		var o = camera.getRotation().toEulerXyz();
		stack.multiply(Quaternion.fromEulerXyz(0, o.getY(), 0));
		stack.multiply(Quaternion.fromEulerXyz(-o.getX(), 0, -o.getZ()));
		stack.translate(-offset.getX(), offset.getY(), offset.getZ());
		stack.multiply(Quaternion.fromEulerXyz(0, (float)Math.PI, (float)Math.PI));
		stack.scale(.035f, .035f, .1f);
		stack.translate(0, 0, -0.25);
		MinecraftClient.getInstance().textRenderer.draw(stack, amount, 0, 0, 0xFFFFFF | intAlpha);
		stack.translate(1, 1, 0.1);
		MinecraftClient.getInstance().textRenderer.draw(stack, amount, 0, 0, 0x666666 | intAlpha);
		stack.pop();
	}
	
	private static void drawQuad(Camera camera, Position pos, Vec3f offset, VertexConsumer cons, float alpha, float scale, float minV, float maxV, int light){
		if(alpha <= 0)
			return;
		
		// based on BillboardParticle
		Vec3f[] corners = { new Vec3f(-1, -1, 0), new Vec3f(-1, 1, 0), new Vec3f(1, 1, 0), new Vec3f(1, -1, 0) };
		Quaternion rot = camera.getRotation();
		for(Vec3f corner : corners){
			corner.scale(scale);
			corner.add(offset);
			corner.rotate(rot);
			corner.add((float)(pos.getX() - camera.getPos().x),
			           (float)(pos.getY() - camera.getPos().y),
			           (float)(pos.getZ() - camera.getPos().z));
		}
		
		cons.vertex(corners[0].getX(), corners[0].getY(), corners[0].getZ())
				.texture(1, maxV)
				.color(1, 1, 1, alpha)
				.light(light)
				.next();
		cons.vertex(corners[1].getX(), corners[1].getY(), corners[1].getZ())
				.texture(1, minV)
				.color(1, 1, 1, alpha)
				.light(light)
				.next();
		cons.vertex(corners[2].getX(), corners[2].getY(), corners[2].getZ())
				.texture(0, minV)
				.color(1, 1, 1, alpha)
				.light(light)
				.next();
		cons.vertex(corners[3].getX(), corners[3].getY(), corners[3].getZ())
				.texture(0, maxV)
				.color(1, 1, 1, alpha)
				.light(light)
				.next();
	}
	
	private static float scale(Node n){
		return 1;
	}
	
	@SuppressWarnings("IntegerDivisionInFloatingPointContext") // intentional
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