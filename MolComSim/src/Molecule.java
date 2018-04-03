/**
 *  Molecule class represents a generic molecule
 *  in the molecular communication simulation
 *  Contains information necessary for moving
 *  the molecule through the medium
 *
 */

public abstract class Molecule {

	private Position position;
	private int numSequence;
	private MovementController movementController;
	protected MolComSim simulation;
	private MoleculeMovementType moleculeMovementType;
	private int startTime = 0;
	private int endTime = 0;
	//Id of the message a molecule carries - null for noise molecules
	protected Integer msgId;
	private double volume = 0.0;

	protected Molecule(MovementController mc, Position psn, MolComSim sim, MoleculeMovementType molMvType, double volume) {
		this.movementController = mc;
		this.position = psn;
		this.simulation = sim;
		this.moleculeMovementType = molMvType; 
		this.volume = volume;
	}
	
	protected Molecule(Position psn, MolComSim sim, MoleculeMovementType molMvType, double volume) {
		this.movementController = null;
		this.position = psn;
		this.simulation = sim;
		this.moleculeMovementType = molMvType;
		this.volume = volume;
	}
	
	protected Molecule(Position psn, MolComSim sim, MoleculeMovementType molMvType) {
		this.movementController = null;
		this.position = psn;
		this.simulation = sim;
		this.moleculeMovementType = molMvType;
	}
	
	protected Molecule(Position psn, int radius, int numSeq, MolComSim sim, MoleculeMovementType molMvType) {
 		this(psn, sim, molMvType);
// 		this.radius = radius;
		this.numSequence = numSeq;
 	}
	
	protected Molecule(Position psn, int numSeq, MolComSim sim, MoleculeMovementType molMvType) {
 		this(psn, sim, molMvType);
		this.numSequence = numSeq;
 	}
	
	protected Molecule(Position psn, int numSeq, MolComSim sim, MoleculeMovementType molMvType, double volume) {
 		this(psn, sim, molMvType, volume);
		this.numSequence = numSeq;
 	}
	
	//Moves the molecule as defined by its movementController
	public abstract void move();

	public Position getPosition() {
		return position;
	}
	
	public int getNumSequence() {
		return numSequence;
	}

	public void setMovementController(MovementController mc) {
		this.movementController = mc;
	}

	public MolComSim getSimulation() {
		return simulation;
	}

	public MovementController getMovementController() {
		return movementController;
	}
	
	protected void setPosition(Position p) {
		this.position = p;
	}

	public MoleculeMovementType getMoleculeMovementType() {
		return moleculeMovementType;
	}
	
	public Integer getMsgId(){
		return this.msgId;
	}
	
	public void setStartTime(int startTime) {
		this.startTime = startTime;
	}
	
	public void setEndTime(int endTime) {
		this.endTime = endTime;
	}
	
	public int getSendTime() {
		return this.endTime - this.startTime;
	}
	
	public double getVolume() {
		return this.volume;
	}

}
