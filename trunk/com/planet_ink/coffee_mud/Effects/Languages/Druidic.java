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

public class Druidic extends StdLanguage
{
	@Override public String ID() { return "Druidic"; }
	public String name(){ return "Druidic";}
	public static Vector wordLists=null;
	public Druidic()
	{
		super();
	}

	@Override public Vector<String[]> translationVector(String language)
	{
		if(wordLists==null)
		{
			String[] one={""};
			String[] two={"hissssss","hoo","caw","arf","bow-wow","bzzzzzz"};
			String[] three={"chirp","tweet","mooooo","oink","quack","tweet"};
			String[] four={"ruff","meow","grrrrowl","roar","cluck","honk"};
			String[] five={"croak","bark","blub-blub","cuckoo","squeak","peep"};
			String[] six={"gobble-gobble","ribbit","b-a-a-a-h","n-a-a-a-y","heehaw","cock-a-doodle-doo"};
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