package arcana.blocks;

import net.minecraft.block.MapColor;
import net.minecraft.sound.BlockSoundGroup;

public record Material(MapColor color, BlockSoundGroup sounds, boolean crushable){
	
	// TODO: remove entirely
	public static final Material
			WOOD = new Material(MapColor.BROWN, BlockSoundGroup.WOOD, false),
			AMETHYST = new Material(MapColor.PINK, BlockSoundGroup.AMETHYST_CLUSTER, false),
			PLANT = new Material(MapColor.GREEN, BlockSoundGroup.GRASS, false),
			DECORATION = new Material(MapColor.BROWN, BlockSoundGroup.STONE, true),
			STONE = new Material(MapColor.STONE_GRAY, BlockSoundGroup.STONE, false),
			METAL = new Material(MapColor.WHITE_GRAY, BlockSoundGroup.METAL, false),
			GLASS = new Material(MapColor.WHITE, BlockSoundGroup.GLASS, false),
			SOIL = new Material(MapColor.BROWN, BlockSoundGroup.ROOTED_DIRT, false),
			AGGREGATE = new Material(MapColor.BROWN, BlockSoundGroup.SAND, false),
			SNOW_BLOCK = new Material(MapColor.WHITE, BlockSoundGroup.SNOW, false),
			SOLID_ORGANIC = new Material(MapColor.GREEN, BlockSoundGroup.GRASS, false),
			LEAVES = new Material(MapColor.DARK_GREEN, BlockSoundGroup.CHERRY_LEAVES, true);
}