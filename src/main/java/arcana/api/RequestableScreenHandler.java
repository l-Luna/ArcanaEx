package arcana.api;

import org.jetbrains.annotations.Nullable;

public interface RequestableScreenHandler{
	
	void handleRequest(@Nullable String tag, int index, int data);
}