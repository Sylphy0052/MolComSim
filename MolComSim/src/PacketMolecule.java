/**
 * Information Molecule is the type of molecule
 * that is sent out by a transmitter to 
 * communicate a message
 */
import java.util.*;

public class PacketMolecule extends Molecule{

	//Indicates where molecule is intended to go
	private ArrayList<NanoMachine> destinations;
	//Where molecule started from
	private NanoMachine source;

	public PacketMolecule(MovementController mc, Position psn, MolComSim sim, NanoMachine src, int msgNum, MoleculeMovementType molMvType, double volume) {
		super(mc, psn, sim, molMvType, volume);
		this.source = src;
		this.msgId = msgNum; 
		this.destinations = sim.getReceivers();
	}
	
	public PacketMolecule(Position psn, MolComSim sim, NanoMachine src, int msgNum, MoleculeMovementType molMvType, double volume) {
		super(psn, sim, molMvType, volume);
		this.source = src; 
		this.msgId = msgNum; 
		this.destinations = sim.getReceivers();
	}
	
	public PacketMolecule(Position psn, int numSeq, MolComSim sim, NanoMachine src, int msgNum, MoleculeMovementType molMvType) {
		super(psn, numSeq, sim, molMvType);
		this.source = src; 
		this.msgId = msgNum; 
		this.destinations = sim.getReceivers();
	}
	
	public PacketMolecule(Position psn, int numSeq, MolComSim sim, NanoMachine src, int msgNum, MoleculeMovementType molMvType, double volume) {
		super(psn, numSeq, sim, molMvType, volume);
		this.source = src; 
		this.msgId = msgNum; 
		this.destinations = sim.getReceivers();
	}
	
	public PacketMolecule(Position psn, int radius, int numSeq, MolComSim sim, NanoMachine src, int msgNum, MoleculeMovementType molMvType) {
		super(psn, radius, numSeq, sim, molMvType);
		this.source = src; 
		this.msgId = msgNum; 
		this.destinations = sim.getReceivers();
	}
	
	public void move() {
		setPosition(getMovementController().getNextPosition(this, getSimulation()));
		NanoMachine rx = simulation.getMedium().getRxNanoMachineAtPos(getPosition());
		if(rx != null) {
			rx.receiveMolecule(this);
		}
	}

}
