package arcana.blocks;

import net.fabricmc.fabric.api.object.builder.v1.block.type.BlockSetTypeBuilder;
import net.fabricmc.fabric.api.object.builder.v1.block.type.WoodTypeBuilder;
import net.minecraft.block.BlockSetType;
import net.minecraft.block.WoodType;

import static arcana.Arcana.arcId;

public class ArcanaBlockSetTypes{
	
	public static final BlockSetType GENERIC_WOOD = BlockSetTypeBuilder.copyOf(BlockSetType.OAK).register(arcId("generic_wood"));
	public static final BlockSetType GENERIC_STONE = BlockSetTypeBuilder.copyOf(BlockSetType.STONE).register(arcId("generic_stone"));
	
	public static final WoodType SILVERWOOD = new WoodTypeBuilder().build(arcId("silverwood"), GENERIC_WOOD);
	public static final WoodType GREATWOOD = new WoodTypeBuilder().build(arcId("greatwood"), GENERIC_WOOD);
	public static final WoodType TAINTWOOD = new WoodTypeBuilder().build(arcId("taintwood"), GENERIC_WOOD);
	public static final WoodType HOLLOWED = new WoodTypeBuilder().build(arcId("hollowed"), GENERIC_WOOD);
}