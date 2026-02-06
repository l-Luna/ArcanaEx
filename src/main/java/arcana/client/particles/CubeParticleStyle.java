package arcana.client.particles;

import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.random.Random;

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
	SHAKE{
		Vec3d easeOffset(float o, Random rng){
			return new Vec3d(rng.nextFloat() - 0.5f, rng.nextFloat() - 0.5f, rng.nextFloat() - 0.5f)
					.multiply(((1-o)*(1-o)) * 0.1f);
		}
		
		float easeAlpha(float o){
			return 1 - o;
		}
		
		float easeScale(float o){
			return 1.1f;
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
	
	Vec3d easeOffset(float o, Random rng){
		return Vec3d.ZERO;
	}
	
	abstract float easeAlpha(float o);
	
	abstract float easeScale(float o);
	
	abstract int maxLife();
}