import java.util.Scanner;

/** Stores the parameters needed
 * to create a particular type of molecule
 *
 */

public class MoleculeParams {

	private int numMolecules;
	private MoleculeType moleculeType;
	private MoleculeMovementType moleculeMovementType = SimulationParams.getMovementDefault(moleculeType);
	private int adaptiveChange = 0; // default is no adaptive change.  Amount to adjust num mols based
									// on comm success/failure.
	private double size = 1.0;
	private double volume = 1.0;

	public MoleculeParams(MoleculeType mType, MoleculeMovementType mMovementType, int numMols, int adaptiveChange, double size) {
		this.numMolecules = numMols;
		this.moleculeMovementType = mMovementType;
		this.moleculeType = mType;
		this.adaptiveChange = adaptiveChange;
		this.size = size;
		this.volume = Math.pow(this.size, 3);
	}

	public MoleculeParams(Scanner readParams) {
		numMolecules = readParams.nextInt();
		moleculeType = MoleculeType.getMoleculeType(readParams.next());
		
		// ノイズ
		if(readParams.hasNextDouble()) {
			size = readParams.nextDouble();
			volume = Math.pow(size, 3);
			return;
		}
		
		moleculeMovementType = MoleculeMovementType.getMovementType(readParams.next());
		adaptiveChange = readParams.nextInt();
		size = readParams.nextDouble();
		volume = Math.pow(size, 3);
	}

	public int getNumMolecules() {
		return numMolecules;
	}

	public MoleculeType getMoleculeType() {
		return moleculeType;
	}

	public MoleculeMovementType getMoleculeMovementType() {
		return moleculeMovementType;
	}
	
	public int getAdaptiveChange() {
		return adaptiveChange;
	}
	
	public double getSize() {
		return size;
	}
	
	public double getVolume() {
		return volume;
	}
	
	// Changes the number of molecules to send out based on prior communication success or failure.
	public void applyAdaptiveChange(int lastTransmissionStatus ) {
		if(lastTransmissionStatus == NanoMachine.LAST_COMMUNICATION_SUCCESS) {
			numMolecules -= adaptiveChange;
			if(numMolecules < 1) {
				numMolecules = 1;
			}
		} else if (lastTransmissionStatus == NanoMachine.LAST_COMMUNICATION_FAILURE){
			numMolecules += adaptiveChange;
		}
	}
}
