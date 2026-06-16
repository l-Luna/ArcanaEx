package arcana.network;

import arcana.ArcanaRegistry;
import arcana.ReflectivelyUtilized;
import arcana.aspects.Aspect;
import arcana.aspects.AspectMap;
import arcana.aspects.Aspects;
import arcana.cca_components.Researcher;
import arcana.items.components.ArcanaItemComponentTypes;
import arcana.research.BuiltinResearch;
import arcana.research.Research;
import arcana.research.puzzles.Chemistry;
import arcana.screens.ResearchTableScreen;
import com.unascribed.lib39.tunnel.api.C2SMessage;
import com.unascribed.lib39.tunnel.api.NetworkContext;
import com.unascribed.lib39.tunnel.api.annotation.field.Optional;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.server.network.ServerPlayerEntity;

public class PkChemistryClick extends C2SMessage{
	
	String hexId = "";
	
	@Optional
	String toSet = null;
	
	@ReflectivelyUtilized
	public PkChemistryClick(NetworkContext ctx){
		super(ctx);
	}
	
	public PkChemistryClick(String hexId, Aspect toSet){
		super(Networking.context);
		this.hexId = hexId;
		this.toSet = toSet == null ? null : toSet.id().toString();
	}
	
	protected void handle(ServerPlayerEntity player){
		// update research notes NBT
		ScreenHandler handler = player.currentScreenHandler;
		if(handler instanceof ResearchTableScreen.Handler rtsh){
			ItemStack notes = rtsh.slots.get(37).getStack();
			if(!notes.isEmpty()){
				Chemistry puzzle = (Chemistry)Research.getPuzzle(notes.get(ArcanaItemComponentTypes.RESEARCH_NOTE_PUZZLE_ID));
				// TODO: validate missing spaces
				NbtCompound puzzleData =notes.get(ArcanaItemComponentTypes.RESEARCH_NOTE_PUZZLE_DATA);
				AspectMap stored = AspectMap.fromNbt(puzzleData.getCompound("stored_aspects"));
				NbtCompound grid = puzzleData.getCompound("grid_aspects");
				Aspect toPlace = toSet == null ? null : Aspects.byName(toSet);
				Aspect toReplace = grid.contains(hexId) ? Aspects.byName(grid.getString(hexId)) : null;
				boolean hasExpertise = Researcher.from(player).isEntryComplete(Research.getEntry(BuiltinResearch.researchExpertiseEntry));
				boolean hasMastery = Researcher.from(player).isEntryComplete(Research.getEntry(BuiltinResearch.researchMasteryEntry));
				float returnChance = hasMastery ? 0.5f : hasExpertise ? 0.25f : 0;
				
				if(toPlace == null){
					grid.remove(hexId);
					if(toReplace != null && hasExpertise && player.getWorld().random.nextFloat() < returnChance){
						stored.add(toReplace, 1);
						puzzleData.put("stored_aspects", stored.toNbt());
					}
				}else if(stored.contains(toPlace)){
					stored.take(toPlace, 1);
					puzzleData.put("stored_aspects", stored.toNbt());
					grid.putString(hexId, toSet);
					if(toReplace != null && hasExpertise && player.getWorld().random.nextFloat() < returnChance){
						stored.add(toReplace, 1);
						puzzleData.put("stored_aspects", stored.toNbt());
					}
				}
				puzzleData.put("grid_aspects", grid); // need to explicitly set in case it didn't exist
				
				if(puzzle.validate(puzzleData)){
					ItemStack complete = new ItemStack(ArcanaRegistry.COMPLETE_RESEARCH_NOTES);
					complete.set(ArcanaItemComponentTypes.RESEARCH_NOTE_PUZZLE_ID, notes.get(ArcanaItemComponentTypes.RESEARCH_NOTE_PUZZLE_ID));
					complete.set(ArcanaItemComponentTypes.RESEARCH_NOTE_PUZZLE_DATA, notes.get(ArcanaItemComponentTypes.RESEARCH_NOTE_PUZZLE_DATA));
					rtsh.slots.get(37).setStack(complete);
				}
				rtsh.updateToClient();
			}
		}
	}
}