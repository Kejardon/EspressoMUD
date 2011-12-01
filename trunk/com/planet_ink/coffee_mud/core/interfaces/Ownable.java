package com.planet_ink.coffee_mud.core.interfaces;
public interface Ownable extends CMObject
{
	public CMObject owner();
	public void setOwner(CMObject newOwner);
	
	public static class O
	{
		public static CMObject getOwnerFrom(CMObject obj)
		{
			while(obj instanceof Ownable) obj=((Ownable)obj).owner();
			return obj;
		}
	}
}