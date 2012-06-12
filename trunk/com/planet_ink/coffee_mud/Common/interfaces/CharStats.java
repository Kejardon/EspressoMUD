package com.planet_ink.coffee_mud.Common.interfaces;
import java.util.*;

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

/*
CoffeeMUD 5.6.2 copyright 2000-2010 Bo Zimmerman
EspressoMUD copyright 2011 Kejardon

Licensed under the Apache License, Version 2.0. You may obtain a copy of the license at
	http://www.apache.org/licenses/LICENSE-2.0
*/
// A container for stats related to a variaety of animated objects.
public interface CharStats extends CMCommon, CMModifiable, CMSavable, Ownable
{
	/*
	public static final int STAT_STRENGTH=0;
	public static final int STAT_INTELLIGENCE=STAT_STRENGTH+1;
	public static final int STAT_DEXTERITY=STAT_INTELLIGENCE+1;
	public static final int STAT_CONSTITUTION=STAT_DEXTERITY+1;
	public static final int STAT_CHARISMA=STAT_CONSTITUTION+1;
	public static final int STAT_WISDOM=STAT_CHARISMA+1;
	public static final int DEFAULT_NUM_BASE_STATS=STAT_WISDOM+1;
	*/
	public static enum Stat
	{
		CONSTITUTION("Constitution", "constitution", "Con"),
		REACTIONS("Reactions", "reactions", "Rct"),
		INTELLIGENCE("Intelligence", "intelligence", "Int"),
		STRENGTH("Strength", "strength", "Str"),
		PRECISION("Precision", "precision", "Pre"),
		OBSERVATION("Observation", "observation", "Obs"),
		WILLPOWER("Willpower", "willpower", "Wil"),
		;
		public final String friendlyName;
		public final String shortName;
		public final String lowerName;
		private Stat(String S1, String S2, String S3){friendlyName=S1; lowerName=S2; shortName=S3;}
	}
/*
	public static final int STAT_SAVE_PARALYSIS=0;
	public static final int STAT_SAVE_FIRE=STAT_SAVE_PARALYSIS+1;
	public static final int STAT_SAVE_COLD=STAT_SAVE_FIRE+1;
	public static final int STAT_SAVE_WATER=STAT_SAVE_COLD+1;
	public static final int STAT_SAVE_GAS=STAT_SAVE_WATER+1;
	public static final int STAT_SAVE_MIND=STAT_SAVE_GAS+1;
	public static final int STAT_SAVE_GENERAL=STAT_SAVE_MIND+1;
	public static final int STAT_SAVE_JUSTICE=STAT_SAVE_GENERAL+1;
	public static final int STAT_SAVE_ACID=STAT_SAVE_JUSTICE+1;
	public static final int STAT_SAVE_ELECTRIC=STAT_SAVE_ACID+1;
	public static final int STAT_SAVE_POISON=STAT_SAVE_ELECTRIC+1;
	public static final int STAT_SAVE_UNDEAD=STAT_SAVE_POISON+1;
	public static final int STAT_SAVE_MAGIC=STAT_SAVE_UNDEAD+1;
	public static final int STAT_SAVE_DISEASE=STAT_SAVE_MAGIC+1;
	public static final int STAT_SAVE_TRAPS=STAT_SAVE_DISEASE+1;
	public static final int STAT_SAVE_DETECTION=STAT_SAVE_TRAPS+1;
	public static final int STAT_SAVE_OVERLOOKING=STAT_SAVE_DETECTION+1;
	public static final int DEFAULT_NUM_SAVES=STAT_SAVE_OVERLOOKING+1;
*/
	public static enum Save
	{
		PARALYSIS, FIRE, COLD, WATER, GAS, MIND, GENERAL, JUSTICE, ACID, ELECTRIC,
		POISON, UNDEAD, MAGIC, DISEASE, TRAPS, DETECTION, OVERLOOKING;
	}
/*
	public final static int STAT_HITPOINTS=0;
	public final static int STAT_MANA=STAT_HITPOINTS+1;
	public final static int STAT_MOVE=STAT_MANA+1;
	public final static int STAT_HUNGER=STAT_MOVE+1;
	public final static int STAT_THIRST=STAT_HUNGER+1;
	public final static int STAT_NUMSTATS=STAT_THIRST+1;
*/
	public static enum Points
	{
		HIT, MANA, FOCUS, FATIGUE, HUNGER, THIRST;
	}

	public static final int VALUE_ALLSTATS_DEFAULT=10;
	public static final int VALUE_ALLSTATS_DEFAULTMAX=18;
	
//	public CharStats setBody(Body newBody);
	/**
	 * Copies the internal data of this object into another of kind.
	 * @param intoStats another CharStats object.
	 */
	public void copyInto(CharStats intoStats);
	public void copyStatic(CharStats intoStats);

	public short getStat(Stat option);
//	public int getMaxStat(Stat option);

	public void setStat(Stat option, short value);
//	public void setMaxStat(Stat option, int value);

	public short getSave(Save option);
	public void setSave(Save option, short value);
/*	ALL THIS SHOULD BE HANDLED BY BODY OBJECTS, NOT CHARSTATS
	// See Race.java interface for available part numbers
	public int getBodyPart(int racialPartNumber);
	public void alterBodypart(int racialPartNumber, int number);

	// Race will be more complicated to properly handle hybrids. Hm. Todo later for now.
	public Race getMyRace();
	public void setMyRace(Race newVal);

	public String raceName();
	public void setRaceName(String newRaceName);

	// Will need SOMETHING like this but I'm not convinced it will be this :|
	public long getWearableRestrictionsBitmap();
	public void setWearableRestrictionsBitmap(long bitmap);

	public char gender();
	public void setGender(char newGen);
	public void setGenderName(String gname);
	public String genderName();
	public String himher();
	public String hisher();
	public String heshe();
	public String HeShe();
	public String sirmadam();
	public String SirMadam();

	public int ageCategory();
	public int age();
	public void setAge(int newAge);

	public String ageName();
*/
	/** constant for how many fatigue points are lost per tick of rest */
//	public final static long REST_PER_TICK=Tickable.TIME_TICK*200;
	/** constant for how many fatigue points are required to be considered fatigued */
//	public final static long FATIGUED_MILLIS=Tickable.TIME_TICK*3000;
	/** constant for how many fatigue points are required to be considered exhausted */
//	public final static long FATIGUED_EXHAUSTED_MILLIS=FATIGUED_MILLIS*10;

	public int getPoints(Points option);
	public double getPointsPercent(Points option);
	public boolean setPoints(Points option, int newVal);	//Return if it broke a min or max cap, do not cap yourself
	public boolean adjPoints(Points option, int byThisMuch);	//Cap, return if cap did something

	public int getMaxPoints(Points option);
	public boolean setMaxPoints(Points option, int newVal);	//Return if reduced below current, do not cap yourself
	public boolean adjMaxPoints(Points option, int byThisMuch);	//Cap, return if cap did something

	public void resetState();

	public void recoverTick(Body body);

//	public void expendEnergy(MOB mob, boolean expendMovement);

	public boolean sameAs(CharStats E);
}
