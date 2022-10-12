package arcana.network;

import arcana.ArcanaRegistry;
import arcana.Networking;
import arcana.ReflectivelyUtilized;
import arcana.aspects.Aspect;
import arcana.aspects.AspectMap;
import arcana.aspects.Aspects;
import arcana.research.Research;
import arcana.research.puzzles.Chemistry;
import arcana.screens.ResearchTableScreenHandler;
import com.unascribed.lib39.tunnel.api.C2SMessage;
import com.unascribed.lib39.tunnel.api.NetworkContext;
import com.unascribed.lib39.tunnel.api.annotation.field.Optional;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;

public class PkChemistryClick extends C2SMessage{
	
	String hexId = "";
	
	@Optional
	String toSet = null;
	
	@ReflectivelyUtilized
	public PkChemistryClick(NetworkContext ctx){
		super(ctx);
	}
	
	public PkChemistryClick(String hexId, Aspect toSet){
		super(Networking.arcCtx);
		this.hexId = hexId;
		this.toSet = toSet == null ? null : toSet.id().toString();
	}
	
	protected void handle(ServerPlayerEntity player){
		// update research notes NBT
		ScreenHandler handler = player.currentScreenHandler;
		if(handler instanceof ResearchTableScreenHandler rtsh){
			var notes = rtsh.slots.get(37).getStack();
			var nbt = notes.getNbt();
			if(nbt != null){
				var puzzleData = nbt.getCompound("puzzle_data");
				AspectMap stored = AspectMap.fromNbt(puzzleData.getCompound("stored_aspects"));
				NbtCompound grid = puzzleData.getCompound("grid_aspects");
				Aspect toPlace = toSet == null ? null : Aspects.byName(toSet);
				
				if(toPlace == null)
					grid.remove(hexId);
				else if(stored.contains(toPlace)){
					stored.take(toPlace, 1);
					puzzleData.put("stored_aspects", stored.toNbt());
					grid.putString(hexId, toSet);
				}
				puzzleData.put("grid_aspects", grid); // need to explicitly set in case it didn't exist
				if(((Chemistry)Research.getPuzzle(new Identifier(nbt.getString("puzzle_id")))).validate(puzzleData)){
					ItemStack complete = new ItemStack(ArcanaRegistry.COMPLETE_RESEARCH_NOTES);
					complete.setNbt(notes.getNbt());
					rtsh.slots.get(37).setStack(complete);
				}
				rtsh.updateToClient();
			}
		}
	}
}