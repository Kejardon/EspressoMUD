package com.planet_ink.coffee_mud.core.database;
import com.planet_ink.coffee_mud.core.interfaces.*;
import com.planet_ink.coffee_mud.core.*;
import com.planet_ink.coffee_mud.Effects.interfaces.*;
import com.planet_ink.coffee_mud.Areas.interfaces.*;
import com.planet_ink.coffee_mud.Behaviors.interfaces.*;
import com.planet_ink.coffee_mud.Commands.interfaces.*;
import com.planet_ink.coffee_mud.Common.interfaces.*;
import com.planet_ink.coffee_mud.Exits.interfaces.*;
import com.planet_ink.coffee_mud.Items.interfaces.*;
import com.planet_ink.coffee_mud.Libraries.interfaces.DatabaseEngine.PlayerData;
import com.planet_ink.coffee_mud.Locales.interfaces.*;
import com.planet_ink.coffee_mud.MOBS.interfaces.*;
import com.planet_ink.coffee_mud.Races.interfaces.*;

import java.sql.*;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


/*
   Copyright 2000-2010 Bo Zimmerman

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

	   http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
*/
@SuppressWarnings("unchecked")
public class DataLoader
{
	protected DBConnector DB=null;
	public DataLoader(DBConnector newDB)
	{
		DB=newDB;
	}
	public String DBRead(String ID)
	{
		DBConnection D=null;
		String data=null;
		try
		{
			D=DB.DBFetch();
			ResultSet R=null;
			R=D.query("SELECT * FROM CMMISC WHERE CMIDNT='"+ID+"'");
			if(R.next())
				data=DBConnections.getRes(R,"CMDATA");
		}
		catch(Exception sqle)
		{
			Log.errOut("DataLoader",sqle);
		}
		if(D!=null) DB.DBDone(D);
		// log comment
		return data;
	}

	public Vector DBRead(Vector sections)
	{
		DBConnection D=null;
		Vector rows=new Vector();
		if((sections==null)||(sections.size()==0))
			return rows;
		try
		{
			D=DB.DBFetch();
			StringBuffer orClause=new StringBuffer("");
			for(int i=0;i<sections.size();i++)
				orClause.append("CMIDNT='"+((String)sections.elementAt(i))+"' OR ");
			String clause=orClause.toString().substring(0,orClause.length()-4);
			ResultSet R=D.query("SELECT * FROM CMMISC WHERE ("+clause+")");
			while(R.next())
			{
				String d=DBConnections.getRes(R,"CMDATA");
				rows.addElement(d);
			}
		}
		catch(Exception sqle)
		{
			Log.errOut("DataLoader",sqle);
		}
		if(D!=null) DB.DBDone(D);
		// log comment
		return rows;
	}

	
	public void DBUpdate(String key, String data)
	{
		DB.update("UPDATE CMMISC SET CMDATA='"+data+"' WHERE CMIDNT='"+key+"'");
	}
	
	public void DBDelete(String ID)
	{
		DBConnection D=null;
		try
		{
			D=DB.DBFetch();
			D.update("DELETE FROM CMMISC WHERE CMIDNT='"+ID+"'",0);
		}
		catch(Exception sqle)
		{
			Log.errOut("DataLoader",sqle);
		}
		finally
		{
			if(D!=null) 
				DB.DBDone(D);
		}
	}

	public void DBCreate(String key, String data)
	{
		DB.update(
		 "INSERT INTO CMMISC ("
		 +"CMIDNT, "
		 +"CMDATA "
		 +") values ("
		 +"'"+key+"',"
		 +"'"+data+"'"
		 +")");
	}
	
}
