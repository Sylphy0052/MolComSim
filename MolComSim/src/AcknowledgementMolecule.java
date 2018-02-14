/**
 * Acknowledgment Molecule is a type of molecule
 * that is sent out by a Receiver NanoMachine
 */

import java.util.*;

public class AcknowledgementMolecule extends Molecule{

	private NanoMachine source; 

	public AcknowledgementMolecule(MovementController mc, Position psn, MolComSim sim, NanoMachine src, int msgNum, MoleculeMovementType molMvType, double volume) {
		super(mc, psn, sim, molMvType, volume);
		source = src;
		msgId = msgNum; 
	}
	
	public AcknowledgementMolecule(Position psn, MolComSim sim, NanoMachine src, int msgNum, MoleculeMovementType molMvType, double volume) {
		super(psn, sim, molMvType, volume);
		source = src;
		msgId = msgNum; 
	}
	
	public void move() {
		setPosition(getMovementController().getNextPosition(this, getSimulation()));
		NanoMachine tx = simulation.getMedium().getTxNanoMachineAtPos(getPosition());
		if(tx != null)
		{
			tx.receiveMolecule(this);
		}
	}

}
