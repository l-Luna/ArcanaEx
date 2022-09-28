package arcana.aspects;

import org.jetbrains.annotations.Nullable;

public interface SpeckIo{
	
	boolean accept(AspectSpeck speck);
	
	@Nullable
	AspectSpeck draw(int max);
}