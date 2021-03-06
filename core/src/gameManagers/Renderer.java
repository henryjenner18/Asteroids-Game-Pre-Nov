package gameManagers;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;

import gameHelpers.AssetLoader;
import gameObjects.Asteroid;
import gameObjects.Fragment;
import gameObjects.Missile;
import gameObjects.PowerUp;
import gameObjects.Rocket;
import gameObjects.Shield;
import gameObjects.Spark;
import gameObjects.UFO;
import main.Main;

public class Renderer {
	
	private World world;
	private ShapeRenderer sr;
	private SpriteBatch batch;
	private int w, h, gameOverY;
	
	public Renderer(World world) {
		this.world = world;
		batch = AssetLoader.batch;
		sr = AssetLoader.sr;
		w = Main.getWidth();
		h = Main.getHeight();
		gameOverY = h + 150;
	}

	public void render(float delta) {
		drawBackground();
		drawPowerUps();
		drawSparks();
		drawAsteroids();
		drawFragments();
		drawMissiles();
		drawUFOs();
		drawShields();
		drawRockets();
		drawGameStats();
		drawText(delta);
	}
	
	private void drawText(float delta) {
		batch.begin();
		GlyphLayout layout = new GlyphLayout();
		String str;
		float strWidth, strHeight;
		
		if(world.isGameOver()) {
			if(Main.isSound()) {
				AssetLoader.inPlayMusic.setVolume((float) (AssetLoader.inPlayMusic.getVolume()*0.95));
			}
			
			int x = Gdx.input.getX();
			int y = h - Gdx.input.getY();
			
			// Game over
			str = "GAME OVER";	
			AssetLoader.font.setColor(Color.RED);
			layout.setText(AssetLoader.font, str);
			strWidth = layout.width;
			strHeight = layout.height;
			AssetLoader.font.draw(batch, str, w/2 - strWidth/2, gameOverY);
			AssetLoader.resetFont();

			if(gameOverY > h/2 + 2*strHeight) {
				gameOverY -= 5;
			}
			
			if(world.gameOverTimer > 0) {
				world.gameOverTimer -= delta;
				
				if(world.gameOverTimer > 1) {
					AssetLoader.translate();
				} else {
					AssetLoader.resetCam();
				}
				
			} else {
				if(Main.isSound()) {
					AssetLoader.inPlayMusic.stop();
				}
			
				Gdx.input.setCursorCatched(false);
				
				// Play again
				AssetLoader.resetFont();
				str = "Play again";
				layout.setText(AssetLoader.font, str);
				strWidth = layout.width;
				strHeight = layout.height;
						
				if(x >= w/2 - strWidth/2 && x <= w/2 + strWidth/2 &&
					y >= h/2 - 2*strHeight && y <= h/2 - strHeight) {
							
					AssetLoader.hoverFont();		
					if(Gdx.input.isTouched()) {
						world.restart();
						gameOverY = h + 150;
					}
				}
						
				AssetLoader.font.draw(batch, str, w/2 - strWidth/2, h/2 - strHeight);
				
				// Exit
				AssetLoader.resetFont();
				str = "Exit";
				layout.setText(AssetLoader.font, str);
				strWidth = layout.width;
				strHeight = layout.height;
						
				if(x >= w/2 - strWidth/2 && x <= w/2 + strWidth/2 &&
					y >= h/2 - 5*strHeight && y <= h/2 - 4*strHeight) {
	
					AssetLoader.hoverFont();		
					if(Gdx.input.isTouched()) {
						Gdx.app.exit();
					}
				}
						
				AssetLoader.font.draw(batch, str, w/2 - strWidth/2, h/2 - 4*strHeight);
				
				// Game time
				AssetLoader.resetFont();
				float gameTimer = world.getGameTimer();
				int mins = MathUtils.floor(gameTimer / 60);
				int secs = Math.round(gameTimer % 60);
				String strTimer = mins + " mins " + secs + " secs";
				
				// High score
				world.compareHighScore(Main.isTwoPlayer());
				
				String strHS;
				
				if(Main.isTwoPlayer() == false) {
					if(world.getScore() == AssetLoader.getHighScore1P()) {
						strHS = "New solo high score!";
					
					} else {
						strHS = "Solo high score: " + AssetLoader.getHighScore1P();
					}
					
				} else {
					if(world.getScore() == AssetLoader.getHighScore2P()) {
						strHS = "New co-op high score!";
					
					} else {
						strHS = "Co-op high score: " + AssetLoader.getHighScore2P();
					}
				}
		
				str = strTimer + "    " + strHS;
				layout.setText(AssetLoader.font, str);
				strWidth = layout.width;
				strHeight = layout.height;
				AssetLoader.font.draw(batch, str, w/2 - strWidth/2, h/2 + 5*strHeight);
			}
		}
		
		if(world.isPause()) {
			// Pause
			str = "II";	
			layout.setText(AssetLoader.font, str);
			strWidth = layout.width;
			strHeight = layout.height;
			AssetLoader.font.draw(batch, str, w/2 - strWidth/2, strHeight + 10);
			
			// Exit and restart
			BitmapFont f = new BitmapFont();
			str = "Press E to exit, R to restart or P to resume";
			layout.setText(f, str);
			strWidth = layout.width;
			strHeight = layout.height;
			f.draw(batch, str, w - strWidth - 10, strHeight + 10);
			
			// Sound
			if(Main.isSound()) {
				str = "Press O to turn sound OFF";
			} else {
				str = "Press O to turn sound ON";
			}
			layout.setText(f, str);
			strWidth = layout.width;
			strHeight = layout.height;
			f.draw(batch, str, 10, strHeight + 10);
			
			if(Gdx.input.isKeyPressed(Input.Keys.E)) {
				Gdx.app.exit();
				
			} else if(Gdx.input.isKeyPressed(Input.Keys.R)) {
				world.restart();
			}
		}
		
		batch.end();
	}
	
	private void drawLives() {
		int lives = world.getLives();
		int height = 80;
		int r = height / 2;
		int width = (r * lives) + (lives - 1) * 10;
		Vector2 position = new Vector2((w / 2) - (width / 2) + (r / 2), h - height / 2 - 15);
		
		for(int i = 0; i < lives; i++) {
			float vertices[][] = new float[4][2];
			
			vertices[0][0] = position.x;
			vertices[0][1] = position.y + (height / 2);
			
			vertices[1][0] = position.x + MathUtils.cos(5 * MathUtils.PI / 4) * height / 3;
			vertices[1][1] = position.y + MathUtils.sin(5 * MathUtils.PI / 4) * height / 3;
			
			vertices[2][0] = position.x;
			vertices[2][1] = position.y - (height / 6);
					
			vertices[3][0] = position.x + MathUtils.cos(- MathUtils.PI / 4) * height / 3;
			vertices[3][1] = position.y + MathUtils.sin(- MathUtils.PI / 4) * height / 3;
			
			sr.begin(ShapeType.Filled);
			Gdx.gl.glEnable(GL20.GL_BLEND);
			sr.setColor(51/255f, 153/255f, 255/255f, 0.5f);
			sr.triangle(vertices[0][0], vertices[0][1],
					vertices[1][0], vertices[1][1],
					vertices[2][0], vertices[2][1]);
			sr.triangle(vertices[0][0], vertices[0][1],
					vertices[3][0], vertices[3][1],
					vertices[2][0], vertices[2][1]);
			sr.end();
			
			float polygon[] = polygonArray(vertices, 4);
			Gdx.gl.glLineWidth(3);
			sr.begin(ShapeType.Line);
			sr.setColor(204/255f, 0/255f, 102/255f, 0.5f);
			sr.polygon(polygon);
			sr.end();
			
			position.x += r + 10;
		}
	}
	
	private void drawGameStats() {
		batch.begin();
			
		// Score
		int sc = world.getScore();
		String score = sc + "";
			
		AssetLoader.font.draw(batch, score, 10, h - 10);
			
		// Level
		int lvl = world.getLevel();
		
		if(world.isNextLevel() == false) {
			String level = "Lvl " + lvl;
				
			GlyphLayout layout = new GlyphLayout();
			layout.setText(AssetLoader.font, level);
			float levelWidth = layout.width;
		
			AssetLoader.font.draw(batch, level, w - levelWidth - 10, h - 10);
		}
		batch.end();
			
		drawLives();
	}
	
	private void drawPowerUps() {
		int numPowerUps = world.getNumPowerUps();
		
		for(int i = 0; i < numPowerUps; i++) {
			PowerUp p = world.getPowerUps().get(i);
			
			float x = p.getX();
			float y = p.getY();
			float vertices[][] = p.getVertices();
			int edges = p.getEdges();	
			int[] fillColour = p.getFillColour();
			int[] lineColour = p.getLineColour();
			
			sr.setColor(fillColour[0]/255f, fillColour[1]/255f, fillColour[2]/255f, 1);
			drawFilledPolygon(x, y, vertices, edges);
			
			// Polygon outline
			Gdx.gl.glLineWidth(3);
			sr.setColor(lineColour[0]/255f, lineColour[1]/255f, lineColour[2]/255f, 1);
			drawPolygonOutline(vertices, edges);
		}
	}
	
	private void drawSparks() {
		int numSparks = world.getNumSparks();
		
		for(int i = 0; i < numSparks; i++) {
			Spark s = world.getSpark(i);
			
			float x = s.getX();
			float y = s.getY();
			float r = s.getR();			
			int[] fillColour = s.getFillColour();
			
			sr.begin(ShapeType.Filled);
			sr.setColor(fillColour[0]/255f, fillColour[1]/255f, fillColour[2]/255f, 1);
			sr.circle(x, y, r);
			sr.end();
		}
	}

	private void drawRockets() {
		Gdx.gl.glEnable(GL20.GL_BLEND);
		Gdx.gl.glLineWidth(3);
		
		int numRockets = world.getNumRockets();
		
		for(int i = 0; i < numRockets; i++) {
			Rocket r = world.getRocket(i);
			
			if(r.getRespawn() == false) {
				// Flame
				float vertices[][] = r.getFlameVertices();
				int edges = vertices.length;
				float[] polygon = polygonArray(vertices, edges);
				
				int[] fillColour = r.getFlameFillColour();
				int[] lineColour = r.getFlameLineColour();
				
				boolean invincible;		
				if(r.getInvincible() == true) {
					invincible = true;
				} else {
					invincible = false;
				}
				
				if(r.getFlameOn() == true && invincible == false) {
					// Filled flame
					sr.begin(ShapeType.Filled);
					sr.setColor(fillColour[0]/255f, fillColour[1]/255f, fillColour[2]/255f, 1);			
					sr.triangle(vertices[0][0], vertices[0][1],
							vertices[1][0], vertices[1][1],
							vertices[2][0], vertices[2][1]);
					sr.end();
					
					// Flame outline
					sr.begin(ShapeType.Line);
					sr.setColor(lineColour[0]/255f, lineColour[1]/255f, lineColour[2]/255f, 1);
					sr.polygon(polygon);
					sr.end();
				}
				
				// Rocket
				vertices = r.getVertices();
				edges = r.getEdges();
				polygon = polygonArray(vertices, edges);		
				fillColour = r.getFillColour();
				lineColour = r.getLineColour();
				
				// Filled polygon
				sr.begin(ShapeType.Filled);
				
				if(invincible == true) {
					sr.setColor(fillColour[0]/255f, fillColour[1]/255f, fillColour[2]/255f, 0.5f);

				} else {
					sr.setColor(fillColour[0]/255f, fillColour[1]/255f, fillColour[2]/255f, 1);
				}
				
				sr.triangle(vertices[0][0], vertices[0][1],
						vertices[1][0], vertices[1][1],
						vertices[2][0], vertices[2][1]);
				sr.triangle(vertices[0][0], vertices[0][1],
						vertices[3][0], vertices[3][1],
						vertices[2][0], vertices[2][1]);
				sr.end();
							
				// Polygon outline
				sr.begin(ShapeType.Line);
				
				if(invincible == true) {
					sr.setColor(lineColour[0]/255f, lineColour[1]/255f, lineColour[2]/255f, 0.5f);

				} else {
					sr.setColor(lineColour[0]/255f, lineColour[1]/255f, lineColour[2]/255f, 1);
				}
				
				sr.polygon(polygon);
				sr.end();
			}
		}
	}
	
	private void drawAsteroids() {
		int numAsteroids = world.getNumAsteroids();
		
		for(int i = 0; i < numAsteroids; i++) {
			Asteroid a = world.getAsteroid(i);
			
			float x = a.getX();
			float y = a.getY();
			float vertices[][] = a.getVertices();
			int edges = a.getEdges();		
			int[] fillColour = a.getFillColour();
			int[] lineColour = a.getLineColour();
			
			sr.setColor(fillColour[0]/255f, fillColour[1]/255f, fillColour[2]/255f, 1);
			drawFilledPolygon(x, y, vertices, edges);
			
			// Polygon outline
			Gdx.gl.glLineWidth(6);
			sr.setColor(lineColour[0]/255f, lineColour[1]/255f, lineColour[2]/255f, 1);
			drawPolygonOutline(vertices, edges);
		}
	}
	
	private void drawMissiles() {
		int numMissiles = world.getNumMissiles();
		
		for(int i = 0; i < numMissiles; i++) {
			Missile m = world.getMissile(i);
			
			float vertices[][] = m.getVertices();
			int[] lineColour = m.getLineColour();
			Gdx.gl.glLineWidth(5);
			sr.begin(ShapeType.Line);
			sr.setColor(lineColour[0]/255f, lineColour[1]/255f, lineColour[2]/255f, 1);
			sr.line(vertices[0][0], vertices[0][1],
					vertices[1][0], vertices[1][1]);
			sr.end();
		}
	}
	
	private void drawUFOs() {
		int numUFOs = world.getNumUFOs();
		
		for(int i = 0; i < numUFOs; i++) {
			UFO u = world.getUFO(i);
			
			float x = u.getX();
			float y = u.getY();
			float vertices[][] = u.getVertices();
			int edges = u.getEdges();
			int[] fillColour = u.getFillColour();
			int[] lineColour = u.getLineColour();
			int[] flashColour = u.getFlashColour();
			
			sr.setColor(fillColour[0]/255f, fillColour[1]/255f, fillColour[2]/255f, 1);
			drawFilledPolygon(x, y, vertices, edges);
			
			Gdx.gl.glLineWidth(3);
			sr.setColor(lineColour[0]/255f, lineColour[1]/255f, lineColour[2]/255f, 1);
			drawPolygonOutline(vertices, edges);
			
			//Horizontal lines
			sr.begin(ShapeType.Line);
			sr.line(vertices[0][0], vertices[0][1], vertices[5][0], vertices[5][1]);
			sr.setColor(flashColour[0]/255f, flashColour[1]/255f, flashColour[2]/255f, 1);	
			sr.line(vertices[1][0], vertices[1][1], vertices[4][0], vertices[4][1]);
			sr.end();
		}
	}
	
	private void drawFragments() {
		int numFragments = world.getNumFragments();
		
		for(int i = 0; i < numFragments; i++) {
			Fragment f = world.getFragment(i);
			
			float x = f.getX();
			float y = f.getY();
			float vertices[][] = f.getVertices();
			int edges = f.getEdges();	
			int[] fillColour = f.getFillColour();
			int[] lineColour = f.getLineColour();
			
			sr.setColor(fillColour[0]/255f, fillColour[1]/255f, fillColour[2]/255f, 1);
			drawFilledPolygon(x, y, vertices, edges);			
			
			Gdx.gl.glLineWidth(3);
			sr.setColor(lineColour[0]/255f, lineColour[1]/255f, lineColour[2]/255f, 1);
			drawPolygonOutline(vertices, edges);
		}
	}
	
	private void drawShields() {
		int numRockets = world.getNumRockets();
		
		for(int i = 0; i < numRockets; i++) {
			Rocket r = world.getRocket(i);
			
			if(r.getShieldOn() == true) {
				Shield s = r.getShield();
				
				float x = s.getX();
				float y = s.getY();
				float vertices[][] = s.getVertices();
				int edges = s.getEdges();
				int[] fillColour = s.getFillColour();
				int[] lineColour = s.getLineColour();
				
				Gdx.gl.glEnable(GL20.GL_BLEND);
				sr.setColor(fillColour[0]/255f, fillColour[1]/255f, fillColour[2]/255f, 0.3f);			
				drawFilledPolygon(x, y, vertices, edges);
							
				Gdx.gl.glLineWidth(3);
				sr.setColor(lineColour[0]/255f, lineColour[1]/255f, lineColour[2]/255f, 0.5f);
				drawPolygonOutline(vertices, edges);
			}			
		}
	}
	
	private void drawFilledPolygon(float x, float y, float[][] vertices, int edges) {
		
		for(int e = 0; e < edges; e++) {
			sr.begin(ShapeType.Filled);

			if(e == edges - 1) { // Final vertex - need to make triangle with the first vertex
				sr.triangle(vertices[e][0], vertices[e][1],
						vertices[0][0], vertices[0][1],
						x, y);
				sr.end();
			} else {
				sr.triangle(vertices[e][0], vertices[e][1],
						vertices[e+1][0], vertices[e+1][1],
						x, y);
				sr.end();
			}
		}
	}
	
	private void drawPolygonOutline(float[][] vertices, int edges) {
		float[] polygon = polygonArray(vertices, edges);
		sr.begin(ShapeType.Line);
		sr.polygon(polygon);
		sr.end();		
	}
	
	private float[] polygonArray(float[][] vertices, int edges) {
		float[] polygon = new float[edges * 2];
		
		for(int i = 0; i < edges; i ++) {
			polygon[i*2] = vertices[i][0];
			polygon[(i*2)+1] = vertices[i][1];
		}
		
		return polygon;	
	}

	private void drawBackground() {
		Gdx.gl.glClearColor(2/255f, 2/255f, 2/255f, 1);
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
		
		sr.begin(ShapeType.Point);
		sr.setColor(Color.WHITE);
	
		int[][] stars = Main.getStars();
		
		for(int i = 0; i < stars.length; i++) {
			sr.point(stars[i][0], stars[i][1], 0);
		}
		
		sr.end();
	}
}
