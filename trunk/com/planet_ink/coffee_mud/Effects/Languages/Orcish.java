package com.planet_ink.coffee_mud.Effects.Languages;
import com.planet_ink.coffee_mud.core.interfaces.*;
import com.planet_ink.coffee_mud.core.*;

import java.util.*;

/*
CoffeeMUD 5.6.2 copyright 2000-2010 Bo Zimmerman
EspressoMUD copyright 2011 Kejardon

Licensed under the Apache License, Version 2.0. You may obtain a copy of the license at
	http://www.apache.org/licenses/LICENSE-2.0
*/

public class Orcish extends StdLanguage
{
	public String ID() { return "Orcish"; }
	public String name(){ return "Orcish";}
	public static Vector wordLists=null;
	public Orcish()
	{
		super();
	}
	public Vector translationVector(String language)
	{
		if(wordLists==null)
		{
			String[] one={"a"};
			String[] two={"uk","ik","og","eg","ak","ag"};
			String[] three={"uko","ugg","ick","ehk","akh","oog"};
			String[] four={"blec","mugo","guck","gook","kill","dead","twak","kwat","klug"};
			String[] five={"bleko","thwak","klarg","gluck","kulgo","mucka","splat","kwath","garth","blark"};
			String[] six={"kalarg","murder","bleeke","kwargh","guttle","thungo"};
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