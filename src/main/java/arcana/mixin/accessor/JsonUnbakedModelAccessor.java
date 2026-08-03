package arcana.mixin.accessor;

import com.mojang.datafixers.util.Either;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.render.model.json.JsonUnbakedModel;
import net.minecraft.client.util.SpriteIdentifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.Map;

@Mixin(JsonUnbakedModel.class)
@Environment(EnvType.CLIENT)
public interface JsonUnbakedModelAccessor{
	
	@Accessor
	Map<String, Either<SpriteIdentifier, String>> getTextureMap();
}