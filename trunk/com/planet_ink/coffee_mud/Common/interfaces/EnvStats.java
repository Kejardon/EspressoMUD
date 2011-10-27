package com.planet_ink.coffee_mud.Common.interfaces;
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
/**
 * The EnvStats interface is a state object that holds some basic information about
 * just about every Environmental object in the game.  Not all stats are relevant for
 * ALL Environmentals, but most, especially the big ones.
 * 
 * Environmentals always keep two instances of this object, a base one, representing
 * his base unmodified state, and current one, representing his state after spells
 * and other affects have had their say.
 * @see com.planet_ink.coffee_mud.core.interfaces.CMModifiable
 * @see com.planet_ink.coffee_mud.core.interfaces.Environmental#baseEnvStats()
 * @see com.planet_ink.coffee_mud.core.interfaces.Environmental#envStats()
 */
public interface EnvStats extends CMCommon, CMModifiable, CMSavable
{
//	public int sensesMask(); // mobs, run-time items
//	public void setSensesMask(int newMask);
//	public int disposition(); // items, mobs
//	public void setDisposition(int newDisposition); // magic/bonus/whatever, see below?
//	public int level(); // items, exits, mobs
//	public void setLevel(int newLevel);
	public int ability(); // items, mobs
	public void setAbility(int newAdjustment);
//	public int rejuv(); // items, mobs
//	public void setRejuv(int newRejuv); // respawn time. usually
	public int weight(); // items, mobs
	public void setWeight(int newWeight);
	public int height(); // items, mobs
	public void setHeight(int newHeight);
	public int length(); // items, mobs
	public void setLength(int newHeight);
	public int width(); // items, mobs
	public void setWidth(int newHeight);
//	public int armor(); // armor items, mobs
//	public void setArmor(int newArmor);
//	public int damage(); // weapon items, mobs
//	public void setDamage(int newDamage);
	public double speed(); // mobs
	public void setSpeed(double newSpeed);
//	public int attackAdjustment(); // weapon items, mobs
//	public void setAttackAdjustment(int newAdjustment);
//	public String newName(); // items, mobs
//	public void setName(String newName);
	public String[] ambiances(); // everything
	public void addAmbiance(String ambiance); // extra display things
	public void delAmbiance(String ambiance);
//	public String getCombatStats();
//	public boolean sameAs(EnvStats E);
	public void copyInto(EnvStats intoStats);

/*
	public final static int STAT_LEVEL=0;
	public final static int STAT_SENSES=1;		 // see Senses class
	public final static int STAT_ARMOR=2;
	public final static int STAT_DAMAGE=3;
	public final static int STAT_ATTACK=4;
	public final static int STAT_DISPOSITION=5;		// see Senses class
	public final static int STAT_REJUV=6;
	public final static int STAT_WEIGHT=7;
	public final static int STAT_ABILITY=8;			// object dependant
	public final static int STAT_HEIGHT=9;
	public final static int NUM_STATS=10;
 
	public final static int CAN_NOT_SEE=1;
	public final static int CAN_SEE_HIDDEN=2;
	public final static int CAN_SEE_INVISIBLE=4;
	public final static int CAN_SEE_EVIL=8;
	public final static int CAN_SEE_GOOD=16;
	public final static int CAN_SEE_SNEAKERS=32;
	public final static int CAN_SEE_BONUS=64;
	public final static int CAN_SEE_DARK=128;
	public final static int CAN_SEE_INFRARED=256;
	public final static int CAN_NOT_HEAR=512;
	public final static int CAN_NOT_MOVE=1024;
	public final static int CAN_NOT_SMELL=2048;
	public final static int CAN_NOT_TASTE=4096;
	public final static int CAN_NOT_SPEAK=8192;
	public final static int CAN_NOT_BREATHE=16384;
	public final static int CAN_SEE_VICTIM=32768;
	public final static int CAN_SEE_METAL=65536;

	public final static int SENSE_UNLOCATABLE=1;
	public final static int SENSE_ITEMNOMINRANGE=2;
	public final static int SENSE_ITEMNOMAXRANGE=4;
	public final static int SENSE_ITEMREADABLE=8;
	public final static int SENSE_ITEMNOTGET=16;
	public final static int SENSE_ITEMNODROP=32;
	public final static int SENSE_ITEMNOREMOVE=64;
	public final static int SENSE_CONTENTSUNSEEN=128;
	public final static int SENSE_ITEMNEVERSAVED=256;
	public final static int SENSE_ROOMUNEXPLORABLE=512;
	public final static int SENSE_ROOMNOMOVEMENT=1024;
	public final static int SENSE_ROOMUNMAPPABLE=2048;
	public final static int SENSE_ROOMGRIDSYNC=4096;
	public final static int SENSE_ITEMNORUIN=8192;
	public final static int SENSE_UNUSEDMASK15=16384;
	public final static int SENSE_UNUSEDMASK16=32768;
	public final static int SENSE_UNUSEDMASK17=65536;

	public static final String[] SENSE_CODES={
		"UNLOCATABLE",
		"ITEMNOMINRANGE",
		"ITEMNOMAXRANGE",
		"ITEMREADABLE",
		"ITEMNOTGET",
		"ITEMNODROP",
		"ITEMNOREMOVE",
		"CONTENTSUNSEEN",
		"ITEMNEVERSAVED",
		"ROOMUNEXPLORABLE",
		"ROOMNOMOVEMENT",
		"ROOMUNMAPPABLE",
		"ROOMGRIDSYNC",
		"ITEMNORUIN",
		"UNUSEDMASK15",
		"UNUSEDMASK16",
		"UNUSEDMASK17",
	};

	public final static long ALLMASK=Integer.MAX_VALUE;

	public final static int IS_NOT_SEEN=1;
	public final static int IS_HIDDEN=2;
	public final static int IS_INVISIBLE=4;
	public final static int IS_EVIL=8;
	public final static int IS_GOOD=16;
	public final static int IS_SNEAKING=32;
	public final static int IS_BONUS=64;
	public final static int IS_DARK=128;
	public final static int IS_GOLEM=256;
	public final static int IS_SLEEPING=512;
	public final static int IS_SITTING=1024;
	public final static int IS_FLYING=2048;
	public final static int IS_SWIMMING=4096;
	public final static int IS_GLOWING=8192;
	public final static int IS_CLIMBING=16384;
	public final static int IS_FALLING=32768;
	public final static int IS_LIGHTSOURCE=65536;
	public final static int IS_BOUND=131072;
	public final static int IS_CLOAKED=262144;
	public final static int IS_UNSAVABLE=524288;
	public final static int IS_CATALOGED=1048576;
	public final static int IS_LAYINGDOWN=2097152;

	public static final String[] CAN_SEE_CODES={"CANNOTSEE",
											  "CANSEEHIDDEN",
											  "CANSEEINVISIBLE",
											  "CANSEEEVIL",
											  "CANSEEGOOD",
											  "CANSEESNEAKERS",
											  "CANSEEBONUS",
											  "CANSEEDARK",
											  "CANSEEINFRARED",
											  "CANNOTHEAR",
											  "CANNOTMOVE",
											  "CANNOTSMELL",
											  "CANNOTTASTE",
											  "CANNOTSPEAK",
											  "CANNOTBREATHE",
											  "CANSEEVICTIM",
											  "CANSEEMETAL"};

	public static final String[] CAN_SEE_DESCS={"Is Blind",
											 "Can see hidden",
											 "Can see invisible",
											 "Can see evil",
											 "Can see good",
											 "Can detect sneakers",
											 "Can see magic",
											 "Can see in the dark",
											 "Has infravision",
											 "Is Deaf",
											 "Is Paralyzed",
											 "Can not smell",
											 "Can not eat",
											 "Is Mute",
											 "Can not breathe",
											 "Can detect victims",
											 "Can detect metal"};

	public static final String[] CAN_SEE_VERBS={"Causes Blindness",
											 "Allows see hidden",
											 "Allows see invisible",
											 "Allows see evil",
											 "Allows see good",
											 "Allows detect sneakers",
											 "Allows see magic",
											 "Allows darkvision",
											 "Allows infravision",
											 "Causes Deafness",
											 "Causes Paralyzation",
											 "Deadens smell",
											 "Disallows eating",
											 "Causes Mutemess",
											 "Causes choking",
											 "Allows detect victims",
											 "Allows detect metal"};

	public static final String[] IS_CODES={"ISSEEN",
											"ISHIDDEN",
											"ISINVISIBLE",
											"ISEVIL",
											"ISGOOD",
											"ISSNEAKING",
											"ISBONUS",
											"ISDARK",
											"ISGOLEM",
											"ISSLEEPING",
											"ISSITTING",
											"ISFLYING",
											"ISSWIMMING",
											"ISGLOWING",
											"ISCLIMBING",
											"ISFALLING",
											"ISLIGHT",
											"ISBOUND",
											"ISCLOAKED",
											"ISUNSAVABLE",
											"ISCATALOGED",
											"ISLAYINGDOWN"};

	public static final String[] IS_DESCS= {"Is never seen",
											"Is hidden",
											"Is invisible",
											"Evil aura",
											"Good aura",
											"Is sneaking",
											"Is magical",
											"Is dark",
											"Is golem",
											"Is sleeping",
											"Is sitting",
											"Is flying",
											"Is swimming",
											"Is glowing",
											"Is climbing",
											"Is falling",
											"Is a light source",
											"Is binding",
											"Is Cloaked",
											"Is never saved",
											"Is cataloged",
											"Is laying down"};

	public static final String[] IS_VERBS= {"Causes Nondetectability",
											"Causes hide",
											"Causes invisibility",
											"Creates Evil aura",
											"Creates Good aura",
											"Causes sneaking",
											"Creates magical aura",
											"Creates dark aura",
											"Creates golem aura",
											"Causes sleeping",
											"Causes sitting",
											"Allows flying",
											"Causes swimming",
											"Causes glowing aura",
											"Allows climbing",
											"Causes falling",
											"Causes a light source",
											"Causes binding",
											"Causes cloaking",
											"Causes disappearance",
											"Causes unsavability",
											"Created from a template",
											"Causes laying down"};
*/
}