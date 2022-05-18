package main;

import java.util.GregorianCalendar;

import com.pi4j.io.spi.SpiDevice;
import com.pi4j.wiringpi.Gpio;
import com.pi4j.wiringpi.Spi;

public class LEDChangerV14 {

	public int num_leds = 158;
	private byte[] currentColors;

	SpiDevice spiDevice;
	private int delay = 550;

	private Thread lightThread;
	private Thread reactionThread;
	private Thread stopThread;
//	private Thread funThread;

	GregorianCalendar lastSending;

	public LEDChangerV14(int num_ledsParam) {
		// 1mhz = 1.000.000
		// RPI max Freq = 1.400.000
//		System.out.println("Out:" + Spi.wiringPiSPISetup(0, 43750));// 1mhz=1000000
//		System.out.println("Out:" + Spi.wiringPiSPISetup(0, 1400000)); // Halbwegs gute Ergebnisse
		System.out.println("Out:" + Spi.wiringPiSPISetupMode(0, 1400000, Spi.MODE_2)); // Beste Ergebnisse bisher
//		System.out.println("Out:" + Gpio.wiringPiSetupSys());
		
		if(num_ledsParam > 0)
			num_leds = num_ledsParam;
		
		currentColors = new byte[num_leds * 3];
	}

	public void LightTheFire() {
		LightTheFire("white");
	}

	public void LightTheFire(String color) {
//		interruptFunThread();
		
		lightThread = new Thread(new Runnable() {
			@Override
			public void run() {
				byte[] newColors = currentColors.clone();
				RBGColor newColor = RBGColors.getColorByName(color);
				System.out.println("Newcolor red: " + newColor.red + " blue: " + newColor.blue + " green: " + newColor.green);
				try {
					for (int i = 0; i < newColors.length; i = i + 3) {
						newColors[i] = (byte) newColor.red;
						newColors[i + 1] = (byte) newColor.blue;
						newColors[i + 2] = (byte) newColor.green;
						SendColors(newColors);
					}

					SendColors(newColors);
				} catch (Exception e) {
					e.printStackTrace();
				}
				lastSending = new GregorianCalendar();
			}
		});
		lightThread.start();
	}

//	public void interruptFunThread() {
//		StringBuffer funThreadOutput = new StringBuffer("FunThread is: ");
//		if(funThread != null)
//		{
//			funThreadOutput.append("not Null");
//			if(funThread.isAlive())
//			{
//				funThreadOutput.append(" and Alive");
//				if(!funThread.isInterrupted())
//				{
//					funThreadOutput.append(" and NOT interrupted");
//					funThread.interrupt();
//				}
//			}
//		}
//		System.out.println(funThreadOutput);
//	}

	public void ShowReaction() {
		reactionThread = new Thread(new Runnable() {
			@Override
			public void run() {
				try {
					byte[] previousColors = currentColors.clone();
					byte[] newColors = currentColors.clone();

					int currentByteAsInt = Byte.toUnsignedInt(newColors[0]);
					int divToHalf = currentByteAsInt - 127;
					int invertIntValue = 127 - divToHalf;
//					System.out.println("Red: asInt: " + currentByteAsInt + "\tdivToHalf: " + divToHalf + "\tinvert: "
//							+ invertIntValue);
					int colorToUse1 = invertIntValue;

					currentByteAsInt = Byte.toUnsignedInt(newColors[1]);
					divToHalf = currentByteAsInt - 127;
					invertIntValue = 127 - divToHalf;
//					System.out.println("Blue: asInt: " + currentByteAsInt + "\tdivToHalf: " + divToHalf + "\tinvert: "
//							+ invertIntValue);
					int colorToUse2 = invertIntValue;

					currentByteAsInt = Byte.toUnsignedInt(newColors[2]);
					divToHalf = currentByteAsInt - 127;
					invertIntValue = 127 - divToHalf;
//					System.out.println("Green: asInt: " + currentByteAsInt + "\tdivToHalf: " + divToHalf + "\tinvert: "
//							+ invertIntValue);
					int colorToUse3 = invertIntValue;

					newColors = new byte[newColors.length];
					for (int i = 0; i < num_leds * 3; i = i + 3) {
						newColors[i] = (byte) colorToUse1;
						newColors[i + 1] = (byte) colorToUse2;
						newColors[i + 2] = (byte) colorToUse3;
					}

					SendColors(newColors);
//
					Utils.wait(500);

					newColors = previousColors;
					SendColors(newColors);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
		reactionThread.start();
	}

	public void StopTheFire() {
		stopThread = new Thread(new Runnable() {
			@Override
			public void run() {
				try {
					// fade all lights out...
//					for (int i = 0; i < colors.length / 2; i++) {
//						colors[(colors.length - 1) - i] = (byte) 0;
//						colors[i] = (byte) 0;
//						SendColors();
//					}

					byte[] newColors = new byte[currentColors.length];
					for (int i = 0; i < newColors.length; i++) {
						newColors[i] = (byte) 0;
						SendColors(newColors);
					}
					SendColors(newColors);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
		stopThread.start();
	}

//	public void FunStuff() {
//		funThread = new Thread(new Runnable() {
//			@Override
//			public void run() {
//				try {
//					byte[] previousColors = currentColors.clone();
//					byte[] newColors = currentColors.clone();
//					
////					for (int i = 0; i < num_leds * 3; i = i + 3) {
////						if (!Thread.interrupted()) {
////							newColors = previousColors.clone();
////							newColors[i] = (byte) RBGColors.green.red;
////							newColors[i + 1] = (byte) RBGColors.green.blue;
////							newColors[i + 2] = (byte) RBGColors.green.green;
////							if (i > 3) {
////								newColors[i - 3] = (byte) RBGColors.red.red;
////								newColors[i - 2] = (byte) RBGColors.red.blue;
////								newColors[i - 1] = (byte) RBGColors.red.green;
////							}
////							if (i > 6) {
////								newColors[i - 4] = (byte) RBGColors.blue.red;
////								newColors[i - 5] = (byte) RBGColors.blue.blue;
////								newColors[i - 6] = (byte) RBGColors.blue.green;
////							}
////							SendColors(newColors);
////							Thread.sleep(500);
////							lastSending = new GregorianCalendar();
////						} else {
////							Thread.sleep(10);
////						}
////					}
//
//					if(!Thread.interrupted())
//					{
//						Thread.sleep(500);
//					}
//					
//					if(!Thread.interrupted())
//					{
//						newColors = previousColors;
//						SendColors(newColors);
//
//						lastSending = new GregorianCalendar();
//					}
//				} catch (Exception e) {
//					e.printStackTrace();
//				}
//			}
//		});
//		funThread.start();
//	}

//	public void LightTheRainbow() {
//		lightThread = new Thread(new Runnable() {
//			@Override
//			public void run() {
//				try {
//
//					colors = new byte[num_leds * 3];
//					for (int i = 0; i < num_leds * 3; i = i + 3) {
//						if (i % 6 == 0) {
//							colors[i] = (byte) 254;
//							colors[i + 1] = (byte) 0;
//							colors[i + 2] = (byte) 0;
//						} else if (i % 9 == 0) {
//							colors[i] = (byte) 0;
//							colors[i + 1] = (byte) 254;
//							colors[i + 2] = (byte) 0;
//						} else {
//							colors[i] = (byte) 0;
//							colors[i + 1] = (byte) 0;
//							colors[i + 2] = (byte) 254;
//						}
//					}
////					
//					SendColors();
//				} catch (Exception e) {
//					e.printStackTrace();
//				}
//			}
//		});
//		lightThread.start();
//	}
//
//	public void LetsDance() {
//		Thread DanceThread = new Thread(new Runnable() {
//			@Override
//			public void run() {
//				try {
//					Boolean even = true;
//					while (true) {
//						colors = colors = new byte[num_leds * 3];
//						if (even) {
//							for (int i = 0; i < num_leds * 3; i = i + 3) {
//								colors[i] = (byte) 200;
//								colors[i + 1] = (byte) 200;
//								colors[i + 2] = (byte) 200;
//							}
//						}
//						even = !even;
//						SendColors();
//					}
//
////					SendColors();
//				} catch (Exception e) {
//					e.printStackTrace();
//				}
//			}
//		});
//		DanceThread.start();
//	}

	private void SendColors(byte[] data) {
		Spi.wiringPiSPIDataRW(0, data.clone(), data.length);
		Gpio.delayMicroseconds(delay); // need to determine optimal value

		Spi.wiringPiSPIDataRW(0, data.clone());
		Gpio.delayMicroseconds(delay); // need to determine optimal value

		currentColors = data.clone();
		
//		System.out.println("Colors were send");
	}

}
