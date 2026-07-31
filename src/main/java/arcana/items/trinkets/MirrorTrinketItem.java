package arcana.items.trinkets;

import arcana.cca_components.MagicMirrorQueue;
import arcana.duck.ArcanaPlayerEntity;
import arcana.items.PersonalMagicMirrorItem;
import arcana.items.components.ArcanaDataComponents;
import arcana.util.InventoryUtil;
import dev.emi.trinkets.api.*;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.world.GameRules;
import net.minecraft.world.World;

import java.util.UUID;

public class MirrorTrinketItem extends PersonalMagicMirrorItem implements Trinket{
	
	// reimplement bits of TrinketItem to take advantage of PersonalMagicMirrorItem's impl
	
	public MirrorTrinketItem(Settings settings){
		super(settings);
		TrinketsApi.registerTrinket(this, this);
	}
	
	public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand){
		ItemStack stack = user.getStackInHand(hand);
		if(TrinketItem.equipItem(user, stack))
			return TypedActionResult.success(stack, world.isClient());
		return super.use(world, user, hand);
	}
	
	// main inventory handling, replace dropAll() with this
	public static void handleInventoryDrops(PlayerInventory inventory, ItemStack mirrorTrinketStack){
		UUID tag = mirrorTrinketStack.get(ArcanaDataComponents.MAGIC_MIRROR_TAG),
		     id = mirrorTrinketStack.get(ArcanaDataComponents.MAGIC_MIRROR_ID);
		MagicMirrorQueue queue = MagicMirrorQueue.from(inventory.player.getWorld());
		InventoryUtil.streamInventory(inventory).forEach(it -> queue.push(tag, id, it));
	}
	
	// trinkets handling
	// TODO: it's possible for a mod to later reset the drop rule to DEFAULT/KEEP, which would cause item duplication
	// there isn't a better way about this using current official API unfortunately
	public static TrinketEnums.DropRule handleTrinketDrops(TrinketEnums.DropRule rule, ItemStack stack, SlotReference ref, LivingEntity entity){
		// don't touch items with preexisting special behaviour
		if(rule == TrinketEnums.DropRule.DEFAULT && entity instanceof ArcanaPlayerEntity player){
			boolean keepInv = entity.getWorld().getGameRules().getBoolean(GameRules.KEEP_INVENTORY);
			if(keepInv)
				return TrinketEnums.DropRule.DEFAULT;
			ItemStack mirrorStack = player.arcana$getDeathStashedMirrorStack();
			if(mirrorStack != null){
				UUID tag = mirrorStack.get(ArcanaDataComponents.MAGIC_MIRROR_TAG),
						id = mirrorStack.get(ArcanaDataComponents.MAGIC_MIRROR_ID);
				MagicMirrorQueue.from(entity.getEntityWorld()).push(tag, id, stack);
				return TrinketEnums.DropRule.DESTROY;
			}
		}
		return rule;
	}
}