package arcana.integration.emi;

import arcana.aspects.Aspect;
import arcana.aspects.AspectStack;
import arcana.aspects.Aspects;
import arcana.client.AspectRenderHelper;
import arcana.client.tooltip.PinkMarkerComponent;
import dev.emi.emi.EmiUtil;
import dev.emi.emi.api.stack.EmiStack;
import dev.emi.emi.api.stack.serializer.EmiStackSerializer;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.tooltip.TooltipComponent;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;

import java.util.ArrayList;
import java.util.List;

public class AspectEmiStack extends EmiStack{
	
	private AspectStack stack;
	
	public AspectEmiStack(AspectStack stack){
		this.stack = stack;
	}
	
	public AspectEmiStack(Aspect aspect, int amount){
		this(new AspectStack(aspect, amount));
	}
	
	public AspectEmiStack(Aspect aspect){
		this(aspect, 1);
	}
	
	public EmiStack copy(){
		return new AspectEmiStack(stack);
	}
	
	public boolean isEmpty(){
		return stack.amount() == 0;
	}
	
	public void render(MatrixStack matrices, int x, int y, float delta, int flags){
		AspectRenderHelper.renderAspectStack(stack, matrices, MinecraftClient.getInstance().textRenderer, x, y, 100);
	}
	
	public NbtCompound getNbt(){
		return null;
	}
	
	public Object getKey(){
		return stack.type();
	}
	
	public Identifier getId(){
		return stack.type().id();
	}
	
	public List<Text> getTooltipText(){
		return List.of(stack.type().name());
	}
	
	public List<TooltipComponent> getTooltip(){
		List<TooltipComponent> tooltips = new ArrayList<>(3);
		tooltips.add(TooltipComponent.of(getName().asOrderedText()));
		if(MinecraftClient.getInstance().options.advancedItemTooltips)
			tooltips.add(TooltipComponent.of(Text.literal(stack.type().id().toString()).formatted(Formatting.DARK_GRAY).asOrderedText()));
		// TODO: remove use of EmiUtil
		tooltips.add(TooltipComponent.of(Text.literal(EmiUtil.getModName(stack.type().id().getNamespace())).formatted(Formatting.BLUE, Formatting.ITALIC).asOrderedText()));
		tooltips.add(new PinkMarkerComponent());
		return tooltips;
	}
	
	public Text getName(){
		return stack.type().name();
	}
	
	public EmiStack setAmount(long amount){
		stack = new AspectStack(stack.type(), (int)amount);
		return this;
	}
	
	public long getAmount(){
		return stack.amount();
	}
	
	/*public static class AspectEmiStackSerializer implements EmiIngredientSerializer<AspectEmiStack>{
		
		public JsonObject serialize(AspectEmiStack stack){
			JsonObject obj = new JsonObject();
			obj.addProperty("id", stack.stack.type().id().toString());
			obj.addProperty("amount", stack.stack.amount());
			return obj;
		}
		
		public String getType(){
			return "arcana:aspect";
		}
		
		public EmiIngredient deserialize(JsonElement elem){
			JsonObject object = elem.getAsJsonObject();
			Identifier id = new Identifier(object.get("id").getAsString());
			int amount = object.get("amount").getAsInt();
			return new AspectEmiStack(Aspects.byName(id), amount);
		}
	}*/
	
	public static class AspectEmiStackSerializer implements EmiStackSerializer<AspectEmiStack>{
		
		public EmiStack create(Identifier id, NbtCompound nbt, long amount){
			return new AspectEmiStack(Aspects.byName(id), (int)amount);
		}
		
		public String getType(){
			return "arcana_aspect";
		}
	}
}