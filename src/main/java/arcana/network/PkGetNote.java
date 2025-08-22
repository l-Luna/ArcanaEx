package arcana.network;

import arcana.ArcanaRegistry;
import arcana.ArcanaTags;
import arcana.ReflectivelyUtilized;
import arcana.research.Puzzle;
import arcana.research.Research;
import com.unascribed.lib39.tunnel.api.C2SMessage;
import com.unascribed.lib39.tunnel.api.NetworkContext;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
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
		if(!canGetNote(player))
			return;
		Puzzle puzzle = Research.getPuzzle(puzzleId);
		ItemStack noteStack = new ItemStack(ArcanaRegistry.RESEARCH_NOTES);
		var tag = noteStack.getOrCreateNbt();
		tag.putString("puzzle_id", puzzleId.toString());
		tag.put("puzzle_data", puzzle.getInitialNoteTag(player));
		if(!player.giveItemStack(noteStack)){
			ItemEntity itemEntity = player.dropItem(noteStack, false);
			if(itemEntity != null){
				itemEntity.resetPickupDelay();
				itemEntity.setOwner(player.getUuid());
			}
		}
		
		player.getInventory().remove(x -> x.isOf(Items.PAPER), 1, player.playerScreenHandler.getCraftingInput());
		for(int i = 0; i < player.getInventory().size(); i++){
			ItemStack stack = player.getInventory().getStack(i);
			if(stack.isIn(ArcanaTags.SCRIBING_TOOLS)){
				stack.damage(1, player, __ -> {});
				break;
			}
		}
	}
	
	public static boolean canGetNote(PlayerEntity player){
		return player.getInventory().contains(ArcanaTags.SCRIBING_TOOLS)
				&& player.getInventory().contains(new ItemStack(Items.PAPER));
	}
}