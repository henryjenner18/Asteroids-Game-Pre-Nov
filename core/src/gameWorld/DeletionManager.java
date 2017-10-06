package gameWorld;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public class DeletionManager {
	
	private GameWorld myWorld;
	private CollisionDetector myCollisionDetector;
	
	private ArrayList<Integer> removeMissiles;
	private ArrayList<Integer> removeAsteroids;
	
	public DeletionManager(GameWorld world, CollisionDetector collisionDetector) {
		myWorld = world;
		myCollisionDetector = collisionDetector;
	}

	public void delete() {
		removeMissiles = new ArrayList<Integer>();
		removeAsteroids = new ArrayList<Integer>(myCollisionDetector.getRemoveAsteroids());
		removeMissiles();
		removeAsteroids();
	}
	
	private void removeMissiles() {
		int numMissiles = myWorld.getNumMissiles();
		
		for(int i = 0; i < numMissiles; i++) {
			if(myWorld.getMissile(i).getTimeLeft() == 0) {
				System.out.println("Need to remove missile " + i);
				removeMissiles.add(i);
			}
		}
		
		Set<Integer> noDuplicates = new HashSet<Integer>();
		noDuplicates.addAll(removeMissiles);
		removeMissiles.clear();
		removeMissiles.addAll(noDuplicates);
		Collections.sort(removeMissiles);
		Collections.reverse(removeMissiles);
		
		for(int i = 0; i < removeMissiles.size(); i++) {
			myWorld.removeMissile(removeMissiles.get(i));
		}
	}
	
	private void removeAsteroids() {
		Set<Integer> noDuplicates = new HashSet<Integer>();
		noDuplicates.addAll(removeAsteroids);
		removeAsteroids.clear();
		removeAsteroids.addAll(noDuplicates);
		Collections.sort(removeAsteroids);
		Collections.reverse(removeAsteroids);
		
		for(int i = 0; i < removeAsteroids.size(); i++) {
			splitAsteroid(i);
			myWorld.removeAsteroid(removeAsteroids.get(i));
		}
	}
	
	private void splitAsteroid(int i) {
		int s = myWorld.getAsteroid(removeAsteroids.get(i)).getSize() - 1;
		float x = myWorld.getAsteroid(removeAsteroids.get(i)).xcoord();
		float y = myWorld.getAsteroid(removeAsteroids.get(i)).ycoord();
		
		if(s >= 1) {
			for(int a = 0; a < 2; a++) {
				myWorld.createAsteroid(s, x, y);
			}
		}
	}
}
