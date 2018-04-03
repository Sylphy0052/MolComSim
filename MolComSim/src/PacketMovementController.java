/**
 * Moves molecules according to passive transport,
 * using a random walk
 *
 */

import java.util.*;

public class PacketMovementController extends MovementController{
	private static Random random = new Random();
	
	public PacketMovementController(CollisionHandler collHandle, MolComSim sim, Molecule mol) {
		super(collHandle, sim, mol);
	}
	
	/** Randomly selects where to move molecule based on simulation step length parameters
	 *  @param molecule The molecule to move
	 *  @return the position to move to
	 */
	protected Position decideNextPosition() {
		//Randomly decide the next position based on current position + some delta.
		Position currentPosition = getMolecule().getPosition();
		int currentX = currentPosition.getX();
		int currentY = currentPosition.getY(); 
		int currentZ = currentPosition.getZ();
		// The idea behind the getMolRandMove~ methods are that the molecule moves a random amount in one step
		// , and that will, for each dimension, be a random amount between -getMolRandMove~ and +getMolRandMove~
		double maxXDelta = getSimulation().getSimParams().getMolRandMoveX();
		double maxYDelta = getSimulation().getSimParams().getMolRandMoveY();;
		double maxZDelta = getSimulation().getSimParams().getMolRandMoveZ();;
		int nextX = currentX + (int)Math.round(random.nextDouble() * (maxXDelta * 2) - maxXDelta);
		int nextY = currentY + (int)Math.round(random.nextDouble() * (maxYDelta * 2) - maxYDelta);
		int nextZ = currentZ + (int)Math.round(random.nextDouble() * (maxZDelta * 2) - maxZDelta);
//		int nextX = currentX + random.nextInt(1 + (2 * (int)maxXDelta)) - (int)maxXDelta;
//		int nextY = currentY + random.nextInt(1 + (2 * (int)maxYDelta)) - (int)maxYDelta;
//		int nextZ = currentZ + random.nextInt(1 + (2 * (int)maxZDelta)) - (int)maxZDelta;
		Position nextPosition = new Position(nextX, nextY, nextZ);
//		
		//If the molecule has ACTIVE movement type, it looks for a nearby microtubule to reattach to
		if (getMolecule().getMoleculeMovementType() == MoleculeMovementType.ACTIVE){
			//If a microtubule is found, change molecule's collision handler and movement controller
			Microtubule microtubule = simulation.getMedium().hasMicrotubule(getMolecule().getPosition());
			if (microtubule != null){
				CollisionHandler collH = simulation.isUsingCollisions() ? 
						(simulation.decomposing() ? new OnTubuleCollisionHandler(new DecomposingCollisionHandler(new SimpleCollisionHandler(), simulation.getDecomposingMode())) :
							new OnTubuleCollisionHandler(new SimpleCollisionHandler())) : new NullCollisionHandler(new SimpleCollisionHandler());
				new OnMicrotubuleMovementController(collH, simulation, getMolecule(), microtubule);
			}
		}
		return nextPosition;
	}
}

