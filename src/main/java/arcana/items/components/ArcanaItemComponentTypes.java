package arcana.items.components;

import arcana.ArcanaRegistry;
import arcana.aspects.AspectMap;
import arcana.aspects.AspectStack;
import arcana.aura.NodeType;
import arcana.util.ArrayInventory;
import com.mojang.serialization.Codec;
import net.minecraft.component.ComponentType;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;
import net.minecraft.util.Uuids;

import java.util.UUID;

import static arcana.Arcana.arcId;

public class ArcanaItemComponentTypes{
	
	// initialised before ArcanaRegistry, so items are still nonexistent
	
	// generic
	public static final ComponentType<Integer> RADIUS = create(Codec.INT);
	public static final ComponentType<NodeType> NODE_TYPE = create(NodeType.CODEC);
	public static final ComponentType<ResearchCompletionComponent> RESEARCH_COMPLETION = create(ResearchCompletionComponent.CODEC);
	
	public static final ComponentType<AspectMap> STORED_ASPECTS = create(AspectMap.CODEC);
	public static final ComponentType<AspectStack> STORED_SINGLE_ASPECT = create(AspectStack.CODEC);
	
	// behavioral
	public static final ComponentType<ArcanaRegistry.Tab> SUBTAB = create(Codec.STRING.xmap(ArcanaRegistry.Tab::valueOf, Enum::name));
	public static final ComponentType<FragileComponent> FRAGILE = create(FragileComponent.CODEC);
	
	// item specific
	public static final ComponentType<ArrayInventory> FOCUS_POUCH_INVENTORY = create(ArrayInventory.CODEC);
	
	public static final ComponentType<UUID> MAGIC_MIRROR_TAG = create(Uuids.CODEC);
	public static final ComponentType<UUID> MAGIC_MIRROR_ID = create(Uuids.CODEC);
	public static final ComponentType<Boolean> MAGIC_MIRROR_BUNDLED_FLAG = create(Codec.BOOL);
	
	public static final ComponentType<Identifier> RESEARCH_NOTE_PUZZLE_ID = create(Identifier.CODEC);
	public static final ComponentType<NbtCompound> RESEARCH_NOTE_PUZZLE_DATA = create(NbtCompound.CODEC);
	
	public static final ComponentType<WandDataComponent> WAND_DATA = create(WandDataComponent.CODEC);
	
	public static void setup(){
		register("radius", RADIUS);
		register("node_type", NODE_TYPE);
		register("research_completion", RESEARCH_COMPLETION);
		register("stored_aspects", STORED_ASPECTS);
		register("stored_single_aspect", STORED_SINGLE_ASPECT);
		
		register("subtab", SUBTAB);
		register("fragile", FRAGILE);
		
		register("focus_pouch_inventory", FOCUS_POUCH_INVENTORY);
		register("magic_mirror_tag", MAGIC_MIRROR_TAG);
		register("magic_mirror_id", MAGIC_MIRROR_ID);
		register("magic_mirror_bundled_flag", MAGIC_MIRROR_BUNDLED_FLAG);
		register("research_note_puzzle_id", RESEARCH_NOTE_PUZZLE_ID);
		register("research_note_puzzle_data", RESEARCH_NOTE_PUZZLE_DATA);
		register("wand_data", WAND_DATA);
	}
	
	private static <T> ComponentType<T> create(Codec<T> codec){
		return ComponentType.<T>builder().codec(codec).build();
	}
	
	private static void register(String id, ComponentType<?> type){
		Registry.register(Registries.DATA_COMPONENT_TYPE, arcId(id), type);
	}
}