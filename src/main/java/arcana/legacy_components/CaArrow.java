package arcana.legacy_components;

import dev.onyxstudios.cca.api.v3.component.Component;
import dev.onyxstudios.cca.api.v3.component.ComponentKey;
import dev.onyxstudios.cca.api.v3.component.ComponentRegistryV3;
import dev.onyxstudios.cca.api.v3.component.sync.AutoSyncedComponent;
import net.minecraft.entity.projectile.ArrowEntity;
import net.minecraft.nbt.NbtCompound;

import static arcana.Arcana.arcId;

// tracks arrows that have been shot from a Crimson Longbow
public class CaArrow implements Component, AutoSyncedComponent{
	
	public static ComponentKey<CaArrow> KEY = ComponentRegistryV3.INSTANCE.getOrCreate(arcId("ca_arrow"), CaArrow.class);
	
	private boolean projected = false;
	
	public static boolean isProjected(ArrowEntity ae){
		return ae.getComponent(KEY).projected;
	}
	
	public static void setProjected(ArrowEntity ae, boolean projected){
		ae.getComponent(KEY).projected = projected;
	}
	
	public void readFromNbt(NbtCompound tag){
		if(tag.contains("projected"))
			projected = tag.getBoolean("projected");
	}
	
	public void writeToNbt(NbtCompound tag){
		tag.putBoolean("projected", projected);
	}
}