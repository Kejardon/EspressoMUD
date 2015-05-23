package com.planet_ink.coffee_mud.core;
import com.planet_ink.coffee_mud.core.interfaces.*;
import java.nio.ByteBuffer;
import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 *
 * @author Kevin Swanson
 */
public abstract class AbstractInteractable implements Interactable //, CMSavable
{
	//@Override protected int AI_ENABLES(){return AI_ENV|AI_AFFECTS|AI_BEHAVES|AI_CHAR|AI_OK|AI_EXC|AI_TICK|AI_NAME|AI_DISP|AI_DESC;}
	protected static final int AI_ENV=1<<0;
	protected static final int AI_AFFECTS=1<<1;
	protected static final int AI_BEHAVES=1<<2;
	protected static final int AI_CHAR=1<<3;
	protected static final int AI_OK=1<<4;
	protected static final int AI_EXC=1<<5;
	protected static final int AI_TICK=1<<6;
	protected static final int AI_NAME=1<<7;
	protected static final int AI_DISP=1<<8;
	protected static final int AI_DESC=1<<9;
	protected static final int AI_FLAGS=(1<<10) - 1;
	
	protected String name="";
	protected String display="";
	protected String desc="";
	protected String plainName;
	protected String plainNameOf;
	protected String plainDisplay;
	protected String plainDisplayOf;
	protected String plainDesc;
	protected String plainDescOf;
	protected EnumSet<ListenHolder.Flags> lFlags=EnumSet.noneOf(ListenHolder.Flags.class);
	protected final CopyOnWriteArrayList<CharAffecter> charAffecters; //=new CopyOnWriteArrayList();
	protected final CopyOnWriteArrayList<OkChecker> okCheckers; //=new CopyOnWriteArrayList();
	protected final CopyOnWriteArrayList<ExcChecker> excCheckers; //=new CopyOnWriteArrayList();
	protected final CopyOnWriteArrayList<TickActer> tickActers; //=new CopyOnWriteArrayList();
	protected final CopyOnWriteArrayList<Effect> affects; //=new CopyOnWriteArrayList();
	protected final CopyOnWriteArrayList<Behavior> behaviors; //=new CopyOnWriteArrayList();
	protected int tickCount=0;
	protected Tickable.TickStat tickStatus=Tickable.TickStat.Not;
	protected Environmental myEnvironmental=null;//(Environmental)((Ownable)CMClass.COMMON.getNew("DefaultEnvironmental")).setOwner(this);

	public AbstractInteractable(){}
	protected AbstractInteractable(AbstractInteractable clone)
	{
		name = clone.name;
		display = clone.display;
		desc = clone.desc;
		plainName = clone.plainName;
		plainNameOf = clone.plainNameOf;
		plainDisplay = clone.plainDisplay;
		plainDisplayOf = clone.plainDisplayOf;
		plainDesc = clone.plainDesc;
		plainDescOf = clone.plainDescOf;
		//lFlags = clone.lFlags.clone();
		lFlags.addAll(clone.lFlags);
		
		if(clone.myEnvironmental != null)
			myEnvironmental=(Environmental)((Ownable)clone.myEnvironmental.copyOf()).setOwner(this);
		if(clone.affects!=null) for(Effect A : clone.affects)
			affects.add(A.copyOnto(this));
		if(clone.behaviors!=null) for(Behavior B : clone.behaviors)
			addBehavior(B.copyOf());
	}
	
	protected int AI_ENABLES(){return 0;}
	{
		int overrides=AI_ENABLES();
		affects=((overrides & AI_AFFECTS)!=AI_AFFECTS)?null:new CopyOnWriteArrayList();
		behaviors=((overrides & AI_BEHAVES)!=AI_BEHAVES)?null:new CopyOnWriteArrayList();
		charAffecters=((overrides & AI_CHAR)!=AI_CHAR)?null:new CopyOnWriteArrayList();
		okCheckers=((overrides & AI_OK)!=AI_OK)?null:new CopyOnWriteArrayList();
		excCheckers=((overrides & AI_EXC)!=AI_EXC)?null:new CopyOnWriteArrayList();
		tickActers=((overrides & AI_TICK)!=AI_TICK)?null:new CopyOnWriteArrayList();
	}
	
	@Override public void initializeClass(){}
	
	@Override public Environmental getEnvObject() {
		if(myEnvironmental==null && (AI_ENABLES()&AI_ENV)!=0)
			synchronized(this){if(myEnvironmental==null) myEnvironmental=(Environmental)((Ownable)CMClass.COMMON.getNew("DefaultEnvironmental")).setOwner(this);}
		return myEnvironmental;
	}
	@Override public void setName(String newName){if((AI_ENABLES() & AI_NAME)!=AI_NAME) return; name=newName; saveThis();}
	//@Override public String name(){ return name==""?"an ordinary item":name;}
	@Override public String plainName()
	{
		String fullName=name();
		if(fullName.equals(plainNameOf))
			return plainName;
		String newName=fullName;
		String newPlain=CMLib.coffeeFilter().toRawString(newName);
		synchronized(this)
		{
			plainName=newPlain;
			plainNameOf=newName;
		}
		return newPlain;
	}
	//@Override public String displayText(){return display==""?"a nondescript item sits here doing nothing.":display;}
	@Override public void setDisplayText(String newDisplayText){if((AI_ENABLES() & AI_DISP)!=AI_DISP) return; display=newDisplayText; saveThis();}
	@Override public String plainDisplayText()
	{
		String fullDisplay=displayText();
		if(fullDisplay.equals(plainDisplayOf))
			return plainDisplay;
		String newDisplay=fullDisplay;
		String newPlain=CMLib.coffeeFilter().toRawString(newDisplay);
		synchronized(this)
		{
			plainDisplay=newPlain;
			plainDisplayOf=newDisplay;
		}
		return newPlain;
	}
	//@Override public String description(){return desc;}
	@Override public void setDescription(String newDescription){if((AI_ENABLES() & AI_DESC)!=AI_DESC) return; desc=newDescription; saveThis();}
	@Override public String plainDescription()
	{
		String fullDesc=description();
		if(fullDesc.equals(plainDescOf))
			return plainDesc;
		String newDesc=fullDesc;
		String newPlain=CMLib.coffeeFilter().toRawString(newDesc);
		synchronized(this)
		{
			plainDesc=newPlain;
			plainDescOf=newDesc;
		}
		return newPlain;
	}
	@Override public void addEffect(Effect to)
	{
		if(affects==null) return;
		affects.add(to);
		to.setAffectedOne(this);
		saveThis();
	}
	@Override public void delEffect(Effect to)
	{
		if(affects==null) return;
		if(affects.remove(to))
		{
			to.setAffectedOne(null);
			saveThis();
		}
	}
	@Override public boolean hasEffect(Effect to)
	{
		return affects==null?false:affects.contains(to);
	}
	@Override public int numEffects(){return affects==null?0:affects.size();}
	@Override public Effect fetchEffect(int index)
	{
		try { return affects==null?null:affects.get(index); }
		catch(java.lang.ArrayIndexOutOfBoundsException x){}
		return null;
	}
	@Override public Vector<Effect> fetchEffect(String ID)
	{
		if(affects==null) return new Vector(0);
		Vector<Effect> V=new Vector(1);
		for(Effect E : affects)
			if(E.ID().equals(ID))
				V.add(E);
		return V;
	}
	@Override public Effect fetchFirstEffect(String ID)
	{
		if(affects==null) return null;
		for(Effect E : affects)
			if(E.ID().equals(ID))
				return E;
		return null;
	}
	@Override public Iterator<Effect> allEffects() { return affects==null?CMParms.EmptyIterator:affects.iterator(); }

	//Behavable
	@Override public void addBehavior(Behavior to)
	{
		if(behaviors==null) return;
		synchronized(behaviors)
		{
			if(fetchBehavior(to.ID())!=null) return;
			to.startBehavior(this);
			behaviors.add(to);
		}
		saveThis();
	}
	@Override public void delBehavior(Behavior to)
	{
		if(behaviors==null) return;
		if(behaviors.remove(to))
		{
			to.startBehavior(null);
			saveThis();
		}
	}
	@Override public int numBehaviors()
	{
		return behaviors==null?0:behaviors.size();
	}
	@Override public Behavior fetchBehavior(int index)
	{
		try { return behaviors==null?null:behaviors.get(index); }
		catch(java.lang.ArrayIndexOutOfBoundsException x){}
		return null;
	}
	@Override public Behavior fetchBehavior(String ID)
	{
		if(behaviors==null) return null;
		for(Behavior B : behaviors)
			if(B.ID().equals(ID))
				return B;
		return null;
	}
	@Override public boolean hasBehavior(String ID)
	{
		if(behaviors==null) return false;
		for(Behavior B : behaviors)
			if(B.ID().equals(ID))
				return true;
		return false;
	}
	@Override public Iterator<Behavior> allBehaviors() { return behaviors==null?CMParms.EmptyIterator:behaviors.iterator(); }

	//Affectable/Behavable shared
	@Override public CopyOnWriteArrayList<CharAffecter> charAffecters(){return charAffecters;}
	@Override public CopyOnWriteArrayList<EnvAffecter> envAffecters(){return null;}	//TODO: Should this give the environmental's instead?
	@Override public CopyOnWriteArrayList<OkChecker> okCheckers(){return okCheckers;}
	@Override public CopyOnWriteArrayList<ExcChecker> excCheckers(){return excCheckers;}
	@Override public CopyOnWriteArrayList<TickActer> tickActers(){return tickActers;}
	@Override public EnumSet<Flags> listenFlags() { return lFlags; }
	
	@Override public Tickable.TickStat getTickStatus(){return tickStatus;}
	@Override public int tickCounter(){return tickCount;}
	//public void tickAct(){}
	@Override public boolean tick(int tickTo)
	{
		if(tickCount==0) tickCount=tickTo-1;
		else if(tickTo>tickCount+10)
			tickTo=tickCount+10;
		while(tickCount<tickTo)
		{
			tickCount++;
			if(!doTick()) {tickCount=0; return false;}	//Overriders of this should handle lFlags.remove(ListenHolder.Flags.TICK); if appropriate
		}
		return true;
	}
	@Override public int compareTo(CMObject o){ return ID().compareToIgnoreCase(o==null?"":o.ID());}
	protected boolean doTick()
	{
		if(tickActers==null) return false;
		tickStatus=Tickable.TickStat.Listener;
		for(TickActer T : tickActers)
			if(!T.tick(tickCount))
				tickActers.remove(T);
		tickStatus=Tickable.TickStat.Not;
		return (!tickActers.isEmpty());
	}
	/*
	protected void cloneFix(AbstractInteractable E)
	{
		if(charAffecters!=null) charAffecters.clear();
		if(okCheckers!=null) okCheckers.clear();
		if(excCheckers!=null) excCheckers.clear();
		if(tickActers!=null) tickActers.clear();
		lFlags=lFlags.clone();
		if(affects!=null) affects.clear();
		if(behaviors!=null) behaviors.clear();
		tickStatus=Tickable.TickStat.Not;
		tickCount=0;
		if(E.myEnvironmental!=null)
			myEnvironmental=(Environmental)((Ownable)myEnvironmental.copyOf()).setOwner(this);

		for(Effect A : E.affects)
			affects.add(A.copyOnto(this));
		for(Behavior B : E.behaviors)
			addBehavior(B.copyOf());
	}
	*/
	protected void saveThis(){}
	/*
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
	*/
}
