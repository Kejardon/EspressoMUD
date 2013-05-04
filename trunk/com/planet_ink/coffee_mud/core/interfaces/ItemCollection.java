package com.planet_ink.coffee_mud.core.interfaces;
import com.planet_ink.coffee_mud.core.interfaces.*;
import com.planet_ink.coffee_mud.core.*;

import java.util.*;
import java.util.concurrent.ConcurrentLinkedQueue;
@SuppressWarnings("unchecked")
public interface ItemCollection extends CMObject, CMModifiable, CMSavable, CMCommon//, ListenHolder
{
	public static interface ItemHolder extends ListenHolder
	{
		public ItemCollection getItemCollection();
		public void setItemCollection(ItemCollection IC, boolean copyInto);
	}

	public boolean canHold(Item item);
	public boolean hasItem(Item item, boolean checkSubItems);
	public void addItem(Item item);
	public void copyFrom(ItemCollection other);
	public void removeItem(Item item);
	public void clearItems(boolean evict);
	public Item removeItem(int i);
	public Item getItem(int i);
	public int numItems();
	public Iterator<Item> allItems();
	public Item[] toArray();

	public static class O
	{
		public static ItemCollection getFrom(CMObject O)
		{
			if(O instanceof ItemCollection) return (ItemCollection)O;
			if(O instanceof ItemHolder) return ((ItemHolder)O).getItemCollection();
			while((O instanceof Ownable)&&((Ownable)O).owner()!=O){
				O=((Ownable)O).owner();
				if(O instanceof ItemCollection) return (ItemCollection)O;
				if(O instanceof ItemHolder) return ((ItemHolder)O).getItemCollection(); }
			return null;
		}
	}
}