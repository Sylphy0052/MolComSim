/**
 * MovementController for molecules in active transport
 *
 */
		

public class OnMicrotubuleMovementController extends MovementController{

	private Microtubule microtubule;

	public OnMicrotubuleMovementController(CollisionHandler collHandle, MolComSim sim, Molecule mol, Microtubule tubule) {
		super(collHandle, sim, mol);
		microtubule = tubule;
	}

	/**
	 * Determines the next position for a molecule moving along a microtubule
	 * @param molecule The molecule whose position is being decided
	 * @return the Position it should go to
	 */
	protected Position decideNextPosition() {
		Position currentPosition = getMolecule().getPosition();
//		Position direction = microtubule.getDirectionVector().toInt();
		DoublePosition doubleDirection = microtubule.getDirectionVector();
		
		Position direction = doubleDirection.toInt();
		if (doubleDirection.getX() < 1 || doubleDirection.getY() < 1 || doubleDirection.getZ() < 1) {
			int steps = simulation.getSimStep();
				double x = 0, y = 0, z = 0;
			if (steps % (int)(1.0 / doubleDirection.getX()) == 0) {
				x = 1.0;
			}
			if (steps % (int)(1.0 / doubleDirection.getY()) == 0) {
				y = 1.0;
			}
			if (steps % (int)(1.0 / doubleDirection.getZ()) == 0) {
				z = 1.0;
			}
			direction = new DoublePosition(x, y, z).toInt();
		}
		
		Position nextPosition = new Position(currentPosition.getX() + direction.getX(), currentPosition.getY() + direction.getY(), currentPosition.getZ() + direction.getZ());
		//If the molecule gets derailed, it moves to the same spot, but switches to passive movement off the microtubule
		if (Math.random() < this.simulation.getSimParams().getProbDRail()){
			CollisionHandler collh;
			if (simulation.isUsingCollisions()){
				if (simulation.decomposing())
					collh = new DecomposingCollisionHandler(new SimpleCollisionHandler(), simulation.getDecomposingMode());
				else
					collh = new StandardCollisionHandler(new SimpleCollisionHandler());
			}
			else
				collh = new SimpleCollisionHandler();
			new DiffusiveRandomMovementController(collh, getSimulation(), getMolecule());
		}
		return nextPosition;
	}

}
