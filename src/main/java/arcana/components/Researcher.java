package arcana.components;

import arcana.research.Entry;
import arcana.research.Puzzle;
import dev.onyxstudios.cca.api.v3.component.Component;
import dev.onyxstudios.cca.api.v3.component.ComponentKey;
import dev.onyxstudios.cca.api.v3.component.ComponentRegistryV3;
import dev.onyxstudios.cca.api.v3.component.sync.AutoSyncedComponent;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.util.Identifier;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.HashMap;
import java.util.Map;

import static arcana.Arcana.arcId;

@ParametersAreNonnullByDefault
public final class Researcher implements Component, AutoSyncedComponent{
	
	public static final ComponentKey<Researcher> KEY = ComponentRegistryV3.INSTANCE.getOrCreate(arcId("researcher"), Researcher.class);
	
	public static Researcher from(PlayerEntity entity){
		return entity.getComponent(KEY);
	}
	
	private final PlayerEntity player;
	private final Map<Identifier, Integer> stages = new HashMap<>();
	
	public Researcher(PlayerEntity player){
		this.player = player;
	}
	
	public PlayerEntity getPlayer(){
		return player;
	}
	
	public int entryStage(Entry entry){
		return stages.getOrDefault(entry.id(), 0);
	}
	
	public boolean isPuzzleComplete(Puzzle puzzle){
		return false;
	}
	
	// checks if all requirements are complete, takes requirements if so, and syncs with client if anything did happen
	public void tryAdvance(Entry entry){
		if(entryStage(entry) < entry.sections().size()){
			var sections = entry.sections().get(entryStage(entry));
			if(sections.getRequirements().stream().allMatch(x -> x.satisfiedBy(player))){
				sections.getRequirements().forEach(x -> x.takeFrom(player));
				advanceEntry(entry);
				player.syncComponent(KEY);
			}
		}
	}
	
	public void advanceEntry(Entry entry){
		// we've already checked requirements
		do{
			stages.put(entry.id(), entryStage(entry) + 1);
			// unlock all following stages that have no requirements, too
			// ends up on entry.sections().size(); an entry with 1 section is on stage 0 by default and can be incremented to 1
		}while(entryStage(entry) < entry.sections().size() && entry.sections().get(entryStage(entry)).getRequirements().size() == 0);
		// TODO: auto-progress children entries
	}
	
	public void completePuzzle(Puzzle puzzle){
	
	}
	
	// for commands
	public void resetEntry(Entry entry){
		stages.remove(entry.id());
	}
	
	public void uncompletePuzzle(Puzzle puzzle){
	
	}
	
	public void readFromNbt(NbtCompound tag){
		for(String key : tag.getKeys())
			stages.put(new Identifier(key), tag.getInt(key));
	}
	
	public void writeToNbt(NbtCompound tag){
		stages.forEach((entry, stage) -> tag.putInt(entry.toString(), stage));
	}
	
	public void applySyncPacket(PacketByteBuf buf){
		AutoSyncedComponent.super.applySyncPacket(buf);
		if(player.world.isClient)
			refreshBookUi();
	}
	
	private static void refreshBookUi(){
		try{
			Class.forName("arcana.client.ArcanaClient").getMethod("refreshResearchEntryUi").invoke(null);
		}catch(Exception e){
			e.printStackTrace();
		}
	}
}