package arcana.components;

import dev.onyxstudios.cca.api.v3.component.Component;
import dev.onyxstudios.cca.api.v3.component.ComponentKey;
import dev.onyxstudios.cca.api.v3.component.ComponentRegistryV3;
import dev.onyxstudios.cca.api.v3.component.sync.AutoSyncedComponent;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.NbtCompound;

import javax.annotation.ParametersAreNonnullByDefault;

import static arcana.Arcana.arcId;

@ParametersAreNonnullByDefault
public final class Researcher implements Component, AutoSyncedComponent{
	
	public static final ComponentKey<Researcher> KEY = ComponentRegistryV3.INSTANCE.getOrCreate(arcId("researcher"), Researcher.class);
	
	public static Researcher from(PlayerEntity entity){
		return entity.getComponent(KEY);
	}
	
	private final PlayerEntity player;
	
	public Researcher(PlayerEntity player){
		this.player = player;
	}
	
	public PlayerEntity getPlayer(){
		return player;
	}
	
	public void readFromNbt(NbtCompound tag){
	
	}
	
	public void writeToNbt(NbtCompound tag){
	
	}
}