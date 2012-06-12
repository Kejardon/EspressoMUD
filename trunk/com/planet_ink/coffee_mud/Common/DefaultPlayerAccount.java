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
public class DefaultPlayerAccount implements PlayerAccount
{
	public String ID(){return "DefaultPlayerAccount";}
	protected HashSet<AccountStats> friends=new HashSet();
	protected HashSet<MOB> ignored=new HashSet();
	//protected HashSet<AccountStats> ignoredBy=new HashSet();
	protected CopyOnWriteArrayList<MOB> fullPlayers = new CopyOnWriteArrayList();
	protected String accountName = "";
	protected byte[] lastIP=null;//"";
	protected long LastDateTime=System.currentTimeMillis();
	protected long lastUpdated=0;
	protected String Password="";
//	protected HashSet<String> acctFlags = new HashSet<String>();
	protected int bitmap=0;
	protected int accBitmap=0;

	protected int saveNum=0;
	protected boolean amDestroyed=false;
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

	public boolean hasFriend(AccountStats M){return ((friends.contains(M))||((M instanceof PlayerStats)&&(friends.contains(((PlayerStats)M).getAccount()))));}
	public boolean addFriend(AccountStats M){ boolean result=false;
		synchronized(friends) { result=friends.add(M); }
		if(result) CMLib.database().saveObject(this);
		return result; }
	public boolean removeFriend(AccountStats M){ boolean result=false;
		synchronized(friends) { result=friends.remove(M); }
		if(result) CMLib.database().saveObject(this);
		return result; }
	public AccountStats removeFriend(String M){
		synchronized(friends) {
			for(AccountStats oldFriend : friends)
			if(((oldFriend instanceof PlayerStats)&&(((PlayerStats)oldFriend).mob().name().equals(M)))
			  ||(((PlayerAccount)oldFriend).accountName().equalsIgnoreCase(M))) {
				friends.remove(oldFriend);
				CMLib.database().saveObject(this);
				return oldFriend; }}
		return null; }
	public AccountStats[] getFriends(){return (AccountStats[])friends.toArray(AccountStats.dummyASArray);}
	public boolean hasIgnored(MOB M){return ignored.contains(M);}
	public boolean addIgnored(MOB M){ boolean result=false;
		synchronized(ignored) { result=ignored.add(M); }
		if(result) CMLib.database().saveObject(this);
		return result; }
	public boolean removeIgnored(MOB M){ boolean result=false;
		synchronized(ignored) { result=ignored.remove(M); }
		if(result) CMLib.database().saveObject(this);
		return result; }
	public MOB[] getIgnored(){return (MOB[])ignored.toArray(MOB.dummyMOBArray);}
	//public boolean addIgnoredBy(AccountStats M){synchronized(ignoredBy) { return ignoredBy.add(M); } }
	//public boolean removeIgnoredBy(AccountStats M){synchronized(ignoredBy) { return ignoredBy.remove(M); } }
	//public Vector<AccountStats> getIgnoredBy(){return ignoredBy;}

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
		if(fullPlayers.addIfAbsent(mob))
			CMLib.database().saveObject(this);
	}
	public boolean isPlayer(String name) 
	{
		if(name!=null)
			for(MOB M : fullPlayers)
				if(M.name().equalsIgnoreCase(name))
					return true;
		return false;
	}
	public MOB getPlayer(String name) 
	{
		if(name!=null)
			for(MOB M : fullPlayers)
				if(M.name().equalsIgnoreCase(name))
					return M;
		return null;
	}
	public void delPlayer(MOB mob) 
	{
		if(fullPlayers.remove(mob))
			CMLib.database().saveObject(this);
	}
	public Iterator<MOB> getLoadPlayers() 
	{
		return fullPlayers.iterator();
	}
	public int numPlayers() { return fullPlayers.size();}
/*
	public boolean isSet(String flagName) { return acctFlags.contains(flagName.toUpperCase());}
	public void setFlag(String flagName, boolean setOrUnset)
	{
		if(setOrUnset)
			acctFlags.add(flagName.toUpperCase());
		else
			acctFlags.remove(flagName.toUpperCase());
		CMLib.database().saveObject(this);
	}
*/
	public int getBitmap() {return bitmap;}	
	public void setBitmap(int newBits) {bitmap=newBits; CMLib.database().saveObject(this);}
	public void setBits(int bits, boolean set)
	{
		if(set)
			bitmap|=bits;
		else
			bitmap&=bits;
		CMLib.database().saveObject(this);
	}
	public boolean hasBits(int bits)
	{
		return ((bitmap&bits)==bits);
	}
	public int getAccBitmap() {return accBitmap;}	
	public void setAccBitmap(int newBits) {accBitmap=newBits; CMLib.database().saveObject(this);}
	public void setAccBits(int bits, boolean set)
	{
		if(set)
			accBitmap|=bits;
		else
			accBitmap&=bits;
		CMLib.database().saveObject(this);
	}
	public boolean hasAccBits(int bits)
	{
		return ((accBitmap&bits)==bits);
	}
	
	public void destroy()	//should only REALLY be called by the MOB's destroy()
	{
		//TODO?
		amDestroyed=true;
		for(AccountStats M : (AccountStats[])friends.toArray(AccountStats.dummyASArray))
		{
			//PlayerStats pstats=M.playerStats();
			M.removeFriend(this);
			/*if(pstats.getAccount()==null)
				pstats.saveThis();
			else
				pstats.getAccount().saveThis(); */
		}
		friends.clear();
		/*for(AccountStats pstats : (AccountStats[])ignoredBy.toArray(AccountStats.dummyASArray))
		{
			pstats.removeIgnored(mob);
			//pstats.saveThis();
		}
		ignoredBy.clear();*/
		if(saveNum!=0)
			CMLib.database().deleteObject(this);
	}
	public boolean amDestroyed()
	{
		return amDestroyed;
	}

	//CMModifiable and CMSavable
	public SaveEnum[] totalEnumS(){return SCode.values();}
	public Enum[] headerEnumS(){return new Enum[] {SCode.values()[0]};}
	public ModEnum[] totalEnumM(){return MCode.values();}
	public Enum[] headerEnumM(){return new Enum[] {MCode.values()[0]};}
	public int saveNum()
	{
		if((saveNum==0)&&(!amDestroyed))
		synchronized(this)
		{
			if(saveNum==0)
				saveNum=SIDLib.ACCOUNTSTATS.getNumber(this);
		}
		return saveNum;
	}
	public void setSaveNum(int num)
	{
		synchronized(this)
		{
			if(saveNum!=0)
				SIDLib.ACCOUNTSTATS.removeNumber(saveNum);
			saveNum=num;
			SIDLib.ACCOUNTSTATS.assignNumber(num, this);
		}
	}
	public boolean needLink(){return true;}
	public void link()
	{
		if(friendsToAdd!=null)
		{
			for(int SID : friendsToAdd)
			{
				AccountStats friend = SIDLib.ACCOUNTSTATS.get(SID);
				if(friend!=null)
					friends.add(friend);
			}
			friendsToAdd=null;
		}
		if(ignoredToAdd!=null)
		{
			for(int SID : ignoredToAdd)
			{
				MOB ignoredM = SIDLib.CREATURE.get(SID);
				if((ignoredM!=null)&&(!ignoredM.isMonster()))
				{
					ignored.add(ignoredM);
					ignoredM.playerStats().addIgnoredBy(this);
				}
			}
			ignoredToAdd=null;
		}
		if(playersToAdd!=null)
		{
			for(int SID : playersToAdd)
			{
				MOB player= SIDLib.CREATURE.get(SID);
				if(player!=null)
					fullPlayers.add(player);
			}
			playersToAdd=null;
		}
		CMLib.players().queueAccount(this);
	}
	public void saveThis(){CMLib.database().saveObject(this);}
	public void prepDefault(){}

	private enum SCode implements CMSavable.SaveEnum{
		FRN(){
			public ByteBuffer save(DefaultPlayerAccount E){ return CMLib.coffeeMaker().savSaveNums((CMSavable[])E.friends.toArray(CMSavable.dummyCMSavableArray)); }
			public int size(){return 0;}
			public void load(DefaultPlayerAccount E, ByteBuffer S){ E.friendsToAdd=CMLib.coffeeMaker().loadAInt(S); } },
		IGN(){
			public ByteBuffer save(DefaultPlayerAccount E){ return CMLib.coffeeMaker().savSaveNums((CMSavable[])E.ignored.toArray(CMSavable.dummyCMSavableArray)); }
			public int size(){return 0;}
			public void load(DefaultPlayerAccount E, ByteBuffer S){ E.ignoredToAdd=CMLib.coffeeMaker().loadAInt(S); } },
		LIP(){
			public ByteBuffer save(DefaultPlayerAccount E){ return (E.lastIP==null?GenericBuilder.emptyBuffer:ByteBuffer.wrap(E.lastIP)); }
			public int size(){return 0;}	//Ideally I can just say 4 but... ipv6.
			public void load(DefaultPlayerAccount E, ByteBuffer S){ E.lastIP=CMLib.coffeeMaker().loadAByte(S); } },
		PSS(){
			public ByteBuffer save(DefaultPlayerAccount E){ return CMLib.coffeeMaker().savString(E.Password); }
			public int size(){return 0;}
			public void load(DefaultPlayerAccount E, ByteBuffer S){ E.Password=CMLib.coffeeMaker().loadString(S); } },
		BIT(){
			public ByteBuffer save(DefaultPlayerAccount E){ return (ByteBuffer)ByteBuffer.wrap(new byte[4]).putInt(E.bitmap).rewind(); }
			public int size(){return 4;}
			public void load(DefaultPlayerAccount E, ByteBuffer S){ E.bitmap=S.getInt(); } },
		ABT(){
			public ByteBuffer save(DefaultPlayerAccount E){ return (ByteBuffer)ByteBuffer.wrap(new byte[4]).putInt(E.accBitmap).rewind(); }
			public int size(){return 4;}
			public void load(DefaultPlayerAccount E, ByteBuffer S){ E.accBitmap=S.getInt(); } },
/*		FLG(){
			public ByteBuffer save(DefaultPlayerAccount E){ return CMLib.coffeeMaker().savAString((String[])E.acctFlags.toArray(new String[0])); }
			public int size(){return 0;}
			public void load(DefaultPlayerAccount E, ByteBuffer S){ for(String newF : CMLib.coffeeMaker().loadAString(S)) E.acctFlags.add(newF); } },
*/
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
					Vector<MOB> V=CMParms.denumerate(E.fullPlayers.iterator());
					int i=CMLib.genEd().promptVector(M, V, false);
					if(--i<0) done=true;
					else if(i<V.size()) {
						char action=M.session().prompt("(D)estroy or (M)odify "+V.get(i).name()+" (default M)? ","M").trim().toUpperCase().charAt(0);
						if(action=='D') {
							MOB mob = V.get(i);
							if(M.session().confirm("WARNING: This will PERMANENTLY DELETE the character "+mob.name()+". Are you sure?", "N")) {
								E.delPlayer(mob);
								mob.destroy(); } }
						else if(action=='M') CMLib.genEd().genMiscSet(M, V.get(i)); } } } },
/*		FLAGS(){
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
*/
		;
		public abstract String brief(DefaultPlayerAccount fromThis);
		public abstract String prompt(DefaultPlayerAccount fromThis);
		public abstract void mod(DefaultPlayerAccount toThis, MOB M);
		public String brief(CMModifiable fromThis){return brief((DefaultPlayerAccount)fromThis);}
		public String prompt(CMModifiable fromThis){return prompt((DefaultPlayerAccount)fromThis);}
		public void mod(CMModifiable toThis, MOB M){mod((DefaultPlayerAccount)toThis, M);} }
}
