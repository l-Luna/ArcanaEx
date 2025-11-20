package arcana.client.particles;

public enum CubeParticleStyle{
	STATIC{
		float easeAlpha(float o){
			return 1 - o;
		}
		
		float easeScale(float o){
			return 1;
		}
		
		int maxLife(){
			return 30;
		}
	},
	APPEAR{
		float easeAlpha(float o){
			return 1 - 4 * (o - .5f) * (o - .5f);
		}
		
		float easeScale(float o){
			return 1 + 0.5f*(1 - (1-(1-o)*(1-o)*(1-o)));
		}
		
		int maxLife(){
			return 20;
		}
	},
	DISAPPEAR{
		float easeAlpha(float o){
			return 1 - o;
		}
		
		float easeScale(float o){
			return 1 + 0.5f*(1-(1-o)*(1-o)*(1-o));
		}
		
		int maxLife(){
			return 10;
		}
	};
	
	abstract float easeAlpha(float o);
	
	abstract float easeScale(float o);
	
	abstract int maxLife();
}