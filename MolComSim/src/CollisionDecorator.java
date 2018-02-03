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
		
		if(vsum + vin > 1) {
			return true;
		}
		
		double p = vsum + vin / (1 - vsum);
		if(Math.random() < p) {
			simulation.addCollisionNum(mol, nextPos, simulation);
			return true;
		}
		return false;
	}

}
