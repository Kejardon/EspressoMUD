package com.planet_ink.coffee_mud.Libraries.interfaces;
import com.planet_ink.coffee_mud.core.interfaces.*;
import com.planet_ink.coffee_mud.core.exceptions.*;
import com.planet_ink.coffee_mud.core.*;
import com.planet_ink.coffee_mud.Effects.interfaces.*;
import com.planet_ink.coffee_mud.Areas.interfaces.*;
import com.planet_ink.coffee_mud.Behaviors.interfaces.*;
import com.planet_ink.coffee_mud.Commands.interfaces.*;
import com.planet_ink.coffee_mud.Common.interfaces.*;
import com.planet_ink.coffee_mud.Exits.interfaces.*;
import com.planet_ink.coffee_mud.Items.interfaces.*;
import com.planet_ink.coffee_mud.Libraries.CMChannels;
import com.planet_ink.coffee_mud.Libraries.EnglishParser;
import com.planet_ink.coffee_mud.Locales.interfaces.*;
import com.planet_ink.coffee_mud.MOBS.interfaces.*;
import com.planet_ink.coffee_mud.Races.interfaces.*;

import java.io.IOException;
import java.util.*;
/* 
   Copyright 2000-2010 Bo Zimmerman

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

	   http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
*/
@SuppressWarnings("unchecked")
public interface EnglishParsing extends CMLibrary
{
	public static final int FLAG_STR=0;
	public static final int FLAG_DOT=1;
	public static final int FLAG_ALL=2;

	public boolean isAnArticle(String s);
	public String cleanArticles(String s);
	public String stripPunctuation(String str);
	public String insertUnColoredAdjective(String str, String adjective);
	public String startWithAorAn(String str);
	public Command findCommand(MOB mob, Vector commands);
	public boolean containsString(String toSrchStr, String srchStr);
	public boolean isCalled(Interactable thing, String name, boolean exact);
	public String bumpDotNumber(String srchStr);
	public Object[] fetchFlags(String srchStr);
	public CMObject fetchObject(Vector<? extends CMObject> list, String srchStr, boolean exactOnly);
//	public int fetchInteractableIndex(Vector<? extends Interactable> list, String srchStr, boolean exactOnly);
//	public Interactable fetchAvailable   (Vector<? extends Interactable> list, String srchStr, boolean exactOnly);
	public Interactable fetchInteractable(Vector<? extends Interactable> list, String srchStr, boolean exactOnly);
//	public Interactable fetchInteractable(Hashtable<String, Interactable> list, String srchStr, boolean exactOnly);
	public Interactable fetchInteractable(Interactable[] list, String srchStr, boolean exactOnly);
	public Interactable fetchInteractable(String srchStr, boolean exactOnly, int maxDepth, Object... list);
	public Vector<Interactable> fetchInteractables(String srchStr, boolean exactOnly, int maxDepth, int toFind, Object... list);
	public Vector<Interactable> fetchInteractables(Vector<? extends Interactable> list, String srchStr, boolean exactOnly);
//	public Item fetchAvailableItem(Vector list, String srchStr, boolean exactOnly);
//	public Vector fetchAvailableItems(Vector list, String srchStr, boolean exactOnly);
	public Vector fetchItemList(Interactable from, MOB mob, Item container, Vector commands, boolean visionMatters);
	public int getContextNumber(Object[] list, Interactable E);
	public int getContextNumber(Vector<? extends Interactable> list, Interactable E);
	public String getContextName(Vector<? extends Interactable> list, Interactable E);
	public String getContextName(Object[] list, Interactable E);
	public int getContextSameNumber(Object[] list, Interactable E);
	public int getContextSameNumber(Vector<? extends Interactable> list, Interactable E);
	public String getContextSameName(Vector<? extends Interactable> list, Interactable E);
	public String getContextSameName(Object[] list, Interactable E);
//	public long numPossibleGold(MOB mine, String itemID);
//	public String numPossibleGoldCurrency(Interactable mine, String itemID);
//	public double numPossibleGoldDenomination(Environmental mine, String currency, String itemID);
//	public Object[] parseMoneyStringSDL(MOB mob, String amount, String correctCurrency);
//	public String matchAnyCurrencySet(String itemID);
//	public double matchAnyDenomination(String currency, String itemID);
//	public Item possibleRoomGold(MOB seer, Room room, Item container, String itemID);
//	public Item bestPossibleGold(MOB mob, Container container, String itemID);
	public Vector possibleContainers(MOB mob, Vector commands, boolean withContentOnly);
	public Item possibleContainer(MOB mob, Vector commands, boolean withStuff);
	public String returnTime(long millis, long ticks);
	public int calculateMaxToGive(MOB mob, Vector commands, boolean breakPackages, Interactable checkWhat, boolean getOnly);
	public int getPartitionIndex(Vector<String> commands, String partitionName);
	public int getPartitionIndex(Vector<String> commands, String partitionName, int defaultTo);
	
/*
	public Interactable parseShopkeeper(MOB mob, Vector commands, String error);
	public boolean evokedBy(Ability thisAbility, String thisWord);
	public boolean evokedBy(Ability thisAbility, String thisWord, String secondWord);
	public String getAnEvokeWord(MOB mob, String word);
	public Ability getToEvoke(MOB mob, Vector commands);
	public boolean preEvoke(MOB mob, Vector commands, int secondsElapsed, double actionsRemaining);
	public void evoke(MOB mob, Vector commands);
*/
}
