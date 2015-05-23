package com.planet_ink.coffee_mud.MOBS;
import com.planet_ink.coffee_mud.core.interfaces.*;
import com.planet_ink.coffee_mud.core.*;
import com.planet_ink.coffee_mud.Libraries.*;
import com.planet_ink.coffee_mud.core.database.DBManager;
import com.planet_ink.coffee_mud.core.interfaces.CharStats.Stat;

import java.util.*;
import java.nio.ByteBuffer;
import java.util.concurrent.CopyOnWriteArrayList;
import java.io.IOException;


/*
CoffeeMUD 5.6.2 copyright 2000-2010 Bo Zimmerman
EspressoMUD copyright 2011 Kejardon

Licensed under the Apache License, Version 2.0. You may obtain a copy of the license at
	http://www.apache.org/licenses/LICENSE-2.0
*/

public class StdMOB extends AbstractSaveInteractable implements MOB
{
//	private static final Vector empty=new Vector();
	@Override protected int AI_ENABLES(){return AI_AFFECTS|AI_BEHAVES|AI_CHAR|AI_OK|AI_EXC|AI_TICK|AI_NAME;}

	@Override public String ID(){return "StdMOB";}
	@Override protected SIDLib.Objects SID(){return SIDLib.CREATURE;}
	//protected String name="";

	protected QueuedCommand currentCommand=null;
	protected ArrayList<QueuedCommand> commandQue=new ArrayList();
	protected long nextAct=0;
	//protected DVector commandQue=new DVector(6);

	protected CharStats baseCharStats=null;//(CharStats)((Ownable)CMClass.COMMON.getNew("MOBCharStats")).setOwner(this);
	protected CharStats charStats=null;//(CharStats)((Ownable)CMClass.COMMON.getNew("MOBCharStats")).setOwner(this);

	protected PlayerStats playerStats=null;

	protected Session mySession=null;
	protected Session myTempSession=null;

	protected Tickable.TickStat actStatus=Tickable.TickStat.Not;

	/* containers of items and attributes*/
	//protected EnumSet<ListenHolder.Flags> lFlags=EnumSet.of(ListenHolder.Flags.OK,ListenHolder.Flags.EXC,ListenHolder.Flags.TICK);
	//protected CopyOnWriteArrayList<CharAffecter> charAffecters=new CopyOnWriteArrayList();
	//protected CopyOnWriteArrayList<OkChecker> okCheckers=new CopyOnWriteArrayList();
	//protected CopyOnWriteArrayList<ExcChecker> excCheckers=new CopyOnWriteArrayList();
	//protected CopyOnWriteArrayList<TickActer> tickActers=new CopyOnWriteArrayList();
	//protected CopyOnWriteArrayList<Effect> affects=new CopyOnWriteArrayList();
	//protected CopyOnWriteArrayList<Behavior> behaviors=new CopyOnWriteArrayList();
	//protected int tickCount=0;
	//protected Tickable.TickStat tickStatus=Tickable.TickStat.Not;

	//protected EatCode myEatAction=null;//defaultEatCode;

	protected ItemCollection inventory=null;//(ItemCollection)((Ownable)CMClass.COMMON.getNew("DefaultItemCol")).setOwner(this);

	// gained attributes
	private double freeActions=0.0;

	// the core state values
	protected Interactable victim=null;
	//protected boolean amDestroyed=false;

	protected Vector<String> titles;
	protected Body myBody=null;

	protected Skilltable skillSet;

	//protected int saveNum=0;
	//protected int[] effectsToLoad=null;
	//protected int[] behavesToLoad=null;
	protected int bodyToLink=0;
	protected int itemCollectionToLoad=0;
	protected int playerStatsToLink=0;

	public StdMOB()
	{
		titles=new Vector();
		skillSet=new Skilltable();
	}
	protected StdMOB(StdMOB clone)
	{
		//Important note: Body is NOT cloned by this
		super(clone);
		if(clone.baseCharStats!=null)
		{
			baseCharStats=clone.baseCharStats.copyOf();
			charStats=(CharStats)CMClass.COMMON.getNew(baseCharStats.ID());
			baseCharStats.copyStatic(charStats);
		}
		//if(clone.inventory!=null) inventory=(ItemCollection)((Ownable)clone.inventory.copyOf()).setOwner(this);
		titles=(Vector<String>)clone.titles.clone();
		skillSet=(Skilltable)clone.skillSet.clone();
		
		//Don't set owner until all values have been set, so we don't save this during constructor
		baseCharStats.setOwner(this);
		charStats.setOwner(this);
	}

	/*
	public static final EatCode defaultEatCode=new EatCode()
	{
		public int eatPrereqs(MOB mob, Body body, Vector<Item> items)
		{
			//TODO: Figure out what to do for composite races.
			return body.race().eatPrereqs(mob, body, items);
		}
		public ArrayList<MOB.QueuedCommand> eatPrereqs(MOB mob, Body body, Vector<Item> items, Vector<Item> failed)
		{
			//TODO: Figure out what to do for composite races.
			return body.race().handleEat(mob, body, items, failed);
		}
	}
	*/
	//public EatCode getEat(){return myEatAction;}

	@Override public void initializeClass(){}
	@Override public StdMOB newInstance()
	{
		try{return getClass().newInstance();}catch(Exception e){Log.errOut(ID(),e);}
		return new StdMOB();
	}

	@Override public void setName(String newName)
	{
		String oldName=name;
		name=newName;
		if((playerStats!=null)&&(!CMLib.players().swapPlayer(this, oldName)))
			name=oldName;
		else
			CMLib.database().saveObject(this);
	}
	@Override public String name(){return name;}
	@Override public String titledName()
	{
		return name;
	}
	@Override public void setBody(Body newBody)
	{
		if(newBody!=null)
			newBody.setMob(this);
		myBody=newBody;
//		baseCharStats.setBody(newBody);
//		charStats.setBody(newBody);
		CMLib.database().saveObject(this);
	}
	@Override public Body body(){return myBody;}
	@Override public void setDescription(String S){}
	@Override public String description(){return "";}
	@Override public String plainDescription(){return "";}
	@Override public void setDisplayText(String S){}
	@Override public String displayText(){return "";}
	@Override public String plainDisplayText(){return "";}

	@Override public ItemCollection getItemCollection()
	{
		if(inventory==null) synchronized(this)
		{
			if(inventory==null)
			{
				inventory=(ItemCollection)((Ownable)CMClass.COMMON.getNew("DefaultItemCol")).setOwner(this);
				inventory.saveThis();
			}
		}
		return inventory;
	}
	@Override public Environmental getEnvObject()
	{
		if(myBody!=null)
			return myBody.getEnvObject();
		return null;
	}
	@Override public void setItemCollection(ItemCollection newInv, boolean copyInto)
	{
		ItemCollection oldInv=inventory;
		inventory=newInv;
		if(copyInto && oldInv!=null)
			newInv.copyFrom(oldInv);
		CMLib.database().saveObject(this);
	}

	//TODO: Put this in body and fix it.
	@Override public String genericName()
	{
//		if(charStats().age()>0)
//			return charStats().ageName().toLowerCase()+" "+charStats().raceName().toLowerCase();
//		return charStats().raceName().toLowerCase();
		return name;
	}
	@Override public int priority(ListenHolder L){return Integer.MAX_VALUE;}
	@Override public void registerListeners(ListenHolder forThis){}
	@Override public void registerAllListeners() {}
	@Override public void clearAllListeners() {}

	//public boolean amDestroyed(){return amDestroyed;}
	/*
	protected void cloneFix(StdMOB E)
	{
		super.cloneFix(E);
		
		commandQue=new ArrayList();
		nextAct=0;
		currentCommand=null;
		if(baseCharStats!=null)
		{
			setBaseCharStats((CharStats)baseCharStats.copyOf());
		}
		playerStats=null;
		mySession=null;
		myTempSession=null;
		//if(inventory!=null) inventory=(ItemCollection)((Ownable)inventory.copyOf()).setOwner(this);
		victim=null;
		titles=(Vector<String>)titles.clone();
	}
	*/

	@Override public StdMOB copyOf()
	{
		return new StdMOB(this);
		//E.saveNum=0;
		//E.cloneFix(this);
	}

	@Override public CharStats baseCharStats()
	{
		if(baseCharStats==null) synchronized(this){
			if(baseCharStats==null)
				setBaseCharStats((CharStats)CMClass.COMMON.getNew("MOBCharStats"));
		}

		return baseCharStats;
	}
	@Override public CharStats charStats()
	{
		if(charStats==null) synchronized(this){
			if(charStats==null)
				setBaseCharStats((CharStats)CMClass.COMMON.getNew("MOBCharStats"));
		}
		return charStats;
	}
	@Override public void recoverCharStats()
	{
		baseCharStats().copyStatic(charStats());
		for(CharAffecter A : charAffecters)
			A.affectCharStats(this,charStats);
		CMLib.database().saveObject(this);
	}
//	public void resetToMaxState() { charStats.resetState(); }

	@Override public void setBaseCharStats(CharStats newBaseCharStats)
	{
		baseCharStats=(CharStats)((Ownable)newBaseCharStats.copyOf()).setOwner(this);
		charStats=(CharStats)((Ownable)CMClass.COMMON.getNew(newBaseCharStats.ID())).setOwner(this);
		recoverCharStats();
		charStats.resetState();
	}
	//public void affectEnvStats(Environmental affected, EnvStats affectableStats) { }
	@Override public PlayerStats playerStats() { return playerStats; }
	@Override public void setPlayerStats(PlayerStats newStats) {
		playerStats=newStats;
		if(newStats!=null)
			newStats.setMOB(this);
		CMLib.database().saveObject(this);
	}
	@Override public MOBSkill getSkill(Skill S) { return skillSet.get(S); }
	@Override public Set<Skill> knownSkills() { return skillSet.keySet(); }
	@Override public boolean changeSkillState(Skill key, int state)
	{
		boolean result = skillSet.changeState(key, state);
		CMLib.database().saveObject(this);
		return result;
	}

	@Override public void destroy()
	{
		if(mySession!=null){ mySession.kill(false); try{Thread.sleep(1000);}catch(Exception e){} mySession=null;}
		if(inventory!=null)
			inventory.destroy();
		commandQue.clear();
		nextAct=0;
		victim=null;
		if((myBody!=null)&&(!myBody.amDestroyed())) myBody=null;
		super.destroy();
	}

	@Override public Interactable getVictim()
	{
		return victim;
	}

	//This almost definitely needs to be redone.
	@Override public void setVictim(Interactable target)
	{
		if(target==null)
		{
			if(victim!=null)
				synchronized(commandQue){commandQue.clear();}
		}
		if(victim==target) return;
		if(target==this) return;
		victim=target;
		if(target!=null)
		{
			if((location()==null)||((myBody!=null)&&(myBody.amDead())))
				victim=null;
		}
	}

	@Override public Room location()
	{
		return CMLib.map().roomLocation(myBody.container());
		/*
		if(myBody==null) return null;
		CMObject O=myBody.container();
		while((O!=null)&&!(O instanceof Room)&&(O instanceof Item))
			O=((Item)O).container();
		if(O instanceof Room)
			return (Room)O;
		return null;
		*/
	}
	@Override public void setLocation(Room newPlace)
	{
		if(myBody==null) return;
		myBody.setContainer(newPlace);
	}
	@Override public boolean goDistance(int[] distance, EnvMap.EnvLocation start, Room room)
	{
		//TODO. Instantaneous for now, needs to be gradual and depend on MOB's speed/move type eventually
		room.placeHere(myBody, true, distance[0]+start.x, distance[1]+start.y, distance[2]+start.z);
		return true;
	}
	//Eventually, return QueuedCommands? Or make this queue the commands itself?
	@Override public boolean goToThing(EnvMap.EnvLocation thing, EnvMap.EnvLocation start, Room room)
	{
		//TODO. Instantaneous for now, needs to be gradual and depend on MOB's speed/move type eventually
		if(thing.item instanceof ExitInstance)
		{
			ExitInstance map=(ExitInstance)thing.item;
			if(!com.planet_ink.coffee_mud.Commands.Go.move(this, location(), map, false, false)) return false;
		}
		else
			room.placeHere(myBody, true, thing.x, thing.y, thing.z);
		return true;
	}
	@Override public Session session()
	{
		if(myTempSession!=null) return myTempSession;
		return mySession;
	}
	@Override public void setSession(Session newSession)
	{
		mySession=newSession;
	}
	@Override public void setTempSession(Session newSession)
	{
		myTempSession=newSession;
	}

	@Override public String displayName(MOB viewer)
	{
		if((CMProps.Bools.INTRODUCTIONSYSTEM.property())
		&&(playerStats()!=null)
		&&(viewer!=null)
		&&(viewer.playerStats()!=null)
		&&(!viewer.playerStats().isIntroducedTo(this)))
			return CMLib.english().startWithAorAn(genericName()).toLowerCase();
		//TODO: return raceDisplayName() + " (" + name() + ")" + " (" + displayStatus + ")"
		return name();
	}

	@Override public double actions(){return freeActions;}
	@Override public void setActions(double remain){freeActions=remain;}
	@Override public int commandQueSize(){return commandQue.size();}
	@Override public void run()
	{
		actStatus=Tickable.TickStat.Start;
		QueuedCommand qCom=null;
		synchronized(commandQue)
		{
			if(commandQue.isEmpty()) return;
			qCom=commandQue.get(0);
			if((currentCommand!=null)&&(currentCommand!=qCom))
				if(!currentCommand.command.interruptCommand(currentCommand, qCom))
					commandQue.remove(currentCommand);
		}
		while(true)
		{
			currentCommand=qCom;
			actStatus=Tickable.TickStat.Listener;
			boolean continuedCommand=doCommand(qCom);
			actStatus=Tickable.TickStat.End;
			synchronized(commandQue)
			{
				if(!continuedCommand)
				{
					commandQue.remove(qCom);
					currentCommand=null;
					if(!commandQue.isEmpty())
					{
						qCom=commandQue.get(0);
						continue;
					}
				}
				else
				{
					//nextAct=qCom.nextAct;
					nextAct=commandQue.get(0).nextAct;	//This is qCom 99% of the time
					CMLib.threads().startTickDown(this);
				}
				break;
			}
		}
		actStatus=Tickable.TickStat.Not;
	}

	@Override public boolean doCommand(QueuedCommand qCom)
	{
		//Log.sysOut("MOB",""+Thread.currentThread()+" is doing "+qCom.command.ID()+" for "+mySession);
		int type=qCom.command.prompter();
		if((type!=0)&&((mySession==Thread.currentThread())||(false)))
		{
			mySession.handlePromptFor(new CommandCallWrap(this, qCom),type);
		}
		else try
		{
			return qCom.command.execute(this,qCom);
		}
		catch(Exception e)
		{
			Log.errOut("StdMOB",qCom.cmdString);
			Log.errOut("StdMOB",e);
			tell("Oops!");
		}
		return false;
	}

	/*public void doCommand(String commands, int metaFlags)
	{
		Command O=CMLib.english().findCommand(this,commands);
		if(O!=null)
			doCommand(O,commands, metaFlags);
		else
			CMLib.commands().handleUnknownCommand(this,commands);
	}
	protected void doCommand(Command O, String commands, int metaFlags)
	{
		if(O.prompter()&&mySession==Thread.currentThread())
			mySession.handlePromptFor(new CommandCallWrap(this, commands, metaFlags, (Command)O));
		else try
		{
			O.execute(this,commands, metaFlags);
		}
		catch(java.io.IOException io)
		{
			Log.errOut("StdMOB",commands);
			if(io.getMessage()!=null)
				Log.errOut("StdMOB",io.getMessage());
			else
				Log.errOut("StdMOB",io);
			tell("Oops!");
		}
		catch(Exception e)
		{
			Log.errOut("StdMOB",commands);
			Log.errOut("StdMOB",e);
			tell("Oops!");
		}
	}
	protected double calculateTickDelay(Object command, String commands, double tickDelay)
	{
		if(tickDelay<=0.0)
		{
			if(command==null){ tell("Huh?!"); return -1.0;}
			if(command instanceof Command)
				tickDelay=((Command)command).actionsCost(this,commands);
//			else if(command instanceof Effect)
//				tickDelay=((Effect)command).castingTime(this,commands);
			else
				tickDelay=1.0;
		}
		return tickDelay;
	}
	public void prequeCommand(String commands, int metaFlags)
	{
		if(commands==null) return;
		Command O=CMLib.english().findCommand(this,commands);
		if(O==null){ CMLib.commands().handleUnknownCommand(this,commands); return;}
		int commandType=commandType(O,commands,tickDelay);
		if((commandType==Command.CT_SYSTEM)||(commandType==Command.CT_NON_ACTION))
			doCommand(O,commands,metaFlags);
		else synchronized(commandQue)
		{
			long[] next=new long[1];
			next[0]=System.currentTimeMillis()-1;
			int[] seconds=new int[1];
			seconds[0]=-1;
			commandQue.insertRowAt(0,O,commands,Double.valueOf(tickDelay),next,seconds,Integer.valueOf(metaFlags));
		}
		dequeCommand();
	}*/
	@Override public long nextAct(){synchronized(commandQue){return nextAct;}}

	@Override public void enqueCommand(QueuedCommand qCom, boolean alwaysAtEnd)
	{
		synchronized(commandQue)
		{
			boolean mustStartTick=commandQue.isEmpty();
			if(!alwaysAtEnd && qCom.commandType==Command.CT_HIGH_P_ACTION)
			{
				commandQue.add(0, qCom);
				if((nextAct>System.currentTimeMillis()+100)&&(CMLib.threads().deleteTick(this)))
				{
					nextAct=qCom.nextAct;
					mustStartTick=true;
				}
			}
			else
				commandQue.add(qCom);
			if(mustStartTick)
				CMLib.threads().startTickDown(this);
		}
	}
	@Override public void enqueCommands(ArrayList<QueuedCommand> qCom, QueuedCommand afterCommand)
	{
		synchronized(commandQue)
		{
			int index=0;
			if(afterCommand!=null) index=commandQue.indexOf(afterCommand);
			if(index==-1) index=0;
			commandQue.addAll(index, qCom);
			if((index==0)&&(nextAct>System.currentTimeMillis()+100)&&(CMLib.threads().deleteTick(this)))
			{
				nextAct=qCom.get(0).nextAct;
				CMLib.threads().startTickDown(this);
			}
		}
	}
	@Override public void enqueCommand(QueuedCommand qCom, QueuedCommand afterCommand)
	{
		synchronized(commandQue)
		{
			int index=0;
			if(afterCommand!=null) index=commandQue.indexOf(afterCommand);
			if(index==-1) index=0;
			commandQue.add(index, qCom);
			if((index==0)&&(nextAct>System.currentTimeMillis()+100)&&(CMLib.threads().deleteTick(this)))
			{
				nextAct=qCom.nextAct;
				CMLib.threads().startTickDown(this);
			}
		}
	}

	@Override public void enqueCommand(String commands, int metaFlags)
	{
		if(commands==null) return;
		Command O=CMLib.english().findCommand(this,commands);
		if(O==null){ CMLib.commands().handleUnknownCommand(this,commands); return;}
		QueuedCommand qCom=O.prepCommand(this, commands, metaFlags);
		if(qCom==null) return;
		if((qCom.commandType==Command.CT_SYSTEM)||(qCom.commandType==Command.CT_NON_ACTION))
		{
			doCommand(qCom);
			return;
		}
		
		synchronized(commandQue)
		{
			boolean mustStartTick=commandQue.isEmpty();
			if(qCom.commandType==Command.CT_HIGH_P_ACTION)
			{
				commandQue.add(0, qCom);
				if((nextAct>System.currentTimeMillis()+100)&&(CMLib.threads().deleteTick(this)))
				{
					nextAct=qCom.nextAct;
					mustStartTick=true;
				}
			}
			else
				commandQue.add(qCom);
			if(mustStartTick)
				CMLib.threads().startTickDown(this);
		}
		//dequeCommand();
	}

	//public CopyOnWriteArrayList<CharAffecter> charAffecters(){return charAffecters;}
	//public CopyOnWriteArrayList<EnvAffecter> envAffecters(){return null;}
	//public CopyOnWriteArrayList<OkChecker> okCheckers(){return okCheckers;}
	//public CopyOnWriteArrayList<ExcChecker> excCheckers(){return excCheckers;}
	//public CopyOnWriteArrayList<TickActer> tickActers(){return tickActers;}
	@Override public void removeListener(Listener oldAffect, EnumSet flags)
	{
		ListenHolder.O.removeListener(this, oldAffect, flags);
	}
	@Override public void addListener(Listener newAffect, EnumSet flags)
	{
		ListenHolder.O.addListener(this, newAffect, flags);
	}
	//public EnumSet<ListenHolder.Flags> listenFlags() {return lFlags;}
/*	public void recheckListeners()
	{
		charAffecters=new Vector<CharAffecter>();
		envAffecters=new Vector<EnvAffecter>();
		okCheckers=new Vector<OkChecker>();
		excCheckers=new Vector<ExcChecker>();
		tickActers=new Vector<TickActer>();
		
		Effect effect=null;
		int num=numEffects();
		for(int a=0;a<num;a++)
		{
			effect=fetchEffect(a);
			if(effect!=null)
				effect.registerListeners(this);
		}
		Item item=null;
		num=numItems();
		for(int i=0;i<num;i++)
		{
			item=getItem(i);
			if(item!=null)
				item.registerListeners(this);
		}
		num=numBehaviors();
		for(int b=0;b<num;b++)
		{
			Behavior behav=fetchBehavior(b);
			if(behav!=null)
				behav.registerListeners(this);
		}
		if(location()!=null)
			location().registerListeners(this);
	}
*/
	@Override public boolean okMessage(ListenHolder.OkChecker myHost, CMMsg msg)
	{
		/*
		Interactable target=msg.target();
		for(CMMsg.MsgCode code : msg.othersCode())
		switch(code)
		{
		}
		*/
		for(OkChecker O : okCheckers)
			if(!O.okMessage(myHost,msg))
				return false;
		return true;
	}

	@Override public void tell(Interactable source, Interactable target, Vector<CMObject> tools, String msg)
	{
		CMObject tool=null;
		if(tools!=null&&tools.size()>0) tool=tools.get(0);
		if((mySession!=null)&&(msg!=null))
		{
			Session S=mySession;
			if(S!=null)
				S.stdPrintln(source,target,tool,msg);
		}
	}
	@Override public void tell(Interactable source, Interactable target, CMObject tool, String msg)
	{
		if((mySession!=null)&&(msg!=null))
		{
			Session S=mySession;
			if(S!=null)
				S.stdPrintln(source,target,tool,msg);
		}
	}
	@Override public void tell(Interactable source, Interactable target, String msg)
	{
		if((mySession!=null)&&(msg!=null))
		{
			Session S=mySession;
			if(S!=null)
				S.stdPrintln(source,target,null,msg);
		}
	}

	@Override public void tell(String msg)
	{
		tell(this,this,msg);
	}
	/*
	protected static ListenHolder.DummyListener EatResponse
	{
		@Override public boolean respondTo(CMMsg msg, Object data)
		{
			MOB mob=(StdMOB)data;
			Body body=mob.myBody;
			boolean always=msg.hasOthersCode(CMMsg.MsgCode.ALWAYS);
			if(body.isComposite())
			{
				WVector<Race> myRaces=body.raceSet();
				Log.errOut("StdMOB",new RuntimeException("Incomplete code!"));	//TODO eventually
				mob.tell("Incomplete code!");
				return false;
			}
			else
			{
				Race myRace=body.race();
				Vector<CMObject> food=msg.tool();
				if((food==null)||(food.size()==0))
				{
					mob.tell("There's nothing to eat!");
					return false;
				}
				boolean found=false;	//Anything that could possible be eaten is found.
				int totalWorth=0;	//'Score' for selection.
				int totalAmount=0;
				int maxAmount=myRace.getMaxBiteSize(body);
				for(CMObject obj : food)
				{
					if(!(obj instanceof Item)) continue;
					Item I=(Item)obj;
					if(I.isComposite())
					{
						Log.errOut("StdMOB",new RuntimeException("Incomplete code!"));	//TODO eventually
						mob.tell("Incomplete code!");
						//return false;
						continue;
					}
					Environmental env=Environmental.O.getFrom(I);
					if(env==null) continue;
					EnvStats stats=env.envStats();
					if(stats.isComposite())
					{
						Log.errOut("StdMOB",new RuntimeException("Incomplete code!"));	//TODO eventually
						mob.tell("Incomplete code!");
						continue;
					}
					int amount=myRace.getBiteSize(body, I);
					boolean wholeBite;
					if(amount<0)
					{
						wholeBite=true;
						amount=-amount;
					}
					else
						wholeBite=false;
					int worth=myRace.diet(body, stats.material());
					if(amount==0 || (found&&wholeBite&&worth<=0)) continue;
					if(!found&&worth<=0)
					{
						Session S=mob.mySession;
						if(S!=null)
						{
							String response=S.newPrompt("That doesn't look very appealing! Eat it anyways? (y/n)", 10000);
							if(response.length()>0&&Character.toUpperCase(response.charAt(0))=='Y')
								worth=0;
						}
					}
					found=true;
					totalWorth+=worth;
				}
				if(!found)
				{
					mob.tell("There's nothing to eat!");
					return false;
				}
				if(totalWorth<0)
				{
					Session S=mob.mySession;
					if(S!=null)
					{
						String response=S.newPrompt("That doesn't look very appealing! Eat it anyways? (y/n)", 10000);
						if(response.length()>0&&Character.toUpperCase(response.charAt(0))=='Y')
							totalWorth=0;
					}
				}
				if(totalWorth<0) return false;
				
				TODO NOW
			}
		}
	}
	protected static ListenHolder.DummyListener DrinkResponse
	{
		public void respondTo(CMMsg msg, Object data)
		{
			MOB mob=(MOB)data;
			boolean always=msg.hasOthersCode(CMMsg.MsgCode.ALWAYS);
			TODO NOW
		}
	}
	*/
	
	@Override public boolean respondTo(CMMsg msg, Object data){return true;}
	@Override public boolean respondTo(CMMsg msg) { return true; }
	@Override public void executeMsg(ListenHolder.ExcChecker myHost, CMMsg msg)
	{
		if(msg.othersMessage()!=null)
			tell(msg.firstSource(),msg.target(),msg.firstTool(),msg.othersMessage());
		for(ExcChecker O : excCheckers)
			O.executeMsg(myHost, msg);
	}

	//public void affectCharStats(MOB affectedMob, CharStats affectableStats){}

	//public Tickable.TickStat getTickStatus(){return tickStatus;}
	@Override public Tickable.TickStat getActStatus(){return actStatus;}
	//public int tickCounter(){return tickCount;}
	//public void tickAct(){}	//Currently unused in favor of run()

	//public long lastAct(){return lastAct;}	//No Action ticks
	//public long lastTick(){return lastTick;}

	@Override public boolean isMonster()
	{
		return playerStats==null;
	}

	//public int compareTo(CMObject o){ return CMClass.classID(this).compareToIgnoreCase(CMClass.classID(o));}

	@Override public Item fetchInventory(String itemName)
	{
		//Interactable[] items=getItemCollection().toArray(Interactable.dummyInteractableArray);
		Item item=(Item)CMLib.english().fetchInteractable(getItemCollection().allItems(),itemName,true);
		if(item==null) item=(Item)CMLib.english().fetchInteractable(inventory.allItems(),itemName,false);
		return item;
	}
	@Override public Vector<Item> fetchInventories(String itemName)
	{ 
		//Interactable[] items=getItemCollection().toArray(Interactable.dummyInteractableArray);
		Vector V=CMLib.english().fetchInteractables(getItemCollection().allItems(),itemName,true);
		if(V.size()>0) return V;
		V=CMLib.english().fetchInteractables(inventory.allItems(),itemName,false);
		return V;
	}

	@Override public boolean willFollowOrdersOf(MOB mob)
	{
		if(isMonster()&&CMSecurity.isAllowed(mob,"ORDER"))
			return true;
		if((!isMonster())
		&&(CMSecurity.isAllowed(mob,"ORDER"))
		&&((!CMSecurity.isASysOp(this))||CMSecurity.isASysOp(mob)))
			return true;
		return false;
	}

	@Override public void setActiveTitle(String S)
	{
		synchronized(titles)
		{
			if(titles.remove(S))
				titles.add(0, S);
		}
	}
	@Override public String getActiveTitle()
	{
		String s;
		synchronized(titles)
		{
			if(titles.size()==0) return null;
			s=titles.firstElement();
		}
		if((s.length()<2)||(s.charAt(0)!='{')||(s.charAt(s.length()-1)!='}'))
			return s;
		return s.substring(1,s.length()-1);
	}
	@Override public String[] getTitles()
	{
		return titles.toArray(CMClass.dummyStringArray);
	}
	@Override public void addTitle(String title){synchronized(titles){if(titles.contains(title)) return; titles.add(title);}}
	@Override public void removeTitle(String title){synchronized(titles){titles.remove(title);}}

	@Override public void giveItem(Item item)
	{
		if(item.container()!=null)
			ItemCollection.O.getFrom(item.container()).removeItem(item);
		getItemCollection().addItem(item);
	}

	@Override public boolean isMine(Interactable env)
	{
		if(env instanceof Item)
		{
			if(inventory==null) return false;
			return inventory.hasItem((Item)env, true);
		}
		return false;
	}

	//CMModifiable and CMSavable
	private static ModEnum[] totalEnumM=null;
	private static Enum[] headerEnumM=null;
	@Override public ModEnum[] totalEnumM()
	{
		if(totalEnumM==null) totalEnumM=CMParms.appendToArray(MCode.values(), super.totalEnumM(), ModEnum[].class);
		return totalEnumM;
	}
	@Override public Enum[] headerEnumM()
	{
		if(headerEnumM==null) headerEnumM=CMParms.appendToArray(new Enum[] {MCode.values()[0]}, super.headerEnumM(), Enum[].class);
		return headerEnumM;
	}
	private static SaveEnum[] totalEnumS=null;
	private static Enum[] headerEnumS=null;
	@Override public SaveEnum[] totalEnumS()
	{
		if(totalEnumS==null) totalEnumS=CMParms.appendToArray(SCode.values(), super.totalEnumS(), SaveEnum[].class);
		return totalEnumS;
	}
	@Override public Enum[] headerEnumS()
	{
		if(headerEnumS==null) headerEnumS=CMParms.appendToArray(new Enum[] {SCode.values()[0]}, super.headerEnumS(), Enum[].class);
		return headerEnumS;
	}
	//public boolean needLink(){return true;}
	@Override public void link()
	{
		super.link();
		if(bodyToLink!=0)
		{
			setBody((Body)SIDLib.ITEM.get(bodyToLink));
			bodyToLink=0;
		}
		if(itemCollectionToLoad!=0)
		{
			//TODO: Ideally original inventory is made only if needed...
			ItemCollection oldInventory=inventory;
			inventory = SIDLib.ITEMCOLLECTION.get(itemCollectionToLoad);
			if(inventory!=null)
			{
				((Ownable)inventory).setOwner(this);
				//Ideally never happens
				if(oldInventory!=null)
				{
					//getItemCollection();
					for(Iterator<Item> iter=oldInventory.allItems();iter.hasNext();)
					{
						Item next=iter.next();
						oldInventory.removeItem(next);
						next.setContainer(null);
						inventory.addItem(next);
					}
					oldInventory.destroy();
				}
			} else {
				inventory = oldInventory;
			}
			itemCollectionToLoad=0;
		}
		if(playerStatsToLink!=0)
		{
			AccountStats O=SIDLib.ACCOUNTSTATS.get(playerStatsToLink);
			if(O instanceof PlayerStats)
			{
				playerStats=(PlayerStats)O;
				CMLib.players().queuePlayer(this);
				playerStats.setMOB(this);
			}
			playerStatsToLink=0;
		}
	}
	//public void saveThis(){CMLib.database().saveObject(this);}
	@Override public void prepDefault(){getItemCollection();}

	@Override public void trainStat(Stat stat, CMMsg message) {
		Body body = myBody;
		if(body.isComposite())
		{
			WVector<Race> myRaces=body.raceSet();
			Log.errOut("StdMOB",new RuntimeException("Incomplete code!"));	//TODO eventually
			tell("Incomplete code!");
			return;
		}
		Race race=body.race();
		race.trainStat(body, stat, message);
		
	}

	private enum SCode implements SaveEnum<StdMOB>{
		BCS(){
			public ByteBuffer save(StdMOB E){ return CMLib.coffeeMaker().savSubFull(E.baseCharStats()); }
			public int size(){return -1;}
			public CMSavable subObject(StdMOB fromThis){return fromThis.baseCharStats();}
			public void load(StdMOB E, ByteBuffer S){ E.baseCharStats=(CharStats)((Ownable)CMLib.coffeeMaker().loadSub(S, E, this)).setOwner(E); } },
		CHS(){
			public ByteBuffer save(StdMOB E){ return CMLib.coffeeMaker().savSubFull(E.charStats()); }
			public int size(){return -1;}
			public CMSavable subObject(StdMOB fromThis){return fromThis.charStats();}
			public void load(StdMOB E, ByteBuffer S){ E.charStats=(CharStats)((Ownable)CMLib.coffeeMaker().loadSub(S, E, this)).setOwner(E); } },
		PLS(){
			public ByteBuffer save(StdMOB E){ return (ByteBuffer)ByteBuffer.wrap(new byte[4]).putInt(E.playerStats==null?0:E.playerStats.saveNum()).rewind(); }
			public int size(){return 4;}
			public void load(StdMOB E, ByteBuffer S){ E.playerStatsToLink=S.getInt(); } },
		INV(){
			public ByteBuffer save(StdMOB E){ return (ByteBuffer)ByteBuffer.wrap(new byte[4]).putInt(E.inventory==null?0:E.inventory.saveNum()).rewind(); }
			public int size(){return 4;}
			public void load(StdMOB E, ByteBuffer S){ E.itemCollectionToLoad=S.getInt(); } },
		TTL(){
			public ByteBuffer save(StdMOB E){ return CMLib.coffeeMaker().savAString((String[])E.titles.toArray(CMClass.dummyStringArray)); }
			public int size(){return 0;}
			public void load(StdMOB E, ByteBuffer S){ for(String newT : CMLib.coffeeMaker().loadAString(S)) E.titles.add(newT); } },
		BDY(){
			public ByteBuffer save(StdMOB E){
				ByteBuffer data=ByteBuffer.wrap(new byte[4]);
				if(E.myBody!=null) data.putInt(E.myBody.saveNum()).rewind();
				return data; }
			public int size(){return 4;}
			public void load(StdMOB E, ByteBuffer S){ E.bodyToLink=S.getInt(); } },
		SKL(){
			public ByteBuffer save(StdMOB E){
				if(E.skillSet.isEmpty()) return CoffeeMaker.emptyBuffer;
				Set<Skill> keys = E.skillSet.keySet();
				int size=0;
				for(Skill skill : keys)
					size+=9+skill.ID().length();
				ByteBuffer data=ByteBuffer.wrap(new byte[size]);
				for(Skill skill : keys) {
					MOBSkill skillData=E.skillSet.get(skill);
					data.putInt(skill.ID().length()).put(skill.ID().getBytes(DBManager.charFormat)).putInt(skillData.EXP).put((byte)skillData.learningState); }
				data.rewind();
				return data; }
			public int size(){return 0;}
			public void load(StdMOB E, ByteBuffer S){
				while(S.hasRemaining()) {
					byte[] stringBytes=new byte[S.getInt()];
					S.get(stringBytes);
					Skill skill=CMClass.SKILL.get(new String(stringBytes, DBManager.charFormat));
					int exp=S.getInt();
					byte state=S.get();
					if(skill!=null) E.skillSet.add(skill, exp, state); } } },
		;
		public CMSavable subObject(StdMOB fromThis){return null;} }
	private enum MCode implements ModEnum<StdMOB>{
		NAME(){
			public String brief(StdMOB E){return E.name;}
			public String prompt(StdMOB E){return E.name;}
			public void mod(StdMOB E, MOB M){
				String newName=CMLib.genEd().stringPrompt(M, ""+E.name, false);
				if(!E.name.equals(newName)) E.setName(newName); } },
		CHARSTATS(){
			public String brief(StdMOB E){return E.charStats().ID();}
			public String prompt(StdMOB E){return "";}
			public void mod(StdMOB E, MOB M){CMLib.genEd().genMiscSet(M, E.charStats);} },
		BASECHARSTATS(){
			public String brief(StdMOB E){return E.baseCharStats().ID();}
			public String prompt(StdMOB E){return "";}
			public void mod(StdMOB E, MOB M){CMLib.genEd().genMiscSet(M, E.baseCharStats);} },
		PLAYERSTATS(){
			public String brief(StdMOB E){return E.playerStats.ID();}
			public String prompt(StdMOB E){return "";}
			public void mod(StdMOB E, MOB M){CMLib.genEd().genMiscSet(M, E.playerStats);} },
		INVENTORY(){
			public String brief(StdMOB E){return E.getItemCollection().ID()+" "+E.inventory.numItems();}
			public String prompt(StdMOB E){return "";}
			public void mod(StdMOB E, MOB M){CMLib.genEd().genMiscSet(M, E.inventory);} },
		BODY(){
			public String brief(StdMOB E){return E.myBody==null?"null":E.myBody.ID();}
			public String prompt(StdMOB E){return "";}
			public void mod(StdMOB E, MOB M){CMLib.genEd().genMiscSet(M, E.myBody);} },
		TITLES(){
			public String brief(StdMOB E){return ""+E.titles.size();}
			public String prompt(StdMOB E){return E.titles.toString();}
			public void mod(StdMOB E, MOB M){
				while((M.session()!=null)&&(!M.session().killFlag())) {
					Vector V=(Vector)E.titles.clone();
					int i=CMLib.genEd().promptVector(M, V, true);
					if(--i<0) break;
					else if(i==V.size()) {
						String S=CMLib.genEd().stringPrompt(M, "", false).trim().toUpperCase();
						if((S.length()>0)&&(!E.titles.contains(S))) E.titles.add(S); }
					else if(i<V.size()) E.titles.remove(V.get(i)); } } },
		SKILLS(){
			public String brief(StdMOB E){return ""+E.skillSet.size();}
			public String prompt(StdMOB E){
				StringBuilder tellSkills=new StringBuilder();
				Set<Skill> skills = E.knownSkills();
				tellSkills.append("Learning:^^ Maintaining:* Forgetting:-\n");
				tellSkills.append("Knows the following skills:\n");
				for(Skill skill : skills) {
					MOB.MOBSkill instance = E.getSkill(skill);
					//TODO: Formatting in columns or something?
					tellSkills.append(skill.playerFriendlyName());
					tellSkills.append("(");
					tellSkills.append(skill.ID());
					tellSkills.append("): ");
					tellSkills.append(instance.level()).append('(').append(instance.EXP).append(" EXP)");
					tellSkills.append(com.planet_ink.coffee_mud.Commands.Skills.learnSymbols[instance.learningState]);
					tellSkills.append('\n'); }
				return tellSkills.toString(); }
			public void mod(StdMOB E, MOB M){
				while((M.session()!=null)&&(!M.session().killFlag())) {
					Skill skill = CMLib.genEd().skillPrompt(M);
					if(skill==null) break;
					String S = M.session().prompt("New EXP value for "+skill.playerFriendlyName()+": ");
					if(S.length()==0) continue;
					if(S.equals("0")) {
						MOBSkill instance = E.getSkill(skill);
						if(instance == null) continue;
						E.skillSet.put(skill, 0);
						if(instance.levelBoost == 0) E.skillSet.remove(skill);
					} else {
						int i=CMath.s_int(S);
						if(i<=0) continue;
						if(i>MOBSkill.levelTiers[skill.maxLevel()]) i=MOBSkill.levelTiers[skill.maxLevel()];
						E.skillSet.put(skill, i); } } } },
		QUEUE(){
			public String brief(StdMOB E){return ""+E.commandQue.size();}
			public String prompt(StdMOB E){return "";}
			public void mod(StdMOB E, MOB M){
				StringBuilder tellMob=new StringBuilder("Current Time: ");
				synchronized(E.commandQue) {
					tellMob.append(new Date().toString()).append("\r\n");
					for(QueuedCommand qCom : E.commandQue) {
						tellMob.append(qCom.command.ID()).append(" (").append(qCom.cmdString).append(")\r\n");
						tellMob.append(qCom.nextAct);
						if(qCom.nextAct>0) tellMob.append(new Date(qCom.nextAct).toString());
						tellMob.append("\r\n"); } }
				M.tell(tellMob.toString()); } },
		; }

	/*
	public boolean sameAs(Interactable E)
	{
		if(!(E instanceof StdMOB)) return false;
		return true;
	}
	*/
}
