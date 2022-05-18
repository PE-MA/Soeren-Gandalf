package main;

import java.util.GregorianCalendar;
import java.util.Scanner;

public class Hello {

	private static Thread mainThread;
	private static Thread voiceListenerThread;
//	private static Thread timerThread;
	private static LEDChangerV14 ledChanger;
//	private static int timeToFunStuff = 0;
	
	public static void main(String[] args) {
		
		int num_leds = 0;
		if(args.length == 2)
		{
			num_leds = Integer.parseInt(args[1]);
			if(num_leds == 0)
				num_leds = 158;
		}
		
		ledChanger = new LEDChangerV14(num_leds);
		
		if(args[0].toLowerCase().equals("voice"))
		{
			startVoiceAction();
		}
		else if (args[0].toLowerCase().equals("console"))
		{
			startConsoleAction();
		}
		else if (args[0].toLowerCase().equals("simple"))
		{
			SimpleLEDTest.DoSimpleTest(num_leds);
		}
	}
	
	private static void startVoiceAction()
	{
		mainThread = Thread.currentThread();
		Runtime.getRuntime().addShutdownHook(new Thread() {
			public void run()
			{
//				ledChanger.interruptFunThread();
				
				System.out.println("Fahre voice runter");
				voiceListenerThread.interrupt();
				
				System.out.println("Fahre mich runter");
				Hello.mainThread.interrupt();
			}
		});
		VoiceListener vl = new VoiceListener(ledChanger);
		voiceListenerThread = new Thread(vl, "VoiceListenerThread");
		voiceListenerThread.setPriority(Thread.MAX_PRIORITY);
		voiceListenerThread.start();
		
//		timerThread = new Thread(new Runnable() {
//			@Override
//			public void run() {
//				while (voiceListenerThread.isAlive()) {
//					if (ledChanger.lastSending != null) {
//						long timePassed = new GregorianCalendar().getTimeInMillis() - ledChanger.lastSending.getTimeInMillis();
//						System.out.println(timePassed);
//						if (timePassed > timeToFunStuff && timeToFunStuff > 0) {
//							ledChanger.FunStuff();
//							ledChanger.lastSending = new GregorianCalendar();
//						}
//						else
//							try {
//								Thread.sleep(1000);
//							} catch (InterruptedException e) {
//								e.printStackTrace();
//							}
//					} else
//						try {
//							Thread.sleep(1000);
//						} catch (InterruptedException e) {
//							e.printStackTrace();
//						}
//				}
//			}
//		});
//		timerThread.start();
	}
	
	private static void startConsoleAction()
	{
		Scanner scan = new Scanner(System.in);
		String input = "";
		while(!input.equals("stop"))
		{
			input = scan.nextLine();
			System.out.println(input);
			if(input.equals("w"))
			{
				ledChanger.LightTheFire("white");
			}
			else if(input.equals("g"))
			{
				ledChanger.LightTheFire("green");
			}
			else if(input.equals("r"))
			{
				ledChanger.LightTheFire("red");
			}
			else if(input.equals("b"))
			{
				ledChanger.LightTheFire("blue");
			}
			else if(input.equals("d"))
			{
				ledChanger.StopTheFire();
			}
			else if(input.equals("reaction"))
			{
				ledChanger.ShowReaction();
			}
			else
			{
				System.out.println("Ich habe dich nicht verstanden");
			}
		}
	}
}
