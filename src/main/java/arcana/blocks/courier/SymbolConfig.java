package arcana.blocks.courier;

import arcana.util.CodecUtil;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

public class SymbolConfig{
	
	public static Codec<SymbolConfig> CODEC = RecordCodecBuilder.create(i -> i.group(
			Codec.INT.fieldOf("priority").forGetter(x -> x.priority),
			CodecUtil.enumCodec(SymbolDirection.values()).fieldOf("is_source").forGetter(x -> x.direction),
			Codec.BOOL.fieldOf("forceful").forGetter(x -> x.forceful),
			CodecUtil.enumCodec(RedstoneMode.values()).fieldOf("redstone_mode").forGetter(x -> x.redstoneMode),
			Codec.INT.fieldOf("quota").forGetter(x -> x.quota)
	).apply(i, SymbolConfig::new));
	
	public int priority = 0;
	public SymbolDirection direction = SymbolDirection.SOURCE;
	public boolean forceful = false;
	public RedstoneMode redstoneMode = RedstoneMode.ALWAYS_ACTIVE;
	// max amount to supply a sink / min amount to leave in a source, or -1 if not in effect
	public int quota = -1;
	// filtering?
	
	public SymbolConfig(){}
	
	public SymbolConfig(int priority, SymbolDirection direction, boolean forceful, RedstoneMode redstoneMode, int quota){
		this.priority = priority;
		this.direction = direction;
		this.forceful = forceful;
		this.redstoneMode = redstoneMode;
		this.quota = quota;
	}
	
	public enum SymbolDirection{
		SOURCE, SINK
	}
	
	public enum RedstoneMode{
		ALWAYS_ACTIVE,
		ACTIVE_IF_ON,
		ACTIVE_IF_OFF
	}
}