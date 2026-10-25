package arcana.util;

import com.mojang.serialization.Codec;
import io.netty.util.collection.IntObjectHashMap;
import net.minecraft.inventory.Inventory;
import net.minecraft.inventory.InventoryChangedListener;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtOps;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.registry.RegistryWrapper;
import org.jetbrains.annotations.Nullable;

import java.util.Map;

public class MnemonicInventoryData implements InventoryChangedListener{
	
	public static final Codec<MnemonicInventoryData> CODEC = CodecUtil.assocListCodec(Codec.INT, ItemStack.CODEC)
			.xmap(MnemonicInventoryData::new, MnemonicInventoryData::getRememberedShapes);
	
	public static final PacketCodec<RegistryByteBuf, MnemonicInventoryData> PACKET_CODEC = PacketCodecs
			.map(IntObjectHashMap::new, PacketCodecs.VAR_INT, ItemStack.PACKET_CODEC)
			.xmap(MnemonicInventoryData::new, MnemonicInventoryData::getRememberedShapes);
	
	private final IntObjectHashMap<ItemStack> rememberedShapes = new IntObjectHashMap<>();
	
	public MnemonicInventoryData(){}
	
	public MnemonicInventoryData(Map<Integer, ItemStack> shapes){
		rememberedShapes.putAll(shapes);
	}
	
	public void onInventoryChanged(Inventory inventory){
		for(int slot = 0; slot < inventory.size(); slot++){
			ItemStack there = inventory.getStack(slot);
			if(!there.isEmpty())
				rememberedShapes.put(slot, there.copyWithCount(1));
		}
	}
	
	public IntObjectHashMap<ItemStack> getRememberedShapes(){
		return rememberedShapes;
	}
	
	public boolean filter(int slot, ItemStack stack){
		return rememberedShapes.containsKey(slot) && ItemStack.areItemsAndComponentsEqual(rememberedShapes.get(slot), stack);
	}
	
	public void clearRememberedStack(int slot){
		rememberedShapes.remove(slot);
	}
	
	public void handleRequest(@Nullable String tag, int index){
		if("clear_slot".equals(tag))
			clearRememberedStack(index);
	}
	
	public NbtElement toNbt(RegistryWrapper.WrapperLookup lookup){
		return CODEC.encodeStart(lookup.getOps(NbtOps.INSTANCE), this).getOrThrow();
	}
	
	public void readNbt(NbtElement nbt, RegistryWrapper.WrapperLookup lookup){
		MnemonicInventoryData read = CODEC.decode(lookup.getOps(NbtOps.INSTANCE), nbt).getOrThrow().getFirst();
		rememberedShapes.clear();
		rememberedShapes.putAll(read.rememberedShapes);
	}
}