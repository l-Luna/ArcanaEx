package arcana.items.components;

import arcana.aspects.AspectMap;
import arcana.aspects.AspectStack;
import arcana.aspects.ItemAspectRegistry;
import arcana.client.AspectRenderHelper;
import arcana.items.HoldingJugItem;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.serialization.Codec;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.api.EnvironmentInterface;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.tooltip.TooltipComponent;
import net.minecraft.item.ItemStack;
import net.minecraft.item.tooltip.TooltipData;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.ColorHelper;

import java.util.ArrayList;
import java.util.List;

import static arcana.Arcana.arcId;

@EnvironmentInterface(value = EnvType.CLIENT, itf = TooltipComponent.class)
public class HoldingJugContentsComponent implements TooltipData, TooltipComponent{
	
	public static final Codec<HoldingJugContentsComponent> CODEC = ItemStack.CODEC
			.listOf()
			.xmap(HoldingJugContentsComponent::new, HoldingJugContentsComponent::stacks);
	
	public static final HoldingJugContentsComponent DEFAULT = new HoldingJugContentsComponent(List.of());
	
	// TODO: use a `Map<ItemStack, Integer>` and check component equality instead
	private final List<ItemStack> stacks;
	private AspectMap cachedAspects = null;
	
	public HoldingJugContentsComponent(List<ItemStack> stacks){
		this.stacks = stacks;
	}
	
	public List<ItemStack> stacks(){
		return stacks;
	}
	
	public AspectMap aspects(){
		if(cachedAspects != null)
			return cachedAspects;
		return cachedAspects = calcAspects();
	}
	
	public HoldingJugContentsComponent withoutFirst(){
		return without(0);
	}
	
	public HoldingJugContentsComponent without(int index){
		List<ItemStack> newStacks = new ArrayList<>(stacks);
		newStacks.remove(index);
		return new HoldingJugContentsComponent(newStacks);
	}
	
	public HoldingJugContentsComponent withFirst(ItemStack stack){
		return with(0, stack);
	}
	
	public HoldingJugContentsComponent with(int index, ItemStack stack){
		List<ItemStack> newStacks = new ArrayList<>(stacks);
		newStacks.add(index, stack);
		return new HoldingJugContentsComponent(newStacks);
	}
	
	private AspectMap calcAspects(){
		AspectMap ret = new AspectMap();
		for(ItemStack stack : stacks){
			AspectMap aspects = ItemAspectRegistry.get(stack);
			aspects.multiply(__ -> (float)stack.getCount());
			ret.add(aspects);
		}
		return ret;
	}
	
	// rendering
	
	private static final Identifier TEXTURE = arcId("textures/gui/holding_jug.png");
	
	@Environment(EnvType.CLIENT)
	public int getHeight(){
		return stacks.isEmpty() ? 28 : 39 + Math.ceilDiv(stacks.size(), 5) * 18;
	}
	
	@Environment(EnvType.CLIENT)
	public int getWidth(TextRenderer textRenderer){
		return 101;
	}
	
	@Environment(EnvType.CLIENT)
	public void drawItems(TextRenderer textRenderer, int x, int y, DrawContext ctx){
		if(stacks.isEmpty())
			ctx.drawTexture(TEXTURE, x, y, 0, 0, 101, 24);
		else{
			// draw background textures
			int rows = Math.ceilDiv(stacks.size(), 5);
			ctx.drawTexture(TEXTURE, x, y, 0, 25, 101, 11);
			for(int i = 0; i < rows; i++)
				ctx.drawTexture(TEXTURE, x, y + 11 + i*18, 0, 37, 101, 18);
			ctx.drawTexture(TEXTURE, x, y + 8 + rows*18, 0, 56, 101, 24);
			// draw items
			for(int i = 0; i < stacks.size(); i++){
				int iX = x + 6 + (i%5)*18,
					iY = y + 8 + (i/5)*18;
				ItemStack item = stacks.get(i);
				ctx.drawItem(item, iX, iY);
				ctx.drawItemInSlot(textRenderer, item, iX, iY);
			}
			// draw aspects
			List<AspectStack> aspects = aspects().asStacks();
			for(int i = 0; i < aspects.size(); i++){
				int aX = x + 7 + i*20,
					aY = y + 11 + rows*18;
				AspectRenderHelper.renderAspect(aspects.get(i).type(), ctx, aX, aY, 0);
				AspectRenderHelper.renderAspectStackOverlay(aspects.get(i).amount(), ctx, textRenderer, aX, aY, 0);
			}
			// and the aspect total
			// each aspect provides `amnt/total * 16` pixels, but we only want to round at the boundaries, so also keep a running total
			int total = 0;
			for(AspectStack aspect : aspects){
				int amnt = aspect.amount();
				int colour = aspect.type().colour();
				int startPixel = (total*16) / HoldingJugItem.MAX_CAPACITY;
				int endPixel = ((total + amnt) * 16) / HoldingJugItem.MAX_CAPACITY;
				RenderSystem.setShaderColor(ColorHelper.Argb.getRed(colour) / 255f, ColorHelper.Argb.getGreen(colour) / 255f, ColorHelper.Argb.getBlue(colour) / 255f, 1);
				ctx.drawTexture(TEXTURE, x + 87, y + 11 + rows * 18 + (16-endPixel), 102, (16-endPixel), 7, endPixel - startPixel);
				RenderSystem.setShaderColor(1, 1, 1, 1);
				total += amnt;
			}
		}
	}
}