package arcana.items;

import arcana.client.research.requirements.PuzzleRequirementRenderer;
import arcana.components.Researcher;
import arcana.research.Puzzle;
import arcana.research.Research;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
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
	public void appendTooltip(ItemStack stack, @Nullable World world, List<Text> tooltip, TooltipContext context){
		super.appendTooltip(stack, world, tooltip, context);
		var nbt = stack.getNbt();
		if(nbt != null)
			if(nbt.contains("puzzle_id"))
				for(MutableText text
						: PuzzleRequirementRenderer.tooltipForPuzzle(Research.getPuzzle(new Identifier(nbt.getString("puzzle_id")))))
					tooltip.add(text.formatted(Formatting.AQUA));
	}
	
	public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand){
		if(complete){
			var stack = user.getStackInHand(hand);
			var nbt = stack.getNbt();
			if(nbt != null)
				if(nbt.contains("puzzle_id")){
					Puzzle puzzle = Research.getPuzzle(new Identifier(nbt.getString("puzzle_id")));
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