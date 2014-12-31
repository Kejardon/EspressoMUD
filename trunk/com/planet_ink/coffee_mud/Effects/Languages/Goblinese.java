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

public class Goblinese extends StdLanguage
{
	public String ID() { return "Goblinese"; }
	public String name(){ return "Goblinese";}
	public static Vector wordLists=null;
	public Goblinese()
	{
		super();
	}
	public Vector translationVector(String language)
	{
		if(wordLists==null)
		{
			String[] one={"i","klpt","ih","g"};
			String[] two={"te","il","ag","go"};
			String[] three={"nik","rem","tit","nip","pop","pon","ipi","wip","pec"};
			String[] four={"perp","merp","nerp","pein","noog","gobo","koer","werp","terp","tert","grlt","Jrl","gran","kert"};
			String[] five={"whamb","thwam","nipgo","pungo","upoin","krepe","tungo","pongo","twang","hrgap","splt","krnch","baam","poww"};
			String[] six={"tawthak","krsplt","palpep","poopoo","dungdung","owwie","greepnak","tengak","grnoc","pisspiss","phlyyytt","plllb","hrangnok","ticktick","nurang"};
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