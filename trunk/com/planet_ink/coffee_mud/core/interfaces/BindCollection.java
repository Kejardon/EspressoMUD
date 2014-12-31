package com.planet_ink.coffee_mud.core.interfaces;
import com.planet_ink.coffee_mud.core.interfaces.*;
import com.planet_ink.coffee_mud.core.*;

import java.util.*;

public interface BindCollection extends CMObject, CMModifiable, CMSavable, CMCommon//, ListenHolder
{
	@Override public BindCollection copyOf();
	@Override public BindCollection newInstance();
	public ArrayList<Bind> bindsTo(Item subItem);
	public Iterator<Bind> allBinds();
	public boolean hasBind(Bind bind, boolean checkSubItems);
	public void addBind(Bind bind);
	public void removeBind(Bind bind);
	public Bind removeBind(int i);
	public Bind getBind(int i);
	public int numBinds();

	public boolean hasItem(Item item, boolean checkSubItems);
	public void removeItem(Item item);
	public int numItems();
	public Item[] itemArray(); //Note: This is semi-unprotected. Calling methods should clone the itemArray if they want to modify it.
	public Iterator<Item> allItems();

	public void clearCache();

	public static class O
	{
		public static BindCollection getFrom(CMObject O)
		{
			if(O instanceof BindCollection) return (BindCollection)O;
			if((O instanceof Item) && ((Item)O).isComposite()) return ((Item)O).subItems();
			while((O instanceof Ownable)&&((Ownable)O).owner()!=O){
				O=((Ownable)O).owner();
				if(O instanceof BindCollection) return (BindCollection)O;
				if((O instanceof Item) && ((Item)O).isComposite()) return ((Item)O).subItems(); }
			return null;
		}
	}
}