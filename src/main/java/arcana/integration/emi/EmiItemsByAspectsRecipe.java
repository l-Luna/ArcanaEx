package arcana.integration.emi;

import arcana.aspects.Aspect;
import arcana.aspects.ItemAspectRegistry;
import arcana.client.RenderHelper;
import com.mojang.blaze3d.systems.RenderSystem;
import dev.emi.emi.EmiRenderHelper;
import dev.emi.emi.EmiUtil;
import dev.emi.emi.api.recipe.EmiIngredientRecipe;
import dev.emi.emi.api.recipe.EmiRecipe;
import dev.emi.emi.api.recipe.EmiRecipeCategory;
import dev.emi.emi.api.render.EmiTexture;
import dev.emi.emi.api.stack.EmiIngredient;
import dev.emi.emi.api.stack.EmiStack;
import dev.emi.emi.api.widget.ButtonWidget;
import dev.emi.emi.api.widget.SlotWidget;
import dev.emi.emi.api.widget.WidgetHolder;
import dev.emi.emi.bom.BoM;
import dev.emi.emi.runtime.EmiDrawContext;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.item.Item;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.function.BooleanSupplier;

import static arcana.Arcana.arcId;

public class EmiItemsByAspectsRecipe extends EmiIngredientRecipe{
	
	public record Entry(Item item, EmiStack emified, int count, float purity){}
	
	private final List<Entry> entries;
	private final List<EmiStack> items;
	private final AspectEmiStack aspectStack;
	
	public EmiItemsByAspectsRecipe(List<Entry> entries, Aspect aspect){
		this.entries = entries;
		this.items = entries.stream().map(Entry::emified).toList();
		this.aspectStack = new AspectEmiStack(aspect);
	}
	
	protected EmiIngredient getIngredient(){
		return aspectStack;
	}
	
	protected List<EmiStack> getStacks(){
		return items;
	}
	
	public void addWidgets(WidgetHolder widgets){
		List<EmiStack> stacks = getStacks();
		widgets.addSlot(getIngredient(), 63, 0);
		final int slotHeight = 32;
		int pageHeight = (widgets.getHeight() - 24) / slotHeight;
		int pageSize = pageHeight * 8;
		AspectPageManager manager = new AspectPageManager(entries, pageSize);
		manager.resort();
		if(pageSize < stacks.size()){
			widgets.addButton(2, 3, 12, 12, 0, 0, () -> true, (mouseX, mouseY, button) -> manager.scroll(-1));
			widgets.addButton(widgets.getWidth() - 14, 3, 12, 12, 12, 0, () -> true, (mouseX, mouseY, button) -> manager.scroll(1));
		}
		widgets.add(new ToggleButtonWidget(63 - 14, 3, 12, 12, 43, 0, ArcanaEmiPlugin.WIDGETS, () -> manager.sortDesc, (_1, _2, _3) -> manager.toggleSortDirection()));
		widgets.add(new ToggleButtonWidget(63 - 14*2, 3, 12, 12, 67, 0, ArcanaEmiPlugin.WIDGETS, () -> manager.sortPurity, (_1, _2, _3) -> manager.toggleSortType()));
		for(int i = 0; i < stacks.size() && i < pageSize; i++)
			widgets.add(new AspectResolutionSlotWidget(manager, i, i % 8 * 18, i / 8 * slotHeight + 24));
	}
	
	public int getDisplayHeight(){
		return ((getStacks().size() - 1) / 8 + 1) * 32 + 24;
	}
	
	protected EmiRecipe getRecipeContext(EmiStack stack, int offset){
		return new EmiAspectsByItemsRecipe(stack, ItemAspectRegistry.get(stack.getItemStack()).asStacks(), stack.getId());
	}
	
	public EmiRecipeCategory getCategory(){
		return ArcanaEmiPlugin.ITEMS_BY_ASPECTS;
	}
	
	public @Nullable Identifier getId(){
		return arcId("/items_with/" + EmiUtil.subId(aspectStack.getId()));
	}
	
	public List<EmiIngredient> getInputs(){
		return new ArrayList<>(items);
	}
	
	public List<EmiStack> getOutputs(){
		return List.of(aspectStack);
	}
	
	// from EmiIngredientRecipe
	private class AspectPageManager{
		private final List<Entry> entries;
		private final int pageSize;
		private int currentPage;
		
		private boolean sortPurity = false, sortDesc = true;
		
		public AspectPageManager(List<Entry> entries, int pageSize){
			this.entries = new ArrayList<>(entries);
			this.pageSize = pageSize;
		}
		
		public void scroll(int delta){
			currentPage += delta;
			int totalPages = (entries.size() - 1) / pageSize + 1;
			if(currentPage < 0)
				currentPage = totalPages - 1;
			if(currentPage >= totalPages)
				currentPage = 0;
		}
		
		public void toggleSortType(){
			sortPurity ^= true;
			resort();
		}
		
		public void toggleSortDirection(){
			sortDesc ^= true;
			resort();
		}
		
		private void resort(){
			Comparator<Entry> comparator = sortPurity
					? Comparator.comparingDouble(x -> x.purity)
					: Comparator.comparingInt(x -> x.count);
			if(sortDesc)
				comparator = comparator.reversed();
			entries.sort(comparator);
		}
		
		@Nullable
		public Entry getEntry(int offset){
			offset += pageSize * currentPage;
			if(offset < entries.size())
				return entries.get(offset);
			return null;
		}
		
		public EmiRecipe getRecipe(int offset){
			offset += pageSize * currentPage;
			if(offset < entries.size())
				return getRecipeContext(entries.get(offset).emified, offset);
			return null;
		}
	}
	
	private class AspectResolutionSlotWidget extends SlotWidget{
		public static final EmiTexture SLOT_BG = new EmiTexture(ArcanaEmiPlugin.WIDGETS, 25, 0, 18, 31);
		public final AspectPageManager manager;
		public final int offset;
		
		public AspectResolutionSlotWidget(AspectPageManager manager, int offset, int x, int y){
			super(EmiStack.EMPTY, x, y);
			this.manager = manager;
			this.offset = offset;
		}
		
		public EmiIngredient getStack(){
			Entry entry = manager.getEntry(offset);
			return entry != null ? entry.emified : EmiStack.EMPTY;
		}
		
		public EmiRecipe getRecipe(){
			return manager.getRecipe(offset);
		}
		
		public void render(DrawContext ctx, int mouseX, int mouseY, float delta){
			Entry entry = manager.getEntry(offset);
			if(!getStack().isEmpty() && entry != null){
				super.render(ctx, mouseX, mouseY, delta);
				RenderHelper.drawTinyNumbers(ctx, String.valueOf(entry.count), x + 2, y + 26, 22 / 255f, 206 / 255f, 242 / 255f, 1);
				RenderHelper.drawTinyNumbers(ctx, Math.round(entry.purity * 100) + "%", x + 2, y + 32, 0.95f, 0.95f, 0.5f, 1);
			}
		}
		
		// FIXME: private EMI API
		public void drawBackground(DrawContext ctx, int mouseX, int mouseY, float delta){
			SLOT_BG.render(ctx, x, y, 0);
			EmiDrawContext context = EmiDrawContext.wrap(ctx);
			if(BoM.getRecipe(getIngredient()) instanceof EmiAspectsByItemsRecipe recipe
					&& recipe.getInputs().equals(List.of(getStack())))
				context.drawTexture(EmiRenderHelper.WIDGETS, x, y, 36, 128, 18, 18);
		}
	}
	
	private static class ToggleButtonWidget extends ButtonWidget{
		
		public ToggleButtonWidget(int x, int y, int width, int height, int u, int v, Identifier texture, BooleanSupplier which, ClickAction action){
			super(x, y, width, height, u, v, texture, which, action);
		}
		
		public void render(DrawContext ctx, int mouseX, int mouseY, float delta){
			int u = this.u;
			int v = this.v;
			if(isActive.getAsBoolean())
				u += width;
			if(getBounds().contains(mouseX, mouseY))
				v += height;
			RenderSystem.enableDepthTest();
			ctx.drawTexture(texture, x, y, 0, u, v, width, height, 256, 256);
		}
	}
}