package arcana.items;

import arcana.ArcanaRegistry;
import arcana.api.*;
import arcana.aspects.*;
import arcana.aura.AuraWorld;
import arcana.aura.Node;
import arcana.blocks.be.InfusionMatrixBlockEntity;
import arcana.client.ArcanaClient;
import arcana.components.Caster;
import arcana.components.Researcher;
import dev.emi.trinkets.api.SlotReference;
import dev.emi.trinkets.api.TrinketsApi;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.item.v1.FabricItem;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.StackReference;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.screen.slot.Slot;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.*;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;

public class WandItem extends Item implements FabricItem, WarpingItem{
	
	public WandItem(Item.Settings settings){
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
	
	// TODO
	/*public void appendStacks(ItemGroup group, DefaultedList<ItemStack> stacks){
		if(isIn(group)){
			stacks.add(withCapAndCore(ArcanaRegistry.IRON_WAND_CAP, ArcanaRegistry.STICK_CORE));
			stacks.add(withCapAndCore(ArcanaRegistry.GOLD_WAND_CAP, ArcanaRegistry.GREATWOOD_WAND_CORE));
			stacks.add(withCapAndCore(ArcanaRegistry.THAUMIUM_WAND_CAP, ArcanaRegistry.SILVERWOOD_WAND_CORE));
			stacks.add(withCapAndCore(ArcanaRegistry.NETHERITE_WAND_CAP, ArcanaRegistry.ARCANIUM_WAND_CORE));
		}
	}*/
	
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
			ScaledAspectMap cost = fi.castCost(wandStack, focusStack, player);
			cost.multiply(aspect -> costMultiplier(aspect, wandStack, player));
			if(aspectsFrom(wandStack).contains(cost)){
				ActionResult result = fi.castOnBlock(context);
				Researcher.from(player).markFocusCast(fi);
				if(result != ActionResult.PASS && result != ActionResult.FAIL){
					// no point charging for something that didn't work
					updateAspects(wandStack, aspects -> aspects.take(cost));
					// updateFocus(wandStack, oldFocus -> oldFocus.setDamage(focusStack.getDamage()));
				}
				return result;
			}
		}
		
		return ActionResult.PASS;
	}
	
	public ActionResult useOnEntity(ItemStack stack, PlayerEntity user, LivingEntity entity, Hand hand){
		// creative mode "helpfully" copies the stack before use on entities, so we get the real thing here
		ItemStack wandStack = user.getStackInHand(hand);
		ItemStack focusStack = focusFrom(wandStack);
		ScaledAspectMap stored = aspectsFrom(wandStack);
		if(focusStack.getItem() instanceof FocusItem fi){
			var cost = fi.castCost(wandStack, focusStack, user);
			cost.multiply(aspect -> costMultiplier(aspect, stack, user));
			if(stored.contains(cost)){
				ActionResult result = fi.castOnEntity(wandStack, focusStack, user, entity);
				Researcher.from(user).markFocusCast(fi);
				if(result != ActionResult.PASS && result != ActionResult.FAIL){
					// no point charging for something that didn't work
					updateAspects(wandStack, aspects -> aspects.take(cost));
					// updateFocus(wandStack, oldFocus -> oldFocus.setDamage(focusStack.getDamage()));
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
		if(world.isClient || !(user instanceof PlayerEntity pe))
			return;
		
		AuraWorld aura = AuraWorld.from(world);
		Optional<Node> nodeO = aura.raycastNodes(user, false);
		nodeO.ifPresent(node -> Caster.from(pe).beginDraining(node, user.getActiveHand()));
		
		if(focusFrom(stack).getItem() instanceof Focus fi && fi.isContinuous())
			Caster.from(pe).beginContinuousCasting(user.getActiveHand());
	}
	
	public void onStoppedUsing(ItemStack stack, World world, LivingEntity user, int remainingUseTicks){
		if(user instanceof PlayerEntity pe)
			Caster.from(pe).endState();
	}
	
	public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand){
		user.setCurrentHand(hand);
		return TypedActionResult.consume(user.getStackInHand(hand));
	}
	
	public Optional<TooltipData> getTooltipData(ItemStack stack){
		return Optional.of(new WandAspectsTooltipData(stack));
	}
	
	public boolean onClicked(ItemStack wandStack, ItemStack otherStack, Slot slot, ClickType clickType, PlayerEntity player, StackReference ref){
		if(clickType == ClickType.RIGHT){
			ItemStack focus = focusFrom(wandStack);
			if(otherStack.isEmpty()){
				if(!focus.isEmpty()){
					ref.set(focus);
					putFocus(wandStack, ItemStack.EMPTY);
					return true;
				}
			}else if(otherStack.getItem() instanceof FocusItem && focus.isEmpty()){
				putFocus(wandStack, otherStack);
				ref.set(ItemStack.EMPTY);
				return true;
			}
		}
		return super.onClicked(wandStack, otherStack, slot, clickType, player, ref);
	}
	
	@Environment(EnvType.CLIENT)
	public void appendTooltip(ItemStack stack, @Nullable World world, List<Text> tooltip, TooltipContext context){
		appendTooltipImpl(stack, tooltip);
	}
	
	@Environment(EnvType.CLIENT) // Environment doesn't guarantee behaviour when used on override
	private void appendTooltipImpl(ItemStack stack, List<Text> tooltip){
		ItemStack focusStack = focusFrom(stack);
		PlayerEntity player = MinecraftClient.getInstance().player;
		if(focusStack.getItem() instanceof FocusItem fi){
			tooltip.add(fi.nameForTooltip(focusStack));
			ScaledAspectMap cost = fi.castCost(stack, focusStack, player);
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
	public static Text costText(ScaledAspectMap map){
		MutableText costs = Text.literal("");
		for(Aspect aspect : map.underlying().aspectSet())
			costs.append(Text.translatable("tooltip.arcana.wand.focus_cost.individual", map.get(aspect), aspect.name())
					.formatted(ArcanaClient.colourForPrimal(aspect)));
		if(map.underlying().isEmpty())
			costs.append(Text.translatable("tooltip.arcana.wand.focus_cost.empty").formatted(Formatting.GRAY));
		return Text.translatable("tooltip.arcana.wand.focus_cost.total", costs).formatted(Formatting.GRAY);
	}
	
	public boolean allowNbtUpdateAnimation(PlayerEntity player, Hand hand, ItemStack oldStack, ItemStack newStack){
		return !focusFrom(oldStack).getItem().equals(focusFrom(newStack).getItem());
	}
	
	public boolean isItemBarVisible(ItemStack stack){
		return focusFrom(stack).getMaxDamage() > 0;
	}
	
	public int getItemBarColor(ItemStack stack){
		return 0xF881D6;
	}
	
	public int getItemBarStep(ItemStack stack){
		ItemStack focusStack = focusFrom(stack);
		return focusStack.getMaxDamage() > 0 ? Math.round(13 - focusStack.getDamage() * 13f / focusStack.getMaxDamage()) : 0;
	}
	
	// TODO: NBT-backed aspect map?
	
	public static ScaledAspectMap aspectsFrom(ItemStack stack){
		return new ScaledAspectMap(AspectMap.fromNbt(stack.getSubNbt("aspects")), 0.1f);
	}
	
	public static void updateAspects(ItemStack stack, Consumer<ScaledAspectMap> updater){
		ScaledAspectMap map = aspectsFrom(stack);
		updater.accept(map);
		stack.getOrCreateNbt().put("aspects", map.underlying().toNbt());
	}
	
	public static Cap capFrom(ItemStack stack){
		return Cap.byName(stack.getOrCreateNbt().getString("cap_id"));
	}
	
	public static Core coreFrom(ItemStack stack){
		return Core.byName(stack.getOrCreateNbt().getString("core_id"));
	}
	
	public static @NotNull ItemStack focusFrom(ItemStack stack){
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