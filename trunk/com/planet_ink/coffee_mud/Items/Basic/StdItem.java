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

public class StdItem implements Item
{
	public String ID(){	return "StdItem";}

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
	//protected String miscText="";
	protected int baseGoldValue=0;
	protected boolean damagable=false;	//Will probably move to environmental later
	protected int wornOut=0;	//Will probably move to environmental later
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
//	protected int material=RawMaterial.RESOURCE_COTTON;	//Should be in environmental
	protected boolean destroyed=false;
	protected Environmental myEnvironmental=null;//(Environmental)((Ownable)CMClass.COMMON.getNew("DefaultEnvironmental")).setOwner(this);
	protected int saveNum=0;
	protected int[] effectsToLoad=null;
	protected int[] behavesToLoad=null;
	protected int rideToLoad=0;

	public Environmental getEnvObject() {
		if(myEnvironmental==null)
			synchronized(this){if(myEnvironmental==null) myEnvironmental=(Environmental)((Ownable)CMClass.COMMON.getNew("DefaultEnvironmental")).setOwner(this);}
		return myEnvironmental;
	}
	public boolean isComposite(){return false;}
	public BindCollection subItems(){return null;}

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
	public StdItem(){}
	public void initializeClass(){}
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
	//public void setMiscText(String newMiscText){miscText=newMiscText; CMLib.database().saveObject(this);}
	//public String text(){return miscText;}
	//public int ridesNumber(){return ridesNumber;}
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

	public StdItem newInstance()
	{
		try
		{
			return this.getClass().newInstance();
		}
		catch(Exception e)
		{
			Log.errOut(ID(),e);
		}
		return new StdItem();
	}
	protected void cloneFix(StdItem E)
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
		if(myEnvironmental!=null) myEnvironmental=(Environmental)((Ownable)myEnvironmental.copyOf()).setOwner(this);

		for(Effect A : E.affects)
			affects.add(A.copyOnto(this));
		for(Behavior B : E.behaviors)
			addBehavior((Behavior)B.copyOf());
	}
	public StdItem copyOf()
	{
		try
		{
			StdItem E=(StdItem)this.clone();
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
		if(damagable)
			return wornOut;
		return 0;
	}
	public void setWornOut(int worn) { wornOut=worn; CMLib.database().saveObject(this);}
	public boolean damagable() {return damagable;}
	public void setDamagable(boolean bool){damagable=bool; CMLib.database().saveObject(this);}


//TODO
	public int compareTo(CMObject o){ return CMClass.classID(this).compareToIgnoreCase(CMClass.classID(o));}

	public boolean okMessage(ListenHolder.OkChecker myHost, CMMsg msg)
	{
		Interactable target=msg.target();
		for(CMMsg.MsgCode code : msg.othersCode())
		switch(code)
		{
			case GET:
				if(target==this)
					msg.addResponse(ListenHolder.InbetweenListener.newListener(ItemGetResponse,this), 9);
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
		public boolean respondTo(CMMsg msg, Object data)
		{
			StdItem item = (StdItem)data;
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
	public boolean respondTo(CMMsg msg, Object data){return true;}
	public boolean respondTo(CMMsg msg){return true;}
	public void executeMsg(ListenHolder.ExcChecker myHost, CMMsg msg)
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
					Item.O.handleBeingLookedAt(this, msg, true);
				break;
		}
		getEnvObject().executeMsg(myHost, msg);
		for(ExcChecker O : excCheckers)
			O.executeMsg(myHost, msg);
	}
	protected void subLook(CMMsg msg, MOB mob, boolean longLook, StringBuilder buf)
	{
		/*
		if(item instanceof Container)
		{
			buf.append("\r\n");
			Container contitem=(Container)item;
			if((contitem.isOpen())
			&&((contitem.capacity()>0)
				||(contitem.getContents().size()>0)
				||((contitem instanceof Drink)&&(((Drink)contitem).liquidRemaining()>0))))
			{
				buf.append(item.name()+" contains:^<!ENTITY container \""+item.name()+"\"^>\r\n");
				Vector newItems=new Vector();
				if((item instanceof Drink)&&(((Drink)item).liquidRemaining()>0))
				{
					RawMaterial l=(RawMaterial)CMClass.getItem("GenLiquidResource");
					int myResource=((Drink)item).liquidType();
					l.setMaterial(myResource);
					((Drink)l).setLiquidType(myResource);
					l.setBaseValue(RawMaterial.CODES.VALUE(myResource));
					l.baseEnvStats().setWeight(1);
					String name=RawMaterial.CODES.NAME(myResource).toLowerCase();
					l.setName("some "+name);
					l.setDisplayText("some "+name+" sits here.");
					l.setDescription("");
					CMLib.materials().addEffectsToResource(l);
					l.recoverEnvStats();
					newItems.addElement(l);
				}

				if(item.owner() instanceof MOB)
				{
					MOB M=(MOB)item.owner();
					for(int i=0;i<M.numItems();i++)
					{
						Item item2=M.getItem(i);
						if((item2!=null)&&(item2.container()==item))
							newItems.addElement(item2);
					}
					buf.append(CMLib.lister().lister(mob,newItems,true,"CMItem","",false));
				}
				else
				if(item.owner() instanceof Room)
				{
					Room room=(Room)item.owner();
					if(room!=null)
					for(int i=0;i<room.numItems();i++)
					{
						Item item2=room.getItem(i);
						if((item2!=null)&&(item2.container()==item))
							newItems.addElement(item2);
					}
					buf.append(CMLib.lister().lister(mob,newItems,true,"CRItem","",false));
				}
			}
			else
			if((contitem.hasALid())&&((contitem.capacity()>0)||(contitem.getContents().size()>0)))
				buf.append(item.name()+" is closed.");
		}
		*/

		if(longLook)
		{
			if(mob.isMine(this))
			{
				int realWeight=getEnvObject().envStats().weight();
				int divider;
				if(realWeight<=1500)
					divider=4;
				else divider=realWeight/250+CMath.random(5)-3;
	
				int l=realWeight/divider;
				int result=l*divider+l/2;
				//String weight=(int)Math.round(CMath.mul(l,divider))+"-"+(int)Math.round(CMath.mul(l,divider)+(divider-1.0));
	
				buf.append("\r\n")
					.append(CMStrings.capitalizeFirstLetter(name()))
					.append(" weighs about ")
					.append(result/1000.0)
					.append(" stones. ");
			}
			/*
			if((mob!=null)&&(mob.charStats().getStat(CharStats.STAT_INTELLIGENCE)<10))
				buf.append("It is mostly made of a kind of ").append(RawMaterial.MATERIAL_NOUNDESCS[(item.material()&RawMaterial.MATERIAL_MASK)>>8].toLowerCase()).append(". ");
			else
				buf.append("It is mostly made of ").append(RawMaterial.CODES.NAME(item.material()).toLowerCase()).append(". ");
			if((item instanceof Weapon)&&((mob==null)||mob.charStats().getStat(CharStats.STAT_INTELLIGENCE)>10)) {
				buf.append("It is a ");
					.append(CMStrings.capitalizeAndLower(Weapon.CLASS_DESCS[((Weapon)item).weaponClassification()]))
					.append(" class weapon that does ")
					.append(CMStrings.capitalizeAndLower(Weapon.TYPE_DESCS[((Weapon)item).weaponType()]))
					.append(" damage. ");
			}
			else
			if((item instanceof Wearable)&&((mob==null)||mob.charStats().getStat(CharStats.STAT_INTELLIGENCE)>10))
			{
				if(item.envStats().height()>0)
					buf.append(" It is a size "+item.envStats().height()+", and is ");
				else
					buf.append(" It is your size, and is ");
				buf.append(((item.rawProperLocationBitmap()==Wearable.WORN_HELD)||(item.rawProperLocationBitmap()==(Wearable.WORN_HELD|Wearable.WORN_WIELD)))
									 ?new StringBuilder("")
									 :new StringBuilder("worn on the "));
				Wearable.CODES codes = Wearable.CODES.instance();
				for(long wornCode : codes.all())
					if((wornCode != Wearable.IN_INVENTORY)
					&&(CMath.bset(item.rawProperLocationBitmap(),wornCode)))
					{
						String wornString=codes.name(wornCode);
						if(wornString.length()>0)
						{
							buf.append(CMStrings.capitalizeAndLower(wornString)+" ");
							if(item.rawLogicalAnd())
								buf.append("and ");
							else
								buf.append("or ");
						}
					}
				if(buf.toString().endsWith(" and "))
					buf.delete(buf.length()-5,buf.length());
				else
				if(buf.toString().endsWith(" or "))
					buf.delete(buf.length()-4,buf.length());
				buf.append(".  ");
			}
			*/
		}
	}
	protected void handleBeingLookedAt(CMMsg msg, boolean longLook)
	{
		for(Interactable I : msg.sourceArr())
		{
			if(!(I instanceof MOB)) continue;
			MOB mob=(MOB)I;
			if(mob.session()==null||mob.playerStats()==null) continue; // no need for monsters to build all this data

			StringBuilder buf=new StringBuilder("");
			if(mob.playerStats().hasBits(PlayerStats.ATT_SYSOPMSGS))
				buf.append("\r\nType  :").append(ID());
			if(description().length()==0)
				buf.append("You don't see anything special about "+name());
			else
				buf.append(description());
			subLook(msg, mob, longLook, buf);
			mob.tell(buf.toString());
		}
	}

	public int recursiveWeight()
	{
		int weight=getEnvObject().envStats().weight();
		ItemCollection subItems=ItemCollection.O.getFrom(this);
		if(subItems!=null)
		for(Iterator<Item> iter=subItems.allItems();iter.hasNext();)
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
	public SaveEnum[] totalEnumS(){return SCode.values();}
	public Enum[] headerEnumS(){return new Enum[] {SCode.values()[0]} ;}
	public ModEnum[] totalEnumM(){return MCode.values();}
	public Enum[] headerEnumM(){return new Enum[] {MCode.values()[0]};}
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
	}
	public void saveThis(){CMLib.database().saveObject(this);}
	public void prepDefault(){} //TODO: Env

	private enum SCode implements SaveEnum<StdItem>{
		VAL(){
			public ByteBuffer save(StdItem E){ return (ByteBuffer)ByteBuffer.wrap(new byte[4]).putInt(E.baseGoldValue).rewind(); }
			public int size(){return 4;}
			public void load(StdItem E, ByteBuffer S){ E.baseGoldValue=S.getInt(); } },
		ENV(){
			public ByteBuffer save(StdItem E){ return CMLib.coffeeMaker().savSubFull(E.getEnvObject()); }
			public int size(){return -1;}
			public CMSavable subObject(StdItem fromThis){return fromThis.myEnvironmental;}
			public void load(StdItem E, ByteBuffer S){
				Environmental old=E.myEnvironmental;
				E.myEnvironmental=(Environmental)CMLib.coffeeMaker().loadSub(S, E, this);
				if(E.myEnvironmental!=null) ((Ownable)E.myEnvironmental).setOwner(E);
				if((old!=null)&&(old!=E.myEnvironmental)) old.destroy(); } },
		WRN(){
			public ByteBuffer save(StdItem E){ return (ByteBuffer)ByteBuffer.wrap(new byte[4]).putInt(E.wornOut).rewind(); }
			public int size(){return 4;}
			public void load(StdItem E, ByteBuffer S){ E.wornOut=S.getInt(); } },
		DMG(){
			public ByteBuffer save(StdItem E){ return (ByteBuffer)ByteBuffer.wrap(new byte[1]).put((byte)(E.damagable?1:0)).rewind(); }
			public int size(){return 1;}
			public void load(StdItem E, ByteBuffer S){ E.damagable=(S.get()==1); } },
		DSP(){
			public ByteBuffer save(StdItem E){ return CMLib.coffeeMaker().savString(E.display); }
			public int size(){return 0;}
			public void load(StdItem E, ByteBuffer S){ E.display=CMLib.coffeeMaker().loadString(S); } },
		DSC(){
			public ByteBuffer save(StdItem E){ return CMLib.coffeeMaker().savString(E.desc); }
			public int size(){return 0;}
			public void load(StdItem E, ByteBuffer S){ E.desc=CMLib.coffeeMaker().loadString(S); } },
		STC(){
			public ByteBuffer save(StdItem E){ return CMLib.coffeeMaker().savString(E.stackName); }
			public int size(){return 0;}
			public void load(StdItem E, ByteBuffer S){ E.stackName=CMLib.coffeeMaker().loadString(S); } },
		/*TXT(){
			public ByteBuffer save(StdItem E){ return CMLib.coffeeMaker().savString(E.miscText); }
			public int size(){return 0;}
			public void load(StdItem E, ByteBuffer S){ E.miscText=CMLib.coffeeMaker().loadString(S); } },*/
		RNM(){	//NOTE: this may be more efficient as a var enum since it's not often used..
			public ByteBuffer save(StdItem E){
				Rideable ride=Rideable.O.getFrom(E.ride);
				if(ride!=null)
					return (ByteBuffer)ByteBuffer.wrap(new byte[4]).putInt(ride.saveNum()).rewind();
				return (ByteBuffer)ByteBuffer.wrap(new byte[4]).putInt(0).rewind(); }
			public int size(){return 4;}
			public void load(StdItem E, ByteBuffer S){ E.rideToLoad=S.getInt(); } },
		EFC(){
			public ByteBuffer save(StdItem E){
				if(E.affects.size()>0) return CMLib.coffeeMaker().savSaveNums((CMSavable[])E.affects.toArray(CMSavable.dummyCMSavableArray));
				return CoffeeMaker.emptyBuffer; }
			public int size(){return 0;}
			public void load(StdItem E, ByteBuffer S){ E.effectsToLoad=CMLib.coffeeMaker().loadAInt(S); } },
		BHV(){
			public ByteBuffer save(StdItem E){
				if(E.behaviors.size()>0) return CMLib.coffeeMaker().savSaveNums((CMSavable[])E.behaviors.toArray(CMSavable.dummyCMSavableArray));
				return CoffeeMaker.emptyBuffer; }
			public int size(){return 0;}
			public void load(StdItem E, ByteBuffer S){ E.behavesToLoad=CMLib.coffeeMaker().loadAInt(S); } },
		NAM(){
			public ByteBuffer save(StdItem E){ return CMLib.coffeeMaker().savString(E.name); }
			public int size(){return 0;}
			public void load(StdItem E, ByteBuffer S){ E.name=CMLib.coffeeMaker().loadString(S); } },
		;
		public CMSavable subObject(StdItem fromThis){return null;} }
	private enum MCode implements ModEnum<StdItem>{
		VALUE(){
			public String brief(StdItem E){return ""+E.baseGoldValue;}
			public String prompt(StdItem E){return ""+E.baseGoldValue;}
			public void mod(StdItem E, MOB M){E.setBaseValue(CMLib.genEd().intPrompt(M, ""+E.baseGoldValue));} },
		ENVIRONMENTAL(){
			public String brief(StdItem E){return E.getEnvObject().ID();}
			public String prompt(StdItem E){return "";}
			public void mod(StdItem E, MOB M){CMLib.genEd().genMiscSet(M, E.myEnvironmental);} },
		WORNOUT(){
			public String brief(StdItem E){return ""+E.wornOut;}
			public String prompt(StdItem E){return ""+E.wornOut;}
			public void mod(StdItem E, MOB M){E.setWornOut(CMLib.genEd().intPrompt(M, ""+E.wornOut));} },
		DAMAGABLE(){
			public String brief(StdItem E){return ""+E.damagable;}
			public String prompt(StdItem E){return ""+E.damagable;}
			public void mod(StdItem E, MOB M){E.setDamagable(CMLib.genEd().booleanPrompt(M, ""+E.damagable));} },
		DISPLAY(){
			public String brief(StdItem E){return E.display;}
			public String prompt(StdItem E){return ""+E.display;}
			public void mod(StdItem E, MOB M){E.setDisplayText(CMLib.genEd().stringPrompt(M, ""+E.display, false));} },
		DESCRIPTION(){
			public String brief(StdItem E){return E.desc;}
			public String prompt(StdItem E){return ""+E.desc;}
			public void mod(StdItem E, MOB M){E.setDescription(CMLib.genEd().stringPrompt(M, ""+E.display, false));} },
		STACKNAME(){
			public String brief(StdItem E){return E.stackName;}
			public String prompt(StdItem E){return ""+E.stackName;}
			public void mod(StdItem E, MOB M){E.setStackableName(CMLib.genEd().stringPrompt(M, ""+E.stackName, false));} },
		/*TEXT(){
			public String brief(StdItem E){return E.miscText;}
			public String prompt(StdItem E){return ""+E.miscText;}
			public void mod(StdItem E, MOB M){E.setMiscText(CMLib.genEd().stringPrompt(M, ""+E.miscText, false));} },*/
		EFFECTS(){
			public String brief(StdItem E){return ""+E.affects.size();}
			public String prompt(StdItem E){return "";}
			public void mod(StdItem E, MOB M){CMLib.genEd().modAffectable(E, M);} },
		BEHAVIORS(){
			public String brief(StdItem E){return ""+E.behaviors.size();}
			public String prompt(StdItem E){return "";}
			public void mod(StdItem E, MOB M){CMLib.genEd().modBehavable(E, M);} },
		NAME(){
			public String brief(StdItem E){return ""+E.name;}
			public String prompt(StdItem E){return ""+E.name;}
			public void mod(StdItem E, MOB M){E.setName(CMLib.genEd().stringPrompt(M, ""+E.name, false));} }
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