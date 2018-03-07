/**
 * Handles collisions in the case that information molecules decompose when 
 * colliding with acknowledgement molecules of matching id
 */
import java.util.ArrayList;

public class DecomposingCollisionHandler extends CollisionDecorator{
	
	public DecomposingCollisionHandler(CollisionHandler cH){
		super(cH);
	}

	/**
	 * Moving molecule stays in place if it collides with another molecule;
	 * Additionally, if an acknowledgement molecule and an information molecule
	 * with the same ID number collide, the information molecule is deleted
	 */
	public Position handlePotentialCollisions(Molecule mol, Position nextPos, MolComSim simulation) {
		Position nextPosition = collH.handlePotentialCollisions(mol, nextPos, simulation);
		Position pos = checkCollsitionNanoMachine(mol, nextPos, simulation);
		if(pos != null) {
			return pos;
		}
		// molがinfoで相手がackかつ同じmessageIDならmolを消滅
		// molがackで相手がinfoかつ同じmessageIDなら相手を消滅
		if (simulation.getMedium().hasMolecule(nextPos) && isCollision(mol, nextPos, simulation)){
			ArrayList<Object> alreadyThere = simulation.getMedium().getObjectsAtPos(nextPosition);
			if (mol instanceof InformationMolecule){
				for (Object o : alreadyThere){
					if (o instanceof AcknowledgementMolecule){
						if (((AcknowledgementMolecule) o).getMsgId() >= mol.getMsgId()){
							//remove info molecule from simulation
							simulation.moveObject(mol, mol.getPosition(), simulation.getMedium().garbageSpot());
							simulation.addDecomposingNum();
							return simulation.getMedium().garbageSpot();
						} else if (((AcknowledgementMolecule) o).getMsgId() < mol.getMsgId()){
							//remove info molecule from simulation
							simulation.getMedium().moveObject(o, nextPosition, simulation.getMedium().garbageSpot());
							simulation.addDecomposingNum();
							break;
						}
					}
				}
			}
			else if (mol instanceof AcknowledgementMolecule){
				for (Object o : alreadyThere){
					if (o instanceof InformationMolecule){
						if (((InformationMolecule) o).getMsgId() <= mol.getMsgId()){
							//remove info molecule from simulation
							simulation.getMedium().moveObject(o, nextPosition, simulation.getMedium().garbageSpot());
							simulation.addDecomposingNum();
							break;
						} else if (((InformationMolecule) o).getMsgId() > mol.getMsgId()){
							//remove info molecule from simulation
							simulation.moveObject(mol, mol.getPosition(), simulation.getMedium().garbageSpot());
							simulation.addDecomposingNum();
							return simulation.getMedium().garbageSpot();
						}
					}
				}
			}
			return mol.getPosition();
		}
		 simulation.moveObject(mol, mol.getPosition(), nextPos);
		 return nextPos;
	}
	
//	public Position handlePotentialCollisions(Molecule mol, Position nextPos, MolComSim simulation) {
//		Position nextPosition = collH.handlePotentialCollisions(mol, nextPos, simulation);
//		Position pos = checkCollsitionNanoMachine(mol, nextPos, simulation);
//		if(pos != null) {
//			return pos;
//		}
//		// molがinfoで相手がackかつ同じmessageIDならmolを消滅
//		// molがackで相手がinfoかつ同じmessageIDなら相手を消滅
//		if (simulation.getMedium().hasMolecule(nextPos) && isCollision(mol, nextPos, simulation)){
//			ArrayList<Object> alreadyThere = simulation.getMedium().getObjectsAtPos(nextPosition);
//			if (mol instanceof InformationMolecule){
//				for (Object o : alreadyThere){
//					if (o instanceof AcknowledgementMolecule){
//						if ( ((AcknowledgementMolecule) o).getMsgId() == mol.getMsgId()){
////						if ( ((AcknowledgementMolecule) o).getMsgId() >= mol.getMsgId()){
//							//remove info molecule from simulation
//							//TODO: a better way to get this spot
//							//TODO: change moveObject to return a position so this can be done in one line
//							simulation.moveObject(mol, mol.getPosition(), simulation.getMedium().garbageSpot());
//							simulation.addDecomposingNum();
//							return simulation.getMedium().garbageSpot();
//						}						
//					}
//				}
//			}
//			else if (mol instanceof AcknowledgementMolecule){
//				for (Object o : alreadyThere){
//					if (o instanceof InformationMolecule){
//						if (((InformationMolecule) o).getMsgId() == mol.getMsgId()){
////						if (((InformationMolecule) o).getMsgId() <= mol.getMsgId()){
//							//remove info molecule from simulation
//							simulation.getMedium().moveObject(o, nextPosition, simulation.getMedium().garbageSpot());
//							simulation.addDecomposingNum();
//							break;
//						}						
//					}
//				}
//			}
//			return mol.getPosition();
//		}
//		 simulation.moveObject(mol, mol.getPosition(), nextPos);
//		 return nextPos;
//	}
}
