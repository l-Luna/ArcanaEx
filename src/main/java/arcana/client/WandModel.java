package arcana.client;

import arcana.ArcanaRegistry;
import arcana.items.Cap;
import arcana.items.Core;
import arcana.items.FocusItem;
import arcana.items.WandItem;
import arcana.mixin.JsonUnbakedModelAccessor;
import com.mojang.datafixers.util.Either;
import com.mojang.datafixers.util.Pair;
import net.fabricmc.fabric.api.client.model.ModelProviderContext;
import net.fabricmc.fabric.api.client.model.ModelResourceProvider;
import net.minecraft.client.render.model.*;
import net.minecraft.client.render.model.json.JsonUnbakedModel;
import net.minecraft.client.render.model.json.ModelOverride;
import net.minecraft.client.render.model.json.ModelOverrideList;
import net.minecraft.client.texture.Sprite;
import net.minecraft.client.texture.SpriteAtlasTexture;
import net.minecraft.client.util.SpriteIdentifier;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Direction;
import net.minecraft.util.registry.Registry;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Stream;

import static arcana.Arcana.arcId;

public final class WandModel implements UnbakedModel{
	
	private static final List<SpriteIdentifier> texDeps = Stream.of(
					Cap.caps.values().stream().map(WandModel::capTexture),
					Core.cores.values().stream().map(WandModel::coreTexture),
					Registry.ITEM.stream().filter(FocusItem.class::isInstance).map(WandModel::focusTexture)
			).flatMap(x -> x).map(WandModel::atlased).toList();
	
	
	public static final Identifier wandModel = arcId("item/wand/wand");
	
	private static final Identifier defaultCoreTexId = coreTexture(ArcanaRegistry.STICK_CORE);
	private static final Identifier defaultCapTexId = capTexture(ArcanaRegistry.IRON_WAND_CAP);
	
	private static final List<Identifier> modelDeps = new ArrayList<>(Registry.ITEM.stream().filter(FocusItem.class::isInstance).map(WandModel::focusModel).toList());
	static{
		modelDeps.add(wandModel);
	}
	
	public Collection<Identifier> getModelDependencies(){
		return modelDeps;
	}
	
	public Collection<SpriteIdentifier> getTextureDependencies(Function<Identifier, UnbakedModel> ubModels, Set<Pair<String, String>> unresolvedTextureReferences){
		return texDeps;
	}
	
	public BakedModel bake(ModelLoader loader, Function<SpriteIdentifier, Sprite> textureGetter, ModelBakeSettings bakeSettings, Identifier modelId){
		return bakeWithTextures(loader, textureGetter, bakeSettings, modelId, defaultCoreTexId, defaultCapTexId, null);
	}
	
	@NotNull
	private static BakedModel bakeWithTextures(ModelLoader loader,
	                                           Function<SpriteIdentifier, Sprite> textureGetter,
	                                           ModelBakeSettings bakeSettings,
	                                           Identifier modelId,
	                                           Identifier coreTexId,
	                                           Identifier capTexId,
	                                           @Nullable Identifier focusModel){
		JsonUnbakedModel model = (JsonUnbakedModel)loader.getOrLoadModel(wandModel);
		// substitute sprites
		Map<String, Either<SpriteIdentifier, String>> texMap = ((JsonUnbakedModelAccessor)model).getTextureMap();
		texMap.put("core", Either.left(atlased(coreTexId)));
		texMap.put("cap", Either.left(atlased(capTexId)));
		// bake
		BakedModel baked = model.bake(loader, textureGetter, bakeSettings, modelId);
		assert baked != null;
		var bbm = (BasicBakedModel)baked;
		// merge focus model if needed
		if(focusModel != null){
			BasicBakedModel fbbm = (BasicBakedModel)loader.getOrLoadModel(focusModel).bake(loader, textureGetter, bakeSettings, focusModel);
			assert fbbm != null;
			for(BakedQuad quad : fbbm.getQuads(null, null, null))
				bbm.getQuads(null, null, null).add(quad);
			for(Direction dir : Direction.values())
				for(BakedQuad quad : fbbm.getQuads(null, dir, null))
					bbm.getQuads(null, dir, null).add(quad);
		}
		// apply overrides
		bbm.itemPropertyOverrides = new WandModelOverrideList(loader, model, loader::getOrLoadModel, List.of(), textureGetter, bakeSettings);
		return baked;
	}
	
	public static Identifier capTexture(Cap cap){
		return new Identifier(cap.id().getNamespace(), "item/wand/caps/" + cap.id().getPath());
	}
	
	public static Identifier coreTexture(Core core){
		return new Identifier(core.id().getNamespace(), "item/wand/cores/" + core.id().getPath());
	}
	
	public static Identifier focusTexture(Item focus){
		Identifier id = Registry.ITEM.getId(focus);
		return new Identifier(id.getNamespace(), "item/wand/foci/" + id.getPath());
	}
	
	public static Identifier focusModel(Item focus){
		Identifier id = Registry.ITEM.getId(focus);
		return new Identifier(id.getNamespace(), "item/wand/foci/" + id.getPath());
	}
	
	@SuppressWarnings("deprecation")
	public static SpriteIdentifier atlased(Identifier i){
		return new SpriteIdentifier(SpriteAtlasTexture.BLOCK_ATLAS_TEXTURE, i);
	}
	
	public static class WandModelOverrideList extends ModelOverrideList{
		
		private final ModelLoader loader;
		private final Function<SpriteIdentifier, Sprite> spriteFn;
		private final ModelBakeSettings mbs;
		
		public WandModelOverrideList(ModelLoader loader, JsonUnbakedModel parent, Function<Identifier, UnbakedModel> ubModels, List<ModelOverride> overrides, Function<SpriteIdentifier, Sprite> fn, ModelBakeSettings mbs){
			super(loader, parent, ubModels, overrides);
			this.loader = loader;
			spriteFn = fn;
			this.mbs = mbs;
		}
		
		@Nullable
		public BakedModel apply(BakedModel model, ItemStack stack, @Nullable ClientWorld world, @Nullable LivingEntity entity, int seed){
			Cap cap = WandItem.capFrom(stack);
			Core core = WandItem.coreFrom(stack);
			var fstack = WandItem.focusFrom(stack);
			Identifier f = fstack.isEmpty() ? null : focusModel(fstack.getItem());
			return bakeWithTextures(loader, spriteFn, mbs, arcId("wand"), coreTexture(core), capTexture(cap), f);
		}
	}
	
	public static class Provider implements ModelResourceProvider{
		
		private static final Identifier wandId = arcId("item/wand");
		
		public @Nullable UnbakedModel loadModelResource(Identifier id, ModelProviderContext ctx){
			return id.equals(wandId) ? new WandModel() : null;
		}
	}
}