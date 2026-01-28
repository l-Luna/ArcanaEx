package arcana.components;

import arcana.util.NbtUtil;
import dev.onyxstudios.cca.api.v3.component.Component;
import dev.onyxstudios.cca.api.v3.component.ComponentKey;
import dev.onyxstudios.cca.api.v3.component.ComponentRegistryV3;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static arcana.Arcana.arcId;

public class MagicMirrorQueue implements Component{
	
	public static final ComponentKey<MagicMirrorQueue> KEY = ComponentRegistryV3.INSTANCE.getOrCreate(arcId("magic_mirror_queue"), MagicMirrorQueue.class);
	
	private final Map<BlockPos, List<ItemStack>> queues = new HashMap<>();
	
	//
	
	public static MagicMirrorQueue from(World world){
		return world.getComponent(KEY);
	}
	
	public void push(BlockPos targetPos, ItemStack stack){
		queues.merge(targetPos, new ArrayList<>(List.of(stack)), (stacks, incoming) -> {
			stacks.addAll(incoming);
			return stacks;
		});
	}
	
	@Nullable
	public ItemStack pull(BlockPos pos){
		List<ItemStack> queue = queues.get(pos);
		if(queue != null && !queue.isEmpty()){
			if(queue.size() == 1)
				queues.remove(pos);
			return queue.remove(0);
		}
		return null;
	}
	
	//
	
	public void readFromNbt(NbtCompound tag){
		queues.clear();
		for(NbtElement queueElem : tag.getList("queues", NbtElement.COMPOUND_TYPE)){
			if(!(queueElem instanceof NbtCompound it))
				continue;
			BlockPos pos = BlockPos.fromLong(it.getLong("pos"));
			List<ItemStack> items = NbtUtil.readMutList(it, "queue", ItemStack::fromNbt);
			queues.put(pos, items);
		}
	}
	
	public void writeToNbt(NbtCompound tag){
		NbtList queues = this.queues.entrySet().stream()
				.map(x -> NbtUtil.from(Map.of(
						"pos", x.getKey().asLong(),
						"queue", x.getValue().stream()
								.map(s -> s.writeNbt(new NbtCompound()))
								.collect(NbtUtil.toNbtList())
				)))
				.collect(NbtUtil.toNbtList());
		tag.put("queues", queues);
	}
}