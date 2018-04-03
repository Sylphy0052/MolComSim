import java.util.Scanner;

public class FECParams {
	private FECMethodType method;
	private int packetNum = 0;
	private int requiredPackets = 0;
	private double rate;

	public FECParams(Scanner readParams) {
		method = FECMethodType.getFECMethodType(readParams.next());
		switch (method) {
		case PARITYCHECK:
			requiredPackets = readParams.nextInt();
			rate = readParams.nextDouble();
			packetNum = (int)(requiredPackets * (1.0 + rate));
//			System.out.println("requiredPackets: " + String.valueOf(requiredPackets));
//			System.out.println("packetNum: " + String.valueOf(packetNum));
//			parityCheckParams(readParams);
			break;
		
		case NONE:
			noneParams(readParams);
			break;
			
		default:
			break;
		}
//		debug();
	}
	
//	private void parityCheckParams(Scanner readParams) {
//		method = FECMethodType.PARITYCHECK;
//		rate = readParams.nextDouble();
//	}
	
	private void noneParams(Scanner readParams) {
		method = FECMethodType.NONE;
	}
	
	@SuppressWarnings("unused")
	private void debug() {
		System.out.println("method: " + method.toString());
		System.out.println("rate: " + rate);
	}
	
	public FECMethodType getFECMethod() {
		return method;
	}
	
	public double getFECRate() {
		return rate;
	}
	
	public int getPacketNum() {
		return packetNum;
	}
	
	public int getNumRequiredPacket() {
		return requiredPackets;
	}
}
