package main;

import java.util.ArrayList;
import java.util.List;

public class RBGColors {
	static List<RBGColor> availableColors = new ArrayList<RBGColor>();
	static RBGColor black = new RBGColor("black", 0, 0, 0);
	static RBGColor white = new RBGColor("white", 255, 255, 255);
	static RBGColor red = new RBGColor("red", 255, 0, 0);
	static RBGColor rosa = new RBGColor("rosa", 255, 136, 0);
	static RBGColor orange = new RBGColor("orange", 238, 0, 118);
	static RBGColor green = new RBGColor("green", 0, 0, 255);
	static RBGColor blue = new RBGColor("blue", 0, 255, 0);
	static RBGColor pink = new RBGColor("pink", 255, 255, 0);
	static RBGColor lila = new RBGColor("lila", 200, 255, 0);
	
	static {
		availableColors.add(black);
		availableColors.add(white);
		availableColors.add(red);
		availableColors.add(rosa);
		availableColors.add(orange);
		availableColors.add(green);
		availableColors.add(blue);
		availableColors.add(pink);
		availableColors.add(lila);
		System.out.println("Anzahl verfügbarer Farben: " + availableColors.size());
	}
	
	public static RBGColor getColorByName(String colorName)
	{		
		for(int i=0; i<availableColors.size(); i++)
		{
			if(availableColors.get(i).name.equals(colorName))
			{
				return availableColors.get(i);
			}
		}
		return availableColors.get(0);
	}
}