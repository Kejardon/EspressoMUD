package com.planet_ink.coffee_mud.Common.interfaces;
import com.planet_ink.coffee_mud.core.interfaces.*;
import com.planet_ink.coffee_mud.Items.interfaces.*;
import com.planet_ink.coffee_mud.core.*;
import com.planet_ink.coffee_mud.MOBS.interfaces.*;
import com.planet_ink.coffee_mud.Common.interfaces.*;

import java.util.*;
@SuppressWarnings("unchecked")
public interface ItemCollection extends CMObject, CMModifiable, CMSavable, CMCommon//, ListenHolder
{
	public static interface ItemHolder extends ListenHolder { public ItemCollection getItemCollection(); }

	public boolean canHold(Item item);
	public boolean hasItem(Item item, boolean checkSubItems);
	public void addItem(Item item);
	public void removeItem(Item item);
	public Item removeItem(int i);
	public Item getItem(int i);
	public int numItems();
	public Vector<Item> allItems();

	public static class O
	{
		public static ItemCollection getFrom(CMObject O)
		{
			if(O instanceof ItemCollection) return (ItemCollection)O;
			while((O instanceof Ownable)&&((Ownable)O).owner()!=O) O=((Ownable)O).owner();
			if(O instanceof ItemHolder) return ((ItemHolder)O).getItemCollection();
			return null;
		}
	}
}