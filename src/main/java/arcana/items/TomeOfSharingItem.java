package arcana.items;

import arcana.cca_components.Researcher;
import arcana.items.components.ArcanaItemComponentTypes;
import arcana.items.components.ResearchCompletionComponent;
import arcana.research.Research;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.tooltip.TooltipType;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Hand;
import net.minecraft.util.Identifier;
import net.minecraft.util.TypedActionResult;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Map;
import java.util.Set;

public class TomeOfSharingItem extends Item{
	
	public TomeOfSharingItem(Settings settings){
		super(settings.component(ArcanaItemComponentTypes.RESEARCH_COMPLETION, ResearchCompletionComponent.DEFAULT));
	}
	
	public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand){
		Researcher researcher = Researcher.from(user);
		ItemStack tome = user.getStackInHand(hand);
		if(user.isSneaking()){
			// teach it everything they know
			ResearchCompletionComponent originalCompletion = tome.get(ArcanaItemComponentTypes.RESEARCH_COMPLETION);
			ResearchCompletionComponent newCompletion = originalCompletion.mutableCopy();
			Map<Identifier, Integer> boundResearch = originalCompletion.stages();
			researcher.getAllResearch().forEach((entry, stage) -> {
				if(boundResearch.getOrDefault(entry, 0) < stage)
					newCompletion.stages().put(entry, stage);
			});
			Set<Identifier> boundPuzzles = originalCompletion.puzzles();
			for(Identifier puzzle : researcher.getAllCompletedPuzzles())
				if(!boundPuzzles.contains(puzzle))
					newCompletion.puzzles().add(puzzle);
			tome.set(ArcanaItemComponentTypes.RESEARCH_COMPLETION, newCompletion);
			return TypedActionResult.success(tome);
		}else{
			// teach them every puzzle it knows
			for(Identifier identifier : getBoundPuzzles(user.getStackInHand(hand)))
				researcher.completePuzzle(Research.getPuzzle(identifier));
			return TypedActionResult.success(tome);
		}
	}
	
	@Environment(EnvType.CLIENT)
	public void appendTooltip(ItemStack stack, @Nullable TooltipContext ctx, List<Text> tooltip, TooltipType type){
		if(getBoundPuzzles(stack).isEmpty() && getBoundResearch(stack).isEmpty()){
			tooltip.add(Text.translatable("item.arcana.tome_of_sharing.unbound").formatted(Formatting.AQUA));
			return;
		}
		
		// if it has even one puzzle you don't, it knows more
		Researcher r = Researcher.from(MinecraftClient.getInstance().player);
		for(Identifier puzzle : getBoundPuzzles(stack))
			if(!r.isPuzzleComplete(Research.getPuzzle(puzzle))){
				tooltip.add(Text.translatable("item.arcana.tome_of_sharing.bound.greater").formatted(Formatting.AQUA));
				return;
			}
		
		tooltip.add(Text.translatable("item.arcana.tome_of_sharing.bound.lesser").formatted(Formatting.AQUA));
	}
	
	// mirror Researcher: store entry stages and completed puzzles
	// entry stages are used by the Knowledgeable Dropper, puzzles are used for sharing functionality
	// we don't store player UUIDs since the player could be offline, but droppers should still work
	
	public static Map<Identifier, Integer> getBoundResearch(ItemStack tome){
		return tome.get(ArcanaItemComponentTypes.RESEARCH_COMPLETION).stages();
	}
	
	public static Set<Identifier> getBoundPuzzles(ItemStack tome){
		return tome.get(ArcanaItemComponentTypes.RESEARCH_COMPLETION).puzzles();
	}
}