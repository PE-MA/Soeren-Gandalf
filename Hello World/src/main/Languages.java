package main;

import java.util.HashMap;

public class Languages {
	public static HashMap<String, String> StateValues = new HashMap<>();
	static {
		StateValues.put("rot", "red");
		StateValues.put("gr�n", "green");
		StateValues.put("blau", "blue");
		StateValues.put("wei�", "white");
		StateValues.put("orange", "orange");
		StateValues.put("pink", "pink");
		StateValues.put("lila", "lila");
	}

	public static String getEnglishStateValue(String germanStateValue) {
		return StateValues.get(germanStateValue);
	}
}
