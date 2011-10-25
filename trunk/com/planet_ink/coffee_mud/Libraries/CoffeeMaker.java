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

	private String[] getBaseString(CMSavable E, CMSavable.SaveEnum[] options)
	{
		String[] baseStrings=basePropertiesStr.get(E.ID());
		if(baseStrings!=null) return baseStrings;
		baseStrings=new String[options.length];
		for(int i=0;i<options.length;i++)
			baseStrings[i]=options[i].save(E);
		basePropertiesStr.put(E.ID(), baseStrings);
		return baseStrings;
	}

	//General format is (saveCodeName+" "+saveSize+" "+saveData) for each entry, repeated until done.
	//Vectors or other things may have an ID at the start. If so it'll just be a (ID+" ") before the entries. CMClass.getUnknown will get the object, or STRING will mean a java String
	public String getPropertiesStr(CMSavable E)
	{
		if(E==null)
			return "null";
		CMSavable.SaveEnum[] options=E.totalEnumS();
		String[] baseStrings=getBaseString(E, options);
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
		try{return (CMSavable.SaveEnum)E.valueOf(E.getClass(), S);}
		catch(IllegalArgumentException e){}
		return null;
	}

	//IMPORTANT! Make sure any Strings saved to the object are NEW STRINGS WITH THEIR OWN MEMORY and not SUBSTRINGS OF OLD STRINGS' MEMORY
	public void setPropertiesStr(CMSavable E, String S)
	{
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
	}

	//The rest of this file is public functions for common load/save code SaveEnums may want to use.
	public CMSavable loadSub(String A)
	{
		if(A.equals("null")) return null;
		int x=A.indexOf(" ");
		String ID=A.substring(0,x);
		CMSavable sub=null;
		if(ID.equals("COMMON"))	//Could add this for all, would save speed loading, but raise DB size a little. Also it would take effort :V
		{
			A=A.substring(x+1);
			x=A.indexOf(" ");
			ID=A.substring(0,x);
			sub=(CMSavable)CMClass.Objects.COMMON.getNew(ID);
		}
		else
			sub=(CMSavable)CMClass.getUnknown(ID);
		
		setPropertiesStr(sub, A.substring(x+1));
		return sub;
	}
	public String getSubStr(CMSavable sub)
	{
		if(sub==null) return "null";
		StringBuilder A=new StringBuilder("");
		if(sub instanceof CMCommon)
			A.append("COMMON ");
		A.append(sub.ID()+" "+getPropertiesStr(sub));
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
	public String getVectorStr(Vector V)
	{
		if(V==null) return "null";
		StringBuilder S=new StringBuilder("");
		for(int i=0;i<V.size();i++)
		{
			Object O=V.get(i);
			if(O == null)
				S.append("null");
			else if(O instanceof String)
			{
				String str=(String)O;
				S.append("STRING "+str.length()+" "+str);
			}
			else
			{
				CMSavable obj=(CMSavable)O;
				String str=getPropertiesStr(obj);
				S.append(obj.ID()+" "+str.length()+" "+str);
			}
		}
		return S.toString();
	}
	public Vector setVectorStr(String S)
	{
		if(S.equals("null")) return null;
		Vector V=new Vector();
		while(!(S.equals("")))
		{
			if(S.startsWith("null"))
			{
				S=S.substring(4);
				V.add(null);
			}
			else if(S.startsWith("STRING"))
			{
				//STRING 4 TEST
				//0123456789012
				//y=8
				//x=4+8+1=13
				int y=(S.substring(7).indexOf(" "))+7;
				int x=Integer.parseInt(S.substring(7,y))+y+1;	//The actual size will never be used so just +y here now
				V.add(S.substring(y+1,x).intern());	//Gotta make sure that S is cleaned up later!
				S=S.substring(x);
			}
			else
			{
				int x=S.indexOf(" ");
				CMSavable E=(CMSavable)CMClass.getUnknown(S.substring(0,x));
				if(E==null)
				{
					Log.errOut("CoffeeMaker","setVectorStr: bad String input: "+S.substring(0,x).intern());
					return null;
				}
				int y=S.substring(x+1).indexOf(" ")+x+1;
				x=Integer.parseInt(S.substring(x+1,y))+y+1;
				setPropertiesStr(E, S.substring(y+1,x));
				S=S.substring(x);
				V.add(E);
			}
		}
		return V;
	}
}