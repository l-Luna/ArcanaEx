package arcana.research;

import net.minecraft.util.Identifier;

public record Parent(Identifier id, int stage, boolean show, boolean hasArrowhead, boolean showReverse){
	
	// ~ means don't show
	// & means no arrowhead
	// / means reversed
	// xyz@s means "entry xyz at stage s"
	// e.g. "&/arcana:pipe_info@2" could mean "no-arrowhead reversed line to pipe_info unlocked at stage 2"
	// (though no-arrowhead should no be used with set stages)
	
	public String asString(){
		StringBuilder builder = new StringBuilder(id.toString().length() + 3);
		if(!show)
			builder.append("~");
		if(!hasArrowhead)
			builder.append("&");
		if(showReverse)
			builder.append("/");
		
		builder.append(id);
		
		if(stage != -1)
			builder.append("@").append(stage);
		
		return builder.toString();
	}
	
	// TODO: relax restriction on prefix symbol order
	public static Parent parse(String s){
		boolean show = true;
		if(s.startsWith("~")){
			show = false; s = s.substring(1);
		}
		boolean hasArrowhead = true;
		if(s.startsWith("&")){
			hasArrowhead = false; s = s.substring(1);
		}
		boolean showReverse = false;
		if(s.startsWith("/")){
			showReverse = true; s = s.substring(1);
		}
		if(s.contains("@")){
			Identifier id = new Identifier(s.substring(0, s.indexOf("@")));
			int stage = Integer.parseInt(s.substring(s.indexOf("@") + 1));
			return new Parent(id, stage, show, hasArrowhead, showReverse);
		}else
			return new Parent(new Identifier(s), -1, show, hasArrowhead, showReverse);
	}
	
	public String toString(){
		return asString();
	}
}
