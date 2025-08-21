package arcana.components;

import arcana.aspects.Aspect;
import arcana.aspects.AspectMap;
import arcana.aspects.Aspects;
import arcana.aura.AuraWorld;
import arcana.aura.Node;
import arcana.aura.NodeReference;
import arcana.items.FocusItem;
import arcana.items.WandItem;
import dev.onyxstudios.cca.api.v3.component.Component;
import dev.onyxstudios.cca.api.v3.component.ComponentKey;
import dev.onyxstudios.cca.api.v3.component.ComponentRegistryV3;
import dev.onyxstudios.cca.api.v3.component.sync.AutoSyncedComponent;
import dev.onyxstudios.cca.api.v3.component.tick.ServerTickingComponent;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.util.Hand;
import net.minecraft.util.Identifier;
import net.minecraft.util.Util;
import net.minecraft.world.World;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static arcana.Arcana.arcId;

// handles wand use related data
// synced to all players
public class Caster implements Component, AutoSyncedComponent, ServerTickingComponent{
	
	enum CasterState{
		IDLE,
		DRAWING,
		CONTINUOUS_CASTING
	}
	
	public static final ComponentKey<Caster> KEY = ComponentRegistryV3.INSTANCE.getOrCreate(arcId("caster"), Caster.class);
	
	public static Caster from(PlayerEntity entity){
		return entity.getComponent(KEY);
	}
	
	private final PlayerEntity player;
	private CasterState state = CasterState.IDLE;
	
	// common between states
	int stateTimer;
	Hand wandHand = Hand.MAIN_HAND;
	
	// for handling ending a cast, server-only
	ItemStack lastWandStack;
	
	// for draining
	NodeReference drainTargetNode;
	Aspect drainTargetAspect;
	int drainTimer;
	
	// for continuous casting
	NbtCompound contFocusState;
	
	public Caster(PlayerEntity player){
		this.player = player;
	}
	
	public PlayerEntity getPlayer(){
		return player;
	}
	
	public void beginDraining(Node from, Hand wandHand){
		if(state != CasterState.IDLE)
			return;
		this.wandHand = wandHand;
		wandStack().ifPresent(wand -> {
			chooseDrainAspect(from, wand).ifPresent(asp -> {
				reset();
				state = CasterState.DRAWING;
				drainTargetNode = NodeReference.ref(from);
				drainTargetAspect = asp;
				sync();
			});
		});
	}
	
	public void beginContinuousCasting(Hand wandHand){
		if(state != CasterState.IDLE)
			return;
		reset();
		this.wandHand = wandHand;
		state = CasterState.CONTINUOUS_CASTING;
		contFocusState = new NbtCompound();
		sync();
	}
	
	public void endState(){
		reset();
		sync();
	}
	
	//
	
	public void serverTick(){
		if(state == CasterState.IDLE)
			return;
		
		var wandO = wandStack();
		if(wandO.isEmpty()){
			endState(); return;
		}
		ItemStack wand = wandO.get();
		lastWandStack = wand;
		AuraWorld auraWorld = AuraWorld.from(world());
		
		switch(state){
			case DRAWING -> {
				// check if draining can continue (node still exists and is closest to player)
				var nodeO = drainTargetNode.deref(world());
				if(nodeO.isEmpty()){
					endState(); return;
				}
				Node node = nodeO.get();
				if(!auraWorld.raycastNodes(player, false).equals(Optional.of(node))){
					endState(); return;
				}
				
				// re-randomise time and draw the next aspect (if not -1)
				AspectMap wandAspects = WandItem.aspectsFrom(wand), nodeAspects = node.getAspects();
				if(!nodeAspects.contains(drainTargetAspect)){
					// if in a stuck state, reroll until we aren't
					Optional<Aspect> aspect = chooseDrainAspect(node, wand);
					if(aspect.isPresent())
						drainTargetAspect = aspect.get();
				}else if(drainTimer <= 0){
					if(drainTimer == 0){
						int aspectDrainAmount = 3 + world().random.nextInt(3);
						int wandCapacity = WandItem.capacity(wand);
						
						int capacityLeft = wandCapacity - wandAspects.get(drainTargetAspect);
						if(capacityLeft < 0)
							capacityLeft = 0;
						int realDrainAmount = Math.min(Math.min(nodeAspects.get(drainTargetAspect), aspectDrainAmount), capacityLeft);
						nodeAspects.take(drainTargetAspect, realDrainAmount);
						node.markDirty();
						WandItem.updateAspects(wand, map -> map.addCapped(drainTargetAspect, realDrainAmount, wandCapacity));
						
						Optional<Aspect> aspect = chooseDrainAspect(node, wand);
						// if the node is out of aspects to draw, stay in this state on the old aspect
						if(aspect.isPresent())
							drainTargetAspect = aspect.get();
					}
					drainTimer = world().random.nextBetween(6, 9);
					sync();
				}
				
				if(nodeAspects.contains(drainTargetAspect))
					drainTimer--;
				stateTimer++;
			}
			case CONTINUOUS_CASTING -> WandItem.updateFocus(wand, focusStack -> {
				if(focusStack.getItem() instanceof FocusItem fi && fi.isContinuous()){
					if(stateTimer == 0){
						var cost = fi.castCost(wand, focusStack, player).copy();
						cost.multiply(aspect -> WandItem.costMultiplier(aspect, wand, player));
						if(WandItem.aspectsFrom(wand).contains(cost)){
							WandItem.updateAspects(wand, aspects -> aspects.take(cost));
							fi.startContinuousCast(wand, focusStack, player, contFocusState);
						}else{
							endState(); return;
						}
					}
					boolean cont = fi.tickContinuousCast(wand, focusStack, player, contFocusState);
					stateTimer++;
					if(!cont)
						endState();
				}
			});
		}
	}
	
	//
 
	private Optional<Aspect> chooseDrainAspect(Node from, ItemStack wand){
		AspectMap wandAspects = WandItem.aspectsFrom(wand);
		List<Aspect> candidateAspects = new ArrayList<>(from.getAspects().aspectSet());
		candidateAspects.removeIf(x -> !Aspects.primals.contains(x));
		candidateAspects.removeIf(x -> wandAspects.get(x) >= WandItem.capacity(wand));
		if(candidateAspects.isEmpty())
			return Optional.empty();
		return Optional.of(Util.getRandom(candidateAspects, player.world.random));
	}
	
	private Optional<ItemStack> wandStack(){
		ItemStack there = player.getStackInHand(wandHand);
		if(there.getItem() instanceof WandItem)
			return Optional.of(there);
		return Optional.empty();
	}
	
	private void reset(){
		if(state == CasterState.CONTINUOUS_CASTING && lastWandStack != null)
			WandItem.updateFocus(lastWandStack, focus -> {
				if(focus.getItem() instanceof FocusItem fi && fi.isContinuous())
					fi.endContinuousCast(lastWandStack, focus, player, contFocusState);
			});
		
		state = CasterState.IDLE;
		stateTimer = 0;
		drainTargetNode = null;
		drainTargetAspect = null;
		drainTimer = -1;
		contFocusState = null;
	}
	
	private World world(){
		return player.world;
	}
	
	//
	
	public void sync(){
		player.syncComponent(KEY);
	}
	
	public void readFromNbt(NbtCompound tag){
		state = CasterState.IDLE;
		String stateName = tag.getString("state");
		for(CasterState value : CasterState.values())
			if(value.name().equals(stateName))
				state = value;
		
		stateTimer = tag.getInt("stateTimer");
		if(tag.contains("drainTargetNode"))
			drainTargetNode = NodeReference.fromNbt(tag.getCompound("drainTargetNode"));
		if(tag.contains("drainTargetAspect"))
			drainTargetAspect = Aspects.byName(new Identifier(tag.getString("drainTargetAspect")));
		if(tag.contains("drainTimer"))
			drainTimer = tag.getInt("drainTimer");
		if(tag.contains("contFocusState"))
			contFocusState = tag.getCompound("contFocusState");
	}
	
	public void writeToNbt(NbtCompound tag){
		tag.putString("state", state.name());
		tag.putInt("stateTimer", stateTimer);
		if(drainTargetNode != null){
			tag.put("drainTargetNode", drainTargetNode.toNbt());
			tag.putString("drainTargetAspect", drainTargetAspect.id().toString());
			tag.putInt("drainTimer", drainTimer);
		}
		if(contFocusState != null)
			tag.put("contFocusState", contFocusState);
	}
}