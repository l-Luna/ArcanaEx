package arcana.client.research;

import arcana.Arcana;
import arcana.ArcanaRegistry;
import arcana.blocks.WardedCampfireBlock;
import net.createmod.catnip.math.Pointing;
import net.createmod.ponder.api.registration.IndexExclusionHelper;
import net.createmod.ponder.api.registration.PonderPlugin;
import net.createmod.ponder.api.registration.PonderSceneRegistrationHelper;
import net.createmod.ponder.api.scene.SceneBuilder;
import net.createmod.ponder.api.scene.SceneBuildingUtil;
import net.createmod.ponder.api.scene.Selection;
import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import org.jetbrains.annotations.NotNull;

import static arcana.Arcana.arcId;

public class ArcanaPonderPlugin implements PonderPlugin{
	
	public @NotNull String getModId(){
		return Arcana.MODID;
	}
	
	public void registerScenes(PonderSceneRegistrationHelper<Identifier> helper){
		helper.addStoryBoard(arcId("warded_campfire_diagram"), arcId("example_ponder"), ArcanaPonderPlugin::examplePonder);
	}
	
	// do not show arcana ponders in the index
	public void indexExclusions(IndexExclusionHelper helper){
		helper.exclude(x -> Registries.ITEM.getId(x.asItem()).getNamespace().equals(Arcana.MODID));
	}
	
	private static void examplePonder(SceneBuilder scene, SceneBuildingUtil util){
		scene.world().showIndependentSectionImmediately(util.select().layersFrom(0));
		Selection campfirePos = util.select().position(2, 1, 2);
		scene.idle(10);
		scene.overlay().showControls(campfirePos.getCenter().add(0, 1, 0), Pointing.DOWN, 20)
				.rightClick()
				.withItem(ArcanaRegistry.FIRE_FOCUS.getDefaultStack());
		scene.world().setBlock(new BlockPos(2, 1, 2), ArcanaRegistry.WARDED_CAMPFIRE.getDefaultState().with(WardedCampfireBlock.LIT, true), false);
	}
}