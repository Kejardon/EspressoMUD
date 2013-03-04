package com.planet_ink.coffee_mud.Commands;
import com.planet_ink.coffee_mud.core.interfaces.*;
import com.planet_ink.coffee_mud.core.*;

import java.util.*;

/*
CoffeeMUD 5.6.2 copyright 2000-2010 Bo Zimmerman
EspressoMUD copyright 2011 Kejardon

Licensed under the Apache License, Version 2.0. You may obtain a copy of the license at
	http://www.apache.org/licenses/LICENSE-2.0
*/
@SuppressWarnings("unchecked")
public class Time extends StdCommand
{
	public Time(){access=new String[]{"TIME","DATE"};}

	public boolean execute(MOB mob, Vector<String> commands, int metaFlags)
	{
		Room room=mob.location();
		if(room==null) return false;
		mob.tell(room.getArea().getTimeObj().timeDescription(mob,room));
/*		if((mob.playerStats()!=null)&&(mob.getBirthday()!=null))
		{
			TimeClock C=CMLib.misc().globalClock();
			int day=C.getDayOfMonth();
			int month=C.getMonth();
			int year=C.getYear();
			int bday=mob.getBirthday()[0];
			int bmonth=mob.getBirthday()[1];
			if((month>bmonth)||((month==bmonth)&&(day>bday)))
				year++;
			
			StringBuffer timeDesc=new StringBuffer("");
			if(C.getDaysInWeek()>0)
			{
				long x=((long)year)*((long)C.getMonthsInYear())*C.getDaysInMonth();
				x=x+((long)(bmonth-1))*((long)C.getDaysInMonth());
				x=x+bday;
				timeDesc.append(C.getWeekNames()[(int)(x%C.getDaysInWeek())]+", ");
			}
			timeDesc.append("the "+bday+CMath.numAppendage(bday));
			timeDesc.append(" day of "+C.getMonthNames()[bmonth-1]);
			if(C.getYearNames().length>0)
				timeDesc.append(", "+C.getYearNames()[year%C.getYearNames().length].replace("#",""+year));
			mob.tell("Your next birthday is "+timeDesc.toString()+".");
		}
*/
		return false;
	}

	public int commandType(MOB mob, String cmds){return CT_NON_ACTION;}
	public boolean canBeOrdered(){return true;}
}
