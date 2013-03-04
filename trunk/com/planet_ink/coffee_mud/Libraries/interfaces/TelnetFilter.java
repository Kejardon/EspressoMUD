package com.planet_ink.coffee_mud.Libraries.interfaces;
import com.planet_ink.coffee_mud.core.interfaces.*;
import com.planet_ink.coffee_mud.core.*;
import com.planet_ink.coffee_mud.Libraries.Sense;

import java.util.*;
import java.nio.ByteBuffer;
import java.util.concurrent.CopyOnWriteArrayList;

/*
CoffeeMUD 5.6.2 copyright 2000-2010 Bo Zimmerman
EspressoMUD copyright 2011 Kejardon

Licensed under the Apache License, Version 2.0. You may obtain a copy of the license at
	http://www.apache.org/licenses/LICENSE-2.0
*/
@SuppressWarnings("unchecked")
public interface TelnetFilter extends CMLibrary
{
	public final static String hexStr="0123456789ABCDEF";
	public final static int HISHER=0;
	public final static int HIMHER=HISHER+1;
	public final static int NAME=HIMHER+1;
	public final static int NAMESELF=NAME+1;
	public final static int HESHE=NAMESELF+1;
	public final static int ISARE=HESHE+1;
	public final static int HASHAVE=ISARE+1;
	public final static int YOUPOSS=HASHAVE+1;
	public final static int HIMHERSELF=YOUPOSS+1;
	public final static int HISHERSELF=HIMHERSELF+1;
	public final static int SIRMADAM=HISHERSELF+1;
	public final static int NAMENOART=SIRMADAM+1;
	public final static String[] FILTER_DESCS={"-HIS-HER","-HIM-HER","-NAME","-NAMESELF",
											   "-HE-SHE","-IS-ARE","-HAS-HAVE","-YOUPOSS",
											   "-HIM-HERSELF","-HIS-HERSELF",
											   "-SIRMADAM","-NAMENOART"};
	
	public Hashtable<String, Integer> getTagTable();
	public String toRawString(String str);
	//public String simpleOutFilter(String msg);
	// no word-wrapping, text filtering or ('\','n') -> '\n' translations
	// (it's not a member of the interface either so probably shouldn't be public)
	public String colorOnlyFilter(String msg, Session S);
	public String[] wrapOnlyFilter(String msg, int wrap);
	public String getLastWord(StringBuilder buf, int lastSp, int lastSpace);
	public String fullOutFilter(Session S,
								MOB mob,
								Interactable source,
								Interactable target,
								CMObject tool,
								String msg,
								boolean wrapOnly);
	//public StringBuffer simpleInFilter(StringBuffer input, boolean allowMXP);
	//public String fullInFilter(String input, boolean allowMXP);
	//public String safetyFilter(String s);
}