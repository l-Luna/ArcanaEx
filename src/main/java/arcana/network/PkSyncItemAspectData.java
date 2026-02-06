package arcana.network;

import arcana.ReflectivelyUtilized;
import arcana.aspects.AspectMap;
import arcana.aspects.ItemAspectRegistry;
import com.unascribed.lib39.tunnel.api.NetworkContext;
import com.unascribed.lib39.tunnel.api.S2CMessage;
import com.unascribed.lib39.tunnel.api.annotation.field.MarshalledAs;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class PkSyncItemAspectData extends S2CMessage{
	
	public record Entry(Identifier item, AspectMap aspects){
	}
	
	@MarshalledAs("arcana.network.PkSyncItemAspectData$Entry-list")
	List<Entry> entries = new ArrayList<>();
	
	@ReflectivelyUtilized
	public PkSyncItemAspectData(NetworkContext ctx){
		super(ctx);
	}
	
	public PkSyncItemAspectData(){
		super(Networking.context);
		ItemAspectRegistry.getAllItemAspects().forEach((item, stacks) -> entries.add(new Entry(Registry.ITEM.getId(item), stacks)));
	}
	
	@Environment(EnvType.CLIENT)
	protected void handle(MinecraftClient client, ClientPlayerEntity player){
		ItemAspectRegistry.setAllItemAspects(entries.stream().collect(Collectors.toMap(x -> Registry.ITEM.get(x.item), y -> y.aspects)));
	}
}