package com.planet_ink.coffee_mud.Locales;
import com.planet_ink.coffee_mud.core.interfaces.*;
import com.planet_ink.coffee_mud.core.*;
import com.planet_ink.coffee_mud.Libraries.*;

import java.util.*;
import java.nio.ByteBuffer;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicReference;
import java.io.*;

/*
CoffeeMUD 5.6.2 copyright 2000-2010 Bo Zimmerman
EspressoMUD copyright 2011 Kejardon

Licensed under the Apache License, Version 2.0. You may obtain a copy of the license at
	http://www.apache.org/licenses/LICENSE-2.0
*/
/*
if(get()==Thread.currentThread()) to check a lock.
compareAndSet(null, Thread.currentThread()) to get a lock.
set(null) to clear a lock (only do if this place got a lock). Done in a finally?
*/

public class StdRoom implements Room
{
	public String ID(){return "StdRoom";}
	protected String name="the room";
	protected String display="Standard Room";
	protected String desc="";
	protected String plainName;
	protected String plainNameOf;
	protected String plainDisplay;
	protected String plainDisplayOf;
	protected String plainDesc;
	protected String plainDescOf;
	protected Area myArea=null;
	protected CopyOnWriteArrayList<ExitInstance> exits=new CopyOnWriteArrayList();
	protected AtomicReference<Thread> activeThread=new AtomicReference<Thread>();
	protected long lockTime;
	
	protected CopyOnWriteArrayList<OkChecker> okCheckers=new CopyOnWriteArrayList();
	protected CopyOnWriteArrayList<ExcChecker> excCheckers=new CopyOnWriteArrayList();
	protected CopyOnWriteArrayList<TickActer> tickActers=new CopyOnWriteArrayList();
	protected CopyOnWriteArrayList<Effect> affects=new CopyOnWriteArrayList();
	protected CopyOnWriteArrayList<Behavior> behaviors=new CopyOnWriteArrayList();
	protected EnumSet<ListenHolder.Flags> lFlags=EnumSet.of(ListenHolder.Flags.OK, ListenHolder.Flags.EXC);
	protected int tickCount=0;
	protected Tickable.TickStat tickStatus=Tickable.TickStat.Not;
	//protected long lastTick=0;
	//TODO: This needs a custom item collection! Eventually. For grids. Some day.
	protected ItemCollection inventory=null;//(ItemCollection)((Ownable)CMClass.COMMON.getNew("DefaultItemCol")).setOwner(this);
	protected EnvMap positions=null;
	protected Domain myDom=Domain.AIR;
	protected Enclosure myEnc=Enclosure.OPEN;
	protected boolean amDestroyed=false;
	protected Environmental myEnvironmental=null;//(Environmental)((Ownable)CMClass.COMMON.getNew("DefaultEnvironmental")).setOwner(this);
	protected ArrayList<Environmental> terrainObjects=new ArrayList();

	protected int saveNum=0;
	protected int[] effectsToLoad=null;
	protected int[] behavesToLoad=null;
	protected int[] exitsToLoad=null;
	protected int areaToLink=0;
	protected int itemCollectionToLoad=0;
	protected int envMapToLoad=0;

	public StdRoom(){}

	public int priority(ListenHolder L){return Integer.MAX_VALUE;}
	public void registerListeners(ListenHolder forThis){}

	public void initializeClass(){}
	public StdRoom newInstance()
	{
		try
		{
			return this.getClass().newInstance();
		}
		catch(Exception e)
		{
			Log.errOut(ID(),e);
		}
		return new StdRoom();
	}

	public Environmental getEnvObject() {
		if(myEnvironmental==null)
			synchronized(this){if(myEnvironmental==null) myEnvironmental=(Environmental)((Ownable)CMClass.COMMON.getNew("DefaultEnvironmental")).setOwner(this);}
		return myEnvironmental;
	}
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
	public void setName(String newName){name=newName; CMLib.database().saveObject(this);}
	public String displayText(){return display;}
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
	public void setDisplayText(String newDisplayText){display=newDisplayText; CMLib.database().saveObject(this);}
	public String description(){return desc;}
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
	public void setDescription(String newDescription){desc=newDescription; CMLib.database().saveObject(this);}

	protected void cloneFix(StdRoom E)
	{
		okCheckers=new CopyOnWriteArrayList();
		excCheckers=new CopyOnWriteArrayList();
		tickActers=new CopyOnWriteArrayList();
		affects=new CopyOnWriteArrayList();
		behaviors=new CopyOnWriteArrayList();
		lFlags=lFlags.clone();
		if(myEnvironmental!=null) myEnvironmental=(Environmental)((Ownable)myEnvironmental.copyOf()).setOwner(this);
		//if(inventory!=null) inventory=inventory.copyOf();
		inventory=null;
		tickStatus=Tickable.TickStat.Not;
		tickCount=0;

		for(Effect A : E.affects)
			affects.add(A.copyOnto(this));
		for(Behavior B : E.behaviors)
			addBehavior((Behavior)B.copyOf());
	}
	public StdRoom copyOf()
	{
		try
		{
			StdRoom R=(StdRoom)this.clone();
			R.saveNum=0;
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
		addExit(E.makeInstance(this, destination));
	}
	public void addExit(ExitInstance R)
	{
		if(exits.addIfAbsent(R))
			CMLib.database().saveObject(this);
	}
	/*
	public void removeExit(Exit E, Room R)
	{
		if(E.removeExitFrom(this, R, exits))
			CMLib.database().saveObject(this);
	}
	*/
	public void removeExit(ExitInstance R)
	{
		if(exits.remove(R))
			CMLib.database().saveObject(this);
	}
	public Exit getExit(int i)
	{
		try{ return exits.get(i).getExit(); }
		catch(ArrayIndexOutOfBoundsException e){}
		return null;
	}
	public Exit getExit(String target)
	{
		//TODO: This totally doesn't work.
//		return (Exit)CMLib.english().fetchInteractable(exits, target, true);
		return null;
	}
	public Room getExitDestination(int i)
	{
		try{ return exits.get(i).getDestination(); }
		catch(ArrayIndexOutOfBoundsException e){}
		return null;
	}
	public Room getExitDestination(Exit E)
	{
		for(ExitInstance exit : exits)
			if(exit.getExit()==E)
				return exit.getDestination();
		return null;
	}
	public ExitInstance getExitInstance(int i)
	{
		try{ return exits.get(i); }
		catch(ArrayIndexOutOfBoundsException e){}
		return null;
	}
	public ExitInstance getExitInstance(String target)
	{
		//Definitely need TODO
		ExitInstance exit = (ExitInstance)CMLib.english().fetchInteractable(exits.iterator(),target,true);
		if(exit!=null) return exit;
		return (ExitInstance)CMLib.english().fetchInteractable(exits.iterator(),target,false);
	}
	public ExitInstance getExitInstance(Exit E, Room R)
	{
		for(ExitInstance exit : exits)
			if(exit.getExit() == E && exit.getDestination() == R)
				return exit;
		return null;
	}
	public boolean hasExit(ExitInstance I){return exits.contains(I);}
	public Iterator<ExitInstance> getAllExits() { return exits.iterator(); }
	/*
	public boolean changeExit(REMap R, Exit newExit)
	{
		return changeExit(R, new REMap(R.room, newExit));
	}
	public boolean changeExit(REMap R, Room newRoom)
	{
		return changeExit(R, new REMap(newRoom, R.exit));
	}
	public boolean changeExit(REMap R, REMap newMap)
	{
		if(exits.remove(R))
		{
			exits.add(newMap);
			CMLib.database().saveObject(this);
			return true;
		}
		return false;
	}
	*/

	public Domain domain(){return myDom;} 
	public Enclosure enclosure(){return myEnc;}

	public Area getArea()
	{
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
			CMLib.database().saveObject(this);
		}
	}
	//Probably unnecessary now!
	public void setAreaRaw(Area newArea)
	{
		myArea=newArea;
		CMLib.database().saveObject(this);
	}
	public boolean hasPositions(){return positions!=null;}
	public EnvMap.EnvLocation positionOf(Environmental.EnvHolder of){return positions.position(of);}

	public boolean okMessage(OkChecker myHost, CMMsg msg)
	{
		//boolean always=false;
		//Interactable target=msg.target();
		for(CMMsg.MsgCode code : msg.othersCode())
		switch(code)
		{
			//case ALWAYS: always=true; break;
			case LEAVE:
				msg.addResponse(ListenHolder.InbetweenListener.newListener(LeaveResponse, this), 10);
				break;
		}
		if(!myArea.okMessage(this,msg))
			return false;
		for(OkChecker O : okCheckers)
			if(!O.okMessage(myHost,msg))
				return false;
		return true;
// TODO
	}
	protected static ListenHolder.DummyListener LeaveResponse = new ListenHolder.DummyListener()
	{
		public boolean respondTo(CMMsg msg, Object data)
		{
			StdRoom room = (StdRoom)data;
			boolean always = msg.hasOthersCode(CMMsg.MsgCode.ALWAYS);
			for(CMObject O : msg.tool())
			if(O instanceof ExitInstance)
			{
				ExitInstance map=(ExitInstance)O;
				Exit exit=map.getExit();
				Room destination=map.getDestination();
				ExitInstance entrance=exit.oppositeOf(map, destination);
				if(destination!=null && entrance!=null)
				{
					EnumSet<CMMsg.MsgCode> codeSet=always?EnumSet.of(CMMsg.MsgCode.ENTER,CMMsg.MsgCode.ALWAYS):EnumSet.of(CMMsg.MsgCode.ENTER);
					CMMsg enterMessage=CMClass.getMsg(msg.source().clone(),null,entrance,codeSet,"^[S-NAME] enter(s).");
					if(destination.okMessage(destination, enterMessage)&&enterMessage.handleResponses())
					{
						msg.addTrailerHappens(destination, enterMessage);
						break;
					}
				}
				return false;
			}
			//TODO: Else if target instanceof Room for teleportation spells?
			return true;
		}
	};
	public boolean respondTo(CMMsg msg, Object data){return true;} //Should never be called
	public boolean respondTo(CMMsg msg){ return true; }
	//TODO NOTE FOR ITEM ROOMS:
	//Room: If host is not a room, you are new host. Send to container and to items.
	//Room: If host is a room (and not you), do not send to container, but do send to items.
	//Item of Room: If host is a room, do not send to room UNLESS host is your container.
	public void executeMsg(ExcChecker myHost, CMMsg msg)
	{
		Interactable target=msg.target();
		for(CMMsg.MsgCode code : msg.othersCode())
		switch(code)
		{
			case ENTER:
			{
				/*
				for(CMObject O : msg.tool()) if(O instanceof Room.REMap)
				{
				}
				*/
				for(Interactable I : msg.source())
				{
					if(I instanceof MOB)
						I=((MOB)I).body();
					if(I instanceof Item)
						bringHere((Item)I, true);
				}
				break;
			}
			case LOOK:
				if(target==this)
					handleBeingLookedAt(msg, false);
				break;
			case EXAMINE:
				if(target==this)
					handleBeingLookedAt(msg, true);
				break;
		}
		myArea.executeMsg(this,msg);
		for(ExcChecker O : excCheckers)
			O.executeMsg(myHost,msg);
// TODO
	}
	public void handleBeingLookedAt(CMMsg msg, boolean longLook)
	{
		for(Interactable I : msg.sourceArr())
		{
			if(!(I instanceof MOB)) continue;
			MOB mob=(MOB)I;
			if(mob.session()==null) continue; // no need for monsters to build all this data

			StringBuilder Say=new StringBuilder("");
			if(mob.playerStats().hasBits(PlayerStats.ATT_SYSOPMSGS))
			{
				if(getArea()!=null)
					Say.append("Area   : "+getArea().name()+"\r\n");
				Say.append("SaveNum: "+saveNum()+"  ("+ID()+")\r\n");
			}
			if(hasPositions())
			{
				EnvMap.EnvLocation mobLoc=positionOf(mob.body());
				if(mobLoc!=null)
					Say.append("(").append(mobLoc.x).append(", ").append(mobLoc.y).append(", ").append(mobLoc.z).append(")\r\n");
			}
			Say.append(displayText()).append("\r\n").append(description()).append("\r\n\r\n");

			ArrayList<Item> viewItems=CMParms.toArrayList(getItemCollection().allItems());

			//NOTE: Will probably redo these tags sometimes.
			StringBuilder itemStr=CMLib.lister().lister(mob,viewItems,false,"RItem"," \"*\"",longLook);
			if(itemStr.length()>0)
				Say.append(itemStr);
	
			if(Say.length()==0)
				mob.tell("You can't see anything!");
			else
				mob.tell(Say.toString());
		}
	}

	public Tickable.TickStat getTickStatus(){return tickStatus;}
	public void tickAct(){}
	public int tickCounter(){return tickCount;}
	public boolean tick(int tickTo)
	{
		if(tickCount==0) tickCount=tickTo-1;
		while(tickCount<tickTo)
		{
			tickCount++;
			if(!doTick()) {tickCount=0; return false;}
		}
		return true;
	}
	protected boolean doTick()
	{
		tickStatus=Tickable.TickStat.Listener;
		for(TickActer T : tickActers)
			if(!T.tick(tickCount))
				removeListener(T, EnumSet.of(ListenHolder.Flags.TICK));
		tickStatus=Tickable.TickStat.Not;
		//lastTick=System.currentTimeMillis();
		return true;
	}
	//public long lastAct(){return 0;}	//No Action ticks
	//public long lastTick(){return lastTick;}

	public void recoverRoomStats()
	{
		//TODO
	}
	public int compareTo(CMObject o)
	{
		if(o instanceof Room)
			return saveNum()-((CMSavable)o).saveNum();
		return CMClass.classID(this).compareToIgnoreCase(CMClass.classID(o));
	}

// actually this should not be called much. Execute Message should handle most of it instead I think? Maybe this should be called by execute message? Probably not...
	public void bringHere(Item I, boolean andRiders)
	{
		if(I==null) return;
		CMObject o=I.container();
		if(o==this) return;

		if(!(andRiders))
		{
			Rideable R=Rideable.O.getFrom(I);
			if(R!=null) R.dropRiders();
		}
		ItemCollection col=ItemCollection.O.getFrom(o);
		if(col!=null) col.removeItem(I);

		getItemCollection().addItem(I);
	}
	public void placeHere(Environmental.EnvHolder I, boolean andRiders, int x, int y, int z)
	{
		if(I==null) return;
		if(I instanceof Item) bringHere((Item)I, andRiders);
		positions.placeThing(I, x, y, z);
	}

	protected void reallySend(CMMsg msg, int depth)
	{
		executeMsg(this,msg);
		// now handle trailer msgs
		Vector<CMMsg.TrailMessage> V;
		if((V=msg.trailerHappens())!=null)
		{
			if(depth>30)
				Log.errOut("Messages",new RuntimeException("Excessive message happens depth: "+msg.toString()));
			else while(V.size()>0)
			{
				CMMsg.TrailMessage msg2=V.remove(V.size()-1);
				if(msg2.room instanceof StdRoom)
					((StdRoom)msg2.room).reallySend(msg2.msg,depth+1);
				msg2.msg.returnMsg();
			}
		}
		if((V=msg.trailerMsgs())!=null)
		{
			if(depth>30)
				Log.errOut("Messages",new RuntimeException("Excessive message trail depth: "+msg.toString()));
			else while(V.size()>0)
			{
				CMMsg.TrailMessage msg2=V.remove(V.size()-1);
				if(msg2.room instanceof StdRoom)
					if((msg2.room.okMessage(msg2.room,msg2.msg))&&(msg2.msg.handleResponses()))
						((StdRoom)msg2.room).reallySend(msg2.msg,depth+1);
				msg2.msg.returnMsg();
			}
		}
	}

	public boolean hasLock(Thread T)
	{
		return (activeThread.get()==T);
	}
	public boolean hasLock()
	{
		return (activeThread.get()==Thread.currentThread());
	}

	public String undoLock()
	{
		Thread T=activeThread.getAndSet(null);
		return (T==null?"null":T.toString());
	}
	//0: Got a lock.
	//1: Got a fresh lock, please return later.
	//2: Failed to get a lock.
	public int getLock(long time)
	{
		Thread myThread=Thread.currentThread();
		boolean freshLock=false;
		int tries=0;
		if(lockTime>0&&lockTime<System.currentTimeMillis())
		{
			boolean gotLock=false;
			synchronized(this)
			{
				gotLock=(lockTime>0&&lockTime<System.currentTimeMillis());
				if(gotLock)
				{
					//Nothing else can set the lock at this point but this thread and the delayed thread.
					lockTime=0;
					freshLock=activeThread.getAndSet(myThread)!=myThread;
				}
			}
			if(!gotLock)
			{
				Log.errOut("StdRoom",new RuntimeException(myThread+" Failed to get roomlock in "+saveNum()+". "+activeThread.get()+"\r\n"+CMClass.getStackTrace(activeThread.get())));
				return 2;
			}
			freshLock=true;
			Log.errOut("StdRoom","A thread failed to return lock in timely fashion for "+saveNum());
		}
		else while(!(freshLock=activeThread.compareAndSet(null, myThread))&&(activeThread.get()!=myThread))
			if(tries++<100)
				try{myThread.sleep(10);}catch(InterruptedException e){}
			else
			{
				Log.errOut("StdRoom",new RuntimeException(myThread+" Failed to get roomlock in "+saveNum()+". "+activeThread.get()+"\r\n"+CMClass.getStackTrace(activeThread.get())));
				return 2;
			}
		if(time>0)
			lockTime=System.currentTimeMillis()+time;
		//if(freshLock) Log.errOut("StdRoom",new RuntimeException(myThread+" got roomlock in "+saveNum()+". "));
		return freshLock?1:0;
	}
	public void returnLock()
	{
		activeThread.compareAndSet(Thread.currentThread(), null);
		//Log.errOut("StdRoom",new RuntimeException(Thread.currentThread()+" returned roomlock in "+saveNum()+". "));
	}

	public boolean doMessage(CMMsg msg)
	{
		//Default lock time?
		int lockStatus=getLock(0);
		if(lockStatus==2) return false;
		try{
			if((okMessage(this,msg))&&(msg.handleResponses()))
			{
				reallySend(msg,0);
				if(lockStatus==1) returnLock();
				return true;
			}
			if(lockStatus==1) returnLock();
			return false;
		}catch(Exception e){if(lockStatus==1) returnLock(); throw e;}
	}
	public boolean doAndReturnMsg(CMMsg msg)
	{
		int lockStatus=getLock(0);
		if(lockStatus==2) return false;
		try{
			if((okMessage(this,msg))&&(msg.handleResponses()))
			{
				reallySend(msg,0);
				if(lockStatus==1) returnLock();
				msg.returnMsg();
				return true;
			}
			if(lockStatus==1) returnLock();
			msg.returnMsg();
			return false;
		}catch(Exception e){if(lockStatus==1) returnLock(); throw e;}	//msg may not be returned. That's okay.
	}
	public void send(CMMsg msg)
	{
		int lockStatus=getLock(0);
		if(lockStatus==2) return;
		try{
			reallySend(msg,0);
			if(lockStatus==1) returnLock();
		}catch(Exception e){if(lockStatus==1) returnLock(); throw e;}
	}

	public void show(Interactable source, String allMessage)
	{
		for(Iterator<Item> iter=getItemCollection().allItems();iter.hasNext();)
		{
			MOB M;
			Item next=iter.next();
			if((next instanceof Body)&&((M=((Body)next).mob())!=null))
				M.tell(source, null, allMessage);
		}
	}
	public void show(Interactable source, 
						Interactable target, 
						CMObject tool, 
						String allMessage)
	{
		for(Iterator<Item> iter=getItemCollection().allItems();iter.hasNext();)
		{
			MOB M;
			Item next=iter.next();
			if((next instanceof Body)&&((M=((Body)next).mob())!=null))
				M.tell(source, target, tool, allMessage);
		}
	}
	public void show(Interactable source,
						Interactable target,
						CMObject tool,
						String srcMessage,
						String tarMessage,
						String othMessage)
	{
		for(Iterator<Item> iter=getItemCollection().allItems();iter.hasNext();)
		{
			MOB M;
			Item next=iter.next();
			if((next instanceof Body)&&((M=((Body)next).mob())!=null))
			{
				if(M==source||next==source)
					M.tell(source, target, tool, srcMessage);
				else if(M==target||next==target)
					M.tell(source, target, tool, tarMessage);
				else
					M.tell(source, target, tool, othMessage);
			}
		}
	}

	//NOTE: this doesn't handle exits currently.. Also, player/MOB ejection is TODO
	public void destroy()
	{
		amDestroyed=true;

		if(myEnvironmental!=null)
			myEnvironmental.destroy();

		for(Effect E : affects)
			E.setAffectedOne(null);
		affects.clear();
		for(Behavior B : behaviors)
			B.startBehavior(null);
		behaviors.clear();

		//TODO: Change for item rooms
		if(inventory!=null)
			inventory.destroy();

		setArea(null);
		if(saveNum!=0)
			CMLib.database().deleteObject(this);
	}
	public boolean amDestroyed(){return amDestroyed;}

	//This might not really be necessary. ..meh
	public boolean isContent(Item E, boolean sub)
	{
		return getItemCollection().hasItem(E, sub);
	}
	public Item fetchItem(String itemID)
	{
		Item item=(Item)CMLib.english().fetchInteractable(getItemCollection().allItems(),itemID,true);
		if(item==null) item=(Item)CMLib.english().fetchInteractable(inventory.allItems(),itemID,false);
		return item;
	}
	public Vector<Item> fetchItems(String itemID)
	{
		Vector items=CMLib.english().fetchInteractables(getItemCollection().allItems(),itemID,true);
		if(items.size()==0)
			items=CMLib.english().fetchInteractables(inventory.allItems(),itemID,false);
		return items;
	}
	/*
	 * TODO
	 * Eventually this should take the MOB's source location, objects hide value, interfering objects,
	 * and MOB's skills into consideration. Also be able to look in nearby rooms.
	 * 
	 */
	public EnvMap.EnvLocation findObject(String findThis, MOB finder, EnvMap.EnvLocation lookingFromHere)
	{
		Vector<Interactable> targets=new Vector();
		for(Iterator<Environmental.EnvHolder> iter=positions.allThings();iter.hasNext();)
		{
			Environmental.EnvHolder next=iter.next();
			if(next instanceof Interactable)
				targets.add((Interactable)next);
		}
		Interactable thing=CMLib.english().fetchInteractable(targets,findThis,true);
		if(thing==null) thing=CMLib.english().fetchInteractable(targets,findThis,false);
		return (thing==null?null:positions.position(thing));
	}
	public MOB fetchInhabitant(String inhabitantID)
	{
		MOB M=null;
		if(inhabitantID.equals("ALL"))
		{
			Iterator<Item> items=getItemCollection().allItems();
			while(items.hasNext())
			{
				Item next=items.next();
				if((next instanceof Body)&&((M=((Body)next).mob())!=null))
					return M;
			}
		}
		else
		{
			Vector<Interactable> items=CMLib.english().fetchInteractables(inhabitantID,false,1,Integer.MAX_VALUE,getItemCollection());
//			Vector<Item> items=(Vector<Item>)CMLib.english().fetchInteractables(getItemCollection().allItems(),inhabitantID,true);
			for(int i=0;i<items.size();i++)
				if((items.get(i) instanceof Body)&&((M=((Body)items.get(i)).mob())!=null))
					return M;
			items=CMLib.english().fetchInteractables(inventory.allItems(),inhabitantID, false);
			for(int i=0;i<items.size();i++)
				if((items.get(i) instanceof Body)&&((M=((Body)items.get(i)).mob())!=null))
					return M;
		}
		return null;
	}
	public Vector<MOB> fetchInhabitants(String inhabitantID)
	{
		Vector<MOB> inhabs=new Vector<MOB>();
		MOB M=null;
		if(inhabitantID.equals("ALL"))
			for(Iterator<Item> iter=getItemCollection().allItems();iter.hasNext();)
			{
				Item next=iter.next();
				if((next instanceof Body)&&((M=((Body)next).mob())!=null))
					inhabs.add(M);
			}
		else
		{
			Vector<Item> items=(Vector)CMLib.english().fetchInteractables(getItemCollection().allItems(),inhabitantID,true);
			for(int i=0;i<items.size();i++)
				if((items.get(i) instanceof Body)&&((M=((Body)items.get(i)).mob())!=null))
					inhabs.add(M);
			if(inhabs.size()==0)
			{
				items=(Vector)CMLib.english().fetchInteractables(inventory.allItems(),inhabitantID, false);
				for(int i=0;i<items.size();i++)
					if((items.get(i) instanceof Body)&&((M=((Body)items.get(i)).mob())!=null))
						inhabs.add(M);
			}
		}
		return inhabs;
	}

	//Things listen to this really, not the other way around, so really simple code here.
	public void removeListener(Listener oldAffect, EnumSet<ListenHolder.Flags> flags)
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
	public CopyOnWriteArrayList<CharAffecter> charAffecters(){return null;}
	public CopyOnWriteArrayList<EnvAffecter> envAffecters(){return null;}
	public CopyOnWriteArrayList<OkChecker> okCheckers(){return okCheckers;}
	public CopyOnWriteArrayList<ExcChecker> excCheckers(){return excCheckers;}
	public CopyOnWriteArrayList<TickActer> tickActers(){return tickActers;}
	public EnumSet<ListenHolder.Flags> listenFlags() {return lFlags;}

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
	public ItemCollection getItemCollection()
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
	public void setItemCollection(ItemCollection newInv, boolean copyInto)
	{
		ItemCollection oldInv=inventory;
		inventory=newInv;
		if(copyInto && oldInv!=null)
			newInv.copyFrom(oldInv);
	}

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
	public int saveNum()
	{
		if((saveNum==0)&&(!amDestroyed))
		synchronized(this)
		{
			if(saveNum==0)
				saveNum=SIDLib.ROOM.getNumber(this);
		}
		return saveNum;
	}
	public void setSaveNum(int num)
	{
		synchronized(this)
		{
			if(saveNum!=0)
				SIDLib.ROOM.removeNumber(saveNum);
			saveNum=num;
			SIDLib.ROOM.assignNumber(num, this);
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
		if(exitsToLoad!=null)
		{
			for(int SID : exitsToLoad)
			{
				ExitInstance e=SIDLib.EXITINSTANCE.get(SID);
				if(e==null) continue;
				exits.add(e);
			}
			exitsToLoad=null;
		}
		if(areaToLink!=0)
		{
			myArea = SIDLib.AREA.get(areaToLink);
			if(myArea!=null)
			{
				myArea.addProperRoom(this);
				if(lFlags.contains(ListenHolder.Flags.TICK))
					myArea.addTickingRoom(this);
			}
			areaToLink=0;
		}
		InvFail:
		if(itemCollectionToLoad!=0)
		{
			ItemCollection newInventory = SIDLib.ITEMCOLLECTION.get(itemCollectionToLoad);
			if(newInventory==null) break InvFail;
			ItemCollection oldInventory=inventory;
			inventory=(ItemCollection)((Ownable)newInventory).setOwner(this);
			itemCollectionToLoad=0;
			//Ideally never happens
			if(oldInventory!=null)
			{
				for(Iterator<Item> iter=oldInventory.allItems();iter.hasNext();)
				{
					Item next=iter.next();
					next.setContainer(null);
					bringHere(next, true);
				}
			}
		}
		mapFail:
		if(envMapToLoad!=0)
		{
			EnvMap envMap = SIDLib.ENVMAP.get(envMapToLoad);
			if(envMap==null) break mapFail;
			EnvMap oldMap = positions;
			positions=(EnvMap)((Ownable)envMap).setOwner(this);
			envMapToLoad=0;
			if(oldMap!=null)
				envMap.copyFrom(oldMap);
		}
	}
	public void saveThis(){CMLib.database().saveObject(this);}
	public void prepDefault(){getEnvObject(); getItemCollection();} //TODO: Env

	private enum SCode implements SaveEnum<StdRoom>{
		ENV(){
			public ByteBuffer save(StdRoom E){ return CMLib.coffeeMaker().savSubFull(E.getEnvObject()); }
			public int size(){return -1;}
			public CMSavable subObject(StdRoom fromThis){return fromThis.myEnvironmental;}
			public void load(StdRoom E, ByteBuffer S){
				Environmental old=E.myEnvironmental;
				E.myEnvironmental=(Environmental)CMLib.coffeeMaker().loadSub(S, E, this);
				if(E.myEnvironmental!=null) ((Ownable)E.myEnvironmental).setOwner(E);
				if((old!=null)&&(old!=E.myEnvironmental)) old.destroy(); } },
		DOM(){
			public ByteBuffer save(StdRoom E){ return CMLib.coffeeMaker().savString(E.myDom.name()); }
			public int size(){return 0;}
			public void load(StdRoom E, ByteBuffer S){
				Domain newDom=(Domain)CMClass.valueOf(Domain.class, CMLib.coffeeMaker().loadString(S));
				if(newDom!=null) E.myDom=newDom; } },
		ENC(){
			public ByteBuffer save(StdRoom E){ return CMLib.coffeeMaker().savString(E.myEnc.name()); }
			public int size(){return 4;}
			public void load(StdRoom E, ByteBuffer S){
				Enclosure newEnc=(Enclosure)CMClass.valueOf(Enclosure.class, CMLib.coffeeMaker().loadString(S));
				if(newEnc!=null) E.myEnc=newEnc; } },
		DSP(){
			public ByteBuffer save(StdRoom E){ return CMLib.coffeeMaker().savString(E.display); }
			public int size(){return 0;}
			public void load(StdRoom E, ByteBuffer S){ E.display=CMLib.coffeeMaker().loadString(S); } },
		DSC(){
			public ByteBuffer save(StdRoom E){ return CMLib.coffeeMaker().savString(E.desc); }
			public int size(){return 0;}
			public void load(StdRoom E, ByteBuffer S){ E.desc=CMLib.coffeeMaker().loadString(S); } },
		EXT(){
			public ByteBuffer save(StdRoom E){ return CMLib.coffeeMaker().savSaveNums(E.exits.toArray(CMSavable.dummyCMSavableArray)); }
			public int size(){return 0;}
			public void load(StdRoom E, ByteBuffer S){ E.exitsToLoad=CMLib.coffeeMaker().loadAInt(S); } },
		ARE(){
			public ByteBuffer save(StdRoom E){ return (ByteBuffer)ByteBuffer.wrap(new byte[4]).putInt((E.myArea==null)?0:E.myArea.saveNum()).rewind(); }
			public int size(){return 4;}
			public void load(StdRoom E, ByteBuffer S){ E.areaToLink=S.getInt(); } },
		INV(){
			public ByteBuffer save(StdRoom E){ return (ByteBuffer)ByteBuffer.wrap(new byte[4]).putInt(E.getItemCollection().saveNum()).rewind(); }
			public int size(){return 4;}
			public void load(StdRoom E, ByteBuffer S){ E.itemCollectionToLoad=S.getInt(); } },
		EFC(){
			public ByteBuffer save(StdRoom E){
				if(E.affects.size()>0) return CMLib.coffeeMaker().savSaveNums((CMSavable[])E.affects.toArray((CMSavable.dummyCMSavableArray)));
				return CoffeeMaker.emptyBuffer; }
			public int size(){return 0;}
			public void load(StdRoom E, ByteBuffer S){ E.effectsToLoad=CMLib.coffeeMaker().loadAInt(S); } },
		BHV(){
			public ByteBuffer save(StdRoom E){
				if(E.behaviors.size()>0) return CMLib.coffeeMaker().savSaveNums((CMSavable[])E.behaviors.toArray(CMSavable.dummyCMSavableArray));
				return CoffeeMaker.emptyBuffer; }
			public int size(){return 0;}
			public void load(StdRoom E, ByteBuffer S){ E.behavesToLoad=CMLib.coffeeMaker().loadAInt(S); } },
		NAM(){
			public ByteBuffer save(StdRoom E){ return CMLib.coffeeMaker().savString(E.name); }
			public int size(){return 0;}
			public void load(StdRoom E, ByteBuffer S){ E.name=CMLib.coffeeMaker().loadString(S); } },
		MAP(){
			public ByteBuffer save(StdRoom E){ return (ByteBuffer)ByteBuffer.wrap(new byte[4]).putInt(E.positions==null?0:E.positions.saveNum()).rewind(); }
			public int size(){return 4;}
			public void load(StdRoom E, ByteBuffer S){ E.envMapToLoad=S.getInt(); } },
		TRN(){
			public ByteBuffer save(StdRoom E){ return CMLib.coffeeMaker().savSubCollection(E.terrainObjects); }
			public int size(){return 0;}
			public void load(StdRoom E, ByteBuffer S){ E.terrainObjects.addAll(IterCollection.ICFactory(CMLib.coffeeMaker().loadSubCollection(S))); } },
		//TODO: EXT()
		;
		public CMSavable subObject(StdRoom fromThis){return null;} }
	private enum MCode implements ModEnum<StdRoom>{
/*		ID(){
			public String brief(StdRoom E){return E.myID;}
			public String prompt(StdRoom E){return E.myID;}
			public void mod(StdRoom E, MOB M){
				//E.setRoomID(CMLib.genEd().stringPrompt(M, ""+E.myID, false));
				M.session().rawPrintln("This value cannot be manually edited");} },
*/
		ENVIRONMENTAL(){
			public String brief(StdRoom E){return E.getEnvObject().ID();}
			public String prompt(StdRoom E){return "";}
			public void mod(StdRoom E, MOB M){CMLib.genEd().genMiscSet(M, E.myEnvironmental);} },
		DOMAIN(){
			public String brief(StdRoom E){return E.myDom.toString();}
			public String prompt(StdRoom E){return E.myDom.toString();}
			public void mod(StdRoom E, MOB M){E.myDom=(Domain)CMLib.genEd().enumPrompt(M, E.myDom.toString(), Domain.values());} },
		ENCLOSURE(){
			public String brief(StdRoom E){return E.myEnc.toString();}
			public String prompt(StdRoom E){return E.myEnc.toString();}
			public void mod(StdRoom E, MOB M){E.myEnc=(Enclosure)CMLib.genEd().enumPrompt(M, E.myEnc.toString(), Enclosure.values());} },
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
			public void mod(StdRoom E, MOB M){ E.setArea(CMLib.genEd().areaPrompt(M));} },
		EXITS(){
			public String brief(StdRoom E){return ""+E.exits.size();}
			public String prompt(StdRoom E){return "";}
			public void mod(StdRoom E, MOB M){CMLib.genEd().modExits(E.exits, E, M);} },
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
			public String brief(StdRoom E){return E.getItemCollection().ID()+" "+E.inventory.numItems();}
			public String prompt(StdRoom E){return "";}
			public void mod(StdRoom E, MOB M){CMLib.genEd().genMiscSet(M, E.inventory);} },
		ROOMMAP(){
			public String brief(StdRoom E){return E.positions==null?"null":(""+E.positions.size());}
			public String prompt(StdRoom E){return "";}
			public void mod(StdRoom E, MOB M){
				char action=M.session().prompt("(F)ill with missing objects, (D)estroy, (C)reate, or (M)odify room map (default M)? ","M").trim().toUpperCase().charAt(0);
				if(action=='F' && E.positions!=null) {
					for(ExitInstance exit : E.exits) if(E.positions.position(exit)==null) E.positions.placeThing(exit, 0, 0, 0);
					for(Iterator<Item> items=E.inventory.allItems();items.hasNext();) {
						Item item=items.next();
						if(E.positions.position(item)==null) E.positions.placeThing(item, 0, 0, 0); } }
				else if(action=='D') {E.positions.destroy(); E.positions=null;}
				else if(action=='C') {
					EnvMap map=CMLib.genEd().mapPrompt(M);
					EnvMap oldMap=E.positions;
					if(map!=null){
						if(oldMap!=null&&M.session().prompt("Copy old map into new? Y/n","Y").trim().toUpperCase().charAt(0)=='Y') map.copyFrom(oldMap);
						E.positions=map; } }
				else if(action=='M') CMLib.genEd().genMiscSet(M, E.positions); } },
		TERRAIN(){
			public String brief(StdRoom E){return ""+E.terrainObjects.size();}
			public String prompt(StdRoom E){return "";}
			public void mod(StdRoom E, MOB M){
				while((M.session()!=null)&&(!M.session().killFlag())) {
					ArrayList<Environmental> V=(ArrayList)E.terrainObjects.clone();
					int i=CMLib.genEd().promptVector(M, V, true);
					if(--i<0) break;
					else if(i==V.size()) {
						Environmental I=CMLib.genEd().newObjectOfType(M, CMClass.COMMON, Environmental.class, true, "Environmental");
						if(I!=null) E.terrainObjects.add(CMLib.genEd().genMiscSet(M, I)); }
					else if(i<V.size()) {
						char action=M.session().prompt("(D)estroy or (M)odify "+V.get(i).ID()+" (default M)? ","M").trim().toUpperCase().charAt(0);
						if(action=='D') {
							if(E.terrainObjects.remove(V.get(i))) V.get(i).destroy(); }
						else if(action=='M') CMLib.genEd().genMiscSet(M, V.get(i)); } } } },
		; }
}
