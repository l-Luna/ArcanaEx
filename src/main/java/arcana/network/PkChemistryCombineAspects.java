package arcana.network;

import arcana.ReflectivelyUtilized;
import arcana.aspects.Aspect;
import arcana.aspects.AspectMap;
import arcana.aspects.Aspects;
import arcana.items.components.ArcanaItemComponentTypes;
import arcana.screens.ResearchTableScreen;
import com.unascribed.lib39.tunnel.api.C2SMessage;
import com.unascribed.lib39.tunnel.api.NetworkContext;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;

public class PkChemistryCombineAspects extends C2SMessage{
	
	Identifier left, right;
	
	@ReflectivelyUtilized
	public PkChemistryCombineAspects(NetworkContext ctx){
		super(ctx);
	}
	
	public PkChemistryCombineAspects(Aspect left, Aspect right){
		super(Networking.context);
		this.left = left.id();
		this.right = right.id();
	}
	
	protected void handle(ServerPlayerEntity player){
		// update research notes NBT
		ScreenHandler handler = player.currentScreenHandler;
		if(handler instanceof ResearchTableScreen.Handler rtsh){
			ItemStack notes = rtsh.slots.get(37).getStack();
			NbtCompound puzzleData = notes.get(ArcanaItemComponentTypes.RESEARCH_NOTE_PUZZLE_DATA);
			AspectMap stored = AspectMap.fromNbt(puzzleData.getCompound("stored_aspects"));
			Aspect leftAsp = Aspects.byName(left), rightAsp = Aspects.byName(right);
			
			if(leftAsp != null && rightAsp != null && stored.contains(leftAsp) && stored.contains(rightAsp)){
				Aspects.combined(leftAsp, rightAsp).ifPresent(combined -> {
					stored.take(leftAsp, 1);
					stored.take(rightAsp, 1);
					stored.add(combined, 1);
				});
			}
			
			puzzleData.put("stored_aspects", stored.toNbt());
			rtsh.updateToClient();
		}
	}
}