package arcana.items.components;

import arcana.aspects.AspectMap;
import arcana.aspects.AspectStack;
import arcana.aspects.ItemAspectRegistry;
import arcana.client.AspectRenderHelper;
import arcana.items.HoldingJugItem;
import arcana.util.Both;
import arcana.util.CodecUtil;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.serialization.Codec;
import it.unimi.dsi.fastutil.Hash;
import it.unimi.dsi.fastutil.objects.Object2IntLinkedOpenCustomHashMap;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntMaps;
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
import net.minecraft.util.math.random.Random;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static arcana.Arcana.arcId;

@EnvironmentInterface(value = EnvType.CLIENT, itf = TooltipComponent.class)
public class StorageMapComponent implements TooltipData, TooltipComponent{
	
	private static final Hash.Strategy<ItemStack> COMPARE_COMPONENTS = new Hash.Strategy<>(){
		public int hashCode(ItemStack o){
			return Objects.hash(o.getItem(), o.getComponents());
		}
		
		public boolean equals(ItemStack a, ItemStack b){
			return (a == b) || (a != null && b != null && ItemStack.areItemsAndComponentsEqual(a, b));
		}
	};
	
	public static final Codec<StorageMapComponent> CODEC = CodecUtil.assocListCodec(ItemStack.UNCOUNTED_CODEC, Codec.INT).xmap(
			x -> new StorageMapComponent(new Object2IntLinkedOpenCustomHashMap<>(x, COMPARE_COMPONENTS)),
			x -> x.stored
	);
	
	public static final StorageMapComponent DEFAULT = new StorageMapComponent(Object2IntMaps.emptyMap());
	
	private final Object2IntMap<ItemStack> stored;
	private AspectMap cachedAspects = null;
	
	private StorageMapComponent(Object2IntMap<ItemStack> stored){
		this.stored = stored;
	}
	
	public static StorageMapComponent from(ItemStack stack){
		return stack.getOrDefault(ArcanaDataComponents.HOLDING_JUG_CONTENTS, DEFAULT);
	}
	
	public Object2IntMap<ItemStack> stored(){
		return stored;
	}
	
	public AspectMap aspects(){
		if(cachedAspects != null)
			return cachedAspects;
		return cachedAspects = calcAspects();
	}
	
	private AspectMap calcAspects(){
		AspectMap ret = new AspectMap();
		stored.forEach((stack, quantity) -> {
			AspectMap aspects = ItemAspectRegistry.get(stack);
			aspects.multiply(quantity);
			ret.add(aspects);
		});
		return ret;
	}
	
	// mutation
	
	public StorageMapComponent with(ItemStack stack){
		Object2IntMap<ItemStack> copy = new Object2IntLinkedOpenCustomHashMap<>(stored, COMPARE_COMPONENTS);
		copy.mergeInt(stack.copyWithCount(1), stack.getCount(), Integer::sum);
		return new StorageMapComponent(copy);
	}
	
	public Both<StorageMapComponent, ItemStack> extractStack(){
		if(stored.isEmpty())
			return Both.of(this, ItemStack.EMPTY);
		ItemStack stack = stored.keySet().iterator().next();
		int taken = Math.min(stored.getInt(stack), stack.getMaxCount());
		Object2IntMap<ItemStack> copy = new Object2IntLinkedOpenCustomHashMap<>(stored, COMPARE_COMPONENTS);
		copy.put(stack, copy.getInt(stack) - taken);
		if(copy.getInt(stack) <= 0)
			copy.removeInt(stack);
		return Both.of(new StorageMapComponent(copy), stack.copyWithCount(taken));
	}
	
	public Both<StorageMapComponent, ItemStack> extractRandom(Random rng){
		List<ItemStack> choices = new ArrayList<>(stored.keySet());
		ItemStack stack = choices.get(rng.nextInt(choices.size()));
		Object2IntMap<ItemStack> copy = new Object2IntLinkedOpenCustomHashMap<>(stored, COMPARE_COMPONENTS);
		copy.put(stack, copy.getInt(stack) - 1);
		if(copy.getInt(stack) <= 0)
			copy.removeInt(stack);
		return Both.of(new StorageMapComponent(copy), stack.copy());
	}
	
	// rendering
	// TODO: move to separate class to decouple from holding jug
	
	private static final Identifier TEXTURE = arcId("textures/gui/holding_jug.png");
	
	@Environment(EnvType.CLIENT)
	public int getHeight(){
		return stored.isEmpty() ? 28 : 39 + Math.ceilDiv(stored.size(), 5) * 18;
	}
	
	@Environment(EnvType.CLIENT)
	public int getWidth(TextRenderer textRenderer){
		return 101;
	}
	
	@Environment(EnvType.CLIENT)
	public void drawItems(TextRenderer textRenderer, int x, int y, DrawContext ctx){
		if(stored.isEmpty())
			ctx.drawTexture(TEXTURE, x, y, 0, 0, 101, 24);
		else{
			List<Object2IntMap.Entry<ItemStack>> stacks = new ArrayList<>(stored.object2IntEntrySet());
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
				ItemStack item = stacks.get(i).getKey();
				ctx.drawItem(item, iX, iY);
				// looks a bit odd, but we want to use small numbers for the count if it's large, but still show durability bars and such
				ctx.drawItemInSlot(textRenderer, item, iX, iY, "");
				int count = stacks.get(i).getIntValue();
				if(count > 1)
					AspectRenderHelper.renderAspectStackOverlay(count, ctx, textRenderer, iX, iY, 400);
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