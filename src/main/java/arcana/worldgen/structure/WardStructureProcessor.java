package arcana.worldgen.structure;

import com.mojang.serialization.MapCodec;
import net.minecraft.structure.processor.StructureProcessor;
import net.minecraft.structure.processor.StructureProcessorType;

public class WardStructureProcessor extends StructureProcessor{
	
	public static final WardStructureProcessor INSTANCE = new WardStructureProcessor();
	public static final MapCodec<WardStructureProcessor> CODEC = MapCodec.unit(INSTANCE);
	public static final StructureProcessorType<WardStructureProcessor> TYPE = () -> CODEC;
	
	private WardStructureProcessor(){}
	
	protected StructureProcessorType<?> getType(){
		return TYPE;
	}
}