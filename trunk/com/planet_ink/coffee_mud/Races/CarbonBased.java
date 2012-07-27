package com.planet_ink.coffee_mud.Races;
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
import com.planet_ink.coffee_mud.Commands.StdCommand;

import java.util.*;

/*
CoffeeMUD 5.6.2 copyright 2000-2010 Bo Zimmerman
EspressoMUD copyright 2011 Kejardon

Licensed under the Apache License, Version 2.0. You may obtain a copy of the license at
	http://www.apache.org/licenses/LICENSE-2.0
*/
@SuppressWarnings("unchecked")
public class CarbonBased extends StdRace
{
	public String ID(){	return "CarbonBased"; }
	public String name(){ return "CarbonBased"; }
	public String racialCategory(){return "CarbonBased";}
	public void initializeClass()
	{
		if(ID()!="CarbonBased") return;
		Command newCommand=new Eat();
		CMClass.COMMAND.remove(newCommand);
		CMClass.COMMAND.add(newCommand);
		newCommand=new Drink();
		CMClass.COMMAND.remove(newCommand);
		CMClass.COMMAND.add(newCommand);
	}

	public static class Eat extends StdCommand
	{
		public Eat(){access=new String[]{"EAT"};}
		
		public boolean execute(MOB mob, MOB.QueuedCommand commands)
		{
			if(commands.nextAct==0)
				commands.nextAct=System.currentTimeMillis();
			Item target;
			if(commands.data==null)
			{
				String ID=CMParms.removeFirst(commands.cmdString);
				
				target=(Item)CMLib.english().fetchInteractable(ID, false, 1, mob.getItemCollection(), mob.location());
				if(target==null)
				{
					mob.tell("You don't see that here!");
					return false;
				}
			}
			else
			{
				target=(Item)commands.data;
				/*
				if(!mob.isMine(target)&&target.container()!=mob.location())
				{
					mob.tell("You can't reach "+target.name()+" anymore!");
					return false;
				}
				*/
			}
			
			boolean success=mob.location().doAndReturnMsg(CMClass.getMsg(mob,target,null,EnumSet.of(CMMsg.MsgCode.EAT),"^[S-NAME] eat^s ^[T-NAME]."));
			if(!success||target.amDestroyed()||mob.body().charStats().getPointsPercent(CharStats.Points.HUNGER)>=1)
				return false;
			
			commands.data=target;
			commands.nextAct+=Tickable.TIME_TICK;
			return true;
		}
		public int commandType(MOB mob, String cmds){return CT_LOW_P_ACTION;}
		public boolean securityCheck(MOB mob)
		{
			Body b=mob.body();
			return ((b!=null)&&(b.charStats().getPointsIndex(CharStats.Points.HUNGER)>=0));
		}
	}
	public static class Drink extends StdCommand
	{
		public Drink(){access=new String[]{"DRINK"};}
		
		public boolean execute(MOB mob, MOB.QueuedCommand commands)
		{
			if(commands.nextAct==0)
				commands.nextAct=System.currentTimeMillis();
			Item target;
			if(commands.data==null)
			{
				String ID=CMParms.removeFirst(commands.cmdString);
				
				target=(Item)CMLib.english().fetchInteractable(ID, false, 1, mob.getItemCollection(), mob.location());
				if(target==null)
				{
					mob.tell("You don't see that here!");
					return false;
				}
			}
			else
			{
				target=(Item)commands.data;
				/*
				if(!mob.isMine(target)&&target.container()!=mob.location())
				{
					mob.tell("You can't reach "+target.name()+" anymore!");
					return false;
				}
				*/
			}
			
			boolean success=mob.location().doAndReturnMsg(CMClass.getMsg(mob,target,null,EnumSet.of(CMMsg.MsgCode.DRINK),"^[S-NAME] drink^s ^[T-NAME]."));
			if(!success||target.amDestroyed()||mob.body().charStats().getPointsPercent(CharStats.Points.THIRST)>=1)
				return false;
			
			commands.data=target;
			commands.nextAct+=Tickable.TIME_TICK;
			return true;
		}
		public int commandType(MOB mob, String cmds){return CT_LOW_P_ACTION;}
		public boolean securityCheck(MOB mob)
		{
			Body b=mob.body();
			return ((b!=null)&&(b.charStats().getPointsIndex(CharStats.Points.THIRST)>=0));
		}
	}

	public int getBiteSize(Body body, Item source)
	{
		//TODO eventually: Base bite size off of head size or something more specific.
		return 5000;
	}
	protected static class RTInfo
	{
		Body body;
		CharStats sourceStats;
		double hunger;
		double thirst;
		double fatigue;
		double damage;
		public RTInfo(Body b, CharStats s, double h, double t, double f, double d)
		{ body=b; sourceStats=s; hunger=h; thirst=t; fatigue=f; damage=d; }
	}
	protected void focusRecover(RTInfo info, CharStats stats)
	{
		double focusThirst=info.thirst*4;	//Multiplying by 4 to make thirst only matter when dire.
		if(focusThirst>1) focusThirst=1;
		int current=stats.getPoints(CharStats.Points.FOCUS);
		int target=stats.getMaxPoints(CharStats.Points.FOCUS);
		if(target<1000) target=1000;
	
		current-=(1-focusThirst*info.fatigue)*200+(target-current)/4;
		if(current<target)
		{
			current+=5;
			if(current>target) current=target;
		}
		else if(current>target)
		{
			current-=5;
			if(current<target) current=target;
		}
	
		//TODO: Focus causing fatigue?
		stats.setPoints(CharStats.Points.FOCUS, current);
	}
	protected void fatigueRecover(RTInfo info, CharStats stats)
	{
		double myFatigue=stats.getPointsPercent(CharStats.Points.FATIGUE);
		if(myFatigue>1) myFatigue=1; else if(myFatigue<0) myFatigue=0;
		int con=stats.getStat(CharStats.Stat.CONSTITUTION);
		if(con<0) con=0;
		int str=stats.getStat(CharStats.Stat.STRENGTH);
		if(str<0) str=0;
		int thirstCost=(int)(5*(1-myFatigue)*info.thirst);
		int hungerCost=(int)(5*(1-myFatigue)*info.hunger);
		int fatigueRegen=(int)Math.round(5+(con+str)*(1-info.damage)*(2-myFatigue)*info.thirst*info.hunger);
		if(myFatigue+0.2<info.fatigue)
		{
			int temp=stats.getMaxPoints(CharStats.Points.FATIGUE)/200;
			fatigueRegen+=temp;
			info.sourceStats.adjPoints(CharStats.Points.FATIGUE, -temp);
		}
		info.sourceStats.adjPoints(CharStats.Points.THIRST, -thirstCost);
		info.sourceStats.adjPoints(CharStats.Points.HUNGER, -hungerCost);
		stats.adjPoints(CharStats.Points.FATIGUE, fatigueRegen);
	}
	protected void hitRecover(RTInfo info, CharStats stats)
	{
		int con=stats.getStat(CharStats.Stat.CONSTITUTION);
		if(con<0) con=0;
		int thirstCost=(int)(10*info.damage*info.thirst);
		int hungerCost=(int)(10*info.damage*info.hunger);
		int hpRegen=(int)Math.round(5+10*con*info.damage*info.hunger*info.thirst);
		info.sourceStats.adjPoints(CharStats.Points.THIRST, -thirstCost);
		info.sourceStats.adjPoints(CharStats.Points.HUNGER, -hungerCost);
		stats.adjPoints(CharStats.Points.HIT, hpRegen);
	}
	protected void manaRecover(RTInfo info, CharStats stats)
	{
		int wil=0;
		MOB M;
		if((M=info.body.mob())!=null)
		{
			wil=M.charStats().getStat(CharStats.Stat.WILLPOWER);
			if(wil<0) wil=0;
		}
		int mpRegen=1+2*wil;
		stats.adjPoints(CharStats.Points.MANA, mpRegen);
	}
	public void recoverTick(Body body, CharStats stats)
	{
		CharStats sourceStats=body.charStats();
		if(sourceStats==null) return;

		double hunger=sourceStats.getPointsPercent(CharStats.Points.HUNGER);
		if(hunger>1) hunger=1; else if(hunger<0) hunger=0;
		double thirst=sourceStats.getPointsPercent(CharStats.Points.THIRST);
		if(thirst>1) thirst=1; else if(thirst<0) thirst=0;
		double fatigue=sourceStats.getPointsPercent(CharStats.Points.FATIGUE);
		if(fatigue>1) fatigue=1; else if(fatigue<0) fatigue=0;
		double damage=1-sourceStats.getPointsPercent(CharStats.Points.HIT);
		if(damage>1) damage=1; else if(damage<0) damage=0;
		RTInfo info=new RTInfo(body, sourceStats, hunger, thirst, fatigue, damage);

		for(CharStats.Points type : stats.getPointOptions()) switch(type)
		{
			case FOCUS:
				focusRecover(info, stats);
				break;
			case FATIGUE:
				fatigueRecover(info, stats);
				break;
			case HIT:
				hitRecover(info, stats);
				break;
			case MANA:
				manaRecover(info, stats);
				break;
		}
	}
}
