package main;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Map;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.Line;
import javax.sound.sampled.Mixer;
import javax.sound.sampled.TargetDataLine;

import ai.picovoice.picovoice.Picovoice;
import ai.picovoice.picovoice.PicovoiceInferenceCallback;
import ai.picovoice.picovoice.PicovoiceWakeWordCallback;

/**
 * A sample program is to demonstrate how to record sound in Java author:
 * www.codejava.net
 */
public class VoiceListener implements Runnable {
	/**
	 * Entry to run the program
	 */
	LEDChangerV14 ledChanger;

	public VoiceListener(LEDChangerV14 ledChanger) {
//		showAudioDevices();
		this.ledChanger = ledChanger;
	}

	@Override
	public void run() {
		StartPicoVoice();
	}

	private void StartPicoVoice() {
		AudioFormat format = new AudioFormat(16000f, 16, 1, true, false);

		// get audio capture device
		DataLine.Info dataLineInfo = new DataLine.Info(TargetDataLine.class, format);
		TargetDataLine micDataLine = null;
		try {
			micDataLine = getAudioDevice(4, dataLineInfo);
			micDataLine.open(format);
		} catch (Exception e) {
			System.err.println("Failed to get a valid capture device. Use --show_audio_devices to "
					+ "show available capture devices and their indices");
			System.exit(1);
			return;
		}

		PicovoiceWakeWordCallback wakeWordCallback = () -> {
//			playSound("Ja_Meister.wav");
			System.out.println("Ja Meister?");
			ledChanger.ShowReaction();

		};
		PicovoiceInferenceCallback inferenceCallback = inference -> {
			if (inference.getIsUnderstood()) {
				String intent = inference.getIntent();
				System.out.println("{");
				System.out.println(String.format("  intent : '%s'", inference.getIntent()));
				System.out.println("  slots : {");
				for (Map.Entry<String, String> slot : inference.getSlots().entrySet()) {
					System.out.println(String.format("    %s : '%s'", slot.getKey(), slot.getValue()));
				}
				System.out.println("  }");
				System.out.println("}");

				if (intent.equals("changeState")) {
					Map<String, String> slots = inference.getSlots();
					String stateValue = slots.getOrDefault("state", "");
					if (!stateValue.isEmpty()) {
						if (stateValue.equals("an")) {
							ledChanger.LightTheFire();
							System.out.println("Das Licht geht an");
						} else if (stateValue.equals("aus")) {
							ledChanger.StopTheFire();
							System.out.println("Das Licht geht aus");
						}
					}
				} else if (intent.equals("changeColor")) {
					Map<String, String> slots = inference.getSlots();
					String stateValue = slots.getOrDefault("color", "");
					if (!stateValue.isEmpty()) {
						ledChanger.LightTheFire(Languages.getEnglishStateValue(stateValue));
						System.out.println("Das Licht wird: " + Languages.getEnglishStateValue(stateValue));
					}
				}
			} else {
				System.out.println("Didn't understand the command.");
			}
		};

		Picovoice picovoice = null;

		try {

			picovoice = new Picovoice.Builder()
					.setKeywordPath(
							"/home/pi/JavaTests/Resources/sören-gandalf__de_raspberry-pi_2021-11-07-utc_v1_9_0.ppn")
					.setWakeWordCallback(wakeWordCallback)
					.setContextPath("/home/pi/JavaTests/Resources/Lichta_de_raspberry-pi_2021-10-07-utc_v1_6_0.rhn")
					.setInferenceCallback(inferenceCallback)
					.setPorcupineLibraryPath("/home/pi/JavaTests/Resources/libpv_porcupine_jni.so")
					.setPorcupineModelPath("/home/pi/JavaTests/Resources/porcupine_params_de.pv")
					.setPorcupineSensitivity(1f).setRhinoLibraryPath("/home/pi/JavaTests/Resources/libpv_rhino_jni.so")
					.setRhinoModelPath("/home/pi/JavaTests/Resources/rhino_params_de.pv").setRhinoSensitivity(0.9f)
					.build();

			micDataLine.start();
			System.out.println("Press enter to stop recording.");
			System.out.println("Listening...");

			// buffers for processing audio
			int frameLength = picovoice.getFrameLength();
			ByteBuffer captureBuffer = ByteBuffer.allocate(frameLength * 2);
			captureBuffer.order(ByteOrder.LITTLE_ENDIAN);
			short[] picovoiceBuffer = new short[frameLength];

			int numBytesRead;
			ledChanger.ShowReaction();
			while (System.in.available() == 0) {

				// read a buffer of audio
				numBytesRead = micDataLine.read(captureBuffer.array(), 0, captureBuffer.capacity());

				// don't pass to Picovoice if we don't have a full buffer
				if (numBytesRead != frameLength * 2) {
					continue;
				}

				// copy into 16-bit buffer
				captureBuffer.asShortBuffer().get(picovoiceBuffer);

				// process with picovoice
				picovoice.process(picovoiceBuffer);
			}
			System.out.println("Stopping...");
		} catch (Exception e) {
			System.err.println(e.toString());
		} finally {
			if (picovoice != null) {
				picovoice.delete();
			}
		}
	}

	private static TargetDataLine getAudioDevice(int deviceIndex, DataLine.Info dataLineInfo) throws Exception {
		if (deviceIndex >= 0) {
			try {
				Mixer.Info mixerInfo = AudioSystem.getMixerInfo()[deviceIndex];
				Mixer mixer = AudioSystem.getMixer(mixerInfo);

				if (mixer.isLineSupported(dataLineInfo)) {
					return (TargetDataLine) mixer.getLine(dataLineInfo);
				} else {
					System.err.printf("Audio capture device at index %s does not support the audio format required by "
							+ "Picovoice. Using default capture device.", deviceIndex);
				}
			} catch (Exception e) {
				System.err.printf("No capture device found at index %s. Using default capture device.", deviceIndex);
			}
		}

		// use default capture device if we couldn't get the one requested
		return getDefaultCaptureDevice(dataLineInfo);
	}

	private static TargetDataLine getDefaultCaptureDevice(DataLine.Info dataLineInfo) throws Exception {

		if (!AudioSystem.isLineSupported(dataLineInfo)) {
			throw new Exception("Default capture device does not support the audio "
					+ "format required by Picovoice (16kHz, 16-bit, linearly-encoded, single-channel PCM).");
		}

		return (TargetDataLine) AudioSystem.getLine(dataLineInfo);
	}

	private static void showAudioDevices() {

		System.out.println("Showing Audio Devices: ");
		// get available audio devices
		Mixer.Info[] allMixerInfo = AudioSystem.getMixerInfo();
		Line.Info captureLine = new Line.Info(TargetDataLine.class);

		for (int i = 0; i < allMixerInfo.length; i++) {

			// check if supports capture in the format we need
			Mixer mixer = AudioSystem.getMixer(allMixerInfo[i]);
			if (mixer.isLineSupported(captureLine)) {
				System.out.printf("Device %d: %s\n", i, allMixerInfo[i].getName());
			}
		}
	}
}