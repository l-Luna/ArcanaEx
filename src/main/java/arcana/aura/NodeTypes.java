package arcana.aura;

import arcana.Arcana;
import arcana.ArcanaDamageSources;
import arcana.ArcanaRegistry;
import arcana.aspects.ItemAspectRegistry;
import arcana.util.SearchUtil;
import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.FluidBlock;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.particle.BlockStateParticleEffect;
import net.minecraft.registry.Registries;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Identifier;
import net.minecraft.util.TypeFilter;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockPos.Mutable;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.BiConsumer;

import static arcana.Arcana.arcId;

public class NodeTypes{
	
	public static final List<NodeType> ALL = new ArrayList<>();
	public static final BiMap<Identifier, NodeType> NODE_TYPES = HashBiMap.create();
	
	public static final NodeType
			NORMAL = create("normal", 43 * 20, 20),
			BRIGHT = create("bright", 33 * 20, 35),
			FADING = create("fading", 65 * 20, 13),
	
			HUNGRY = create("hungry", 40 * 20, 25, NodeTypes::tickHungry),
			ELDRITCH = create("eldritch", 49 * 20, 18),
			PURE = create("pure", 48 * 20, 23, NodeTypes::tickPure),
			TAINTED = create("tainted", 60 * 20, 12, NodeTypes::tickTainted);
	
	public static final List<NodeType> normalTypes = List.of(NORMAL, BRIGHT, FADING);
	public static final List<NodeType> specialTypes = List.of(HUNGRY, ELDRITCH, PURE);
	
	private static NodeType create(String id, int rechargeTime, int aspectCap){
		return create(id, rechargeTime, aspectCap, null);
	}
	
	private static NodeType create(String id, int rechargeTime, int aspectCap, BiConsumer<Node, World> ticker){
		Identifier identifier = arcId(id);
		NodeType type = new NodeType(identifier, rechargeTime, aspectCap, ticker);
		NODE_TYPES.put(identifier, type);
		ALL.add(type);
		return type;
	}
	
	public static NodeType byName(Identifier id){
		return NODE_TYPES.get(id);
	}
	
	public static NodeType cycle(NodeType prev){
		return ALL.get((ALL.indexOf(prev) + 1) % ALL.size());
	}
	
	public static @Nullable NodeType weakerType(NodeType type){
		if(type == FADING || type == TAINTED)
			return null;
		if(type == NORMAL)
			return FADING;
		if(type == BRIGHT)
			return NORMAL;
		
		if(type == HUNGRY)
			return HUNGRY;
		
		return FADING; // PURE and ELDRITCH
	}
	
	public static @NotNull NodeType strongerType(NodeType type){
		if(type == FADING)
			return NORMAL;
		if(type == NORMAL)
			return BRIGHT;
		
		return type;
	}
	
	// TODO: data
	private static final float hungryCarryFraction = 0.4f;
	
	private static <T extends Entity> void tickHungry(Node node, World world){
		BlockPos pos = node.asBlockPos();
		int range = /*(int)(.7 * Math.sqrt(node.getAspects().asStacks().stream().mapToInt(AspectStack::amount).sum()) + 1)*/6;
		// check blocks in range
		Mutable cursor = new Mutable();
		for(int x = -range; x < range; x++){
			for(int y = -range; y < range; y++){
				for(int z = -range; z < range; z++){
					cursor.set(pos).move(x, y, z);
					if(x * x + y * y + z * z > range * range)
						continue;
					// if they have an empty neighbor,
					BlockState state = world.getBlockState(cursor);
					if(!empty(state)){
						if(Arrays.stream(Direction.values()).anyMatch(dir -> empty(world.getBlockState(cursor.offset(dir))))){
							// ticking always happens on the server, so this always passes
							// (TODO: just pass serverworld?)
							if(world instanceof ServerWorld sw){
								// spawn particles
								if(world.getRandom().nextInt(2) == 0){
									float xR = world.getRandom().nextFloat(), yR = world.getRandom().nextFloat(), zR = world.getRandom().nextFloat();
									Vec3d fromPos = new Vec3d(cursor.getX() + xR, cursor.getY() + yR, cursor.getZ() + zR);
									Vec3d mov = node.asVec3d().subtract(fromPos).multiply(1/20f);
									sw.spawnParticles(new BlockStateParticleEffect(ArcanaRegistry.HUNGRY_NODE_BLOCK, state), fromPos.x, fromPos.y, fromPos.z, 0, mov.x, mov.y, mov.z, 1);
								}
								
								// TODO: min break time
								float hardness = state.getHardness(world, cursor);
								if(hardness != -1 && world.getRandom().nextInt((int)(hardness * 300) + 1) == 0){
									NbtCompound blocks = node.getOrCreateTag().getCompound("blocks");
									node.getTag().put("blocks", blocks);
									// keep track of broken blocks
									String key = Registries.BLOCK.getId(state.getBlock()).toString();
									blocks.putInt(key, blocks.getInt(key) + 1);
									// gain some of its aspects
									for(ItemStack stack : Block.getDroppedStacks(state, sw, cursor, world.getBlockEntity(cursor))){
										var aspects = ItemAspectRegistry.get(stack).copy();
										aspects.multiply(hungryCarryFraction);
										node.getAspects().add(aspects);
										node.markDirty();
									}
									// destroy block
									world.removeBlock(cursor, false);
								}
							}
						}
					}
				}
			}
		}
		// pull items nearby
		for(ItemEntity entity : world.getEntitiesByType(EntityType.ITEM, new Box(pos).expand(range), x -> x.getPos().isInRange(node, range))){
			Vec3d toVec = node.asVec3d().subtract(entity.getPos()).normalize().multiply(0.1f);
			entity.addVelocity(toVec.x, toVec.y, toVec.z);
		}
		
		// and damage entities that touch it
		for(Entity entity : world.getEntitiesByType(TypeFilter.instanceOf(LivingEntity.class), new Box(pos).expand(range), x -> x.getPos().isInRange(node, range * 0.75f)))
			entity.damage(ArcanaDamageSources.hungryNode(world), 2f);
		for(Entity entity : world.getEntitiesByType(EntityType.ITEM, new Box(pos).expand(1), x -> x.getPos().isInRange(node, Node.HALF_NODE)))
			entity.damage(ArcanaDamageSources.hungryNode(world), 2f);
		
		// make disc particles
		// disc radius = 1/3 * pull radius
		NbtCompound blocks = node.getTag().getCompound("blocks");
		if(!blocks.getKeys().isEmpty() && world instanceof ServerWorld sw){
			float discRad = (float)(range * (1 / 3f) + world.getRandom().nextGaussian() / 5f);
			float xPos = (float)(node.getX());
			float zPos = (float)(node.getZ() - discRad);
			// TODO: weighted selection
			BlockState state = (Registries.BLOCK.get(Identifier.of(blocks.getKeys().toArray(new String[0])[world.getRandom().nextInt(blocks.getKeys().size())]))).getDefaultState();
			sw.spawnParticles(new BlockStateParticleEffect(ArcanaRegistry.HUNGRY_NODE_DISC, state), xPos, node.getY(), zPos, 0, discRad / 6f, 0, discRad / 6f, 1);
		}
	}
	
	private static void tickPure(Node node, World world){
		if(world.random.nextInt(30) == 0)
			AuraWorld.from(world).incrementFlux(-world.random.nextBetween(3, 8), null, node.asBlockPos());
		
		if(world.random.nextInt(80) == 0)
			SearchUtil.randomSearch(world, node.asBlockPos(), 5, 3, (pos, state) -> InfestedChunk.setInfested(world, pos, false) || Taint.untaintBlock(world, pos));
	}
	
	private static void tickTainted(Node node, World world){
		if(world.random.nextInt(Arcana.CONFIG.taintConfig.taintedNodeFluxInvChance) == 0)
			AuraWorld.from(world).incrementFlux(world.random.nextBetween(1, 4), null, node.asBlockPos());
		
		if(world.random.nextInt(Arcana.CONFIG.taintConfig.taintedNodeInfestInvChance) == 0)
			SearchUtil.randomSearch(world, node.asBlockPos(), 7, 12, (pos, state) -> !state.isAir() && InfestedChunk.setInfested(world, pos, true));
	}
	
	private static boolean empty(BlockState state){
		return state.isAir() || state.getBlock() instanceof FluidBlock;
	}
}