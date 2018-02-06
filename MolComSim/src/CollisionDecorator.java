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
		int from_x = molPosition.getX();
		int from_y = molPosition.getY();
		int from_z = molPosition.getZ();

		int to_x = nextPos.getX();
		int to_y = nextPos.getY();
		int to_z = nextPos.getZ();

		int diff_x = to_x - from_x;
		int diff_y = to_y - from_y;
		int diff_z = to_z - from_z;

		Position pos = null;
		
		if(diff_x >=0 && diff_y >=0 && diff_z >= 0) {
			pos = loopCheck(mol, diff_x, diff_y, diff_z, simulation, 0);
		} else if(diff_x >= 0 && diff_y >= 0 && diff_z < 0) {
			pos = loopCheck(mol, diff_x, diff_y, diff_z, simulation, 1);
		} else if(diff_x >= 0 && diff_y < 0 && diff_z >= 0) {
			pos = loopCheck(mol, diff_x, diff_y, diff_z, simulation, 2);
		} else if(diff_x >= 0 && diff_y < 0 && diff_z < 0) {
			pos = loopCheck(mol, diff_x, diff_y, diff_z, simulation, 3);
		} else if(diff_x < 0 && diff_y >= 0 && diff_z >= 0) {
			pos = loopCheck(mol, diff_x, diff_y, diff_z, simulation, 4);
		} else if(diff_x < 0 && diff_y >= 0 && diff_z < 0) {
			pos = loopCheck(mol, diff_x, diff_y, diff_z, simulation, 5);
		} else if(diff_x < 0 && diff_y < 0 && diff_z >= 0) {
			pos = loopCheck(mol, diff_x, diff_y, diff_z, simulation, 6);
		} else if(diff_x < 0 && diff_y < 0 && diff_z < 0) {
			pos = loopCheck(mol, diff_x, diff_y, diff_z, simulation, 7);
		}

		return pos;
	}

	private Position loopCheck(Molecule mol, int diff_x, int diff_y, int diff_z, MolComSim simulation, int type) {
		Position pos = null;

		switch(type) {
		case 0:
			for(int i=0; i <= diff_x; i++) {
				for(int j=0; j <= diff_y; j++) {
					for(int k=0; k <= diff_z; k++) {
						Position checkPos = new Position(mol.getPosition().getX() + i, mol.getPosition().getY() + j, mol.getPosition().getZ() + k);
            switch(mol.getClass().getName()) {
              case "InformationMolecule":
              if(simulation.getMedium().getRxNanoMachineAtPos(checkPos) != null) {
                return checkPos;
              }
              break;
              case "AcknowledgementMolecule":
              if(simulation.getMedium().getTxNanoMachineAtPos(checkPos) != null) {
                return checkPos;
              }
              break;
            }
					}
				}
			}
			break;
		case 1:
    for(int i=0; i <= diff_x; i++) {
      for(int j=0; j <= diff_y; j++) {
        for(int k=diff_z; k >= 0; k--) {
          Position checkPos = new Position(mol.getPosition().getX() + i, mol.getPosition().getY() + j, mol.getPosition().getZ() + k);
          switch(mol.getClass().getName()) {
            case "InformationMolecule":
            if(simulation.getMedium().getRxNanoMachineAtPos(checkPos) != null) {
              return checkPos;
            }
            break;
            case "AcknowledgementMolecule":
            if(simulation.getMedium().getTxNanoMachineAtPos(checkPos) != null) {
              return checkPos;
            }
            break;
          }
        }
      }
    }
			break;
		case 2:
    for(int i=0; i <= diff_x; i++) {
      for(int j=diff_y; j >= 0; j--) {
        for(int k=0; k <= diff_z; k++) {
          Position checkPos = new Position(mol.getPosition().getX() + i, mol.getPosition().getY() + j, mol.getPosition().getZ() + k);
          switch(mol.getClass().getName()) {
            case "InformationMolecule":
            if(simulation.getMedium().getRxNanoMachineAtPos(checkPos) != null) {
              return checkPos;
            }
            break;
            case "AcknowledgementMolecule":
            if(simulation.getMedium().getTxNanoMachineAtPos(checkPos) != null) {
              return checkPos;
            }
            break;
          }
        }
      }
    }
			break;
		case 3:
    for(int i=0; i <= diff_x; i++) {
      for(int j=diff_y; j >= 0; j--) {
        for(int k=diff_z; k >= 0; k--) {
          Position checkPos = new Position(mol.getPosition().getX() + i, mol.getPosition().getY() + j, mol.getPosition().getZ() + k);
          switch(mol.getClass().getName()) {
            case "InformationMolecule":
            if(simulation.getMedium().getRxNanoMachineAtPos(checkPos) != null) {
              return checkPos;
            }
            break;
            case "AcknowledgementMolecule":
            if(simulation.getMedium().getTxNanoMachineAtPos(checkPos) != null) {
              return checkPos;
            }
            break;
          }
        }
      }
    }
			break;
      case 4:
  			for(int i=diff_x; i >= 0; i++) {
  				for(int j=0; j <= diff_y; j++) {
  					for(int k=0; k <= diff_z; k++) {
  						Position checkPos = new Position(mol.getPosition().getX() + i, mol.getPosition().getY() + j, mol.getPosition().getZ() + k);
              switch(mol.getClass().getName()) {
                case "InformationMolecule":
                if(simulation.getMedium().getRxNanoMachineAtPos(checkPos) != null) {
                  return checkPos;
                }
                break;
                case "AcknowledgementMolecule":
                if(simulation.getMedium().getTxNanoMachineAtPos(checkPos) != null) {
                  return checkPos;
                }
                break;
              }
  					}
  				}
  			}
  			break;
  		case 5:
      for(int i=diff_x; i >= 0; i++) {
        for(int j=0; j <= diff_y; j++) {
          for(int k=diff_z; k >= 0; k--) {
            Position checkPos = new Position(mol.getPosition().getX() + i, mol.getPosition().getY() + j, mol.getPosition().getZ() + k);
            switch(mol.getClass().getName()) {
              case "InformationMolecule":
              if(simulation.getMedium().getRxNanoMachineAtPos(checkPos) != null) {
                return checkPos;
              }
              break;
              case "AcknowledgementMolecule":
              if(simulation.getMedium().getTxNanoMachineAtPos(checkPos) != null) {
                return checkPos;
              }
              break;
            }
          }
        }
      }
  			break;
  		case 6:
      for(int i=diff_x; i >= 0; i++) {
        for(int j=diff_y; j >= 0; j--) {
          for(int k=0; k <= diff_z; k++) {
            Position checkPos = new Position(mol.getPosition().getX() + i, mol.getPosition().getY() + j, mol.getPosition().getZ() + k);
            switch(mol.getClass().getName()) {
              case "InformationMolecule":
              if(simulation.getMedium().getRxNanoMachineAtPos(checkPos) != null) {
                return checkPos;
              }
              break;
              case "AcknowledgementMolecule":
              if(simulation.getMedium().getTxNanoMachineAtPos(checkPos) != null) {
                return checkPos;
              }
              break;
            }
          }
        }
      }
  			break;
  		case 7:
      for(int i=diff_x; i >= 0; i++) {
        for(int j=diff_y; j >= 0; j--) {
          for(int k=diff_z; k >= 0; k--) {
            Position checkPos = new Position(mol.getPosition().getX() + i, mol.getPosition().getY() + j, mol.getPosition().getZ() + k);
            switch(mol.getClass().getName()) {
              case "InformationMolecule":
              if(simulation.getMedium().getRxNanoMachineAtPos(checkPos) != null) {
                return checkPos;
              }
              break;
              case "AcknowledgementMolecule":
              if(simulation.getMedium().getTxNanoMachineAtPos(checkPos) != null) {
                return checkPos;
              }
              break;
            }
          }
        }
      }
  			break;
		}

		return null;
	}
}
