package gameObjects;

import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;

import gameManagers.World;
import main.Main;

public class Rocket extends SpaceObject {
	
	private float[][] flame;
	private int[] flameFillColour, flameLineColour;
	private int height, dh, terminalVel;
	private boolean thrusting, left, right, flameOn, tripleMissile, invincible, shieldOn, continuousFire, respawn;
	private float tripleMissileTimer, invincibleTimer, continuousFireTimer, respawnTimer;
	private Shield shield;
	
	public Rocket(World world, int player) {
		super(world);
		vertices = new float[4][2];
		flame = new float[3][2];
		height = 80; 		
		r = height / 2;
		edges = vertices.length;
		dh = 4;
		terminalVel = 10;
		setColours(player);
		init();
		resetTripleMissileTimer();
		resetInvincibleTimer();
		resetContinuousFireTimer();
		resetRespawnTimer();
		respawn = false;
		shield = new Shield(world);
	}
	
	public void init() {
		left = right = flameOn = tripleMissile = invincible = shieldOn =  continuousFire = false;
		position = new Vector2(Main.getWidth() / 2, Main.getHeight() / 2);
		velocity = new Vector2(0, 0);
		heading = 90;
		respawn = true;
	}
	
	public void update(float delta) {
		if(respawn == false) {
			checkThrust(delta);	
			asteroidsG(delta);	
			terminalVelCheck();
			position.add(velocity);
			
			if(left) heading += dh;
			if(right) heading -= dh;
			
			wrap();
			setVertices();
			
		} else {
			respawnTimer -= delta;
			if(respawnTimer <= 0) {
				respawn = false;
				invincible = true;
				resetRespawnTimer();
			}
		}
		
		if(tripleMissile == true) {
			tripleMissileTimer -= delta;
			
			if(tripleMissileTimer <= 0) {
				tripleMissile = false;
				resetTripleMissileTimer();
			}
		}
		
		if(invincible == true) {
			invincibleTimer -= delta;
			
			if(invincibleTimer <= 0) {
				invincible = false;
				resetInvincibleTimer();
			}
		}
		
		if(continuousFire == true) {
			continuousFireTimer -= delta;
			
			if(continuousFireTimer <= 0) {
				setContinuousFire(false);
				resetContinuousFireTimer();
			}
		}

		if(shieldOn == true) {
			shield.update(delta, position.x, position.y, heading);
		}
		
		if(continuousFire == true) {
			int n;
			if(tripleMissile == true) {
				n = 3;
			} else {
				n = 1;
			}
			
			world.objSpawner.missile('r', n, position.x, position.y, heading, height, velocity, missileV, missileColour);
		}
	}
	
	private void checkThrust(float delta) {
		if(thrusting) {
			thrust(delta);
			flameOn = !flameOn;

		} else {
			flameOn = false;
		}
	}
	
	private void asteroidsG(float delta) {
		int numAsteroids = world.getNumAsteroids();
		
		for(int a = 0; a < numAsteroids; a++) { // Force required from each asteroid
			Asteroid ast = world.getAsteroid(a);
			Vector2 gForce = new Vector2();
			Vector2 asteroid = new Vector2(ast.getX(), ast.getY());
			
			float i = asteroid.x - position.x; // Relative position
			float j = asteroid.y - position.y;
			double r = Math.sqrt(Math.pow(i, 2) + Math.pow(j, 2)); // Magnitude of i and j
			
			double theta = Math.atan(j / i) * (180 / Math.PI); // Angle for force to be directed
			if(position.x > asteroid.x) {
				theta += 180;
			}
			
			double mag = 3 * ast.getArea() / Math.pow(r, 2); // Representation of gravitational force equation, F = GMm / r^2
			
			float radians = (float) Math.toRadians(theta);		
			gForce.x = (float) (MathUtils.cos(radians) * delta * mag);
			gForce.y = (float) (MathUtils.sin(radians) * delta * mag);
			velocity.add(gForce); // Apply gravitational force
		}
	}
	
	private void thrust(float delta) {
		Vector2 force = new Vector2();
		float radians = (float) Math.toRadians(heading);
		
		force.x = MathUtils.cos(radians) * delta * 10;
		force.y = MathUtils.sin(radians) * delta * 10;
		
		velocity.add(force);	
	}
	
	private void terminalVelCheck() {
		double resultantVel = Math.sqrt(Math.pow(velocity.x, 2) + Math.pow(velocity.y, 2)); // Pythagoras' theorem
		
		if(resultantVel > terminalVel) {
			velocity.x = (float) ((velocity.x / resultantVel) * terminalVel); // Reduce x and y components
			velocity.y = (float) ((velocity.y / resultantVel) * terminalVel);
		}
	}
	
	private void setColours(int player) {
		fillColour = new int[3];
		lineColour = new int[3];
		missileColour = new int[3];
		
		if(player == 1) {
			fillColour[0] = 51;
			fillColour[1] = 153;
			fillColour[2] = 255;
			
			lineColour[0] = 204;
			lineColour[1] = 0;
			lineColour[2] = 102;
		} else {
			fillColour[0] = 255;
			fillColour[1] = 51;
			fillColour[2] = 51;
			
			lineColour[0] = 102;
			lineColour[1] = 102;
			lineColour[2] = 255;
		}
	
		missileColour[0] = 255;
		missileColour[1] = 255;
		missileColour[2] = 0;
		
		flameFillColour = new int[3];
		flameFillColour[0] = 255;
		flameFillColour[1] = 128;
		flameFillColour[2] = 0;
		
		flameLineColour = new int[3];
		flameLineColour[0] = 255;
		flameLineColour[1] = 255;
		flameLineColour[2] = 0;
	}
	
	private void setVertices() {
		float radians = (float) Math.toRadians(heading);
		
		vertices[0][0] = position.x + MathUtils.cos(radians) * height / 2;
		vertices[0][1] = position.y + MathUtils.sin(radians) * height / 2;
		
		vertices[1][0] = position.x + MathUtils.cos(radians + 3 * MathUtils.PI / 4) * height / 3;
		vertices[1][1] = position.y + MathUtils.sin(radians + 3 * MathUtils.PI / 4) * height / 3;
		
		vertices[2][0] = position.x - MathUtils.cos(radians) * (height) / 6;
		vertices[2][1] = position.y - MathUtils.sin(radians) * (height) / 6;
				
		vertices[3][0] = position.x + MathUtils.cos(radians - 3 * MathUtils.PI / 4) * height / 3;
		vertices[3][1] = position.y + MathUtils.sin(radians - 3 * MathUtils.PI / 4) * height / 3;
		setFlame();
	}
	
	private void setFlame() {
		float radians = (float) Math.toRadians(heading - 90);
		flame[0][0] = position.x + MathUtils.cos(radians) * height / 6;
		flame[0][1] = position.y + MathUtils.sin(radians) * height / 6;
		
		radians = (float) Math.toRadians(heading + 90);
		flame[1][0] = position.x + MathUtils.cos(radians) * height / 6;
		flame[1][1] = position.y + MathUtils.sin(radians) * height / 6;
		
		radians = (float) Math.toRadians(heading + 180);
		flame[2][0] = position.x + MathUtils.cos(radians) * height / 2;
		flame[2][1] = position.y + MathUtils.sin(radians) * height / 2;
	}
	
	public void hyperspace() {
		position.x = rand.nextInt(Main.getWidth() + 1);
		position.y = rand.nextInt(Main.getHeight() + 1);
	}
	
	public void setRespawn(boolean b) {
		respawn = b;
	}
	
	public void setThrusting(boolean b) {
		thrusting = b;
	}
	
	public void setLeft(boolean b) {
		left = b;
	}
	
	public void setRight(boolean b) {
		right = b;
	}
	
	public void setTripleMissile(boolean b) {
		tripleMissile = b;
	}
	
	public void setInvincible(boolean b) {
		invincible = b;
	}
	
	public void setShieldOn(boolean b) {
		shieldOn = b;
	}
	
	public void setContinuousFire(boolean b) {
		if(continuousFire == true) {
			resetContinuousFireTimer();
		}
		
		continuousFire = b;
	}
	
	public void resetTripleMissileTimer() {
		tripleMissileTimer = 5;
	}
	
	private void resetInvincibleTimer() {
		invincibleTimer = 3;
	}
	
	private void resetContinuousFireTimer() {
		continuousFireTimer = 5;
	}
	
	private void resetRespawnTimer() {
		respawnTimer = 2;
	}
	
	public int[] getFlameFillColour() {
		return flameFillColour;
	}
	
	public int[] getFlameLineColour() {
		return flameLineColour;
	}
	
	public int getHeight() {
		return height;
	}
	
	public float[][] getFlameVertices() {
		return flame;
	}
	
	public boolean getFlameOn() {
		return flameOn;
	}
	
	public boolean getTripleMissile() {
		return tripleMissile;
	}
	
	public boolean getInvincible() {
		return invincible;
	}
	
	public boolean getShieldOn() {
		return shieldOn;
	}
	
	public boolean getContinuousFire() {
		return continuousFire;
	}
	
	public boolean getRespawn() {
		return respawn;
	}

	public Shield getShield() {
		return shield;
	}
}
