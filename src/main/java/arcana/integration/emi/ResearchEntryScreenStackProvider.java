package arcana.integration.emi;

import arcana.components.Researcher;
import arcana.research.Entry;
import arcana.research.Requirement;
import arcana.research.requirements.ItemRequirement;
import arcana.research.requirements.ItemTagRequirement;
import arcana.screens.ResearchEntryScreen;
import dev.emi.emi.api.EmiStackProvider;
import dev.emi.emi.api.stack.EmiStackInteraction;
import dev.emi.emi.api.stack.ItemEmiStack;
import dev.emi.emi.api.stack.TagEmiIngredient;
import net.minecraft.client.MinecraftClient;
import net.minecraft.item.ItemStack;

import java.util.List;

import static arcana.screens.ResearchEntryScreen.bgHeight;

public class ResearchEntryScreenStackProvider implements EmiStackProvider<ResearchEntryScreen>{
	
	public EmiStackInteraction getStackAt(ResearchEntryScreen screen, int mouseX, int mouseY){
		Researcher r = Researcher.from(MinecraftClient.getInstance().player);
		Entry entry = screen.getEntry();
		if(r.entryStage(entry) < entry.sections().size() && entry.sections().get(r.entryStage(entry)).getRequirements().size() > 0){
			List<Requirement> requirements = entry.sections().get(r.entryStage(entry)).getRequirements();
			int y = (screen.height - bgHeight) / 2 + 175;
			int reqSize = 20;
			int baseX = (screen.width / 2) - (reqSize * requirements.size() / 2);
			for(int i = 0, size = requirements.size(); i < size; i++)
				if(mouseX >= reqSize * i + baseX && mouseX <= reqSize * i + baseX + reqSize && mouseY >= y && mouseY <= y + reqSize)
					return forRequirement(requirements.get(i));
		}
		return EmiStackInteraction.EMPTY;
	}
	
	private static EmiStackInteraction forRequirement(Requirement requirement){
		if(requirement instanceof ItemRequirement ir)
			return new EmiStackInteraction(ItemEmiStack.of(new ItemStack(ir.getItem(), ir.getAmount())), null, false);
		else if(requirement instanceof ItemTagRequirement itr)
			return new EmiStackInteraction(new TagEmiIngredient(itr.getTag(), itr.getAmount()), null, false);
		return EmiStackInteraction.EMPTY;
	}
}