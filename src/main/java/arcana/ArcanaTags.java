package arcana;

import net.minecraft.block.Block;
import net.minecraft.fluid.Fluid;
import net.minecraft.tag.TagKey;
import net.minecraft.util.registry.Registry;

import static arcana.Arcana.arcId;

public final class ArcanaTags{
	
	public static final TagKey<Block> CRUCIBLE_HEATING_BLOCKS = TagKey.of(Registry.BLOCK_KEY, arcId("crucible_heating_blocks"));
	public static final TagKey<Fluid> CRUCIBLE_HEATING_FLUIDS = TagKey.of(Registry.FLUID_KEY, arcId("crucible_heating_fluids"));
}