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
		public static Closeable getFrom(CMObject O)
		{
			if(O instanceof Closeable) return (Closeable)O;
			while(O instanceof Ownable) O=((Ownable)O).owner();
			if(O instanceof CloseableHolder) return ((CloseableHolder)O).getLidObject();
			return null;
		}
	}
}