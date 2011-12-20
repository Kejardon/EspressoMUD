package com.planet_ink.coffee_mud.core;
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

/*
CoffeeMUD 5.6.2 copyright 2000-2010 Bo Zimmerman
EspressoMUD copyright 2011 Kejardon

Licensed under the Apache License, Version 2.0. You may obtain a copy of the license at
	http://www.apache.org/licenses/LICENSE-2.0
*/
@SuppressWarnings("unchecked")
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

	private DVector resources=new DVector(3);

	public static void clearResources(){instance()._clearResources();}
	public static void removeResource(String ID){ instance()._removeResource(ID);}
	public static Vector findResourceKeys(String srch){return instance()._findResourceKeys(srch);}
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
		return "\n\r";
	}
	
	public static Vector getFileLineVector(StringBuffer buf)
	{
		Vector V=new Vector();
		if(buf==null) return V;
		StringBuffer str=new StringBuffer("");
		for(int i=0;i<buf.length();i++)
		{
			if(((buf.charAt(i)=='\n')&&(i<buf.length()-1)&&(buf.charAt(i+1)=='\r'))
			   ||((buf.charAt(i)=='\r')&&(i<buf.length()-1)&&(buf.charAt(i+1)=='\n')))
			{
				i++;
				V.addElement(str.toString());
				str.setLength(0);
			}
			else
			if(((buf.charAt(i)=='\r'))
			||((buf.charAt(i)=='\n')))
			{
				V.addElement(str.toString());
				str.setLength(0);
			}
			else
				str.append(buf.charAt(i));
		}
		if(str.length()>0)
			V.addElement(str.toString());
		V.trimToSize();
		return V;
	}

	public static String buildResourcePath(String path)
	{
		if((path==null)||(path.length()==0)) return "resources/";
		return "resources/"+path+"/";
	}

	public static void updateMultiList(String filename, Hashtable lists)
	{
		StringBuffer str=new StringBuffer("");
		for(Enumeration e=lists.keys();e.hasMoreElements();)
		{
			String ml=(String)e.nextElement();
			Vector V=(Vector)lists.get(ml);
			str.append(ml+"\r\n");
			if(V!=null)
			for(int v=0;v<V.size();v++)
				str.append(((String)V.elementAt(v))+"\r\n");
			str.append("\r\n");
		}
		new CMFile(filename,null,false).saveText(str);
	}

	public static Hashtable getMultiLists(String filename)
	{
		Hashtable oldH=new Hashtable();
		Vector V=new Vector();
		try{
			V=getFileLineVector(new CMFile("resources/"+filename,null,false).text());
		}catch(Exception e){}
		if((V!=null)&&(V.size()>0))
		{
			String journal="";
			Vector set=new Vector();
			for(int v=0;v<V.size();v++)
			{
				String s=(String)V.elementAt(v);
				if(s.trim().length()==0)
					journal="";
				else
				if(journal.length()==0)
				{
					journal=s;
					set=new Vector();
					oldH.put(journal,set);
				}
				else
					set.addElement(s);
			}
		}
		return oldH;
	}

	public static String makeFileResourceName(String filename)
	{
		return "resources/"+filename;
	}

	public void _clearResources()
	{
		synchronized(resources)
		{
			resources.clear();
		}
	}

	public Vector _findResourceKeys(String srch)
	{
		synchronized(resources)
		{
			Vector V=new Vector();
			for(int i=0;i<resources.size();i++)
			{
				String key=(String)resources.elementAt(i,1);
				if((srch.length()==0)||(key.toUpperCase().indexOf(srch.toUpperCase())>=0))
					V.addElement(key);
			}
			return V;
		}
	}

	private int _getResourceIndex(String ID)
	{
		// protected elsewhere
		if(resources.size()==0) return -1;
		int start=0;
		int end=resources.size()-1;
		while(start<=end)
		{
			int mid=(end+start)/2;
			int comp=((String)resources.elementAt(mid,1)).compareToIgnoreCase(ID);
			if(comp==0)
				return mid;
			else
			if(comp>0)
				end=mid-1;
			else
				start=mid+1;

		}
		return -1;
	}
	
	public Object _getResource(String ID)
	{
		synchronized(resources)
		{
			// protected elsewhere
			int x = _getResourceIndex(ID);
			if((x<resources.size())&&(x>=0))
				return resources.elementAt(x,2);
			return null;
		}
	}

	public static Object prepareObject(Object obj)
	{
		if(obj instanceof Vector) ((Vector)obj).trimToSize();
		if(obj instanceof DVector) ((DVector)obj).trimToSize();
		return obj;
	}

	public void _submitResource(String ID, Object obj)
	{
		synchronized(resources)
		{
			int properIndex=-1;
			if(ID.length()==0) 
				properIndex=0;
			else
			if(resources.size()>0)
			{
				int start=0;
				int end=resources.size()-1;
				int mid=0;
				while(start<=end)
				{
					mid=(end+start)/2;
					int comp=((String)resources.elementAt(mid,1)).compareToIgnoreCase(ID);
					if(comp==0) 
						return;
					else
					if(comp>0)
						end=mid-1;
					else
						start=mid+1;
				}
				if(end<0) 
					properIndex=0;
				else
				if(start>=resources.size()) 
					properIndex=resources.size()-1;
				else
					properIndex=mid;
			}
			Object prepared=prepareObject(obj);
			Boolean preparedB=new Boolean(prepared!=obj);
			if(properIndex<0)
				resources.addElement(ID,prepared,preparedB);
			else
			{
				int comp=((String)resources.elementAt(properIndex,1)).compareToIgnoreCase(ID);
				if(comp>0)
					resources.insertElementAt(properIndex,ID,prepared,preparedB);
				else
				if(properIndex==resources.size()-1)
					resources.addElement(ID,prepared,preparedB);
				else
					resources.insertElementAt(properIndex+1,ID,prepared,preparedB);
			}
		}
	}

	private Object _updateResource(String ID, Object obj)
	{
		synchronized(resources)
		{
			int index=_getResourceIndex(ID);
			if(index<0) return null;
			Object prepared=prepareObject(obj);
			resources.setElementAt(index,2,prepared);
			resources.setElementAt(index,3,new Boolean(prepared!=obj));
			return prepared;
		}
	}

	public void _removeResource(String ID)
	{
		synchronized(resources)
		{
			try{
				int index=_getResourceIndex(ID);
				if(index<0) return;
				resources.removeElementAt(index);
			}catch(ArrayIndexOutOfBoundsException e){}
		}
	}

	public boolean _isFileResource(String filename)
	{
		if(_getResource(filename)!=null) return true;
		if(new CMFile(makeFileResourceName(filename),null,false).exists())
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
		StringBuffer buf=new CMFile(makeFileResourceName(filename),null,reportErrors).text();
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
		filename="//"+filename;
		return new CMFile(filename,whom,false).saveRaw(myRsc);
	}

	public boolean _findRemoveProperty(CMFile F, String match)
	{
		boolean removed=false;
		StringBuffer text=F.textUnformatted();
		int x=text.toString().toUpperCase().indexOf(match.toUpperCase());
		while(x>=0)
		{
			if(((x==0)||(!Character.isLetterOrDigit(text.charAt(x-1))))
			&&(text.substring(x+match.length()).trim().startsWith("=")))
			{
				int zb1=text.lastIndexOf("\n",x);
				int zb2=text.lastIndexOf("\r",x);
				int zb=(zb2>zb1)?zb2:zb1;
				if(zb<0) zb=0; else zb++;
				int ze1=text.indexOf("\n",x);
				int ze2=text.indexOf("\r",x);
				int ze=ze2+1;
				if((ze1>zb)&&(ze1==ze2+1)) ze=ze1+1;
				else
				if((ze2<0)&&(ze1>0)) ze=ze1+1;
				if(ze<=0) ze=text.length();
				if(!text.substring(zb).trim().startsWith("#"))
				{
					text.delete(zb,ze);
					x=-1;
					removed=true;
				}
			}
			x=text.toString().toUpperCase().indexOf(match.toUpperCase(),x+1);
		}
		if(removed) F.saveRaw(text);
		return removed;
	}
}