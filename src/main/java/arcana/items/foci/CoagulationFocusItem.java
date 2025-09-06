package arcana.items.foci;

import arcana.items.FocusItem;

public class CoagulationFocusItem extends FocusItem{
	
	public CoagulationFocusItem(Settings settings){
		super(settings);
	}
	
	public boolean isContinuous(){
		return true;
	}
}