package arcana.datagen;

import arcana.ArcanaRegistry;
import arcana.aspects.Aspects;
import arcana.blocks.ArcanaBlockSettings;
import arcana.blocks.CrystalClusterBlock;
import net.fabricmc.fabric.api.datagen.v1.FabricDataGenerator;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricBlockLootTableProvider;
import net.minecraft.block.Block;
import net.minecraft.block.FlowerPotBlock;
import net.minecraft.block.SlabBlock;
import net.minecraft.data.server.BlockLootTableGenerator;
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
import net.minecraft.predicate.NumberRange;
import net.minecraft.predicate.StatePredicate;
import net.minecraft.predicate.item.EnchantmentPredicate;
import net.minecraft.predicate.item.ItemPredicate;
import net.minecraft.tag.ItemTags;

import static arcana.ArcanaRegistry.SILVERWOOD_LEAVES;
import static arcana.ArcanaRegistry.SILVERWOOD_SAPLING;

public class ArcanaLootTablesProvider extends FabricBlockLootTableProvider{
	
	private static final float[] saplingDropChance = { .05f, .0625f, .083333336f, .1f };
	
	protected ArcanaLootTablesProvider(FabricDataGenerator dataGenerator){
		super(dataGenerator);
	}
	
	protected void generateBlockLootTables(){
		for(Block block : ArcanaRegistry.blocks)
			if(block.settings instanceof ArcanaBlockSettings abs)
				if(abs.getDropsSelf())
					if(block instanceof SlabBlock)
						addDrop(block, BlockLootTableGenerator::slabDrops);
					else if(block instanceof FlowerPotBlock)
						addPottedPlantDrop(block);
					else
						addDrop(block);
		
		addDrop(SILVERWOOD_LEAVES, leaves -> leavesDrop(leaves, SILVERWOOD_SAPLING, saplingDropChance));
		
		Aspects.clusters.forEach((aspect, cluster) -> {
			var drop = Aspects.crystals.get(aspect);
			addDrop(cluster,
					c -> LootTable.builder().pool(
							LootPool.builder().rolls(ConstantLootNumberProvider.create(1)).with(
									ItemEntry.builder(c)
											// fully grown crystals with silk touch drop themselves
											.conditionally(MatchToolLootCondition.builder(ItemPredicate.Builder.create().enchantment(new EnchantmentPredicate(Enchantments.SILK_TOUCH, NumberRange.IntRange.atLeast(1)))))
											.conditionally(BlockStatePropertyLootCondition.builder(c).properties(StatePredicate.Builder.create().exactMatch(CrystalClusterBlock.size, 3)))
											.alternatively(
													ItemEntry.builder(drop)
															// fully grown crystals drop 2-4 using a pickaxe, with fortune applied
															.apply(SetCountLootFunction.builder(UniformLootNumberProvider.create(2, 4)))
															.apply(ApplyBonusLootFunction.oreDrops(Enchantments.FORTUNE))
															.conditionally(MatchToolLootCondition.builder(ItemPredicate.Builder.create().tag(ItemTags.CLUSTER_MAX_HARVESTABLES)))
															.conditionally(BlockStatePropertyLootCondition.builder(c).properties(StatePredicate.Builder.create().exactMatch(CrystalClusterBlock.size, 3)))
															
															// fully grown crystals otherwise drop 2
															.alternatively(applyExplosionDecay(c, ItemEntry.builder(drop).apply(SetCountLootFunction.builder(ConstantLootNumberProvider.create(2))))
																	.conditionally(BlockStatePropertyLootCondition.builder(c).properties(StatePredicate.Builder.create().exactMatch(CrystalClusterBlock.size, 3))))
															
															// other crystals drop 1
															.alternatively(applyExplosionDecay(c, ItemEntry.builder(drop).apply(SetCountLootFunction.builder(ConstantLootNumberProvider.create(1)))))
											)
							)
					)
			);
		});
	}
	
	public String getName(){
		return "Arcana Block Loot Tables";
	}
}