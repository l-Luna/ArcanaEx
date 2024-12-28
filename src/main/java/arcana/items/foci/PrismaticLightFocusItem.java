package arcana.items.foci;

import arcana.ArcanaRegistry;
import arcana.entities.PrismaticOrbEntity;
import arcana.items.FocusItem;
import arcana.util.MathUtil;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

import java.util.UUID;

public class PrismaticLightFocusItem extends FocusItem{
	
	public PrismaticLightFocusItem(Settings settings){
		super(settings);
	}
	
	public static Vec3d hoverPosition(PlayerEntity player){
		return player.getEyePos().add(MathUtil.facingToVec(player).multiply(2));
	}
	
	public boolean isContinuous(){
		return true;
	}
	
	public void startContinuousCast(ItemStack wand, ItemStack focus, PlayerEntity user){
		World w = user.world;
		if(!w.isClient){
			PrismaticOrbEntity orb = new PrismaticOrbEntity(ArcanaRegistry.PRISMATIC_ORB, w);
			orb.setOwner(user);
			w.spawnEntity(orb);
			orb.setPosition(hoverPosition(user));
			focus.getOrCreateNbt().putUuid("orbId", orb.getUuid());
		}
	}
	
	public void endContinuousCast(ItemStack wand, ItemStack focus, PlayerEntity user){
		World w = user.world;
		if(!w.isClient){
			ServerWorld sw = (ServerWorld)w;
			var stackNbt = focus.getNbt();
			if(stackNbt == null || !stackNbt.containsUuid("orbId"))
				return;
			UUID orbId = stackNbt.getUuid("orbId");
			Entity e = sw.getEntity(orbId);
			if(e instanceof PrismaticOrbEntity poe)
				poe.release();
		}
	}
}