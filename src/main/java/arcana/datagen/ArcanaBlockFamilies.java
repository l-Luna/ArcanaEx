package arcana.datagen;

import arcana.ArcanaRegistry;
import net.minecraft.data.family.BlockFamilies;
import net.minecraft.data.family.BlockFamily;

import java.util.List;

import static arcana.ArcanaRegistry.*;

public class ArcanaBlockFamilies{
	
	public static final BlockFamily ARCANE_STONE = BlockFamilies.register(ArcanaRegistry.ARCANE_STONE)
			.button(ARCANE_STONE_BUTTON)
			.pressurePlate(ARCANE_STONE_PRESSURE_PLATE)
			.slab(ARCANE_STONE_SLAB)
			.stairs(ARCANE_STONE_STAIRS)
			.wall(ARCANE_STONE_WALL)
			.noGenerateModels()
			.noGenerateRecipes()
			.build();
	
	public static final BlockFamily ARCANE_STONE_BRICKS = BlockFamilies.register(ArcanaRegistry.ARCANE_STONE_BRICKS)
			.button(ARCANE_STONE_BRICKS_BUTTON)
			.pressurePlate(ARCANE_STONE_BRICKS_PRESSURE_PLATE)
			.slab(ARCANE_STONE_BRICKS_SLAB)
			.stairs(ARCANE_STONE_BRICKS_STAIRS)
			.wall(ARCANE_STONE_BRICKS_WALL)
			.noGenerateModels()
			.noGenerateRecipes()
			.build();
	
	public static final BlockFamily SILVERWOOD = BlockFamilies.register(SILVERWOOD_PLANKS)
			.button(SILVERWOOD_BUTTON)
			.fence(SILVERWOOD_FENCE)
			.fenceGate(SILVERWOOD_FENCE_GATE)
			.pressurePlate(SILVERWOOD_PRESSURE_PLATE)
			.sign(SILVERWOOD_SIGN, SILVERWOOD_WALL_SIGN)
			.slab(SILVERWOOD_SLAB)
			.stairs(SILVERWOOD_STAIRS)
			.door(SILVERWOOD_DOOR)
			.trapdoor(SILVERWOOD_TRAPDOOR)
			.group("wooden")
			.unlockCriterionName("has_planks")
			.noGenerateModels()
			.noGenerateRecipes()
			.build();
	
	public static final BlockFamily GREATWOOD = BlockFamilies.register(GREATWOOD_PLANKS)
			.button(GREATWOOD_BUTTON)
			.fence(GREATWOOD_FENCE)
			.fenceGate(GREATWOOD_FENCE_GATE)
			.pressurePlate(GREATWOOD_PRESSURE_PLATE)
			.sign(GREATWOOD_SIGN, GREATWOOD_WALL_SIGN)
			.slab(GREATWOOD_SLAB)
			.stairs(GREATWOOD_STAIRS)
			.door(GREATWOOD_DOOR)
			.trapdoor(GREATWOOD_TRAPDOOR)
			.group("wooden")
			.unlockCriterionName("has_planks")
			.noGenerateModels()
			.noGenerateRecipes()
			.build();
	
	// we handle generating the models and recipes
	public static final List<BlockFamily> ALL = List.of(ARCANE_STONE, ARCANE_STONE_BRICKS, SILVERWOOD, GREATWOOD);
}
