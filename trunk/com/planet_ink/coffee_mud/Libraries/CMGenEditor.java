package com.planet_ink.coffee_mud.Libraries;
import com.planet_ink.coffee_mud.core.interfaces.*;
import com.planet_ink.coffee_mud.core.exceptions.*;
import com.planet_ink.coffee_mud.core.*;
import com.planet_ink.coffee_mud.Libraries.interfaces.*;
import com.planet_ink.coffee_mud.Libraries.interfaces.MoneyLibrary.MoneyDenomination;
import com.planet_ink.coffee_mud.Effects.interfaces.*;
import com.planet_ink.coffee_mud.Areas.interfaces.*;
import com.planet_ink.coffee_mud.Behaviors.interfaces.*;
import com.planet_ink.coffee_mud.Commands.interfaces.*;
import com.planet_ink.coffee_mud.Common.interfaces.*;
import com.planet_ink.coffee_mud.Exits.interfaces.*;
import com.planet_ink.coffee_mud.Items.interfaces.*;
import com.planet_ink.coffee_mud.Locales.interfaces.*;
import com.planet_ink.coffee_mud.MOBS.interfaces.*;
import com.planet_ink.coffee_mud.Races.interfaces.*;

import java.io.IOException;
import java.util.*;
import java.util.regex.*;

/*
CoffeeMUD 5.6.2 copyright 2000-2010 Bo Zimmerman
EspressoMUD copyright 2011 Kejardon

Licensed under the Apache License, Version 2.0. You may obtain a copy of the license at
	http://www.apache.org/licenses/LICENSE-2.0
*/
@SuppressWarnings("unchecked")
public class CMGenEditor extends StdLibrary implements GenericEditor
{
	public String ID(){return "CMGenEditor";}

	public CMObject genMiscSet(MOB mob, CMModifiable E)
	{
		if(E==null)
			return null;
		boolean done=false;
		CMModifiable.ModEnum[] options=E.totalEnumM();
		while((mob.session()!=null)&&(!mob.session().killFlag())&&(!done))
		{
			for(int i=0;i<options.length;i++)
				mob.session().rawPrintln((1+i)+". "+options[i]+": '"+options[i].brief(E)+"'.");
			
			int pickOption=CMath.s_int(mob.session().prompt("Edit which? ",""));
			if(--pickOption<0) done=true;
			else if(pickOption<options.length)
			{
				mob.session().rawPrintln((1+pickOption)+". "+options[pickOption]+": '"+options[pickOption].prompt(E)+"'.");
				options[pickOption].mod(E, mob);
			}
		}
		return E;
	}
	
	public void ashortPrompt(MOB mob, short[] values)
	{
		boolean done=false;
		while((mob.session()!=null)&&(!mob.session().killFlag())&&(!done))
		{
			for(int i=0;i<values.length;i++)
				mob.session().rawPrintln((1+i)+": "+values[i]);
			int pickOption=CMath.s_int(mob.session().prompt("Edit which? ",""));
			if(--pickOption<0) done=true;
			else if(pickOption<values.length)
				values[pickOption]=shortPrompt(mob, String.valueOf(values[pickOption]));
		}
	}
	public short shortPrompt(MOB mob, String defaultTo)
	{
		String newName=mob.session().prompt("Enter a new short: ",((defaultTo==null)?"":defaultTo));
		if(CMath.isInteger(newName))
			return CMath.s_short(newName);
		return CMath.s_short(defaultTo);
	}
	public void aintPrompt(MOB mob, int[] values)
	{
		boolean done=false;
		while((mob.session()!=null)&&(!mob.session().killFlag())&&(!done))
		{
			for(int i=0;i<values.length;i++)
				mob.session().rawPrintln((1+i)+": "+values[i]);
			int pickOption=CMath.s_int(mob.session().prompt("Edit which? ",""));
			if(--pickOption<0) done=true;
			else if(pickOption<values.length)
				values[pickOption]=intPrompt(mob, String.valueOf(values[pickOption]));
		}
	}
	public int intPrompt(MOB mob, String defaultTo)
	{
		String newName=mob.session().prompt("Enter a new integer: ",((defaultTo==null)?"":defaultTo));
		if(CMath.isInteger(newName))
			return CMath.s_int(newName);
		return CMath.s_int(defaultTo);
	}
	public void alongPrompt(MOB mob, long[] values)
	{
		boolean done=false;
		while((mob.session()!=null)&&(!mob.session().killFlag())&&(!done))
		{
			for(int i=0;i<values.length;i++)
				mob.session().rawPrintln((1+i)+": "+values[i]);
			int pickOption=CMath.s_int(mob.session().prompt("Edit which? ",""));
			if(--pickOption<0) done=true;
			else if(pickOption<values.length)
				values[pickOption]=longPrompt(mob, String.valueOf(values[pickOption]));
		}
	}
	public long longPrompt(MOB mob, String defaultTo)
	{
		String newName=mob.session().prompt("Enter a new long: ",((defaultTo==null)?"":defaultTo));
		if(CMath.isLong(newName))
			return CMath.s_long(newName);
		return CMath.s_long(defaultTo);
	}
	public void adoublePrompt(MOB mob, double[] values)
	{
		boolean done=false;
		while((mob.session()!=null)&&(!mob.session().killFlag())&&(!done))
		{
			for(int i=0;i<values.length;i++)
				mob.session().rawPrintln((1+i)+": "+values[i]);
			int pickOption=CMath.s_int(mob.session().prompt("Edit which? ",""));
			if(--pickOption<0) done=true;
			else if(pickOption<values.length)
				values[pickOption]=doublePrompt(mob, String.valueOf(values[pickOption]));
		}
	}
	public double doublePrompt(MOB mob, String defaultTo)
	{
		String newName=mob.session().prompt("Enter a new double: ",((defaultTo==null)?"":defaultTo));
		if(CMath.isDouble(newName))
			return CMath.s_double(newName);
		return CMath.s_double(defaultTo);
	}
	public void abooleanPrompt(MOB mob, boolean[] values)
	{
		boolean done=false;
		while((mob.session()!=null)&&(!mob.session().killFlag())&&(!done))
		{
			for(int i=0;i<values.length;i++)
				mob.session().rawPrintln((1+i)+": "+values[i]);
			int pickOption=CMath.s_int(mob.session().prompt("Edit which? ",""));
			if(--pickOption<0) done=true;
			else if(pickOption<values.length)
				values[pickOption]=booleanPrompt(mob, String.valueOf(values[pickOption]));
		}
	}
	public boolean booleanPrompt(MOB mob, String defaultTo)
	{
		String newName=mob.session().prompt("Enter a new boolean: ",((defaultTo==null)?"":defaultTo));
		if(CMath.isBool(newName))
			return CMath.s_bool(newName);
		return CMath.s_bool(defaultTo);
	}
	public void astringPrompt(MOB mob, String[] values, boolean allowNull)
	{
		boolean done=false;
		while((mob.session()!=null)&&(!mob.session().killFlag())&&(!done))
		{
			for(int i=0;i<values.length;i++)
				mob.session().rawPrintln((1+i)+": "+values[i]);
			int pickOption=CMath.s_int(mob.session().prompt("Edit which? ",""));
			if(--pickOption<0) done=true;
			else if(pickOption<values.length)
				values[pickOption]=stringPrompt(mob, values[pickOption], allowNull);
		}
	}
	public String stringPrompt(MOB mob, String defaultTo, boolean allowNull)
	{
		String newString=mob.session().prompt("Enter a new string: ",((defaultTo==null)?"":defaultTo));
		if("NULL".equalsIgnoreCase(newString))
		{
			if(allowNull) return null;
			else return "";
		}
		return newString;
	}
	//IMPORTANT: ALL CALLS TO THIS MUST HAVE UPPERCASE ENUM OPTIONS.
	public Enum enumPrompt(MOB M, String defaultTo, Enum[] options)
	{
		M.session().rawPrintln("Options: ");
		for(Enum E : options)
			M.session().rawPrint(E.toString());
		
		String newString=M.session().prompt("Enter a new value: ",((defaultTo==null)?"":defaultTo));
		Enum E=CMClass.valueOf(options[0].getClass(), newString);
		if((E==null)&&(defaultTo!=null))
			E=CMClass.valueOf(options[0].getClass(), defaultTo);
		return E;
	}
	public void acharPrompt(MOB mob, char[] values)
	{
		boolean done=false;
		while((mob.session()!=null)&&(!mob.session().killFlag())&&(!done))
		{
			for(int i=0;i<values.length;i++)
				mob.session().rawPrintln((1+i)+": "+values[i]);
			int pickOption=CMath.s_int(mob.session().prompt("Edit which? ",""));
			if(--pickOption<0) done=true;
			else if(pickOption<values.length)
				values[pickOption]=charPrompt(mob, String.valueOf(values[pickOption]));
		}
	}
	public char charPrompt(MOB mob, String defaultTo)
	{
		return mob.session().prompt("Enter a new character: ",((defaultTo==null)?"":defaultTo)).charAt(0);
	}

	public int promptVector(MOB mob, Vector V, boolean newOption)
	{
		int i=0;
		for(;i<V.size();i++)
		{
			Object O=V.get(i);
			boolean str=(O instanceof String);
			boolean inter=(O instanceof Interactable);
			mob.session().rawPrintln((1+i)+". "+(str?(String)O:(((CMObject)O).ID()+(inter?" "+((Interactable)O).name():""))));
		}
		if(newOption)
			mob.session().rawPrintln((1+i)+". New Element");
		return CMath.s_int(mob.session().prompt("Edit which? ",""));
	}
	public int promptWVector(MOB mob, WVector V, boolean newOption)
	{
		int i=0;
		for(;i<V.size();i++)
		{
			Object O=V.get(i);
			boolean str=(O instanceof String);
			boolean inter=(O instanceof Interactable);
			mob.session().rawPrintln((1+i)+". ("+V.weight(i)+") "+(str?(((CMObject)O).ID()+(inter?" "+((Interactable)O).name():"")):(String)O));
		}
		if(newOption)
			mob.session().rawPrintln((1+i)+". New Element");
		return CMath.s_int(mob.session().prompt("Edit which? ",""));
	}
	public Effect newAnyEffect(MOB mob){return newAnyEffect(mob, new Vector());}
	public Effect newAnyEffect(MOB mob, Vector notThese)
	{
		while((mob.session()!=null)&&(!mob.session().killFlag()))
		{
			String behave=mob.session().prompt("Enter an effect to add (?)\n\r:","");
			if(behave.length()>0)
			{
				if(behave.equalsIgnoreCase("?"))
					mob.tell(CMLib.lister().reallyList(CMClass.Objects.EFFECT.all()).toString());
				else
				{
					Effect chosenOne=null;
					chosenOne=(Effect)CMClass.Objects.EFFECT.getNew(behave);
					if(chosenOne!=null)
					{
						boolean alreadyHasIt=false;
						behave=chosenOne.ID();
						for(int i=0;i<notThese.size();i++)
							if(((Effect)notThese.get(i)).ID()==behave)
								{alreadyHasIt=true; break;}
						if(!alreadyHasIt)
						{
							mob.tell("Adding "+chosenOne.ID());
							return chosenOne;
						}
						else
							mob.tell("That effect is already on the list");
					}
					else
					{
						mob.tell("'"+behave+"' is not recognized.  Try '?'.");
					}
				}
			}
			else
				return null;
		}
		return null;
	}
	public Behavior newAnyBehavior(MOB mob){return newAnyBehavior(mob, new Vector());}
	public Behavior newAnyBehavior(MOB mob, Vector notThese)
	{
		while((mob.session()!=null)&&(!mob.session().killFlag()))
		{
			String behave=mob.session().prompt("Enter a behavior to add (?)\n\r:","");
			if(behave.length()>0)
			{
				if(behave.equalsIgnoreCase("?"))
					mob.tell(CMLib.lister().reallyList(CMClass.Objects.BEHAVIOR.all()).toString());
				else
				{
					Behavior chosenOne=null;
					chosenOne=(Behavior)CMClass.Objects.BEHAVIOR.getNew(behave);
					if(chosenOne!=null)
					{
						boolean alreadyHasIt=false;
						behave=chosenOne.ID();
						for(int i=0;i<notThese.size();i++)
							if(((Effect)notThese.get(i)).ID()==behave)
								{alreadyHasIt=true; break;}
						if(!alreadyHasIt)
						{
							mob.tell("Adding "+chosenOne.ID());
							return chosenOne;
						}
						else
							mob.tell("That behavior is already on the list");
					}
					else
					{
						mob.tell("'"+behave+"' is not recognized.  Try '?'.");
					}
				}
			}
			else
				return null;
		}
		return null;
	}
	public Item newAnyItem(MOB mob)
	{
		while((mob.session()!=null)&&(!mob.session().killFlag()))
		{
			String behave=mob.session().prompt("Enter an item to add (?)\n\r:","");
			if(behave.length()>0)
			{
				if(behave.equalsIgnoreCase("?"))
				{
//					mob.tell(CMLib.lister().reallyList(CMClass.Objects.WEARABLE.all()).toString());
					mob.tell(CMLib.lister().reallyList(CMClass.Objects.WEAPON.all()).toString());
					mob.tell(CMLib.lister().reallyList(CMClass.Objects.ITEM.all()).toString());
				}
				else
				{
					Item chosenOne=null;
					chosenOne=(Item)CMClass.Objects.ITEM.getNew(behave);
					if(chosenOne==null) chosenOne=(Item)CMClass.Objects.WEAPON.getNew(behave);
					if(chosenOne!=null)
					{
						mob.tell("Adding "+chosenOne.ID());
						return (Item)chosenOne.copyOf();
					}
					else
					{
						mob.tell("'"+behave+"' is not recognized.  Try '?'.");
					}
				}
			}
			else
				return null;
		}
		return null;
	}
	public Exit newAnyExit(MOB mob)
	{
		while((mob.session()!=null)&&(!mob.session().killFlag()))
		{
			String exit=mob.session().prompt("Enter an exit ID (?)\n\r:","");
			if(exit.length()>0)
			{
				if(exit.equalsIgnoreCase("?"))
					mob.tell(CMLib.lister().reallyList(CMClass.Objects.EXIT.all()).toString());
				else
				{
					Exit chosenOne=null;
					chosenOne=(Exit)CMClass.Objects.EXIT.getNew(exit);
					if(chosenOne!=null)
					{
						mob.tell("Adding "+chosenOne.ID());
						return chosenOne;
					}
					else
						mob.tell("'"+exit+"' is not recognized.  Try '?'.");
				}
			}
			else
				return null;
		}
		return null;
	}
	public Closeable newAnyCloseable(MOB mob)
	{
		while((mob.session()!=null)&&(!mob.session().killFlag()))
		{
			String exit=mob.session().prompt("Enter a closeable ID (?)\n\r:","");
			if(exit.length()>0)
			{
				if(exit.equalsIgnoreCase("?"))
					mob.tell(CMLib.lister().reallyList(CMClass.Objects.CLOSEABLE.all()).toString());
				else
				{
					Closeable chosenOne=null;
					chosenOne=(Closeable)CMClass.Objects.CLOSEABLE.getNew(exit);
					if(chosenOne!=null)
					{
						mob.tell("Adding "+chosenOne.ID());
						return chosenOne;
					}
					else
						mob.tell("'"+exit+"' is not recognized.  Try '?'.");
				}
			}
			else
				return null;
		}
		return null;
	}

	public void modExits(Vector<Room.REMap> exits, MOB M)
	{
		boolean done=false;
		while((M.session()!=null)&&(!M.session().killFlag())&&(!done))
		{
			Vector<Room.REMap> V=(Vector)exits.clone();
			int i=0;
			for(;i<V.size();i++)
			{
				Exit e=V.get(i).exit;
				M.session().rawPrintln((1+i)+". "+e.ID()+" "+e.exitID()+" to "+V.get(i).room.roomID());
			}
			M.session().rawPrintln((1+i)+". New Element");
			i=CMath.s_int(M.session().prompt("Edit which? ",""));
			if(--i<0) done=true;
			else if(i==V.size())
			{
				Exit e=newAnyExit(M);
				if(e!=null)
				{
					CMLib.genEd().genMiscSet(M, e);
					Room R=null;
					while(R==null)
					{
						String roomID=M.session().prompt("Connect this exit to what room? ","");
						if(roomID.equals("")) break;
						R=CMLib.map().getRoom(roomID);
					}
					if(R!=null)
						exits.add(new Room.REMap(R, e));
				}
			}
			else if(i<V.size())
			{
				char action=M.session().prompt("Edit (E)xit, target (R)oom, or (D)estroy link?","").trim().toUpperCase().charAt(0);
				if(action=='D') { exits.remove(V.get(i)); }
				else if(action=='E') CMLib.genEd().genMiscSet(M, V.get(i).exit);
				else if(action=='R')
				{
					Room R=null;
					while(R==null)
					{
						String roomID=M.session().prompt("Connect this exit to what room? ","");
						if(roomID.equals("")) break;
						R=CMLib.map().getRoom(roomID);
					}
					if(R!=null) synchronized(exits)
					{
						Room.REMap old=V.get(i);
						i=exits.indexOf(old);
						if(i<0)
							M.tell("Whoops, that exit disappeared when I wasn't looking!");
						else
							exits.set(i, new Room.REMap(R, old.exit));
					}
				}
			}
		}
	}
	public void modAffectable(Affectable E, MOB M)
	{
		boolean done=false;
		while((M.session()!=null)&&(!M.session().killFlag())&&(!done))
		{
			Vector<Effect> V=E.allEffects();
			int i=CMLib.genEd().promptVector(M, V, true);
			if(--i<0) done=true;
			else if(i==V.size())
			{
				Effect I=CMLib.genEd().newAnyEffect(M);
				if(I!=null)
					E.addEffect((Effect)CMLib.genEd().genMiscSet(M, I));
			}
			else if(i<V.size())
			{
				char action=M.session().prompt("(D)estroy or (M)odify "+V.get(i).ID()+" (default M)? ","M").trim().toUpperCase().charAt(0);
				if(action=='D') E.delEffect(V.get(i));
				else if(action=='M') CMLib.genEd().genMiscSet(M, V.get(i));
			}
		}
	}
	public void modBehavable(Behavable E, MOB M)
	{
		boolean done=false;
		while((M.session()!=null)&&(!M.session().killFlag())&&(!done))
		{
			Vector<Behavior> V=E.allBehaviors();
			int i=CMLib.genEd().promptVector(M, V, true);
			if(--i<0) done=true;
			else if(i==V.size())
			{
				Behavior I=CMLib.genEd().newAnyBehavior(M);
				if(I!=null)
					E.addBehavior((Behavior)CMLib.genEd().genMiscSet(M, I));
			}
			else if(i<V.size())
			{
				char action=M.session().prompt("(D)estroy or (M)odify "+V.get(i).ID()+" (default M)? ","M").trim().toUpperCase().charAt(0);
				if(action=='D') E.delBehavior(V.get(i));
				else if(action=='M') CMLib.genEd().genMiscSet(M, V.get(i));
			}
		}
	}
	//IMPORTANT: ALL CALLS TO THIS MUST HAVE UPPERCASE ENUM OPTIONS.
	public void enumSetPrompt(MOB M, Enum[] options, EnumSet set)
	{
		while((M.session()!=null)&&(!M.session().killFlag()))
		{
			StringBuilder S=new StringBuilder("Options: ");
			for(Enum E : options)
				S.append(E.toString());
			M.session().rawPrintln(S.toString());
			S=new StringBuilder("Current: ");
			for(Enum E : (Enum[])set.toArray(new Enum[0]))
				S.append(E.toString());
			M.session().rawPrintln(S.toString());
			String newString=M.session().prompt("Enter an option to toggle: ","");
			if(newString.equals("")) break;
			Enum E=CMClass.valueOf(options[0].getClass(), newString);
			if(E!=null)
				if(!set.remove(E))
					set.add(E);
		}
	}
	public Area areaPrompt(MOB M)
	{
		Area A=null;
		while((M.session()!=null)&&(!M.session().killFlag()))
		{
			String areaName=M.session().prompt("Enter an area name: ","");
			if(areaName.equals("")) return null;
			if(areaName.equals("?"))
			{
				for(Enumeration<Area> e=CMLib.map().areas();e.hasMoreElements();)
					M.session().rawPrintln(e.nextElement().name());
				
				continue;
			}
			A=CMLib.map().getArea(areaName);
			if(A==null)
			{
				A=CMLib.map().findAreaStartsWith(areaName);
				if(A==null)
				{
					M.session().rawPrintln("No area with that name found.");
					continue;
				}
				if(!M.session().prompt("Do you mean "+A.name()+"? (Y/n): ","Y").toUpperCase().startsWith("Y")) continue;
			}
			return A;
		}
		return null;
	}
	public Race racePrompt(MOB M)
	{
		while((M.session()!=null)&&(!M.session().killFlag()))
		{
			String raceName=M.session().prompt("Enter a race name: ","");
			if(raceName.equals("")) return null;
			if(raceName.equals("?"))
			{
				for(Iterator<Race> e=(Iterator)CMClass.Objects.RACE.all();e.hasNext();)
					M.session().rawPrintln(e.next().ID());
				
				continue;
			}
			Race R=(Race)CMClass.Objects.RACE.get(raceName);
			if(R==null)
			{
				M.session().rawPrintln("No race with that name found.");
				continue;
			}
			return R;
		}
		return null;
	}
}