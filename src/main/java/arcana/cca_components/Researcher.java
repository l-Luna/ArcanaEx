package arcana.cca_components;

import arcana.api.WarpingItem;
import arcana.aura.AuraWorld;
import arcana.aura.NodeTypes;
import arcana.enchantments.ArcanaEnchantmentComponents;
import arcana.items.FocusItem;
import arcana.research.*;
import arcana.util.InventoryUtil;
import arcana.util.NbtUtil;
import com.mojang.datafixers.util.Pair;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.enchantment.EnchantmentLevelBasedValue;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;
import net.minecraft.nbt.NbtString;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.registry.Registries;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Box;
import org.ladysnake.cca.api.v3.component.Component;
import org.ladysnake.cca.api.v3.component.ComponentKey;
import org.ladysnake.cca.api.v3.component.ComponentRegistryV3;
import org.ladysnake.cca.api.v3.component.sync.AutoSyncedComponent;

import java.util.*;
import java.util.stream.Collectors;

import static arcana.Arcana.arcId;

// handles tracking research progress and research book preferences
// not synced to other clients
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
	private final Set<Identifier> castFoci = new HashSet<>();
	
	// warp level
	private int warp;
	
	// warp event tracking
	private long lastWarpEventTime = -1;
	private boolean wasPrecursor = false;
	
	private boolean firstSync = true;
	
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
	
	public boolean canAccess(Entry entry){
		// TODO: check parents, or mark accessibility more explicitly than stages.contains
		// since 0 is the default value already
		return stages.containsKey(entry.id());
	}
	
	public int getCompletedVisiblePuzzleCount(){
		return (int)completedPuzzles.stream()
				.map(Research::getPuzzle)
				.filter(Objects::nonNull)
				.filter(Puzzle::visible)
				.count();
	}
	
	public int getCastFociCount(){
		return castFoci.size();
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
			// mark this entry as having some progress for unread indicators
			if(!stages.containsKey(entry.id()))
				stages.put(entry.id(), 0);
			EntrySection sections = entry.sections().get(entryStage(entry));
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
		}while(entryStage(entry) < entry.sections().size() && entry.sections().get(entryStage(entry)).getRequirements().isEmpty());
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
	
	public void markFocusCast(FocusItem focus){
		castFoci.add(Registries.ITEM.getId(focus));
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
	
	public void uncompleteAddendum(Addendum addendum){
		completedAddenda.remove(addendum.id());
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
		int total = InventoryUtil.streamAllItems(player).mapToInt(x -> warpFromStack(x, player)).sum();
		// find bonus warp by eldritch nodes
		Box nodeBox = new Box(player.getPos().add(4, 4, 4), player.getPos().subtract(4, 4, 4));
		total += (int)AuraWorld.from(player.getWorld())
				.getNodesInBounds(nodeBox)
				.stream()
				.filter(x -> x.getType() == NodeTypes.ELDRITCH)
				.count();
		return total;
	}
	
	private static int warpFromStack(ItemStack stack, PlayerEntity player){
		float total = 0;
		if(!stack.isEmpty()){
			Pair<List<EnchantmentLevelBasedValue>, Integer> warpingEffect = EnchantmentHelper.getEffectListAndLevel(stack, ArcanaEnchantmentComponents.WARPING);
			if(warpingEffect != null){
				List<EnchantmentLevelBasedValue> first = warpingEffect.getFirst();
				for(EnchantmentLevelBasedValue value : first)
					total += value.getValue(warpingEffect.getSecond());
				if(stack.getItem() instanceof WarpingItem wi)
					total += wi.warping(stack, player);
			}
		}
		return (int)total;
	}
	
	public void readFromNbt(NbtCompound tag, RegistryWrapper.WrapperLookup lookup){
		warp = tag.getInt("warp");
		lastWarpEventTime = tag.getLong("last_warp_event_time");
		wasPrecursor = tag.getBoolean("was_precursor");
		
		stages.clear();
		NbtCompound entries = tag.getCompound("stages");
		for(String key : entries.getKeys())
			stages.put(Identifier.of(key), entries.getInt(key));
		
		pinned.clear();
		NbtCompound pins = tag.getCompound("pins");
		for(String key : pins.getKeys())
			pinned.put(Identifier.of(key), Arrays.stream(pins.getIntArray(key)).boxed().collect(Collectors.toCollection(ArrayList::new)));
		
		completedPuzzles.clear();
		for(NbtElement puzzle : tag.getList("puzzles", NbtElement.STRING_TYPE))
			completedPuzzles.add(Identifier.of(puzzle.asString()));
		
		completedAddenda.clear();
		for(NbtElement addendum : tag.getList("addenda", NbtElement.STRING_TYPE))
			completedAddenda.add(Identifier.of(addendum.asString()));
		
		castFoci.clear();
		for(NbtElement addendum : tag.getList("cast_foci", NbtElement.STRING_TYPE))
			castFoci.add(Identifier.of(addendum.asString()));
	}
	
	public void writeToNbt(NbtCompound tag, RegistryWrapper.WrapperLookup lookup){
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
		tag.put("cast_foci", castFoci.stream().map(x -> NbtString.of(x.toString())).collect(NbtUtil.toNbtList()));
	}
	
	public void applySyncPacket(RegistryByteBuf buf){
		// make a list of all new entries and addenda, and pass those to the client
		Set<Identifier> oldAddenda = new HashSet<>(completedAddenda);
		Map<Identifier, Integer> oldStages = new HashMap<>(stages);

		AutoSyncedComponent.super.applySyncPacket(buf);
		
		if(!firstSync){
			Set<Addendum> newAddenda = completedAddenda.stream()
					.filter(x -> !oldAddenda.contains(x))
					.map(Research::getAddendum)
					.collect(Collectors.toSet());
			Set<Entry> newEntries = stages.entrySet().stream()
					.filter(x -> x.getValue() > oldStages.getOrDefault(x.getKey(), -1))
					.map(Map.Entry::getKey)
					.map(Research::getEntry)
					.collect(Collectors.toSet());
			postResearchUpdate(player, newAddenda, newEntries);
		}
		
		firstSync = false;
	}
	
	private void postResearchUpdate(PlayerEntity player, Set<Addendum> newAddenda, Set<Entry> newEntries){
		if(player.getWorld().isClient){
			try{
				Class.forName("arcana.client.ArcanaClient").getMethod("postResearchUpdate", Set.class, Set.class).invoke(null, newAddenda, newEntries);
			}catch(Exception e){
				e.printStackTrace();
			}
		}
	}
	
	public boolean shouldSyncWith(ServerPlayerEntity player){
		return player == this.player;
	}
}