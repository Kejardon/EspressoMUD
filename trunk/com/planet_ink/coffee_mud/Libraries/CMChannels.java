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
//TODO: This looks like it needs to be redone more sensibly and efficiently sometime
@SuppressWarnings("unchecked")
public class CMChannels extends StdLibrary implements ChannelsLibrary
{
	public String ID(){return "CMChannels";}
	public final int QUEUE_SIZE=100;

	//public int numChannelsLoaded=0;
	public int numIChannelsLoaded=0;
	public String[] channelNames=CMClass.dummyStringArray;
	public String[] channelMasks=CMClass.dummyStringArray;
	public HashSet<ChannelFlag>[] channelFlags=CMClass.dummyHashSetArray;
	public String[] ichannelList=CMClass.dummyStringArray;
	public Vector<String>[] channelQue=CMClass.dummyVectorArray;
	//public final Vector emptyVector=new Vector(1);
	public final HashSet<ChannelFlag> emptyFlags=new HashSet<ChannelFlag>(1);
	protected final static String[] channelFlagArray=CMParms.toStringArray(ChannelFlag.values());

	public int getNumChannels()
	{
		return channelNames.length;
	}

	public String getChannelMask(int i)
	{
		if((i>=0)&&(i<channelMasks.length))
			return channelMasks[i];
		return "";
	}

	public HashSet<ChannelFlag> getChannelFlags(int i)
	{
		if((i>=0)&&(i<channelFlags.length))
			return channelFlags[i];
		return emptyFlags;
	}

	public String getChannelName(int i)
	{
		if((i>=0)&&(i<channelNames.length))
			return channelNames[i];
		return "";
	}

	public Vector<String> getChannelQue(int i)
	{
		if((i>=0)&&(i<channelQue.length))
			return channelQue[i];
		return new Vector();
	}

	public boolean mayReadThisChannel(MOB M, int i)
	{
		if(((M==null)||(M.playerStats()==null))
		  ||(i>=getNumChannels()))
			return false;

		if((CMLib.masking().maskCheck(getChannelMask(i),M,true))
		&&((M.playerStats().getChannelMask()&(1<<i))==0))
			return true;
		return false;
	}

	public boolean mayReadThisChannel(Session ses, int i)
	{
		if((ses==null)||(!ses.killFlag()))
			return false;
		return mayReadThisChannel(ses.mob(), i);
	}

	public void channelQueUp(int i, String msg)
	{
		//TODO
		//CMLib.map().sendGlobalMessage(null,EnumSet.of(CMMsg.MsgCode.CHANNEL),msg);
		Vector<String> q=getChannelQue(i);
		synchronized(q)
		{
			if(q.size()>=QUEUE_SIZE)
				q.remove(0);
			q.add(msg);
		}
	}

	public int getChannelIndex(String channelName)
	{
		channelName=channelName.toUpperCase();
		String[] channelNames=this.channelNames;
		for(int c=0;c<channelNames.length;c++)
			if(channelNames[c].startsWith(channelName))
				return c;
		return -1;
	}

	public int getChannelCodeNumber(String channelName)
	{
		channelName=channelName.toUpperCase();
		for(int c=0;c<channelNames.length;c++)
			if(channelNames[c].startsWith(channelName))
				return 1<<c;
		return -1;
	}

	public String getChannelName(String channelName)
	{
		channelName=channelName.toUpperCase();
		for(String S : channelNames)
			if(S.startsWith(channelName))
				return S.toUpperCase();
		return "";
	}

	public Vector<String> getFlaggedChannelNames(ChannelFlag flag)
	{
		Vector<String> channels=new Vector();
		for(int c=0;c<channelNames.length;c++)
			if(channelFlags[c].contains(flag))
				channels.add(channelNames[c].toUpperCase());
		return channels;
	}

	public String getExtraChannelDesc(String channelName)
	{
		StringBuilder str=new StringBuilder("");
		int dex = getChannelIndex(channelName);
		if(dex >= 0)
		{
			HashSet<ChannelFlag> flags = getChannelFlags(dex);
			String mask = getChannelMask(dex);
			if(flags.contains(ChannelFlag.PLAYERREADONLY)||flags.contains(ChannelFlag.READONLY))
				str.append(" This channel is read-only.");
			if((mask!=null)&&(mask.trim().length()>0))
				str.append(" The following may read this channel : "+CMLib.masking().maskDesc(mask));
		}
		return str.toString();
	}

	private void clearChannels()
	{
		//numChannelsLoaded=0;
		numIChannelsLoaded=0;
		channelNames=CMClass.dummyStringArray;
		channelMasks=CMClass.dummyStringArray;
		channelFlags=CMClass.dummyHashSetArray;
		ichannelList=CMClass.dummyStringArray;
		channelQue=CMClass.dummyVectorArray;
	}

	public boolean shutdown()
	{
		clearChannels();
		return true;
	}

	public String[][] iChannelsArray()
	{
		if(channelNames.length==0) return new String[0][0];
		String[][] array=new String[numIChannelsLoaded][4];
		int num=0;
		for(int i=0;i<channelNames.length;i++)
		{
			String iname=ichannelList[i].trim();
			if(iname.length()>0)
			{
				array[num][0]=iname;
				array[num][1]=channelNames[i].trim();
				array[num][2]=channelMasks[i];
				array[num][3]=CMParms.combine(channelFlags[i],0);
				num++;
			}
		}
		return array;
	}
	public String[] getChannelNames()
	{
		return (String[])channelNames.clone();
	}

	public String parseOutFlags(String mask, HashSet<ChannelFlag> flags)
	{
		Vector<String> V=CMParms.parse(mask);
		for(int v=V.size()-1;v>=0;v--)
		{
			String s=V.get(v).toUpperCase();
			if(CMParms.contains(channelFlagArray, s))
			{
				V.remove(v);
				flags.add(ChannelFlag.valueOf(s));
			}
		}
		return CMParms.combine(V,0);
	}

	public int loadChannels(String list, String ilist)
	{
		int numChannelsLoaded=0;
		clearChannels();
		ArrayList<String> iList=new ArrayList();
		ArrayList<String> names=new ArrayList();
		ArrayList<String> masks=new ArrayList();
		ArrayList<HashSet<ChannelFlag>> flagSets=new ArrayList();
		while(list.length()>0)
		{
			int x=list.indexOf(",");

			String item=null;
			if(x<0)
			{
				item=list.trim();
				list="";
			}
			else
			{
				item=list.substring(0,x).trim();
				list=list.substring(x+1);
			}
			numChannelsLoaded++;
			x=item.indexOf(" ");
			HashSet<ChannelFlag> flags=new HashSet<ChannelFlag>();
			if(x>0)
			{
				masks.add(parseOutFlags(item.substring(x+1).trim(),flags));
				item=item.substring(0,x);
			}
			else
				masks.add("");
			iList.add("");
			names.add(item.toUpperCase().trim());
			flagSets.add(flags);
		}
		while(ilist.length()>0)
		{
			int x=ilist.indexOf(",");

			String item=null;
			if(x<0)
			{
				item=ilist.trim();
				ilist="";
			}
			else
			{
				item=ilist.substring(0,x).trim();
				ilist=ilist.substring(x+1);
			}
			int y1=item.indexOf(" ");
			int y2=item.lastIndexOf(" ");
			if((y1<0)||(y2<=y1)) continue;
			numChannelsLoaded++;
			numIChannelsLoaded++;
			String lvl=item.substring(y1+1,y2).trim();
			String ichan=item.substring(y2+1).trim();
			item=item.substring(0,y1);
			names.add(item.toUpperCase().trim());
			HashSet<ChannelFlag> flags=new HashSet<ChannelFlag>();
			masks.add(parseOutFlags(lvl,flags));
			flagSets.add(flags);
			iList.add(ichan);
		}

		channelQue=new Vector[numChannelsLoaded];
		for(int i=0;i<numChannelsLoaded;i++)
			channelQue[i]=new Vector();
		channelMasks=masks.toArray(CMClass.dummyStringArray);
		channelFlags=flagSets.toArray(CMClass.dummyHashSetArray);
		ichannelList=iList.toArray(CMClass.dummyStringArray);
		channelNames=names.toArray(CMClass.dummyStringArray);

		return numChannelsLoaded;
	}

	public boolean channelTo(Session ses, int channelInt, String msg)
	{
		MOB M=ses.mob();
		if(mayReadThisChannel(ses,channelInt))
		{
			ses.print(msg);
			return true;
		}
		return false;
	}

	public void reallyChannel(MOB mob, String channelName, String message, boolean systemMsg)
	{
		int channelInt=getChannelIndex(channelName);
		if(channelInt<0) return;

		HashSet<ChannelFlag> flags=getChannelFlags(channelInt);
		channelName=getChannelName(channelInt);
		
		//<CHANNEL "name">[name] message</CHANNEL> if MXP is enabled
		String msg=null;
		if(systemMsg)
		{
			if((mob!=null)&&(!mob.name().startsWith("^"))||(mob.name().length()>2))
				msg="^Q"+mob.name()+" "+"["+channelName+"] '"+message+"'^?^.";
			else
				msg="^Q["+channelName+"] '"+message+"'^?^.";
		}
		else
		if(message.startsWith(",")
		||(message.startsWith(":")
			&&(message.length()>1)
			&&(Character.isLetter(message.charAt(1))||message.charAt(1)==' ')))
		{
			msg="^Q["+channelName+"] "+mob.name()+" "+message.substring(1).trim()+"^.";
		}
		else
			msg="^Q"+mob.name()+" "+channelName+"S '"+message+"'^.";
		channelQueUp(channelInt,msg);
		for(Session ses : CMLib.sessions().toArray())
			channelTo(ses,channelInt,msg);
	}
/*
	public Vector clearInvalidSnoopers(Session mySession, int channelCode)
	{
		Vector invalid=null;
		if(mySession!=null)
		{
			Session S=null;
			for(int s=0;s<CMLib.sessions().size();s++)
			{
				S=CMLib.sessions().elementAt(s);
				if((S!=mySession)
				&&(S.mob()!=null)
				&&(mySession.amBeingSnoopedBy(S))
				&&(!mayReadThisChannel(S.mob(),channelCode,false)))
				{
					if(invalid==null) invalid=new Vector();
					invalid.add(S);
					mySession.stopBeingSnoopedBy(S);
				}
			}
		}
		return invalid;
	}
	public void restoreInvalidSnoopers(Session mySession, Vector invalid)
	{
		if((mySession==null)||(invalid==null)) return;
		for(int s=0;s<invalid.size();s++)
			mySession.startBeingSnoopedBy((Session)invalid.elementAt(s));
	}
*/
}