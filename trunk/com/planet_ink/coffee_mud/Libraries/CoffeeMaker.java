package com.planet_ink.coffee_mud.Libraries;
import com.planet_ink.coffee_mud.core.interfaces.*;
import com.planet_ink.coffee_mud.core.*;
import com.planet_ink.coffee_mud.core.database.*;
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
import java.nio.ByteBuffer;
import java.nio.CharBuffer;

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

/*
	private Hashtable<String, String[]> basePropertiesStr=new Hashtable<String, String[]>();
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
*/
	protected byte[] toBytes(int i)
	{
		byte[] result = new byte[4];
		result[0] = (byte) (i >> 24);
		result[1] = (byte) (i >> 16);
		result[2] = (byte) (i >> 8);
		result[3] = (byte) (i);
		return result;
	}
	protected byte[] toBytes(int i, int index, byte[] toThis)
	{
		toThis[index] = (byte) (i >> 24);
		toThis[index+1] = (byte) (i >> 16);
		toThis[index+2] = (byte) (i >> 8);
		toThis[index+3] = (byte) (i);
		return toThis;
	}

	//The rest of this file is public functions for common load/save code SaveEnums may want to use.
	public ByteBuffer savAShort(short[] val)
	{
		ByteBuffer buf=ByteBuffer.wrap(new byte[val.length*2]);
		for(short s : val)
			buf.putShort(s);
		buf.rewind();
		return buf;
	}
	public short[] loadAShort(ByteBuffer A)
	{
		short[] vals=new short[A.remaining()/2];
		for(int i=0;i<vals.length;i++)
			vals[i]=A.getShort();
		return vals;
	}
	public ByteBuffer savAInt(int[] val)
	{
		ByteBuffer buf=ByteBuffer.wrap(new byte[val.length*4]);
		for(int i : val)
			buf.putInt(i);
		buf.rewind();
		return buf;
	}
	public int[] loadAInt(ByteBuffer A)
	{
		int[] vals=new int[A.remaining()/4];
		for(int i=0;i<vals.length;i++)
			vals[i]=A.getInt();
		return vals;
	}
	public ByteBuffer savALong(long[] val)
	{
		ByteBuffer buf=ByteBuffer.wrap(new byte[val.length*8]);
		for(long l : val)
			buf.putLong(l);
		buf.rewind();
		return buf;
	}
	public long[] loadALong(ByteBuffer A)
	{
		long[] vals=new long[A.remaining()/8];
		for(int i=0;i<vals.length;i++)
			vals[i]=A.getLong();
		return vals;
	}
	public ByteBuffer savADouble(double[] val)
	{
		ByteBuffer buf=ByteBuffer.wrap(new byte[val.length*8]);
		for(double d : val)
			buf.putDouble(d);
		buf.rewind();
		return buf;
	}
	public double[] loadADouble(ByteBuffer A)
	{
		double[] vals=new double[A.remaining()/8];
		for(int i=0;i<vals.length;i++)
			vals[i]=A.getDouble();
		return vals;
	}
	//This can be compressed better, but compression will take processing.
	public ByteBuffer savABoolean(boolean[] val)
	{
		ByteBuffer buf=ByteBuffer.wrap(new byte[val.length]);
		for(boolean s : val)
			buf.put(s?(byte)1:(byte)0);
		buf.rewind();
		return buf;
	}
	public boolean[] loadABoolean(ByteBuffer A)
	{
		boolean[] bools=new boolean[A.remaining()];
		for(boolean bool : bools)
			bool=(A.get()==0)?false:true;
		return bools;
	}
	public ByteBuffer savAChar(char[] val)
	{
		return DBManager.charFormat.encode(CharBuffer.wrap(val));
	}
	public char[] loadAChar(ByteBuffer A)
	{
		return DBManager.charFormat.decode(A).array();
	}
	public ByteBuffer savAString(String[] val)
	{
		if(val.length==0) return emptyBuffer;
		byte[][] entries=new byte[1+2*val.length][];
		int size=0;
		entries[0]=toBytes(val.length);
		for(int i=0;i<val.length;i++)
		{
			entries[2*i+2]=val[i].getBytes(DBManager.charFormat);
			entries[2*i+1]=toBytes(entries[2*i+2].length);
			size+=4+entries[2*i+2].length;
		}
		ByteBuffer values=ByteBuffer.wrap(new byte[size+4]);
		for(byte[] entry : entries)
			values.put(entry);
		values.rewind();
		return values;
	}
	public ByteBuffer savAAString(String[]... val)
	{
		if(val.length==0) return emptyBuffer;
		int numStrings=0;
		for(String[] set : val) numStrings+=set.length;
		byte[][] entries=new byte[1+2*numStrings][];
		int size=0;
		entries[0]=toBytes(numStrings);
		int i=0;
		for(String[] set : val)
		{
			for(String str : set)
			{
				entries[2*i+2]=str.getBytes(DBManager.charFormat);
				entries[2*i+1]=toBytes(entries[2*i+2].length);
				size+=4+entries[2*i+2].length;
				i++;
			}
		}
		ByteBuffer values=ByteBuffer.wrap(new byte[size+4]);
		for(byte[] entry : entries)
			values.put(entry);
		values.rewind();
		return values;
	}
	public String[] loadAString(ByteBuffer A)
	{
		String[] strings=new String[A.getInt()];
		for(int i=0;i<strings.length;i++)
		{
			byte[] buf=new byte[A.getInt()];
			A.get(buf);
			strings[i]=new String(buf, DBManager.charFormat);
		}
		return strings;
	}
	public ByteBuffer savString(String val)
	{
		if(val.length()==0) return emptyBuffer;
		return ByteBuffer.wrap(val.getBytes(DBManager.charFormat));
	}
	public String loadString(ByteBuffer A)
	{
		byte[] buf=new byte[A.remaining()];
		A.get(buf);
		return new String(buf, DBManager.charFormat);
	}
/*	Not needed, this will be done within DBManager if possible.
	public ByteBuffer savSubFixed(CMSavable.CMSubSavable sub)	//prepend with 00
	{
		int size=1;
		ByteBuffer[] fixedVars=DBManager.getFormat(sub).getPartFixedVals(sub);
		
		return null;
	}
	public ByteBuffer savSubVar(CMSavable.CMSubSavable sub)	//prepend with 01
	{
		return null;
	}
*/
	public ByteBuffer savSubFull(CMSavable sub)	//prepend with 02, ID Size, ID
	{
		//Total size: 1(header)+4(Type ID size)+?(Type ID)+4(ID Size)+?(ID)+?(Data)=9+typeSize+IDSize+DataSize
		CMClass.Objects type=CMClass.getType(sub);
		byte[] typeBytes=type.name().getBytes(DBManager.charFormat);
		byte[] IDbytes=sub.ID().getBytes(DBManager.charFormat);
		int totalSize=9+IDbytes.length+typeBytes.length;
		Vector<ByteBuffer> allVals=new Vector();
		for(CMSavable.SaveEnum thisEnum : sub.totalEnumS())
		{
			ByteBuffer buf=thisEnum.save(sub);
			int size=buf.remaining();
			if(size==0) continue;
			allVals.add(DBManager.charFormat.encode(thisEnum.name()));
			allVals.add((ByteBuffer)ByteBuffer.wrap(new byte[4]).putInt(size).rewind());
			allVals.add(buf);
			totalSize+=size+7;
		}
		ByteBuffer saveData=ByteBuffer.wrap(new byte[totalSize]);
		saveData.put((byte)2).putInt(typeBytes.length).put(typeBytes).putInt(IDbytes.length).put(IDbytes);
		for(Enumeration<ByteBuffer> e=allVals.elements();e.hasMoreElements();)
			saveData.put(e.nextElement());
		saveData.rewind();
		return saveData;
	}
	public CMSavable loadSub(ByteBuffer buf, CMSavable sub)
	{
		switch(buf.get())
		{
			case 3:	//fixed data
			{
				DBManager.SaveFormat format=DBManager.getFormat(sub);
				//TODO: These should be cached and loaded from format, not generated on the fly
				CMSavable.SaveEnum[] parsers=new CMSavable.SaveEnum[format.enums.size()];
				int[] weights=new int[parsers.length];
				format.enums.toArrays(parsers, weights);
				for(int parserNum=0;parserNum<parsers.length;parserNum++)
				{
					if(parsers[parserNum]==null)
					{
						buf.position(buf.position()+weights[parserNum]);
						continue;
					}
					ByteBuffer saveBuffer=buf.slice();
					buf.position(buf.position()+weights[parserNum]);
					saveBuffer.limit(weights[parserNum]);
					parsers[parserNum].load(sub, saveBuffer);
				}
				break;
			}
			case 2:	//all data
			{
				byte[] typeBytes=new byte[buf.getInt()];
				buf.get(typeBytes);
				byte[] IDbytes=new byte[buf.getInt()];
				buf.get(IDbytes);
				sub=(CMSavable)((CMClass.Objects)CMClass.valueOf(CMClass.Objects.class, new String(typeBytes, DBManager.charFormat))).getNew(new String(IDbytes, DBManager.charFormat));
				//fall through to var data
			}
			case 1:	//var data
			{
				DBManager.SaveFormat format=DBManager.getFormat(sub);
				byte[] enumName=new byte[3];
				Enum[] options=format.myObject.headerEnumS();
				while(buf.remaining()>0)
				{
					buf.get(enumName);
					String option=new String(enumName, DBManager.charFormat);
					CMSavable.SaveEnum thisEnum=null;
					for(int j=0;(j<options.length)&&(thisEnum==null);j++)
						thisEnum=DBManager.getParser(options[j], option);
					int size=buf.getInt();
					if(thisEnum==null)
					{
						buf.position(buf.position()+size);
						continue;
					}
					ByteBuffer saveBuffer=buf.slice();
					buf.position(buf.position()+size);
					saveBuffer.limit(size);
					thisEnum.load(sub, saveBuffer);
				}
				break;
			}
			case 0:	//Null flag
				return null;
			//Should there be a default flag that just returns sub as-is?
		}
		return sub;
	}
	//The reverse of these is just loadAInt
/*	public ByteBuffer savSaveNums(Vector<CMSavable> e)
	{
		return savSaveNums((CMSavable[])e.toArray(new CMSavable[e.size()]));
	} */
	public ByteBuffer savSaveNums(CMSavable[] e)
	{
		ByteBuffer buf=ByteBuffer.wrap(new byte[e.length*4]);
		for(CMSavable obj : e)
			buf.putInt(obj==null?0:obj.saveNum());
		buf.rewind();
		return buf;
	}
	public ByteBuffer savExits(Room.REMap[] exits)
	{
		ByteBuffer buf=ByteBuffer.wrap(new byte[exits.length*8]);
		for(Room.REMap exit : exits)
			buf.putInt(exit.room.saveNum()).putInt(exit.exit.saveNum());
		buf.rewind();
		return buf;
	}
	public int[][] loadExits(ByteBuffer buf)
	{
		int[][] nums=new int[buf.remaining()/8][2];
		for(int[] theseNums : nums)
		{
			theseNums[0]=buf.getInt();
			theseNums[1]=buf.getInt();
		}
		return nums;
	}
	
	//Save a list of races.
	public ByteBuffer getRaceWVector(WVector<Race> V)
	{
		if(V==null) return emptyBuffer;
		
		int totalSize=0;
		for(int i=0;i<V.size();i++)
			totalSize+=8+V.get(i).ID().length();
		
		ByteBuffer buf=ByteBuffer.wrap(new byte[totalSize]);
		for(int i=0;i<V.size();i++)
		{
			String ID=V.get(i).ID();
			buf.putInt(V.weight(i)).putInt(ID.length()).put(ID.getBytes(DBManager.charFormat));
		}
		buf.rewind();
		return buf;
	}
	public WVector<Race> setRaceWVector(ByteBuffer S)
	{
		if(S.remaining()==0) return null;
		WVector V=new WVector();
		while(S.remaining()>0)
		{
			int weight=S.getInt();
			byte[] raceBytes=new byte[S.getInt()];
			S.get(raceBytes);
			Race R=(Race)CMClass.Objects.RACE.get(new String(raceBytes, DBManager.charFormat));
			if(R!=null)
				V.add(R, weight);
		}
		return V;
	}
/*
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
	public ByteBuffer getWVectorStr(WVector V)
	{
		if(V==null) return ByteBuffer.wrap(new byte[0]);
		
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
	public WVector setWVectorStr(ByteBuffer S)
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
*/
}