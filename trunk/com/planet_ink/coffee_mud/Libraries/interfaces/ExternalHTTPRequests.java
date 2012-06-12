package com.planet_ink.coffee_mud.Libraries.interfaces;
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

import com.planet_ink.coffee_mud.core.exceptions.*;

import java.util.*;
import java.nio.ByteBuffer;
import java.util.concurrent.CopyOnWriteArrayList;
import java.net.InetAddress;

/*
CoffeeMUD 5.6.2 copyright 2000-2010 Bo Zimmerman
EspressoMUD copyright 2011 Kejardon

Licensed under the Apache License, Version 2.0. You may obtain a copy of the license at
	http://www.apache.org/licenses/LICENSE-2.0
*/
@SuppressWarnings("unchecked")
public interface ExternalHTTPRequests extends CMLibrary
{
	public byte [] doVirtualPage(byte [] data)
		throws HTTPRedirectException;
	public String doVirtualPage(String s) 
		throws HTTPRedirectException;
	public StringBuffer doVirtualPage(StringBuffer s) 
		throws HTTPRedirectException;
	public String ServerVersionString();
	public String getWebServerPortStr();
	public int getWebServerPort();
	public String getHTTPstatus();
	public String getHTTPstatusInfo();
	public String getHTTPclientIP();
	public String getWebServerPartialName();
	public CMFile grabFile(String filename);
	public String getMimeType(String a_extension);
	public MudHost getMUD();
	public boolean isRequestParameter(String key);
	public String getRequestParameter(String key);
	public void removeRequestParameter(String key);
	public void addRequestParameters(String key, String value);
	public Vector getAllRequestParameterKeys(String keyMask);
	//public Hashtable getVirtualDirectories();
	public String getRequestEncodedParameters();
	public InetAddress getServerAddress();
	public String getPageContent(String filename);
	public Hashtable getRequestObjects();
}
