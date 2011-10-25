package com.planet_ink.coffee_mud.core.interfaces;
import com.planet_ink.coffee_mud.core.*;
import com.planet_ink.coffee_mud.MOBS.interfaces.*;

import java.util.*;
import java.io.*;

@SuppressWarnings("unchecked")
public interface Closeable extends CMObject, CMModifiable, CMSavable
{
	public static interface CloseableHolder extends CMObject { public Closeable getLidObject(); }

	public String keyName();
	public void setKeyName(String keyName);
	public boolean isLocked();
	public boolean hasALock();
	public boolean isOpen();
	public boolean hasALid();
	public boolean obviousLock();	//Key required for the lock will be found automatically
	public void setLidsNLocks(boolean newHasALid, boolean newIsOpen, boolean newHasALock, boolean newIsLocked, boolean newObvious);
	public void destroy();

	public enum SCode implements CMSavable.SaveEnum{
		KEY(){
			public String save(DefaultLid E){ return E.key; }
			public void load(DefaultLid E, String S){E.key=S.intern(); } },
		LCK(){
			public String save(DefaultLid E){ return ""+E.haslock; }
			public void load(DefaultLid E, String S){E.haslock=Boolean.getBoolean(S); } },
		OPN(){
			public String save(DefaultLid E){ return ""+E.open; }
			public void load(DefaultLid E, String S){E.open=Boolean.getBoolean(S); } },
		LID(){
			public String save(DefaultLid E){ return ""+E.closeable; }
			public void load(DefaultLid E, String S){E.closeable=Boolean.getBoolean(S); } },
		LKD(){
			public String save(DefaultLid E){ return ""+E.locked; }
			public void load(DefaultLid E, String S){E.locked=Boolean.getBoolean(S); } },
		OBV(){
			public String save(DefaultLid E){ return ""+E.obvious; }
			public void load(DefaultLid E, String S){E.obvious=Boolean.getBoolean(S); } }
		;
		public abstract String save(DefaultLid E);
		public abstract void load(DefaultLid E, String S);
		public String save(CMSavable E){return save((DefaultLid)E);}
		public void load(CMSavable E, String S){load((DefaultLid)E, S);} }
	public enum MCode implements CMModifiable.ModEnum{
		KEYNAME(){
			public String brief(DefaultLid E){return E.key;}
			public String prompt(DefaultLid E){return E.key;}
			public void mod(DefaultLid E, MOB M){E.key=CMLib.genEd().stringPrompt(M, ""+E.key, false);} },
		LOCKED(){
			public String brief(DefaultLid E){return ""+E.locked;}
			public String prompt(DefaultLid E){return ""+E.locked;}
			public void mod(DefaultLid E, MOB M){E.locked=CMLib.genEd().booleanPrompt(M, ""+E.locked);} },
		OPEN(){
			public String brief(DefaultLid E){return ""+E.open;}
			public String prompt(DefaultLid E){return ""+E.open;}
			public void mod(DefaultLid E, MOB M){E.open=CMLib.genEd().booleanPrompt(M, ""+E.open);} },
		CLOSEABLE(){
			public String brief(DefaultLid E){return ""+E.closeable;}
			public String prompt(DefaultLid E){return ""+E.closeable;}
			public void mod(DefaultLid E, MOB M){E.closeable=CMLib.genEd().booleanPrompt(M, ""+E.closeable);} },
		HASLOCK(){
			public String brief(DefaultLid E){return ""+E.haslock;}
			public String prompt(DefaultLid E){return ""+E.haslock;}
			public void mod(DefaultLid E, MOB M){E.haslock=CMLib.genEd().booleanPrompt(M, ""+E.haslock);} },
		OBVIOUSKEY() {
			public String brief(DefaultLid E){return ""+E.obvious;}
			public String prompt(DefaultLid E){return ""+E.obvious;}
			public void mod(DefaultLid E, MOB M){E.obvious=CMLib.genEd().booleanPrompt(M, ""+E.obvious);} }
		;
		public abstract String brief(DefaultLid fromThis);
		public abstract String prompt(DefaultLid fromThis);
		public abstract void mod(DefaultLid toThis, MOB M);
		public String brief(CMModifiable fromThis){return brief((DefaultLid)fromThis);}
		public String prompt(CMModifiable fromThis){return prompt((DefaultLid)fromThis);}
		public void mod(CMModifiable toThis, MOB M){mod((DefaultLid)toThis, M);} }
	public static class DefaultLid implements Closeable, Ownable
	{
		public static Closeable getFrom(CMObject O)
		{
			if(O instanceof Closeable) return (Closeable)O;
			while(O instanceof Ownable) O=((Ownable)O).getOwner();
			if(O instanceof CloseableHolder) return ((CloseableHolder)O).getLidObject();
			return null;
		}

		protected String key="skeleton";
		protected boolean locked=false;
		protected boolean haslock=false;
		protected boolean open=false;
		protected boolean closeable=true;
		protected boolean obvious=true;
		protected CMObject parent;

		public DefaultLid(CMObject O){parent=O;}

		//Ownable
		public CMObject getOwner(){return parent;}

		//CMObject. Not meant for use, get the owner!
		public String ID(){return "DefaultLid";}
		public CMObject newInstance(){return null;}
		public CMObject copyOf(){return null;}
		public void initializeClass(){}
		public int compareTo(CMObject O){return 0;}

		//CMModifiable and CMSavable
		public SaveEnum[] totalEnumS(){return SCode.values();}
		public Enum[] headerEnumS(){return new Enum[] {SCode.values()[0]} ;}
		public ModEnum[] totalEnumM(){return MCode.values();}
		public Enum[] headerEnumM(){return new Enum[] {MCode.values()[0]};}

		//Closeable
		public String keyName(){return key;}
		public void setKeyName(String keyName){key=keyName;}
		public boolean isLocked(){return locked;}
		public boolean hasALock(){return haslock;}
		public boolean isOpen(){return open;}
		public boolean hasALid(){return closeable;}
		public boolean obviousLock(){return obvious;}
		public void setLidsNLocks(boolean newHasALid, boolean newIsOpen, boolean newHasALock, boolean newIsLocked, boolean newObvious)
		{
			closeable=newHasALid;
			open=newIsOpen;
			haslock=newHasALock;
			locked=newIsLocked;
			obvious=newObvious;
		}
		public void destroy(){open=true; locked=false; closeable=false; key="";}

	}
}