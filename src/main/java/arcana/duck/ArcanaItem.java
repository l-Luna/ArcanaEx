package arcana.duck;

import arcana.items.FragileComponent;
import org.jetbrains.annotations.Nullable;

public interface ArcanaItem{

	@Nullable
	FragileComponent arcana$getFragileComponent();
}