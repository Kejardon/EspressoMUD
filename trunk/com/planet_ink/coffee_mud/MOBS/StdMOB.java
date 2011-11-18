package com.planet_ink.coffee_mud.MOBS;
import com.planet_ink.coffee_mud.core.interfaces.*;
import com.planet_ink.coffee_mud.core.*;
import com.planet_ink.coffee_mud.Effects.interfaces.*;
import com.planet_ink.coffee_mud.Areas.interfaces.*;
import com.planet_ink.coffee_mud.Behaviors.interfaces.*;
import com.planet_ink.coffee_mud.Commands.interfaces.*;
import com.planet_ink.coffee_mud.Common.interfaces.*;
import com.planet_ink.coffee_mud.Exits.interfaces.*;
//import com.planet_ink.coffee_mud.Items.Basic.StdItem;
import com.planet_ink.coffee_mud.Items.interfaces.*;
import com.planet_ink.coffee_mud.Locales.interfaces.*;
import com.planet_ink.coffee_mud.MOBS.interfaces.*;
import com.planet_ink.coffee_mud.Races.interfaces.*;
import java.util.*;

import com.planet_ink.coffee_mud.Libraries.interfaces.*;
import java.io.IOException;


/*
   Copyright 2000-2010 Bo Zimmerman

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

	   http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, e\ither express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
*/
@SuppressWarnings("unchecked")
public class StdMOB implements MOB
{
	private static final Vector empty=new Vector();

	public String ID(){return "StdMOB";}
	public String name="";

	protected CharStats baseCharStats=(CharStats)CMClass.Objects.COMMON.getNew("DefaultCharStats");
	protected CharStats charStats=(CharStats)CMClass.Objects.COMMON.getNew("DefaultCharStats");

	protected PlayerStats playerStats=null;

//	protected boolean amDead=false;
//	protected Room location=null;
//	protected Room lastLocation=null;

	protected Session mySession=null;
	protected Session myTempSession=null;
//	protected boolean pleaseDestroy=false;

	protected Tickable.TickStat tickStatus=Tickable.TickStat.Not;
	protected long lastTick=0;
	protected long lastAct=0;

	/* containers of items and attributes*/
	protected EnumSet<ListenHolder.Flags> lFlags=EnumSet.of(ListenHolder.Flags.OK,ListenHolder.Flags.EXC,ListenHolder.Flags.TICK);
	protected Vector<CharAffecter> charAffecters=new Vector<CharAffecter>();
//	protected Vector<EnvAffecter> envAffecters=new Vector<EnvAffecter>();
	protected Vector<OkChecker> okCheckers=new Vector<OkChecker>();
	protected Vector<ExcChecker> excCheckers=new Vector<ExcChecker>();
	protected Vector<TickActer> tickActers=new Vector<TickActer>();

	protected ItemCollection inventory=new ItemCollection.DefaultItemCol(this);
	protected Vector affects=new Vector(1);
	protected Vector behaviors=new Vector(1);

	protected DVector commandQue=new DVector(6);

	// gained attributes
	private double freeActions=0.0;

	// the core state values
	private long lastTickedDateTime=0;
	private long lastCommandTime=System.currentTimeMillis();
	public long lastTickedDateTime(){return lastTickedDateTime;}

//	protected Room startRoomPossibly=null;
//	protected int WimpHitPoint=0;
	protected Interactable victim=null;
//	protected MOB soulMate=null;
	protected boolean amDestroyed=false;
//	protected boolean kickFlag=false;
//	protected boolean imMobile=false;

	protected Vector titles=new Vector();
	protected Body myBody=null;

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

	public void setName(String newName){
		name=newName;
	}
	public String name()
	{
		return name;
	}
	public String titledName()
	{
		return name;
	}
	public void setBody(Body newBody){myBody=newBody;}
	public Body body(){return myBody;}
	public void setDescription(String S){}
	public String description(){return "";}
	public void setDisplayText(String S){}
	public String displayText(){return "";}

	public ItemCollection getItemCollection(){return inventory;}
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

	public StdMOB() { }
	public boolean amDestroyed(){return amDestroyed;}
	protected void cloneFix(MOB E)
	{
		//TODO
		if(E==null) return;
	}

	public CMObject copyOf()
	{
		try
		{
			StdMOB E=(StdMOB)this.clone();
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
		for(int a=charAffecters.size();a>0;a--)
			charAffecters.get(a-1).affectCharStats(this,charStats);
	}
//	public void resetToMaxState() { charStats.resetState(); }

	public void setBaseCharStats(CharStats newBaseCharStats)
	{
		baseCharStats=(CharStats)newBaseCharStats.copyOf();
		charStats=(CharStats)CMClass.Objects.COMMON.getNew(newBaseCharStats.ID());
		recoverCharStats();
		charStats.resetState();
	}
	public void affectEnvStats(Environmental affected, EnvStats affectableStats) { }
	public PlayerStats playerStats() { return playerStats; }
	public void setPlayerStats(PlayerStats newStats) { playerStats=newStats; }

	public void destroy()
	{
		if(session()!=null){ session().kill(false,false,false); try{Thread.sleep(1000);}catch(Exception e){}}
		while(numBehaviors()>0)
			delBehavior(fetchBehavior(0));
		while(numEffects()>0)
			delEffect(fetchEffect(0));
		charStats=baseCharStats;
		playerStats=null;
		mySession=null;
		affects=new Vector(1);
		behaviors=new Vector(1);
		commandQue=new DVector(6);
		victim=null;
		amDestroyed=true;
		myBody=null;
	}

	public Interactable getVictim()
	{
		return victim;
	}

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
		&&(!viewer.playerStats().isIntroducedTo(name())))
			return CMLib.english().startWithAorAn(genericName()).toLowerCase();
		return name();
	}

	public double actions(){return freeActions;}
	public void setActions(double remain){freeActions=remain;}
	public int commandQueSize(){return commandQue.size();}
	public boolean dequeCommand()
	{
		while((session()==null)||(!session().killFlag()))
		{
			Object[] doCommand=null;
			synchronized(commandQue)
			{
				if(commandQue.size()==0) return false;
				Object[] ROW=commandQue.elementsAt(0);
				double diff=actions()-((Double)ROW[2]).doubleValue();
				if(diff>=0.0)
				{
					long nextTime=lastCommandTime
								 +Math.round(((Double)ROW[2]).doubleValue()
											 /myBody.getEnvObject().envStats().speed()
											 *TIME_TICK_DOUBLE);
					if((System.currentTimeMillis()<nextTime)&&(session()!=null))
						return false;
					ROW=commandQue.removeElementsAt(0);
					setActions(diff);
					doCommand=ROW;
				}
			}
			if(doCommand!=null)
			{
				lastCommandTime=System.currentTimeMillis();
				doCommand(doCommand[0],(Vector)doCommand[1],((Integer)doCommand[5]).intValue());
				synchronized(commandQue)
				{
					if(commandQue.size()>0)
					{
						Object O=commandQue.elementAt(0,1);
						Double D=Double.valueOf(calculateTickDelay(O,(Vector)doCommand[1],0.0));
						if(commandQue.size()>0) commandQue.setElementAt(0,3,D);
					}
					else
						return false;
					return true;
				}
			}

			synchronized(commandQue)
			{
				if(commandQue.size()==0) return false;
				Object[] ROW=commandQue.elementsAt(0);
				if(System.currentTimeMillis()<((long[])ROW[3])[0])
					return false;
				double diff=actions()-((Double)ROW[2]).doubleValue();
				Object O=ROW[0];
				Vector commands=(Vector)ROW[1];
				((long[])ROW[3])[0]=((long[])ROW[3])[0]+1000;
				((int[])ROW[4])[0]+=1;
				int secondsElapsed=((int[])ROW[4])[0];
				int metaFlags=((Integer)ROW[5]).intValue();
				try
				{
					if(O instanceof Command)
					{
						if(!((Command)O).preExecute(this,commands,metaFlags,secondsElapsed,-diff))
						{
							commandQue.removeElementsAt(0);
							return true;
						}
					}
/*					else
					if(O instanceof Effect)
					{
						if(!CMLib.english().preEvoke(this,commands,secondsElapsed,-diff))
						{
							commandQue.removeElementsAt(0);
							return true;
						}
					}
*/				}
				catch(Exception e)
				{
					return false;
				}
			}
		}
		return false;
	}

	public void doCommand(Vector commands, int metaFlags)
	{
		Object O=CMLib.english().findCommand(this,commands);
		if(O!=null)
			doCommand(O,commands, metaFlags);
		else
			CMLib.commands().handleUnknownCommand(this,commands);
	}

	protected void doCommand(Object O, Vector commands, int metaFlags)
	{
		try
		{
			if(O instanceof Command)
				((Command)O).execute(this,commands, metaFlags);
//			else
//			if(O instanceof Social)
//				((Social)O).invoke(this,commands,null,false);
//			else
//			if(O instanceof Effect)
//				CMLib.english().evoke(this,commands);
			else
				CMLib.commands().handleUnknownCommand(this,commands);
		}
		catch(java.io.IOException io)
		{
			Log.errOut("StdMOB",CMParms.toStringList(commands));
			if(io.getMessage()!=null)
				Log.errOut("StdMOB",io.getMessage());
			else
				Log.errOut("StdMOB",io);
			tell("Oops!");
		}
		catch(Exception e)
		{
			Log.errOut("StdMOB",CMParms.toStringList(commands));
			Log.errOut("StdMOB",e);
			tell("Oops!");
		}
	}

	protected double calculateTickDelay(Object command, Vector commands, double tickDelay)
	{
		if(tickDelay<=0.0)
		{
			if(command==null){ tell("Huh?!"); return -1.0;}
			if(command instanceof Command)
				tickDelay=((Command)command).actionsCost(this,commands);
//			else
//			if(command instanceof Effect)
//				tickDelay=((Effect)command).castingTime(this,commands);
			else
				tickDelay=1.0;
		}
		return tickDelay;
	}

	public void prequeCommand(Vector commands, int metaFlags, double tickDelay)
	{
		if(commands==null) return;
		Object O=CMLib.english().findCommand(this,commands);
		if(O==null){ CMLib.commands().handleUnknownCommand(this,commands); return;}
		tickDelay=calculateTickDelay(O,commands,tickDelay);
		if(tickDelay<0.0) return;
		if(tickDelay==0.0)
			doCommand(O,commands,metaFlags);
		else
		synchronized(commandQue)
		{
			long[] next=new long[1];
			next[0]=System.currentTimeMillis()-1;
			int[] seconds=new int[1];
			seconds[0]=-1;
			commandQue.insertElementAt(0,O,commands,Double.valueOf(tickDelay),next,seconds,Integer.valueOf(metaFlags));
		}
		dequeCommand();
	}

	public void enqueCommand(Vector commands, int metaFlags, double tickDelay)
	{
		if(commands==null) return;
		Object O=CMLib.english().findCommand(this,commands);
		if(O==null){ CMLib.commands().handleUnknownCommand(this,commands); return;}
		tickDelay=calculateTickDelay(O,commands,tickDelay);
		if(tickDelay<0.0) return;
		if(tickDelay==0.0)
			doCommand(commands,metaFlags);
		else
		synchronized(commandQue)
		{
			long[] next=new long[1];
			next[0]=System.currentTimeMillis()-1;
			int[] seconds=new int[1];
			seconds[0]=-1;
			commandQue.addElement(O,commands,Double.valueOf(tickDelay),next,seconds,Integer.valueOf(metaFlags));
		}
		dequeCommand();
	}

	public Vector<CharAffecter> charAffecters(){return charAffecters;}
	public Vector<EnvAffecter> envAffecters(){return null;}
	public Vector<OkChecker> okCheckers(){return okCheckers;}
	public Vector<ExcChecker> excCheckers(){return excCheckers;}
	public Vector<TickActer> tickActers(){return tickActers;}
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
		for(int i=okCheckers.size();i>0;i--)
			if(!okCheckers.get(i-1).okMessage(myHost,msg))
				return false;
		return true;
	}

	public void tell(Interactable source, Interactable target, Vector<CMObject> tools, String msg)
	{
		CMObject tool=null;
		if(tools.size()>0) tool=tools.get(0);
		if((mySession!=null)&&(msg!=null))
		{
			Session S=mySession;
			if(S!=null)
				S.stdPrintln(source,target,tool,msg);
		}
	}

	public void tell(String msg)
	{
		tell(this,this,null,msg);
	}
	public boolean respondTo(CMMsg msg){return true;}
	public void executeMsg(ListenHolder.ExcChecker myHost, CMMsg msg)
	{
		for(int i=excCheckers.size();i>0;i--)
			excCheckers.get(i-1).executeMsg(myHost,msg);
	}

	public void affectCharStats(MOB affectedMob, CharStats affectableStats){}

	public Tickable.TickStat getTickStatus(){return tickStatus;}
	public boolean tick(Tickable ticking, Tickable.TickID tickID)
	{
		if(tickID==Tickable.TickID.Action) return false;
		tickStatus=Tickable.TickStat.Listener;
		for(int i=tickActers.size()-1;i>=0;i--)
		{
			TickActer T=tickActers.get(i);
			if(!T.tick(ticking, tickID))
				removeListener(T, EnumSet.of(ListenHolder.Flags.TICK));
		}
		tickStatus=Tickable.TickStat.Not;
		lastTick=System.currentTimeMillis();
		return true;
	}
	public long lastAct(){return lastAct;}	//No Action ticks
	public long lastTick(){return lastTick;}

	public boolean isMonster()
	{
		return playerStats==null;
	}

	public int compareTo(CMObject o){ return CMClass.classID(this).compareToIgnoreCase(CMClass.classID(o));}

	public Item fetchInventory(String itemName)
	{
		Vector<Item> inv=inventory.allItems();
		Item item=null;
		item=(Item)CMLib.english().fetchInteractable(inv,itemName,true);
		if(item==null) item=(Item)CMLib.english().fetchInteractable(inv,itemName,false);
		return item;
	}
	public Vector fetchInventories(String itemName)
	{ 
		Vector<Item> inv=inventory.allItems();
		Vector V=CMLib.english().fetchInteractables(inv,itemName,true);
		if((V!=null)&&(V.size()>0)) return V;
		V=CMLib.english().fetchInteractables(inv,itemName,false);
		if(V!=null) return V;
		return new Vector(1);
	}

	public boolean willFollowOrdersOf(MOB mob)
	{
		if(isMonster()&&CMSecurity.isAllowed(mob,location(),"ORDER"))
			return true;
		if((!isMonster())
		&&(CMSecurity.isAllowedEverywhere(mob,"ORDER"))
		&&((!CMSecurity.isASysOp(this))||CMSecurity.isASysOp(mob)))
			return true;
		return false;
	}

	public String getActiveTitle()
	{
		if((titles==null)||(titles.size()==0)) return null;
		String s=(String)titles.firstElement();
		if((s.length()<2)||(s.charAt(0)!='{')||(s.charAt(s.length()-1)!='}'))
			return s;
		return s.substring(1,s.length()-1);
	}
	
	public Vector getTitles()
	{
		return titles;
	}

	//Affectable
	public void addEffect(Effect to)
	{
		if(to==null) return;
		affects.addElement(to);
		to.setAffectedOne(this);
	}
	public void delEffect(Effect to)
	{
		if(affects.removeElement(to))
			to.setAffectedOne(null);
	}
	public int numEffects()
	{
		if(affects==null) return 0;
		return affects.size();
	}
	public Effect fetchEffect(int index)
	{
		if(affects==null) return null;
		try
		{
			return (Effect)affects.elementAt(index);
		}
		catch(java.lang.ArrayIndexOutOfBoundsException x){}
		return null;
	}
	public Vector<Effect> fetchEffect(String ID)
	{
		Vector<Effect> V=new Vector<Effect>();
		if(affects==null) return null;
		for(int a=0;a<numEffects();a++)
		{
			Effect A=fetchEffect(a);
			if((A!=null)&&(A.ID().equals(ID)))
				V.add(A);
		}
		return V;
	}
	public Vector<Effect> allEffects() { return (Vector<Effect>)affects.clone(); }

	//Behavable
	public void addBehavior(Behavior to)
	{
		if(to==null) return;
		if(behaviors==null) behaviors=new Vector(1);
		if(fetchBehavior(to.ID())!=null) return;
		to.startBehavior(this);
		behaviors.addElement(to);
	}
	public void delBehavior(Behavior to)
	{
		if(behaviors==null) return;
		behaviors.removeElement(to);
	}
	public int numBehaviors()
	{
		if(behaviors==null) return 0;
		return behaviors.size();
	}
	public Behavior fetchBehavior(int index)
	{
		if(behaviors==null) return null;
		try
		{
			return (Behavior)behaviors.elementAt(index);
		}
		catch(java.lang.ArrayIndexOutOfBoundsException x){}
		return null;
	}
	public Vector<Behavior> fetchBehavior(String ID)
	{
		Vector<Behavior> V=new Vector<Behavior>();
		if(behaviors==null) return V;
		for(int b=0;b<numBehaviors();b++)
		{
			Behavior B=fetchBehavior(b);
			if((B!=null)&&(B.ID().equalsIgnoreCase(ID)))
				V.add(B);
		}
		return V;
	}
	public Vector<Behavior> allBehaviors(){ return (Vector<Behavior>)behaviors.clone(); }

	public void giveItem(Item item)
	{
		if(item.container()!=null)
			ItemCollection.DefaultItemCol.getFrom(item.container()).removeItem(item);
		inventory.addItem(item);
	}

	public boolean isMine(Interactable env)
	{
		if(env instanceof Item)
			return inventory.hasItem((Item)env, true);
		return false;
	}

	//CMModifiable and CMSavable
	public SaveEnum[] totalEnumS(){return SCode.values();}
	public Enum[] headerEnumS(){return new Enum[] {SCode.values()[0]} ;}
	public ModEnum[] totalEnumM(){return MCode.values();}
	public Enum[] headerEnumM(){return new Enum[] {MCode.values()[0]};}

	private enum SCode implements CMSavable.SaveEnum{
		NAM(){
			public String save(StdMOB E){ return ""+E.name; }
			public void load(StdMOB E, String S){ E.name=S.intern(); } },
		BCS(){
			public String save(StdMOB E){ return CMLib.coffeeMaker().getSubStr(E.baseCharStats); }
			public void load(StdMOB E, String S){ E.baseCharStats=(CharStats)CMLib.coffeeMaker().loadSub(S); } },
		CHS(){
			public String save(StdMOB E){ return CMLib.coffeeMaker().getSubStr(E.charStats); }
			public void load(StdMOB E, String S){ E.charStats=(CharStats)CMLib.coffeeMaker().loadSub(S); } },
		PLS(){
			public String save(StdMOB E){ return CMLib.coffeeMaker().getSubStr(E.playerStats); }
			public void load(StdMOB E, String S){ E.playerStats=(PlayerStats)CMLib.coffeeMaker().loadSub(S); } },
		INV(){
			public String save(StdMOB E){ return CMLib.coffeeMaker().getSubStr(E.inventory); }
			public void load(StdMOB E, String S){ E.inventory=(ItemCollection)CMLib.coffeeMaker().loadSub(S); } },
		TTL(){
			public String save(StdMOB E){ return CMLib.coffeeMaker().savAString((String[])E.titles.toArray()); }
			public void load(StdMOB E, String S){ for(String newI : CMLib.coffeeMaker().loadAString(S)) E.titles.add(newI); } },
		EFC(){
			public String save(StdMOB E){ return CMLib.coffeeMaker().getVectorStr(E.affects); }
			public void load(StdMOB E, String S){
				Vector<Effect> V=CMLib.coffeeMaker().setVectorStr(S);
				for(Effect A : V)
					E.addEffect(A);
				
				} },
		BHV(){
			public String save(StdMOB E){ return CMLib.coffeeMaker().getVectorStr(E.behaviors); }
			public void load(StdMOB E, String S){
				Vector<Behavior> V=CMLib.coffeeMaker().setVectorStr(S);
				for(Behavior A : V)
					E.addBehavior(A);
				
				} },
		BDY(){
			public String save(StdMOB E){ if(E.isMonster()||E.myBody==null) return ""; return CMLib.coffeeMaker().getSubStr(E.myBody); }
			public void load(StdMOB E, String S){ E.myBody=(Body)CMLib.coffeeMaker().loadSub(S); } },
		;
		public abstract String save(StdMOB E);
		public abstract void load(StdMOB E, String S);
		public String save(CMSavable E){return save((StdMOB)E);}
		public void load(CMSavable E, String S){load((StdMOB)E, S);} }
	private enum MCode implements CMModifiable.ModEnum{
		NAME(){
			public String brief(StdMOB E){return E.name;}
			public String prompt(StdMOB E){return E.name;}
			public void mod(StdMOB E, MOB M){
				if(!E.isMonster()){
					M.tell("Player name changes are not supported yet.");
					return; }
				E.name=CMLib.genEd().stringPrompt(M, ""+E.name, false); } },
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
			public String brief(StdMOB E){return E.inventory.ID()+" "+E.inventory.numItems();}
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
