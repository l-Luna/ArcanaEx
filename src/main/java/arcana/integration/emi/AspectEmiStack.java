package arcana.integration.emi;

import arcana.aspects.Aspect;
import arcana.aspects.AspectStack;
import arcana.aspects.Aspects;
import arcana.client.AspectRenderer;
import com.google.gson.JsonObject;
import dev.emi.emi.EmiPort;
import dev.emi.emi.EmiStackSerializer;
import dev.emi.emi.EmiUtil;
import dev.emi.emi.api.stack.EmiIngredient;
import dev.emi.emi.api.stack.EmiStack;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.tooltip.TooltipComponent;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.text.OrderedText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;

import java.util.ArrayList;
import java.util.List;

import static arcana.Arcana.arcId;

public class AspectEmiStack extends EmiStack{
	
	private final AspectStack stack;
	private final AspectEntry entry;
	
	public AspectEmiStack(AspectStack stack){
		this.stack = stack;
		entry = new AspectEntry(stack.type());
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
		AspectRenderer.renderAspectStack(stack, matrices, MinecraftClient.getInstance().textRenderer, x, y, 100);
	}
	
	public NbtCompound getNbt(){
		return null;
	}
	
	public Object getKey(){
		return stack.type();
	}
	
	public Entry<?> getEntry(){
		return entry;
	}
	
	public Identifier getId(){
		return stack.type().id();
	}
	
	public List<Text> getTooltipText(){
		return List.of(stack.type().name());
	}
	
	public List<TooltipComponent> getTooltip(){
		List<TooltipComponent> tooltips = new ArrayList<>(3);
		tooltips.add(TooltipComponent.of(OrderedText.of(EmiPort.ordered(getName()))));
		if(MinecraftClient.getInstance().options.advancedItemTooltips)
			tooltips.add(TooltipComponent.of(EmiPort.ordered(EmiPort.literal(stack.type().id().toString()).formatted(Formatting.DARK_GRAY))));
		tooltips.add(TooltipComponent.of(EmiPort.ordered(EmiPort.literal(EmiUtil.getModName(stack.type().id().getNamespace()), Formatting.BLUE, Formatting.ITALIC))));
		return tooltips;
	}
	
	public Text getName(){
		return stack.type().name();
	}
	
	public static class AspectEntry extends Entry<Aspect>{
		
		public AspectEntry(Aspect value){
			super(value);
		}
		
		public Class<? extends Aspect> getType(){
			return Aspect.class;
		}
		
		public boolean equals(Object obj){
			return obj instanceof AspectEntry e && e.getValue().equals(getValue());
		}
	}
	
	public static class AspectEmiStackSerializer implements EmiStackSerializer<AspectEmiStack>{
		
		public static final Identifier ID = arcId("aspect");
		
		public Identifier getId(){
			return ID;
		}
		
		public JsonObject toJson(AspectEmiStack stack){
			JsonObject obj = new JsonObject();
			obj.addProperty("id", stack.stack.type().id().toString());
			obj.addProperty("amount", stack.stack.amount());
			return obj;
		}
		
		public EmiIngredient toStack(JsonObject object){
			Identifier id = new Identifier(object.get("id").getAsString());
			int amount = object.get("amount").getAsInt();
			return new AspectEmiStack(Aspects.byName(id), amount);
		}
	}
}