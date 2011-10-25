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
import com.planet_ink.coffee_mud.Libraries.MUDZapper;
import com.planet_ink.coffee_mud.Locales.interfaces.*;
import com.planet_ink.coffee_mud.MOBS.interfaces.*;
import com.planet_ink.coffee_mud.Races.interfaces.*;

import java.io.ByteArrayInputStream;
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
public interface MaskingLibrary extends CMLibrary
{
	public String rawMaskHelp();
	public String maskHelp(String CR, String word);
	public String maskDesc(String text);
	public String maskDesc(String text, boolean skipFirstWord);
	public Vector maskCompile(String text);
	public boolean maskCheck(Vector cset, Interactable E, boolean actual);
	public boolean maskCheck(String text, Interactable E, boolean actual);
	public boolean syntaxCheck(String text, Vector errorSink);

	public final String DEFAULT_MASK_HELP =
		"+SYSOP (allow archons to bypass the rules)  <BR>"
		+"-SYSOP (always <WORD> archons)  <BR>"
		+"+SUBOP (allow archons or area staff to bypass the rules)  <BR>"
		+"-SUBOP (always <WORD> archons and area staff)  <BR>"
		+"-PLAYER (<WORD> all players) <BR>"
		+"-MOB (<WORD> all mobs/npcs)  <BR>"
		+"-thief -mage  -ranger (<WORD> only listed classes)<BR>"
		+"-RACE (<WORD> all races)  <BR>"
		+"-RACE +elf +dwarf +human +half +gnome (create exceptions)  <BR>"
		+"-elf -dwarf -human -half -gnome (<WORD> only listed races)  <BR>"
		+"-RACECAT (<WORD> all racial categories)  <BR>"
		+"-RACECAT +elf +insect +humanoid +canine +gnome (create exceptions)  <BR>"
		+"+RACECAT (do not <WORD> all racial categories)  <BR>"
		+"+RACECAT -elf -insect -humanoid -canine -gnome (create exceptions)  <BR>"
		+"-GENDER (<WORD> all genders)  <BR>"
		+"-GENDER +male +female +neuter (create exceptions)  <BR>"
		+"-male -female -neuter (<WORD> only listed genders)  <BR>"
		+"-SECURITY (<WORD> all security flags, even a lack of a security) <BR>"
		+"-SECURITY +cmdrooms +area cmditems etc..  (create exceptions)<BR>"
		+"+SECURITY (do not <WORD> any or no expertises) <BR>"
		+"+SECURITY -cmdrooms +area cmditems, etc.. (create exceptions) <BR>"
		+"-NAMES (<WORD> everyone) <BR>"
		+"-NAMES +bob \"+my name\" etc.. (create name exceptions) <BR>"
		+"+NAMES (do not <WORD> anyone who has a name) <BR>"
		+"+NAMES -bob \"-my name\" etc.. (create name exceptions) <BR>"
		+"-STR X (<WORD> those with strength greater than X)  <BR>"
		+"+STR X (<WORD> those with strength less than X)  <BR>"
		+"-INT X (<WORD> those with intelligence greater than X)  <BR>"
		+"+INT X (<WORD> those with intelligence less than X)  <BR>"
		+"-WIS X (<WORD> those with wisdom greater than X)  <BR>"
		+"+WIS X (<WORD> those with wisdom less than X)  <BR>"
		+"-CON X (<WORD> those with constitution greater than X)  <BR>"
		+"+CON X (<WORD> those with constitution less than X)  <BR>"
		+"-CHA X (<WORD> those with charisma greater than X)  <BR>"
		+"+CHA X (<WORD> those with charisma less than X)  <BR>"
		+"-DEX X (<WORD> those with dexterity greater than X)  <BR>"
		+"+DEX X (<WORD> those with dexterity less than X) <BR>"
		+"+-ADJSTR...ADJCON (Same as above, but uses current values) <BR>"
		+"-AREA (<WORD> in all areas) <BR>"
		+"-AREA \"+my areaname\" etc.. (create exceptions) <BR>"
		+"+AREA (do not <WORD> any areas) <BR>"
		+"+AREA \"-my areaname\" etc.. (create exceptions) <BR>"
		+"-HOME (<WORD> in all home/beacon areas) <BR>"
		+"-HOME \"+my home/beacon areaname\" etc.. (create exceptions) <BR>"
		+"+HOME (do not <WORD> any home/beacon areas) <BR>"
		+"+HOME \"-my home/beacon areaname\" etc.. (create exceptions) <BR>"
		+"-ITEM \"+item name\" etc... (<WORD> only those with an item name) <BR>"
		+"-WORN \"+item name\" etc... (<WORD> only those wearing item name) <BR>"
		+"-EFFECTS (<WORD> anyone, even no effects) <BR>"
		+"-EFFECTS +Sleep \"+Wood Chopping\" etc.. (create name exceptions) <BR>"
		+"+EFFECTS (do not <WORD> anyone, even non effected people) <BR>"
		+"+EFFECTS -Sleep \"-Wood Chopping\" etc.. (create name exceptions) <BR>"
		+"-MATERIAL \"+WOODEN\" etc.. (<WORD> only items of added materials) <BR>"
		+"+MATERIAL \"-WOODEN\" etc.. (Do not <WORD> items of -materials) <BR>"
		+"-RESOURCES \"+OAK\" etc.. (<WORD> only items of added resources) <BR>"
		+"+RESOURCES \"-OAK\" etc.. (Do not <WORD> items of -resources) <BR>"
		+"-JAVACLASS \"+GENMOB\" etc.. (<WORD> only objects of +java class) <BR>"
		+"+JAVACLASS \"-GENITEM\" etc.. (Do not <WORD> objs of -classes) <BR>"
		+"-VALUE X (<WORD> those with value or money less than X)  <BR>"
		+"+VALUE X (<WORD> those with value or money greater than X) <BR>"
		+"-WEIGHT X (<WORD> those weighing less than X)  <BR>"
		+"+WEIGHT X (<WORD> those weighing more than X) <BR>"
		+"-ARMOR X (<WORD> those with armor bonus less than X)  <BR>"
		+"+ARMOR X (<WORD> those with armor bonus more than X) <BR>"
		+"-DAMAGE X (<WORD> those with damage bonus less than X)  <BR>"
		+"+DAMAGE X (<WORD> those with damage bonus more than X) <BR>"
		+"-ATTACK X (<WORD> those with attack bonus less than X)  <BR>"
		+"+ATTACK X (<WORD> those with attack bonus more than X) <BR>"
		+"-WORNON \"+TORSO\" etc.. (<WORD> only items wearable on +locations) <BR>"
		+"+WORNON \"-NECK\" etc.. (Do not <WORD> items wearable on -locations) <BR>"
		+"-DISPOSITION \"+ISHIDDEN\" etc.. (<WORD> only with +dispositions) <BR>"
		+"+DISPOSITION \"-ISHIDDEN\" etc.. (Do not <WORD> only with sub disp) <BR>"
		+"-SENSES \"+CANSEEDARK\" etc.. (<WORD> only those with +senses) <BR>"
		+"+SENSES \"-CANSEEDARK\" etc.. (Do not <WORD> those with -senses) <BR>"
		+"-HOUR +X (<WORD> always, unless the hour is X)  <BR>"
		+"+HOUR -X (<WORD> those only when the hour is X) <BR>"
		+"-SEASON +FALL (<WORD> those only when season is FALL)  <BR>"
		+"+SEASON -SPRING (<WORD> those whenever the season is SPRING) <BR>"
		+"-MONTH +X (<WORD> those only when month number is X)  <BR>"
		+"+MONTH -X (<WORD> those whenever the month number is X) <BR>"
		+"-DAY +X (<WORD> those only when day number is X)  <BR>"
		+"+DAY -X (<WORD> those whenever the day number is X)  <BR>"
		+"-WEATHER +DROUGHT (<WORD> those only when weather is DROUGHT)  <BR>"
		+"+WEATHER -BLIZZARD (<WORD> those whenever the weather is BLIZZARD) <BR>"
		+"-CHANCE 50 (<WORD> the given % of the time)";
}
