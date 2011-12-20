package com.planet_ink.coffee_mud.Common.interfaces;
import com.planet_ink.coffee_mud.core.interfaces.*;
import com.planet_ink.coffee_mud.core.*;
import com.planet_ink.coffee_mud.MOBS.interfaces.*;

import java.util.*;
import java.io.*;

@SuppressWarnings("unchecked")
public interface Closeable extends CMObject, CMModifiable, CMSavable, CMCommon
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

	public static class O
	{
		private static int saveNumber=1;
		private static boolean started=false;
		private static HashMap<Integer, Closeable> assignedNumbers=new HashMap<Integer, Closeable>();
		public synchronized static int getNumber()
		{
			if(!started)
			{
				String S=CMLib.database().DBReadData("CloseSNum");
				if(S==null)
				{
					saveNumber=1;
					CMLib.database().DBCreateData("CloseSNum","1");
				}
				else
					saveNumber=CMath.s_int(S);
				assignedNumber.put(0,null);
				started=true;
			}
			if(assignedNumbers.containsKey(saveNumber))
			{
				int inc=1;
				while(assignedNumbers.containsKey(saveNumber+inc))
				{
					inc=inc*2;
					if(inc==1) saveNumber+=1580030169; //(2^32)/e ; optimal interval for poking around randomly
				}
				saveNumber+=inc;
			}
			return saveNumber++;
		}
		public static void save()
		{
			CMLib.database().DBUpdateData("CloseSNum",""+saveNumber);
		}
		public static void assignNumber(int i, Closeable A)
		{
			assignedNumbers.put(i, A);
		}
		public static void removeNumber(int i, Closeable A)
		{
			assignedNumbers.remove(i);
		}
		public static Closeable get(Integer i)
		{
			Closeable A=assignedNumbers.get(i);
			if(A==null)
			{
				A=(Closeable)CMLib.database().DBReadCMSaveData("CMCLOS", i.intValue(), CMClass.Objects.CLOSEABLE);
			}
			return ;
		}
		public static Closeable getFrom(CMObject O)
		{
			if(O instanceof Closeable) return (Closeable)O;
			while(O instanceof Ownable) O=((Ownable)O).owner();
			if(O instanceof CloseableHolder) return ((CloseableHolder)O).getLidObject();
			return null;
		}
	}
}