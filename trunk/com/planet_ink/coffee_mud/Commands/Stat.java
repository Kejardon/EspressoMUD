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
public class Stat  extends StdCommand
{
	public Stat(){}

	private String[] access={"STAT"};
	public String[] getAccessWords(){return access;}

	public static final int ABLETYPE_EQUIPMENT=-2;
	public static final int ABLETYPE_INVENTORY=-3;
	public static final int ABLETYPE_TITLES=-8;
	public static final int ABLETYPE_ROOMSEXPLORED=-9;
	public static final int ABLETYPE_AREASEXPLORED=-10;
	public static final int ABLETYPE_WORLDEXPLORED=-11;
	
	public static final String[][] ABLETYPE_DESCS={
		{"EQUIPMENT","EQ","EQUIP"},
		{"INVENTORY","INVEN","INV"},
		{"COMBAT"},
		{"TITLES","TITLE"},
		{"ROOMSEXPLORED"},
		{"AREASEXPLORED"},
		{"WORLDEXPLORED"},
	};
	
	public MOB getTarget(MOB mob, String targetName, boolean quiet)
	{
		MOB target=null;
		if(targetName.length()>0)
		{
			target=mob.location().fetchInhabitant(targetName);
			if(target==null)
			{
				Environmental t=mob.location().fetchFromRoomFavorItems(null,targetName,Wearable.FILTER_UNWORNONLY);
				if((t!=null)&&(!(t instanceof MOB)))
				{
					if(!quiet)
						mob.tell(mob,t,null,"You can't do that to <T-NAMESELF>.");
					return null;
				}
			}
		}
		return target;
	}

	public boolean showTableStats(MOB mob, int days, int scale, String rest)
	{
		Calendar ENDQ=Calendar.getInstance();
		ENDQ.add(Calendar.DATE,-days);
		ENDQ.set(Calendar.HOUR_OF_DAY,23);
		ENDQ.set(Calendar.MINUTE,59);
		ENDQ.set(Calendar.SECOND,59);
		ENDQ.set(Calendar.MILLISECOND,999);
		mob.tell("No Stats?!");
		return false;
	}

	public boolean execute(MOB mob, Vector commands, int metaFlags)
		throws java.io.IOException
	{
		commands.removeElementAt(0);
		if((commands.size()>0)
		&&(commands.firstElement() instanceof String)
		&&((String)commands.firstElement()).equals("?"))
		{
			StringBuilder msg = new StringBuilder("STAT allows the following options: \n\r");
			msg.append("[MOB/PLAYER NAME], [NUMBER] [DAYS/WEEKS/MONTHS], ");
			for(int i=0;i<ABLETYPE_DESCS.length;i++)
				msg.append(ABLETYPE_DESCS[i][0]+", ");
			msg.append(CMParms.toStringList(Ability.ACODE_DESCS));
			mob.tell(msg.toString());
			return false;
		}
		if(commands.size()==0) commands.addElement("TODAY");
		String s1=(commands.size()>0)?((String)commands.elementAt(0)).toUpperCase():"";
		String s2=(commands.size()>1)?((String)commands.elementAt(1)).toUpperCase():"";
		if(s1.equalsIgnoreCase("TODAY"))
			return showTableStats(mob,1,1,CMParms.combine(commands,1));
		else
		if(commands.size()>1)
		{
			String rest=(commands.size()>2)?CMParms.combine(commands,2):"";
			if(s2.equals("DAY")&&(CMath.isNumber(s1)))
				return showTableStats(mob,(CMath.s_int(s1)),1,rest);
			else
			if(s2.equals("DAYS")&&(CMath.isNumber(s1)))
				return showTableStats(mob,(CMath.s_int(s1)),1,rest);
			else
			if(s2.equals("WEEK")&&(CMath.isNumber(s1)))
				return showTableStats(mob,(CMath.s_int(s1)*7),7,rest);
			else
			if(s2.equals("WEEKS")&&(CMath.isNumber(s1)))
				return showTableStats(mob,(CMath.s_int(s1)*7),7,rest);
			else
			if(s2.equals("MONTH")&&(CMath.isNumber(s1)))
				return showTableStats(mob,(CMath.s_int(s1)*30),30,rest);
			else
			if(s2.equals("MONTHS")&&(CMath.isNumber(s1)))
				return showTableStats(mob,(CMath.s_int(s1)*30),30,rest);
			else
			if(s2.equals("YEAR")&&(CMath.isNumber(s1)))
				return showTableStats(mob,(CMath.s_int(s1)*365),365,rest);
			else
			if(s2.equals("YEARS")&&(CMath.isNumber(s1)))
				return showTableStats(mob,(CMath.s_int(s1)*365),365,rest);
		}
		
		int ableTypes=-1;
		if(commands.size()>1)
		{
			String s=((String)commands.elementAt(0)).toUpperCase();
			for(int i=0;i<ABLETYPE_DESCS.length;i++)
				for(int is=0;is<ABLETYPE_DESCS[i].length;is++)
					if(s.equals(ABLETYPE_DESCS[i][is]))
					{
						ableTypes=-2 -i;
						commands.removeElementAt(0);
						break;
					}
			if(ableTypes==-1)
			for(int a=0;a<Ability.ACODE_DESCS.length;a++)
			{
				if((Ability.ACODE_DESCS[a]+"S").equals(s)||(Ability.ACODE_DESCS[a]).equals(s))
				{
					ableTypes=a;
					commands.removeElementAt(0);
					break;
				}
			}
		}
		String MOBname=CMParms.combine(commands,0);
		MOB target=getTarget(mob,MOBname,true);
		if((target==null)||(!target.isMonster()))
			target=mob.location().fetchInhabitant(MOBname);
		if((target==null)||(!target.isMonster()))
		{
			try
			{
				Vector inhabs=CMLib.map().findInhabitants(CMLib.map().rooms(), mob,MOBname,100);
				for(Enumeration m=inhabs.elements();m.hasMoreElements();)
				{
					MOB mob2=(MOB)m.nextElement();
					Room R=mob2.location();
					if(CMSecurity.isAllowed(mob,R,"STAT"))
					{
						target=mob2;
						break;
					}
				}
			}catch(NoSuchElementException nse){}
		}
		if(target==null)
			target=CMLib.players().getLoadPlayer(MOBname);
		if(target==null)
		{
			mob.tell("You can't stat '"+MOBname+"'  -- he doesn't exist.");
			return false;
		}

		StringBuilder str=new StringBuilder("");
		if(ableTypes==ABLETYPE_EQUIPMENT)
			str=CMLib.commands().getEquipment(mob,target);
		else
		if(ableTypes==ABLETYPE_INVENTORY)
			str=CMLib.commands().getInventory(mob,target);
		else
		if(ableTypes==ABLETYPE_TITLES)
		{
			str.append("Titles:");
			StringBuffer ttl=new StringBuffer("");
			if(target.playerStats()!=null)
				for(int t=0;t<target.getTitles().size();t++)
				{
					String title = (String)target.getTitles().elementAt(t);
					ttl.append(" "+title+",");
				}
			if(ttl.length()==0)
				ttl.append(" None!");
			ttl.deleteCharAt(ttl.length()-1);
			str.append(ttl);
			str.append("\n\r");
		}
		else
		if(ableTypes==ABLETYPE_WORLDEXPLORED)
		{
			if(target.playerStats()!=null)
				str.append(target.name()+" has explored "+mob.playerStats().percentVisited(mob,null)+"% of the world.\n\r");
			else
				str.append("Exploration data is not kept on mobs.\n\r");
		}
		else
		if(ableTypes==ABLETYPE_AREASEXPLORED)
		{
			if(target.playerStats()!=null)
			{
				for(Enumeration e=CMLib.map().areas();e.hasMoreElements();)
				{
					Area A=(Area)e.nextElement();
					int pct=mob.playerStats().percentVisited(target, A);
					if(pct>0) str.append("^H"+A.name()+"^N: "+pct+"%, ");
				}
				str=new StringBuilder(str.toString().substring(0,str.toString().length()-2)+"\n\r");
			}
			else
				str.append("Exploration data is not kept on mobs.\n\r");
		}
		else
		if(ableTypes==ABLETYPE_ROOMSEXPLORED)
		{
			if(target.playerStats()!=null)
			{
				for(Enumeration e=CMLib.map().rooms();e.hasMoreElements();)
				{
					Room R=(Room)e.nextElement();
					if((R.roomID().length()>0)&&(mob.playerStats().hasVisited(R)))
						str.append("^H"+R.roomID()+"^N, ");
				}
				str=new StringBuilder(str.toString().substring(0,str.toString().length()-2)+"\n\r");
			}
			else
				str.append("Exploration data is not kept on mobs.\n\r");
		}
		else
			str=CMLib.commands().getScore(target);
		if(!mob.isMonster())
			mob.session().wraplessPrintln(str.toString());
		return false;
	}

	public void recoverMOB(MOB M)
	{
		M.recoverCharStats();
		M.recoverEnvStats();
		M.resetToMaxState();
	}
	public void testMOB(MOB target,MOB M, Environmental test)
	{
		test.affectCharStats(target,M.charStats());
		test.affectEnvStats(target,M.envStats());
	}
	public void reportOnDiffMOB(Environmental test, int diff, StringBuilder str)
	{
		if(diff>0)
			str.append("^C"+CMStrings.padRight(test.Name(),40)+": ^W+"+diff+"\n\r");
		else
		if(diff<0)
			str.append("^C"+CMStrings.padRight(test.Name(),40)+": ^W"+diff+"\n\r");
	}
	
	public boolean canBeOrdered(){return true;}
	public boolean securityCheck(MOB mob){return CMSecurity.isAllowed(mob,mob.location(),"STAT");}

	
}
