package arcana.client.research.sections;

import arcana.client.research.EntrySectionRenderer;
import arcana.research.sections.PonderSection;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.systems.VertexSorter;
import net.createmod.catnip.animation.LerpedFloat;
import net.createmod.catnip.data.Pair;
import net.createmod.catnip.render.DefaultSuperRenderTypeBuffer;
import net.createmod.catnip.render.SuperRenderTypeBuffer;
import net.createmod.ponder.foundation.PonderIndex;
import net.createmod.ponder.foundation.PonderScene;
import net.createmod.ponder.foundation.registration.PonderLocalization;
import net.createmod.ponder.foundation.ui.PonderUI;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.util.Window;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.lwjgl.glfw.GLFW;

import java.util.List;

import static arcana.Arcana.arcId;
import static arcana.screens.ResearchEntryScreen.*;

public class PonderSectionRenderer implements EntrySectionRenderer<PonderSection>{
	
	private static List<PonderScene> lastSceneList = null;
	private static Identifier lastSceneId = null;
	private static int curSceneIndex = 0, lastSceneIndex = 0;
	
	public void render(DrawContext ctx, PonderSection section, int pageIdx, int screenWidth, int screenHeight, int mouseX, int mouseY, boolean right){
		if(right)
			return;
		
		final int fullBookX = 13, fullBookY = 5, fullBookWidth = 230, fullBookHeight = 167;
		
		int x = fullBookX + (screenWidth - 256) / 2;
		int y = (screenHeight - bgHeight) / 2 + fullBookY;
		
		Identifier sceneId = section.getScene();
		PonderScene scene;
		try{
			boolean newPlace = !sceneId.equals(lastSceneId);
			boolean newScene = newPlace || curSceneIndex != lastSceneIndex;
			if(newPlace)
				curSceneIndex = 0;
			if(newScene){
				lastSceneId = sceneId;
				lastSceneList = PonderIndex.getSceneAccess().compile(sceneId);
				lastSceneIndex = curSceneIndex;
			}
			
			scene = lastSceneList.get(curSceneIndex);
			if(newScene)
				scene.begin();
		}catch(Exception e){
			ctx.drawText(textRenderer(), e.toString(), x, y, 0, false);
			return;
		}
		
		SuperRenderTypeBuffer buffer = DefaultSuperRenderTypeBuffer.getInstance();
		MatrixStack matrices = ctx.getMatrices();
		float pt = client().getRenderTickCounter().getTickDelta(false);
		
		RenderSystem.enableBlend();
		RenderSystem.enableDepthTest();
		RenderSystem.backupProjectionMatrix();
		
		//
		
		// has to be outside of MS transforms, important for vertex sorting
		Matrix4f matrix4f = new Matrix4f(RenderSystem.getProjectionMatrix());
		matrix4f.translate(0, 0, 800);
		RenderSystem.setProjectionMatrix(matrix4f, VertexSorter.BY_DISTANCE);
		RenderSystem.setupLevelDiffuseLighting(new Vector3f(0.4F, -1.0F, 0.7F).normalize(), new Vector3f(-0.4F, -0.5F, 0.7F).normalize());
		
		float scale = Math.min((float)fullBookWidth / screenWidth, (float)fullBookHeight / screenHeight);
		matrices.push();
		matrices.translate(x, y, -800);
		matrices.scale(scale, scale, scale);
		// a ponder tries to use ~half the screen, try to use the full book size instead
		scene.getTransform().updateScreenParams((int)(fullBookWidth / scale), (int)(fullBookHeight / scale), /*slide*/ 0);
		scene.getTransform().apply(matrices, pt);
		scene.getTransform().updateSceneRVE(pt);
		scene.renderScene(buffer, ctx, pt);
		buffer.draw();
		matrices.pop();
		
		RenderSystem.restoreProjectionMatrix();
		
		// and the overlay
		
		matrices.push();
		matrices.translate(x, y, 0);
		matrices.scale(scale, scale, scale);
		scene.renderOverlay(DummyPonderUI.DUMMY, ctx, pt);
		matrices.pop();
		
		Pair<ItemStack, BlockPos> what = getIdentifiedItem(scene, x, y, scale);
		ItemStack it = what.getFirst();
		if(!it.isEmpty())
			ctx.drawItemTooltip(textRenderer(), it, mouseX, mouseY);
	}
	
	public void renderAfter(DrawContext ctx, PonderSection section, int pageIdx, int screenWidth, int screenHeight, int mouseX, int mouseY, boolean right){
		int x = right ? pageX + rightXOffset : pageX;
	}
	
	public int span(PonderSection section, PlayerEntity player){
		return 2;
	}
	
	public static void onRemoved(){
		lastSceneList = null;
		lastSceneId = null;
		curSceneIndex = lastSceneIndex = 0;
	}
	
	public static void tick(){
		if(lastSceneList != null && !lastSceneList.isEmpty())
			lastSceneList.get(curSceneIndex).tick();
	}
	
	public static void onKeyReleased(int key, int scancode, int modifiers){
		if(key == GLFW.GLFW_KEY_P){
			curSceneIndex++;
			if(lastSceneList != null)
				curSceneIndex = Math.min(curSceneIndex, lastSceneList.size() - 1);
		}else if(key == GLFW.GLFW_KEY_O){
			curSceneIndex--;
			curSceneIndex = Math.max(curSceneIndex, 0);
		}else if(key == GLFW.GLFW_KEY_K && lastSceneList != null){
			PonderScene.SceneTransform transform = lastSceneList.get(curSceneIndex).getTransform();
			transform.yRotation.chase(transform.yRotation.getChaseTarget() + 45, 0.4f, LerpedFloat.Chaser.EXP);
		}
		else if(key == GLFW.GLFW_KEY_L && lastSceneList != null){
			PonderScene.SceneTransform transform = lastSceneList.get(curSceneIndex).getTransform();
			transform.yRotation.chase(transform.yRotation.getChaseTarget() - 45, 0.4f, LerpedFloat.Chaser.EXP);
		}
	}
	
	public static Pair<ItemStack, BlockPos> getIdentifiedItem(PonderScene activeScene, int x, int y, float scale){
		MinecraftClient client = MinecraftClient.getInstance();
		Window w = client.getWindow();
		double mouseX = ((client.mouse.getX() * w.getScaledWidth() / w.getWidth()) - x) / scale;
		double mouseY = ((client.mouse.getY() * w.getScaledHeight() / w.getHeight()) - y) / scale;
		PonderScene.SceneTransform t = activeScene.getTransform();
		Vec3d vec1 = t.screenToScene(mouseX, mouseY, 1000, 0);
		Vec3d vec2 = t.screenToScene(mouseX, mouseY, -100, 0);
		return activeScene.rayTraceScene(vec1, vec2);
	}
	
	private static class DummyPonderUI extends PonderUI{
		
		public static final PonderScene DUMMY_SCENE = new PonderScene(null, (PonderLocalization)PonderIndex.getLangAccess(), "", arcId("arcanum"), List.of(), List.of());
		public static final PonderUI DUMMY = new DummyPonderUI(List.of(DUMMY_SCENE));
		
		protected DummyPonderUI(List<PonderScene> scenes){
			super(scenes);
			client = MinecraftClient.getInstance();
			textRenderer = client.textRenderer;
			width = client.getWindow().getScaledWidth();
			height = client.getWindow().getScaledHeight();
		}
	}
}