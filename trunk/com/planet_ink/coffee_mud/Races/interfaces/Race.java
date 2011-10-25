package com.planet_ink.coffee_mud.Races.interfaces;
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
import java.util.*;

@SuppressWarnings("unchecked")
public interface Race extends Tickable, StatsAffecting, ListenHolder.MsgListener, CMObject
{
	/*
	public final static int AGE_INFANT=0;
	public final static int AGE_TODDLER=1;
	public final static int AGE_CHILD=2;
	public final static int AGE_YOUNGADULT=3;
	public final static int AGE_MATURE=4;
	public final static int AGE_MIDDLEAGED=5;
	public final static int AGE_OLD=6;
	public final static int AGE_VENERABLE=7;
	public final static int AGE_ANCIENT=8;
	public final static String[] AGE_DESCS={"Infant","Toddler","Child","Young adult","Adult", "Mature", "Old", "Venerable", "Ancient"};
	*/

	// Return a nice, displayable name for this race
	public String name();
	// Which racial category this race falls in.
	public String racialCategory();
	// -1 if not intended to have anywhere. 0 if available for NPCs. 1 if available for players and NPCs.
	public int availabilityCode();

	//Dimensional values will have 3 fields: Typical, maximum deviation, and degree of deviation.
	//Degree of deviation will be handled with CMath.curvedRandom(degree), so a randomly generated
	//value will be
	//Typical + (int)Math.round(deviation * CMath.curvedRandom(degree/100.0))

	//Typical and deviation will be measured in 1/100ths of inches, which will be the standard unit for the mud.
	//Degree will be an int for programming convenience, but converted to a double by dividing by 100. So a
	//degree of 100 will call curvedRandom for 1.

	//Typical height for the given gender
	public int[] height(char c);
	public int[] weight(char c);

//	public int[] getAgingChart();

	//ummmmm this is going to be complicated
	public HashMap<String, Body.BodyPart> bodyMap();
	
	// Chance of impregnation(0-100) when having sex with the given race.
	public int fertile(String race);
/*
	public String healthText(MOB viewer, MOB mob);

	public Weapon myNaturalWeapon();

	public Vector myResources();

	public DeadBody getCorpseContainer(MOB mob, Room room);
	public String arriveStr();
	public String leaveStr();
	public Vector racialEffects(MOB mob);
	public void agingAffects(MOB mob, CharStats baseStats, CharStats charStats);
*/
	/**
	 * Returns a list of the stat adjustments made by this race
	 * @return a list of the stat adjustments made by this race
	 */
	public String getStatAdjDesc();
	/**
	 * Returns the list of modifications to senses done by this race
	 * or nothing.
	 * @return the list of modifications to senses done by this race
	 */
	public String getSensesChgDesc();
	/**
	 * Returns the list of modifications to disposition done by this race
	 * or nothing.
	 * @return the list of modifications to disposition done by this race
	 */
	public String getDispositionChgDesc();
	/**
	 * Returns the list of racial abilities granted to those of this race
	 * or nothing.
	 * @return the list of racial abilities granted to those of this race
	 */
	public String getAbilitiesDesc();
	/**
	 * Returns the list of racial languages granted to those of this race
	 * or nothing.
	 * @return the list of racial languages granted to those of this race
	 */
	public String getLanguagesDesc();
/*
	public final static int BODY_ANTENEA=0;
	public final static int BODY_EYE=1;
	public final static int BODY_EAR=2;
	public final static int BODY_HEAD=3;
	public final static int BODY_NECK=4;
	public final static int BODY_ARM=5;
	public final static int BODY_HAND=6;
	public final static int BODY_TORSO=7;
	public final static int BODY_LEG=8;
	public final static int BODY_FOOT=9;
	public final static int BODY_NOSE=10;
	public final static int BODY_GILL=11;
	public final static int BODY_MOUTH=12;
	public final static int BODY_WAIST=13;
	public final static int BODY_TAIL=14;
	public final static int BODY_WING=15;
	public final static int BODY_PARTS=16;

	public final static Hashtable BODYPARTHASH=CMStrings.makeNumericHash(BODYPARTSTR);

	public final static long[] BODY_WEARVECTOR={
		Wearable.WORN_HEAD, // ANTENEA, having any of these removes that pos
		Wearable.WORN_EYES, // EYES, having any of these adds this position
		Wearable.WORN_EARS, // EARS, gains a wear position here for every 2
		Wearable.WORN_HEAD, // HEAD, gains a wear position here for every 1
		Wearable.WORN_NECK, // NECK, gains a wear position here for every 1
		Wearable.WORN_ARMS, // ARMS, gains a wear position here for every 2
		Wearable.WORN_HANDS, // HANDS, gains a wear position here for every 1
		Wearable.WORN_TORSO, // TORSO, gains a wear position here for every 1
		Wearable.WORN_LEGS, // LEGS, gains a wear position here for every 2
		Wearable.WORN_FEET, // FEET, gains a wear position here for every 2
		Wearable.WORN_HEAD, // NOSE, No applicable wear position for this body part
		Wearable.WORN_HEAD, // GILLS, No applicable wear position for this body part
		Wearable.WORN_MOUTH, // MOUTH, gains a wear position here for every 1
		Wearable.WORN_WAIST, // WAIST, gains a wear position here for every 1
		Wearable.WORN_BACK, // TAIL, having any of these removes that pos
		Wearable.WORN_BACK, // WINGS, having any of these removes that pos
	};
*/
}
