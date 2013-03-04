package com.planet_ink.coffee_mud.Libraries.interfaces;
import com.planet_ink.coffee_mud.core.interfaces.*;
import com.planet_ink.coffee_mud.core.*;
import com.planet_ink.coffee_mud.core.exceptions.*;
import com.planet_ink.coffee_mud.Libraries.CMChannels;
import com.planet_ink.coffee_mud.Libraries.EnglishParser;

import java.util.*;
import java.nio.ByteBuffer;
import java.util.concurrent.CopyOnWriteArrayList;
import java.io.IOException;

/*
CoffeeMUD 5.6.2 copyright 2000-2010 Bo Zimmerman
EspressoMUD copyright 2011 Kejardon

Licensed under the Apache License, Version 2.0. You may obtain a copy of the license at
	http://www.apache.org/licenses/LICENSE-2.0
*/
@SuppressWarnings("unchecked")
public interface EnglishParsing extends CMLibrary
{
	//public static final int SRCH_ONLY=1;	//Only one other bit should be set if this one is set
	public static final int SRCH_MOBINV=1;	//'held' items
	public static final int SRCH_MOBEQ=SRCH_MOBINV*2;	//Items worn on body
	public static final int SRCH_MOB=SRCH_MOBEQ*2;	//Body parts
	public static final int SRCH_ROOM=SRCH_MOB*2;	//Any item lying in the room.
	public static final int SRCH_ALL=SRCH_ROOM|SRCH_MOBINV|SRCH_MOBEQ|SRCH_MOB;

	public static final int SUB_ITEMCOLL=1;	//Search items stored inside
	public static final int SUB_RIDERS=SUB_ITEMCOLL*2;	//Search items stored on top
	public static final int SUB_ALL=SUB_ITEMCOLL|SUB_RIDERS;

	//public static final int FLAG_STR=0;
	//public static final int FLAG_DOT=1;
	//public static final int FLAG_ALL=2;
	public static class StringFlags
	{
		public boolean allFlag;
		public boolean myFlag;
		public String srchStr;
		public int occurrance=1;
		public int toFind=1;
	}

	public boolean isAnArticle(String s);
	public String cleanArticles(String s);
	public String stripPunctuation(String str);
	public String insertUnColoredAdjective(String str, String adjective);
	public String startWithAorAn(String str);
	public Command findCommand(MOB mob, String commands);
	public Command findCommand(MOB mob, String commands, String firstWord);
	public boolean containsString(String toSrchStr, String srchStr);
	public boolean isCalled(Interactable thing, String name, boolean exact);
	public StringFlags fetchFlags(String srchStr);
	public CMObject fetchObject(Vector<? extends CMObject> list, String srchStr, boolean exactOnly);
	public Interactable fetchInteractable(Vector<? extends Interactable> list, String srchStr, boolean exactOnly);
	public Interactable fetchInteractable(Iterator<? extends Interactable> list, String srchStr, boolean exactOnly);
	public Interactable fetchInteractable(Interactable[] list, String srchStr, boolean exactOnly);
	public Interactable fetchInteractable(String srchStr, boolean exactOnly, int maxDepth, Object... list);
	public Vector<Interactable> fetchInteractables(String srchStr, boolean exactOnly, int maxDepth, int toFind, Object... list);
	public Vector<Interactable> fetchInteractables(Vector<? extends Interactable> list, String srchStr, boolean exactOnly);
	public Vector<Interactable> fetchInteractables(Iterator<? extends Interactable> list, String srchStr, boolean exactOnly);
	public String returnTime(long millis, long ticks);
	public int calculateMaxToGive(MOB mob, Vector<String> commands, Interactable checkWhat, boolean getOnly);
	public Vector<Interactable> getTargets(MOB mob, String commands, String partitioner, int searchFlags, int subObjectFlags);
	public Vector<Interactable> getTargets(MOB mob, Vector<String> commands, String partitioner, int searchFlags, int subObjectFlags);
	public int getPartitionIndex(Vector<String> commands, String partitionName);
	public int getPartitionIndex(Vector<String> commands, String partitionName, int defaultTo);

}
