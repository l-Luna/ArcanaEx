package arcana.network;

import arcana.ArcanaRegistry;
import arcana.Networking;
import arcana.ReflectivelyUtilized;
import arcana.research.Puzzle;
import arcana.research.Research;
import com.unascribed.lib39.tunnel.api.C2SMessage;
import com.unascribed.lib39.tunnel.api.NetworkContext;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;

public class PkGetNote extends C2SMessage{
	
	Identifier puzzleId;
	
	@ReflectivelyUtilized
	public PkGetNote(NetworkContext ctx){
		super(ctx);
	}
	
	public PkGetNote(Identifier puzzleId){
		super(Networking.arcCtx);
		this.puzzleId = puzzleId;
	}
	
	protected void handle(ServerPlayerEntity player){
		// TODO: check for scribing tools and paper
		Puzzle puzzle = Research.getPuzzle(puzzleId);
		ItemStack noteStack = new ItemStack(ArcanaRegistry.RESEARCH_NOTES);
		var tag = noteStack.getOrCreateNbt();
		tag.putString("puzzle_id", puzzleId.toString());
		tag.put("puzzle_data", puzzle.getInitialNoteTag());
		player.giveItemStack(noteStack);
	}
}