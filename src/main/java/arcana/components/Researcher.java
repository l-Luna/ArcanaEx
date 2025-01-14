package arcana.components;

import arcana.ArcanaRegistry;
import arcana.aura.AuraWorld;
import arcana.aura.NodeTypes;
import arcana.items.WarpingItem;
import arcana.research.*;
import arcana.util.NbtUtil;
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

import java.util.*;
import java.util.stream.Collectors;

import static arcana.Arcana.arcId;

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
	private final Set<Identifier> completedAddenda = new HashSet<>();
	
	// notifying of new addenda
	// this is set to always be one resync out of date (or null if there are no syncs), to allow the client to see new addenda
	private Set<Identifier> oldAddenda;
	
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
	
	public boolean isPuzzleComplete(Identifier id){
		return completedPuzzles.contains(id);
	}
	
	public boolean isAddendumComplete(Addendum addendum){
		return completedAddenda.contains(addendum.id());
	}
	
	public boolean isAddendumComplete(Identifier addendum){
		return completedAddenda.contains(addendum);
	}
	
	public int getWarp(){
		return warp;
	}
	
	public void setWarp(int warp){
		this.warp = warp;
	}
	
	public boolean isEntryComplete(Entry entry){
		// >= to handle entries being removed later
		return entryStage(entry) >= entry.sections().size();
	}
	
	public int getCompletedVisiblePuzzleCount(){
		return (int)completedPuzzles.stream()
				.map(Research::getPuzzle)
				.filter(Puzzle::visible)
				.count();
	}
	
	// checks if all requirements are complete, takes requirements if so, and syncs with client if anything did happen
	public void tryAdvance(Entry entry, boolean onlyFree){
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
			List<Requirement> reqs = sections.getRequirements();
			if(onlyFree ? reqs.isEmpty() : reqs.stream().allMatch(x -> x.satisfiedBy(player))){
				reqs.forEach(x -> x.takeFrom(player));
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
				tryAdvance(x.getLeft(), true);
		});
	}
	
	public void completePuzzle(Puzzle puzzle){
		completedPuzzles.add(puzzle.id());
	}
	
	public void completePuzzle(Identifier puzzle){
		completedPuzzles.add(puzzle);
	}
	
	public void completeAddendum(Addendum addendum){
		completedAddenda.add(addendum.id());
	}
	
	public void completeAddendum(Identifier addendum){
		completedAddenda.add(addendum);
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
		
		stages.clear();
		NbtCompound entries = tag.getCompound("stages");
		for(String key : entries.getKeys())
			stages.put(new Identifier(key), entries.getInt(key));
		
		pinned.clear();
		NbtCompound pins = tag.getCompound("pins");
		for(String key : pins.getKeys())
			pinned.put(new Identifier(key), Arrays.stream(pins.getIntArray(key)).boxed().collect(Collectors.toCollection(ArrayList::new)));
		
		completedPuzzles.clear();
		for(NbtElement puzzle : tag.getList("puzzles", NbtElement.STRING_TYPE))
			completedPuzzles.add(new Identifier(puzzle.asString()));
		
		completedAddenda.clear();
		for(NbtElement addendum : tag.getList("addenda", NbtElement.STRING_TYPE))
			completedAddenda.add(new Identifier(addendum.asString()));
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
		
		tag.put("addenda", completedAddenda.stream().map(x -> NbtString.of(x.toString())).collect(NbtUtil.toNbtList()));
	}
	
	public void applySyncPacket(PacketByteBuf buf){
		preResearchUpdate(player);
		AutoSyncedComponent.super.applySyncPacket(buf);
		postResearchUpdate(player);
		oldAddenda = new HashSet<>(completedAddenda);
	}
	
	private static void preResearchUpdate(PlayerEntity player){
		if(player.world.isClient)
			try{
				Class.forName("arcana.client.ArcanaClient").getMethod("preResearchUpdate").invoke(null);
			}catch(Exception e){
				e.printStackTrace();
			}
	}
	
	private void postResearchUpdate(PlayerEntity player){
		if(player.world.isClient){
			Set<Identifier> newAddenda;
			if(oldAddenda != null){
				newAddenda = new HashSet<>(completedAddenda);
				newAddenda.removeAll(oldAddenda);
			}else
				newAddenda = Set.of();
			try{
				Class.forName("arcana.client.ArcanaClient").getMethod("postResearchUpdate", Set.class).invoke(null, newAddenda);
			}catch(Exception e){
				e.printStackTrace();
			}
		}
	}
}