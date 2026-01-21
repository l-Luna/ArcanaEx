package arcana.util;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.loot.condition.LootCondition;
import net.minecraft.loot.condition.LootConditionType;
import net.minecraft.loot.context.LootContext;
import net.minecraft.loot.context.LootContextParameters;
import net.minecraft.util.Identifier;
import net.minecraft.util.JsonHelper;
import net.minecraft.util.JsonSerializer;
import net.minecraft.util.registry.Registry;

public class RandomChanceOnceLootCondition implements LootCondition{
	
	public static final LootConditionType TYPE = new LootConditionType(new Serializer());
	
	private final float chance;
	private final Item filter;
	
	public RandomChanceOnceLootCondition(float chance, Item filter){
		this.chance = chance;
		this.filter = filter;
	}
	
	public boolean test(LootContext ctx){
		Entity entity = ctx.get(LootContextParameters.KILLER_ENTITY);
		int looting = 0;
		if(entity instanceof LivingEntity le){
			if(entity instanceof PlayerEntity player && InventoryUtil.streamAllItems(player).anyMatch(x -> x.getItem() == filter))
				return false;
			looting = EnchantmentHelper.getLooting(le);
		}
		
		return ctx.getRandom().nextFloat() < chance + looting * 0.05;
	}
	
	public LootConditionType getType(){
		return TYPE;
	}
	
	public static class Serializer implements JsonSerializer<RandomChanceOnceLootCondition>{
		public void toJson(JsonObject obj, RandomChanceOnceLootCondition cond, JsonSerializationContext ctx){
			obj.addProperty("chance", cond.chance);
		}
		
		public RandomChanceOnceLootCondition fromJson(JsonObject obj, JsonDeserializationContext ctx){
			return new RandomChanceOnceLootCondition(JsonHelper.getFloat(obj, "chance"), Registry.ITEM.get(new Identifier(JsonHelper.getString(obj, "filter"))));
		}
	}
}