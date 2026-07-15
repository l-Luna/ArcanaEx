package arcana.util;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.loot.condition.LootCondition;
import net.minecraft.loot.condition.LootConditionType;
import net.minecraft.loot.context.LootContext;
import net.minecraft.loot.context.LootContextParameters;
import net.minecraft.registry.RegistryCodecs;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.entry.RegistryEntryList;

public class RandomChanceOnceLootCondition implements LootCondition{
	
	public static final MapCodec<RandomChanceOnceLootCondition> CODEC = RecordCodecBuilder.mapCodec(i -> i.group(
			Codec.FLOAT.fieldOf("chance").forGetter(x -> x.chance),
			RegistryCodecs.entryList(RegistryKeys.ITEM).fieldOf("filter").forGetter(x -> x.filter)
	).apply(i, RandomChanceOnceLootCondition::new));
	
	public static final LootConditionType TYPE = new LootConditionType(CODEC);
	
	private final float chance;
	private final RegistryEntryList<Item> filter;
	
	public RandomChanceOnceLootCondition(float chance, RegistryEntryList<Item> filter){
		this.chance = chance;
		this.filter = filter;
	}
	
	public boolean test(LootContext ctx){
		Entity entity = ctx.get(LootContextParameters.ATTACKING_ENTITY);
		int looting = 0;
		if(entity instanceof PlayerEntity player && InventoryUtil.streamAllItems(player).anyMatch(x -> x.isIn(filter)))
			return false;
		
		return ctx.getRandom().nextFloat() < chance + looting * 0.05;
	}
	
	public LootConditionType getType(){
		return TYPE;
	}
}