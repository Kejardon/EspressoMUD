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
import com.planet_ink.coffee_mud.Libraries.interfaces.*;
import com.planet_ink.coffee_mud.Locales.interfaces.*;
import com.planet_ink.coffee_mud.MOBS.StdMOB;
import com.planet_ink.coffee_mud.MOBS.interfaces.*;
import com.planet_ink.coffee_mud.Races.interfaces.*;

import java.util.*;
import java.nio.ByteBuffer;

/*
CoffeeMUD 5.6.2 copyright 2000-2010 Bo Zimmerman
EspressoMUD copyright 2011 Kejardon

Licensed under the Apache License, Version 2.0. You may obtain a copy of the license at
	http://www.apache.org/licenses/LICENSE-2.0
*/
@SuppressWarnings("unchecked")
public class DefaultPlayerAccount implements PlayerAccount
{
	public String ID(){return "DefaultPlayerAccount";}
	protected HashSet<MOB> friends=new HashSet();
	protected HashSet<MOB> ignored=new HashSet();
	protected Vector<AccountStats> ignoredBy=new Vector();
	protected Vector<MOB> fullPlayers = new Vector<MOB>();
	protected String accountName = "";
	protected byte[] lastIP=null;//"";
	protected long LastDateTime=System.currentTimeMillis();
	protected long lastUpdated=0;
	protected String Password="";
	protected HashSet<String> acctFlags = new HashSet<String>();

	protected int saveNum=0;
	protected int[] friendsToAdd=null;
	protected int[] ignoredToAdd=null;
	protected int[] playersToAdd=null;

	public boolean sameAs(PlayerAccount E)
	{
		if(!(E instanceof DefaultPlayerAccount)) return false;
		return true;
	}

	public CMObject newInstance(){try{return (CMObject)getClass().newInstance();}catch(Exception e){return new DefaultPlayerAccount();}}
	public void initializeClass(){}
	public CMObject copyOf()
	{
		try
		{
			DefaultPlayerAccount O=(DefaultPlayerAccount)this.clone();
			O.saveNum=0;
			O.friends=(HashSet)friends.clone();
			O.ignored=(HashSet)ignored.clone();
			return O;
		}
		catch(CloneNotSupportedException e)
		{
			return new DefaultPlayerAccount();
		}
	}
	public int compareTo(CMObject o){ return CMClass.classID(this).compareToIgnoreCase(CMClass.classID(o));}
	public byte[] lastIP(){return lastIP;}
	public void setLastIP(byte[] ip){lastIP=ip; CMLib.database().saveObject(this);}
	public long lastUpdated(){return lastUpdated;}
	public void setLastUpdated(long time){lastUpdated=time; CMLib.database().saveObject(this);}
	public long lastDateTime(){return LastDateTime;}
	public void setLastDateTime(long C){ LastDateTime=C; CMLib.database().saveObject(this);}
	public String password(){return Password;}
	public void setPassword(String newPassword){Password=newPassword; CMLib.database().saveObject(this);}

	public HashSet<MOB> getFriends(){return friends;}
	public HashSet<MOB> getIgnored(){return ignored;}
	public Vector<AccountStats> getIgnoredBy(){return ignoredBy;}

/*	protected String getPrivateList(HashSet h)
	{
		if((h==null)||(h.size()==0)) return "";
		StringBuffer list=new StringBuffer("");
		for(Iterator e=h.iterator();e.hasNext();)
			list.append(((String)e.next())+";");
		return list.toString();
	}
*/

	public String accountName() { return accountName;}
	public void setAccountName(String name) { accountName = name; CMLib.database().saveObject(this);}
	
	public void addNewPlayer(MOB mob) 
	{
		synchronized(fullPlayers)
		{
			if(fullPlayers.contains(mob))
				return;
			fullPlayers.add(mob);
		}
		CMLib.database().saveObject(this);
	}
	
	public boolean isPlayer(String name) 
	{
		if(name==null) return false;
		for(int i=0;i<fullPlayers.size();i++)
			if(fullPlayers.get(i).name().equals(name)) return true;
		return false;
	}
	
	public void delPlayer(MOB mob) 
	{
		synchronized(fullPlayers) {fullPlayers.remove(mob); }
		CMLib.database().saveObject(this);
	}
	public Enumeration<MOB> getLoadPlayers() 
	{
		return fullPlayers.elements();
	}
	public int numPlayers() { return fullPlayers.size();}
	public boolean isSet(String flagName) { return acctFlags.contains(flagName.toUpperCase());}
	public void setFlag(String flagName, boolean setOrUnset)
	{
		if(setOrUnset)
			acctFlags.add(flagName.toUpperCase());
		else
			acctFlags.remove(flagName.toUpperCase());
		CMLib.database().saveObject(this);
	}
	
	public void destroy()
	{
		//TODO
		CMLib.database().deleteObject(this);
	}

	//CMModifiable and CMSavable
	public SaveEnum[] totalEnumS(){return SCode.values();}
	public Enum[] headerEnumS(){return new Enum[] {SCode.values()[0]};}
	public ModEnum[] totalEnumM(){return MCode.values();}
	public Enum[] headerEnumM(){return new Enum[] {MCode.values()[0]};}
	public int saveNum()
	{
		if(saveNum==0)
		synchronized(this)
		{
			if(saveNum==0)
				saveNum=SIDLib.Objects.ACCOUNT.getNumber(this);
		}
		return saveNum;
	}
	public void setSaveNum(int num)
	{
		synchronized(this)
		{
			if(saveNum!=0)
				SIDLib.Objects.ACCOUNT.removeNumber(saveNum);
			saveNum=num;
			SIDLib.Objects.ACCOUNT.assignNumber(num, this);
		}
	}
	public boolean needLink(){return true;}
	public void link()
	{
		if(friendsToAdd!=null)
		{
			for(int SID : friendsToAdd)
			{
				MOB friend = (MOB)SIDLib.Objects.CREATURE.get(SID);
				if(friend!=null)
					friends.add(friend);
			}
			friendsToAdd=null;
		}
		if(ignoredToAdd!=null)
		{
			for(int SID : ignoredToAdd)
			{
				MOB ignoredM = (MOB)SIDLib.Objects.CREATURE.get(SID);
				if((ignoredM!=null)&&(!ignoredM.isMonster()))
				{
					ignored.add(ignoredM);
					ignoredM.playerStats().getIgnoredBy().add(this);
				}
			}
			ignoredToAdd=null;
		}
		if(playersToAdd!=null)
		{
			for(int SID : playersToAdd)
			{
				MOB player= (MOB)SIDLib.Objects.CREATURE.get(SID);
				if(player!=null)
					fullPlayers.add(player);
			}
			playersToAdd=null;
		}
	}
	public void saveThis(){CMLib.database().saveObject(this);}

	private enum SCode implements CMSavable.SaveEnum{
		FRN(){
			public ByteBuffer save(DefaultPlayerAccount E){ return CMLib.coffeeMaker().savSaveNums((CMSavable[])E.friends.toArray(new CMSavable[E.friends.size()])); }
			public int size(){return 0;}
			public void load(DefaultPlayerAccount E, ByteBuffer S){ E.friendsToAdd=CMLib.coffeeMaker().loadAInt(S); } },
		IGN(){
			public ByteBuffer save(DefaultPlayerAccount E){ return CMLib.coffeeMaker().savSaveNums((CMSavable[])E.ignored.toArray(new CMSavable[E.ignored.size()])); }
			public int size(){return 0;}
			public void load(DefaultPlayerAccount E, ByteBuffer S){ E.ignoredToAdd=CMLib.coffeeMaker().loadAInt(S); } },
		LIP(){
			public ByteBuffer save(DefaultPlayerAccount E){ return ByteBuffer.wrap(E.lastIP); }
			public int size(){return 0;}	//Ideally I can just say 4 but... ipv6.
			public void load(DefaultPlayerAccount E, ByteBuffer S){ S.get(E.lastIP); } },
		PSS(){
			public ByteBuffer save(DefaultPlayerAccount E){ return CMLib.coffeeMaker().savString(E.Password); }
			public int size(){return 0;}
			public void load(DefaultPlayerAccount E, ByteBuffer S){ E.Password=CMLib.coffeeMaker().loadString(S); } },
		FLG(){
			public ByteBuffer save(DefaultPlayerAccount E){ return CMLib.coffeeMaker().savAString((String[])E.acctFlags.toArray(new String[0])); }
			public int size(){return 0;}
			public void load(DefaultPlayerAccount E, ByteBuffer S){ for(String newF : CMLib.coffeeMaker().loadAString(S)) E.acctFlags.add(newF); } },
		;
		public abstract ByteBuffer save(DefaultPlayerAccount E);
		public abstract void load(DefaultPlayerAccount E, ByteBuffer S);
		public ByteBuffer save(CMSavable E){return save((DefaultPlayerAccount)E);}
		public CMSavable subObject(CMSavable fromThis){return null;}
		public void load(CMSavable E, ByteBuffer S){load((DefaultPlayerAccount)E, S);} }
	private enum MCode implements CMModifiable.ModEnum{
		CHARACTERS(){
			public String brief(DefaultPlayerAccount E){ return ""+E.fullPlayers.size();}
			public String prompt(DefaultPlayerAccount E){ return "";}
			public void mod(DefaultPlayerAccount E, MOB M){
				boolean done=false;
				while((M.session()!=null)&&(!M.session().killFlag())&&(!done)) {
					Vector<MOB> V=(Vector<MOB>)E.fullPlayers.clone();
					int i=CMLib.genEd().promptVector(M, V, false);
					if(--i<0) done=true;
					else if(i<V.size()) {
						char action=M.session().prompt("(D)estroy or (M)odify "+V.get(i).name()+" (default M)? ","M").trim().toUpperCase().charAt(0);
						if(action=='D') {
							MOB mob = V.get(i);
							if(M.session().confirm("WARNING: This will PERMANENTLY DELETE the character "+mob.name()+". Are you sure?", "N")) {
								E.delPlayer(mob);
								mob.destroy(); } }	//TODO: Database deletion of character?
						else if(action=='M') CMLib.genEd().genMiscSet(M, V.get(i)); } } } },
		FLAGS(){
			public String brief(DefaultPlayerAccount E){return ""+E.acctFlags.size();}
			public String prompt(DefaultPlayerAccount E){return ""+E.acctFlags.toArray(new String[0]);}
			public void mod(DefaultPlayerAccount E, MOB M){
				boolean done=false;
				while((M.session()!=null)&&(!M.session().killFlag())&&(!done)) {
					String S=CMLib.genEd().stringPrompt(M, "", false).toUpperCase();
					if(S.length()>0) {
						if(E.acctFlags.contains(S)&&(M.session().confirm("Remove this flag?", "N"))) E.acctFlags.remove(S);
						else if(M.session().confirm("Add this flag?", "N")) E.acctFlags.add(S); }
					else done=true; } } },
		;
		public abstract String brief(DefaultPlayerAccount fromThis);
		public abstract String prompt(DefaultPlayerAccount fromThis);
		public abstract void mod(DefaultPlayerAccount toThis, MOB M);
		public String brief(CMModifiable fromThis){return brief((DefaultPlayerAccount)fromThis);}
		public String prompt(CMModifiable fromThis){return prompt((DefaultPlayerAccount)fromThis);}
		public void mod(CMModifiable toThis, MOB M){mod((DefaultPlayerAccount)toThis, M);} }
}
