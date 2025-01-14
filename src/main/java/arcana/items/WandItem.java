package arcana.items;

import arcana.ArcanaRegistry;
import arcana.aspects.*;
import arcana.aura.AuraWorld;
import arcana.aura.Node;
import arcana.blocks.be.InfusionMatrixBlockEntity;
import arcana.client.ArcanaClient;
import dev.emi.trinkets.api.SlotReference;
import dev.emi.trinkets.api.TrinketsApi;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.client.item.TooltipData;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.*;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;

public class WandItem extends Item implements WarpingItem{
	
	public WandItem(Settings settings){
		super(settings);
	}
	
	public static ItemStack withCapAndCore(Cap cap, Core core){
		ItemStack stack = new ItemStack(ArcanaRegistry.WAND);
		NbtCompound tag = stack.getOrCreateNbt();
		tag.putString("cap_id", cap.id().toString());
		tag.putString("core_id", core.id().toString());
		return stack;
	}
	
	public static ItemStack basicWand(){
		return withCapAndCore(ArcanaRegistry.IRON_WAND_CAP, ArcanaRegistry.STICK_CORE);
	}
	
	public Text getName(ItemStack stack){
		return Text.translatable(
				"item.arcana.wand",
				Text.translatable(capFrom(stack).translationKey()),
				Text.translatable(coreFrom(stack).translationKey()),
				Text.translatable("wand.variant.arcana.wand"));
	}
	
	public void appendStacks(ItemGroup group, DefaultedList<ItemStack> stacks){
		if(isIn(group)){
			stacks.add(withCapAndCore(ArcanaRegistry.IRON_WAND_CAP, ArcanaRegistry.STICK_CORE));
			stacks.add(withCapAndCore(ArcanaRegistry.GOLD_WAND_CAP, ArcanaRegistry.GREATWOOD_WAND_CORE));
			stacks.add(withCapAndCore(ArcanaRegistry.THAUMIUM_WAND_CAP, ArcanaRegistry.SILVERWOOD_WAND_CORE));
			stacks.add(withCapAndCore(ArcanaRegistry.NETHERITE_WAND_CAP, ArcanaRegistry.ARCANIUM_WAND_CORE));
		}
	}
	
	public ActionResult useOnBlock(ItemUsageContext context){
		World world = context.getWorld();
		PlayerEntity player = context.getPlayer();
		BlockPos pos = context.getBlockPos();
		BlockState state = world.getBlockState(pos);
		
		ItemStack wandStack = context.getStack();
		ItemStack focusStack = focusFrom(wandStack);
		
		if((player != null && player.isSneaking()) || focusStack.isEmpty()){
			if(state.getBlock() == Blocks.CAULDRON){
				world.setBlockState(pos, ArcanaRegistry.CRUCIBLE.getDefaultState());
				world.playSound(player, pos, SoundEvents.ENTITY_EVOKER_CAST_SPELL, SoundCategory.PLAYERS, 1, 1);
				for(int i = 0; i < 20; i++)
					world.addParticle(ParticleTypes.END_ROD, pos.getX() + world.random.nextDouble(), pos.getY() + world.random.nextDouble(), pos.getZ() + world.random.nextDouble(), 0, 0, 0);
				return ActionResult.SUCCESS;
			}
			if(state.getBlock() == Blocks.CRAFTING_TABLE){
				world.setBlockState(pos, ArcanaRegistry.ARCANE_CRAFTING_TABLE.getDefaultState());
				world.playSound(player, pos, SoundEvents.ENTITY_EVOKER_CAST_SPELL, SoundCategory.PLAYERS, 1, 1);
				for(int i = 0; i < 20; i++)
					world.addParticle(ParticleTypes.END_ROD, (pos.getX() - .1f) + world.random.nextDouble() * 1.2f, (pos.getY() - .1f) + world.random.nextDouble() * 1.2f, (pos.getZ() - .1f) + world.random.nextDouble() * 1.2f, 0, 0, 0);
				return ActionResult.SUCCESS;
			}
			if(state.getBlock() == ArcanaRegistry.INFUSION_MATRIX)
				if(world.getBlockEntity(pos) instanceof InfusionMatrixBlockEntity e)
					e.activate(player);
		}else if(focusStack.getItem() instanceof FocusItem fi){
			AspectMap cost = fi.castCost(wandStack, focusStack, player).copy();
			cost.multiply(aspect -> costMultiplier(aspect, wandStack, player));
			if(aspectsFrom(wandStack).contains(cost)){
				ActionResult result = fi.castOnBlock(context);
				if(result != ActionResult.PASS && result != ActionResult.FAIL){
					// no point charging for something that didn't work
					updateAspects(wandStack, aspects -> aspects.take(cost));
				}
				return result;
			}
		}
		
		return ActionResult.PASS;
	}
	
	public ActionResult useOnEntity(ItemStack stack, PlayerEntity user, LivingEntity entity, Hand hand){
		// creative mode "helpfully" copies the stack before use on entities, so we get the real thing here
		ItemStack wand = user.getStackInHand(hand);
		ItemStack focusStack = focusFrom(wand);
		AspectMap stored = aspectsFrom(wand);
		if(focusStack.getItem() instanceof FocusItem fi){
			var cost = fi.castCost(wand, focusStack, user).copy();
			cost.multiply(aspect -> costMultiplier(aspect, stack, user));
			if(stored.contains(cost)){
				ActionResult result = fi.castOnEntity(wand, focusStack, user, entity);
				if(result != ActionResult.PASS && result != ActionResult.FAIL){
					// no point charging for something that didn't work
					updateAspects(wand, aspects -> aspects.take(cost));
				}
				return result;
			}
		}
		return super.useOnEntity(stack, user, entity, hand);
	}
	
	public int getMaxUseTime(ItemStack stack){
		return 72000;
	}
	
	public void usageTick(World world, LivingEntity user, ItemStack stack, int remainingUseTicks){
		// TODO: use reach-entity-attributes to check player's true range
		if(world.isClient)
			return;
		AuraWorld aura = AuraWorld.from(world);
		Optional<Node> nodeO = aura.raycastNodes(user.getEyePos(), 4.5, false, user);
		if(nodeO.isPresent() && !isContinuousCasting(stack)){
			Node node = nodeO.get();
			// only attempt to drain aspects that the node has and the wand needs
			AspectMap nodeAspects = node.getAspects();
			AspectMap wandAspects = aspectsFrom(stack);
			int wandCapacity = capacity(stack);
			List<Aspect> candidateAspects = new ArrayList<>(nodeAspects.aspectSet());
			candidateAspects.removeIf(x -> !Aspects.primals.contains(x));
			candidateAspects.removeIf(x -> wandAspects.get(x) >= wandCapacity);
			
			if(!candidateAspects.isEmpty()){
				Aspect aspect = Util.getRandom(candidateAspects, world.random);
				int aspectDrainWait = 8;
				int aspectDrainAmount = 3 + world.random.nextInt(3);
				if(world.getTime() % aspectDrainWait == 0){
					var capacityLeft = wandCapacity - wandAspects.get(aspect);
					if(capacityLeft < 0)
						capacityLeft = 0;
					int realDrainAmount = Math.min(Math.min(nodeAspects.get(aspect), aspectDrainAmount), capacityLeft);
					nodeAspects.take(aspect, realDrainAmount);
					updateAspects(stack, map -> map.addCapped(aspect, realDrainAmount, wandCapacity));
					aura.sync();
				}
			}
		}else{
			updateFocus(stack, focusStack -> {
				if(user instanceof PlayerEntity player && focusStack.getItem() instanceof FocusItem fi && fi.isContinuous()){
					boolean successful = isContinuousCasting(stack);
					int ticksUsed = getMaxUseTime(stack) - remainingUseTicks;
					if(ticksUsed == 0){
						var cost = fi.castCost(stack, focusStack, player).copy();
						cost.multiply(aspect -> costMultiplier(aspect, stack, player));
						if(aspectsFrom(stack).contains(cost)){
							updateAspects(stack, aspects -> aspects.take(cost));
							fi.startContinuousCast(stack, focusStack, player);
							successful = true;
							setIsContinuousCasting(stack, true);
						}
					}
					if(successful)
						fi.tickContinuousCast(stack, focusStack, player);
				}
			});
		}
	}
	
	public void onStoppedUsing(ItemStack stack, World world, LivingEntity user, int remainingUseTicks){
		updateFocus(stack, focusStack -> {
			if(user instanceof PlayerEntity player && focusStack.getItem() instanceof FocusItem fi && fi.isContinuous())
				fi.endContinuousCast(stack, focusStack, player);
		});
		setIsContinuousCasting(stack, false);
	}
	
	public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand){
		user.setCurrentHand(hand);
		return TypedActionResult.consume(user.getStackInHand(hand));
	}
	
	public Optional<TooltipData> getTooltipData(ItemStack stack){
		return Optional.of(new WandAspectsTooltipData(stack));
	}
	
	@Environment(EnvType.CLIENT) // access The Player and Text
	public void appendTooltip(ItemStack stack, @Nullable World world, List<Text> tooltip, TooltipContext context){
		ItemStack focusStack = focusFrom(stack);
		var player = MinecraftClient.getInstance().player;
		if(focusStack.getItem() instanceof FocusItem fi){
			tooltip.add(fi.nameForTooltip(focusStack));
			var cost = fi.castCost(stack, focusStack, player).copy();
			cost.multiply(aspect -> costMultiplier(aspect, stack, player));
			tooltip.add(costText(cost));
		}
		int warping = warping(stack, player);
		if(warping != 0)
			tooltip.add(ArcanaRegistry.WARPING.getName(warping));
		// if the discount in all primals is the same, say vis discount, otherwise list every aspect
		int air = percentOff(Aspects.AIR, stack, player);
		boolean all = true;
		for(Aspect primal : Aspects.primals)
			if(percentOff(primal, stack, player) != air){
				all = false;
				break;
			}
		
		if(all && air != 0)
			tooltip.add(Text.translatable("tooltip.arcana.wand.discount.all", air));
		else
			for(Aspect primal : Aspects.primals){
				var v = percentOff(primal, stack, player);
				if(v != 0)
					tooltip.add(Text.translatable(
							"tooltip.arcana.wand.discount.aspect", v,
							primal.name().formatted(ArcanaClient.colourForPrimal(primal))
					));
			}
	}
	
	@Environment(EnvType.CLIENT)
	public static Text costText(AspectMap map){
		MutableText costs = Text.literal("");
		for(AspectStack stack : map.asStacks())
			costs.append(Text.translatable("tooltip.arcana.wand.focus_cost.individual", stack.amount(), stack.type().name())
					.formatted(ArcanaClient.colourForPrimal(stack.type())));
		return Text.translatable("tooltip.arcana.wand.focus_cost.total", costs);
	}
	
	// TODO: NBT-backed aspect map?
	
	public static AspectMap aspectsFrom(ItemStack stack){
		return AspectMap.fromNbt(stack.getSubNbt("aspects"));
	}
	
	public static void putAspects(ItemStack stack, AspectMap aspects){
		stack.getOrCreateNbt().put("aspects", aspects.toNbt());
	}
	
	public static void updateAspects(ItemStack stack, Consumer<AspectMap> updater){
		var map = aspectsFrom(stack);
		updater.accept(map);
		putAspects(stack, map);
	}
	
	public static Cap capFrom(ItemStack stack){
		return Cap.byName(stack.getOrCreateNbt().getString("cap_id"));
	}
	
	public static Core coreFrom(ItemStack stack){
		return Core.byName(stack.getOrCreateNbt().getString("core_id"));
	}
	
	public static ItemStack focusFrom(ItemStack stack){
		return ItemStack.fromNbt(stack.getOrCreateNbt().getCompound("focus"));
	}
	
	public static void putFocus(ItemStack wand, ItemStack focus){
		var focusTag = new NbtCompound();
		focus.writeNbt(focusTag);
		wand.setSubNbt("focus", focusTag);
	}
	
	public static void updateFocus(ItemStack wand, Consumer<ItemStack> updater){
		ItemStack focusStack = focusFrom(wand);
		updater.accept(focusStack);
		putFocus(wand, focusStack);
	}
	
	public static boolean isContinuousCasting(ItemStack wand){
		return wand.getOrCreateNbt().getBoolean("continuous_casting");
	}
	
	public static void setIsContinuousCasting(ItemStack wand, boolean value){
		wand.getOrCreateNbt().putBoolean("continuous_casting", value);
	}
	
	public int warping(ItemStack stack, PlayerEntity player){
		return capFrom(stack).warping() + coreFrom(stack).warping();
	}
	
	public static int percentOff(Aspect aspect, ItemStack stack, PlayerEntity player){
		return capFrom(stack).percentOff(aspect) + coreFrom(stack).percentOff(aspect);
	}
	
	public static int percentOffFromEquipment(Aspect aspect, PlayerEntity player){
		int ret = 0;
		
		for(ItemStack stack : player.getArmorItems())
			if(stack.getItem() instanceof VisDiscountingItem vdi)
				ret += vdi.percentOff(stack, aspect, player);
		
		var tcomp = TrinketsApi.getTrinketComponent(player);
		if(tcomp.isPresent())
			for(Pair<SlotReference, ItemStack> pair : tcomp.get().getAllEquipped())
				if(pair.getRight().getItem() instanceof VisDiscountingItem vdi)
					ret += vdi.percentOff(pair.getRight(), aspect, player);
		return ret;
	}
	
	public static float costMultiplier(Aspect aspect, ItemStack stack, PlayerEntity player){
		return (100 - (percentOff(aspect, stack, player) + percentOffFromEquipment(aspect, player))) / 100f;
	}
	
	public static int capacity(ItemStack stack){
		return capFrom(stack).capacity() + coreFrom(stack).capacity();
	}
	
	public static int focusStrength(ItemStack stack, @Nullable PlayerEntity player){
		int strength = capFrom(stack).strength() + coreFrom(stack).strength();
		if(player != null && player.hasStatusEffect(ArcanaRegistry.ARCANE_AURA))
			strength += (int)(strength * 0.2);
		return strength;
	}
	
	public static int focusComplexity(ItemStack stack){
		return capFrom(stack).complexity() + coreFrom(stack).complexity();
	}
}