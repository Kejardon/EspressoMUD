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
CoffeeMUD 5.6.2 copyright 2000-2010 Bo Zimmerman
EspressoMUD copyright 2011 Kejardon

Licensed under the Apache License, Version 2.0. You may obtain a copy of the license at
	http://www.apache.org/licenses/LICENSE-2.0
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

	public void executeMsg(ListenHolder.ExcChecker myHost, CMMsg msg)
	{
		super.executeMsg(myHost,msg);
		if((behaver instanceof Interactable)
		&&(msg.isTarget((Interactable)behaver))
		&&(msg.hasTargetCode(CMMsg.MsgCode.SNIFF))
		&&(smells!=null))
		{
			Vector emote=(Vector)smells.elementAt(CMath.random(smells.size()));
			Interactable emoter=null;
			if(behaver instanceof Room)
			{
				emoter=(Item)CMClass.Objects.ITEM.getNew("StdItem");
				for(MOB M : (MOB[])msg.source().toArray(new MOB[0]))
					emoteHere((Room)behaver,emoter,emote,M,false);
				emoter.destroy();
				return;
			}
			Room room=CMLib.map().roomLocation(behaver);
			if(room!=null)
			{
				if(behaver instanceof Interactable)
				{
					emoter=(MOB)behaver;
					emoteHere(room,emoter,emote,null,true);
				}
				else
				{
					emoter=(Item)CMClass.Objects.ITEM.getNew("StdItem");
					emoter.setName("something");
					emoteHere(room,emoter,emote,null,true);
					emoter.destroy();
				}
			}
		}
	}

	protected void emoteHere(Room room,
						   Interactable emoter,
						   Vector emote,
						   MOB emoteTo,
						   boolean Wrapper)
	{
		if(room==null) return;
		if(inroom.length()>0)
		{
			String ID=room.ID();
			if((ID.length()==0)
			||((!inroom.equals(ID))&&(!inroom.endsWith(ID))&&(inroom.indexOf(ID+";")<0)))
				return;
		}
		CMMsg msg;
		String str=(String)emote.elementAt(2);
		if(Wrapper) str="^E<S-NAME> "+str+" ^?";
		if(emoteTo!=null)
		{
			emoteTo.tell(emoter,emoteTo,null,str);
			return;
		}
		msg=CMClass.getMsg(emoter,null,null,EnumSet.of(CMMsg.MsgCode.EMOTE),str);
		if((room.okMessage(emoter,msg))&&(msg.handleResponses()))
			room.executeMsg(emoter,msg);
	}

	public boolean tick(Tickable ticking, Tickable.TickID tickID)
	{
		super.tick(ticking,tickID);
		parseEmotes();
		if((emotes.size()>0)
		&&(!CMSecurity.isDisabled("EMOTERS")))
		{
			if((expires>0)&&((--expires)==0))
			{
				behaver.delBehavior(this);
				return false;
			}
			Vector emote=(Vector)emotes.elementAt(CMath.random(emotes.size()));
			Interactable emoter=null;

			Room room=CMLib.map().roomLocation(behaver);
			boolean killEmoter=false;
			if(room==null) return true;
			if(behaver instanceof Interactable)
				emoter=(Interactable)ticking;
			else
			{
				emoter=(Item)CMClass.Objects.ITEM.getNew("StdItem");
				killEmoter=true;
				emoter.setName("something");
			}
			emoteHere(room,emoter,emote,null,true);
/*
			if(((Boolean)emote.elementAt(1)).booleanValue())
			{
				if(behaver instanceof MOB)
				{
					emoter=CMClass.Objects.ITEM.getNew("StdItem");
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
			} */
			if(killEmoter) emoter.destroy();
		}
		return true;
	}
}


