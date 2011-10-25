package com.planet_ink.coffee_mud.Commands;
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
public class Score extends Affect
{
	public Score(){}

	private String[] access={"SCORE","SC"};
	public String[] getAccessWords(){return access;}

	public StringBuilder getScore(MOB mob){return getScore(mob,"");}
	public StringBuilder getScore(MOB mob, String parm)
	{
		StringBuilder msg=new StringBuilder("^N");

		msg.append("You are ^H"+mob.Name()+"^?.\n\r");

		if(CMProps.getBoolVar(CMProps.SYSTEMB_ACCOUNTEXPIRATION)&&(mob.playerStats()!=null))
			msg.append("Your account is Registered and Active until: "+CMLib.time().date2String(mob.playerStats().getAccountExpiration())+"!\n\r");

		String genderName="neuter";
		if(mob.charStats().gender()=='M') genderName="male";
		else
		if(mob.charStats().gender()=='F') genderName="female";
		msg.append("You are a ");
		if(mob.baseCharStats().age()>0)
			msg.append("^!"+mob.baseCharStats().age()+"^? year old ");
		msg.append("^!"+genderName);
		if((!CMSecurity.isDisabled("RACES")))
			msg.append(" "+mob.charStats().getMyRace().name() + "^?");
		else
			msg.append("^?");
		if(mob.getLiegeID().length()>0)
		{
			if(mob.isMarriedToLiege())
				msg.append(" who is married to ^H"+mob.getLiegeID()+"^?");
			else
				msg.append(" who serves ^H"+mob.getLiegeID()+"^?");
		}
		msg.append(".\n\r");
		msg.append("\n\r^NYour stats are: ");
		msg.append(CMProps.mxpImage(mob," ALIGN=RIGHT H=70 W=70"));
		msg.append("\n\r");
		int max=CMProps.getIntVar(CMProps.SYSTEMI_BASEMAXSTAT);
		CharStats CT=mob.charStats();
		if(parm.equalsIgnoreCase("BASE")) CT=mob.baseCharStats();
		msg.append("^N^!");
		msg.append("^?\n\r");
		msg.append("You have ^H"+mob.charStats().getPoints(CharStats.STAT_HITPOINTS)+"/"+mob.charStats().getMaxPoints(CharStats.STAT_HITPOINTS)+"^? ^<HELP^>hit points^</HELP^>, ^H");
		msg.append(mob.charStats().getPoints(CharStats.STAT_MANA)+"/"+mob.charStats().getMaxPoints(CharStats.STAT_MANA)+"^? ^<HELP^>mana^</HELP^>, and ^H");
		msg.append(mob.charStats().getPoints(CharStats.STAT_MOVE)+"/"+mob.charStats().getMaxPoints(CharStats.STAT_MOVE)+"^? ^<HELP^>movement^</HELP^>.\n\r");
		msg.append("You are ^!"+mob.envStats().height()+"^? inches tall.\n\r");
		if(CMSecurity.isAllowed(mob,mob.location(),"CARRYALL"))
			msg.append("You are carrying ^!"+mob.numItems()+"^? items weighing ^!"+mob.envStats().weight()+"^? pounds.\n\r");
		msg.append("You have been online for ^!"+Math.round(CMath.div(mob.getAgeHours(),60.0))+"^? hours.\n\r");
		msg.append("Your ^<HELP^>armored defence^</HELP^> is: ^H"+CMLib.combat().armorStr(mob)+"^?.\n\r");
		msg.append("Your ^<HELP^>combat prowess^</HELP^> is : ^H"+CMLib.combat().fightingProwessStr(mob)+"^?.\n\r");
		//if(CMLib.flags().canSeeHidden(mob))
		//	msg.append("Your ^<HELP^>observation score^</HELP^> : ^H"+CMLib.flags().getDetectScore(mob)+"^?.\n\r");
		msg.append("Wimpy is set to ^!"+mob.getWimpHitPoint()+"^? hit points.\n\r");
		if((mob.playerStats()!=null)&&(mob.soulMate()==null)&&(mob.playerStats().getHygiene()>=PlayerStats.HYGIENE_DELIMIT))
		{
			if(CMSecurity.isASysOp(mob)) 
				mob.playerStats().setHygiene(0);
			else
			{
				int x=(int)(mob.playerStats().getHygiene()/PlayerStats.HYGIENE_DELIMIT);
				if(x<=1) msg.append("^!You could use a bath.^?\n\r"); 
				else
				if(x<=3) msg.append("^!You could really use a bath.^?\n\r"); 
				else
				if(x<=7) msg.append("^!You need to bathe, soon.^?\n\r");
				else
				if(x<15) msg.append("^!You desperately need to bathe.^?\n\r");
				else msg.append("^!Your stench is horrendous! Bathe dammit!.^?\n\r");
			}
		}

		if(CMLib.flags().isBound(mob))
			msg.append("^!You are bound.^?\n\r");

		// dont do falling -- the flag doubles for drowning/treading water anyway.
		//if(CMLib.flags().isFalling(mob))
		//	msg.append("^!You are falling!!!^?\n\r");
		//else
		if(CMLib.flags().isSleeping(mob))
			msg.append("^!You are sleeping.^?\n\r");
		else
		if(CMLib.flags().isSitting(mob))
			msg.append("^!You are resting.^?\n\r");
		else
		if(CMLib.flags().isSwimmingInWater(mob))
			msg.append("^!You are swimming.^?\n\r");
		else
		if(CMLib.flags().isClimbing(mob))
			msg.append("^!You are climbing.^?\n\r");
		else
		if(CMLib.flags().isFlying(mob))
			msg.append("^!You are flying.^?\n\r");
		else
		if(CMLib.flags().isLayingDown(mob))
			msg.append("^!You are laying down.^?\n\r");
		else
			msg.append("^!You are standing.^?\n\r");
		if(CMLib.flags().isInvisible(mob))
			msg.append("^!You are invisible.^?\n\r");
		if(CMLib.flags().isHidden(mob))
			msg.append("^!You are hidden.^?\n\r");// ("+CMLib.flags().getHideScore(mob)+").^?\n\r");
		if(CMLib.flags().isSneaking(mob))
			msg.append("^!You are sneaking.^?\n\r");
		if(mob.charStats().getFatigue()>CharStats.FATIGUED_MILLIS)
			msg.append("^!You are fatigued.^?\n\r");
		if(mob.charStats().getPoints(CharStats.STAT_HUNGER)<1)
			msg.append("^!You are hungry.^?\n\r");
		if(mob.charStats().getPoints(CharStats.STAT_THIRST)<1)
			msg.append("^!You are thirsty.^?\n\r");
		msg.append(getAffects(mob.session(),mob,false));
		return msg;
	}

	public boolean execute(MOB mob, Vector commands, int metaFlags)
		throws java.io.IOException
	{
		StringBuilder msg=getScore(mob);
		if(commands.size()==0)
		{
			commands.addElement(msg);
			return false;
		}
		if(!mob.isMonster())
			mob.session().wraplessPrintln(msg.toString());
		return false;
	}
	
	public boolean canBeOrdered(){return true;}

	
}
