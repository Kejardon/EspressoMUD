package com.planet_ink.coffee_mud.Behaviors;
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
public class Emoter extends ActiveTicker
{
	public String ID(){return "Emoter";}
	protected int expires=0;
	public Emoter()
	{
        super();
		minTicks=10;maxTicks=30;chance=50;expires=0;
		tickReset();
	}


	public void setParms(String newParms)
	{
		super.setParms(newParms);
		expires=CMParms.getParmInt(parms,"expires",0);
	    inroom=CMParms.getParmStr(parms,"inroom","").toUpperCase();
		emotes=null;
		smells=null;
	}

	protected Vector emotes=null;
	protected Vector smells=null;
	protected boolean broadcast=false;
	protected String inroom="";

	protected final static int EMOTE_VISUAL=0;
	protected final static int EMOTE_SOUND=1;
	protected final static int EMOTE_SMELL=2;
    protected final static int EMOTE_SOCIAL=3;
	protected int emoteType=0;

    protected boolean setEmoteType(String str)
    {
        str=str.toUpperCase().trim();
        if(str.equals("BROADCAST"))
            broadcast=true;
        else
        if(str.equals("NOBROADCAST"))
            broadcast=false;
        else
        if(str.equals("VISUAL")||(str.equals("SIGHT")))
            emoteType=EMOTE_VISUAL;
        else
        if(str.equals("AROMA")||(str.equals("SMELL")))
            emoteType=EMOTE_SMELL;
        else
        if(str.equals("SOUND")||(str.equals("NOISE")))
            emoteType=EMOTE_SOUND;
        else
        if(str.equals("SOCIAL"))
            emoteType=EMOTE_SOCIAL;
        else
            return false;
        return true;
    }
    protected void setEmoteTypes(Vector V, boolean respectOnlyBeginningAndEnd)
	{
        if(respectOnlyBeginningAndEnd)
        {
            if(setEmoteType((String)V.firstElement()))
                V.removeElementAt(0);
            else
            if(setEmoteType((String)V.lastElement()))
                V.removeElementAt(V.size()-1);
        }
        else
		for(int v=V.size()-1;v>=0;v--)
		{
            if(setEmoteType((String)V.elementAt(v)))
                V.removeElementAt(v);
		}
	}

    protected Vector parseEmotes()
	{
		if(emotes!=null) return emotes;
		broadcast=false;
		emoteType=EMOTE_VISUAL;
		emotes=new Vector();
		String newParms=getParms();
		char c=';';
		int x=newParms.indexOf(c);
		if(x<0){ c='/'; x=newParms.indexOf(c);}
		if(x>0)
		{
			String oldParms=newParms.substring(0,x);
			setEmoteTypes(CMParms.parse(oldParms),false);
			newParms=newParms.substring(x+1);
		}
		int defaultType=emoteType;
		boolean defaultBroadcast=broadcast;
		while(newParms.length()>0)
		{
			Vector thisEmoteV=new Vector();
			String thisEmote=newParms;
			x=newParms.indexOf(";");
			if(x<0)
				newParms="";
			else
			{
				thisEmote=newParms.substring(0,x);
				newParms=newParms.substring(x+1);
			}
			if(thisEmote.trim().length()>0)
			{
				Vector V=CMParms.parse(thisEmote);
				emoteType=defaultType;
				broadcast=defaultBroadcast;
				setEmoteTypes(V,true);
				thisEmote=CMParms.combine(V,0);
				if(thisEmote.length()>0)
				{
					thisEmoteV.addElement(Integer.valueOf(emoteType));
					thisEmoteV.addElement(Boolean.valueOf(broadcast));
					thisEmoteV.addElement(thisEmote);
					if(emoteType==EMOTE_SMELL)
					{
					    if(smells==null) smells=new Vector();
					    smells.addElement(thisEmoteV);
					}
					emotes.addElement(thisEmoteV);
				}
			}
		}
		return emotes;
	}

	public void executeMsg(Environmental myHost, CMMsg msg)
	{
		super.executeMsg(myHost,msg);
		if((msg.amITarget(myHost))
		&&(msg.targetMinor()==CMMsg.TYP_SNIFF)
		&&(CMLib.flags().canSmell(msg.source()))
		&&(smells!=null))
		{
		    Vector emote=(Vector)smells.elementAt(CMLib.dice().roll(1,smells.size(),-1));
	        MOB emoter=null;
			if(myHost instanceof Room)
			{
				emoter=CMClass.getMOB("StdMOB");
				emoteHere((Room)myHost,emoter,emote,msg.source(),false);
                emoter.destroy();
				return;
			}
			Room room=getBehaversRoom(myHost);
			if(room!=null)
			{
				if(myHost instanceof MOB)
                {
					emoter=(MOB)myHost;
                    emoteHere(room,emoter,emote,null,true);
                }
				else
				{
					if((myHost instanceof Item)&&(!CMLib.flags().isInTheGame(myHost,false)))
						return;
					emoter=CMClass.getMOB("StdMOB");
					emoter.setName(myHost.name());
                    emoteHere(room,emoter,emote,null,true);
                    emoter.destroy();
				}
			}
		}
	}

    protected void emoteHere(Room room,
	        			   MOB emoter,
	        			   Vector emote,
	        			   MOB emoteTo,
	        			   boolean Wrapper)
	{
		if(room==null) return;
		if(inroom.length()>0)
		{
		    String ID=CMLib.map().getExtendedRoomID(room);
		    if((ID.length()==0)
		    ||((!inroom.equals(ID))&&(!inroom.endsWith(ID))&&(inroom.indexOf(ID+";")<0)))
		        return;
		}
		CMMsg msg;
		Room oldLoc=emoter.location();
		String str=(String)emote.elementAt(2);
		if(emoter.location()!=room) emoter.setLocation(room);
        if(((Integer)emote.elementAt(0)).intValue()==EMOTE_SOCIAL)
        {
            Social S=CMLib.socials().fetchSocial(str,true);
            if(S==null) S=CMLib.socials().fetchSocial(str,false);
            if(S!=null)
            {
                S.invoke(emoter,CMParms.parse(str),emoteTo,false);
                return;
            }
        }
        if(Wrapper) str="^E<S-NAME> "+str+" ^?";
		if(emoteTo!=null)
		{
		    emoteTo.tell(emoter,emoteTo,null,str);
		    return;
		}
        msg=CMClass.getMsg(emoter,null,CMMsg.MSG_EMOTE,str);
		if(room.okMessage(emoter,msg))
		for(int i=0;i<room.numInhabitants();i++)
		{
			MOB M=room.fetchInhabitant(i);
			if((M!=null)&&(!M.isMonster()))
			switch(((Integer)emote.elementAt(0)).intValue())
			{
			case EMOTE_VISUAL:
				if(CMLib.flags().canBeSeenBy(emoter,M))	M.executeMsg(M,msg);
				break;
			case EMOTE_SOUND:
				if(CMLib.flags().canBeHeardBy(emoter,M)) M.executeMsg(M,msg);
				break;
			case EMOTE_SMELL:
				if(CMLib.flags().canSmell(M)) M.executeMsg(M,msg);
				break;
			}
		}
		if(oldLoc!=null) emoter.setLocation(oldLoc);
	}

	public boolean tick(Tickable ticking, int tickID)
	{
		super.tick(ticking,tickID);
		parseEmotes();
		if((canAct(ticking,tickID))
		&&(emotes.size()>0)
		&&(!CMSecurity.isDisabled("EMOTERS")))
		{
			if((expires>0)&&((--expires)==0))
			{
				if(ticking instanceof Environmental)
					((Environmental)ticking).delBehavior(this);
				return false;
			}
			Vector emote=(Vector)emotes.elementAt(CMLib.dice().roll(1,emotes.size(),-1));
			MOB emoter=null;
			if(ticking instanceof Area)
			{
				emoter=CMClass.getMOB("StdMOB");
				for(Enumeration r=((Area)ticking).getMetroMap();r.hasMoreElements();)
				{
					Room R=(Room)r.nextElement();
					emoteHere(R,emoter,emote,null,false);
				}
                emoter.destroy();
				return true;
			}
			if(ticking instanceof Room)
			{
				emoter=CMClass.getMOB("StdMOB");
				emoteHere((Room)ticking,emoter,emote,null,false);
                emoter.destroy();
				return true;
			}

			Room room=getBehaversRoom(ticking);
			if(room==null) return true;
            boolean killEmoter=false;
			if(ticking instanceof MOB)
			{
				if(canFreelyBehaveNormal(ticking))
					emoter=(MOB)ticking;
			}
			else
			{
				if((ticking instanceof Item)&&(!CMLib.flags().isInTheGame((Item)ticking,false)))
					return true;

				emoter=CMClass.getMOB("StdMOB");
                killEmoter=true;
				MOB mob=getBehaversMOB(ticking);
				String name=ticking.name();
				if(ticking instanceof Environmental)
					name=((Environmental)ticking).name();
				if(mob!=null)
				{
					if(!CMLib.flags().isInTheGame(mob,false))
						emoter.setName(name+" carried by "+mob.name());
					else
						emoter=null;
				}
				else
					emoter.setName(name);
			}
            if(emoter==null) return true;
            emoteHere(room,emoter,emote,null,true);

			if(((Boolean)emote.elementAt(1)).booleanValue())
			{
				if(ticking instanceof MOB)
                {
					emoter=CMClass.getMOB("StdMOB");
                    killEmoter=true;
                }
				for(int d=Directions.NUM_DIRECTIONS()-1;d>=0;d--)
				{
					Room R=room.getRoomInDir(d);
					Exit E=room.getExitInDir(d);
					if((R!=null)&&(E!=null)&&(E.isOpen()))
					{
						emoter.setName("something "+Directions.getInDirectionName(Directions.getOpDirectionCode(d)));
						emoteHere(R,emoter,emote,null,true);
					}
				}
			}
            if(killEmoter) emoter.destroy();
		}
		return true;
	}
}


