package com.planet_ink.coffee_mud.Items.Basic;
import com.planet_ink.coffee_mud.core.interfaces.*;
import com.planet_ink.coffee_mud.core.*;
import com.planet_ink.coffee_mud.Libraries.*;

import java.util.*;
import java.nio.ByteBuffer;
import java.util.concurrent.CopyOnWriteArrayList;

/*
CoffeeMUD 5.6.2 copyright 2000-2010 Bo Zimmerman
EspressoMUD copyright 2011 Kejardon

Licensed under the Apache License, Version 2.0. You may obtain a copy of the license at
	http://www.apache.org/licenses/LICENSE-2.0
*/

public class MixedItem implements Item
{
	@Override public String ID(){	return "MixedItem";}

	protected String name="an ordinary item";
	protected String display="a nondescript item sits here doing nothing.";
	protected String desc="";
	protected String plainName;
	protected String plainNameOf;
	protected String plainDisplay;
	protected String plainDisplayOf;
	protected String plainDesc;
	protected String plainDescOf;
	protected String stackName="";
	protected int baseGoldValue=0;
	//protected boolean damagable=false;	//Will probably move to environmental later
	//protected int wornOut=0;	//Will probably move to environmental later
	protected EnumSet<ListenHolder.Flags> lFlags=EnumSet.of(ListenHolder.Flags.OK,ListenHolder.Flags.EXC);
	protected CopyOnWriteArrayList<OkChecker> okCheckers=new CopyOnWriteArrayList();
	protected CopyOnWriteArrayList<ExcChecker> excCheckers=new CopyOnWriteArrayList();
	protected CopyOnWriteArrayList<TickActer> tickActers=new CopyOnWriteArrayList();
	protected CopyOnWriteArrayList<Effect> affects=new CopyOnWriteArrayList();
	protected CopyOnWriteArrayList<Behavior> behaviors=new CopyOnWriteArrayList();
	protected int tickCount=0;
	protected Tickable.TickStat tickStatus=Tickable.TickStat.Not;
	//Should this be ItemCollection? No.
	protected CMObject container=null;
	protected CMObject ride=null;
	protected boolean destroyed=false;
	protected Environmental myEnvironmental=null;//(Environmental)((Ownable)CMClass.COMMON.getNew("DefaultEnvironmental")).setOwner(this);
	protected BindCollection binds=null;//(BindCollection)((Ownable)CMClass.COMMON.getNew("DefaultBindCollection")).setOwner(this);

	protected int saveNum=0;
	protected int[] effectsToLoad=null;
	protected int[] behavesToLoad=null;
	protected int rideToLoad=0;
	protected int bindsToLoad=0;

	public MixedItem(){}
	public MixedItem(MixedItem original, BindCollection col)
	{
		binds=col;
		//TODO: Copy/modify over String stuff
	}
	public MixedItem(BindCollection col)
	{ binds=col; }

	public Environmental getEnvObject() {
		if(myEnvironmental==null)
			synchronized(this) { if(myEnvironmental==null) {
				myEnvironmental=(Environmental)((Ownable)CMClass.COMMON.getNew("DefaultEnvironmental")).setOwner(this);
				resetEnvObject();
			} }
		return myEnvironmental;
	}
	public void resetEnvObject()
	{
		//TODO: Recalc environmental based on child objects
	}
	public boolean isComposite(){return true;}
	public BindCollection subItems(){
		if(binds==null)
			synchronized(this) { if(binds==null) {
				binds=(BindCollection)((Ownable)CMClass.COMMON.getNew("DefaultBindCollection")).setOwner(this);
				//resetEnvObject();
			} }
		return binds;
	}

	public int priority(ListenHolder L){return Integer.MAX_VALUE;}
	public void registerListeners(ListenHolder here) { here.addListener(this, lFlags); }
	public void registerAllListeners()
	{
		if(container instanceof ListenHolder)
			((ListenHolder)container).addListener(this, lFlags);
	}
	public void clearAllListeners()
	{
		if(container instanceof ListenHolder)
			((ListenHolder)container).removeListener(this, lFlags);
	}
	@Override public void initializeClass(){}
	public void setName(String newName){name=newName; CMLib.database().saveObject(this);}
	public String name(){ return name;}
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
	public String displayText(){return display;}
	public void setDisplayText(String newDisplayText){display=newDisplayText; CMLib.database().saveObject(this);}
	public String plainDisplayText()
	{
		if(display==plainDisplayOf)
			return plainDisplay;
		String newDisplay=display;
		String newPlain=CMLib.coffeeFilter().toRawString(newDisplay);
		synchronized(this)
		{
			plainDisplay=newPlain;
			plainDisplayOf=newDisplay;
		}
		return newPlain;
	}
	public String description(){return desc;}
	public void setDescription(String newDescription){desc=newDescription; CMLib.database().saveObject(this);}
	public String plainDescription()
	{
		if(desc==plainDescOf)
			return plainDesc;
		String newDesc=desc;
		String newPlain=CMLib.coffeeFilter().toRawString(newDesc);
		synchronized(this)
		{
			plainDesc=newPlain;
			plainDescOf=newDesc;
		}
		return newPlain;
	}
	public String stackableName(){return stackName;}
	public void setStackableName(String newSN){stackName=newSN; CMLib.database().saveObject(this);}
	public CMObject ride(){return ride;}
	public void setRide(CMObject R){ride=R; CMLib.database().saveObject(this);}
	public CMObject container(){return container;}
	public void setContainer(CMObject E)
	{
		if(E!=container)
		{
			clearAllListeners();
			container=E;
		}
		registerAllListeners();
		//CMLib.database().saveObject(this);
	}

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
	public Effect fetchFirstEffect(String ID)
	{
		for(Effect E : affects)
			if(E.ID().equals(ID))
				return E;
		return null;
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

	//Affectable/Behavable shared
	public CopyOnWriteArrayList<CharAffecter> charAffecters(){return null;}
	public CopyOnWriteArrayList<EnvAffecter> envAffecters(){return null;}	//TODO: Should this give the environmental's instead?
	public CopyOnWriteArrayList<OkChecker> okCheckers(){return okCheckers;}
	public CopyOnWriteArrayList<ExcChecker> excCheckers(){return excCheckers;}
	public CopyOnWriteArrayList<TickActer> tickActers(){return tickActers;}
	public void removeListener(Listener oldAffect, EnumSet flags)
	{
		ListenHolder.O.removeListener(this, oldAffect, flags);
		if((flags.contains(ListenHolder.Flags.TICK))&&(tickActers.isEmpty())&&(lFlags.remove(ListenHolder.Flags.TICK)))
			if(container instanceof ListenHolder)
				((ListenHolder)container).removeListener(this, EnumSet.of(ListenHolder.Flags.TICK));
	}
	public void addListener(Listener newAffect, EnumSet flags)
	{
		ListenHolder.O.addListener(this, newAffect, flags);
		if((flags.contains(ListenHolder.Flags.TICK))&&(!tickActers.isEmpty())&&(lFlags.add(ListenHolder.Flags.TICK)))
			if(container instanceof ListenHolder)
				((ListenHolder)container).addListener(this, EnumSet.of(ListenHolder.Flags.TICK));
	}
	public EnumSet<ListenHolder.Flags> listenFlags() {return lFlags;}
	//Tickable
	public Tickable.TickStat getTickStatus(){return tickStatus;}
	public int tickCounter(){return tickCount;}
	public void tickAct(){}
	public boolean tick(int tickTo)
	{
		if(tickCount==0) tickCount=tickTo-1;
		else if(tickTo>tickCount+10)
			tickTo=tickCount+10;
		while(tickCount<tickTo)
		{
			tickCount++;
			if(!doTick()) {tickCount=0; lFlags.remove(ListenHolder.Flags.TICK); return false;}	//Possibly also lFlags.remove(ListenHolder.Flags.TICK);
		}
		return true;
	}
	protected boolean doTick()
	{
		tickStatus=Tickable.TickStat.Listener;
		for(TickActer T : tickActers)
			if(!T.tick(tickCount))
				tickActers.remove(T);
		tickStatus=Tickable.TickStat.Not;
		return (!tickActers.isEmpty());
	}
	//public long lastAct(){return 0;}	//No Action ticks
	//public long lastTick(){return lastTick;}

	@Override public MixedItem newInstance()
	{
		try
		{
			return getClass().newInstance();
		}
		catch(Exception e)
		{
			Log.errOut(ID(),e);
		}
		return new MixedItem();
	}
	protected void cloneFix(MixedItem E)
	{
//		destroyed=false;
		okCheckers=new CopyOnWriteArrayList();
		excCheckers=new CopyOnWriteArrayList();
		tickActers=new CopyOnWriteArrayList();
		affects=new CopyOnWriteArrayList();
		behaviors=new CopyOnWriteArrayList();
		lFlags=lFlags.clone();
		ride=null;
		tickStatus=Tickable.TickStat.Not;
		tickCount=0;
		container=null;
		binds=E.binds.copyOf();
		if(myEnvironmental!=null) myEnvironmental=(Environmental)((Ownable)myEnvironmental.copyOf()).setOwner(this);

		for(Effect A : E.affects)
			affects.add(A.copyOnto(this));
		for(Behavior B : E.behaviors)
			addBehavior(B.copyOf());
	}
	@Override public MixedItem copyOf()
	{
		try
		{
			MixedItem E=(MixedItem)this.clone();
			E.saveNum=0;
			E.cloneFix(this);
			return E;
		}
		catch(CloneNotSupportedException e)
		{
			return this.newInstance();
		}
	}

	public int value()
	{
		return baseGoldValue();
	}
	public int baseGoldValue(){return baseGoldValue;}
	public void setBaseValue(int newValue) { baseGoldValue=newValue; CMLib.database().saveObject(this);}
	public int wornOut()
	{
		int numWornItems = 0;
		int wornOut = 0;
		for(Item item : binds.itemArray())
		{
			if(item.damagable())
			{
				wornOut+=item.wornOut();
				numWornItems++;
			}
		}
		if(numWornItems > 0)
			return wornOut/numWornItems;
		return 0;
	}
	public void setWornOut(int worn) {}
	public boolean damagable()
	{
		for(Item item : binds.itemArray())
			if(item.damagable())
				return true;
		return false;
	}
	public void setDamagable(boolean bool){}

	public int compareTo(CMObject o){ return CMClass.classID(this).compareToIgnoreCase(CMClass.classID(o));}

	@Override public boolean okMessage(ListenHolder.OkChecker myHost, CMMsg msg)
	{
		Interactable target=msg.target();
		for(CMMsg.MsgCode code : msg.othersCode())
		switch(code)
		{
			case GET:
				if(target==this)
					msg.addResponse(ListenHolder.InbetweenListener.newListener(ItemGetResponse, this), 9);
				break;
		}
		if(!getEnvObject().okMessage(myHost, msg))
			return false;
		for(OkChecker O : okCheckers)
			if(!O.okMessage(myHost,msg))
				return false;
		return true;
	}
	protected static ListenHolder.DummyListener ItemGetResponse = new ListenHolder.DummyListener()
	{
		@Override public boolean respondTo(CMMsg msg, Object data)
		{
			MixedItem item = (MixedItem)data;
			Interactable I = msg.firstSource();
			if(!(I instanceof MOB)) return false;
			if(msg.hasOthersCode(CMMsg.MsgCode.ALWAYS)) return true;
			MOB mob=(MOB)I;
			if(!mob.getItemCollection().canHold(item))
			{
				mob.tell("You can't pick it up!");
				return false;
			}
			return true;
		}
	};
	@Override public boolean respondTo(CMMsg msg, Object data){return true;}
	@Override public boolean respondTo(CMMsg msg){ return true;}
	@Override public void executeMsg(ListenHolder.ExcChecker myHost, CMMsg msg)
	{
		Interactable target=msg.target();
		for(CMMsg.MsgCode code : msg.othersCode())
		switch(code)
		{
			case GET:
				if(target==this)
				{
					Interactable I = msg.firstSource();
					if(!(I instanceof MOB)) break;
					MOB mob=(MOB)I;
					mob.giveItem(this);
				}
				break;
			case DROP:
				if(msg.isTool(this))
				{
					Interactable I = target;
					if(I==null) I = msg.firstSource();
					Room targetRoom=null;
					if(I instanceof Item)
						targetRoom=CMLib.map().roomLocation(((Item)I).container());
					else if(I instanceof MOB)
						targetRoom=((MOB)I).location();
					if(targetRoom!=null)
						targetRoom.bringHere(this, true);
					if(target!=null)
					{
						Rideable ride = Rideable.O.getFrom(target);
						if((ride!=null)&&(ride.canBeRidden(this)))
							ride.addRider(this);
					}
				}
				break;
			case LOOK:
				if(target==this)
					Item.O.handleBeingLookedAt(this, msg, false);
				break;
			case EXAMINE:
				if(target==this)
				{
					Interactable I = msg.firstSource();
					if(!(I instanceof MOB)) break;
					Item.O.handleBeingLookedAt(this, msg, true);
					Item[] subitems = binds.itemArray();
					StringBuilder buf=new StringBuilder("\r\nIt is made up of ");
					for(Item item : subitems)
					{
						buf.append(item.name());
						buf.append(", ");
					}
					buf.setCharAt(buf.length()-2, '.');
					((MOB)I).tell(buf.toString());
				}
				break;
		}
		getEnvObject().executeMsg(myHost, msg);
		for(ExcChecker O : excCheckers)
			O.executeMsg(myHost, msg);
	}

	public int recursiveWeight()
	{
		int weight=getEnvObject().envStats().weight();
		ItemCollection subItems=ItemCollection.O.getFrom(this);
		if(binds!=null)
		for(Iterator<Item> iter=binds.allItems();iter.hasNext();)
			weight+=iter.next().recursiveWeight();
		return weight;
	}

	public void destroy()
	{
		clearAllListeners();
		if(myEnvironmental!=null)
			myEnvironmental.destroy();

		destroyed=true;

		for(Effect E : affects)
			E.setAffectedOne(null);
		affects.clear();
		for(Behavior B : behaviors)
			B.startBehavior(null);
		behaviors.clear();
		ItemCollection owner=ItemCollection.O.getFrom(container);
		if(owner!=null)
			owner.removeItem(this);
		Rideable rideable=Rideable.O.getFrom(ride);
		if(rideable!=null)
			rideable.removeRider(this);
		ItemCollection inv=ItemCollection.O.getFrom(this);
		if(inv!=null)
		{
			if(owner==null)
				for(Iterator<Item> iter=inv.allItems();iter.hasNext();)
					iter.next().destroy();
			else
				for(Iterator<Item> iter=inv.allItems();iter.hasNext();)
					owner.addItem(iter.next());
		}
		if(saveNum!=0)	//NOTE: I think this should be a standard destroy() check?
			CMLib.database().deleteObject(this);
	}
	public boolean amDestroyed(){return destroyed;}

	//CMModifiable and CMSavable
	@Override public SaveEnum[] totalEnumS(){return SCode.values();}
	@Override public Enum[] headerEnumS(){return new Enum[] {SCode.values()[0]} ;}
	@Override public ModEnum[] totalEnumM(){return MCode.values();}
	@Override public Enum[] headerEnumM(){return new Enum[] {MCode.values()[0]};}
	public int saveNum()
	{
		if((saveNum==0)&&(!destroyed))
		synchronized(this)
		{
			if(saveNum==0)
				saveNum=SIDLib.ITEM.getNumber(this);
		}
		return saveNum;
	}
	public void setSaveNum(int num)
	{
		synchronized(this)
		{
			if(saveNum!=0)
				SIDLib.ITEM.removeNumber(saveNum);
			saveNum=num;
			SIDLib.ITEM.assignNumber(num, this);
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
		if(rideToLoad!=0)
		{
			Rideable rideable=SIDLib.RIDEABLE.get(rideToLoad);
			if(rideable!=null)
			{
				rideable.addRider(this);
				ride=Ownable.O.getOwnerFrom(rideable);
			}
		}
		if(bindsToLoad!=0)
		{
			BindCollection oldBinds=binds;
			binds=SIDLib.BINDCOLLECTION.get(bindsToLoad);
			if(binds!=null)
			{
				((Ownable)binds).setOwner(this);
				//Ideally never happens
				if(oldBinds!=null)
				{
					for(Iterator<Bind> iter=oldBinds.allBinds();iter.hasNext();)
					{
						Bind next=iter.next();
						next.setOwner(binds);
						binds.addBind(next);
					}
					oldBinds.destroy();
				}
			} else {
				binds = oldBinds;
			}
			bindsToLoad=0;
		}
	}
	public void saveThis(){CMLib.database().saveObject(this);}
	public void prepDefault(){} //TODO: Env

	private enum SCode implements SaveEnum<MixedItem>{
		NAM(){
			public ByteBuffer save(MixedItem E){ return CMLib.coffeeMaker().savString(E.name); }
			public int size(){return 0;}
			public void load(MixedItem E, ByteBuffer S){ E.name=CMLib.coffeeMaker().loadString(S); } },
		DSP(){
			public ByteBuffer save(MixedItem E){ return CMLib.coffeeMaker().savString(E.display); }
			public int size(){return 0;}
			public void load(MixedItem E, ByteBuffer S){ E.display=CMLib.coffeeMaker().loadString(S); } },
		DSC(){
			public ByteBuffer save(MixedItem E){ return CMLib.coffeeMaker().savString(E.desc); }
			public int size(){return 0;}
			public void load(MixedItem E, ByteBuffer S){ E.desc=CMLib.coffeeMaker().loadString(S); } },
		STC(){
			public ByteBuffer save(MixedItem E){ return CMLib.coffeeMaker().savString(E.stackName); }
			public int size(){return 0;}
			public void load(MixedItem E, ByteBuffer S){ E.stackName=CMLib.coffeeMaker().loadString(S); } },
		VAL(){
			public ByteBuffer save(MixedItem E){ return (ByteBuffer)ByteBuffer.wrap(new byte[4]).putInt(E.baseGoldValue).rewind(); }
			public int size(){return 4;}
			public void load(MixedItem E, ByteBuffer S){ E.baseGoldValue=S.getInt(); } },
		RNM(){	//NOTE: this may be more efficient as a var enum since it's not often used..
			public ByteBuffer save(MixedItem E){
				Rideable ride=Rideable.O.getFrom(E.ride);
				if(ride!=null)
					return (ByteBuffer)ByteBuffer.wrap(new byte[4]).putInt(ride.saveNum()).rewind();
				return (ByteBuffer)ByteBuffer.wrap(new byte[4]).putInt(0).rewind(); }
			public int size(){return 4;}
			public void load(MixedItem E, ByteBuffer S){ E.rideToLoad=S.getInt(); } },
		ENV(){
			public ByteBuffer save(MixedItem E){ return CMLib.coffeeMaker().savSubFull(E.getEnvObject()); }
			public int size(){return -1;}
			public CMSavable subObject(MixedItem fromThis){return fromThis.myEnvironmental;}
			public void load(MixedItem E, ByteBuffer S){
				Environmental old=E.myEnvironmental;
				E.myEnvironmental=(Environmental)CMLib.coffeeMaker().loadSub(S, E, this);
				if(E.myEnvironmental!=null) ((Ownable)E.myEnvironmental).setOwner(E);
				if((old!=null)&&(old!=E.myEnvironmental)) old.destroy(); } },
		EFC(){
			public ByteBuffer save(MixedItem E){
				if(E.affects.size()>0) return CMLib.coffeeMaker().savSaveNums((CMSavable[])E.affects.toArray(CMSavable.dummyCMSavableArray));
				return CoffeeMaker.emptyBuffer; }
			public int size(){return 0;}
			public void load(MixedItem E, ByteBuffer S){ E.effectsToLoad=CMLib.coffeeMaker().loadAInt(S); } },
		BHV(){
			public ByteBuffer save(MixedItem E){
				if(E.behaviors.size()>0) return CMLib.coffeeMaker().savSaveNums((CMSavable[])E.behaviors.toArray(CMSavable.dummyCMSavableArray));
				return CoffeeMaker.emptyBuffer; }
			public int size(){return 0;}
			public void load(MixedItem E, ByteBuffer S){ E.behavesToLoad=CMLib.coffeeMaker().loadAInt(S); } },
		BND(){
			public ByteBuffer save(MixedItem E){
				return (ByteBuffer)ByteBuffer.wrap(new byte[4]).putInt(E.binds.saveNum()).rewind(); }
			public int size(){return 4;}
			public void load(MixedItem E, ByteBuffer S){ E.bindsToLoad=S.getInt(); } },
		;
		public CMSavable subObject(MixedItem fromThis){return null;} }
	private enum MCode implements ModEnum<MixedItem>{
		NAME(){
			public String brief(MixedItem E){return ""+E.name;}
			public String prompt(MixedItem E){return ""+E.name;}
			public void mod(MixedItem E, MOB M){E.setName(CMLib.genEd().stringPrompt(M, ""+E.name, false));} },
		DISPLAY(){
			public String brief(MixedItem E){return E.display;}
			public String prompt(MixedItem E){return ""+E.display;}
			public void mod(MixedItem E, MOB M){E.setDisplayText(CMLib.genEd().stringPrompt(M, ""+E.display, false));} },
		DESCRIPTION(){
			public String brief(MixedItem E){return E.desc;}
			public String prompt(MixedItem E){return ""+E.desc;}
			public void mod(MixedItem E, MOB M){E.setDescription(CMLib.genEd().stringPrompt(M, ""+E.display, false));} },
		STACKNAME(){
			public String brief(MixedItem E){return E.stackName;}
			public String prompt(MixedItem E){return ""+E.stackName;}
			public void mod(MixedItem E, MOB M){E.setStackableName(CMLib.genEd().stringPrompt(M, ""+E.stackName, false));} },
		VALUE(){
			public String brief(MixedItem E){return ""+E.baseGoldValue;}
			public String prompt(MixedItem E){return ""+E.baseGoldValue;}
			public void mod(MixedItem E, MOB M){E.setBaseValue(CMLib.genEd().intPrompt(M, ""+E.baseGoldValue));} },
		ENVIRONMENTAL(){
			public String brief(MixedItem E){return E.getEnvObject().ID();}
			public String prompt(MixedItem E){return "";}
			public void mod(MixedItem E, MOB M){CMLib.genEd().genMiscSet(M, E.myEnvironmental);} },
		EFFECTS(){
			public String brief(MixedItem E){return ""+E.affects.size();}
			public String prompt(MixedItem E){return "";}
			public void mod(MixedItem E, MOB M){CMLib.genEd().modAffectable(E, M);} },
		BEHAVIORS(){
			public String brief(MixedItem E){return ""+E.behaviors.size();}
			public String prompt(MixedItem E){return "";}
			public void mod(MixedItem E, MOB M){CMLib.genEd().modBehavable(E, M);} },
		BINDS(){
			public String brief(MixedItem E){return E.subItems().ID();}
			public String prompt(MixedItem E){return "";}
			public void mod(MixedItem E, MOB M){CMLib.genEd().genMiscSet(M, E.binds);} },
		; }
	public boolean sameAs(Interactable E)
	{
/*TODO
		if(!(E instanceof StdItem)) return false;
		return true;
*/
		return false;
	}
}