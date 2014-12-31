package com.planet_ink.coffee_mud.core;
import com.planet_ink.coffee_mud.core.interfaces.*;
import com.planet_ink.coffee_mud.core.*;

import java.util.*;

/*
CoffeeMUD 5.6.2 copyright 2000-2010 Bo Zimmerman
EspressoMUD copyright 2011 Kejardon

Licensed under the Apache License, Version 2.0. You may obtain a copy of the license at
	http://www.apache.org/licenses/LICENSE-2.0
*/

public class Resources
{
	private static Resources rscs=null;
	public Resources() { if(rscs==null) rscs=this; }
	public static Resources instance()
	{
		if(rscs==null) rscs=new Resources();
		return rscs;
	}

	public static Resources newResources(){ return new Resources();}

	//private DVector resources=new DVector(3);
	private HashMap<String, Object> resources=new HashMap();

	public static void clearResources(){instance()._clearResources();}
	public static void removeResource(String ID){ instance()._removeResource(ID);}
	public static ArrayList<String> findResourceKeys(String srch){return instance()._findResourceKeys(srch);}
	public static Object getResource(String ID){return instance()._getResource(ID);}
	public static void submitResource(String ID, Object obj){instance()._submitResource(ID,obj);}
	public static boolean isFileResource(String filename){return instance()._isFileResource(filename);}
	public static StringBuffer getFileResource(String filename, boolean reportErrors){return instance()._getFileResource(filename,reportErrors);}
	public static boolean saveFileResource(String filename, MOB whom, StringBuffer myRsc){return instance()._saveFileResource(filename,whom,myRsc);}
	public static boolean updateFileResource(String filename, Object obj){return instance()._updateFileResource(filename,obj);}
	public static boolean findRemoveProperty(CMFile F, String match){return instance()._findRemoveProperty(F,match);}

	public static String getLineMarker(StringBuffer buf)
	{
		for(int i=0;i<buf.length()-1;i++)
			switch(buf.charAt(i))
			{
			case '\n':
				if(buf.charAt(i+1)=='\r')
					return "\n\r";
				return "\n";
			case '\r':
				if(buf.charAt(i+1)=='\n')
					return "\r\n";
				return "\r";
			}
		return "\r\n";
	}

	//More efficient, preferred option when you want to iterate anyways.
	public static LinkedList<String> getFileLineList(StringBuffer buf)
	{
		LinkedList<String> V=new LinkedList();
		if(buf==null) return V;
		int start=0;
		int stop=buf.length();
		for(int i=0;i<stop;i++)
		{
			if((i<stop-1)&&
			   (((buf.charAt(i)=='\n')&&(buf.charAt(i+1)=='\r'))||((buf.charAt(i)=='\r')&&(buf.charAt(i+1)=='\n'))))
			{
				V.add(buf.substring(start, i));
				i++;
				start=i+1;
			}
			else
			if((buf.charAt(i)=='\r')||(buf.charAt(i)=='\n'))
			{
				V.add(buf.substring(start, i));
				start=i+1;
			}
		}
		if(stop>start)
			V.add(buf.substring(start));
		return V;
	}
	public static Vector<String> getFileLineVector(StringBuffer buf)
	{
		Vector<String> V=new Vector();
		if(buf==null) return V;
		int start=0;
		int stop=buf.length();
		for(int i=0;i<stop;i++)
		{
			if((i<stop-1)&&
			   (((buf.charAt(i)=='\n')&&(buf.charAt(i+1)=='\r'))||((buf.charAt(i)=='\r')&&(buf.charAt(i+1)=='\n'))))
			{
				V.addElement(buf.substring(start, i));
				i++;
				start=i+1;
			}
			else
			if((buf.charAt(i)=='\r')||(buf.charAt(i)=='\n'))
			{
				V.addElement(buf.substring(start, i));
				start=i+1;
			}
		}
		if(stop>start)
			V.addElement(buf.substring(start));
		//V.trimToSize();
		return V;
	}

	public static String buildResourcePath(String path)
	{
		if((path==null)||(path.length()==0)) return "resources/";
		return "resources/"+path+"/";
	}

	//What is this for? Saves a Hshtable to file I guess, nothing currently uses it.. Might be intended for stuff like lists.ini
	public static void updateMultiList(String filename, Hashtable<String, Vector<String>> lists)
	{
		StringBuffer str=new StringBuffer("");
		for(Enumeration<String> e=lists.keys();e.hasMoreElements();)
		{
			String ml=e.nextElement();
			Vector<String> V=lists.get(ml);
			str.append(ml+"\n");	//Use \n because this is what CMFile prefers
			if(V!=null)
			for(int v=0;v<V.size();v++)
				str.append(V.elementAt(v)+"\n");
			str.append("\n");
		}
		new CMFile(filename,null,false).saveText(str);
	}

	//Nothing currently uses this. Inverse of above
	public static Hashtable<String, Vector<String>> getMultiLists(String filename)
	{
		Hashtable<String, Vector<String>> oldH=new Hashtable();
		LinkedList<String> V=new LinkedList();
		try{
			V=getFileLineList(new CMFile("resources/"+filename,null,false).text());
		}catch(Exception e){}
		if((V!=null)&&(V.peek()!=null))
		{
			String journal="";
			Vector<String> set=null;//=new Vector();
			while(V.peek()!=null)
			{
				String s=V.pop();
				if(s.trim().length()==0)
					journal="";
				else if(journal.length()==0)
				{
					journal=s;
					set=new Vector();
					oldH.put(journal,set);
				}
				else if(set!=null)
					set.add(s);
			}
		}
		return oldH;
	}

	public void _clearResources()
	{
		synchronized(resources)
		{
			resources.clear();
		}
	}

	public ArrayList<String> _findResourceKeys(String srch)
	{
		srch=srch.toUpperCase();
		ArrayList<String> V=new ArrayList();
		synchronized(resources)
		{
			Iterator<String> keys=resources.keySet().iterator();
			while(keys.hasNext())
			{
				String key=keys.next();
				if((srch.length()==0)||(key.toUpperCase().indexOf(srch)>=0))
					V.add(key);
			}
		}
		return V;
	}

	public Object _getResource(String ID)
	{
		synchronized(resources)
		{
			return resources.get(ID);
		}
	}

	public static Object prepareObject(Object obj)
	{
		if(obj instanceof Vector) ((Vector)obj).trimToSize();
		else if(obj instanceof DVector) ((DVector)obj).trimToSize();
		return obj;
	}

	public void _submitResource(String ID, Object obj)
	{
		synchronized(resources)
		{
			Object prepared=prepareObject(obj);
			resources.put(ID, prepared);
		}
	}

	private Object _updateResource(String ID, Object obj)
	{
		synchronized(resources)
		{
			Object prepared=prepareObject(obj);
			resources.put(ID, prepared);
			return prepared;
		}
	}

	public void _removeResource(String ID)
	{
		synchronized(resources)
		{
			resources.remove(ID);
		}
	}

	public boolean _isFileResource(String filename)
	{
		if(_getResource(filename)!=null) return true;
		if(new CMFile("resources/"+filename,null,false).exists())
			return true;
		return false;
	}

	public StringBuffer _toStringBuffer(Object o)
	{
		if(o!=null)
		{
			if(o instanceof StringBuffer)
				return (StringBuffer)o;
			else
			if(o instanceof String)
				return new StringBuffer((String)o);
		}
		return null;
	}
	
	public StringBuffer _getFileResource(String filename, boolean reportErrors)
	{
		Object rsc=_getResource(filename);
		if(rsc != null)
			return _toStringBuffer(rsc);
		StringBuffer buf=new CMFile("resources/"+filename,null,reportErrors).text();
		_submitResource(filename,buf);
		return buf;
	}

	public boolean _updateFileResource(String filename, Object obj)
	{
		_updateResource(CMFile.vfsifyFilename(filename), obj);
		return _saveFileResource(filename,null,_toStringBuffer(obj));
	}

	public boolean _saveFileResource(String filename, MOB whom, StringBuffer myRsc)
	{
		filename=CMFile.vfsifyFilename(filename);
		if(!filename.startsWith("resources/"))
			filename="resources/"+filename;
		return new CMFile(filename,whom,false).saveRaw(myRsc);
	}

	public boolean _findRemoveProperty(CMFile F, String match)
	{
		boolean removed=false;
		StringBuffer text=F.textUnformatted();
		String upper=text.toString().toUpperCase();
		match=match.toUpperCase();
		int x=upper.lastIndexOf(match);
		//int offset=0;
		while(x>=0)
		{
			falsePos:
			if((x==0)||(!Character.isLetterOrDigit(text.charAt(x-1))))
			{
				int i;
				for(i=x+match.length();Character.isWhitespace(text.charAt(i));i++){}
				if(text.charAt(i)!='=') break falsePos;
				int temp1=text.lastIndexOf("\n",x);
				int temp2=text.lastIndexOf("\r",x);
				int zb=(temp2>temp1)?temp2:temp1;
				if(zb<0) zb=0; else zb++;
				temp1=text.indexOf("\n",i);
				temp2=text.indexOf("\r",i);
				int ze=temp2+1;
				if((temp1>zb)&&(temp1==temp2+1)) ze=temp1+1;
				else if((temp2<0)&&(temp1>0)) ze=temp1+1;
				if(ze<=0) ze=text.length();
				for(i=zb;Character.isWhitespace(text.charAt(i));i++){}
				if(text.charAt(i)=='#') break falsePos;
				text.delete(zb,ze);
				//x=-1;
				removed=true;
			}
			x=upper.lastIndexOf(match,x-match.length());
		}
		if(removed) F.saveRaw(text);
		return removed;
	}
}