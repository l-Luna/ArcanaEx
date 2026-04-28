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
	
	@Comment("Always show an outline around the hovered position in research books")
	@Comment("Off by default")
	public boolean alwaysShowResearchBookCursor = false;
	
	@Comment("Taint and flux related options, server-only")
	public TaintConfig taintConfig = new TaintConfig();
	
	public static class TaintConfig implements Section{
		@Comment("Amount of flux a chunk must have before flux can spread out of it")
		@Comment("20 by default")
		public float fluxSpreadThreshold = 20;
		@Comment("Difference of flux required between two chunks for flux to spread from one to another")
		@Comment("12 by default")
		public float fluxSpreadRelativeThreshold = 12;
		@Comment("Amount of flux a chunk must have before infestation or taint can spread")
		@Comment("20 by default")
		public float taintSpreadThreshold = 20;
		
		@Comment("Amount of flux consumed to spawn a tainted node")
		@Comment("80 by default")
		public float taintedNodeSpawnCost = 80;
		@Comment("Amount of flux consumed to infest a block")
		@Comment("5 by default")
		public float infestCost = 5;
		@Comment("Amount of flux consumed to convert an infest block into its tainted form")
		@Comment("12 by default")
		public float taintCost = 12;
		
		@Comment("Inverse probability of a tainted node producing flux per tick")
		@Comment("90 by default")
		public int taintedNodeFluxInvChance = 90;
		@Comment("Inverse probability of a tainted node infesting a nearby block per tick")
		@Comment("300 by default")
		public int taintedNodeInfestInvChance = 90;
		@Comment("Inverse probability of infestation or taint attempting to spread per tick")
		@Comment("10 by default")
		public int taintSpreadInvChance = 10;
	}
}