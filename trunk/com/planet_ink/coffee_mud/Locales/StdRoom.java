package com.planet_ink.coffee_mud.Locales;
import com.planet_ink.coffee_mud.Libraries.interfaces.*;
import com.planet_ink.coffee_mud.core.interfaces.*;
import com.planet_ink.coffee_mud.core.*;
import com.planet_ink.coffee_mud.Effects.interfaces.*;
import com.planet_ink.coffee_mud.Areas.interfaces.*;
import com.planet_ink.coffee_mud.Behaviors.interfaces.*;
import com.planet_ink.coffee_mud.Commands.interfaces.*;
import com.planet_ink.coffee_mud.Common.interfaces.*;
import com.planet_ink.coffee_mud.Exits.interfaces.*;
import com.planet_ink.coffee_mud.Items.Basic.StdItem;
import com.planet_ink.coffee_mud.Items.interfaces.*;
import com.planet_ink.coffee_mud.Locales.interfaces.*;
import com.planet_ink.coffee_mud.MOBS.interfaces.*;
import com.planet_ink.coffee_mud.Races.interfaces.*;



import java.util.*;
import java.io.*;
/* 
   Copyright 2000-2010 Bo Zimmerman

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

	   http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
*/
@SuppressWarnings("unchecked")
public class StdRoom implements Room
{
	public String ID(){return "StdRoom";}
	protected String myID="";
	protected String name="the room";
	protected String display="Standard Room";
	protected String desc="";
	protected Area myArea=null;
	protected Vector<Exit> exits=new Vector(1);
	protected Vector<Room> exitDests=new Vector(1);
	protected Vector<OkChecker> okCheckers=null;
	protected Vector<ExcChecker> excCheckers=null;
	protected Vector<TickActer> tickActers=null;
	protected Vector affects=new Vector(1);
	protected Vector behaviors=new Vector(1);
	protected String exitsToLoad=null;
	protected EnumSet<ListenHolder.Flags> lFlags=EnumSet.of(ListenHolder.Flags.OK, ListenHolder.Flags.EXC, ListenHolder.Flags.TICK);
	protected long lastTick=0;
	//TODO: This needs a custom item collection!
	protected ItemCollection inventory=new ItemCollection.DefaultItemCol(this);
	protected Domain myDom=Domain.Plains;
	protected Enclosure myEnc=Enclosure.Open;
	protected boolean amDestroyed=false;
	protected Tickable.TickStat tickStatus=Tickable.TickStat.Not;
	protected Environmental.DefaultEnv myEnvironmental=new Environmental.DefaultEnv(this);

	public int priority(ListenHolder L){return Integer.MAX_VALUE;}
	public void registerListeners(ListenHolder forThis){}

	public StdRoom(){}
	public void initializeClass(){}
	public CMObject newInstance()
	{
		try
		{
			return (StdRoom)this.getClass().newInstance();
		}
		catch(Exception e)
		{
			Log.errOut(ID(),e);
		}
		return new StdRoom();
	}

	public Environmental getEnvObject() {return myEnvironmental;}
	public String roomID() { return myID; }
	public void setName(String newName){name=newName;}
	public String name(){ return name;}
	public String displayText(){return display;}
	public void setDisplayText(String newDisplayText){display=newDisplayText;}
	public String description(){return desc;}
	public void setDescription(String newDescription){desc=newDescription;}

	protected void cloneFix(Room E)
	{ //TODO
	}
	public CMObject copyOf()
	{
		try
		{
			StdRoom R=(StdRoom)this.clone();
			R.cloneFix(this);
			return R;
		}
		catch(CloneNotSupportedException e)
		{
			return this.newInstance();
		}
	}
	public int numExits() { return exits.size(); }
	public void addExit(Exit E, Room destination)
	{
		synchronized(exits)
		{
			for(int i=0; i<exits.size();i++)
				if(exits.get(i)==E)
					if(exitDests.get(i)==destination)
						return;
			exits.add(E);
			exitDests.add(destination);
		}
	}
	public void removeExit(Exit E)
	{
		synchronized(exits)
		{
			for(int i=0; i<exits.size();i++)
				if(exits.get(i)==E)
				{
					exits.remove(i);
					exitDests.remove(i);
					return;
				}
		}
	}
	public void removeExit(int i)
	{
		try{
		synchronized(exits)
		{
			exits.remove(i);
			exitDests.remove(i);
		} }
		catch(ArrayIndexOutOfBoundsException e){}
	}
	public Exit getExit(int i)
	{
		try{ return exits.get(i); }
		catch(ArrayIndexOutOfBoundsException e){}
		return null;
	}
	public int getExit(String target)
	{
		int i=CMLib.english().fetchInteractableIndex(exits, target, true);
		if(i==-1) i=CMLib.english().fetchInteractableIndex(exits, target, false);
		return i;
	}
	public Room getExitDestination(int i)
	{
		try{ return exitDests.get(i); }
		catch(ArrayIndexOutOfBoundsException e){}
		return null;
	}
	public Room getExitDestination(Exit E)
	{
		for(int i=0; i<exits.size();i++)
			if(exits.get(i)==E)
				return exitDests.get(i);
		return null;
	}
	public boolean changeExit(int i, Exit newExit)
	{
		try{
		synchronized(exits)
		{
			exits.set(i, newExit);
			return true;
		} }
		catch(ArrayIndexOutOfBoundsException e){}
		return false;
	}
	public boolean changeExit(Exit oldExit, Exit newExit)
	{
		synchronized(exits)
		{
			for(int i=0; i<exits.size();i++)
				if(exits.get(i)==E)
				{
					exits.set(i, newExit);
					return true;
				}
		}
		return false;
	}
	public boolean changeRoom(int i, Room R)
	{
		try{
		synchronized(exits)
		{
			exitDests.set(i, R);
			return true;
		} }
		catch(ArrayIndexOutOfBoundsException e){}
		return false;
	}
	public boolean changeRoom(Exit E, Room R)
	{
		synchronized(exits)
		{
			for(int i=0; i<exits.size();i++)
				if(exits.get(i)==E)
				{
					exitDests.set(i, R);
					return true;
				}
		}
		return false;
	}
	public int getExitIndex(Exit E, Room R)
	{
		for(int i=0; i<exits.size();i++)
			if(exits.get(i)==E)
				if(exitDests.get(i)==R)
					return i;
		return -1;
	}


	public Domain domain(){return myDom;} 
	public Enclosure enclosure(){return myEnc;}

	public void setRoomID(String newID)
	{
/*		if((myID!=null)&&(!myID.equals(newID)))
		{
			myID=newID;
			if(myArea!=null)
			{ 
				// force the re-sort TODO
				myArea.delProperRoom(this);
				myArea.addProperRoom(this);
			}
		}
		else */
		myID=newID;
	}
	public Area getArea()
	{
		if(myArea==null) return CMClass.anyOldArea();
		return myArea;
	}
	public void setArea(Area newArea)
	{
		if(newArea!=myArea)
		{
			if(myArea!=null)
			{
				myArea.delProperRoom(this);
				if(lFlags.contains(ListenHolder.Flags.TICK))
					myArea.removeTickingRoom(this);
			}
			myArea=newArea;
			if(myArea!=null)
			{
				myArea.addProperRoom(this);
				if(lFlags.contains(ListenHolder.Flags.TICK))
					myArea.addTickingRoom(this);
			}
		}
	}
	public void setAreaRaw(Area newArea)
	{
		myArea=newArea;
	}

	public boolean okMessage(OkChecker myHost, CMMsg msg)
	{
		if(!getArea().okMessage(this,msg))
			return false;
		for(int i=okCheckers.size();i>0;i--)
			if(!okCheckers.get(i-1).okMessage(myHost,msg))
				return false;
		return true;
// TODO
	}
	public boolean respondTo(CMMsg msg){return true;}
	//TODO NOTE FOR ITEM ROOMS:
	//Room: If host is not a room, you are new host. Send to container and to items.
	//Room: If host is a room (and not you), do not send to container, but do send to items.
	//Item of Room: If host is a room, do not send to room UNLESS host is your container.
	public void executeMsg(ExcChecker myHost, CMMsg msg)
	{
		getArea().executeMsg(this,msg);
		for(int i=excCheckers.size();i>0;i--)
			excCheckers.get(i-1).executeMsg(myHost,msg);
// TODO
	}

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
	public long lastAct(){return 0;}	//No Action ticks
	public long lastTick(){return lastTick;}

	public void recoverRoomStats()
	{
		//TODO
	}
	public int compareTo(CMObject o){ return CMClass.classID(this).compareToIgnoreCase(CMClass.classID(o));}

// actually this should not be called much. Execute Message should handle most of it instead I think? Maybe this should be called by execute message? Probably not...
	public void bringHere(Item I, boolean andRiders)
	{
		if(I==null) return;
		CMObject o=I.container();
		if((o==null)||(o==inventory)) return;

		if((I instanceof Rideable)&&(!(andRiders)))
		{
			Rideable R=(Rideable)I;
			for(int i=R.numRiders()-1;i>=0;i--)
				R.removeRider(i);
		}
		if(o instanceof ItemCollection)((ItemCollection)o).removeItem(I);

		inventory.addItem(I);
	}

	protected void reallySend(CMMsg msg, int depth)
	{
/*
		if((Log.debugChannelOn())&&(CMSecurity.isDebugging("MESSAGES")))
			Log.debugOut("StdRoom",((msg.source()!=null)?msg.source().ID():"null")+":"+msg.sourceCode()+":"+msg.sourceMessage()+"/"+((msg.target()!=null)?msg.target().ID():"null")+":"+msg.targetCode()+":"+msg.targetMessage()+"/"+((msg.tool()!=null)?msg.tool().ID():"null")+"/"+msg.othersCode()+":"+msg.othersMessage());
*/
		executeMsg(this,msg);
		// now handle trailer msgs
		if(msg.trailerMsgs()!=null)
		{
			if(depth>30)
				Log.errOut("Messages","Excessive message depth: "+msg.toString());
			for(int i=0;i<msg.trailerMsgs().size();i++)
			{
				CMMsg msg2=(CMMsg)msg.trailerMsgs().elementAt(i);
				if((okMessage(this,msg2))&&(msg2.handleResponses()))
					reallySend(msg2,depth+1);
			}
		}
	}

	public void send(CMMsg msg)
	{
		reallySend(msg,0);
	}

	public void showHappens(EnumSet<CMMsg.MsgCode> allCode, Object source, String allMessage)
	{
		reallySend(CMClass.getMsg(source,null,null,allCode,allMessage),0);
	}
	public boolean show(Object source, 
						Interactable target, 
						Object tool, 
						EnumSet<CMMsg.MsgCode> allCode, 
						String allMessage)
	{
		CMMsg msg=CMClass.getMsg(source,target,tool,allCode,allMessage);
		if((!allCode.contains(CMMsg.MsgCode.Always))&&(!okMessage(this,msg)))
			return false;
		send(msg);
		return true;
	}
	public boolean show(Object source,
						Interactable target,
						Object tool,
						EnumSet<CMMsg.MsgCode> srcCode,
						String srcMessage,
						EnumSet<CMMsg.MsgCode> tarCode,
						String tarMessage,
						EnumSet<CMMsg.MsgCode> othCode,
						String othMessage)
	{
		CMMsg msg=CMClass.getMsg(source,target,tool,srcCode,srcMessage,tarCode,tarMessage,othCode,othMessage);
		if((!srcCode.contains(CMMsg.MsgCode.Always))&&(!okMessage(this,msg)))
			return false;
		send(msg);
		return true;
	}

	public void destroy()
	{
		//TODO: This probably needs to be checked further
		try{
		for(int a=numEffects()-1;a>=0;a--)
			fetchEffect(a).unInvoke();
		}catch(Exception e){}
		while(numEffects()>0)
			delEffect(fetchEffect(0));
		try{
			Vector<Item> V=new Vector<Item>();
			for(int v=0;v<inventory.numItems();v++)
				V.addElement(inventory.getItem(v));
			for(int v=0;v<V.size();v++)
				V.elementAt(v).destroy();
			while(inventory.numItems()>0)
				inventory.removeItem(0);
		}catch(Exception e){}
		while(numBehaviors()>0)
			delBehavior(fetchBehavior(0));
		setArea(null); // this actually deletes the room from the cache map
//		baseEnvStats=(EnvStats)CMClass.getCommon("DefaultEnvStats");
//		envStats=baseEnvStats;
		affects=null;
		behaviors=null;
		amDestroyed=true;
	}
	public boolean amDestroyed(){return amDestroyed;}

	//This might not really be necessary. ..meh
	public boolean isContent(Item E, boolean sub)
	{
		return inventory.hasItem(E, sub);
	}
	public Item fetchItem(String itemID)
	{
		Item item=CMLib.english().fetchAvailableItem(inventory.allItems(),itemID,true);
		if(item==null) item=CMLib.english().fetchAvailableItem(inventory.allItems(),itemID,false);
		return item;
	}
	public Vector<Item> fetchItems(String itemID)
	{
		Vector items=CMLib.english().fetchAvailableItems(inventory.allItems(),itemID,true);
		if(items.size()==0)
			items=CMLib.english().fetchAvailableItems(inventory.allItems(),itemID,false);
		return items;
	}
	public Vector<MOB> fetchInhabitants(String inhabitantID)
	{
		Vector<MOB> inhabs=new Vector<MOB>();
		MOB M=null;
		if(inhabitantID.equals("ALL"))
		{
			Vector<Item> items=inventory.allItems();
			for(int i=0;i<items.size();i++)
				if((items.get(i) instanceof Body)&&((M=((Body)items.get(i)).getMOB())!=null))
					inhabs.add(M);
		}
		else
		{
			Vector<Item> items=CMLib.english().fetchAvailableItems(inventory.allItems(),inhabitantID,true);
			for(int i=0;i<items.size();i++)
				if((items.get(i) instanceof Body)&&((M=((Body)items.get(i)).getMOB())!=null))
					inhabs.add(M);
			if(inhabs.size()==0)
			{
				items=CMLib.english().fetchAvailableItems(inventory.allItems(),inhabitantID, false);
				for(int i=0;i<items.size();i++)
					if((items.get(i) instanceof Body)&&((M=((Body)items.get(i)).getMOB())!=null))
						inhabs.add(M);
			}
		}
		return inhabs;
	}
	
	public void initExits()
	{
		if(exitsToLoad==null) return;
		int x=0;
		while((x=exitsToLoad.indexOf(';'))>0)
		{
			Exit newExit=CMLib.map().getExit(exitsToLoad.substring(0,x));
			exitsToLoad=exitsToLoad.substring(x+1);
			x=exitsToLoad.indexOf(';');
			Room newRoom=CMLib.map().getRoom(exitsToLoad.substring(0,x));
			exitsToLoad=exitsToLoad.substring(x+1);
			if((newExit!=null)&&(newRoom!=null))
				addExit(newExit, newRoom);
		}
		exitsToLoad=null;
	}
	public String exitsToString()
	{
		StringBuilder S=new StringBuilder();
		synchronized(exits)
		{
			for(int i=0;i<exits.size();i++)
				S.append(exits.get(i).exitID()+";"+exitDests.get(i).roomID()+";");
		}
		return S.toString();
	}

	//Things listen to this really, not the other way around, so really simple code here.
	public void removeListener(Listener oldAffect, EnumSet flags)
	{
		ListenHolder.O.removeListener(this, oldAffect, flags);
		if((flags.contains(ListenHolder.Flags.TICK))&&(tickActers.isEmpty())&&(lFlags.remove(ListenHolder.Flags.TICK)))
			if(myArea!=null)
				myArea.removeTickingRoom(this);
	}
	public void addListener(Listener newAffect, EnumSet<ListenHolder.Flags> flags)
	{
		ListenHolder.O.addListener(this, newAffect, flags);
		if((flags.contains(ListenHolder.Flags.TICK))&&(!tickActers.isEmpty())&&(lFlags.add(ListenHolder.Flags.TICK)))
			if(myArea!=null)
				myArea.addTickingRoom(this);
	}
	public void registerAllListeners() {}
	public void clearAllListeners() {}
	public Vector<CharAffecter> charAffecters(){return null;}
	public Vector<EnvAffecter> envAffecters(){return null;}
	public Vector<OkChecker> okCheckers(){if(okCheckers==null) okCheckers=new Vector(); return okCheckers;}
	public Vector<ExcChecker> excCheckers(){if(excCheckers==null) excCheckers=new Vector(); return excCheckers;}
	public Vector<TickActer> tickActers(){if(tickActers==null) tickActers=new Vector(); return tickActers;}
	public EnumSet<ListenHolder.Flags> listenFlags() {return lFlags;}

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
	public ItemCollection getItemCollection(){return inventory;}

	public boolean sameAs(Interactable E)
	{
		if(!(E instanceof StdRoom)) return false;
		return true;
	}

	//CMModifiable and CMSavable
	public SaveEnum[] totalEnumS(){return SCode.values();}
	public Enum[] headerEnumS(){return new Enum[] {SCode.values()[0]} ;}
	public ModEnum[] totalEnumM(){return MCode.values();}
	public Enum[] headerEnumM(){return new Enum[] {MCode.values()[0]};}

	private enum SCode implements CMSavable.SaveEnum{
//		ID(){
//			public String save(StdRoom E){ return E.myID; }
//			public void load(StdRoom E, String S){ E.setRoomID(S.intern()); } },
		ENV(){
			public String save(StdRoom E){ return CMLib.coffeeMaker().getPropertiesStr(E.myEnvironmental); }
			public void load(StdRoom E, String S){
				Environmental.DefaultEnv newEnv=new Environmental.DefaultEnv(E);
				CMLib.coffeeMaker().setPropertiesStr(newEnv, S);
				E.myEnvironmental.destroy();
				E.myEnvironmental=newEnv; } },
		DOM(){
			public String save(StdRoom E){ return E.myDom.toString() ; }
			public void load(StdRoom E, String S){ E.myDom=CMClass.valueOf(Domain.class, S); } },
		ENC(){
			public String save(StdRoom E){ return E.myEnc.toString() ; }
			public void load(StdRoom E, String S){ E.myEnc=CMClass.valueOf(Enclosure.class, S); } },
		DSP(){
			public String save(StdRoom E){ return E.display; }
			public void load(StdRoom E, String S){ E.display=S.intern(); } },
		DSC(){
			public String save(StdRoom E){ return E.desc; }
			public void load(StdRoom E, String S){ E.desc=S.intern(); } }, 
		EXT(){
			public String save(StdRoom E){ return E.exitsToString(); }
			public void load(StdRoom E, String S){ E.exitsToLoad=S.intern(); } },
		ARE(){
			public String save(StdRoom E){ return E.myArea; }
			public void load(StdRoom E, String S){ E.setArea(CMLib.map().getArea(S)); } },
		INV(){
			public String save(StdRoom E){ return CMLib.coffeeMaker().getPropertiesStr(E.inventory); }
			public void load(StdRoom E, String S){
				ItemCollection.DefaultItemCol newInv=new ItemCollection.DefaultItemCol(E);
				CMLib.coffeeMaker().setPropertiesStr(newInv, S);
				E.inventory.destroy();
				E.inventory=newInv; } },
		EFC(){
			public String save(StdRoom E){ return CMLib.coffeeMaker().getVectorStr(E.affects); }
			public void load(StdRoom E, String S){
				Vector<Effect> V=CMLib.coffeeMaker().setVectorStr(S);
				for(Effect A : V)
					E.addEffect(A);
				
				} },
		BHV(){
			public String save(StdRoom E){ return CMLib.coffeeMaker().getVectorStr(E.behaviors); }
			public void load(StdRoom E, String S){
				Vector<Behavior> V=CMLib.coffeeMaker().setVectorStr(S);
				for(Behavior A : V)
					E.addBehavior(A);
				
				} },
		NAM(){
			public String save(StdRoom E){ return ""+E.name; }
			public void load(StdRoom E, String S){ E.name=S.intern(); } }
		//TODO: EXT()
		;
		public abstract String save(StdRoom E);
		public abstract void load(StdRoom E, String S);
		public String save(CMSavable E){return save((StdRoom)E);}
		public void load(CMSavable E, String S){load((StdRoom)E, S);} }
	private enum MCode implements CMModifiable.ModEnum{
		ID(){
			public String brief(StdRoom E){return E.myID;}
			public String prompt(StdRoom E){return E.myID;}
			public void mod(StdRoom E, MOB M){
				//E.setRoomID(CMLib.genEd().stringPrompt(M, ""+E.myID, false));
				M.session().rawPrintln("This value cannot be manually edited");} },
		ENVIRONMENTAL(){
			public String brief(StdRoom E){return E.myEnvironmental.ID();}
			public String prompt(StdRoom E){return "";}
			public void mod(StdRoom E, MOB M){CMLib.genEd().genMiscSet(M, E.myEnvironmental);} },
		DOMAIN(){
			public String brief(StdRoom E){return E.myDom.toString();}
			public String prompt(StdRoom E){return E.myDom.toString();}
			public void mod(StdRoom E, MOB M){E.myDom=CMLib.genEd().enumPrompt(M, E.myDom.toString(), Domain.values());} },
		ENCLOSURE(){
			public String brief(StdRoom E){return E.myEnc.toString();}
			public String prompt(StdRoom E){return E.myEnc.toString();}
			public void mod(StdRoom E, MOB M){E.myEnc=CMLib.genEd().enumPrompt(M, E.myEnc.toString(), Enclosure.values());} },
		DISPLAY(){
			public String brief(StdRoom E){return E.display;}
			public String prompt(StdRoom E){return E.display;}
			public void mod(StdRoom E, MOB M){E.display=CMLib.genEd().stringPrompt(M, E.display, false);} },
		DESCRIPTION(){
			public String brief(StdRoom E){return E.desc;}
			public String prompt(StdRoom E){return E.desc;}
			public void mod(StdRoom E, MOB M){E.desc=CMLib.genEd().stringPrompt(M, E.desc, false);} }, 
		AREA(){
			public String brief(StdRoom E){return E.myArea.name();}
			public String prompt(StdRoom E){return E.myArea.name();}
			public void mod(StdRoom E, MOB M){ E.setArea(CMLib.map().getArea(CMLib.genEd().stringPrompt(M, E.myArea.name(), false)));} },
		EXITS(){
			public String brief(StdRoom E){return ""+E.exits.size();}
			public String prompt(StdRoom E){return "";}
			public void mod(StdRoom E, MOB M){CMLib.genEd().modExits(E.exits, E.exitDests, M);} },
		EFFECTS(){
			public String brief(StdRoom E){return ""+E.affects.size();}
			public String prompt(StdRoom E){return "";}
			public void mod(StdRoom E, MOB M){CMLib.genEd().modAffectable(E, M);} },
		BEHAVIORS(){
			public String brief(StdRoom E){return ""+E.behaviors.size();}
			public String prompt(StdRoom E){return "";}
			public void mod(StdRoom E, MOB M){CMLib.genEd().modBehavable(E, M);} },
		NAME(){
			public String brief(StdRoom E){return E.name;}
			public String prompt(StdRoom E){return E.name;}
			public void mod(StdRoom E, MOB M){E.name=CMLib.genEd().stringPrompt(M, E.name, false);} },
		INVENTORY(){
			public String brief(StdRoom E){return E.inventory.ID()+" "+E.inventory.numItems();}
			public String prompt(StdRoom E){return "";}
			public void mod(StdRoom E, MOB M){CMLib.genEd().genMiscSet(M, E.inventory);} },
		//TODO: Exits
		;
		public abstract String brief(StdRoom fromThis);
		public abstract String prompt(StdRoom fromThis);
		public abstract void mod(StdRoom toThis, MOB M);
		public String brief(CMModifiable fromThis){return brief((StdRoom)fromThis);}
		public String prompt(CMModifiable fromThis){return prompt((StdRoom)fromThis);}
		public void mod(CMModifiable toThis, MOB M){mod((StdRoom)toThis, M);} }
}
