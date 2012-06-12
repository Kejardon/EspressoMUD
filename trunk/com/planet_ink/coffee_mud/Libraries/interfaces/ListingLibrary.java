package com.planet_ink.coffee_mud.Libraries.interfaces;
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
import java.nio.ByteBuffer;
import java.util.concurrent.CopyOnWriteArrayList;

/*
CoffeeMUD 5.6.2 copyright 2000-2010 Bo Zimmerman
EspressoMUD copyright 2011 Kejardon

Licensed under the Apache License, Version 2.0. You may obtain a copy of the license at
	http://www.apache.org/licenses/LICENSE-2.0
*/
@SuppressWarnings("unchecked")
public interface ListingLibrary extends CMLibrary
{
	public String itemSeenString(MOB viewer, Interactable item, boolean useName);	//boolean longLook, boolean sysMsgs
	public int getReps(Item item, ArrayList<Item> theRest, MOB mob, boolean useName);	//boolean longLook
	public void appendReps(int reps, StringBuilder say);
	public StringBuilder lister(MOB mob, ArrayList<Item> things, boolean useName, String tag, String tagParm, boolean longLook);
	//public StringBuilder reallyList(Hashtable these, int ofType);
	public StringBuilder reallyList(Hashtable these);
	public StringBuilder reallyList(Hashtable these, Room likeRoom);
	//public StringBuilder reallyList(Vector these, int ofType);
	//public StringBuilder reallyList(Enumeration these, int ofType);
	public StringBuilder reallyList(Vector these);
	public StringBuilder reallyList(Enumeration these);
	public StringBuilder reallyList(Iterator these);
	public StringBuilder reallyList(Vector these, Room likeRoom);
	//public StringBuilder reallyList(Hashtable these, int ofType, Room likeRoom);
	//public StringBuilder reallyList(Vector these, int ofType, Room likeRoom);
	public StringBuilder reallyList(Enumeration these, Room likeRoom);
	//public StringBuilder reallyList(Enumeration these, int ofType, Room likeRoom);
	public StringBuilder reallyList2Cols(Enumeration these, Room likeRoom);
	public StringBuilder reallyList2Cols(Iterator these, Room likeRoom);
	public StringBuilder fourColumns(Vector<String> reverseList);
	public StringBuilder fourColumns(Vector<String> reverseList, String tag);
}