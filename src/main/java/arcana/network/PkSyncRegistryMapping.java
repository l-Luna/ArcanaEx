package arcana.network;

import arcana.ReflectivelyUtilized;
import arcana.aura.Taint;
import arcana.enchantments.LootSwapEnchantment;
import arcana.util.RegistryMapping;
import com.unascribed.lib39.tunnel.api.NetworkContext;
import com.unascribed.lib39.tunnel.api.S2CMessage;
import com.unascribed.lib39.tunnel.api.annotation.field.MarshalledAs;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;

public class PkSyncRegistryMapping extends S2CMessage{
	
	@MarshalledAs("int8")
	byte mappingId;
	NbtCompound nbt = new NbtCompound();
	
	@ReflectivelyUtilized
	public PkSyncRegistryMapping(NetworkContext ctx){
		super(ctx);
	}
	
	public PkSyncRegistryMapping(byte mappingId){
		super(Networking.context);
		this.mappingId = mappingId;
		saveRegistry(getMapping(mappingId));
	}
	
	private <T> void saveRegistry(RegistryMapping<T> mapping){
		if(mapping == null)
			return;
		Registry<T> registry = mapping.getRegistry();
		mapping.getEntryMap().forEach((from, to) -> nbt.putString(registry.getId(from).toString(), registry.getId(to).toString()));
		mapping.getTagMap().forEach((tag, to) -> nbt.putString("#" + tag.id().toString(), registry.getId(to).toString()));
	}
	
	@Environment(EnvType.CLIENT)
	protected void handle(MinecraftClient client, ClientPlayerEntity player){
		handle(getMapping(mappingId));
	}
	
	private <T> void handle(RegistryMapping<T> mapping){
		if(mapping == null)
			return;
		mapping.clear();
		for(String key : nbt.getKeys()){
			Identifier to = Identifier.of(nbt.getString(key));
			if(key.startsWith("#"))
				mapping.addTagEntry(Identifier.of(key.substring(1)), to);
			else
				mapping.addIdentifierEntry(Identifier.of(key), to);
		}
	}
	
	private static RegistryMapping<?> getMapping(byte mappingId){
		return switch(mappingId){
			case 0 -> Taint.TAINT_MAP;
			case 1 -> Taint.UNTAINT_MAP;
			case 2 -> LootSwapEnchantment.PURIFYING_MAP;
			case 3 -> LootSwapEnchantment.TRANSMUTATIVE_MAP;
			default -> null;
		};
	}
}