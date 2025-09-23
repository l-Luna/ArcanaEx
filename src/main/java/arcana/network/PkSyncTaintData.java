package arcana.network;

import arcana.ReflectivelyUtilized;
import arcana.aura.Taint;
import com.unascribed.lib39.tunnel.api.NetworkContext;
import com.unascribed.lib39.tunnel.api.S2CMessage;
import net.minecraft.block.Block;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.tag.TagKey;
import net.minecraft.util.Identifier;
import net.minecraft.util.Pair;
import net.minecraft.util.registry.Registry;

public class PkSyncTaintData extends S2CMessage{
	
	NbtCompound taintMaps = new NbtCompound();
	NbtCompound untaintMaps = new NbtCompound();
	
	@ReflectivelyUtilized
	public PkSyncTaintData(NetworkContext ctx){
		super(ctx);
	}
	
	public PkSyncTaintData(){
		super(Networking.context);
		Taint.TAINT_MAP.forEach((k, v) -> taintMaps.putString(Registry.BLOCK.getId(k).toString(), Registry.BLOCK.getId(v).toString()));
		Taint.TAINT_TAGS.forEach(p -> taintMaps.putString("#" + p.getLeft().id().toString(), Registry.BLOCK.getId(p.getRight()).toString()));
		Taint.UNTAINT_MAP.forEach((k, v) -> untaintMaps.putString(Registry.BLOCK.getId(k).toString(), Registry.BLOCK.getId(v).toString()));
		Taint.UNTAINT_TAGS.forEach(p -> untaintMaps.putString("#" + p.getLeft().id().toString(), Registry.BLOCK.getId(p.getRight()).toString()));
	}
	
	protected void handle(MinecraftClient client, ClientPlayerEntity player){
		Taint.resetMappings();
		for(String key : taintMaps.getKeys()){
			Block output = Registry.BLOCK.get(new Identifier(taintMaps.getString(key)));
			if(key.startsWith("#"))
				Taint.TAINT_TAGS.add(new Pair<>(TagKey.of(Registry.BLOCK_KEY, new Identifier(key.substring(1))), output));
			else
				Taint.TAINT_MAP.put(Registry.BLOCK.get(new Identifier(key)), output);
		}
		for(String key : untaintMaps.getKeys()){
			Block output = Registry.BLOCK.get(new Identifier(untaintMaps.getString(key)));
			if(key.startsWith("#"))
				Taint.UNTAINT_TAGS.add(new Pair<>(TagKey.of(Registry.BLOCK_KEY, new Identifier(key.substring(1))), output));
			else
				Taint.UNTAINT_MAP.put(Registry.BLOCK.get(new Identifier(key)), output);
		}
	}
}