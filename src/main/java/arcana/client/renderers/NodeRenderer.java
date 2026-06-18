package arcana.client.renderers;

import arcana.aspects.Aspect;
import arcana.aspects.Aspects;
import arcana.aura.*;
import arcana.cca_components.Caster;
import arcana.client.AspectRenderHelper;
import arcana.client.RenderHelper;
import arcana.items.GogglesOfRevealingItem;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext;
import net.fabricmc.fabric.impl.event.lifecycle.LoadedChunksCache;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.render.*;
import net.minecraft.client.util.BufferAllocator;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.util.Identifier;
import net.minecraft.util.JsonHelper;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Position;
import net.minecraft.util.math.RotationAxis;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.World;
import org.joml.Matrix4f;
import org.joml.Matrix4fStack;
import org.joml.Quaternionf;
import org.joml.Vector3f;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.*;

public final class NodeRenderer{
	
	private static class NodeState{
		public float aspectLerp, drawLerp, shakeTimer;
	}
	
	private static final Map<NodeType, Integer> framesByType = new HashMap<>(NodeTypes.NODE_TYPES.size());
	
	private static boolean showNodeHitboxes = false;
	// TODO: use UUIDs instead to be stable when resyncing
	private static final Map<Node, NodeState> nodeStates = new WeakHashMap<>();
	
	@SuppressWarnings("resource") // ???
	public static void render(WorldRenderContext context){
		context.profiler().push("arcana:nodes");
		
		ClientPlayerEntity player = MinecraftClient.getInstance().player;
		float dt = MinecraftClient.getInstance().getRenderTickCounter().getLastFrameDuration();
		boolean hasGoggles = GogglesOfRevealingItem.hasRevealing(player);
		
		context.lightmapTextureManager().enable();
		RenderSystem.disableCull();
		RenderSystem.enableBlend();
		RenderSystem.blendFunc(GlStateManager.SrcFactor.SRC_ALPHA, GlStateManager.DstFactor.ONE_MINUS_SRC_ALPHA);
		
		RenderSystem.setShader(GameRenderer::getParticleProgram);
		RenderSystem.setShaderColor(1, 1, 1, 1);
		RenderSystem.depthMask(true);
		
		Tessellator tessellator = Tessellator.getInstance();
		
		Camera camera = context.camera();
		ClientWorld world = context.world();
		
		AuraWorld auraWorld = AuraWorld.from(world);
		List<Node> allVisible = ((LoadedChunksCache)world).fabric_getLoadedChunks().stream()
				.map(AuraChunk::from)
				.flatMap(x -> x.nodes().stream())
				.toList();
		
		Map<NodeType, List<Node>> nodesByType = new HashMap<>();
		for(Node node : allVisible)
			nodesByType.computeIfAbsent(node.getType(), __ -> new ArrayList<>()).add(node);
		
		// update node states
		// only render aspects for the one you look at
		Node looking = auraWorld.raycastNodes(player, false).orElse(null);
		NodeReference drainingRef = Caster.from(player).drainTargetNode();
		Node draining = drainingRef == null ? null : drainingRef.deref(world).orElse(null);
		for(Node node : allVisible){
			NodeState ns = stateFor(node);
			ns.aspectLerp = MathHelper.lerp(1 - (float)Math.pow(2, -dt / 3), ns.aspectLerp, node.equals(looking) ? 1 : 0);
			boolean isDT = node.equals(draining);
			ns.drawLerp = MathHelper.lerp(1 - (float)Math.pow(2, -dt / (isDT ? 8 : 2)), ns.drawLerp, isDT ? 1 : 0);
			ns.shakeTimer = Math.max(0, ns.shakeTimer - 1);
		}
		
		// first pass, visible through blocks if you have goggles of revealing
		if(hasGoggles)
			RenderSystem.disableDepthTest();
		nodesByType.forEach((type, nodes) -> {
			RenderSystem.setShaderTexture(0, loadTexture(type));
			BufferBuilder buffer = tessellator.begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_TEXTURE_COLOR_LIGHT);
			for(Node node : nodes)
				drawNode(camera, node, buffer, .12f, world);
			RenderHelper.drawBuffer(buffer);
		});
		
		// second pass, hidden by blocks, requires goggles
		RenderSystem.enableDepthTest();
		if(hasGoggles){
			nodesByType.forEach((type, nodes) -> {
				RenderSystem.setShaderTexture(0, loadTexture(type));
				BufferBuilder buffer = tessellator.begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_TEXTURE_COLOR_LIGHT);
				for(Node node : nodes)
					drawNode(camera, node, buffer, .85f, world);
				RenderHelper.drawBuffer(buffer);
			});
			
			Aspects.primals.forEach(primal -> {
				RenderSystem.setShaderTexture(0, AspectRenderHelper.texture(primal));
				BufferBuilder buffer = tessellator.begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_TEXTURE_COLOR_LIGHT);
				for(Node node : allVisible)
					drawNodeAspect(camera, node, buffer, primal, world);
				RenderHelper.drawBuffer(buffer);
			});
			
			for(Node node : allVisible){
				// can't batch non-primals, so avoid these if we can
				for(Aspect aspect : node.getAspects().aspectSet())
					if(!Aspects.primals.contains(aspect)){
						RenderSystem.setShaderTexture(0, AspectRenderHelper.texture(aspect));
						BufferBuilder buffer = tessellator.begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_TEXTURE_COLOR_LIGHT);
						drawNodeAspect(camera, node, buffer, aspect, world);
						RenderHelper.drawBuffer(buffer);
					}
			}
			
			for(Node node : allVisible)
				for(Aspect aspect : node.getAspects().aspectSet())
					drawNodeAspectCount(camera, node, aspect);
		}
		
		// show node hitboxes
		if(showNodeHitboxes){
			RenderSystem.setShader(GameRenderer::getRenderTypeLinesProgram);
			RenderSystem.lineWidth(1f);
			BufferBuilder buffer = tessellator.begin(VertexFormat.DrawMode.LINES, VertexFormats.LINES);
			for(Node node : allVisible)
				WorldRenderer.drawBox(new MatrixStack(), buffer, node.bounds().offset(camera.getPos().negate()), 0f, 0.5f, 1f, 1f);
			RenderHelper.drawBuffer(buffer);
		}
		
		RenderSystem.depthMask(true);
		context.lightmapTextureManager().disable();
		
		context.profiler().pop();
	}
	
	//
	
	public static boolean toggleHitboxRendering(){
		return showNodeHitboxes ^= true;
	}
	
	public static void shakeNode(Node node, int ticks){
		NodeState ns = stateFor(node);
		ns.shakeTimer = Math.max(ns.shakeTimer, ticks);
	}
	
	//
	
	private static NodeState stateFor(Node node){
		return nodeStates.computeIfAbsent(node, __ -> new NodeState());
	}
	
	private static float scaleFor(Node n){
		NodeState ns = stateFor(n);
		return MathHelper.lerp(Math.min(ns.drawLerp + ns.shakeTimer / 50, 1), 1, 0.5f);
	}
	
	private static int lightFor(Node n, World world){
		return WorldRenderer.getLightmapCoordinates(world, n.asBlockPos());
	}
	
	private static Vector3f offsetFor(Node n){
		float timer = stateFor(n).shakeTimer;
		if(timer <= 0)
			return new Vector3f();
		Random rng = MinecraftClient.getInstance().world.random;
		Vector3f f = new Vector3f(rng.nextFloat() - 0.5f, rng.nextFloat() - 0.5f, rng.nextFloat() - 0.5f);
		f.mul(MathHelper.sqrt(timer) / 15);
		return f;
	}
	
	//
	
	private static void drawNode(Camera camera, Node node, BufferBuilder buffer, float alpha, World w){
		float scale = scaleFor(node);
		Vector3f offset = offsetFor(node);
		offset.add(-scale, -scale, 0);
		drawQuad(camera, node, offset, buffer, alpha, scale * 2, v(node, false, w), v(node, true, w), 1, lightFor(node, w));
	}
	
	private static void drawNodeAspect(Camera camera, Node node, BufferBuilder buffer, Aspect aspect, World world){
		NodeState ns = stateFor(node);
		if(!node.getAspects().contains(aspect) || ns.aspectLerp < 0.00001)
			return;
		float scale = .7f;
		// calculate positions in a circle around the node
		Vector3f offset = new Vector3f(0, 1, 0);
		offset.mul(1.2f * ns.aspectLerp);
		offset.add(0, 0, -0.01f);
		offset.rotate(RotationAxis.POSITIVE_Z.rotation((float)((Math.PI * 2) * (node.getAspects().indexOf(aspect) / (float)node.getAspects().size()))));
		// centre, face to camera
		offset.add(-scale / 2, -scale / 2, 0);
		
		float alpha = ns.aspectLerp * (float)(.85 - Math.sqrt(MinecraftClient.getInstance().player.squaredDistanceTo(node.getX(), node.getY(), node.getZ())) / 10);
		float frac = node.getAspectCap().contains(aspect) ? node.getAspects().get(aspect) / (float)node.getAspectCap().get(aspect) : 1;
		frac = Math.min(frac, 1);
		// draw bottom "full" part, frac offset, frac size
		drawQuad(camera, node, offset, buffer, alpha, scale, 1 - frac, 1, frac, lightFor(node, world));
		// draw top "empty" part, 0 offset, 1-frac size
		offset.add(0, (frac) * scale, 0);
		drawQuad(camera, node, offset, buffer, alpha / 2, scale, 0, 1 - frac, 1 - frac, lightFor(node, world));
	}
	
	private static void drawNodeAspectCount(Camera camera, Node node, Aspect aspect){
		if(!node.getAspects().contains(aspect))
			return;
		
		NodeState ns = stateFor(node);
		String amount = node.getAspects().underlying().get(aspect).toString();
		
		double sqrDist = MinecraftClient.getInstance().player.squaredDistanceTo(node.getX(), node.getY(), node.getZ());
		float alpha = (float)(1 - Math.sqrt(sqrDist) / 10);
		alpha *= ns.aspectLerp;
		if(alpha < 4 / 255f) // text renderer treats zero/very low alpha as implicit full alpha
			alpha = 4 / 255f;
		var intAlpha = (int)(alpha * 255) << 24;
		
		Vector3f offset = new Vector3f(0, 1, 0);
		offset.mul(1.2f * ns.aspectLerp);
		offset.rotate(RotationAxis.POSITIVE_Z.rotation((float)((Math.PI * 2) * (node.getAspects().indexOf(aspect) / (float)node.getAspects().size()))));
		
		// TODO: fix text rendering
		VertexConsumerProvider.Immediate vcp = VertexConsumerProvider.immediate(new BufferAllocator(512));
		Matrix4fStack stack = RenderSystem.getModelViewStack();
		stack.pushMatrix();
		stack.rotate(camera.getRotation());
		stack.translate((float)-node.getX(), (float)node.getY(), (float)-node.getZ());
		stack.translate((float)camera.getPos().x, (float)-camera.getPos().y, (float)camera.getPos().z);
		Vector3f o = camera.getRotation().getEulerAnglesXYZ(new Vector3f());
		stack.rotate(new Quaternionf().rotateXYZ(0, o.y(), 0));
		stack.rotate(new Quaternionf().rotateXYZ(-o.x(), 0, -o.z()));
		stack.translate(-offset.x(), offset.y(), offset.z());
		stack.rotate(new Quaternionf().rotateXYZ(0, MathHelper.PI, MathHelper.PI));
		stack.scale(.035f, .035f, .1f);
		stack.translate(0, 0, -0.25f);
		MinecraftClient.getInstance().textRenderer.draw(amount, 0, 0, 0xFFFFFF | intAlpha, false, stack.get(new Matrix4f()), vcp, TextRenderer.TextLayerType.POLYGON_OFFSET, 0, LightmapTextureManager.MAX_LIGHT_COORDINATE);
		stack.translate(1, 1, 0.1f);
		MinecraftClient.getInstance().textRenderer.draw(amount, 0, 0, 0xFFFFFF | intAlpha, false, stack.get(new Matrix4f()), vcp, TextRenderer.TextLayerType.POLYGON_OFFSET, 0, LightmapTextureManager.MAX_LIGHT_COORDINATE);
		stack.popMatrix();
		vcp.draw();
	}
	
	private static void drawQuad(Camera camera, Position pos, Vector3f offset, VertexConsumer cons, float alpha, float scale, float minV, float maxV, float height, int light){
		if(alpha <= 0)
			return;
		
		// based on BillboardParticle
		Vector3f[] corners = { new Vector3f(0, 0, 0), new Vector3f(0, height, 0), new Vector3f(1, height, 0), new Vector3f(1, 0, 0) };
		Quaternionf rot = camera.getRotation();
		for(Vector3f corner : corners){
			corner.mul(scale);
			corner.add(offset);
			corner.rotate(rot);
			corner.add((float)(pos.getX() - camera.getPos().x),
					(float)(pos.getY() - camera.getPos().y),
					(float)(pos.getZ() - camera.getPos().z));
		}
		
		cons.vertex(corners[0].x(), corners[0].y(), corners[0].z())
				.texture(1, maxV)
				.color(1, 1, 1, alpha)
				.light(light);
		cons.vertex(corners[1].x(), corners[1].y(), corners[1].z())
				.texture(1, minV)
				.color(1, 1, 1, alpha)
				.light(light);
		cons.vertex(corners[2].x(), corners[2].y(), corners[2].z())
				.texture(0, minV)
				.color(1, 1, 1, alpha)
				.light(light);
		cons.vertex(corners[3].x(), corners[3].y(), corners[3].z())
				.texture(0, maxV)
				.color(1, 1, 1, alpha)
				.light(light);
	}
	
	@SuppressWarnings("IntegerDivisionInFloatingPointContext") // intentional
	private static float v(Node n, boolean max, World world){
		int f = maxFrames(n.getType());
		return (1f / f) * ((world.getTime() / 2 + n.getUuid().hashCode()) % f + (max ? 1 : 0));
	}
	
	//
	
	// TODO: reimpl
	
	private static Identifier loadTexture(NodeType nt){
		if(!framesByType.containsKey(nt))
			loadMeta(nt);
		return getNodeResourceId(nt, ".png");
	}
	
	private static void loadMeta(NodeType nt){
		// this is incredibly stupid
		try{
			try(InputStream stream = MinecraftClient.getInstance().getResourceManager().getResource(getNodeResourceId(nt, ".png.nodemeta")).get().getInputStream()){
				var metaObj = JsonHelper.deserialize(new BufferedReader(new InputStreamReader(stream)));
				framesByType.put(nt, JsonHelper.getArray(JsonHelper.getObject(metaObj, "animation"), "frames").size());
			}
		}catch(IOException e){
			throw new RuntimeException(e);
		}
	}
	
	private static Identifier getNodeResourceId(NodeType nt, String ext){
		return Identifier.of(nt.id().getNamespace(), "textures/nodes/" + nt.id().getPath() + ext);
	}
	
	private static int maxFrames(NodeType nt){
		return framesByType.get(nt);
	}
}