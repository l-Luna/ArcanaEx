package arcana.items.foci;

import arcana.ArcanaRegistry;
import arcana.aspects.AspectMap;
import arcana.aspects.AspectStack;
import arcana.aspects.Aspects;
import arcana.blocks.CrimsonCampfireBlock;
import arcana.blocks.WardedCampfireBlock;
import arcana.items.FocusItem;
import arcana.items.WandItem;
import net.minecraft.advancement.criterion.Criteria;
import net.minecraft.block.*;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.state.property.Properties;
import net.minecraft.util.ActionResult;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.event.GameEvent;

public class FireFocusItem extends FocusItem{
	
	public FireFocusItem(Settings settings){
		super(settings);
	}
	
	public AspectMap deciCastCost(ItemStack wand, ItemStack focus, PlayerEntity user){
		// TODO: check player reach
		BlockState looking = user.getWorld().getBlockState(((BlockHitResult)user.raycast(5.5, 0, false)).getBlockPos());
		if(WardedCampfireBlock.canBeLit(looking) || CrimsonCampfireBlock.canBeLit(looking))
			return AspectMap.fromAspectStacks(new AspectStack(Aspects.ORDER, 100), new AspectStack(Aspects.FIRE, 100));
		return AspectMap.fromAspectStack(new AspectStack(Aspects.FIRE, 3));
	}
	
	public ActionResult castOnBlock(ItemUsageContext ctx){
		// from FlintAndSteelItem
		// can't just use `Items.FLINT_AND_STEEL.useOnBlock(ctx)` since it damages the item
		// TODO: use a dummy item context with a fake stack?
		// or do we want to do further changes?
		PlayerEntity player = ctx.getPlayer();
		World world = ctx.getWorld();
		BlockPos pos = ctx.getBlockPos();
		BlockState bs = world.getBlockState(pos);
		if(!CampfireBlock.canBeLit(bs) && !WardedCampfireBlock.canBeLit(bs) && !CrimsonCampfireBlock.canBeLit(bs) && !CandleBlock.canBeLit(bs) && !CandleCakeBlock.canBeLit(bs)){
			BlockPos toLight = pos.offset(ctx.getSide());
			if(AbstractFireBlock.canPlaceAt(world, toLight, ctx.getHorizontalPlayerFacing())){
				world.playSound(player, toLight, SoundEvents.ITEM_FLINTANDSTEEL_USE, SoundCategory.BLOCKS, 1, world.getRandom().nextFloat() * .4f + .8f);
				world.setBlockState(toLight, AbstractFireBlock.getState(world, toLight), Block.NOTIFY_ALL | Block.REDRAW_ON_MAIN_THREAD);
				world.emitGameEvent(player, GameEvent.BLOCK_PLACE, pos);
				if(player instanceof ServerPlayerEntity)
					Criteria.PLACED_BLOCK.trigger((ServerPlayerEntity)player, toLight, ctx.getStack());
				
				return ActionResult.success(world.isClient());
			}
			return ActionResult.FAIL;
		}else{
			world.playSound(player, pos, SoundEvents.ITEM_FLINTANDSTEEL_USE, SoundCategory.BLOCKS, 1, world.getRandom().nextFloat() * .4f + .8f);
			world.setBlockState(pos, bs.with(Properties.LIT, Boolean.TRUE), Block.NOTIFY_ALL | Block.REDRAW_ON_MAIN_THREAD);
			world.emitGameEvent(player, GameEvent.BLOCK_CHANGE, pos);
			
			return ActionResult.success(world.isClient());
		}
	}
	
	public ActionResult castOnEntity(ItemStack wand, ItemStack focus, PlayerEntity user, LivingEntity target){
		// anything from 10 to 45 is obtainable
		int strength = WandItem.focusStrength(wand, user);
		float damage = Math.round(0.12f * strength + 3);
		
		// bonus damage if the user has ignis power
		if(user.hasStatusEffect(RegistryEntry.of(ArcanaRegistry.FIRE_POWER)))
			damage += 4;
		
		target.setOnFireFor((int)(damage + 2));
		target.damage(user.getDamageSources().inFire(), damage);
		target.setAttacker(user);
		return ActionResult.success(user.getWorld().isClient);
	}
}