package arcana.items;

import arcana.cca_components.Researcher;
import arcana.client.research.requirements.PuzzleRequirementRenderer;
import arcana.items.components.ArcanaDataComponents;
import arcana.research.Puzzle;
import arcana.research.Research;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.tooltip.TooltipType;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Hand;
import net.minecraft.util.Identifier;
import net.minecraft.util.TypedActionResult;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class ResearchNotesItem extends Item{
	
	private final boolean complete;
	
	public ResearchNotesItem(Settings settings, boolean complete){
		super(settings);
		this.complete = complete;
	}
	
	@Environment(EnvType.CLIENT) // must access I18n to provide alternative translations
	public void appendTooltip(ItemStack stack, @Nullable TooltipContext ctx, List<Text> tooltip, TooltipType type){
		super.appendTooltip(stack, ctx, tooltip, type);
		Identifier puzzleId = stack.getOrDefault(ArcanaDataComponents.RESEARCH_NOTE_PUZZLE_ID, null);
		if(puzzleId != null){
			Puzzle puzzle = Research.getPuzzle(puzzleId);
			if(puzzle != null)
				for(MutableText text : PuzzleRequirementRenderer.tooltipForPuzzle(puzzle))
					tooltip.add(text.formatted(Formatting.AQUA));
		}
	}
	
	public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand){
		if(complete){
			ItemStack stack = user.getStackInHand(hand);
			Identifier puzzleId = stack.getOrDefault(ArcanaDataComponents.RESEARCH_NOTE_PUZZLE_ID, null);
			if(puzzleId != null){
				Puzzle puzzle = Research.getPuzzle(puzzleId);
				Researcher researcher = Researcher.from(user);
				researcher.completePuzzle(puzzle);
				if(!user.isCreative())
					stack.decrement(1);
				return TypedActionResult.success(stack);
			}
		}
		return super.use(world, user, hand);
	}
}