package com.planet_ink.coffee_mud.Common;
import com.planet_ink.coffee_mud.core.interfaces.*;
import com.planet_ink.coffee_mud.core.*;
import com.planet_ink.coffee_mud.Libraries.*;

import java.util.*;
import java.nio.ByteBuffer;
import java.util.concurrent.CopyOnWriteArrayList;
//import com.planet_ink.coffee_mud.MOBS.StdMOB;

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

	protected HashSet<String> securityGroups=new HashSet();
	protected HashSet<AccountStats> friends=new HashSet();
	protected HashSet<MOB> ignored=new HashSet();
	protected HashSet<MOB> introductions=new HashSet();
	protected HashSet<AccountStats> ignoredBy=new HashSet();
	protected LinkedList<String> tellStack=new LinkedList();
	//protected Vector<String> gtellStack=new Vector();
	protected HashMap<String, String> alias=new HashMap();
	protected byte[] lastIP=null;//"";
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
	protected MOB mob=null;

	protected int saveNum=0;
	protected boolean amDestroyed=false;
	protected int[] friendsToAdd=null;
	protected int[] ignoredToAdd=null;
	protected int[] introducedToAdd=null;
	protected int accountToLink=0;

//	protected RoomnumberSet visitedRoomSet=null;
	
	protected int bitmap=0;

	public DefaultPlayerStats() {
		super();
	}
	
/*	public boolean sameAs(PlayerStats E)
	{
		if(!(E instanceof DefaultPlayerStats)) return false;
		return true;
	} */
	
	public MOB mob(){return mob;}
	public void setMOB(MOB m){mob=m;}
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
			synchronized(securityGroups) {O.securityGroups=(HashSet)securityGroups.clone();}
			synchronized(friends) {O.friends=(HashSet)friends.clone();}
			synchronized(ignored) {O.ignored=(HashSet)ignored.clone();}
			synchronized(ignoredBy) {O.ignoredBy=(HashSet)ignoredBy.clone();}
			synchronized(introductions) {O.introductions=(HashSet)introductions.clone();}
			synchronized(tellStack) {O.tellStack=(LinkedList)tellStack.clone(); }
			//O.gtellStack=(Vector)gtellStack.clone();
			synchronized(alias) {O.alias=(HashMap)alias.clone();}
			return O;
		}
		catch(CloneNotSupportedException e)
		{
			return new DefaultPlayerStats();
		}
	}
	public byte[] lastIP(){return lastIP;}
	public void setLastIP(byte[] ip)
	{
		lastIP=ip;
		if(account != null)
			account.setLastIP(ip);
	}
	public long lastUpdated(){return lastUpdated;}
	public void setLastUpdated(long time)
	{
		lastUpdated=time;
		CMLib.database().saveObject(this);
		if(account != null)
		{
			account.setLastUpdated(time);
			//account.saveThis();
		}
	}
	public long lastDateTime(){return LastDateTime;}
	public void setLastDateTime(long C)
	{ 
		LastDateTime=C;
		CMLib.database().saveObject(this);
		if(account != null)
		{
			account.setLastDateTime(C);
			//account.saveThis();
		}
	}
	public String password(){return (account!=null)?account.password():Password;}
	public void setPassword(String newPassword)
	{
		Password=newPassword;
		CMLib.database().saveObject(this);
		if(account != null)
		{
			account.setPassword(newPassword);
			//account.saveThis();
		}
	}
	
	public int getWrap(){return wrap;}
	public void setWrap(int newWrap){wrap=newWrap; CMLib.database().saveObject(this);}
	public int getPageBreak(){return pageBreak;}
	public void setPageBreak(int newBreak){pageBreak=newBreak; CMLib.database().saveObject(this);}
	public void setChannelMask(int newMask){ channelMask=newMask; CMLib.database().saveObject(this);}
	public int getChannelMask(){ return channelMask;}
	public MOB replyTo(){	return replyTo;	}
	public int replyType(){	return replyType;}
	public long replyTime(){	return replyTime;	}
	public void setReplyTo(MOB mob, int replyType)
	{	
		replyTo=mob;
		this.replyType=replyType;
	}
	public void setPrompt(String newPrompt){prompt=newPrompt; CMLib.database().saveObject(this);}
	public String getColorStr(){return colorStr;}
	public void setColorStr(String newColors){colorStr=newColors; CMLib.database().saveObject(this);}
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

	public boolean isIntroducedTo(MOB mob){return introductions.contains(mob);}
	public void introduceTo(MOB mob)
	{
		if(mob!=null)
		{
			boolean result=false;
			synchronized(introductions){ result=introductions.add(mob);}
			if(result)
				CMLib.database().saveObject(this);
		}
	}
	protected MOB[] getIntroduced() { synchronized(introductions){return (MOB[])introductions.toArray(MOB.dummyMOBArray);}}

	public void addTellStack(String msg)
	{
		synchronized(tellStack)
		{
			if(tellStack.size()>TELL_STACK_MAX_SIZE)
				tellStack.removeFirst();
			tellStack.add(msg);
		}
	}
	
	public LinkedList<String> getTellStack()
	{
		synchronized(tellStack)
		{ return (LinkedList<String>)tellStack.clone(); }
	}
	/*public void addGTellStack(String msg)
	{
		if(gtellStack.size()>GTELL_STACK_MAX_SIZE)
			gtellStack.removeElementAt(0);
		gtellStack.addElement(msg);
	}
	public Vector<String> getGTellStack()
	{
		return (Vector)gtellStack.clone();
	}*/

	public boolean hasFriend(AccountStats M){
		if((account!=null)&&(account.hasFriend(M))) return true;
		return ((friends.contains(M))||((M instanceof PlayerStats)&&(friends.contains(((PlayerStats)M).getAccount())))); }
	public boolean addFriend(AccountStats M){
		if(account==null){
			boolean result=false;
			synchronized(friends) { result=friends.add(M);}
			if(result) CMLib.database().saveObject(this);
			return result; }
		return account.addFriend(M); }
	public boolean addFriend(MOB M){
		PlayerStats friendStats=M.playerStats();
		PlayerAccount friendAcct=friendStats.getAccount();
		if(friendAcct!=null) return addFriend(friendAcct);
		return addFriend(friendStats);}
	public boolean removeFriend(AccountStats M){
		boolean result=false;
		if(account!=null) result=account.removeFriend(M);
		synchronized(friends) { result|=friends.remove(M); }
		if(result) CMLib.database().saveObject(this);
		return result; }
	public AccountStats removeFriend(String M){
		if(account!=null){
			AccountStats result=account.removeFriend(M);
			if(result!=null) return result;}
		synchronized(friends) {
			for(AccountStats oldFriend : friends)
			if(((oldFriend instanceof PlayerStats)&&(((PlayerStats)oldFriend).mob().name().equals(M)))
			  ||(((PlayerAccount)oldFriend).accountName().equalsIgnoreCase(M))) {
				friends.remove(oldFriend);
				CMLib.database().saveObject(this);
				return oldFriend; }}
		return null; }
	public boolean hasIgnored(MOB M){
		if((account!=null)&&(account.hasIgnored(M))) return true;
		return ignored.contains(M);}
	public boolean addIgnored(MOB M){
		if(account==null){
			boolean result=false;
			synchronized(ignored) { result=ignored.add(M); }
			if(result) CMLib.database().saveObject(this);
			return result;}
		return account.addIgnored(M);}
	public boolean removeIgnored(MOB M){
		boolean result=false;
		if(account!=null) result=account.removeIgnored(M);
		synchronized(ignored) { result|=ignored.remove(M); }
		if(result) CMLib.database().saveObject(this);
		return result;}

	public AccountStats[] getFriends()
	{
		if(account != null)
			return account.getFriends();
		synchronized(friends) {return (AccountStats[])friends.toArray(AccountStats.dummyASArray);}
	}
	protected AccountStats[] getOwnFriends(){synchronized(friends) {return (AccountStats[])friends.toArray(AccountStats.dummyASArray);}}
	public MOB[] getIgnored()
	{
		if(account != null)
			return account.getIgnored();
		synchronized(ignored) {return (MOB[])ignored.toArray(MOB.dummyMOBArray);}
	}
	public MOB[] getOwnIgnored() {synchronized(ignored) {return (MOB[])ignored.toArray(MOB.dummyMOBArray);}}
	/*public HashSet<AccountStats> getIgnoredBy()
	{
		if(account != null)
			return account.getIgnoredBy();
		return ignoredBy;
	} */
	public boolean addIgnoredBy(AccountStats M){synchronized(ignoredBy){return ignoredBy.add(M);}}
	public boolean removeIgnoredBy(AccountStats M){synchronized(ignoredBy){return ignoredBy.remove(M);}}
	public AccountStats[] getIgnoredBy(){synchronized(ignoredBy){return (AccountStats[])ignoredBy.toArray(AccountStats.dummyASArray);}}
	
	public String[] getAliasNames(){ synchronized(alias){return (String[])alias.keySet().toArray(CMClass.dummyStringArray); }}
	protected String[] getAliasValues(){ synchronized(alias){return (String[])alias.values().toArray(CMClass.dummyStringArray); }}
	//TODO: Should this return "" or null?
	public String getAlias(String named)
	{
		String S=alias.get(named);
		if(S==null) return "";
		return S;
	}
	public void delAliasName(String named) { synchronized(alias){alias.remove(named);} CMLib.database().saveObject(this);}
	public void setAlias(String named, String value) { synchronized(alias){alias.put(named, value);} CMLib.database().saveObject(this);}

	public HashSet<String> getSecurityGroups(){	return securityGroups;}
	
	public int getBitmap() {return bitmap;}	
	public void setBitmap(int newBits) {bitmap=newBits; CMLib.database().saveObject(this);}
	public void setBits(int bits, boolean set)
	{
		if(set)
			bitmap|=bits;
		else
			bitmap&=~bits;
		CMLib.database().saveObject(this);
	}
	public boolean hasBits(int bits)
	{
		return ((bitmap&bits)==bits);
	}
	
	public PlayerAccount getAccount() { return account;}
	public void setAccount(PlayerAccount account) { this.account = account; CMLib.database().saveObject(this);}

	public void destroy()	//should only REALLY be called by the MOB's destroy()
	{
		//TODO?
		amDestroyed=true;
		for(AccountStats nextMOB : getOwnFriends())
		{
			nextMOB.removeFriend(this);
			/*PlayerStats pstats=nextMOB.playerStats();
			pstats.getFriends().remove(mob);
			/*if(pstats.getAccount()==null)
				pstats.saveThis();
			else
				pstats.getAccount().saveThis();*/
		}
		friends.clear();
		for(AccountStats pstats : getIgnoredBy())
		{
			pstats.removeIgnored(mob);
			//pstats.saveThis();
		}
		ignoredBy.clear();
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
		if(introducedToAdd!=null)
		{
			for(int SID : introducedToAdd)
			{
				MOB player=SIDLib.CREATURE.get(SID);
				if(player!=null)
					introductions.add(player);
			}
			introducedToAdd=null;
		}
		if(accountToLink!=0)
		{
			AccountStats O=SIDLib.ACCOUNTSTATS.get(accountToLink);
			if(O instanceof PlayerAccount)
				account=(PlayerAccount)O;
			accountToLink=0;
		}
	}
	public void saveThis(){CMLib.database().saveObject(this);}
	public void prepDefault(){}

	private enum SCode implements SaveEnum<DefaultPlayerStats>{
		FRN(){
			public ByteBuffer save(DefaultPlayerStats E){ return CMLib.coffeeMaker().savSaveNums((CMSavable[])E.getOwnFriends()); }
			public int size(){return 0;}
			public void load(DefaultPlayerStats E, ByteBuffer S){ E.friendsToAdd=CMLib.coffeeMaker().loadAInt(S); } },
		IGN(){
			public ByteBuffer save(DefaultPlayerStats E){ return CMLib.coffeeMaker().savSaveNums((CMSavable[])E.getOwnIgnored()); }
			public int size(){return 0;}
			public void load(DefaultPlayerStats E, ByteBuffer S){ E.ignoredToAdd=CMLib.coffeeMaker().loadAInt(S); } },
		ALS(){
			public ByteBuffer save(DefaultPlayerStats E){ return CMLib.coffeeMaker().savAAString(E.getAliasNames(), E.getAliasValues()); }
			public int size(){return 0;}
			public void load(DefaultPlayerStats E, ByteBuffer S){
				String[] vals=CMLib.coffeeMaker().loadAString(S);
				int div=vals.length/2;
				for(int i=div-1; i>=0;i--)
					E.setAlias(vals[i], vals[i+div]); } },
		LIP(){
			public ByteBuffer save(DefaultPlayerStats E){ return (E.lastIP==null?CoffeeMaker.emptyBuffer:ByteBuffer.wrap(E.lastIP)); }
			public int size(){return 0;}	//Ideally I can just say 4 but... ipv6.
			public void load(DefaultPlayerStats E, ByteBuffer S){ E.lastIP=CMLib.coffeeMaker().loadAByte(S); } },
		CHN(){
			public ByteBuffer save(DefaultPlayerStats E){ return (ByteBuffer)ByteBuffer.wrap(new byte[4]).putInt(E.channelMask).rewind(); }
			public int size(){return 4;}
			public void load(DefaultPlayerStats E, ByteBuffer S){ E.channelMask=S.getInt(); } },
		PSS(){
			public ByteBuffer save(DefaultPlayerStats E){ return CMLib.coffeeMaker().savString(E.Password); }
			public int size(){return 0;}
			public void load(DefaultPlayerStats E, ByteBuffer S){ E.Password=CMLib.coffeeMaker().loadString(S); } },
		COL(){
			public ByteBuffer save(DefaultPlayerStats E){ return CMLib.coffeeMaker().savString(E.colorStr); }
			public int size(){return 0;}
			public void load(DefaultPlayerStats E, ByteBuffer S){ E.colorStr=CMLib.coffeeMaker().loadString(S); } },
		PMP(){
			public ByteBuffer save(DefaultPlayerStats E){ return CMLib.coffeeMaker().savString(E.prompt); }
			public int size(){return 0;}
			public void load(DefaultPlayerStats E, ByteBuffer S){ E.prompt=CMLib.coffeeMaker().loadString(S); } },
		ACT(){
			public ByteBuffer save(DefaultPlayerStats E){
				if(E.account==null) return ByteBuffer.wrap(new byte[4]);
				return (ByteBuffer)ByteBuffer.wrap(new byte[4]).putInt(E.account.saveNum()).rewind(); }
			public int size(){return 4;}
			public void load(DefaultPlayerStats E, ByteBuffer S){ E.accountToLink=S.getInt(); } },
		WRP(){
			public ByteBuffer save(DefaultPlayerStats E){ return (ByteBuffer)ByteBuffer.wrap(new byte[4]).putInt(E.wrap).rewind(); }
			public int size(){return 4;}
			public void load(DefaultPlayerStats E, ByteBuffer S){ E.wrap=S.getInt(); } },
		BRK(){
			public ByteBuffer save(DefaultPlayerStats E){ return (ByteBuffer)ByteBuffer.wrap(new byte[4]).putInt(E.pageBreak).rewind(); }
			public int size(){return 4;}
			public void load(DefaultPlayerStats E, ByteBuffer S){ E.pageBreak=S.getInt(); } },
		INT(){
			public ByteBuffer save(DefaultPlayerStats E){ return CMLib.coffeeMaker().savSaveNums((CMSavable[])E.getIntroduced()); }
			public int size(){return 0;}
			public void load(DefaultPlayerStats E, ByteBuffer S){ E.introducedToAdd=CMLib.coffeeMaker().loadAInt(S); } },
		BIT(){
			public ByteBuffer save(DefaultPlayerStats E){ return (ByteBuffer)ByteBuffer.wrap(new byte[4]).putInt(E.bitmap).rewind(); }
			public int size(){return 4;}
			public void load(DefaultPlayerStats E, ByteBuffer S){ E.bitmap=S.getInt(); } },
		;
		public CMSavable subObject(DefaultPlayerStats fromThis){return null;} }
	private enum MCode implements ModEnum<DefaultPlayerStats>{
		ACCOUNT(){
			public String brief(DefaultPlayerStats E){if(E.account==null) return ""; return E.account.accountName();}
			public String prompt(DefaultPlayerStats E){if(E.account==null) return ""; return E.account.accountName();}
			public void mod(DefaultPlayerStats E, MOB M){
				String S=CMLib.genEd().stringPrompt(M, "", true);
				if(S==null) E.account=null;
				PlayerAccount acc=CMLib.players().getAccount(S);
				if(acc!=null) E.account=acc; } },
		SECURITY(){
			public String brief(DefaultPlayerStats E){return ""+E.securityGroups.size();}
			public String prompt(DefaultPlayerStats E){synchronized(E.securityGroups) {return E.securityGroups.toString();}}
			public void mod(DefaultPlayerStats E, MOB M){
				boolean done=false;
				while((M.session()!=null)&&(!M.session().killFlag())&&(!done)) {
					Vector<String> V;
					synchronized(E.securityGroups) {V=new Vector(E.securityGroups);}
					int i=CMLib.genEd().promptVector(M, V, true);
					if(--i<0) done=true;
					else if(i==V.size()) {
						String S=CMLib.genEd().stringPrompt(M, "", false).trim().toUpperCase();
						if(S.length()>0) synchronized(E.securityGroups) {E.securityGroups.add(S); } }
					else if(i<V.size()) synchronized(E.securityGroups) {E.securityGroups.remove(V.get(i)); } } } },
		INTRODUCED(){
			public String brief(DefaultPlayerStats E){return ""+E.introductions.size();}
			public String prompt(DefaultPlayerStats E){return ""+E.getIntroduced();}
			public void mod(DefaultPlayerStats E, MOB M){
				boolean done=false;
				while((M.session()!=null)&&(!M.session().killFlag())&&(!done)) {
					String S=CMStrings.capitalizeAndLower(CMLib.genEd().stringPrompt(M, "", false));
					if(S.length()>0) {
						MOB newI=CMLib.players().getPlayer(S);
						if(E.introductions.contains(newI)&&(M.session().confirm("Remove this name?", "N"))) synchronized(E.introductions){E.introductions.remove(newI);}
						else if(M.session().confirm("Add this name?", "N")) synchronized(E.introductions){E.introductions.add(newI);} }
					else done=true; } } },
		; }

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
