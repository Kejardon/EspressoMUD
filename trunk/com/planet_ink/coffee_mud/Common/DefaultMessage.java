package com.planet_ink.coffee_mud.Common;
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

/*
CoffeeMUD 5.6.2 copyright 2000-2010 Bo Zimmerman
EspressoMUD copyright 2011 Kejardon

Licensed under the Apache License, Version 2.0. You may obtain a copy of the license at
	http://www.apache.org/licenses/LICENSE-2.0
*/
@SuppressWarnings("unchecked")
public class DefaultMessage implements CMMsg
{
	public String ID(){return "DefaultMessage";}
	public CMObject newInstance(){try{return (CMObject)getClass().newInstance();}catch(Exception e){return new DefaultMessage();}}
	public void initializeClass(){}
	public int compareTo(CMObject o){ return CMClass.classID(this).compareToIgnoreCase(CMClass.classID(o));}
	public CMObject copyOf()
	{
		try
		{
			DefaultMessage clone=(DefaultMessage)this.clone();
			clone.sources=(Vector<Interactable>)sources.clone();
			clone.tools=(Vector<CMObject>)tools.clone();
			if(clone.trailMsgs!=null)
				clone.trailMsgs=(Vector<TrailMessage>)trailMsgs.clone();
			if(clone.trailHpns!=null)
				clone.trailHpns=(Vector<TrailMessage>)trailHpns.clone();
			clone.responders=(SortedList<SortedList.SortableObject<ListenHolder.MsgListener>>)responders.clone();
			return clone;
		}
		catch(CloneNotSupportedException e)
		{
			return newInstance();
		}
	}
	
	protected EnumSet<MsgCode> targetCode=null;
	protected EnumSet<MsgCode> sourceCode=null;
	protected EnumSet<MsgCode> othersCode=null;
	protected String targetMsg=null;
	protected String othersMsg=null;
	protected String sourceMsg=null;
	protected Vector<Interactable> sources=null;
	protected Interactable target=null;
	protected Vector<CMObject> tools=null;
	protected int value=0;
	protected Vector<TrailMessage> trailMsgs=null;
	protected Vector<TrailMessage> trailHpns=null;
	protected SortedList<SortedList.SortableObject<ListenHolder.MsgListener>> responders=null;
	protected int[] obvious=new int[SenseFlags_SIZE];

	public void returnMsg()
	{
		targetCode=null;
		sourceCode=null;
		othersCode=null;
		targetMsg=null;
		othersMsg=null;
		sourceMsg=null;
		sources=null;
		target=null;
		tools=null;
		if(trailMsgs!=null)
		{
			for(CMMsg.TrailMessage trailer : trailMsgs)
				trailer.msg.returnMsg();
			trailMsgs=null;
		}
		if(trailHpns!=null)
		{
			for(CMMsg.TrailMessage trailer : trailHpns)
				trailer.msg.returnMsg();
			trailHpns=null;
		}
		value=0;
		responders=null;
		obvious=new int[SenseFlags_SIZE];
		CMClass.returnMsg(this);
		//if(!CMClass.returnMsg(this))
		//	super.finalize();
	}

	public EnumSet<MsgCode> targetCode() { return targetCode; }
	public void setTargetCode(EnumSet<MsgCode> codes){targetCode=codes;}
	public boolean hasTargetCode(MsgCode... code)
	{
		if(targetCode==null)
			return false;
		for (MsgCode a : code)
			if(targetCode.contains(a))
				return true;
		return false;
	}
	public boolean addTargetCode(MsgCode code)
	{
		if(targetCode==null)
		{
			targetCode=EnumSet.of(code);
			return true;
		}
		return targetCode.add(code);
	}
	public boolean removeTargetCode(MsgCode code)
	{
		if(targetCode==null)
			return false;
		return targetCode.add(code);
	}
	public void setTargetMessage(String str){targetMsg=str;}
	public String targetMessage() { return targetMsg;}

	public EnumSet<MsgCode> sourceCode() { return sourceCode; }
	public void setSourceCode(EnumSet<MsgCode> codes){sourceCode=codes;}
	public boolean hasSourceCode(MsgCode... code)
	{
		if(sourceCode==null)
			return false;
		for (MsgCode a : code)
			if(sourceCode.contains(a))
				return true;
		return false;
	}
	public boolean addSourceCode(MsgCode code)
	{
		if(sourceCode==null)
		{
			sourceCode=EnumSet.of(code);
			return true;
		}
		return sourceCode.add(code);
	}
	public boolean removeSourceCode(MsgCode code)
	{
		if(sourceCode==null)
			return false;
		return sourceCode.add(code);
	}
	public void setSourceMessage(String str){sourceMsg=str;}
	public String sourceMessage() { return sourceMsg;}
	public Interactable[] sourceArr() { return (Interactable[])sources.toArray(Interactable.dummyInteractableArray); }

	public EnumSet<MsgCode> othersCode() { return othersCode; }
	public void setOthersCode(EnumSet<MsgCode> codes){othersCode=codes;}
	public boolean hasOthersCode(MsgCode... code)
	{
		if(othersCode==null)
			return false;
		for (MsgCode a : code)
			if(othersCode.contains(a))
				return true;
		return false;
	}
	public boolean addOthersCode(MsgCode code)
	{
		if(othersCode==null)
		{
			othersCode=EnumSet.of(code);
			return true;
		}
		return othersCode.add(code);
	}
	public boolean removeOthersCode(MsgCode code)
	{
		if(othersCode==null)
			return false;
		return othersCode.add(code);
	}
	public void setOthersMessage(String str){othersMsg=str;}
	public String othersMessage() { return othersMsg; }

	public int value(){return value;}
	public void setValue(int amount)
	{
		value=amount;
	}
	public int obvious(int type){return obvious[type];}
	public void setObvious(int type, int value){obvious[type]=value;}

	public Vector<ListenHolder.MsgListener> responders()
	{
		Vector<ListenHolder.MsgListener> V=new Vector<ListenHolder.MsgListener>();
		if(responders!=null)
			for(ListIterator<SortedList.SortableObject<ListenHolder.MsgListener>> L=responders.listIterator();L.hasNext();V.add(L.next().myObj)){}
		return V;
	}
	public void addResponse(ListenHolder.MsgListener E, int priority)
	{
		if(responders==null) responders=new SortedList<SortedList.SortableObject<ListenHolder.MsgListener>>();
		responders.add(new SortedList.SortableObject<ListenHolder.MsgListener>(E, priority));
	}
	public boolean handleResponses()
	{
		ListenHolder.MsgListener next=null;
		if(responders!=null)
		for(ListIterator<SortedList.SortableObject<ListenHolder.MsgListener>> L=responders.listIterator();L.hasNext();next=L.next().myObj)
			if(!(next.respondTo(this)))
				return false;
		return true;
	}

	public Vector<TrailMessage> trailerMsgs(){ return trailMsgs;}
	public void addTrailerMsg(Room forHere, CMMsg msg)
	{
		if(trailMsgs==null) trailMsgs=new Vector<TrailMessage>();
		trailMsgs.add(new TrailMessage(forHere, msg));
	}
	public Vector<TrailMessage> trailerHappens(){ return trailHpns;}
	public void addTrailerHappens(Room forHere, CMMsg msg)
	{
		if(trailHpns==null) trailHpns=new Vector<TrailMessage>();
		trailHpns.add(new TrailMessage(forHere, msg));
	}

	public Interactable target() { return target; }
	public void setTarget(Interactable E){target=E;}

	public Vector<Interactable> source(){ return sources; }
	public Interactable firstSource()
	{
		if((sources==null)||(sources.size()==0)) return null;
		return sources.get(0);
	}
	public void setSource(Vector<Interactable> V){sources=V;}
	public boolean addSource(Interactable E)
	{
		if(sources==null) sources=new Vector<Interactable>();
		else if(sources.contains(E))
			return false;
		return sources.add(E);
	}
	public boolean removeSource(Interactable E){return sources.remove(E);}

	public Vector<CMObject> tool() { return tools; }
	public CMObject firstTool()
	{
		if((tools==null)||(tools.size()==0)) return null;
		return tools.get(0);
	}
	public void setTools(Vector<CMObject> V){tools=V;}
	public boolean addTool(CMObject O)
	{
		if(tools==null) tools=new Vector<CMObject>();
		else if(tools.contains(O))
			return false;
		return tools.add(O);
	}
	public boolean removeTool(CMObject O){return tools.remove(O);}
	public CMObject[] toolArr() { return (CMObject[])tools.toArray(CMObject.dummyCMOArray); }

	public boolean isTool(CMObject E)
	{
		if(tools==null) return false;
		return tools.contains(E);
	}
	public boolean isTarget(Interactable E) { return target==E; }
	public boolean isSource(Interactable E)
	{
		if(sources==null) return false;
		return sources.contains(E);
	}
	//TODO
	public boolean isOthers(Interactable E){return (!isTarget(E))&&(!isSource(E))&&(!isTool(E));}
	
	public String toString()
	{
		StringBuilder output=new StringBuilder();
		if(sources!=null)
		for(int i=sources.size()-1;i>=0;i--)
		{
			output.append(sources.get(i).ID()+", "+sources.get(i).name());
			if(i!=0) output.append(", ");
		}
		output.append(':');
		for(MsgCode c : sourceCode)
			output.append(c.toString()+" ");
		output.append(':');
		output.append(sourceMsg);
		output.append('/');
		if(target!=null)
			output.append(target.ID()+" "+target.name());
		else
			output.append("null");
		output.append(':');
		for(MsgCode c : targetCode)
			output.append(c.toString()+" ");
		output.append(':');
		output.append(targetMsg);
		output.append('/');
		if(tools!=null)
		for(int i=tools.size()-1;i>=0;i--)
		{
			output.append(tools.get(i).ID()+", "+sources.get(i).name());
			if(i!=0) output.append(", ");
			
		}
		output.append('/');
		for(MsgCode c : othersCode)
			output.append(c.toString()+" ");
		output.append(':');
		output.append(othersMsg);
		return output.toString();
	}
}
