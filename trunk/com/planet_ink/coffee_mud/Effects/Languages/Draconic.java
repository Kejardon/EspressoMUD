package com.planet_ink.coffee_mud.Effects.Languages;
import com.planet_ink.coffee_mud.core.interfaces.*;
import com.planet_ink.coffee_mud.core.*;
import com.planet_ink.coffee_mud.Effects.interfaces.*;
import com.planet_ink.coffee_mud.Areas.interfaces.*;
import com.planet_ink.coffee_mud.Behaviors.interfaces.*;
import com.planet_ink.coffee_mud.Commands.interfaces.*;
import com.planet_ink.coffee_mud.Common.interfaces.*;
import com.planet_ink.coffee_mud.Exits.interfaces.*;
import com.planet_ink.coffee_mud.Items.interfaces.*;
import com.planet_ink.coffee_mud.Locales.interfaces.*;
import com.planet_ink.coffee_mud.MOBS.interfaces.*;
import com.planet_ink.coffee_mud.Races.interfaces.*;

import java.util.*;

/*
CoffeeMUD 5.6.2 copyright 2000-2010 Bo Zimmerman
EspressoMUD copyright 2011 Kejardon

Licensed under the Apache License, Version 2.0. You may obtain a copy of the license at
	http://www.apache.org/licenses/LICENSE-2.0
*/
@SuppressWarnings("unchecked")
public class Draconic extends StdLanguage
{
	public String ID() { return "Draconic"; }
	public String name(){ return "Draconic";}
	public static Vector wordLists=null;
	private static boolean mapped=false;
	public Draconic()
	{
		super();
	}

	public Vector translationVector(String language)
	{
		if(wordLists==null)
		{
			String[] one={"y"};
			String[] two={"ve","ov","iv","si","es","se"};
			String[] three={"see","sev","ave","ces","ven","sod"};
			String[] four={"nirg","avet","sav`e","choc","sess","sens","vent","vens","sven","yans","vays"};
			String[] five={"splut","svets","fruite","dwagg","vrers","verrs","srens","swath","senys","varen"};
			String[] six={"choccie","svenren","yorens","vyrues","whyrie","vrysenso","forin","sinnes","sessis","uroven","xorers","nosees"};
			wordLists=new Vector();
			wordLists.addElement(one);
			wordLists.addElement(two);
			wordLists.addElement(three);
			wordLists.addElement(four);
			wordLists.addElement(five);
			wordLists.addElement(six);
		}
		return wordLists;
	}
}