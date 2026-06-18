package arcana.mixin.accessor;

import net.minecraft.client.render.model.BasicBakedModel;
import net.minecraft.client.render.model.json.ModelOverrideList;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(BasicBakedModel.class)
public interface BasicBakedModelAccessor{
	
	@Accessor("itemPropertyOverrides")
	@Mutable
	void arcana$setItemPropertyOverrides(ModelOverrideList overrides);
}