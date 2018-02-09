import java.util.ArrayList;
import java.util.Arrays;
import java.util.Random;

/**
 * CollisionDecorator uses the Decorator design pattern
 * to allow CollisionHandlers to use any combination of parameters:
 * Null, Standard, OnTubule, or Decomposing
 * 
 */
public abstract class CollisionDecorator implements CollisionHandler {
	
	protected CollisionHandler collH;
	private static Random random = new Random();
	
	public CollisionDecorator(CollisionHandler cH){
		collH = cH;
	}
	
	public Position handlePotentialCollisions(Molecule mol, Position nextPosition, MolComSim simulation){
		return collH.handlePotentialCollisions(mol, nextPosition, simulation);
	}
	
	public boolean isCollision(Molecule mol, Position nextPos, MolComSim simulation) {
		ArrayList<Object> alreadyThere = simulation.getMedium().getObjectsAtPos(nextPos);
		
		if(alreadyThere == null) {
			return false;
		}
		
		double vsum = 0.0;
		double vin = 0.0;
		
		double vinfo = 1.0;
		double vack = 1.0;
		double vnoise = 1.0;
		
		
		for(MoleculeParams molParams: simulation.getSimParams().getAllMoleculeParams()) {
			switch(molParams.getMoleculeType()) {
			case INFO:
				double infoSize = molParams.getSize();
				vinfo = Math.pow(infoSize, 3);
				break;
			case ACK:
				double ackSize = molParams.getSize();
				vack = Math.pow(ackSize, 3);
				break;
			case NOISE:
				double noiseSize = molParams.getSize();
				vnoise = Math.pow(noiseSize, 3);
				break;
			}
		}
		
		switch(mol.getClass().getName()) {
		case "InformationMolecule":
			vin = vinfo;
			break;
		case "AcknowledgementMolecule":
			vin = vack;
			break;
		}
		
		for(Object o : alreadyThere) {
			String objectName = o.getClass().getName();
			switch(objectName) {
			case "InformationMolecule":
				vsum += vinfo;
				break;
			case "AcknowledgementMolecule":
				vsum += vack;
				break;
			case "NoiseMolecule":
				vsum += vnoise;
				break;
			}
		}
		
		if(vsum + vin >= 1) {
			simulation.addCollisionNum(mol, nextPos, simulation);
			return true;
		}
		
		double p = vsum + vin / (1 - vsum);
		
		if(Math.random() < (1.0 - p)) {
			simulation.addCollisionNum(mol, nextPos, simulation);
			return true;
		}
		return false;
	}
	
	public Position checkCollsitionNanoMachine(Molecule mol, Position nextPos, MolComSim simulation) {
		Position molPosition = mol.getPosition();
		
		double stepLength = simulation.getSimParams().getMolRandMoveX();
		
		int x1 = molPosition.getX();
		int y1 = molPosition.getY();
		int z1 = molPosition.getZ();

		int x2 = nextPos.getX();
		int y2 = nextPos.getY();
		int z2 = nextPos.getZ();
		
		int delX = (x2 - x1);
		int delY = (y2 - y1);
		int delZ = (z2 - z1);
		
		double unitLength = Math.sqrt(delX*delX + delY*delY + delZ*delZ);
		
//		double x = ((double)delX*stepLength/unitLength);
//		double y = ((double)delY*stepLength/unitLength);
//		double z = ((double)delZ*stepLength/unitLength);
		
		double x = ((double)delX/unitLength);
		double y = ((double)delY/unitLength);
		double z = ((double)delZ/unitLength);
		
		DoublePosition direction = new DoublePosition(x, y, z);
		DoublePosition currentPos = direction.toDouble(molPosition);
		
		int count = 0;
		while(!currentPos.toRoundInt().equals(nextPos)) {
			count++;
			Position tmpPos = checkIsThereNanoMachine(mol, currentPos.toInt(), simulation);
			if(tmpPos != null) {
				return tmpPos;
			}
			currentPos = currentPos.addDouble(direction);
			if(count == (int)stepLength * 2) {
				System.out.println("ERROR");
				System.exit(1);
			}
		}
		return null;
	}
	
	private Position checkIsThereNanoMachine(Molecule mol, Position pos, MolComSim simulation) {
		switch(mol.getClass().getName()) {
		case "InformationMolecule":
			if(simulation.getMedium().getRxNanoMachineAtPos(pos) != null) {
				return pos;
			}
			break;
		case "AcknowledgementMolecule":
			if(simulation.getMedium().getTxNanoMachineAtPos(pos) != null) {
				return pos;
			}
			break;
		}
		return null;
	}
}
