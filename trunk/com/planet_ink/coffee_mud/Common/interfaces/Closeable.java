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
	public void setLocked(boolean bool);
	public void setLock(boolean bool);
	public void setClosed(boolean bool);
	public void setClosable(boolean bool);
	public void setObvious(boolean bool);
	public boolean locked();
	public boolean hasLock();
	public boolean closed();
	public boolean canClose();
	public boolean obviousLock();	//Key required for the lock will be found automatically
	public void setLidsNLocks(boolean newHasALid, boolean newIsOpen, boolean newHasALock, boolean newIsLocked, boolean newObvious);
	public void destroy();

	public static class O
	{
		public static Closeable getFrom(CMObject O)
		{
			if(O instanceof Closeable) return (Closeable)O;
			if(O instanceof CloseableHolder) return ((CloseableHolder)O).getLidObject();
			while((O instanceof Ownable)&&((Ownable)O).owner()!=O){
				O=((Ownable)O).owner();
				if(O instanceof Closeable) return (Closeable)O;
				if(O instanceof CloseableHolder) return ((CloseableHolder)O).getLidObject(); }
			return null;
		}
	}
}