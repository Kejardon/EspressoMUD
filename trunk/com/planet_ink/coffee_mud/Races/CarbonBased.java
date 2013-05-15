package com.planet_ink.coffee_mud.Races;
import com.planet_ink.coffee_mud.core.interfaces.*;
import com.planet_ink.coffee_mud.core.*;
import com.planet_ink.coffee_mud.Libraries.*;
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
			Body body=mob.body();
			boolean didPrereqFlag=(commands.nextAct==1); //MOBEatFlag
			if(commands.nextAct==0 || didPrereqFlag)
				commands.nextAct=System.currentTimeMillis();
			Vector<Interactable> targets;
			if(commands.data==null)
			{
				String ID=CMParms.removeFirst(commands.cmdString);
				targets=CMLib.english().getTargets(mob, ID, "from", EnglishParser.SRCH_ALL, EnglishParser.SUB_ALL);
				if(targets==null) return false;
				commands.data=targets;
				//if(targets.size()==0)	//Shouldn't happen I think
			}
			else
			{
				targets=(Vector)commands.data;
				/*
				if(!mob.isMine(target)&&target.container()!=mob.location())
				{
					mob.tell("You can't reach "+target.name()+" anymore!");
					return false;
				}
				*/
			}
			//EatCode MOBEat=mob.getEat();
			//Vector<Item> failed=(MOBEat==null?null:new Vector());
			if(!didPrereqFlag)
			{
				ArrayList<MOB.QueuedCommand> prereqs=body.getEat().eatPrereqs(mob, body, targets);	//failed
				if(prereqs==null)
				{
					//if(MOBEat==null)
						return false;
					//prereqs=MOBEat.eatPrereqs(mob, body, failed, null);
					//if(prereqs==null) return false;
				}
				if(prereqs.size()>0)
				{
					commands.nextAct=1;
					/* Enque command at end.
					   I think proper way to do this is have eatPrereqs enque its commands, have this enque itself after those commands,
					   and then return false to remove the first instance of this command and immediately do the next command. */
					mob.enqueCommand(commands, prereqs.get(prereqs.size()-1));
					return false;
				}
			}
			//else check if prereqs exist. sendEat should check as well so that works instead I guess.
			
			long results=body.getEat().sendEat(mob, body, targets);
			//if((results==-1)||(results==0)) return false;
			if(results<=0) return false;
			
			/*
			boolean success=mob.location().doAndReturnMsg(CMClass.getMsg(mob,target,null,EnumSet.of(CMMsg.MsgCode.EAT),"^[S-NAME] eat^s ^[T-NAME]."));
			if(!success||target.amDestroyed()||mob.body().charStats().getPointsPercent(CharStats.Points.HUNGER)>=1)
				return false;
			commands.data=target;
			*/

			//commands.nextAct+=Tickable.TIME_TICK;
			commands.nextAct+=results;
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

	public long sendEat(MOB mob, Body body, Vector<Interactable> items)
	{
		if(items.size()==0) return 0;
		Item I=(Item)items.get(0);
		
		if(body.isComposite())
		{
			WVector<Race> myRaces=body.raceSet();
			Log.errOut("StdMOB",new RuntimeException("Incomplete code!"));	//TODO eventually
			mob.tell("Incomplete code!");
			return -1;
		}
		Race myRace=body.race();
		int amount=myRace.getBiteSize(body, I);
		boolean wholeBite;
		if(amount<0)
		{
			wholeBite=true;
			amount=-amount;
		}
		else
			wholeBite=false;
		CMMsg msg=CMClass.getMsg(mob,body,I,EnumSet.of(CMMsg.MsgCode.EAT),"^[S-NAME] eat^s ^[O-NAME].");
		msg.setValue(amount);
		boolean success=mob.location().doAndReturnMsg(msg);
		if(!success)
			return -1;
		
		if(body.charStats().getPointsPercent(CharStats.Points.HUNGER)>=1)
		{
			mob.tell("You're full now.");
			return 0;
		}
		
		if(!wholeBite) return Tickable.TIME_TICK*amount/myRace.getMaxBiteSize(body);
		return Tickable.TIME_TICK;
	}
	//Execute msg sort of code. Should not need any sanity checks.
	public boolean handleEat(CMMsg msg)
	{
		int amountToEat=msg.value();
		if(amountToEat == 0) return false;
		//if(!satisfyPrereqs(msg)) return false;
		Interactable target=msg.target();
		if(!(target instanceof Body)) return false;
		Body body=(Body)target;
		//Vector<CMObject> tools=msg.tool();
		//NOTE: Right now there is no way to eat multiple items at the same time so this may be changed if that is added.
		CMObject tool=msg.firstTool();
		if(!(tool instanceof Item)) return false;

		Item I=(Item)tool;
		Environmental E=I.getEnvObject();
		EnvStats env=E.baseEnvStats();
		/*
		int total=0;
		if(env.isComposite())
		{
			WVector<RawMaterial.Resource> mats=env.materialSet();
			for(int j=0;j<mats.size();j++)
				total+=(int)(diet(body, mats.get(j))*msg.value()*mats.pct(j));
		}
		else
		{
			total=diet(body, env.material())*msg.value();
		}
		*/
		
		applyDiet(body, (Item)tool, amountToEat);
		long remains=env.volume() - amountToEat;
		if(remains==0)
		{
			I.destroy();
		}
		else
		{
			env.setVolume(remains);
			env.recalcLengthsFromVolume();
			env.recalcWeightFromVolume();
			E.recoverEnvStats();
		}
		return true;
	}
	public void applyDiet(Body body, Item source, int volume)
	{
		if(source.isComposite())
		{
			Log.errOut(ID(),new RuntimeException("Incomplete code!"));	//TODO eventually
			MOB mob=body.mob();
			if(mob!=null)
				mob.tell("Incomplete code to handle "+source.name()+"!");
			return;
		}
		EnvStats env=source.getEnvObject().baseEnvStats();
		CharStats stats=body.charStats();
		if(env.isComposite())
		{
			WVector<RawMaterial.Resource> mats=env.materialSet();
			for(int j=0;j<mats.size();j++)
			{
				RawMaterial.Resource mat=mats.get(j);
				//float gain=diet(body, mat)*msg.value()*mats.pct(j);
				stats.adjPoints(CharStats.Points.HUNGER, (int)(volume*mats.pct(j)*nutrition(mat)));
				stats.adjPoints(CharStats.Points.HUNGER, (int)(volume*mats.pct(j)*waterContent(mat)));
			}
		}
		else
		{
			RawMaterial.Resource mat=env.material();
			//int gain=diet(body, mat)*msg.value();
			stats.adjPoints(CharStats.Points.HUNGER, (int)(volume*nutrition(mat)));
			stats.adjPoints(CharStats.Points.HUNGER, (int)(volume*waterContent(mat)));
		}
	}
	public int diet(Body body, RawMaterial.Resource mat)
	{
		/*
		switch(mat)
		{
		}
		*/
		switch(mat.material)
		{
			case UNKNOWN: return 0;
			case CLOTH: return 0;
			case LEATHER: return 0;
			case METAL: return -100;
			case MITHRIL: return -100;
			case WOODEN: return 0;
			case GLASS: return -100;
			case VEGETATION: return 100;
			case FLESH: return 100;
			case PAPER: return 0;
			case ROCK: return -100;
			case LIQUID: return 100;
			case PRECIOUS: return -100;
			case ENERGY: return -100;
			case PLASTIC: return -100;
		}
		return -100;
	}
	//Default to basic omnivore
	public float nutrition(RawMaterial.Resource mat)
	{
		/*
		switch(mat)
		{
		}
		*/
		switch(mat.material)
		{
			//case CLOTH: return 0.0;
			//case LEATHER: return 0.0;
			//case WOODEN: return 0.0;
			case VEGETATION: return (float)0.9;
			case FLESH: return (float)0.98;
			//case PAPER: return 0.0;
			//case LIQUID: return 0.0;
		}
		return (float)0.0;
	}
	public float waterContent(RawMaterial.Resource mat)
	{
		/*
		switch(mat)
		{
		}
		*/
		switch(mat.material)
		{
			//case CLOTH: return 0.0;
			//case LEATHER: return 0.0;
			//case WOODEN: return 0.0;
			case VEGETATION: return (float)0.1;
			case FLESH: return (float)0.02;
			//case PAPER: return 0.0;
			case LIQUID: return (float)1.0;
		}
		return (float)0.0;
	}
	public boolean satisfiesEatReqs(CMMsg msg)
	{
		Interactable target=msg.target();
		Body body=(target instanceof Body)?(Body)target:null;
		if(body==null) return false;
		CMObject toEat=msg.firstTool();
		Item I=(toEat instanceof Item)?(Item)toEat:null;
		if(I==null) return false;
		EnvStats env=I.getEnvObject().envStats();
		int total=0;
		if(env.isComposite())
		{
			WVector<RawMaterial.Resource> mats=env.materialSet();
			for(int j=0;j<mats.size();j++)
				total+=(int)(diet(body, mats.get(j))*mats.pct(j));
		}
		else
		{
			total=diet(body, env.material());
		}
		if(total<=-100)
		{
			/*
			Interactable source=msg.firstSource();
			MOB mob=(source instanceof MOB)?source:null;
			if(mob!=null)
				mob.tell(" simply cannot eat "+I.name()+".");
			*/
			return false;
		}
		return true;
	}
	public boolean satisfiesEatPrereqs(CMMsg msg)
	{
		Interactable source=msg.firstSource();
		MOB mob=(source instanceof MOB)?(MOB)source:null;
		if(mob==null) return false;
		Interactable target=msg.target();
		Body body=(target instanceof Body)?(Body)target:null;
		if(body==null) return false;
		if((mob.body()!=body) && (mob.location() != body.container()))
		{
			return false;
		}
		CMObject toEat=msg.firstTool();
		Item I=(toEat instanceof Item)?(Item)toEat:null;
		if(I==null) return false;
		if((!mob.getItemCollection().hasItem(I, true)) && (I.container()!=mob.location()))
		{
			return false;
		}
		return true;
	}
	public ArrayList<MOB.QueuedCommand> eatPrereqs(MOB mob, Body body, Vector<Interactable> items)
	{
		//TODO: Go to object. Pick up if needed?
		for(int i=0;i<items.size();i++)
		{
			Interactable thing=items.get(i);
			Item I = (thing instanceof Item)?(Item)thing:null;
			if(I==null) continue;
			if((!mob.getItemCollection().hasItem(I, true)) && (I.container()!=mob.location()))
			{
				items.remove(i);
				i--;
				continue;
			}
			if(I.isComposite())
			{
				Log.errOut(ID(),new RuntimeException("Incomplete code!"));	//TODO eventually
				mob.tell("Incomplete code to handle "+I.name()+"!");
				items.remove(i);
				i--;
				continue;
			}
			else
			{
				EnvStats env=I.getEnvObject().envStats();
				int total=0;
				if(env.isComposite())
				{
					WVector<RawMaterial.Resource> mats=env.materialSet();
					for(int j=0;j<mats.size();j++)
						total+=(int)(diet(body, mats.get(j))*mats.pct(j));
				}
				else
				{
					total=diet(body, env.material());
				}
				if(total<=0)
				{
					//NOTE: I am not sure how thread-safe a prompt here is, but it SHOULD be thread-safe if it isn't yet.
					String response;
					if(total<=-100)
					{
						response="N";
						mob.tell("You simply cannot eat "+I.name()+".");
					}
					else
					{
						Session S=mob.session();
						if(S==null)
							response="N";
						else
							response=S.newPrompt(I.name()+" doesn't look very appealing! Eat it anyways? (y/N)", 10000);
					}
					if(response.length()>0&&Character.toUpperCase(response.charAt(0))!='Y')
					{
						items.remove(i);
						i--;
						continue;
					}
				}
			}
			break;
		}
		if(items.size()==0) return null;
		return CMClass.emptyAL;
	}
	public int getMaxBiteSize(Body body)
	{
		//TODO eventually: Base bite size off of head size or something more specific
		return 10;
	}
	public int getBiteSize(Body body, Item source)
	{
		//TODO eventually: Base bite size off of head size or something more specific. Also source's hardness?
		long max=source.getEnvObject().envStats().volume();
		if(max<=10) return (int)-max;
		return 10;
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
