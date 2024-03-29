package arcana.components;

import arcana.ArcanaRegistry;
import arcana.items.WarpingItem;
import arcana.nodes.NodeTypes;
import arcana.research.Entry;
import arcana.research.Parent;
import arcana.research.Puzzle;
import arcana.research.Research;
import dev.onyxstudios.cca.api.v3.component.Component;
import dev.onyxstudios.cca.api.v3.component.ComponentKey;
import dev.onyxstudios.cca.api.v3.component.ComponentRegistryV3;
import dev.onyxstudios.cca.api.v3.component.sync.AutoSyncedComponent;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;
import net.minecraft.nbt.NbtString;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Box;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.*;
import java.util.stream.Collectors;

import static arcana.Arcana.arcId;

@ParametersAreNonnullByDefault
public final class Researcher implements Component, AutoSyncedComponent{
	
	public static final ComponentKey<Researcher> KEY = ComponentRegistryV3.INSTANCE.getOrCreate(arcId("researcher"), Researcher.class);
	
	public static Researcher from(PlayerEntity entity){
		return entity.getComponent(KEY);
	}
	
	// the player
	private final PlayerEntity player;
	
	// research progress
	private final Map<Identifier, Integer> stages = new HashMap<>();
	private final Map<Identifier, ArrayList<Integer>> pinned = new HashMap<>();
	private final Set<Identifier> completedPuzzles = new HashSet<>();
	
	// warp level
	private int warp;
	
	// warp event tracking
	private long lastWarpEventTime = -1;
	private boolean wasPrecursor = false;
	
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
		return completedPuzzles.contains(puzzle.id());
	}
	
	public int getWarp(){
		return warp;
	}
	
	public void setWarp(int warp){
		this.warp = warp;
	}
	
	public boolean isEntryComplete(Entry entry){
		return entryStage(entry) == entry.sections().size();
	}
	
	public int getCompletedPuzzleCount(){
		return completedPuzzles.size();
	}
	
	// checks if all requirements are complete, takes requirements if so, and syncs with client if anything did happen
	public void tryAdvance(Entry entry){
		if(entryStage(entry) < entry.sections().size()){
			for(Parent parent : entry.parents()){
				Entry pEntry = Research.getEntry(parent.id());
				if(parent.stage() == -1){
					if(!isEntryComplete(pEntry))
						return; // missing a parent that must be complete
				}else if(entryStage(pEntry) < parent.stage())
					return; // not enough progress on that parent
			}
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
		if(isEntryComplete(entry)){
			int warping = entry.warping();
			if(warping > 0 && warping <= 5) // anything out of this range doesn't get displayed, so it's unfair to add
				warp += warping;
		}
		// auto-progress unlockable children entries
		Research.streamChildrenOf(entry).forEach(x -> {
			Parent parent = x.getRight();
			if((parent.stage() != -1 && parent.stage() <= entryStage(entry)) || (parent.stage() == -1 && isEntryComplete(entry)))
				tryAdvance(x.getLeft());
		});
	}
	
	public void completePuzzle(Puzzle puzzle){
		completedPuzzles.add(puzzle.id());
	}
	
	public long getLastWarpEventTime(){
		return lastWarpEventTime;
	}
	
	public boolean wasLastWarpEventPrecursor(){
		return wasPrecursor;
	}
	
	public void setLastWarpEvent(long time, boolean wasPrecursor){
		lastWarpEventTime = time;
		this.wasPrecursor = wasPrecursor;
	}
	
	public Map<Identifier, Integer> getAllResearch(){
		return stages;
	}
	
	public Set<Identifier> getAllCompletedPuzzles(){
		return completedPuzzles;
	}
	
	// for commands
	public void completeEntry(Entry entry){
		if(!isEntryComplete(entry)){
			int warping = entry.warping();
			if(warping > 0 && warping <= 5)
				warp += warping;
		}
		stages.put(entry.id(), entry.sections().size());
	}
	
	public void reset(){
		stages.clear();
		completedPuzzles.clear();
		warp = 0;
	}
	
	public void resetEntry(Entry entry){
		if(isEntryComplete(entry)){
			int warping = entry.warping();
			if(warping > 0 && warping <= 5)
				warp -= warping;
		}
		stages.remove(entry.id());
	}
	
	public void uncompletePuzzle(Puzzle puzzle){
		completedPuzzles.remove(puzzle.id());
	}
	
	public Map<Identifier, ? extends List<Integer>> getPinned(){
		return pinned;
	}
	
	public void addPinned(Identifier entry, int stage){
		List<Integer> stages = pinned.computeIfAbsent(entry, k -> new ArrayList<>(1));
		if(!stages.contains(stage))
			stages.add(stage);
	}
	
	public void removePinned(Identifier entry, Integer stage){
		List<Integer> integers = pinned.get(entry);
		if(integers == null)
			return;
		integers.remove(stage);
		if(integers.isEmpty())
			pinned.remove(entry);
	}
	
	public void doSync(){
		KEY.sync(player);
	}
	
	public int getEffectiveWarp(){
		return getWarp() + bonusWarp(player);
	}
	
	public static int bonusWarp(PlayerEntity player){
		// get warping from items
		int total = 0;
		for(int i = 0; i < player.getInventory().size(); i++){
			ItemStack stack = player.getInventory().getStack(i);
			if(!stack.isEmpty()){
				total += EnchantmentHelper.getLevel(ArcanaRegistry.WARPING, stack);
				if(stack.getItem() instanceof WarpingItem wi)
					total += wi.warping(stack, player);
			}
		}
		// find bonus warp by eldritch nodes
		Box nodeBox = new Box(player.getPos().add(4, 4, 4), player.getPos().subtract(4, 4, 4));
		total += AuraWorld.from(player.world)
				.getNodesInBounds(nodeBox)
				.stream()
				.filter(x -> x.getType() == NodeTypes.ELDRITCH)
				.count();
		return total;
	}
	
	public void readFromNbt(NbtCompound tag){
		warp = tag.getInt("warp");
		lastWarpEventTime = tag.getLong("last_warp_event_time");
		wasPrecursor = tag.getBoolean("was_precursor");
		
		NbtCompound entries = tag.getCompound("stages");
		for(String key : entries.getKeys())
			stages.put(new Identifier(key), entries.getInt(key));
		
		NbtCompound pins = tag.getCompound("pins");
		for(String key : pins.getKeys())
			pinned.put(new Identifier(key), Arrays.stream(pins.getIntArray(key)).boxed().collect(Collectors.toCollection(ArrayList::new)));
		
		NbtList puzzles = tag.getList("puzzles", NbtElement.STRING_TYPE);
		for(NbtElement puzzle : puzzles)
			completedPuzzles.add(new Identifier(puzzle.asString()));
	}
	
	public void writeToNbt(NbtCompound tag){
		tag.putInt("warp", warp);
		tag.putLong("last_warp_event_time", lastWarpEventTime);
		tag.putBoolean("was_precursor", wasPrecursor);
		
		NbtCompound stagesTag = new NbtCompound();
		stages.forEach((entry, stage) -> stagesTag.putInt(entry.toString(), stage));
		tag.put("stages", stagesTag);
		
		NbtCompound pinsTag = new NbtCompound();
		getPinned().forEach((entry, pins) -> pinsTag.putIntArray(entry.toString(), pins));
		tag.put("pins", pinsTag);
		
		NbtList puzzlesTag = new NbtList();
		completedPuzzles.forEach(x -> puzzlesTag.add(NbtString.of(x.toString())));
		tag.put("puzzles", puzzlesTag);
	}
	
	public void applySyncPacket(PacketByteBuf buf){
		preResearchUpdate(player);
		AutoSyncedComponent.super.applySyncPacket(buf);
		postResearchUpdate(player);
	}
	
	private static void preResearchUpdate(PlayerEntity player){
		if(player.world.isClient)
			try{
				Class.forName("arcana.client.ArcanaClient").getMethod("preResearchUpdate").invoke(null);
			}catch(Exception e){
				e.printStackTrace();
			}
	}
	
	private static void postResearchUpdate(PlayerEntity player){
		if(player.world.isClient)
			try{
				Class.forName("arcana.client.ArcanaClient").getMethod("postResearchUpdate").invoke(null);
			}catch(Exception e){
				e.printStackTrace();
			}
	}
}