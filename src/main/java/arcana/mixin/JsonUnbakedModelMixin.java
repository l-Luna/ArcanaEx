package arcana.mixin;

import net.minecraft.client.render.model.ModelBakeSettings;
import net.minecraft.client.render.model.ModelLoader;
import net.minecraft.client.render.model.json.JsonUnbakedModel;
import net.minecraft.client.texture.MissingSprite;
import net.minecraft.client.texture.Sprite;
import net.minecraft.client.util.SpriteIdentifier;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

import java.util.function.Function;

@Mixin(JsonUnbakedModel.class)
public abstract class JsonUnbakedModelMixin{
	
	@Shadow public abstract SpriteIdentifier resolveSprite(String spriteName);
	
	@ModifyVariable(method = "bake(Lnet/minecraft/client/render/model/ModelLoader;Lnet/minecraft/client/render/model/json/JsonUnbakedModel;Ljava/util/function/Function;Lnet/minecraft/client/render/model/ModelBakeSettings;Lnet/minecraft/util/Identifier;Z)Lnet/minecraft/client/render/model/BakedModel;",
	                at = @At("STORE"),
	                name = "sprite", ordinal = 0)
	private Sprite modifyItemParticleSprite(Sprite old, ModelLoader loader, JsonUnbakedModel parent, Function<SpriteIdentifier, Sprite> textureGetter, ModelBakeSettings settings, Identifier id, boolean hasDepth){
		if(old.getId().equals(MissingSprite.getMissingSpriteId()))
			return textureGetter.apply(resolveSprite("layer0"));
		return old;
	}
}