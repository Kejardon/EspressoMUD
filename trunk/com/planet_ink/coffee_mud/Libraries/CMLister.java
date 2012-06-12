package com.planet_ink.coffee_mud.Libraries;
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
import com.planet_ink.coffee_mud.Libraries.interfaces.*;
import com.planet_ink.coffee_mud.MOBS.interfaces.*;
import com.planet_ink.coffee_mud.Races.interfaces.*;

import java.util.*;
import java.nio.ByteBuffer;
import java.util.concurrent.CopyOnWriteArrayList;

/*
CoffeeMUD 5.6.2 copyright 2000-2010 Bo Zimmerman
EspressoMUD copyright 2011 Kejardon

Licensed under the Apache License, Version 2.0. You may obtain a copy of the license at
	http://www.apache.org/licenses/LICENSE-2.0
*/
@SuppressWarnings("unchecked")
public class CMLister extends StdLibrary implements ListingLibrary
{
	public String ID(){return "CMLister";}
	public String itemSeenString(MOB viewer,
								 Interactable item,
								 boolean useName
								 //boolean longLook,
								 //boolean sysmsgs
								 )
	{
		if((useName)||(item.displayText().length()==0))
			return CMStrings.capitalizeFirstLetter(item.name());
		else
			return CMStrings.capitalizeFirstLetter(item.displayText());
	}
	
	public int getReps(Item item, 
					   ArrayList<Item> theRest, 
					   MOB mob, 
					   boolean useName)
	{
		String stackName;
		if(useName)
			stackName=item.name();
		else
			stackName=item.stackableName();
		if(stackName.length()<=0) return 0;
		int reps=0;
		if(useName) for(int here=theRest.size()-1;here>=0;here--)
		{
			if(stackName.equals(theRest.get(here).name()))
			{
				reps++;
				theRest.remove(here);
			}
		}
		else for(int here=theRest.size()-1;here>=0;here--)
		{
			if(stackName.equals(theRest.get(here).stackableName()))
			{
				reps++;
				theRest.remove(here);
			}
		}
		return reps;
	}
	
	public void appendReps(int reps, StringBuilder say)
	{
		if(reps==0) say.append("      ");
		else
		if(reps>=99)
			say.append("("+CMStrings.padLeftPreserve(""+(reps+1),3)+") ");
		else
		if(reps>0)
			say.append(" ("+CMStrings.padLeftPreserve(""+(reps+1),2)+") ");
	}
	
	public String summarizeTheRest(MOB mob, ArrayList<Item> things) 
	{
		ArrayList restV=new ArrayList();
		Item I=null;
		String name="";
		boolean otherItemsHere=false;
		for(int v=0;v<things.size();v++)
		{
			I=things.get(v);
			name=CMLib.materials().genericType(I).toLowerCase();
			if(name.startsWith("item"))
				otherItemsHere=true;
			else if(!restV.contains(name))
				restV.add(name);
		}
		if((restV.size()==0)&&(!otherItemsHere)) return "";
		if(otherItemsHere) restV.add("other");
		StringBuilder theRest=new StringBuilder("");
		int size=restV.size();
		for(int o=0;o<size;o++)
		{
			theRest.append(restV.get(o));
			if(o<size-1)
				theRest.append(", ");
			if((size>1)&&(o==(size-2)))
				theRest.append("and ");
		}
		return "There are also "+theRest.toString()+" items here.\r\n";
	}
	
	public StringBuilder lister(MOB mob, 
							   ArrayList<Item> things,
							   boolean useName, 
							   String tag,
							   String tagParm,
							   boolean longLook)
	{
		//boolean nameTagParm=((tagParm!=null)&&(tagParm.indexOf("*")>=0));
		StringBuilder say=new StringBuilder("");
		Item item=null;
		boolean sysmsgs=(mob!=null)?mob.playerStats().hasBits(PlayerStats.ATT_SYSOPMSGS):false;
		int numShown=0;
		int maxToShow=CMProps.Ints.MAXITEMSHOWN.property();
		while(things.size()>0)
		{
			if((maxToShow>0)&&(!longLook)&&(!sysmsgs)&&(!useName)&&(numShown>=maxToShow))
			{
				say.append(summarizeTheRest(mob,things));
				things.clear();
				break;
			}
			item=things.remove(0);
			int reps=getReps(item,things,mob,(useName||longLook));
			if((item.displayText().length()>0)||sysmsgs||useName)
			{
				numShown++;
				appendReps(reps,say);
				if(sysmsgs)
					say.append("("+item.ID()+") ");
				say.append("");
				
				/*if(tag!=null)
				{
					if(nameTagParm)
						say.append("^<"+tag+tagParm.replace("*",item.name())+"^>");
					else
						say.append("^<"+tag+tagParm+"^>");
				}*/
				if(reps>0)
				{
					if(!(useName||longLook))
						say.append(CMStrings.endWithAPeriod(item.stackableName()));
					else
						say.append(CMStrings.endWithAPeriod(item.name()));
				}
				else
					say.append(CMStrings.endWithAPeriod(itemSeenString(mob,item,useName)));
				/*if(tag!=null)
					say.append("^</"+tag+"^>");*/
//				say.append(CMLib.flags().colorCodes(item,mob)+"\r\n");

				//This should be in Container's class, not here.
/*				if((longLook)
				&&(item instanceof Container)
				&&(((Container)item).container()==null)
				&&(((Container)item).isOpen())
				&&(!((Container)item).hasALid())
				&&(!CMLib.flags().canBarelyBeSeenBy(item,mob)))
				{
					Vector V=((Container)item).getContents();
					Item item2=null;
					while(V.size()>0)
					{
						item2=(Item)V.firstElement();
						V.removeElementAt(0);
						int reps2=getReps(item2,V,mob,useName,false);
						if(CMLib.flags().canBeSeenBy(item2,mob)
						&&((item2.displayText().length()>0)
							||sysmsgs
							||(useName)))
						{
							say.append("      ");
							appendReps(reps2,say);
							if((mob!=null)&&(!mob.isMonster())&&(mob.session().clientTelnetMode(Session.TELNET_MXP)))
								say.append(CMProps.mxpImage(item," H=10 W=10",""," "));
							say.append(CMStrings.endWithAPeriod(itemSeenString(mob,item2,useName)));
							say.append(CMLib.flags().colorCodes(item2,mob)+"\r\n");
						}
					}
				}
*/
			}
		}
		return say;
	}
	
	public StringBuilder reallyList(Hashtable these)
	{
		return reallyList(these,null);
	}
	public StringBuilder reallyList(Vector these)
	{
		return reallyList(these.elements(),null);
	}
	public StringBuilder reallyList(Enumeration these)
	{
		return reallyList(these,null);
	}
	public StringBuilder reallyList(Vector these, Room likeRoom)
	{
		return reallyList(these.elements(),likeRoom);
	}
	public StringBuilder reallyList(Hashtable these, Room likeRoom)
	{
		StringBuilder lines=new StringBuilder("");
		if(these.size()==0) return lines;
		int column=0;
		for(Enumeration e=these.elements();e.hasMoreElements();)
		{
			Object thisThang=e.nextElement();
			String list=null;
			if(thisThang instanceof String)
				list=(String)thisThang;
			else
			if(thisThang instanceof CMObject)
				list=((CMObject)thisThang).ID();
			else
				list=CMClass.classID(thisThang);
			if(((likeRoom!=null)&&(thisThang instanceof Room))
			  &&((((Room)thisThang).saveNum()!=0)&&(!((Room)thisThang).getArea().name().equals(likeRoom.getArea().name()))))
				list=null;
			if(list!=null)
			{
				if(++column>3)
				{
					lines.append("\r\n");
					column=1;
				}
				lines.append(CMStrings.padRight(list,24)+" ");
			}
		}
		lines.append("\r\n");
		return lines;
	}
	public StringBuilder reallyList(Enumeration these, Room likeRoom)
	{
		StringBuilder lines=new StringBuilder("");
		if(!these.hasMoreElements()) return lines;
		int column=0;
		for(Enumeration e=these;e.hasMoreElements();)
		{
			Object thisThang=e.nextElement();
			String list=null;
			if(thisThang instanceof String)
				list=(String)thisThang;
			else
			if(thisThang instanceof CMObject)
				list=((CMObject)thisThang).ID();
			else
				list=CMClass.classID(thisThang);
			if(((likeRoom!=null)&&(thisThang instanceof Room))
			  &&((((Room)thisThang).saveNum()!=0)&&(!((Room)thisThang).getArea().name().equals(likeRoom.getArea().name()))))
				list=null;
			if(list!=null)
			{
				if(++column>3)
				{
					lines.append("\r\n");
					column=1;
				}
				lines.append(CMStrings.padRight(list,24)+" ");
			}
		}
		lines.append("\r\n");
		return lines;
	}
	public StringBuilder reallyList(Iterator these)
	{
		StringBuilder lines=new StringBuilder("");
		if(!these.hasNext()) return lines;
		int column=0;
		while(these.hasNext())
		{
			Object thisThang=these.next();
			String list=null;
			if(thisThang instanceof String)
				list=(String)thisThang;
			else
			if(thisThang instanceof CMObject)
				list=((CMObject)thisThang).ID();
			else
				list=CMClass.classID(thisThang);
			if(list.length()>0)
			{
				if(++column>3)
				{
					lines.append("\r\n");
					column=1;
				}
				lines.append(CMStrings.padRight(list,24)+" ");
			}
		}
		lines.append("\r\n");
		return lines;
	}
	public StringBuilder reallyList2Cols(Enumeration these, Room likeRoom)
	{
		StringBuilder lines=new StringBuilder("");
		if(!these.hasMoreElements()) return lines;
		int column=0;
		for(Enumeration e=these;e.hasMoreElements();)
		{
			Object thisThang=e.nextElement();
			String list=null;
			if(thisThang instanceof String)
				list=(String)thisThang;
			else if(thisThang instanceof CMObject)
				list=((CMObject)thisThang).ID();
			else
				list=CMClass.classID(thisThang);
			if(((likeRoom!=null)&&(thisThang instanceof Room))
			  &&((((Room)thisThang).saveNum()!=0)&&(!((Room)thisThang).getArea().name().equals(likeRoom.getArea().name()))))
				list=null;
			if(list!=null)
			{
				if(++column>2)
				{
					lines.append("\r\n");
					column=1;
				}
				lines.append(CMStrings.padRight(list,37)+" ");
			}
		}
		lines.append("\r\n");
		return lines;
	}
	public StringBuilder reallyList2Cols(Iterator these, Room likeRoom)
	{
		StringBuilder lines=new StringBuilder("");
		if(!these.hasNext()) return lines;
		int column=0;
		while(these.hasNext())
		{
			Object thisThang=these.next();
			String list=null;
			if(thisThang instanceof String)
				list=(String)thisThang;
			else if(thisThang instanceof CMObject)
				list=((CMObject)thisThang).ID();
			else
				list=CMClass.classID(thisThang);
			if(((likeRoom!=null)&&(thisThang instanceof Room))
			  &&((((Room)thisThang).saveNum()!=0)&&(!((Room)thisThang).getArea().name().equals(likeRoom.getArea().name()))))
				list=null;
			if(list!=null)
			{
				if(++column>2)
				{
					lines.append("\r\n");
					column=1;
				}
				lines.append(CMStrings.padRight(list,37)+" ");
			}
		}
		lines.append("\r\n");
		return lines;
	}
	
	public StringBuilder fourColumns(Vector<String> reverseList)
	{ return fourColumns(reverseList,null);}
	public StringBuilder fourColumns(Vector<String> reverseList, String tag)
	{
		StringBuilder topicBuffer=new StringBuilder("");
		int col=0;
		String s=null;
		for(int i=0;i<reverseList.size();i++)
		{
			if((++col)>4)
			{
				topicBuffer.append("\r\n");
				col=1;
			}
			s=reverseList.get(i);
			/*if((tag!=null)&&(tag.length()>0))
				s="^<"+tag+"^>"+s+"^</"+tag+"^>";*/
			if(s.length()>18)
			{
				topicBuffer.append(CMStrings.padRight(s,(18*2)+1)+" ");
				++col;
			}
			else
				topicBuffer.append(CMStrings.padRight(s,18)+" ");
		}
		return topicBuffer;
	}
}
