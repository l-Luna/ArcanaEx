package arcana.cca_components;

import arcana.util.NbtUtil;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;
import org.ladysnake.cca.api.v3.component.Component;
import org.ladysnake.cca.api.v3.component.ComponentKey;
import org.ladysnake.cca.api.v3.component.ComponentRegistryV3;

import java.util.*;

import static arcana.Arcana.arcId;

// TODO: use level component/share across dimensions(?)
public class MagicMirrorQueue implements Component{
	
	private record Entry(UUID senderId, ItemStack stack){}
	
	public static final ComponentKey<MagicMirrorQueue> KEY = ComponentRegistryV3.INSTANCE.getOrCreate(arcId("magic_mirror_queue"), MagicMirrorQueue.class);
	
	private final Map<UUID, List<Entry>> queues = new HashMap<>();
	
	//
	
	public static MagicMirrorQueue from(World world){
		return world.getComponent(KEY);
	}
	
	public void push(UUID targetTag, UUID selfId, ItemStack stack){
		queues.merge(targetTag, new ArrayList<>(List.of(new Entry(selfId, stack))), (stacks, incoming) -> {
			stacks.addAll(incoming);
			return stacks;
		});
	}
	
	@Nullable
	public ItemStack pull(UUID selfTag, UUID selfId){
		List<Entry> queue = queues.get(selfTag);
		if(queue != null && !queue.isEmpty()){
			for(int i = 0; i < queue.size(); i++){
				Entry entry = queue.get(i);
				if(!entry.senderId.equals(selfId)){
					queue.remove(i);
					return entry.stack;
				}
			}
		}
		return null;
	}
	
	//
	
	public void readFromNbt(NbtCompound tag, RegistryWrapper.WrapperLookup lookup){
		queues.clear();
		for(NbtElement queueElem : tag.getList("queues", NbtElement.COMPOUND_TYPE)){
			if(!(queueElem instanceof NbtCompound it))
				continue;
			queues.put(it.getUuid("target"), NbtUtil.readMutList(it, "queue", nbt ->
					new Entry(nbt.getUuid("sender_id"), ItemStack.fromNbtOrEmpty(lookup, nbt.getCompound("stack")))));
		}
	}
	
	public void writeToNbt(NbtCompound tag, RegistryWrapper.WrapperLookup lookup){
		NbtList queues = this.queues.entrySet().stream()
				.map(x -> NbtUtil.from(Map.of(
						"target", x.getKey(),
						"queue", x.getValue().stream()
								.map(e -> NbtUtil.from(Map.of(
										"stack", e.stack.encode(lookup),
										"sender_id", e.senderId
								)))
								.collect(NbtUtil.toNbtList())
				)))
				.collect(NbtUtil.toNbtList());
		tag.put("queues", queues);
	}
}