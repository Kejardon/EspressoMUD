package com.planet_ink.coffee_mud.core.interfaces;

import java.util.*;
/*
	Copyright 2011 Kejardon
*/
public interface CMReadable extends CMObject
{
	public boolean isReadable();
	public String readableText();
	public void setReadableText(String S);
}
