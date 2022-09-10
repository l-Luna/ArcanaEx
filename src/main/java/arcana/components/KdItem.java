package arcana.components;

import dev.onyxstudios.cca.api.v3.component.Component;
import dev.onyxstudios.cca.api.v3.component.ComponentKey;
import dev.onyxstudios.cca.api.v3.component.ComponentRegistryV3;
import dev.onyxstudios.cca.api.v3.component.sync.AutoSyncedComponent;
import net.minecraft.entity.ItemEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.util.math.BlockPos;
import org.jetbrains.annotations.Nullable;

import javax.annotation.ParametersAreNonnullByDefault;

import static arcana.Arcana.arcId;

// tracks the Knowledgeable Dropper that dropped this ItemEntity
@ParametersAreNonnullByDefault
public class KdItem implements Component, AutoSyncedComponent{
	
	public static ComponentKey<KdItem> KEY = ComponentRegistryV3.INSTANCE.getOrCreate(arcId("kd_item"), KdItem.class);
	
	@Nullable
	private BlockPos pos = null;
	
	@Nullable
	public static BlockPos getSource(ItemEntity ie){
		return ie.getComponent(KEY).pos;
	}
	
	public static void setSource(ItemEntity ie, @Nullable BlockPos pos){
		ie.getComponent(KEY).pos = pos;
	}
	
	public void readFromNbt(NbtCompound tag){
		if(tag.contains("pos"))
			pos = BlockPos.fromLong(tag.getLong("pos"));
	}
	
	public void writeToNbt(NbtCompound tag){
		if(pos != null)
			tag.putLong("pos", pos.asLong());
	}
}