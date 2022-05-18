package main;

public class Utils {
	public static void showByte(String preString, byte data) {
		System.out.print(preString + ": ");
		System.out.print(String.format("0x%02X", data));
		System.out.println();
	}

	public static void showByteArray(String preString, byte[] colorData) {
		System.out.print(preString + ": ");
		for (int i = 0; i < colorData.length; i++) {
			System.out.print(String.format("0x%02X", colorData[i]));
//			System.out.print(colorData[i]);
		}
		System.out.println();
	}
	
	public static void wait(int ms) {
		try {
			Thread.sleep(ms);
		} catch (InterruptedException ex) {
			Thread.currentThread().interrupt();
		}
	}
}
