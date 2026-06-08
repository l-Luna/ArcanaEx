package arcana.worldgen.mushroom;

import arcana.ArcanaTags;
import net.minecraft.block.Block;
import net.minecraft.structure.StructurePlacementData;
import net.minecraft.structure.StructureTemplate;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3i;
import net.minecraft.world.StructureWorldAccess;
import net.minecraft.world.gen.feature.Feature;
import net.minecraft.world.gen.feature.util.FeatureContext;

public class StructureMushroomFeature extends Feature<StructureMushroomFeatureConfig>{
	
	public StructureMushroomFeature(){
		super(StructureMushroomFeatureConfig.CODEC);
	}
	
	public boolean generate(FeatureContext<StructureMushroomFeatureConfig> context){
		StructureMushroomFeatureConfig config = context.getConfig();
		int baseStemHeight = config.stemHeight().get(context.getRandom()),
			maxStemHeight = config.stemHeight().getMax();
		StructureWorldAccess world = context.getWorld();
		StructureTemplate template = world.toServerWorld().getStructureTemplateManager().getTemplateOrBlank(config.capStructureId());
		Vec3i size = template.getSize();
		BlockPos origin = context.getOrigin();
		BlockPos capCornerOrigin = origin.add(-size.getX() / 2, 0, -size.getZ() / 2);
		// check for all valid heights
		height:
		for(int h = baseStemHeight; h <= maxStemHeight; h++){
			// check if everything in bounds is replaceable; if not, move up
			for(int x = 0; x < size.getX(); x++)
				for(int y = 0; y < size.getY(); y++)
					for(int z = 0; z < size.getZ(); z++){
						BlockPos here = capCornerOrigin.add(x, h + y, z);
						if(!world.getBlockState(here).isAir() && !world.getBlockState(here).isIn(ArcanaTags.HUGE_MUSHROOM_REPLACEABLES))
							continue height;
					}
			
			// found a valid height, generate here
			for(int i = 0; i < h; i++){
				BlockPos here = origin.up(i);
				world.setBlockState(here, config.stemProvider().getBlockState(context.getRandom(), here), Block.NOTIFY_ALL);
			}
			BlockPos capPos = capCornerOrigin.up(h);
			template.place(world, capPos, origin, new StructurePlacementData().setPosition(capPos), context.getRandom(), Block.NOTIFY_ALL);
			return true;
		}
		// all heights were invalid
		return false;
	}
}