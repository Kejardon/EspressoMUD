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

import com.planet_ink.coffee_mud.Libraries.MUDZapper;

import java.util.*;
import java.nio.ByteBuffer;
import java.util.concurrent.CopyOnWriteArrayList;
import java.io.ByteArrayInputStream;
import java.io.IOException;

/*
CoffeeMUD 5.6.2 copyright 2000-2010 Bo Zimmerman
EspressoMUD copyright 2011 Kejardon

Licensed under the Apache License, Version 2.0. You may obtain a copy of the license at
	http://www.apache.org/licenses/LICENSE-2.0
*/
@SuppressWarnings("unchecked")
public interface MaskingLibrary extends CMLibrary
{
	public String rawMaskHelp();
	//public String maskHelp(String CR, String word);
	public String maskDesc(String text);
	public String maskDesc(String text, boolean skipFirstWord);
	public Vector maskCompile(String text);
	public boolean maskCheck(Vector cset, Interactable E, boolean actual);
	public boolean maskCheck(String text, Interactable E, boolean actual);
	public boolean syntaxCheck(String text, Vector errorSink);

	public final String DEFAULT_MASK_HELP =
		"+SYSOP (allow archons to bypass the rules)  \r\n"
		+"-SYSOP (always <WORD> archons)  \r\n"
		+"+SUBOP (allow archons or area staff to bypass the rules)  \r\n"
		+"-SUBOP (always <WORD> archons and area staff)  \r\n"
		+"-PLAYER (<WORD> all players) \r\n"
		+"-MOB (<WORD> all mobs/npcs)  \r\n"
		+"-thief -mage  -ranger (<WORD> only listed classes)\r\n"
		+"-RACE (<WORD> all races)  \r\n"
		+"-RACE +elf +dwarf +human +half +gnome (create exceptions)  \r\n"
		+"-elf -dwarf -human -half -gnome (<WORD> only listed races)  \r\n"
		+"-RACECAT (<WORD> all racial categories)  \r\n"
		+"-RACECAT +elf +insect +humanoid +canine +gnome (create exceptions)  \r\n"
		+"+RACECAT (do not <WORD> all racial categories)  \r\n"
		+"+RACECAT -elf -insect -humanoid -canine -gnome (create exceptions)  \r\n"
		+"-GENDER (<WORD> all genders)  \r\n"
		+"-GENDER +male +female +neuter (create exceptions)  \r\n"
		+"-male -female -neuter (<WORD> only listed genders)  \r\n"
		+"-SECURITY (<WORD> all security flags, even a lack of a security) \r\n"
		+"-SECURITY +cmdrooms +area cmditems etc..  (create exceptions)\r\n"
		+"+SECURITY (do not <WORD> any or no expertises) \r\n"
		+"+SECURITY -cmdrooms +area cmditems, etc.. (create exceptions) \r\n"
		+"-NAMES (<WORD> everyone) \r\n"
		+"-NAMES +bob \"+my name\" etc.. (create name exceptions) \r\n"
		+"+NAMES (do not <WORD> anyone who has a name) \r\n"
		+"+NAMES -bob \"-my name\" etc.. (create name exceptions) \r\n"
		+"-STR X (<WORD> those with strength greater than X)  \r\n"
		+"+STR X (<WORD> those with strength less than X)  \r\n"
		+"-INT X (<WORD> those with intelligence greater than X)  \r\n"
		+"+INT X (<WORD> those with intelligence less than X)  \r\n"
		+"-WIS X (<WORD> those with wisdom greater than X)  \r\n"
		+"+WIS X (<WORD> those with wisdom less than X)  \r\n"
		+"-CON X (<WORD> those with constitution greater than X)  \r\n"
		+"+CON X (<WORD> those with constitution less than X)  \r\n"
		+"-CHA X (<WORD> those with charisma greater than X)  \r\n"
		+"+CHA X (<WORD> those with charisma less than X)  \r\n"
		+"-DEX X (<WORD> those with dexterity greater than X)  \r\n"
		+"+DEX X (<WORD> those with dexterity less than X) \r\n"
		+"+-ADJSTR...ADJCON (Same as above, but uses current values) \r\n"
		+"-AREA (<WORD> in all areas) \r\n"
		+"-AREA \"+my areaname\" etc.. (create exceptions) \r\n"
		+"+AREA (do not <WORD> any areas) \r\n"
		+"+AREA \"-my areaname\" etc.. (create exceptions) \r\n"
		+"-HOME (<WORD> in all home/beacon areas) \r\n"
		+"-HOME \"+my home/beacon areaname\" etc.. (create exceptions) \r\n"
		+"+HOME (do not <WORD> any home/beacon areas) \r\n"
		+"+HOME \"-my home/beacon areaname\" etc.. (create exceptions) \r\n"
		+"-ITEM \"+item name\" etc... (<WORD> only those with an item name) \r\n"
		+"-WORN \"+item name\" etc... (<WORD> only those wearing item name) \r\n"
		+"-EFFECTS (<WORD> anyone, even no effects) \r\n"
		+"-EFFECTS +Sleep \"+Wood Chopping\" etc.. (create name exceptions) \r\n"
		+"+EFFECTS (do not <WORD> anyone, even non effected people) \r\n"
		+"+EFFECTS -Sleep \"-Wood Chopping\" etc.. (create name exceptions) \r\n"
		+"-MATERIAL \"+WOODEN\" etc.. (<WORD> only items of added materials) \r\n"
		+"+MATERIAL \"-WOODEN\" etc.. (Do not <WORD> items of -materials) \r\n"
		+"-RESOURCES \"+OAK\" etc.. (<WORD> only items of added resources) \r\n"
		+"+RESOURCES \"-OAK\" etc.. (Do not <WORD> items of -resources) \r\n"
		+"-JAVACLASS \"+GENMOB\" etc.. (<WORD> only objects of +java class) \r\n"
		+"+JAVACLASS \"-GENITEM\" etc.. (Do not <WORD> objs of -classes) \r\n"
		+"-VALUE X (<WORD> those with value or money less than X)  \r\n"
		+"+VALUE X (<WORD> those with value or money greater than X) \r\n"
		+"-WEIGHT X (<WORD> those weighing less than X)  \r\n"
		+"+WEIGHT X (<WORD> those weighing more than X) \r\n"
		+"-ARMOR X (<WORD> those with armor bonus less than X)  \r\n"
		+"+ARMOR X (<WORD> those with armor bonus more than X) \r\n"
		+"-DAMAGE X (<WORD> those with damage bonus less than X)  \r\n"
		+"+DAMAGE X (<WORD> those with damage bonus more than X) \r\n"
		+"-ATTACK X (<WORD> those with attack bonus less than X)  \r\n"
		+"+ATTACK X (<WORD> those with attack bonus more than X) \r\n"
		+"-WORNON \"+TORSO\" etc.. (<WORD> only items wearable on +locations) \r\n"
		+"+WORNON \"-NECK\" etc.. (Do not <WORD> items wearable on -locations) \r\n"
		+"-DISPOSITION \"+ISHIDDEN\" etc.. (<WORD> only with +dispositions) \r\n"
		+"+DISPOSITION \"-ISHIDDEN\" etc.. (Do not <WORD> only with sub disp) \r\n"
		+"-SENSES \"+CANSEEDARK\" etc.. (<WORD> only those with +senses) \r\n"
		+"+SENSES \"-CANSEEDARK\" etc.. (Do not <WORD> those with -senses) \r\n"
		+"-HOUR +X (<WORD> always, unless the hour is X)  \r\n"
		+"+HOUR -X (<WORD> those only when the hour is X) \r\n"
		+"-SEASON +FALL (<WORD> those only when season is FALL)  \r\n"
		+"+SEASON -SPRING (<WORD> those whenever the season is SPRING) \r\n"
		+"-MONTH +X (<WORD> those only when month number is X)  \r\n"
		+"+MONTH -X (<WORD> those whenever the month number is X) \r\n"
		+"-DAY +X (<WORD> those only when day number is X)  \r\n"
		+"+DAY -X (<WORD> those whenever the day number is X)  \r\n"
		+"-WEATHER +DROUGHT (<WORD> those only when weather is DROUGHT)  \r\n"
		+"+WEATHER -BLIZZARD (<WORD> those whenever the weather is BLIZZARD) \r\n"
		+"-CHANCE 50 (<WORD> the given % of the time)";
}
