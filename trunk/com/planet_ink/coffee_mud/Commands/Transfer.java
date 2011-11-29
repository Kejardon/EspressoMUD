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
public class Transfer extends At
{
	public Transfer(){}

	private String[] access={"TRANSFER"};
	public String[] getAccessWords(){return access;}
	public boolean execute(MOB mob, Vector commands, int metaFlags)
		throws java.io.IOException
	{
		Room room=null;
		if(commands.size()<3)
		{
			mob.tell("Transfer whom/what to where? Try all or an item/mob name, followed by a Room ID, target player name, area name, or room text!");
			return false;
		}
		commands.remove(0);
		boolean mobFlag=false;
		boolean itemFlag=false;
		if(((String)commands.get(0)).equalsIgnoreCase("ITEM"))
		{
			itemFlag=true;
			commands.remove(0);
		}
		if(((String)commands.get(0)).equalsIgnoreCase("MOB"))
		{
			mobFlag=true;
			commands.remove(0);
		}
		if(commands.size()<2)
		{
			mob.tell("Be more specific, WHAT item/mob do you want to transfer?");
			return false;
		}

		int maxToDo=CMLib.english().calculateMaxToGive(mob,commands,false,null,true);
		if(maxToDo<0) return false;

		int partition=CMLib.english().getPartitionIndex(commands, "to", 1);
		String mobname=CMParms.combine(commands,0,partition);
		String cmd = CMParms.combine(commands,partition);
		Room curRoom=mob.location();
		Vector<Item> V=null;

		String whatToDo=mobname.toUpperCase();
		boolean allFlag=whatToDo.startsWith("ALL ")||whatToDo.equals("ALL");
		if(whatToDo.toUpperCase().startsWith("ALL.")){ allFlag=true; whatToDo="ALL "+whatToDo.substring(4);}
		if(whatToDo.toUpperCase().endsWith(".ALL")){ allFlag=true; whatToDo="ALL "+whatToDo.substring(0,whatToDo.length()-4);}

		done:
		if(allFlag)
		{
			V=(Vector)CMLib.english().fetchInteractables(whatToDo,false,1,Integer.MAX_VALUE,curRoom.getItemCollection());
			if(mobFlag) for(int i=V.size()-1;i>=0;i--)
			{
				Item I=V.get(i);
				if((I instanceof Body)&&(((Body)I).mob()!=null))
					V.remove(i);
			}
			else if(itemFlag) for(int i=V.size()-1;i>=0;i--)
			{
				Item I=V.get(i);
				if((I instanceof Body)&&(((Body)I).mob()!=null))
					V.remove(i);
			}
			if(V.size()>maxToDo)
				V.setSize(maxToDo);
		}
		else
		{
			if(itemFlag)
			{
				V=(Vector)CMLib.english().fetchInteractables(whatToDo,false,1,Integer.MAX_VALUE,curRoom.getItemCollection());
				for(int i=0;i<V.size();i++)
				{
					Item I=V.get(i);
					if(!(I instanceof Body)||(((Body)I).mob()==null))
					{
						V=new Vector();
						V.add(I);
						break;
					}
				}
			}
			else if(mobFlag)
			{
				V=(Vector)CMLib.english().fetchInteractables(whatToDo,false,1,Integer.MAX_VALUE,curRoom.getItemCollection());
				for(int i=0;i<V.size();i++)
				{
					Item I=V.get(i);
					if((I instanceof Body)&&(((Body)I).mob()!=null))
					{
						V=new Vector();
						V.add(I);
						break;
					}
				}
			}
			else
			{
				V=new Vector();
				for(int s=0;s<CMLib.sessions().size();s++)
				{
					Session S=CMLib.sessions().elementAt(s);
					MOB M=S.mob();
					if((M!=null)&&(M.name().equalsIgnoreCase(mobname)))
					{
						V.addElement(M.body());
						break done;
					}
				}
				V.add((Item)CMLib.english().fetchInteractable(whatToDo,false,1,curRoom.getItemCollection()));
			}
		}

		if((V.size()==0)||(V.get(0)==null))
		{
			mob.tell("Transfer what?  '"+mobname+"' is unknown to you.");
			return false;
		}

		if(cmd.equalsIgnoreCase("here")||cmd.equalsIgnoreCase("."))
			room=curRoom;
		else
			room=CMLib.map().findWorldRoomLiberally(mob,cmd,"RIPME",100,120);

		if(room==null)
		{
			mob.tell("Transfer where? '"+cmd+"' is unknown.  Enter a Room ID, player name, area name, or room text!");
			return false;
		}
		for(int i=0;i<V.size();i++)
		{
			Item I=V.get(i);
			room.bringHere(I,false);
			if((I instanceof Body)&&(((Body)I).mob()!=null))
				CMLib.commands().postLook(((Body)I).mob());
		}
		mob.tell("Done.");
		return false;
	}
	
	public boolean canBeOrdered(){return true;}
	public boolean securityCheck(MOB mob){return CMSecurity.isAllowed(mob,mob.location(),"TRANSFER");}
}
