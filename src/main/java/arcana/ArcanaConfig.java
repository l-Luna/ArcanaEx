package arcana;

import folk.sisby.kaleido.api.WrappedConfig;
import folk.sisby.kaleido.lib.quiltconfig.api.annotations.Comment;
import folk.sisby.kaleido.lib.quiltconfig.api.annotations.FloatRange;

@SuppressWarnings("CanBeFinal")
public class ArcanaConfig extends WrappedConfig{
	
	@Comment("Whether to show \"unsettling\" warp-related visuals")
	@Comment("Gameplay-related visuals are restyled")
	@Comment("On by default")
	public boolean unsettlingWarp = true;
	
	@FloatRange(min=0.5f, max=1f)
	@Comment("Scale of text in research entries")
	@Comment("Relative to your GUI scale; 1 is vanilla text size")
	@Comment("0.7 by default")
	public float textScaling = 0.7f;
}