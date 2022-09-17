package arcana.nodes;

import arcana.aspects.ItemAspectRegistry;
import arcana.components.AuraWorld;
import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.FluidBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockPos.Mutable;
import net.minecraft.util.math.Direction;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.World;

import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;

import static arcana.Arcana.arcId;

public class NodeTypes{
	
	public static final BiMap<Identifier, NodeType> NODE_TYPES = HashBiMap.create();
	
	public static final NodeType
			NORMAL = create("normal", 43 * 20, 20),
			BRIGHT = create("bright", 33 * 20, 35),
			FADING = create("fading", 65 * 20, 13),
	
			HUNGRY = create("hungry", 40 * 20, 25, NodeTypes::tickHungry),
			ELDRITCH = create("eldritch", 49 * 20, 18),
			PURE = create("pure", 48 * 2, 23);
	
	public static final List<NodeType> normalTypes = List.of(NORMAL, BRIGHT, FADING);
	public static final List<NodeType> specialTypes = List.of(HUNGRY, ELDRITCH, PURE);
	
	private static NodeType create(String id, int rechargeTime, int aspectCap){
		return create(id, rechargeTime, aspectCap, null);
	}
	
	private static NodeType create(String id, int rechargeTime, int aspectCap, Consumer<Node> ticker){
		Identifier identifier = arcId(id);
		NodeType type = new NodeType(identifier, rechargeTime, aspectCap, ticker);
		NODE_TYPES.put(identifier, type);
		return type;
	}
	
	public static NodeType byName(Identifier id){
		return NODE_TYPES.get(id);
	}
	
	// TODO: config
	private static final float hungryCarryFraction = 0.2f;
	
	private static void tickHungry(Node node){
		World world = node.getWorld();
		BlockPos pos = new BlockPos(node);
		// check blocks in range
		int range = /*(int)(.7 * Math.sqrt(node.getAspects().asStacks().stream().mapToInt(AspectStack::amount).sum()) + 1)*/6;
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
							// spawn particles
							
							// TODO: min break time
							if(!world.isClient){
								ServerWorld sw = (ServerWorld)world;
								float hardness = state.getHardness(world, cursor);
								if(hardness != -1 && world.getRandom().nextInt((int)(hardness * 300) + 1) == 0){
									NbtCompound blocks = node.getOrCreateTag().getCompound("blocks");
									node.getTag().put("blocks", blocks);
									// keep track of broken blocks
									String key = Registry.BLOCK.getId(state.getBlock()).toString();
									blocks.putInt(key, blocks.getInt(key) + 1);
									// gain some of its aspects
									for(ItemStack stack : Block.getDroppedStacks(state, sw, cursor, world.getBlockEntity(cursor))){
										var aspects = ItemAspectRegistry.get(stack).copy();
										aspects.multiply(__ -> hungryCarryFraction);
										node.getAspects().add(aspects);
									}
									// sync
									AuraWorld.from(world).sync();
									// destroy block
									world.removeBlock(cursor, false);
								}
							}
						}
					}
				}
			}
		}
		// create disc particles
	}
	
	private static boolean empty(BlockState state){
		return state.isAir() || state.getBlock() instanceof FluidBlock;
	}
}