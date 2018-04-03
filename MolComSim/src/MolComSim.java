
import java.io.*;
import java.util.*;

public class MolComSim {

	//Parameters for this simulation instance and a reader for it
//	private FileReader paramsFile;
	private SimulationParams simParams;
	private FileWriter outputFile = null;
	private static final boolean APPEND_TO_FILE = true; // used to set the append field for FileWriter to write out to the
							// same file as other simulations during a batch run.
	
	//Collections of all the actors in this simulation
	private ArrayList<Microtubule> microtubules;
	private ArrayList<NanoMachine> nanoMachines;
	private ArrayList<NanoMachine> transmitters;
	private ArrayList<NanoMachine> receivers;
	private ArrayList<Molecule> movingMolecules; 

	//The medium in which the simulation takes place
	private Medium medium;

	//Max number of steps to allow in the simulation
	private int simStep;
	private int finishSimStep;
	
	private boolean isFinish;
	private int allInfoTime;
	private int allAckTime;
	private int allInfoNum;
	private int allAckNum;
	
	// 衝突回数
	private ArrayList<Integer> collisionAA;
	private ArrayList<Integer> collisionAI;
	private ArrayList<Integer> collisionAN;
	private ArrayList<Integer> collisionII;
	private ArrayList<Integer> collisionIN;
	private ArrayList<Integer> collisions;
//	private int collisionNumAA;
//	private int collisionNumAI;
//	private int collisionNumAN;
//	private int collisionNumII;
//	private int collisionNumIN;
	private String[] MolType = {"AcknowledgementMolecule", "InformationMolecule", "NoiseMolecule"};
//	private int[] collNums;
	
	private int decomposingNum;
	private ArrayList<Integer> infoAdjustNum;
	private ArrayList<Integer> ackAdjustNum;
	private ArrayList<Integer> adjustSteps;
	
	// 再送信回数
	private ArrayList<Integer> retransmitNum;
	private ArrayList<Integer> txRetransmitNum;
	private ArrayList<Integer> rxRetransmitNum;
	private boolean failure;
	
	//Keeping track of messages sent and received
	//to identify when simulation completed 
	private int messagesCompleted;
	private boolean lastMsgCompleted;
//	private int numMessages;
	
	private ForwardErrorCorrection FEC = null;
	//Number of packets are required when making data
	private int numRequiredPackets;
	
	
	//This instance of the Molecular Communication Simulation
	static MolComSim molComSim;

	
	/** Creates a singleton instance of MolComSim
	 *  and runs the simulation according to input 
	 *  parameters
	 *  
	 *  @param args Should be a parameter file
	 * 
	 */
	public static void main(String[] args) throws IOException {
//		long start = System.currentTimeMillis();
		MolComSim molComSim = createInstance();
		molComSim.run(args);
//		System.out.println(String.format("Finish: %d ms", System.currentTimeMillis() - start));
	}

	/** Begins simulation with the parameter arguments
	 *  and sets flags simStep and lasMsgCompleted
	 * 
	 * @param args Should be a parameter file
	 */
	private void startSim(String[] args) throws IOException {
		simStep = 0;
		lastMsgCompleted = false;
		initParams();
		
		simParams = new SimulationParams(args);
		if((simParams.getOutputFileName() != null) && (!simParams.isBatchRun())) {
			outputFile = new FileWriter(simParams.getOutputFileName());
		}
		if(simParams.isFEC()) {
			FEC = FECFactory.create(simParams.getFECParams(), simParams.getNumRequiredPackets());
		} else {
			FEC = null;
		}
		microtubules = new ArrayList<Microtubule>();
		nanoMachines = new ArrayList<NanoMachine>();
		transmitters = new ArrayList<NanoMachine>();
		receivers = new ArrayList<NanoMachine>();
		movingMolecules = new ArrayList<Molecule>();
		createMedium();
		createMicrotubules(); 
		createNanoMachines();

		// Note: it is the job of the medium and NanoMachines to create molecules
	}
	
	public void initParams() {
		decomposingNum = 0;
		
		retransmitNum = new ArrayList<Integer>();
		txRetransmitNum = new ArrayList<Integer>();
		rxRetransmitNum = new ArrayList<Integer>();
		
		adjustSteps = new ArrayList<Integer>();
		infoAdjustNum = new ArrayList<Integer>();
		ackAdjustNum = new ArrayList<Integer>();
		
		collisionAA = new ArrayList<Integer>();
		collisionAI = new ArrayList<Integer>();
		collisionAN = new ArrayList<Integer>();
		collisionII = new ArrayList<Integer>();
		collisionIN = new ArrayList<Integer>();
		collisions = new ArrayList<Integer>();
	}

	/** Makes sure there is only one instance of MolComSim
	 * 
	 * @return the only instance of MolComSim
	 */
	public static MolComSim createInstance() {
		if(molComSim == null){
			molComSim = new MolComSim();
		}
		return molComSim;
	}
	
	/** Runs the simulation according to given parameters
	 *  Moves each molecule and nanomachine for each time
	 *  step in the simulation 
	 * 
	 * @param args Should be a parameter file
	 */
	private void run(String[] args)  throws IOException {
		startSim(args);
		//As long as we have not run for too long and have not
		//yet finished sending our messages, move the simulation forward
//		for(; (simStep < simParams.getMaxNumSteps()) && (!lastMsgCompleted); simStep++)
		// ToDo: 情報分子と確認応答分子が全てなくなるまで実行
//		for(; (simStep < simParams.getMaxNumSteps()) && (!lastMsgCompleted) && (!movingMolecules.isEmpty()); simStep++) 
		for(; (!isFinish) || (movingMolecules.size() != 0); simStep++) {
			if ((simStep >= simParams.getMaxNumSteps() || lastMsgCompleted) && !isFinish) {
				finishSimStep = simStep;
				isFinish = true;
//				calc_collision();
				if(!simParams.isWait()) {
					break;
				}
			}
			
			for(NanoMachine nm : nanoMachines){
				nm.nextStep();
			}
			
			for(Molecule m : movingMolecules){
				m.move();
			}
			collectGarbage();
		}
		simStep--;
		endSim();
	}

	public int getSimStep() {
		return simStep;
	}
	
	public void addInfoNum() {
		this.allInfoNum++;
	}
	
	public void addAckNum() {
		this.allAckNum++;
	}
	
	public void addInfoTime(int addTime) {
		this.allInfoTime += addTime;
	}
	
	public void addAckTime(int addTime) {
		this.allAckTime += addTime;
	}

	public boolean isLastMsgCompleted() {
		return lastMsgCompleted;
	}
	
	public int getNumMessages(){
		return simParams.getNumMessages();
	}

	/** Creates the medium in which the simulation takes place
	 *  and places noise molecules inside it
	 * 
	 */
	private void createMedium() {
		//get Medium params, NoiseMolecule params from simParams
		int medLength = simParams.getMediumLength();
		int medHeight = simParams.getMediumHeight();
		int medWidth = simParams.getMediumWidth();
		ArrayList<MoleculeParams> nMParams = simParams.getNoiseMoleculeParams();
		medium = new Medium(medLength, medHeight, medWidth, nMParams, this);
		medium.createMolecules();
	}
	

	/** Creates all nanomachines needed for the simulation
	 *  Each nanomachine creates its own information or
	 *  acknowledgment molecules
	 * 
	 */
	private void createNanoMachines() {
		ArrayList<MoleculeParams> ackParams = simParams.getAcknowledgmentMoleculeParams();
		ArrayList<MoleculeParams> infoParams = simParams.getInformationMoleculeParams();
		for (NanoMachineParam nmp : simParams.getTransmitterParams()){
			NanoMachine nm = NanoMachine.createTransmitter(nmp.getCenter(), nmp.getRadius(), nmp.getMolReleasePoint(), infoParams, this);
			growNanoMachine(nm); // adds NanoMachine to medium's grid
			nm.createInfoMolecules();
			transmitters.add(nm);
			nanoMachines.add(nm);
		}
		for (NanoMachineParam nmp : simParams.getReceiverParams()) {
			NanoMachine nm = NanoMachine.createReceiver(nmp.getCenter(), nmp.getRadius(), nmp.getMolReleasePoint(), ackParams, this);
			growNanoMachine(nm); // adds NanoMachine to medium's grid
			receivers.add(nm);
			nanoMachines.add(nm);			
		}
		for (IntermediateNodeParam inp : simParams.getIntermediateNodeParams()) {
			NanoMachine nm = NanoMachine.createIntermediateNode(inp.getCenter(), inp.getRadius(), 
					inp.getInfoMolReleasePoint(), inp.getAckMolReleasePoint(), infoParams, ackParams, this);
			growNanoMachine(nm); // adds NanoMachine to medium's grid
			transmitters.add(nm);
			receivers.add(nm);
			nanoMachines.add(nm);			
		}
	}

	// Adds nanoMachine to medium's grid throughout its entire volume. 
	private void growNanoMachine(NanoMachine nm) {
		Position center = nm.getPosition();
		int radius = nm.getRadius();
		// doubly nested loop to go over positions for all three dimensions.
		// note that the center position is included, so we subtract one 
		// from the radius, and go from -(radius - 1) to +(radius - 1) units 
		// around the center point in all directions.
		int startX = center.getX() - (radius - 1);
		int endX = center.getX() + (radius - 1);
		int startY = center.getY() - (radius - 1);
		int endY = center.getY() + (radius - 1);
		int startZ = center.getZ() - (radius - 1);
		int endZ = center.getZ() + (radius - 1);
		for(int x = startX; x <= endX; x++) {
			for(int y = startY; y <= endY; y++) {
				for(int z = startZ; z <= endZ; z++) {
					addObject(nm, new Position(x, y, z));
				}
			}
		}
	}
	
	private void createMicrotubules() {
		//		get microtubule params from simParams
		for(MicrotubuleParams mtps : simParams.getMicrotubuleParams()) {
			Position start = mtps.getStartPoint();
			Position end = mtps.getEndPoint();			
			Microtubule tempMT = new Microtubule(start, end, this);
			growMicrotubule(tempMT);
			microtubules.add(tempMT);
		}
	}

	//Adds microtubule to medium's grid all along its length
	private void growMicrotubule(Microtubule tempMT){
		//Collect all positions the microtubule occupies
		HashSet<Position> mtPos = new HashSet<Position>();
		Position start = tempMT.getStartPoint();
		mtPos.add(start);
		Position end = tempMT.getEndPoint();
		//Determine the direction the microtubule is pointed in, using doubles
		DoublePosition direction = tempMT.getUnitVector();
		DoublePosition currentPos = direction.toDouble(start);
		//Add positions to position set, until we reach the end of the microtubule
		while (!mtPos.contains(end)){	
			mtPos.addAll(direction.add(currentPos));
			currentPos = currentPos.addDouble(direction);
		}
		//Add microtubule and its positions to the grid
		addObjects(tempMT, mtPos);
	}

	//any cleanup tasks, including printing simulation results to monitor or file.
	private void endSim() throws IOException {
		String endMessage = "";
		if(simParams.isWait()) {
			endMessage = "Ending simulation: Last step: " + finishSimStep + " RetransmitNum: " + retransmitNum + "\n";
		} else {
			endMessage = "Ending simulation: Last step: " + simStep + " RetransmitNum: " + retransmitNum + "\n";
		}
		
		if(messagesCompleted < simParams.getNumMessages()){
			endMessage += "Total messages completed: " + messagesCompleted + 
					" out of " + simParams.getNumMessages() + "\n";
		} else {
			endMessage += "All " + simParams.getNumMessages() + " messages completed.\n";
		}
		
		if(!simParams.isBatchRun()) {
			System.out.print(endMessage);
			System.out.println("decomposing Num: " + decomposingNum);
//			if(simParams.isAdjust()) {
//				printNumMolecules();
//			}
		}
		if((outputFile != null) && (!simParams.isBatchRun())) {
			try {
				outputFile.write(endMessage);
			} catch (IOException e) {
				System.out.println("Error: unable to write to file: " + simParams.getOutputFileName());
				e.printStackTrace();
			}
		}

		if((outputFile != null) && (!simParams.isBatchRun())) {
			outputFile.close();
		} else if(simParams.isBatchRun()) {		// Append batch file result to batch file:		
			if(simParams.isAdjust()) {
				printBatchNumMolecules();
			}
			if(simParams.isUsingCollisions()) {
				printBatchCollision();
			}
			if(simParams.getNumRetransmissions() != 0 && simParams.getRetransmitWaitTime() != 0) {
				printBatchRetransmission();
			}
			if(simParams.isWait()) {
				printBatchWait();
			}
			
			FileWriter batchWriter = new FileWriter("batch_" + simParams.getOutputFileName(), APPEND_TO_FILE);
			if(batchWriter != null) {
//				if(simParams.isWait()) {
//					batchWriter.append(finishSimStep + "," + allInfoTime + "," + allInfoNum + "," + allAckTime + "," + allAckNum);
//				} else {
//					batchWriter.append(String.valueOf(simStep));
//				}
				batchWriter.append(String.valueOf(simStep) + "\n");
				batchWriter.close();
			}
		}
	}
	
	public void printBatchWait() throws IOException {
		FileWriter batchWriter = new FileWriter("ptime_batch_" + simParams.getOutputFileName(), APPEND_TO_FILE);
		if(batchWriter != null) {
			batchWriter.append(allInfoTime + "," + allInfoNum + "," + allAckTime + "," + allAckNum + "\n");
		}
		batchWriter.close();
	}
	
	public void printBatchCollision() throws IOException {
		FileWriter batchWriter = new FileWriter("collision_batch_" + simParams.getOutputFileName(), APPEND_TO_FILE);
		if(batchWriter != null) {
			ArrayList<Integer> collisionNum = calcCollisionNum();
			if(simParams.isUsingCollisions()) {
				String str = String.valueOf(collisions.get(0));
				for(int i = 1; i < collisions.size(); i++) {
					str += "/" + String.valueOf(collisions.get(i));
				}
				str += "," + String.valueOf(collisionNum.get(0));
				for(int i = 1; i < collisionNum.size(); i++) {
					str += "/" + String.valueOf(collisionNum.get(i));
				}
				if(simParams.isDecomposing()) {
					str += "," + String.valueOf(decomposingNum);
				}
				batchWriter.append(str + "\n");
			}
			batchWriter.close();
		}
	}
	
	public ArrayList<Integer> calcCollisionNum() {
		ArrayList<Integer> collisionNum = new ArrayList<Integer>();
		collisionNum.add(collisionAA.size());
		collisionNum.add(collisionAI.size());
		collisionNum.add(collisionAN.size());
		collisionNum.add(collisionII.size());
		collisionNum.add(collisionIN.size());
		
		return collisionNum;
	}
	
	public void printBatchRetransmission() throws IOException {
		FileWriter batchWriter = new FileWriter("retransmission_batch_" + simParams.getOutputFileName(), APPEND_TO_FILE);
		if(batchWriter != null) {
			String str = "";
			if(failure) {
				str = "F";
			} else {
				str = "T";
			}
			str += ",0";
			if(retransmitNum.size() != 0) {
				for(int i = 0; i < retransmitNum.size(); i++) {
					str += "/" + String.valueOf(retransmitNum.get(i));
				}
				str += ",0";
				for(int i = 0; i < txRetransmitNum.size(); i++) {
					str += "/" + String.valueOf(txRetransmitNum.get(i));
				}
				str += ",0";
				for(int i = 0; i < rxRetransmitNum.size(); i++) {
					str += "/" + String.valueOf(rxRetransmitNum.get(i));
				}
			}
			batchWriter.append(str + "\n");
			batchWriter.close();
		}
	}
	
	public void printBatchNumMolecules() throws IOException {
		stackAdjustParams();
		FileWriter batchWriter = new FileWriter("adjust_batch_" + simParams.getOutputFileName(), APPEND_TO_FILE);
		if(batchWriter != null) {
			if(simParams.isAdjust()) {
				for(int i = 0; i < adjustSteps.size() - 1; i++) {
					batchWriter.append(adjustSteps.get(i) + "/" + infoAdjustNum.get(i) + "/" + ackAdjustNum.get(i) + ",");
				}
				batchWriter.append(adjustSteps.get(adjustSteps.size() - 1) + "/" + infoAdjustNum.get(adjustSteps.size() - 1) + "/" + ackAdjustNum.get(adjustSteps.size() - 1) + "\n");
			}
			batchWriter.close();
		}
	}
	
	public void printNumMolecules() {
		stackAdjustParams();
		try {
			File file = new File("adjustNum.txt");
			PrintWriter pw = new PrintWriter(new BufferedWriter(new FileWriter(file)));
			for(int i = 0; i < adjustSteps.size(); i++) {
				System.out.println(adjustSteps.get(i) + ": " + infoAdjustNum.get(i) + "/" + ackAdjustNum.get(i));
				pw.println(adjustSteps.get(i) + "," + infoAdjustNum.get(i) + "," + ackAdjustNum.get(i));
			}
			pw.close();
		} catch(IOException e) {
			e.printStackTrace();
		}
	}
	
	public void stackAdjustParams() {
		adjustSteps.add(simStep);
		for(MoleculeParams param: simParams.getAllMoleculeParams()) {
			switch(param.getMoleculeType()) {
			case INFO:
				infoAdjustNum.add(param.getNumMolecules());
				break;
			case ACK:
				ackAdjustNum.add(param.getNumMolecules());
				break;
			default:
				break;
			}
		}
	}
	
	public void addTxRetransmitNum() {
		retransmitNum.add(simStep);
		txRetransmitNum.add(simStep);
	}
	
	public void addRxRetransmitNum() {
		retransmitNum.add(simStep);
		rxRetransmitNum.add(simStep);
	}
	
	public void addDecomposingNum() {
		decomposingNum++;
	}
	
	public int getNumRequiredPackets() {
		return simParams.getNumRequiredPackets();
	}

	/** Add molecules to molecules list field
	 * 
	 * @param mols List of molecules to add to simulation list
	 */
	public void addMolecules(ArrayList<Molecule> mols) {
		for (Molecule mol : mols){
			// Only add the molecules to the movingMolecules list if they do, in fact, move.
			if(!(mol.getMovementController() instanceof NullMovementController)) {
				movingMolecules.add(mol);
			}
			addObject(mol, mol.getPosition());
		}
	}

	//Reports to the console that a message has been completed
	public void completedMessage(int msgNum) {
		messagesCompleted = msgNum;
		String completedMessage = "Completed message: " + msgNum + ", at step: " + simStep + "\n";
		finishSimStep = simStep;
//		stackAdjustParams();
		
		if(msgNum >= simParams.getNumMessages()){
			lastMsgCompleted = true;
			completedMessage += "Last message completed.\n";
		}
		if(!simParams.isBatchRun()) { 
			System.out.print(completedMessage);
		}
		if((outputFile != null)  && (!simParams.isBatchRun())) {
			try {
				outputFile.write(completedMessage);
			} catch (IOException e) {
				System.out.println("Error: unable to write to file: " + simParams.getOutputFileName());
				e.printStackTrace();
			}
		}
	}
	
	public void recievedMessage(int msgId,int numRecievedPackets) {	
		String recievedMessage = "Received message: " + (msgId + 1) + "-" +
					numRecievedPackets + ", at step: " + simStep + "\n";
		if(!simParams.isBatchRun()) { 
//			System.out.print(recievedMessage);
		}
		if((outputFile != null)  && (!simParams.isBatchRun())) {
//			try {
//				outputFile.write(recievedMessage);
//			} catch (IOException e) {
//				System.out.println("Error: unable to write to file: " + simParams.getOutputFileName());
//				e.printStackTrace();
//			}
		}
	}

	public int getMessagesCompleted() {
		return messagesCompleted;
	}

	public SimulationParams getSimParams() {
		return simParams;
	}

	public ArrayList<Molecule> getMovingMolecules() {
		return movingMolecules;
	}

	public ArrayList<Microtubule> getMicrotubules() {
		return microtubules;
	}

	public ArrayList<NanoMachine> getNanoMachines() {
		return nanoMachines;
	}

	public Medium getMedium() {
		return medium;
	}

	public ArrayList<NanoMachine> getReceivers() {
		return receivers;
	}

	public ArrayList<NanoMachine> getTransmitters() {
		return transmitters;
	}

	public boolean isUsingAcknowledgements() {
		return simParams.isUsingAcknowledgements();
	}

	public int getNumRetransmissions() {
		return simParams.getNumRetransmissions();
	}
	
	public boolean isUsingCollisions() {
		return simParams.isUsingCollisions();
	}
	
	public boolean assembling(){
		return simParams.isAssembling();
	}
	
	public boolean decomposing(){
		return simParams.isDecomposing();
	}
	
	public int getDecomposingMode() {
		return simParams.getDecomposing();
	}
	
	public int getRetransmitWaitTime(){
		return simParams.getRetransmitWaitTime();
	}
	
	public void setFailure(boolean failure) {
		this.failure = failure;
	}
	
	public ForwardErrorCorrection getFEC() {
		return FEC;
	}
	
	//Add an object to the medium's position grid
	public void addObject(Object obj, Position pos){
		medium.addObject(obj, pos);
	}
	
	//Add an object to multiple positions in the medium's grid
	public void addObjects(Object obj, HashSet<Position> pos){
		for (Position p : pos){
			medium.addObject(obj, p);
		}
	}
	
	public void moveObject(Object obj, Position oldPos, Position newPos){
		medium.moveObject(obj, oldPos, newPos);
	}
	
	public boolean isOccupied(Position pos){
		return medium.isOccupied(pos);
	}
	
	//Removes all molecules located at the garbageSpot, waiting to be deleted
	public void collectGarbage(){
		ArrayList<Object> garbage = medium.getObjectsAtPos(medium.garbageSpot());
		medium.collectGarbage();
		for (Object o : garbage){
			movingMolecules.remove(o);
		}
	}
	
	public FileWriter getOutputFile() {
		return outputFile;
	}
	
	public void addCollisionNum(Molecule mol, Position nextPosition, MolComSim simulation) {
		ArrayList<Object> alreadyThere = simulation.getMedium().getObjectsAtPos(nextPosition);
		if(alreadyThere == null) {
			return;
		}
		
		for(Object o : alreadyThere) {
			// なぜか自分と衝突判定してる??
			if(mol != o) {
				String myMolType = mol.getClass().getName();
				String collMolType = o.getClass().getName();
				String types[] = {myMolType, collMolType};
				
//				System.out.println(myMolType + " vs " + collMolType);
				
				// Info,Ack,Noiseの場合だけ
				if(Arrays.asList(MolType).contains(myMolType) && Arrays.asList(MolType).contains(collMolType)) {
					Arrays.sort(types); // Ack -> Info -> Noise順番に
					if(myMolType == MolType[0]) {
						if(collMolType == MolType[0]) {
							collisionAA.add(simStep);
						} else if(collMolType == MolType[1]) {
							collisionAI.add(simStep);
						} else if(collMolType == MolType[2]) {
							collisionAN.add(simStep);
						}
					} else if(myMolType == MolType[1]) {
						if(collMolType == MolType[1]) {
							collisionII.add(simStep);
						} else if(collMolType == MolType[2]) {
							collisionIN.add(simStep);
						}
					}
					collisions.add(simStep);
				}
			}
		}
	}
}
