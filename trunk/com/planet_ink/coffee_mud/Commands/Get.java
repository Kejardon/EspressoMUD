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

public class Get extends StdCommand
{
	public Get(){access=new String[]{"GET","G"};}

	@Override public boolean execute(MOB mob, Vector<String> commands, int metaFlags)
	{
		Room R=mob.location();

		if(commands.size()<2)
		{
			mob.tell("Get what?");
			return false;
		}
		commands.removeElementAt(0);

		//Object getFrom=null;
		String whatToGet;
		boolean containerAll;
		Vector<Interactable> containers=null;

		int partition=CMLib.english().getPartitionIndex(commands, "from");
		if(partition==-1)
		{
			//getFrom=R;
			whatToGet=CMParms.combine(commands,0);
		}
		else
		{
			String containerName=CMParms.combine(commands,partition);
			String upper=containerName.toUpperCase();
			containerAll=upper.startsWith("ALL ");
			if(upper.startsWith("ALL.")){ containerAll=true; containerName="ALL "+containerName.substring(4);}
			if(upper.endsWith(".ALL")){ containerAll=true; containerName="ALL "+containerName.substring(0,containerName.length()-4);}
			if(containerAll)
			{
				Vector<Interactable> V=CMLib.english().fetchInteractables(containerName,false,1,Integer.MAX_VALUE,mob.getItemCollection(),R.getItemCollection());
				if(V.isEmpty())
				{
					mob.tell("You don't see '"+containerName+"' here.");
					return false;
				}
				for(int i=V.size()-1;i>=0;i--)
					if(!(V.get(i) instanceof Container))
						V.remove(i);
				if(V.isEmpty())
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

		int maxToGet=CMLib.english().calculateMaxToGive(mob,commands,R,true);
		if(maxToGet<0) return false;

		String unmodifiedWhatToGet=whatToGet;
		whatToGet=whatToGet.toUpperCase();
		boolean allFlag=whatToGet.startsWith("ALL ");
		if(whatToGet.toUpperCase().startsWith("ALL.")){ allFlag=true; whatToGet="ALL "+whatToGet.substring(4);}
		if(whatToGet.toUpperCase().endsWith(".ALL")){ allFlag=true; whatToGet="ALL "+whatToGet.substring(0,whatToGet.length()-4);}
		done:
		if(allFlag)
		{
			Vector<Interactable> getThese;
			if(containers==null)
			{
				getThese=CMLib.english().fetchInteractables(whatToGet,false,1,maxToGet,R.getItemCollection());
				if(getThese.isEmpty())
				{
					mob.tell("You don't see '"+unmodifiedWhatToGet+"' here.");
					return false;
				}
				for(Item I : (Item[])getThese.toArray(Item.dummyItemArray))
				{
					CMMsg msg=CMClass.getMsg(mob,I,(Vector)null,EnumSet.of(CMMsg.MsgCode.GET),"^[S-NAME] get^s ^[T-NAME].");
					if(!R.doMessage(msg))
					{
						msg.returnMsg();
						break;
					}
					msg.returnMsg();
				}
			}
			else
			{
				getThese=new Vector();
				//NOTE: Loops like these won't entirely work how I like with stuff like '6.potion'
				for(Container C : (Container[])containers.toArray(Container.dummyContainerArray))
				{
					Vector<Interactable> subGetThese=CMLib.english().fetchInteractables(whatToGet,false,1,maxToGet,C.getItemCollection());
					getThese.addAll(subGetThese);
					maxToGet-=subGetThese.size();
					if(maxToGet==0) break;
				}
				for(Item I : (Item[])getThese.toArray(Item.dummyItemArray))
				{
					CMMsg msg=CMClass.getMsg(mob,I,I.container(),EnumSet.of(CMMsg.MsgCode.GET),"^[S-NAME] get^s ^[T-NAME] from ^[O-NAME].");
					if(!R.doMessage(msg))
					{
						msg.returnMsg();
						break;
					}
					msg.returnMsg();
				}
			}
		}
		else
		{
			Interactable getThis;
			if(containers==null)
			{
				getThis=CMLib.english().fetchInteractable(whatToGet,false,1,R.getItemCollection());
				if(getThis==null)
				{
					mob.tell("You don't see '"+unmodifiedWhatToGet+"' here.");
					return false;
				}
				CMMsg msg=CMClass.getMsg(mob,getThis,(Vector)null,EnumSet.of(CMMsg.MsgCode.GET),"^[S-NAME] get^s ^[T-NAME].");
				R.doMessage(msg);
				msg.returnMsg();
			}
			else
			{
				for(Container C : (Container[])containers.toArray(Container.dummyContainerArray))
				{
					getThis=CMLib.english().fetchInteractable(whatToGet,false,1,C.getItemCollection());
					if(getThis!=null)
					{
						CMMsg msg=CMClass.getMsg(mob,getThis,(Vector)null,EnumSet.of(CMMsg.MsgCode.GET),"^[S-NAME] get^s ^[T-NAME].");
						R.doMessage(msg);
						msg.returnMsg();
						break done;
					}
				}
				mob.tell("You don't see '"+unmodifiedWhatToGet+"' here.");
				return false;
			}
		}
		return false;
	}
	@Override public int commandType(MOB mob, String cmds){return CT_LOW_P_ACTION;}
	@Override public boolean canBeOrdered(){return true;}
}