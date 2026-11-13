package arcana.blocks.courier;

import arcana.blocks.courier.symbols.InventorySymbolStorage;
import arcana.blocks.courier.symbols.PedestalItemSymbolStorage;
import arcana.blocks.courier.symbols.VoidingSymbolStorage;
import com.mojang.serialization.Codec;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

import static arcana.Arcana.arcId;

public record SymbolType(Identifier id, Function<SymbolContext, @Nullable SymbolStorage> storage, boolean adjustableArea){
	
	public static Map<Identifier, SymbolType> TYPES = new HashMap<>();
	
	public static final SymbolType INVENTORY = register(new SymbolType(arcId("inventory"), InventorySymbolStorage::fromContext, false));
	public static final SymbolType PEDESTAL_ITEM = register(new SymbolType(arcId("pedestal_item"), PedestalItemSymbolStorage::new, false));
	public static final SymbolType VOID = register(new SymbolType(arcId("void"), __ -> new VoidingSymbolStorage(), false));
	
	public static Codec<SymbolType> CODEC = Identifier.CODEC.xmap(TYPES::get, SymbolType::id);
	
	private static SymbolType register(SymbolType type){
		TYPES.put(type.id, type);
		return type;
	}
}