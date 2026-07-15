package arcana.items.trinkets;

import arcana.ArcanaRegistry;
import arcana.ArcanaTags;
import arcana.api.WarpingItem;
import arcana.cca_components.RunicShielding;
import arcana.network.PkEntityStatusEx;
import arcana.util.InventoryUtil;
import dev.emi.trinkets.api.TrinketItem;
import net.fabricmc.fabric.api.util.TriState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.tooltip.TooltipType;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.Vec3d;

import java.util.List;

public class ClawTrinketItem extends TrinketItem implements WarpingItem{
	
	private final float attackBonus;
	private final boolean ruby;
	
	public ClawTrinketItem(Settings settings, float attackBonus, boolean ruby){
		super(settings);
		this.attackBonus = attackBonus;
		this.ruby = ruby;
	}
	
	public float getUnarmedAttackBonus(){
		return attackBonus;
	}
	
	public int warping(ItemStack stack, PlayerEntity player){
		return ruby ? 3 : 1;
	}
	
	public void appendTooltip(ItemStack stack, TooltipContext context, List<Text> tooltip, TooltipType type){
		super.appendTooltip(stack, context, tooltip, type);
		float bonus = getUnarmedAttackBonus();
		Object txt = bonus == (int)bonus ? (Object)(int)bonus : bonus;
		tooltip.add(Text.translatable("tooltip.arcana.unarmed_damage", txt).formatted(Formatting.BLUE));
		tooltip.add(WarpingItem.warpingTooltip(ruby ? 3 : 1));
	}
	
	public static void handleEntityDeath(ServerWorld world, Entity killerEntity, LivingEntity killed){
		if(killerEntity instanceof PlayerEntity killer
				&& killer.getWeaponStack().isEmpty()){
			TriState ringType = getRingType(killer);
			boolean isRuby = ringType == TriState.TRUE;
			spawnAttackParticleAt(killed, isRuby);
			if(!killed.getType().isIn(ArcanaTags.CANNOT_STEAL_LIFE_FROM)){
				if(ringType != TriState.DEFAULT){
					gainHungerByAttack(killer, isRuby);
					PkEntityStatusEx.sendStatus(killer, isRuby ? PkEntityStatusEx.STATUS_ATE_WITH_RUBY_CLAWS : PkEntityStatusEx.STATUS_ATE_WITH_CLAWS);
					if(isRuby)
						RunicShielding.from(killer).recharge(1);
					// TODO: custom lifesteal sound, vfx
					world.playSound(null, killed.getX(), killed.getY(), killed.getZ(), SoundEvents.ENTITY_CAT_HISS, SoundCategory.HOSTILE, 0.5f, 0.5f, 0);
				}
			}
		}
	}
	
	public static void handleEntityHit(LivingEntity entity, DamageSource source, float baseDamageTaken, float damageTaken, boolean blocked){
		if(damageTaken > 0 && source.getAttacker() instanceof PlayerEntity attacker && attacker.getWeaponStack().isEmpty()){
			TriState type = getRingType(attacker);
			if(type != TriState.DEFAULT)
				spawnAttackParticleAt(entity, type == TriState.TRUE);
		}
	}
	
	private static void spawnAttackParticleAt(LivingEntity entity, boolean isRuby){
		Vec3d where = entity.getBoundingBox().getCenter();
		((ServerWorld)entity.getEntityWorld()).spawnParticles(isRuby ? ArcanaRegistry.CLAW_RUBY : ArcanaRegistry.CLAW, where.x, where.y, where.z, 0, 0, 0, 0, 0);
	}
	
	public static void gainHungerByAttack(PlayerEntity player, boolean ruby){
		player.getHungerManager().add(ruby ? 3 : 2, 2);
	}
	
	public static TriState getRingType(PlayerEntity player){
		return InventoryUtil.fromFirstTrinket(player, it -> it.getItem() instanceof ClawTrinketItem c ? TriState.of(c.ruby) : null).orElse(TriState.DEFAULT);
	}
}