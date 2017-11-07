package screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;

import gameHelpers.InputHandler;
import gameWorld.CollisionDetector;
import gameWorld.DeletionManager;
import gameWorld.GameRenderer;
import gameWorld.GameWorld;

public class GameScreen implements Screen { // Implementing methods of the screen interface
	// GameScreen class does not do any rendering/updating itself
	
	private GameWorld world;
	private CollisionDetector collisionDetector;
	private DeletionManager deletionManager;
	private GameRenderer renderer;
	private static int score;
	
	public GameScreen() {
		world = new GameWorld(); // Initialise world
		collisionDetector = new CollisionDetector(world); // Initialise collision detector
		deletionManager = new DeletionManager(world, collisionDetector); // Initialise deletion manager
		renderer = new GameRenderer(world, collisionDetector); // Initialise renderer; can retrieve objects from world
		
		Gdx.input.setInputProcessor(new InputHandler(world.getRocket(), world));
		
		score = 0;
	}
	
	public static void changeScore(int p) {
		score += p;
	}

	@Override
	public void render(float delta) { // Renders the game each second; delta is the time since last called
		world.update(delta); // Updates all game objects
		collisionDetector.manage();
		deletionManager.delete();
		//System.out.println(score);
		renderer.render(); // Render all game objects
	}
	
	@Override
	public void show() {}

	@Override
	public void resize(int width, int height) {}

	@Override
	public void pause() {}

	@Override
	public void resume() {}

	@Override
	public void hide() {}

	@Override
	public void dispose() {}

}
