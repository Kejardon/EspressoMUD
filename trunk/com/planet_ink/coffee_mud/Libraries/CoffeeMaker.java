package com.planet_ink.coffee_mud.Libraries;
import com.planet_ink.coffee_mud.core.interfaces.*;
import com.planet_ink.coffee_mud.core.*;
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

import java.util.*;

import com.planet_ink.coffee_mud.Libraries.interfaces.*;
import com.planet_ink.coffee_mud.Libraries.interfaces.XMLLibrary.XMLpiece;

/*
CoffeeMUD 5.6.2 copyright 2000-2010 Bo Zimmerman
EspressoMUD copyright 2011 Kejardon

Licensed under the Apache License, Version 2.0. You may obtain a copy of the license at
	http://www.apache.org/licenses/LICENSE-2.0
*/
/*
When passed an object, converts it to a string. Accomplish this by getting the object's CMSavable, then the CMSavable's
list of enums (Enum.values()), then running each enum's save(). Default values will be skipped to save room and time
loading, only real cost is one time prep when saving
When passed an object + String, loads the String to the object. Get the CMSavable, then the header, then check each
header string (headerEnum.valueOf() to find the load function to call) and load each argument. Send errors for
headers not found, don't worry about defaults. If defaults are important the caller will send a new object to load the
string to.
*/
@SuppressWarnings("unchecked")
public class CoffeeMaker extends StdLibrary implements GenericBuilder
{
	public String ID(){return "CoffeeMaker";}

	private Hashtable<String, String[]> basePropertiesStr=new Hashtable<String, String[]>();
/*
	private String[] getBaseString(CMSavable E, CMSavable.SaveEnum[] options)
	{
		String[] baseStrings=basePropertiesStr.get(E.ID());
		if(baseStrings!=null) return baseStrings;
		CMSavable baseObject=(CMSavable)E.newInstance();
		baseStrings=new String[options.length];
		for(int i=0;i<options.length;i++)
			baseStrings[i]=options[i].save(baseObject);
		basePropertiesStr.put(baseObject.ID(), baseStrings);
		return baseStrings;
	}
*/
	//General format is (saveCodeName+" "+saveSize+" "+saveData) for each entry, repeated until done.
	//Vectors or other things may have an ID at the start. If so it'll just be a (ID+" ") before the entries. CMClass.getUnknown will get the object, or STRING will mean a java String
	public ByteBuffer[] getPropertiesStr(CMSavable E)
	{
		if(E==null)
			return "null";
		CMSavable.SaveEnum[] options=E.totalEnumS();
		ByteBuffer[] values=new ByteBuffer[options.length];
		Vector<String> saveStrings=new Vector();
		for(int i=0;i<options.length;i++)
		{
			String A=options[i].save(E);
			if(A.equals(baseStrings[i])) continue;
			saveStrings.add(options[i].toString()+" "+A.length()+" "+A);
		}
		StringBuilder Combined=new StringBuilder("");
		for(int commandIndex=0;commandIndex<saveStrings.size();commandIndex++)
			Combined.append(saveStrings.elementAt(commandIndex).toString());
		for(int i=0;(i=Combined.indexOf("'",i))>=0;Combined.setCharAt(i++,'`')){}
		return Combined.toString();
	}

	private CMSavable.SaveEnum getParser(Enum E, String S)
	{
		try{return (CMSavable.SaveEnum)E.valueOf((Class)E.getClass().getSuperclass(), S);}
		catch(IllegalArgumentException e){if(e.getMessage().startsWith("No")) return null;}
		try{return (CMSavable.SaveEnum)E.valueOf(E.getClass(), S);}
		catch(IllegalArgumentException e){}
		return null;
	}

	//IMPORTANT! Make sure any Strings saved to the object are NEW STRINGS WITH THEIR OWN MEMORY and not SUBSTRINGS OF OLD STRINGS' MEMORY
	public void setPropertiesStr(CMSavable E, String S)
	{
		try{
		if(E==null)
		{
			Log.errOut("CoffeeMaker","setPropertiesStr: null 'E'");
			return;
		}
		Enum[] options=E.headerEnumS();
		while(S.length()>0)
		{
			int spaceIndex=S.indexOf(' ');
			String option=S.substring(0, spaceIndex);
			S=S.substring(spaceIndex+1);
			spaceIndex=S.indexOf(' ');
			int length=Integer.parseInt(S.substring(0, spaceIndex));
			S=S.substring(spaceIndex+1);
			String A=S.substring(0,length);
			S=S.substring(length);
			
			CMSavable.SaveEnum parser=null;
			for(int i=0;(i<options.length)&&(parser==null);i++)
				parser=getParser(options[i], option);

			if (parser==null)
				continue;
			parser.load(E, A);
		}
		}catch(RuntimeException e){Log.errOut("CoffeeMaker","Error in S: "+S); throw e;}
	}

	//The rest of this file is public functions for common load/save code SaveEnums may want to use.
	public CMSavable loadSub(String A)
	{
		if(A.equals("null")) return null;
		int x=A.indexOf(" ");
		
		CMClass.Objects type=(CMClass.Objects)CMClass.valueOf(CMClass.Objects.class, A.substring(0,x));
		if(type==null)
		{
			Log.errOut("CoffeeMaker","loadSub: bad String input for type: "+A.substring(0,x).intern());
			return null;
		}
		A=A.substring(x+1);
		x=A.indexOf(" ");
		CMSavable E=(CMSavable)type.getNew(A.substring(0,x));
		if(E==null)
		{
			Log.errOut("CoffeeMaker","loadSub: bad String input for object: "+A.substring(0,x).intern());
			return null;
		}
		setPropertiesStr((CMSavable)E, A.substring(x+1));
		return E;
	}
	public String getSubStr(CMSavable sub)
	{
		if(sub==null) return "null";
		StringBuilder A=new StringBuilder("");
		CMClass.Objects type=CMClass.getType(sub);
		A.append(type.name()+" "+sub.ID()+" "+getPropertiesStr(sub));
		return A.toString();
	}
	public String savAShort(short[] val)
	{
		StringBuilder A=new StringBuilder(""+val.length);
		for(short s : val)
			A.append(" "+s);
		return A.toString();
	}
	public short[] loadAShort(String A)
	{
		int x=A.indexOf(" ");
		short[] val=new short[Integer.parseInt(A.substring(0,x))];
		A=A.substring(x+1);
		for(int i=1;i<val.length;i++)
		{
			x=A.indexOf(" ");
			val[i-1]=Short.parseShort(A.substring(0,x));
			A=A.substring(x+1);
		}
		val[val.length-1]=Short.parseShort(A);
		return val;
	}
	public String savAInt(int[] val)
	{
		StringBuilder A=new StringBuilder(""+val.length);
		for(int s : val)
			A.append(" "+s);
		return A.toString();
	}
	public int[] loadAInt(String A)
	{
		int x=A.indexOf(" ");
		int[] val=new int[Integer.parseInt(A.substring(0,x))];
		A=A.substring(x+1);
		for(int i=1;i<val.length;i++)
		{
			x=A.indexOf(" ");
			val[i-1]=Integer.parseInt(A.substring(0,x));
			A=A.substring(x+1);
		}
		val[val.length-1]=Integer.parseInt(A);
		return val;
	}
	public String savALong(long[] val)
	{
		StringBuilder A=new StringBuilder(""+val.length);
		for(long s : val)
			A.append(" "+s);
		return A.toString();
	}
	public long[] loadALong(String A)
	{
		int x=A.indexOf(" ");
		long[] val=new long[Integer.parseInt(A.substring(0,x))];
		A=A.substring(x+1);
		for(int i=1;i<val.length;i++)
		{
			x=A.indexOf(" ");
			val[i-1]=Long.parseLong(A.substring(0,x));
			A=A.substring(x+1);
		}
		val[val.length-1]=Long.parseLong(A);
		return val;
	}
	public String savADouble(double[] val)
	{
		StringBuilder A=new StringBuilder(""+val.length);
		for(double s : val)
			A.append(" "+Double.doubleToLongBits(s));
		return A.toString();
	}
	public double[] loadADouble(String A)
	{
		int x=A.indexOf(" ");
		double[] val=new double[Integer.parseInt(A.substring(0,x))];
		A=A.substring(x+1);
		for(int i=1;i<val.length;i++)
		{
			x=A.indexOf(" ");
			val[i-1]=Double.longBitsToDouble(Long.parseLong(A.substring(0,x)));
			A=A.substring(x+1);
		}
		val[val.length-1]=Double.longBitsToDouble(Long.parseLong(A));
		return val;
	}
	public String savABoolean(boolean[] val)
	{
		StringBuilder A=new StringBuilder(""+val.length+" ");
		for(boolean s : val)
			A.append((s?'T':'F'));
		return A.toString();
	}
	public boolean[] loadABoolean(String A)
	{
		int x=A.indexOf(" ");
		boolean[] val=new boolean[Integer.parseInt(A.substring(0,x))];
		A=A.substring(x+1);
		for(int i=0;i<val.length;i++)
			val[i]=(A.charAt(i)=='T');
		return val;
	}
	public String savAChar(char[] val)
	{
		StringBuilder A=new StringBuilder(""+val.length+" ");
		A.append(val);
		return A.toString();
	}
	public char[] loadAChar(String A)
	{
		int x=A.indexOf(" ");
		char[] val=new char[Integer.parseInt(A.substring(0,x))];
		A=A.substring(x+1);
		for(int i=0;i<val.length;i++)
			val[i]=A.charAt(i);
		return val;
	}
	public String savAString(String[] val)
	{
		StringBuilder A=new StringBuilder(""+val.length+" ");
		for(String s : val)
			A.append(s.length()+" "+s);
		return A.toString();
	}
	public String[] loadAString(String A)
	{
		int x=A.indexOf(" ");
		int y=0;
		String[] val=new String[Integer.parseInt(A.substring(0,x))];
		A=A.substring(x+1);
		for(int i=0;i<val.length;i++)
		{
			x=A.indexOf(" ");
			y=Integer.parseInt(A.substring(0,x))+x+1;
			val[i]=A.substring(x+1,y).intern();	//Keeping the memory clean!
			A=A.substring(y);
		}
		return val;
	}
	public String savStringsInterlaced(String[] ... val)
	{
		int numArrays=val.length;
		if(numArrays==0) return "0 ";	//Space is needed by loadStrings method
		int arrayLength=val[0].length;	//Need that check/return to avoid trouble with this line.
		StringBuilder A=new StringBuilder(""+arrayLength*numArrays+" ");
		for(int i=0;i<arrayLength;i++)
			for(int j=0;j<numArrays;j++)
			{
				String s=val[j][i];
				A.append(s.length()+" "+s);
			}
		return A.toString();
	}
	public String[][] loadStringsInterlaced(String A, int dim)
	{
		int x=A.indexOf(" ");
		int y=0;
		int length=Integer.parseInt(A.substring(0,x))/dim;
		String[][] val=new String[dim][length];
		A=A.substring(x+1);
		for(int i=0;i<length;i++)
			for(int j=0;j<dim;j++)
			{
				x=A.indexOf(" ");
				y=Integer.parseInt(A.substring(0,x))+x+1;
				val[j][i]=A.substring(x+1,y).intern();	//Keeping the memory clean!
				A=A.substring(y);
			}
		return val;
	}
	public String getSaveNumStr(Enumeration<CMSavable> e)
	{
		StringBuilder str=new StringBuilder("");
		for(;e.hasMoreElements();)
		{
			CMSavable A=e.nextElement();
			if(str.length()>0) str.append(";");
			str.append(A.saveNum());
		}
		return str.toString();
	}
	//Note: Does not support most core/interface files!
	public String getVectorStr(Vector V)
	{
		if(V==null) return "null";
		StringBuilder S=new StringBuilder("");
		for(int i=0;i<V.size();i++)
		{
			Object O=V.get(i);
			if(O == null)
				S.append("nul");
			else if(O instanceof String)
			{
				String str=(String)O;
				S.append("STRING "+str.length()+" "+str);
			}
			else
			{
				CMObject obj=(CMObject)O;
				CMClass.Objects type=CMClass.getType(obj);
				if(obj instanceof CMSavable)
				{
					String str=getPropertiesStr((CMSavable)obj);
					S.append(type.name()+" "+obj.ID()+" "+str.length()+" "+str);
				}
				else
					S.append(type.name()+" "+obj.ID()+" ");
			}
		}
		return S.toString();
	}
	//Note: Does not support most core/interface files!
	public Vector setVectorStr(String S)
	{
		if(S.equals("null")) return null;
		Vector V=new Vector();
		while(!(S.equals("")))
		{
			if(S.startsWith("nul"))
			{
				S=S.substring(3);
				V.add(null);
			}
			else if(S.startsWith("STRING"))
			{
				int y=(S.substring(7).indexOf(" "))+7;
				int x=Integer.parseInt(S.substring(7,y))+y+1;	//The actual size will never be used so just +y here now
				V.add(S.substring(y+1,x).intern());	//Gotta make sure that S is cleaned up later!
				S=S.substring(x);
			}
			else
			{
				int x=S.indexOf(" ");
				CMClass.Objects type=(CMClass.Objects)CMClass.valueOf(CMClass.Objects.class, S.substring(0,x));
				if(type==null)
				{
					Log.errOut("CoffeeMaker","setVectorStr: bad String input for type: "+S.substring(0,x).intern());
					return null;
				}
				S=S.substring(x+1);
				x=S.indexOf(" ");
				CMObject E=(CMObject)type.getNew(S.substring(0,x));
				if(E==null)
				{
					Log.errOut("CoffeeMaker","setVectorStr: bad String input for object: "+S.substring(0,x).intern());
					return null;
				}
				if(E instanceof CMSavable)
				{
					int y=S.substring(x+1).indexOf(" ")+x+1;
					x=Integer.parseInt(S.substring(x+1,y))+y+1;
					setPropertiesStr((CMSavable)E, S.substring(y+1,x));
					S=S.substring(x);
				}
				else
					S=S.substring(x+1);
				V.add(E);
			}
		}
		return V;
	}
	//Note: Does not support most core/interface files!
	public String getWVectorStr(WVector V)
	{
		if(V==null) return "null";
		StringBuilder S=new StringBuilder("");
		for(int i=0;i<V.size();i++)
		{
			S.append(V.weight(i)+" ");
			Object O=V.get(i);
			if(O == null)
				S.append("nul");
			else if(O instanceof String)
			{
				String str=(String)O;
				S.append("STRING "+str.length()+" "+str);
			}
			else
			{
				CMObject obj=(CMObject)O;
				CMClass.Objects type=CMClass.getType(obj);
				if(O instanceof CMSavable)
				{
					String str=getPropertiesStr((CMSavable)obj);
					S.append(type.name()+" "+obj.ID()+" "+str.length()+" "+str);
				}
				else
					S.append(type.name()+" "+obj.ID()+" ");
			}
		}
		return S.toString();
	}
	//Note: Does not support most core/interface files!
	public WVector setWVectorStr(String S)
	{
		if(S.equals("null")) return null;
		WVector V=new WVector();
		while(!(S.equals("")))
		{
			Object O=null;
			int i=S.indexOf(" ");
			int weight=Integer.parseInt(S.substring(0,i));
			S=S.substring(i+1);
			if(S.startsWith("nul"))
			{
				S=S.substring(3);
			}
			else if(S.startsWith("STRING"))
			{
				int y=(S.substring(7).indexOf(" "))+7;
				int x=Integer.parseInt(S.substring(7,y))+y+1;
				O=S.substring(y+1,x).intern();
				S=S.substring(x);
			}
			else
			{
				int x=S.indexOf(" ");
				CMClass.Objects type=(CMClass.Objects)CMClass.valueOf(CMClass.Objects.class, S.substring(0,x));
				if(type==null)
				{
					Log.errOut("CoffeeMaker","setVectorStr: bad String input for type: "+S.substring(0,x).intern());
					return null;
				}
				S=S.substring(x+1);
				x=S.indexOf(" ");
				CMObject E=(CMObject)type.getNew(S.substring(0,x));
				if(E==null)
				{
					Log.errOut("CoffeeMaker","setVectorStr: bad String input for object: "+S.substring(0,x).intern());
					return null;
				}
				if(E instanceof CMSavable)
				{
					int y=S.substring(x+1).indexOf(" ")+x+1;
					x=Integer.parseInt(S.substring(x+1,y))+y+1;
					setPropertiesStr((CMSavable)E, S.substring(y+1,x));
					S=S.substring(x);
				}
				else
					S=S.substring(x+1);
				O=E;
			}
			
			V.add(O, weight);
		}
		return V;
	}
}