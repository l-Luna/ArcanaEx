package arcana.client.research;

import arcana.client.research.requirements.ItemRequirementRenderer;
import arcana.client.research.requirements.ItemTagRequirementRenderer;
import arcana.client.research.requirements.PuzzleRequirementRenderer;
import arcana.client.research.requirements.XpRequirementRenderer;
import arcana.research.Requirement;
import arcana.research.requirements.ItemRequirement;
import arcana.research.requirements.ItemTagRequirement;
import arcana.research.requirements.PuzzleRequirement;
import arcana.research.requirements.XpRequirement;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public interface RequirementRenderer<T extends Requirement>{
	
	// registry
	
	Map<Identifier, RequirementRenderer<?>> renderers = new LinkedHashMap<>();
	
	static void setup(){
		renderers.put(ItemRequirement.TYPE, new ItemRequirementRenderer());
		renderers.put(ItemTagRequirement.TYPE, new ItemTagRequirementRenderer());
		renderers.put(XpRequirement.TYPE, new XpRequirementRenderer());
		renderers.put(PuzzleRequirement.TYPE, new PuzzleRequirementRenderer());
	}
	
	@SuppressWarnings("unchecked")
	static <T extends Requirement> RequirementRenderer<T> get(Identifier type){
		return (RequirementRenderer<T>)renderers.get(type);
	}
	
	@SuppressWarnings("unchecked")
	static <T extends Requirement> RequirementRenderer<T> get(T requirement){
		return (RequirementRenderer<T>)renderers.get(requirement.type());
	}
	
	//
	
	void render(MatrixStack matrices, int x, int y, T requirement, int time, float delta);
	
	List<? extends Text> tooltip(T requirement, int time);
	
	default boolean shouldDrawTickOrCross(T requirement, int amount){
		return amount == 1;
	}
	
	default MinecraftClient client(){
		return MinecraftClient.getInstance();
	}
}