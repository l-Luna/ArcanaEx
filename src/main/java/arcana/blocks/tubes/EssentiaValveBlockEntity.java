package arcana.blocks.tubes;

import arcana.ArcanaRegistry;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.util.math.BlockPos;

public class EssentiaValveBlockEntity extends EssentiaTubeBlockEntity{
	
	public boolean disabledManually = false, disabledByRedstone = false;
	public long lastChangedTick = -1; // just for visual effects
	
	public EssentiaValveBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state){
		super(type, pos, state);
	}
	
	public EssentiaValveBlockEntity(BlockPos pos, BlockState state){
		super(ArcanaRegistry.ESSENTIA_VALVE_BE, pos, state);
	}
	
	public void updateChangedTick(){
		lastChangedTick = world.getTime();
	}
	
	public boolean enabled(){
		return !(disabledManually || disabledByRedstone);
	}
	
	protected void writeNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup registryLookup){
		super.writeNbt(nbt, registryLookup);
		nbt.putBoolean("disabledManually", disabledManually);
		nbt.putBoolean("disabledByRedstone", disabledByRedstone);
	}
	
	public void readNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup registryLookup){
		super.readNbt(nbt, registryLookup);
		disabledManually = nbt.getBoolean("disabledManually");
		disabledByRedstone = nbt.getBoolean("disabledByRedstone");
	}
}