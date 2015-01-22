package com.planet_ink.coffee_mud.core.interfaces;
import com.planet_ink.coffee_mud.core.interfaces.*;
import com.planet_ink.coffee_mud.core.*;
import java.util.*;

/*
CoffeeMUD 5.6.2 copyright 2000-2010 Bo Zimmerman
EspressoMUD copyright 2011 Kejardon

Licensed under the Apache License, Version 2.0. You may obtain a copy of the license at
	http://www.apache.org/licenses/LICENSE-2.0
*/

public interface Race extends StatsAffecting, ListenHolder.MsgListener, CMObject, EatCode//, DrinkCode	//Tickable?
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

	public Gender[] possibleGenders();
	//Typical height for the given gender
//	public int[] height(Gender g, int age);
//	public int[] weight(Gender g, int age);

//	public int[] getAgingChart();
	
	public void trainStat(Body body, CharStats.Stat stat, CMMsg message);
	
	public void recoverTick(Body body, CharStats stats);
	
	public ActionCode getAction(ActionCode.Type T);
	
	//Return expected nutritional value of material
	//Below -100 is 'completely unable to eat'.
	public int diet(Body body, RawMaterial.Resource material);
	//'body' consumes 'volume' amount of 'source'. This only affects body, source should be handled by caller (after this?). 'volume' is not necessarily (but usually) actual volume.
	//Should source be an Item or something else? EnvStats maybe?
	public void applyDiet(Body body, Item source, int volume);
	//How much (volume) of the source should be consumed per eat action. Returns negative if there is more to eat after this bite.
	public int getBiteSize(Body body, Item source);
	public int getMaxBiteSize(Body body);
	//ummmmm this is going to be complicated
	public HashMap<String, Body.BodyPart> bodyMap();
	// Chance of impregnation(0-100) when having sex with the given race. Can be negative- 0 means 'impossible naturally but possible with magic/science aid', -100 means 'completely impossible'
//	public int fertile(String race);
	//public String getStatAdjDesc();
	//public String getLanguagesDesc();
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
