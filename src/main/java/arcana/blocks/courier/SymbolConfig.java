package arcana.blocks.courier;

public class SymbolConfig{
	
	public int priority = 0;
	public SymbolType type = SymbolType.SOURCE;
	public boolean forceful = false;
	public RedstoneMode redstoneMode = RedstoneMode.ALWAYS_ACTIVE;
	// max amount to supply a sink / min amount to leave in a source, or -1 if not in effect
	public int quota = -1;
	// filtering?
	
	public enum SymbolType{
		SOURCE, SINK
	}
	
	public enum RedstoneMode{
		ALWAYS_ACTIVE,
		ACTIVE_IF_ON,
		ACTIVE_IF_OFF
	}
}