package com.planet_ink.coffee_mud.Common;
import com.planet_ink.coffee_mud.Libraries.interfaces.*;
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
import com.planet_ink.coffee_mud.MOBS.StdMOB;
import com.planet_ink.coffee_mud.MOBS.interfaces.*;
import com.planet_ink.coffee_mud.Races.interfaces.*;

import java.util.*;

/*
CoffeeMUD 5.6.2 copyright 2000-2010 Bo Zimmerman
EspressoMUD copyright 2011 Kejardon

Licensed under the Apache License, Version 2.0. You may obtain a copy of the license at
	http://www.apache.org/licenses/LICENSE-2.0
*/
@SuppressWarnings("unchecked")
public class DefaultPlayerStats implements PlayerStats
{
	public String ID(){return "DefaultPlayerStats";}
	protected final static int TELL_STACK_MAX_SIZE=50;
	protected final static int GTELL_STACK_MAX_SIZE=50;

	protected HashSet<String> friends=new HashSet();
	protected HashSet<String> ignored=new HashSet();
	protected Vector<String> tellStack=new Vector();
	protected Vector<String> gtellStack=new Vector();
	protected HashMap<String, String> alias=new HashMap();
	protected String lastIP="";
	protected long LastDateTime=System.currentTimeMillis();
	protected long lastUpdated=0;
	protected int channelMask;
	protected String Password="";
	protected String colorStr="";
	protected String prompt="";
	protected PlayerAccount account = null;
	protected int wrap=78;
	protected int pageBreak=0;
	protected MOB replyTo=null;
	protected int replyType=0;
	protected long replyTime=0;
	
	protected Vector<String> securityGroups=new Vector();
//	protected RoomnumberSet visitedRoomSet=null;
	protected HashSet<String> introductions=new HashSet();
	
	protected int bitmap=0;

	public DefaultPlayerStats() {
		super();
	}
	
/*	public boolean sameAs(PlayerStats E)
	{
		if(!(E instanceof DefaultPlayerStats)) return false;
		return true;
	} */
	
	public CMObject newInstance(){try{return (CMObject)getClass().newInstance();}catch(Exception e){return new DefaultPlayerStats();}}
	public void initializeClass(){}
	public CMObject copyOf()
	{
		try
		{
			DefaultPlayerStats O=(DefaultPlayerStats)this.clone();
//			if(visitedRoomSet!=null)
//				O.visitedRoomSet=(RoomnumberSet)visitedRoomSet.copyOf();
//			else
//				O.visitedRoomSet=null;
			O.securityGroups=(Vector)securityGroups.clone();
			O.friends=(HashSet)friends.clone();
			O.ignored=(HashSet)ignored.clone();
			O.tellStack=(Vector)tellStack.clone();
			O.gtellStack=(Vector)gtellStack.clone();
			O.alias=(HashMap)alias.clone();
			return O;
		}
		catch(CloneNotSupportedException e)
		{
			return new DefaultPlayerStats();
		}
	}
	public String lastIP(){return lastIP;}
	public void setLastIP(String ip)
	{
		lastIP=ip;
		if(account != null)
			account.setLastIP(ip);
	}
	public long lastUpdated(){return lastUpdated;}
	public void setLastUpdated(long time)
	{
		lastUpdated=time;
		if(account != null)
			account.setLastUpdated(time);
	}
	public long lastDateTime(){return LastDateTime;}
	public void setLastDateTime(long C)
	{ 
		LastDateTime=C;
		if(account != null)
			account.setLastDateTime(C);
	}
	public String password(){return (account!=null)?account.password():Password;}
	public void setPassword(String newPassword)
	{
		Password=newPassword;
		if(account != null)
			account.setPassword(newPassword);
	}
	
	public int getWrap(){return wrap;}
	public void setWrap(int newWrap){wrap=newWrap;}
	public int getPageBreak(){return pageBreak;}
	public void setPageBreak(int newBreak){pageBreak=newBreak;}
	public void setChannelMask(int newMask){ channelMask=newMask;}
	public int getChannelMask(){ return channelMask;}
	public MOB replyTo(){	return replyTo;	}
	public int replyType(){	return replyType;}
	public long replyTime(){	return replyTime;	}
	public void setReplyTo(MOB mob, int replyType)
	{	
		replyTo=mob;
		this.replyType=replyType;
	}
	public void setPrompt(String newPrompt){prompt=newPrompt;}
	public String getColorStr(){return colorStr;}
	public void setColorStr(String newColors){colorStr=newColors;}
	public String getPrompt()
	{
		if((prompt==null)||(prompt.length()==0))
		{
			prompt=CMProps.Strings.DEFAULTPROMPT.property();
			if((prompt==null)||(prompt.length()==0))
				return "^N%E<^h%hhp ^m%mm ^v%vmv^N>";
		}
		return prompt;
	}

	public boolean isIntroducedTo(String name){return introductions.contains(name.toUpperCase().trim());}
	public void introduceTo(String name){
		if((!isIntroducedTo(name))&&(name.trim().length()>0))
			introductions.add(name.toUpperCase().trim());
	}
	
	public HashSet getHashFrom(String str)
	{
		HashSet h=new HashSet();
		if((str==null)||(str.length()==0)) return h;
		str=CMStrings.replaceAll(str,"<FRIENDS>","");
		str=CMStrings.replaceAll(str,"<IGNORED>","");
		str=CMStrings.replaceAll(str,"<INTROS>","");
		str=CMStrings.replaceAll(str,"</INTROS>","");
		str=CMStrings.replaceAll(str,"</FRIENDS>","");
		str=CMStrings.replaceAll(str,"</IGNORED>","");
		int x=str.indexOf(";");
		while(x>=0)
		{
			String fi=str.substring(0,x).trim();
			if(fi.length()>0) h.add(fi);
			str=str.substring(x+1);
			x=str.indexOf(";");
		}
		if(str.trim().length()>0)
			h.add(str.trim());
		return h;
	}

	public void addTellStack(String msg)
	{
		if(tellStack.size()>TELL_STACK_MAX_SIZE)
			tellStack.removeElementAt(0);
		tellStack.addElement(msg);
	}
	
	public Vector<String> getTellStack()
	{
		return (Vector)tellStack.clone();
	}
	public void addGTellStack(String msg)
	{
		if(gtellStack.size()>GTELL_STACK_MAX_SIZE)
			gtellStack.removeElementAt(0);
		gtellStack.addElement(msg);
	}
	
	public Vector<String> getGTellStack()
	{
		return (Vector)gtellStack.clone();
	}
	
	public HashSet getFriends()
	{
		if(account != null)
			return account.getFriends();
		return friends;
	}
	public HashSet getIgnored()
	{
		if(account != null)
			return account.getIgnored();
		return ignored;
	}
	
	public String[] getAliasNames()
	{ return (String[])alias.keySet().toArray(new String[0]); }
	//TODO: Should this return "" or null?
	public String getAlias(String named)
	{
		String S=alias.get(named);
		if(S==null) return "";
		return S;
	}
	public void delAliasName(String named) { alias.remove(named); }
	public void setAlias(String named, String value) { alias.put(named, value); }

	public Vector<String> getSecurityGroups(){	return securityGroups;}
	
	public int getBitmap() {return bitmap;}
	
	public void setBitmap(int newBits) {bitmap=newBits;}
	
	public PlayerAccount getAccount() { return account;}
	public void setAccount(PlayerAccount account) { this.account = account;}

	//CMModifiable and CMSavable
	public SaveEnum[] totalEnumS(){return SCode.values();}
	public Enum[] headerEnumS(){return new Enum[] {SCode.values()[0]} ;}
	public ModEnum[] totalEnumM(){return MCode.values();}
	public Enum[] headerEnumM(){return new Enum[] {MCode.values()[0]};}

	private enum SCode implements CMSavable.SaveEnum{
		FRN(){
			public String save(DefaultPlayerStats E){ return CMLib.coffeeMaker().savAString((String[])E.friends.toArray(new String[0])); }
			public void load(DefaultPlayerStats E, String S){ for(String newF : CMLib.coffeeMaker().loadAString(S)) E.friends.add(newF); } },
		IGN(){
			public String save(DefaultPlayerStats E){ return CMLib.coffeeMaker().savAString((String[])E.ignored.toArray(new String[0])); }
			public void load(DefaultPlayerStats E, String S){ for(String newI : CMLib.coffeeMaker().loadAString(S)) E.ignored.add(newI); } },
		ALS(){
			public String save(DefaultPlayerStats E){
				return CMLib.coffeeMaker().savStringsInterlaced((String[])E.alias.keySet().toArray(new String[0]), (String[])E.alias.values().toArray(new String[0])); }
			public void load(DefaultPlayerStats E, String S){
				String[][] vals=CMLib.coffeeMaker().loadStringsInterlaced(S, 2);
				for(String[] entry : vals)
					E.setAlias(entry[0], entry[1]); } },
		LIP(){
			public String save(DefaultPlayerStats E){ return E.lastIP; }
			public void load(DefaultPlayerStats E, String S){ E.lastIP=S.intern(); } },
		CHN(){
			public String save(DefaultPlayerStats E){ return ""+E.channelMask; }
			public void load(DefaultPlayerStats E, String S){ E.channelMask=Integer.parseInt(S); } },
		PSS(){
			public String save(DefaultPlayerStats E){ return E.Password; }
			public void load(DefaultPlayerStats E, String S){ E.Password=S.intern(); } },
		COL(){
			public String save(DefaultPlayerStats E){ return E.colorStr; }
			public void load(DefaultPlayerStats E, String S){ E.colorStr=S.intern(); } },
		PMP(){
			public String save(DefaultPlayerStats E){ return E.prompt; }
			public void load(DefaultPlayerStats E, String S){ E.prompt=S.intern(); } },
		ACT(){
			public String save(DefaultPlayerStats E){ if(E.account==null) return ""; return E.account.accountName(); }
			public void load(DefaultPlayerStats E, String S){ E.account=CMLib.players().getLoadAccount(S); } },
		WRP(){
			public String save(DefaultPlayerStats E){ return ""+E.wrap; }
			public void load(DefaultPlayerStats E, String S){ E.wrap=Integer.parseInt(S); } },
		BRK(){
			public String save(DefaultPlayerStats E){ return ""+E.pageBreak; }
			public void load(DefaultPlayerStats E, String S){ E.pageBreak=Integer.parseInt(S); } },
		INT(){
			public String save(DefaultPlayerStats E){ return CMLib.coffeeMaker().savAString((String[])E.introductions.toArray(new String[0])); }
			public void load(DefaultPlayerStats E, String S){ for(String newI : CMLib.coffeeMaker().loadAString(S)) E.introductions.add(newI); } },
		BIT(){
			public String save(DefaultPlayerStats E){ return ""+E.bitmap; }
			public void load(DefaultPlayerStats E, String S){ E.bitmap=Integer.parseInt(S); } },
		;
		public abstract String save(DefaultPlayerStats E);
		public abstract void load(DefaultPlayerStats E, String S);
		public String save(CMSavable E){return save((DefaultPlayerStats)E);}
		public void load(CMSavable E, String S){load((DefaultPlayerStats)E, S);} }
	private enum MCode implements CMModifiable.ModEnum{
		ACCOUNT(){
			public String brief(DefaultPlayerStats E){if(E.account==null) return ""; return E.account.accountName();}
			public String prompt(DefaultPlayerStats E){if(E.account==null) return ""; return E.account.accountName();}
			public void mod(DefaultPlayerStats E, MOB M){
				String S=CMLib.genEd().stringPrompt(M, "", true);
				if(S==null) E.account=null;
				PlayerAccount acc=CMLib.players().getLoadAccount(S);
				if(acc!=null) E.account=acc; } },
		SECURITY(){
			public String brief(DefaultPlayerStats E){return ""+E.securityGroups.size();}
			public String prompt(DefaultPlayerStats E){return E.securityGroups.toString();}
			public void mod(DefaultPlayerStats E, MOB M){
				boolean done=false;
				while((M.session()!=null)&&(!M.session().killFlag())&&(!done)) {
					Vector<String> V=(Vector<String>)E.securityGroups.clone();
					int i=CMLib.genEd().promptVector(M, V, true);
					if(--i<0) done=true;
					else if(i==V.size()) {
						String S=CMLib.genEd().stringPrompt(M, "", false).trim().toUpperCase();
						if((S.length()>0)&&(!E.securityGroups.contains(S))) E.securityGroups.add(S); }
					else if(i<V.size()) E.securityGroups.remove(V.get(i)); } } },
		INTRODUCED(){
			public String brief(DefaultPlayerStats E){return ""+E.introductions.size();}
			public String prompt(DefaultPlayerStats E){return ""+E.introductions.toArray(new String[0]);}
			public void mod(DefaultPlayerStats E, MOB M){
				boolean done=false;
				while((M.session()!=null)&&(!M.session().killFlag())&&(!done)) {
					String S=CMStrings.capitalizeAndLower(CMLib.genEd().stringPrompt(M, "", false));
					if(S.length()>0) {
						if(E.introductions.contains(S)&&(M.session().confirm("Remove this name?", "N"))) E.introductions.remove(S);
						else if(M.session().confirm("Add this name?", "N")) E.introductions.add(S); }
					else done=true; } } },
		;
		public abstract String brief(DefaultPlayerStats fromThis);
		public abstract String prompt(DefaultPlayerStats fromThis);
		public abstract void mod(DefaultPlayerStats toThis, MOB M);
		public String brief(CMModifiable fromThis){return brief((DefaultPlayerStats)fromThis);}
		public String prompt(CMModifiable fromThis){return prompt((DefaultPlayerStats)fromThis);}
		public void mod(CMModifiable toThis, MOB M){mod((DefaultPlayerStats)toThis, M);} }

	public int compareTo(CMObject o){ return CMClass.classID(this).compareToIgnoreCase(CMClass.classID(o));}
/*	public RoomnumberSet roomSet()
	{
		if(visitedRoomSet==null)
			visitedRoomSet=((RoomnumberSet)CMClass.getCommon("DefaultRoomnumberSet"));
		return visitedRoomSet;
	}
	public void addRoomVisit(Room R)
	{
		if((!CMSecurity.isDisabled("ROOMVISITS"))
		&&(R!=null)
		&&(!CMath.bset(R.envStats().sensesMask(),EnvStats.SENSE_ROOMUNEXPLORABLE)))
			roomSet().add(CMLib.map().getExtendedRoomID(R));
	}
	public boolean hasVisited(Room R)
	{
		return roomSet().contains(CMLib.map().getExtendedRoomID(R));
	}
	public boolean hasVisited(Area A)
	{
		int numRooms=A.getAreaIStats()[Area.AREASTAT_VISITABLEROOMS];
		if(numRooms<=0) return true;
		return roomSet().roomCount(A.Name())>0;
	}
	public int percentVisited(MOB mob, Area A)
	{
		if(A==null)
		{
			long totalRooms=0;
			long totalVisits=0;
			for(Enumeration e=CMLib.map().areas();e.hasMoreElements();)
			{
				A=(Area)e.nextElement();
				if((CMLib.flags().canAccess(mob,A))
				&&(!CMath.bset(A.flags(),Area.FLAG_INSTANCE_CHILD)))
				{
					int[] stats=A.getAreaIStats();
					if(stats[Area.AREASTAT_VISITABLEROOMS]>0)
					{
						totalRooms+=stats[Area.AREASTAT_VISITABLEROOMS];
						totalVisits+=roomSet().roomCount(A.Name());
					}
				}
			}
			if(totalRooms==0) return 100;
			double pct=CMath.div(totalVisits,totalRooms);
			return (int)Math.round(100.0*pct);
		}
		int numRooms=A.getAreaIStats()[Area.AREASTAT_VISITABLEROOMS];
		if(numRooms<=0) return 100;
		double pct=CMath.div(roomSet().roomCount(A.Name()),numRooms);
		return (int)Math.round(100.0*pct);
	}
	public void addAliasName(String named)
	{
		named=named.toUpperCase().trim();
		if(getAlias(named).length()==0)
			alias.addElement(named,"");
	}
*/
}
