package arcana.items;

import net.fabricmc.fabric.api.item.v1.FabricItemSettings;
import net.minecraft.entity.effect.StatusEffect;
import org.jetbrains.annotations.Nullable;

public class ArcanaItemSettings extends FabricItemSettings{

	@Nullable
	private FragileComponent fragileComponent;
	
	//
	
	public ArcanaItemSettings fragile(int colour){
		fragileComponent = new FragileComponent(colour);
		return this;
	}
	
	public ArcanaItemSettings fragile(int colour, StatusEffect impactEffect){
		fragileComponent = new FragileComponent(colour, impactEffect);
		return this;
	}
	
	//
	
	public @Nullable FragileComponent getFragileComponent(){
		return fragileComponent;
	}
}