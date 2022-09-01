package arcana;

import arcana.blocks.*;
import arcana.items.*;
import arcana.screens.ArcaneCraftingScreenHandler;
import net.fabricmc.fabric.api.client.itemgroup.FabricItemGroupBuilder;
import net.fabricmc.fabric.api.object.builder.v1.block.entity.FabricBlockEntityTypeBuilder;
import net.minecraft.block.Block;
import net.minecraft.block.Material;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.Item.Settings;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.util.registry.Registry;

import java.util.ArrayList;
import java.util.List;

import static arcana.Arcana.arcId;
import static net.fabricmc.fabric.api.object.builder.v1.block.FabricBlockSettings.of;

public final class ArcanaRegistry{
	
	public static class Tab{
		// tfw "illegal forward reference"
		public static final ItemGroup ARCANA = FabricItemGroupBuilder.build(
				arcId("group"),
				() -> new ItemStack(IRON_WAND_CAP)
		);
	}
	
	public static final Settings GROUPED = new Settings().group(Tab.ARCANA);
	
	public static final CapItem IRON_WAND_CAP = new CapItem(GROUPED);
	public static final CapItem COPPER_WAND_CAP = new CapItem(GROUPED);
	public static final CapItem GOLD_WAND_CAP = new CapItem(GROUPED);
	public static final Cap MISSING_CAP = new Cap.Impl(arcId("missing"));
	
	public static final CoreItem BONE_WAND_CORE = new CoreItem(GROUPED);
	public static final CoreItem BLAZE_WAND_CORE = new CoreItem(GROUPED);
	public static final CoreItem ICE_WAND_CORE = new CoreItem(GROUPED);
	public static final Core STICK_CORE = new Core.Impl(arcId("stick_wand_core"));
	public static final Core MISSING_CORE = new Core.Impl(arcId("missing"));
	
	public static final Item WAND = new WandItem(GROUPED);
	
	public static final Item ARCANUM = new ResearchBookItem(GROUPED, arcId("arcanum"));
	public static final Item CRIMSON_RITES = new ResearchBookItem(GROUPED, arcId("crimson_rites"));
	public static final Item TAINTED_CODEX = new ResearchBookItem(GROUPED, arcId("tainted_codex"));
	
	public static final Block ARCANE_CRAFTING_TABLE = new ArcaneCraftingTableBlock(of(Material.WOOD).nonOpaque());
	public static final Block CRUCIBLE = new CrucibleBlock(of(Material.METAL).nonOpaque());
	public static final Block RESEARCH_TABLE = new ResearchTableBlock(of(Material.WOOD).nonOpaque().strength(3));
	
	public static final ScreenHandlerType<ArcaneCraftingScreenHandler> ARCANE_CRAFTING_SCREEN_HANDLER
			= new ScreenHandlerType<>(ArcaneCraftingScreenHandler::new);
	
	public static BlockEntityType<CrucibleBlockEntity> CRUCIBLE_BE = FabricBlockEntityTypeBuilder
			.create(CrucibleBlockEntity::new, CRUCIBLE)
			.build();
	public static BlockEntityType<ResearchTableBlockEntity> RESEARCH_TABLE_BE = FabricBlockEntityTypeBuilder
			.create(ResearchTableBlockEntity::new, RESEARCH_TABLE)
			.build();
	
	public static final List<Item> ITEMS = new ArrayList<>();
	public static final List<Block> BLOCKS = new ArrayList<>();
	
	public static void setup(){
		// items + wand components
		register("iron_wand_cap", IRON_WAND_CAP);
		register("copper_wand_cap", COPPER_WAND_CAP);
		register("gold_wand_cap", GOLD_WAND_CAP);
		registerCapOnly(MISSING_CAP);
		
		register("bone_wand_core", BONE_WAND_CORE);
		register("blaze_wand_core", BLAZE_WAND_CORE);
		register("ice_wand_core", ICE_WAND_CORE);
		registerCoreOnly(STICK_CORE);
		registerCoreOnly(MISSING_CORE);
		
		register("wand", WAND);
		
		register("arcanum", ARCANUM);
		register("crimson_rites", CRIMSON_RITES);
		register("tainted_codex", TAINTED_CODEX);
		
		// blocks
		register("arcane_crafting_table", ARCANE_CRAFTING_TABLE);
		register("crucible", CRUCIBLE);
		register("research_table", RESEARCH_TABLE, false);
		register("research_table", new ResearchTableItem(GROUPED)); // it's a block item, it doesn't count
		
		// screen handlers
		register("arcane_crafting", ARCANE_CRAFTING_SCREEN_HANDLER);
		
		// block entities
		register("crucible", CRUCIBLE_BE);
		register("research_table", RESEARCH_TABLE_BE);
	}
	
	private static void register(String name, Item item){
		Registry.register(Registry.ITEM, arcId(name), item);
		ITEMS.add(item);
		if(item instanceof Cap c)
			registerCapOnly(c);
		if(item instanceof Core c)
			registerCoreOnly(c);
	}
	
	private static void register(String name, Block block){
		register(name, block, true);
	}
	
	private static void register(String name, Block block, boolean andItem){
		Registry.register(Registry.BLOCK, arcId(name), block);
		BLOCKS.add(block);
		if(andItem)
			register(name, new BlockItem(block, GROUPED));
	}
	
	private static void register(String name, ScreenHandlerType<?> type){
		Registry.register(Registry.SCREEN_HANDLER, arcId(name), type);
	}
	
	private static void register(String name, BlockEntityType<?> type){
		Registry.register(Registry.BLOCK_ENTITY_TYPE, arcId(name), type);
	}
	
	private static void registerCapOnly(Cap cap){
		Cap.caps.put(cap.id(), cap);
	}
	
	private static void registerCoreOnly(Core core){
		Core.cores.put(core.id(), core);
	}
}