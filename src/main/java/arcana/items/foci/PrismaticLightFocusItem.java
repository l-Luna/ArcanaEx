package arcana.items.foci;

import arcana.ArcanaRegistry;
import arcana.aspects.AspectMap;
import arcana.aspects.AspectStack;
import arcana.aspects.Aspects;
import arcana.entities.PrismaticOrbEntity;
import arcana.items.FocusItem;
import arcana.util.MathUtil;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

public class PrismaticLightFocusItem extends FocusItem{
	
	public PrismaticLightFocusItem(Settings settings){
		super(settings);
	}
	
	public static Vec3d hoverPosition(PlayerEntity player){
		return player.getEyePos().add(MathUtil.facingToVec(player).multiply(2));
	}
	
	public AspectMap deciCastCost(@Nullable ItemStack wand, ItemStack focus, PlayerEntity user){
		return AspectMap.fromAspectStacks(new AspectStack(Aspects.FIRE, 7), new AspectStack(Aspects.AIR, 7));
	}
	
	public boolean isContinuous(){
		return true;
	}
	
	public void startContinuousCast(ContinuousCastContext ccc){
		PlayerEntity user = ccc.user;
		NbtCompound state = ccc.state;
		World w = user.getWorld();
		if(!w.isClient){
			PrismaticOrbEntity orb = new PrismaticOrbEntity(ArcanaRegistry.PRISMATIC_ORB, w);
			orb.setOwner(user);
			orb.setPosition(hoverPosition(user));
			orb.setBurning(user.hasStatusEffect(RegistryEntry.of(ArcanaRegistry.FIRE_POWER)));
			w.spawnEntity(orb);
			state.putUuid("orbId", orb.getUuid());
		}
	}
	
	public void endContinuousCast(ContinuousCastContext ccc){
		PlayerEntity user = ccc.user;
		NbtCompound state = ccc.state;
		World w = user.getWorld();
		if(!w.isClient){
			ServerWorld sw = (ServerWorld)w;
			if(!state.containsUuid("orbId"))
				return;
			UUID orbId = state.getUuid("orbId");
			Entity e = sw.getEntity(orbId);
			if(e instanceof PrismaticOrbEntity poe)
				poe.release();
		}
	}
}