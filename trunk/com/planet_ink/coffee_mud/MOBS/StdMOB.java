package com.planet_ink.coffee_mud.MOBS;
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
import java.io.IOException;


/*
CoffeeMUD 5.6.2 copyright 2000-2010 Bo Zimmerman
EspressoMUD copyright 2011 Kejardon

Licensed under the Apache License, Version 2.0. You may obtain a copy of the license at
	http://www.apache.org/licenses/LICENSE-2.0
*/
@SuppressWarnings("unchecked")
public class StdMOB implements MOB
{
//	private static final Vector empty=new Vector();

	public String ID(){return "StdMOB";}
	protected String name="";
	protected String plainName;
	protected String plainNameOf;

	protected QueuedCommand currentCommand=null;
	protected ArrayList<QueuedCommand> commandQue=new ArrayList();
	protected long nextAct=0;
	//protected DVector commandQue=new DVector(6);

	protected CharStats baseCharStats=(CharStats)((Ownable)CMClass.COMMON.getNew("MOBCharStats")).setOwner(this);
	protected CharStats charStats=(CharStats)((Ownable)CMClass.COMMON.getNew("MOBCharStats")).setOwner(this);

	protected PlayerStats playerStats=null;

//	protected boolean amDead=false;

	protected Session mySession=null;
	protected Session myTempSession=null;
//	protected boolean pleaseDestroy=false;

	protected Tickable.TickStat actStatus=Tickable.TickStat.Not;
	//protected long lastTick=0;
	//protected long lastAct=0;

	/* containers of items and attributes*/
	protected EnumSet<ListenHolder.Flags> lFlags=EnumSet.of(ListenHolder.Flags.OK,ListenHolder.Flags.EXC,ListenHolder.Flags.TICK);
	protected CopyOnWriteArrayList<CharAffecter> charAffecters=new CopyOnWriteArrayList();
	protected CopyOnWriteArrayList<OkChecker> okCheckers=new CopyOnWriteArrayList();
	protected CopyOnWriteArrayList<ExcChecker> excCheckers=new CopyOnWriteArrayList();
	protected CopyOnWriteArrayList<TickActer> tickActers=new CopyOnWriteArrayList();
	protected CopyOnWriteArrayList<Effect> affects=new CopyOnWriteArrayList();
	protected CopyOnWriteArrayList<Behavior> behaviors=new CopyOnWriteArrayList();
	protected int tickCount=0;
	protected Tickable.TickStat tickStatus=Tickable.TickStat.Not;

	protected ItemCollection inventory=null;//(ItemCollection)((Ownable)CMClass.COMMON.getNew("DefaultItemCol")).setOwner(this);

	// gained attributes
	private double freeActions=0.0;

	// the core state values

//	protected Room startRoomPossibly=null;
//	protected int WimpHitPoint=0;
	protected Interactable victim=null;
	protected boolean amDestroyed=false;
//	protected boolean kickFlag=false;
//	protected boolean imMobile=false;

	protected Vector<String> titles=new Vector();
	protected Body myBody=null;

	protected int saveNum=0;
	protected int[] effectsToLoad=null;
	protected int[] behavesToLoad=null;
	protected int bodyToLink=0;
	protected int itemCollectionToLoad=0;
	protected int playerStatsToLink=0;

	public StdMOB()
	{
//		((Ownable)inventory).setOwner(this);
	}

	public void initializeClass(){}
	public CMObject newInstance()
	{
		try
		{
			return (StdMOB)this.getClass().newInstance();
		}
		catch(Exception e)
		{
			Log.errOut(ID(),e);
		}
		return new StdMOB();
	}

/*	public Room getStartRoom(){
		return CMLib.map().getRoom(startRoomPossibly);
	}
	public void setStartRoom(Room room){
		startRoomPossibly=room;
	} */

	public void setName(String newName)
	{
		String oldName=name;
		name=newName;
		if((playerStats!=null)&&(!CMLib.players().swapPlayer(this, oldName)))
			name=oldName;
		else
			CMLib.database().saveObject(this);
	}
	public String name()
	{
		return name;
	}
	public String plainName()
	{
		if(name==plainNameOf)
			return plainName;
		String newName=name;
		String newPlain=CMLib.coffeeFilter().toRawString(newName);
		synchronized(this)
		{
			plainName=newPlain;
			plainNameOf=newName;
		}
		return newPlain;
	}
	public String titledName()
	{
		return name;
	}
	public void setBody(Body newBody)
	{
		if(newBody!=null)
			newBody.setMob(this);
		myBody=newBody;
//		baseCharStats.setBody(newBody);
//		charStats.setBody(newBody);
		CMLib.database().saveObject(this);
	}
	public Body body(){return myBody;}
	public void setDescription(String S){}
	public String description(){return "";}
	public String plainDescription(){return "";}
	public void setDisplayText(String S){}
	public String displayText(){return "";}
	public String plainDisplayText(){return "";}

	public ItemCollection getItemCollection()
	{
		if(inventory==null) synchronized(this)
		{
			if(inventory==null)
				inventory=(ItemCollection)((Ownable)CMClass.COMMON.getNew("DefaultItemCol")).setOwner(this);
		}
		return inventory;
	}
	public Environmental getEnvObject()
	{
		if(myBody!=null)
			return myBody.getEnvObject();
		return null;
	}

	//TODO: Put this in body and fix it.
	public String genericName()
	{
//		if(charStats().age()>0)
//			return charStats().ageName().toLowerCase()+" "+charStats().raceName().toLowerCase();
//		return charStats().raceName().toLowerCase();
		return name;
	}
	public int priority(ListenHolder L){return Integer.MAX_VALUE;}
	public void registerListeners(ListenHolder forThis){}
	public void registerAllListeners() {}
	public void clearAllListeners() {}

	public boolean amDestroyed(){return amDestroyed;}
	protected void cloneFix(StdMOB E)
	{
		charAffecters=new CopyOnWriteArrayList();
		okCheckers=new CopyOnWriteArrayList();
		excCheckers=new CopyOnWriteArrayList();
		tickActers=new CopyOnWriteArrayList();
		affects=new CopyOnWriteArrayList();
		behaviors=new CopyOnWriteArrayList();
		commandQue=new ArrayList();
		nextAct=0;
		currentCommand=null;
		if(baseCharStats!=null) setBaseCharStats((CharStats)baseCharStats.copyOf());
		playerStats=null;
		mySession=null;
		myTempSession=null;
		tickStatus=Tickable.TickStat.Not;
		tickCount=0;
		if(inventory!=null) inventory=(ItemCollection)inventory.copyOf();
		victim=null;
		titles=(Vector<String>)titles.clone();
		
		for(Effect A : E.affects)
			affects.add(A.copyOnto(this));
		for(Behavior B : E.behaviors)
			addBehavior((Behavior)B.copyOf());
//		if(E==null) return;
	}

	public CMObject copyOf()
	{
		try
		{
			StdMOB E=(StdMOB)this.clone();
			E.saveNum=0;
			E.cloneFix(this);
			return E;
		}
		catch(CloneNotSupportedException e)
		{
			return this.newInstance();
		}
	}

	public CharStats baseCharStats(){return baseCharStats;}
	public CharStats charStats(){return charStats;}
	public void recoverCharStats()
	{
		baseCharStats.copyStatic(charStats);
		for(CharAffecter A : charAffecters)
			A.affectCharStats(this,charStats);
		CMLib.database().saveObject(this);
	}
//	public void resetToMaxState() { charStats.resetState(); }

	public void setBaseCharStats(CharStats newBaseCharStats)
	{
		baseCharStats=(CharStats)((Ownable)newBaseCharStats.copyOf()).setOwner(this);
		charStats=(CharStats)((Ownable)CMClass.COMMON.getNew(newBaseCharStats.ID())).setOwner(this);
		recoverCharStats();
		charStats.resetState();
	}
	public void affectEnvStats(Environmental affected, EnvStats affectableStats) { }
	public PlayerStats playerStats() { return playerStats; }
	public void setPlayerStats(PlayerStats newStats) {
		playerStats=newStats;
		if(newStats!=null)
			newStats.setMOB(this);
	}

	public void destroy()
	{
		if(mySession!=null){ mySession.kill(false); try{Thread.sleep(1000);}catch(Exception e){} mySession=null;}
		for(Effect E : affects)
			E.setAffectedOne(null);
		affects.clear();
		for(Behavior B : behaviors)
			B.startBehavior(null);
		behaviors.clear();
		if(inventory!=null)
			inventory.destroy();
		commandQue.clear();
		nextAct=0;
		victim=null;
		amDestroyed=true;
		if((myBody!=null)&&(!myBody.amDestroyed())) myBody=null;
		CMLib.database().deleteObject(this);
	}

	public Interactable getVictim()
	{
		return victim;
	}

	//This almost definitely needs to be redone.
	public void setVictim(Interactable target)
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

	public Room location()
	{
		if(myBody==null) return null;
		CMObject O=myBody.container();
		while((O!=null)&&!(O instanceof Room)&&(O instanceof Item))
			O=((Item)O).container();
		if(O instanceof Room)
			return (Room)O;
		return null;
	}
	public void setLocation(Room newPlace)
	{
		if(myBody==null) return;
		myBody.setContainer(newPlace);
	}
	public Session session()
	{
		if(myTempSession!=null) return myTempSession;
		return mySession;
	}
	public void setSession(Session newSession)
	{
		mySession=newSession;
	}
	public void setTempSession(Session newSession)
	{
		myTempSession=newSession;
	}

	public String displayName(MOB viewer)
	{
		if((CMProps.Bools.INTRODUCTIONSYSTEM.property())
		&&(playerStats()!=null)
		&&(viewer!=null)
		&&(viewer.playerStats()!=null)
		&&(!viewer.playerStats().isIntroducedTo(this)))
			return CMLib.english().startWithAorAn(genericName()).toLowerCase();
		return name();
	}

	public double actions(){return freeActions;}
	public void setActions(double remain){freeActions=remain;}
	public int commandQueSize(){return commandQue.size();}
	public void run()
	{
		actStatus=Tickable.TickStat.Start;
		QueuedCommand qCom=null;
		synchronized(commandQue)
		{
			if(commandQue.size()==0) return;
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
					nextAct=qCom.nextAct;
					CMLib.threads().startTickDown(this);
				}
				break;
			}
		}
		actStatus=Tickable.TickStat.Not;
	}

	public boolean doCommand(QueuedCommand qCom)
	{
		if((qCom.command.prompter())&&(mySession==Thread.currentThread()))
			mySession.handlePromptFor(new CommandCallWrap(this, qCom));
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
	public long nextAct(){synchronized(commandQue){return nextAct;}}

	public void enqueCommand(String commands, int metaFlags)
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

	public CopyOnWriteArrayList<CharAffecter> charAffecters(){return charAffecters;}
	public CopyOnWriteArrayList<EnvAffecter> envAffecters(){return null;}
	public CopyOnWriteArrayList<OkChecker> okCheckers(){return okCheckers;}
	public CopyOnWriteArrayList<ExcChecker> excCheckers(){return excCheckers;}
	public CopyOnWriteArrayList<TickActer> tickActers(){return tickActers;}
	public void removeListener(Listener oldAffect, EnumSet flags)
	{
		ListenHolder.O.removeListener(this, oldAffect, flags);
	}
	public void addListener(Listener newAffect, EnumSet flags)
	{
		ListenHolder.O.addListener(this, newAffect, flags);
	}
	public EnumSet<ListenHolder.Flags> listenFlags() {return lFlags;}
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
	public boolean okMessage(ListenHolder.OkChecker myHost, CMMsg msg)
	{
		for(OkChecker O : okCheckers)
			if(!O.okMessage(myHost,msg))
				return false;
		return true;
	}

	public void tell(Interactable source, Interactable target, Vector<CMObject> tools, String msg)
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
	public void tell(Interactable source, Interactable target, CMObject tool, String msg)
	{
		if((mySession!=null)&&(msg!=null))
		{
			Session S=mySession;
			if(S!=null)
				S.stdPrintln(source,target,tool,msg);
		}
	}
	public void tell(Interactable source, Interactable target, String msg)
	{
		if((mySession!=null)&&(msg!=null))
		{
			Session S=mySession;
			if(S!=null)
				S.stdPrintln(source,target,null,msg);
		}
	}

	public void tell(String msg)
	{
		tell(this,this,new Vector(),msg);
	}
	public boolean respondTo(CMMsg msg){return true;}
	public void executeMsg(ListenHolder.ExcChecker myHost, CMMsg msg)
	{
		for(ExcChecker O : excCheckers)
			O.executeMsg(myHost, msg);
	}

	public void affectCharStats(MOB affectedMob, CharStats affectableStats){}

	public Tickable.TickStat getTickStatus(){return tickStatus;}
	public Tickable.TickStat getActStatus(){return actStatus;}
	public int tickCounter(){return tickCount;}
	public void tickAct(){}	//Currently unused in favor of run()
	public boolean tick(int tickTo)
	{
		if(tickCount==0) tickCount=tickTo-1;
		else if(tickTo>tickCount+10)
			tickTo=tickCount+10;
		while(tickCount<tickTo)
		{
			tickCount++;
			doTick();
		}
		return true;
	}
	protected void doTick()
	{
		tickStatus=Tickable.TickStat.Listener;
		for(TickActer T : tickActers)
			if(!T.tick(tickCount))
				tickActers.remove(T);
		tickStatus=Tickable.TickStat.Not;
	}
	//public long lastAct(){return lastAct;}	//No Action ticks
	//public long lastTick(){return lastTick;}

	public boolean isMonster()
	{
		return playerStats==null;
	}

	public int compareTo(CMObject o){ return CMClass.classID(this).compareToIgnoreCase(CMClass.classID(o));}

	public Item fetchInventory(String itemName)
	{
		//Interactable[] items=getItemCollection().toArray(Interactable.dummyInteractableArray);
		Item item=(Item)CMLib.english().fetchInteractable(getItemCollection().allItems(),itemName,true);
		if(item==null) item=(Item)CMLib.english().fetchInteractable(inventory.allItems(),itemName,false);
		return item;
	}
	public Vector<Item> fetchInventories(String itemName)
	{ 
		//Interactable[] items=getItemCollection().toArray(Interactable.dummyInteractableArray);
		Vector V=CMLib.english().fetchInteractables(getItemCollection().allItems(),itemName,true);
		if(V.size()>0) return V;
		V=CMLib.english().fetchInteractables(inventory.allItems(),itemName,false);
		return V;
	}

	public boolean willFollowOrdersOf(MOB mob)
	{
		if(isMonster()&&CMSecurity.isAllowed(mob,"ORDER"))
			return true;
		if((!isMonster())
		&&(CMSecurity.isAllowed(mob,"ORDER"))
		&&((!CMSecurity.isASysOp(this))||CMSecurity.isASysOp(mob)))
			return true;
		return false;
	}

	public void setActiveTitle(String S)
	{
		synchronized(titles)
		{
			if(titles.remove(S))
				titles.add(0, S);
		}
	}
	public String getActiveTitle()
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
	public String[] getTitles()
	{
		return titles.toArray(CMClass.dummyStringArray);
	}
	public void addTitle(String title){synchronized(titles){if(titles.contains(title)) return; titles.add(title);}}
	public void removeTitle(String title){synchronized(titles){titles.remove(title);}}

	//Affectable
	public void addEffect(Effect to)
	{
		affects.add(to);
		to.setAffectedOne(this);
		CMLib.database().saveObject(this);
	}
	public void delEffect(Effect to)
	{
		if(affects.remove(to))
		{
			to.setAffectedOne(null);
			CMLib.database().saveObject(this);
		}
	}
	public boolean hasEffect(Effect to)
	{
		return affects.contains(to);
	}
	public int numEffects(){return affects.size();}
	public Effect fetchEffect(int index)
	{
		try { return affects.get(index); }
		catch(java.lang.ArrayIndexOutOfBoundsException x){}
		return null;
	}
	public Vector<Effect> fetchEffect(String ID)
	{
		Vector<Effect> V=new Vector(1);
		for(Effect E : affects)
			if(E.ID().equals(ID))
				V.add(E);
		return V;
	}
	public Iterator<Effect> allEffects() { return affects.iterator(); }

	//Behavable
	public void addBehavior(Behavior to)
	{
		synchronized(behaviors)
		{
			if(fetchBehavior(to.ID())!=null) return;
			to.startBehavior(this);
			behaviors.add(to);
		}
		CMLib.database().saveObject(this);
	}
	public void delBehavior(Behavior to)
	{
		if(behaviors.remove(to))
		{
			to.startBehavior(null);
			CMLib.database().saveObject(this);
		}
	}
	public int numBehaviors()
	{
		return behaviors.size();
	}
	public Behavior fetchBehavior(int index)
	{
		try { return behaviors.get(index); }
		catch(java.lang.ArrayIndexOutOfBoundsException x){}
		return null;
	}
	public Behavior fetchBehavior(String ID)
	{
		for(Behavior B : behaviors)
			if(B.ID().equals(ID))
				return B;
		return null;
	}
	public boolean hasBehavior(String ID)
	{
		for(Behavior B : behaviors)
			if(B.ID().equals(ID))
				return true;
		return false;
	}
	public Iterator<Behavior> allBehaviors() { return behaviors.iterator(); }

	public void giveItem(Item item)
	{
		if(item.container()!=null)
			ItemCollection.O.getFrom(item.container()).removeItem(item);
		getItemCollection().addItem(item);
	}

	public boolean isMine(Interactable env)
	{
		if(env instanceof Item)
		{
			if(inventory==null) return false;
			return inventory.hasItem((Item)env, true);
		}
		return false;
	}

	//CMModifiable and CMSavable
	public SaveEnum[] totalEnumS(){return SCode.values();}
	public Enum[] headerEnumS(){return new Enum[] {SCode.values()[0]} ;}
	public ModEnum[] totalEnumM(){return MCode.values();}
	public Enum[] headerEnumM(){return new Enum[] {MCode.values()[0]};}
	public int saveNum()
	{
		if(saveNum==0)
		synchronized(this)
		{
			if(saveNum==0)
				saveNum=SIDLib.CREATURE.getNumber(this);
		}
		return saveNum;
	}
	public void setSaveNum(int num)
	{
		synchronized(this)
		{
			if(saveNum!=0)
				SIDLib.CREATURE.removeNumber(saveNum);
			saveNum=num;
			SIDLib.CREATURE.assignNumber(num, this);
		}
	}
	public boolean needLink(){return true;}
	public void link()
	{
		if(effectsToLoad!=null)
		{
			for(int SID : effectsToLoad)
			{
				Effect to = SIDLib.EFFECT.get(SID);
				if(to==null) continue;
				affects.add(to);
				to.setAffectedOne(this);
			}
			effectsToLoad=null;
		}
		if(behavesToLoad!=null)
		{
			for(int SID : behavesToLoad)
			{
				Behavior to = SIDLib.BEHAVIOR.get(SID);
				if(to==null) continue;
				to.startBehavior(this);
				behaviors.add(to);
			}
			behavesToLoad=null;
		}
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
				((Ownable)inventory).setOwner(this);
			itemCollectionToLoad=0;
			//Ideally never happens
			if(oldInventory!=null)
			{
				getItemCollection();
				for(Iterator<Item> iter=oldInventory.allItems();iter.hasNext();)
				{
					Item next=iter.next();
					next.setContainer(null);
					inventory.addItem(next);
				}
			}
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
	public void saveThis(){CMLib.database().saveObject(this);}
	public void prepDefault(){getItemCollection();}

	private enum SCode implements CMSavable.SaveEnum{
		NAM(){
			public ByteBuffer save(StdMOB E){ return CMLib.coffeeMaker().savString(E.name); }
			public int size(){return 0;}
			public void load(StdMOB E, ByteBuffer S){ E.name=CMLib.coffeeMaker().loadString(S); } },
		BCS(){
			public ByteBuffer save(StdMOB E){ return CMLib.coffeeMaker().savSubFull(E.baseCharStats); }
			public int size(){return -1;}
			public CMSavable subObject(CMSavable fromThis){return ((StdMOB)fromThis).baseCharStats;}
			public void load(StdMOB E, ByteBuffer S){ E.baseCharStats=(CharStats)((Ownable)CMLib.coffeeMaker().loadSub(S, E, this)).setOwner(E); } },
		CHS(){
			public ByteBuffer save(StdMOB E){ return CMLib.coffeeMaker().savSubFull(E.charStats); }
			public int size(){return -1;}
			public CMSavable subObject(CMSavable fromThis){return ((StdMOB)fromThis).charStats;}
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
		EFC(){
			public ByteBuffer save(StdMOB E){
				if(E.affects.size()>0) return CMLib.coffeeMaker().savSaveNums((CMSavable[])E.affects.toArray(CMSavable.dummyCMSavableArray));
				return GenericBuilder.emptyBuffer; }
			public int size(){return 0;}
			public void load(StdMOB E, ByteBuffer S){ E.effectsToLoad=CMLib.coffeeMaker().loadAInt(S); } },
		BHV(){
			public ByteBuffer save(StdMOB E){
				if(E.behaviors.size()>0) return CMLib.coffeeMaker().savSaveNums((CMSavable[])E.behaviors.toArray(CMSavable.dummyCMSavableArray));
				return GenericBuilder.emptyBuffer; }
			public int size(){return 0;}
			public void load(StdMOB E, ByteBuffer S){ E.behavesToLoad=CMLib.coffeeMaker().loadAInt(S); } },
		BDY(){
			public ByteBuffer save(StdMOB E){
				ByteBuffer data=ByteBuffer.wrap(new byte[4]);
				if(E.myBody!=null) data.putInt(E.myBody.saveNum()).rewind();
				return data; }
			public int size(){return 4;}
			public void load(StdMOB E, ByteBuffer S){ E.bodyToLink=S.getInt(); } },
		;
		public abstract ByteBuffer save(StdMOB E);
		public abstract void load(StdMOB E, ByteBuffer S);
		public ByteBuffer save(CMSavable E){return save((StdMOB)E);}
		public CMSavable subObject(CMSavable fromThis){return null;}
		public void load(CMSavable E, ByteBuffer S){load((StdMOB)E, S);} }
	private enum MCode implements CMModifiable.ModEnum{
		NAME(){
			public String brief(StdMOB E){return E.name;}
			public String prompt(StdMOB E){return E.name;}
			public void mod(StdMOB E, MOB M){
				String newName=CMLib.genEd().stringPrompt(M, ""+E.name, false);
				if(!E.name.equals(newName)) E.setName(newName); } },
		CHARSTATS(){
			public String brief(StdMOB E){return E.charStats.ID();}
			public String prompt(StdMOB E){return "";}
			public void mod(StdMOB E, MOB M){CMLib.genEd().genMiscSet(M, E.charStats);} },
		BASECHARSTATS(){
			public String brief(StdMOB E){return E.baseCharStats.ID();}
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
		EFFECTS(){
			public String brief(StdMOB E){return ""+E.affects.size();}
			public String prompt(StdMOB E){return "";}
			public void mod(StdMOB E, MOB M){CMLib.genEd().modAffectable(E, M);} },
		BEHAVIORS(){
			public String brief(StdMOB E){return ""+E.behaviors.size();}
			public String prompt(StdMOB E){return "";}
			public void mod(StdMOB E, MOB M){CMLib.genEd().modBehavable(E, M);} },
		BODY(){
			public String brief(StdMOB E){return E.myBody==null?"null":E.myBody.ID();}
			public String prompt(StdMOB E){return "";}
			public void mod(StdMOB E, MOB M){CMLib.genEd().genMiscSet(M, E.myBody);} },
		TITLES(){
			public String brief(StdMOB E){return ""+E.titles.size();}
			public String prompt(StdMOB E){return E.titles.toString();}
			public void mod(StdMOB E, MOB M){
				boolean done=false;
				while((M.session()!=null)&&(!M.session().killFlag())&&(!done)) {
					Vector V=(Vector)E.titles.clone();
					int i=CMLib.genEd().promptVector(M, V, true);
					if(--i<0) done=true;
					else if(i==V.size()) {
						String S=CMLib.genEd().stringPrompt(M, "", false).trim().toUpperCase();
						if((S.length()>0)&&(!E.titles.contains(S))) E.titles.add(S); }
					else if(i<V.size()) E.titles.remove(V.get(i)); } } },
		;
		public abstract String brief(StdMOB fromThis);
		public abstract String prompt(StdMOB fromThis);
		public abstract void mod(StdMOB toThis, MOB M);
		public String brief(CMModifiable fromThis){return brief((StdMOB)fromThis);}
		public String prompt(CMModifiable fromThis){return prompt((StdMOB)fromThis);}
		public void mod(CMModifiable toThis, MOB M){mod((StdMOB)toThis, M);} }

	public boolean sameAs(Interactable E)
	{
		if(!(E instanceof StdMOB)) return false;
		return true;
	}
}
