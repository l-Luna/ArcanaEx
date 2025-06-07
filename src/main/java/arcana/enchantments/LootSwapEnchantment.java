package arcana.enchantments;

import arcana.ArcanaRegistry;
import net.fabricmc.fabric.api.loot.v2.LootTableSource;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.enchantment.EnchantmentTarget;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.loot.LootManager;
import net.minecraft.loot.LootTable;
import net.minecraft.loot.context.LootContext;
import net.minecraft.loot.context.LootContextParameters;
import net.minecraft.loot.context.LootContextType;
import net.minecraft.loot.context.LootContextTypes;
import net.minecraft.loot.function.LootFunction;
import net.minecraft.loot.function.LootFunctionType;
import net.minecraft.resource.ResourceManager;
import net.minecraft.util.Identifier;

import java.util.Map;

public class LootSwapEnchantment extends Enchantment{
	
	private final Map<Item, Item> swaps;
	private final int maxLevel;
	private final float baseChance;
	
	public LootSwapEnchantment(EnchantmentTarget type, Map<Item, Item> swaps, int maxLevel, float baseChance){
		super(Rarity.VERY_RARE, type, new EquipmentSlot[]{EquipmentSlot.MAINHAND, EquipmentSlot.OFFHAND});
		this.swaps = swaps;
		this.maxLevel = maxLevel;
		this.baseChance = baseChance;
	}
	
	public int getMaxLevel(){
		return maxLevel;
	}
	
	public boolean isAvailableForRandomSelection(){
		return false;
	}
	
	public boolean isAvailableForEnchantedBookOffer(){
		return false;
	}
	
	public boolean isTreasure(){
		return true;
	}
	
	public static void modifyLootTable(ResourceManager resourceManager,
	                                   LootManager lootManager,
	                                   Identifier id,
	                                   LootTable.Builder tableBuilder,
	                                   LootTableSource source){
		if(!source.isBuiltin())
			return;
		
		LootContextType tableType = tableBuilder.build().getType();
		if(tableType == LootContextTypes.ENTITY)
			tableBuilder.apply(new ReplacingLootFunction(ArcanaRegistry.TRANSMUTATIVE));
		if(tableType == LootContextTypes.BLOCK)
			tableBuilder.apply(new ReplacingLootFunction(ArcanaRegistry.PURIFYING));
	}
	
	private static class ReplacingLootFunction implements LootFunction{
		
		private final LootSwapEnchantment enchantment;
		
		private ReplacingLootFunction(LootSwapEnchantment enchantment){
			this.enchantment = enchantment;
		}
		
		public LootFunctionType getType(){
			throw new UnsupportedOperationException("ReplacingLootFunction is not serializable");
		}
		
		public ItemStack apply(ItemStack stack, LootContext context){
			if(!enchantment.swaps.containsKey(stack.getItem()))
				return stack;
			
			int level = -1;
			if(context.get(LootContextParameters.KILLER_ENTITY) instanceof LivingEntity l)
				level = EnchantmentHelper.getEquipmentLevel(enchantment, l);
			ItemStack toolStack = context.get(LootContextParameters.TOOL);
			if(toolStack != null)
				level = Math.max(level, EnchantmentHelper.getLevel(enchantment, toolStack));
			
			if(level > 0){
				float bc = enchantment.baseChance;
				float chance = (level * bc) / stack.getCount();
				if(bc >= 1.0 || context.getRandom().nextFloat() < chance)
					stack = new ItemStack(enchantment.swaps.get(stack.getItem()), stack.getCount());
			}
			return stack;
		}
	}
}