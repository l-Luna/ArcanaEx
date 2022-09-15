package arcana.screens;

import arcana.aspects.Aspect;
import arcana.aspects.AspectMap;
import arcana.aspects.Aspects;
import arcana.client.AspectRenderer;
import arcana.items.WandItem;
import arcana.recipes.ShapedArcaneCraftingRecipe;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Vec2f;

import java.util.Map;

import static arcana.Arcana.arcId;

public class ArcaneCraftingScreen extends HandledScreen<ArcaneCraftingScreenHandler>{
	
	private static final Identifier texture = arcId("textures/gui/container/arcane_crafting_table.png");
	
	private static final Map<Aspect, Vec2f> aspectPositions = Map.of(
			Aspects.AIR, new Vec2f(65, 15),
			Aspects.FIRE, new Vec2f(22, 39),
			Aspects.WATER, new Vec2f(108, 39),
			Aspects.EARTH, new Vec2f(22, 89),
			Aspects.ORDER, new Vec2f(108, 89),
			Aspects.ENTROPY, new Vec2f(65, 113)
	);
	
	public ArcaneCraftingScreen(ArcaneCraftingScreenHandler handler, PlayerInventory inventory, Text title){
		super(handler, inventory, title);
	}
	
	protected void init(){
		backgroundWidth = 187;
		backgroundHeight = 233;
		super.init();
		titleX = 10;
		titleY = -5;
	}
	
	protected void drawBackground(MatrixStack matrices, float delta, int mouseX, int mouseY){
		RenderSystem.setShader(GameRenderer::getPositionTexShader);
		RenderSystem.setShaderColor(1, 1, 1, 1);
		RenderSystem.setShaderTexture(0, texture);
		drawTexture(matrices, x, y, 0, 0, backgroundWidth, backgroundHeight);
		
		// draw required aspects for recipe
		ClientWorld world = MinecraftClient.getInstance().world;
		ItemStack wand = handler.wand.getStack(0);
		world.getRecipeManager().getFirstMatch(ShapedArcaneCraftingRecipe.TYPE, handler.input, world).ifPresent(recipe -> {
			AspectMap stored = wand.getItem() instanceof WandItem ? WandItem.aspectsFrom(wand) : new AspectMap();
			for(Aspect aspect : recipe.aspects().aspectSet()){
				int amount = recipe.aspects().get(aspect);
				amount *= WandItem.costMultiplier(aspect, wand, client.player);
				boolean blink = !stored.contains(aspect, amount);
				matrices.push();
				matrices.translate(x, y, getZOffset());
				int x = (int)aspectPositions.get(aspect).x;
				int y = (int)aspectPositions.get(aspect).y;
				float alpha = blink ? (float)Math.abs(Math.sin((world.getTime() + delta) / 4.5f)) * 0.6f + 0.4f : 1;
				AspectRenderer.renderAspect(aspect, matrices, x, y, 0, 1, 1, 1, alpha);
				AspectRenderer.renderAspectStackOverlay(amount, matrices, MinecraftClient.getInstance().textRenderer, x, y, 0);
				matrices.pop();
			}
		});
	}
	
	public void render(MatrixStack matrices, int mouseX, int mouseY, float delta) {
		renderBackground(matrices);
		super.render(matrices, mouseX, mouseY, delta);
		drawMouseoverTooltip(matrices, mouseX, mouseY);
	}
	
	protected void drawForeground(MatrixStack matrices, int mouseX, int mouseY){
		// no-op - don't draw label
	}
}