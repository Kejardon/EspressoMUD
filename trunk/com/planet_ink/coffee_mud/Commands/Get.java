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
public class Get extends StdCommand
{
	public Get(){}

	private String[] access={"GET","G"};
	public String[] getAccessWords(){return access;}

	public boolean execute(MOB mob, Vector commands, int metaFlags)
		throws java.io.IOException
	{
		Room R=mob.location();

		if(commands.size()<2)
		{
			mob.tell("Get what?");
			return false;
		}
		commands.removeElementAt(0);

		Object getFrom=null;
		String whatToGet=null;
		boolean containerAll=false;
		Vector<Interactable> containers=null;

		int partition=CMLib.english().getPartitionIndex(commands, "from");
		if(partition==-1)
		{
			getFrom=R;
			whatToGet=CMParms.combine(commands,0);
		}
		else
		{
			String containerName=CMParms.combine(commands,partition);
			String upper=containerName.toUpperCase();
			containerAll=containerName.startsWith("ALL ");
			if(containerName.startsWith("ALL.")){ containerAll=true; containerName="ALL "+containerName.substring(4);}
			if(containerName.endsWith(".ALL")){ containerAll=true; containerName="ALL "+containerName.substring(0,containerName.length()-4);}
			if(containerAll)
			{
				Vector<Interactable> V=CMLib.english().fetchInteractables(containerName,false,1,Integer.MAX_VALUE,mob.getItemCollection(),R.getItemCollection());
				if(V.size()==0)
				{
					mob.tell("You don't see '"+containerName+"' here.");
					return false;
				}
				for(int i=V.size()-1;i>=0;i--)
					if(!(V.get(i) instanceof Container))
						V.remove(i);
				if(V.size()==0)
				{
					mob.tell("None of those are containers!");
					return false;
				}
				containers=V;
			}
			else
			{
				Interactable I=CMLib.english().fetchInteractable(containerName,false,1,mob.getItemCollection(),R.getItemCollection());
				if(I==null)
				{
					mob.tell("You don't see '"+containerName+"' here.");
					return false;
				}
				if(!(I instanceof Container))
				{
					mob.tell("That doesn't have anything inside it.");
					return false;
				}
				containers=new Vector(1);
				containers.add(I);
			}
			whatToGet=CMParms.combine(commands,0,partition);
		}

		int maxToGet=CMLib.english().calculateMaxToGive(mob,commands,false,R,true);
		if(maxToGet<0) return false;

		String unmodifiedWhatToGet=whatToGet;
		whatToGet=whatToGet.toUpperCase();
		boolean allFlag=whatToGet.startsWith("ALL ");
		if(whatToGet.toUpperCase().startsWith("ALL.")){ allFlag=true; whatToGet="ALL "+whatToGet.substring(4);}
		if(whatToGet.toUpperCase().endsWith(".ALL")){ allFlag=true; whatToGet="ALL "+whatToGet.substring(0,whatToGet.length()-4);}
		done:
		if(allFlag)
		{
			Vector<Interactable> getThese=null;
			if(containers==null)
			{
				getThese=CMLib.english().fetchInteractables(whatToGet,false,1,maxToGet,R.getItemCollection());
//				for(int i=getThese.size()-1;i>=0;i--)
//					if(!(getThese.get(i) instanceof Item))
//						getThese.remove(i);
				if(getThese.size()==0)
				{
					mob.tell("You don't see '"+unmodifiedWhatToGet+"' here.");
					return false;
				}
				for(Item I : (Item[])getThese.toArray())
					if(!R.doMessage(CMClass.getMsg(mob,I,null,EnumSet.of(CMMsg.MsgCode.GET),"<S-NAME> get(s) <T-NAME>.")))
						break;
			}
			else
			{
				getThese=new Vector();
				//NOTE: Loops like these won't entirely work how I like with stuff like '6.potion'
				for(Container C : (Container[])containers.toArray())
				{
					Vector<Interactable> subGetThese=CMLib.english().fetchInteractables(whatToGet,false,1,maxToGet,C.getItemCollection());
//					for(int i=subGetThese.size()-1;i>=0;i--)
//						if(!(subGetThese.get(i) instanceof Item))
//							subGetThese.remove(i);
					getThese.addAll(subGetThese);
					maxToGet-=subGetThese.size();
					if(maxToGet==0) break;
				}
				for(Item I : (Item[])getThese.toArray())
					if(!R.doMessage(CMClass.getMsg(mob,I,I.container(),EnumSet.of(CMMsg.MsgCode.GET),"<S-NAME> get(s) <T-NAME> from <O-NAME>.")))
						break;
			}
		}
		else
		{
			Interactable getThis=null;
			if(containers==null)
			{
				getThis=CMLib.english().fetchInteractable(whatToGet,false,1,R.getItemCollection());
				if(getThis==null)
				{
					mob.tell("You don't see '"+unmodifiedWhatToGet+"' here.");
					return false;
				}
				R.doMessage(CMClass.getMsg(mob,getThis,null,EnumSet.of(CMMsg.MsgCode.GET),"<S-NAME> get(s) <T-NAME>."));
			}
			else
			{
				for(Container C : (Container[])containers.toArray())
				{
					getThis=CMLib.english().fetchInteractable(whatToGet,false,1,C.getItemCollection());
					if(getThis!=null)
					{
						R.doMessage(CMClass.getMsg(mob,getThis,null,EnumSet.of(CMMsg.MsgCode.GET),"<S-NAME> get(s) <T-NAME>."));
						break done;
					}
				}
				mob.tell("You don't see '"+unmodifiedWhatToGet+"' here.");
				return false;
			}
		}
		return false;
	}
	public double actionsCost(MOB mob, Vector cmds){return DEFAULT_NONCOMBATACTION;}
	public boolean canBeOrdered(){return true;}
}
