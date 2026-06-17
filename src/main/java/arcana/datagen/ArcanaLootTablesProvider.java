package arcana.datagen;

import arcana.ArcanaRegistry;
import arcana.aspects.Aspect;
import arcana.aspects.Aspects;
import arcana.blocks.ArcanaBlockSettings;
import arcana.blocks.CrystalClusterBlock;
import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricBlockLootTableProvider;
import net.minecraft.block.Block;
import net.minecraft.block.FlowerPotBlock;
import net.minecraft.block.SlabBlock;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.loot.LootPool;
import net.minecraft.loot.LootTable;
import net.minecraft.loot.condition.BlockStatePropertyLootCondition;
import net.minecraft.loot.condition.MatchToolLootCondition;
import net.minecraft.loot.entry.ItemEntry;
import net.minecraft.loot.function.ApplyBonusLootFunction;
import net.minecraft.loot.function.SetCountLootFunction;
import net.minecraft.loot.provider.number.ConstantLootNumberProvider;
import net.minecraft.loot.provider.number.UniformLootNumberProvider;
import net.minecraft.predicate.StatePredicate;
import net.minecraft.predicate.item.ItemPredicate;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.registry.tag.ItemTags;

import java.util.concurrent.CompletableFuture;

import static arcana.ArcanaRegistry.*;

public class ArcanaLootTablesProvider extends FabricBlockLootTableProvider{
	
	private static final float[] saplingDropChance = { .05f, .0625f, .083333336f, .1f };
	
	protected ArcanaLootTablesProvider(FabricDataOutput gen, CompletableFuture<RegistryWrapper.WrapperLookup> lookupFuture){
		super(gen, lookupFuture);
	}
	
	public void generate(){
		RegistryWrapper.Impl<Enchantment> enchantments = registryLookup.getWrapperOrThrow(RegistryKeys.ENCHANTMENT);
		
		for(Block block : ArcanaRegistry.blocks)
			if(block.getSettings() instanceof ArcanaBlockSettings abs)
				if(abs.getDropsSelf())
					if(block instanceof SlabBlock)
						addDrop(block, this::slabDrops);
					else if(block instanceof FlowerPotBlock)
						addPottedPlantDrops(block);
		
		addDrop(SILVERWOOD_LEAVES, leaves -> leavesDrops(leaves, SILVERWOOD_SAPLING, saplingDropChance));
		addDrop(GREATWOOD_LEAVES, leaves -> leavesDrops(leaves, GREATWOOD_SAPLING, saplingDropChance));
		
		addDrop(SILVERWOOD_DOOR, this::doorDrops);
		addDrop(GREATWOOD_DOOR, this::doorDrops);
		
		addDrop(GREATWOOD_SLAB, this::slabDrops);
		
		addPottedPlantDrops(POTTED_SILVERWOOD_SAPLING);
		addPottedPlantDrops(POTTED_GREATWOOD_SAPLING);
		addPottedPlantDrops(POTTED_VISHROOM);
		addPottedPlantDrops(POTTED_CORDISPORA);
		addPottedPlantDrops(POTTED_SNOWDROP);
		addPottedPlantDrops(POTTED_FIREWHEEL);
		addPottedPlantDrops(POTTED_LILIUM);
		
		addDrop(TAINTED_GRASS_BLOCK, it -> drops(it, TAINTED_SOIL));
		
		Aspects.clusters.forEach((aspect, cluster) -> {
			var drop = Aspects.crystals.get(aspect);
			addDrop(cluster,
					c -> LootTable.builder().pool(
							LootPool.builder().rolls(ConstantLootNumberProvider.create(1)).with(
									ItemEntry.builder(c)
											// fully grown crystals with silk touch drop themselves
											.conditionally(createSilkTouchCondition())
											.conditionally(BlockStatePropertyLootCondition.builder(c).properties(StatePredicate.Builder.create().exactMatch(CrystalClusterBlock.SIZE, 3)))
											.alternatively(
													ItemEntry.builder(drop)
															// fully grown crystals drop 2-4 using a pickaxe, with fortune applied
															.apply(SetCountLootFunction.builder(UniformLootNumberProvider.create(2, 4)))
															.apply(ApplyBonusLootFunction.oreDrops(enchantments.getOrThrow(Enchantments.FORTUNE)))
															.conditionally(MatchToolLootCondition.builder(ItemPredicate.Builder.create().tag(ItemTags.CLUSTER_MAX_HARVESTABLES)))
															.conditionally(BlockStatePropertyLootCondition.builder(c).properties(StatePredicate.Builder.create().exactMatch(CrystalClusterBlock.SIZE, 3)))
															
															// fully grown crystals otherwise drop 2
															.alternatively(applyExplosionDecay(c, ItemEntry.builder(drop).apply(SetCountLootFunction.builder(ConstantLootNumberProvider.create(2))))
																	.conditionally(BlockStatePropertyLootCondition.builder(c).properties(StatePredicate.Builder.create().exactMatch(CrystalClusterBlock.SIZE, 3))))
															
															// other crystals drop 1
															.alternatively(applyExplosionDecay(c, ItemEntry.builder(drop).apply(SetCountLootFunction.builder(ConstantLootNumberProvider.create(1)))))
											)
							)
					)
			);
		});
		
		for(Aspect a : Aspects.primals){
			Block block = Aspects.crystalBlocks.get(a);
			addDrop(block, it -> drops(it, Aspects.crystals.get(a), UniformLootNumberProvider.create(0, 1)));
			Block pillarBlock = Aspects.crystalPillars.get(a);
			addDrop(pillarBlock, it -> drops(it, Aspects.crystals.get(a), UniformLootNumberProvider.create(0, 1)));
		}
	}
	
	public String getName(){
		return "Arcana Block Loot Tables";
	}
}