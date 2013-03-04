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
@SuppressWarnings("unchecked")
public class Drunken extends StdLanguage
{
	public String ID() { return "Drunken"; }
	public String name(){ return "Drunken";}
	public static Vector wordLists=null;
	public Drunken()
	{
		super();
	}

	public Vector translationVector(String language)
	{
		return wordLists;
	}

	protected Vector getSChoices(StringBuffer word)
	{
		Vector V=new Vector();
		int x=word.toString().toUpperCase().indexOf("S");
		while(x>=0)
		{
			if((x>=word.length()-1)||(Character.toUpperCase(word.charAt(x+1))!='H'))
				V.addElement(Integer.valueOf(x));
			x=word.toString().toUpperCase().indexOf("S",x+1);
		}
		return V;
	}

	protected Vector getVChoices(StringBuffer word)
	{
		Vector V=new Vector();
		for(int x=0;x<word.length();x++)
		{
			if(("AEIOU").indexOf(Character.toUpperCase(word.charAt(x)))>=0)
			{
				if(V.contains(Integer.valueOf(x-1)))
					V.remove(Integer.valueOf(x-1));
				V.addElement(Integer.valueOf(x));
			}
		}
		return V;
	}

	public String translate(String language, String word)
	{
		StringBuffer sbw=new StringBuffer(word);
		Vector V=getSChoices(sbw);
		if(V.size()>0)
			sbw.insert(((Integer)V.elementAt(CMLib.dice().roll(1,V.size(),-1))).intValue()+1,'h');
		if(CMLib.dice().rollPercentage()<50)
			return fixCase(word,sbw.toString());

		V=getVChoices(sbw);
		if(V.size()>0)
		switch(CMLib.dice().roll(1,3,0))
		{
		case 1:
			{
				int x=((Integer)V.elementAt(CMLib.dice().roll(1,V.size(),-1))).intValue();
				for(int i=0;i<CMLib.dice().roll(1,5,0);i++)
					sbw.insert(x+1,sbw.charAt(x));
				break;
			}
		case 2:
			{
				int x=((Integer)V.elementAt(CMLib.dice().roll(1,V.size(),-1))).intValue();
				for(int i=0;i<CMLib.dice().roll(1,5,0);i++)
					sbw.insert(x+1,"-"+sbw.charAt(x));
				break;
			}
		case 3:
			{
				int x=((Integer)V.elementAt(CMLib.dice().roll(1,V.size(),-1))).intValue();
				sbw.insert(x+1,"sh");
				break;
			}
		}
		return fixCase(word,sbw.toString());
	}
}
