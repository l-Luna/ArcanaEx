package arcana.items;

import arcana.components.Researcher;
import arcana.research.Research;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;
import net.minecraft.nbt.NbtString;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Hand;
import net.minecraft.util.Identifier;
import net.minecraft.util.TypedActionResult;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public class TomeOfSharingItem extends Item{
	
	public TomeOfSharingItem(Settings settings){
		super(settings);
	}
	
	public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand){
		Researcher researcher = Researcher.from(user);
		ItemStack tome = user.getStackInHand(hand);
		if(user.isSneaking()){
			// teach it everything they know
			NbtCompound researchTag = tome.getOrCreateSubNbt("research");
			var boundResearch = getBoundResearch(tome);
			researcher.getAllResearch().forEach((entry, stage) -> {
				if(boundResearch.getOrDefault(entry, 0) < stage)
					researchTag.putInt(entry.toString(), stage);
			});
			NbtList puzzlesList = tome.getOrCreateNbt().getList("puzzles", NbtElement.STRING_TYPE);
			var boundPuzzles = getBoundPuzzles(tome);
			for(Identifier puzzle : researcher.getAllCompletedPuzzles())
				if(!boundPuzzles.contains(puzzle))
					puzzlesList.add(NbtString.of(puzzle.toString()));
			if(!tome.getNbt().contains("puzzles"))
				tome.getNbt().put("puzzles", puzzlesList);
			return TypedActionResult.success(tome);
		}else{
			// teach them every puzzle it knows
			for(Identifier identifier : getBoundPuzzles(user.getStackInHand(hand)))
				researcher.completePuzzle(Research.getPuzzle(identifier));
			return TypedActionResult.success(tome);
		}
	}
	
	@Environment(EnvType.CLIENT)
	public void appendTooltip(ItemStack stack, @Nullable World world, List<Text> tooltip, TooltipContext context){
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
		if(tome.isEmpty())
			return Map.of();
		NbtCompound researchTag = tome.getSubNbt("research");
		if(researchTag != null){
			Map<Identifier, Integer> research = new HashMap<>(researchTag.getKeys().size());
			for(String key : researchTag.getKeys())
				research.put(new Identifier(key), researchTag.getInt(key));
			return research;
		}
		return Map.of();
	}
	
	public static Set<Identifier> getBoundPuzzles(ItemStack tome){
		NbtCompound tag = tome.getNbt();
		if(tag != null){
			NbtList puzzleList = tag.getList("puzzles", NbtElement.STRING_TYPE);
			Set<Identifier> puzzles = new HashSet<>(puzzleList.size());
			for(NbtElement element : puzzleList)
				puzzles.add(new Identifier(element.asString())); // NbtString returns its value
			return puzzles;
		}
		return Set.of();
	}
}