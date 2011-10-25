package com.planet_ink.coffee_mud.core.interfaces;
import com.planet_ink.coffee_mud.core.*;

public interface Interactable extends Environmental.EnvHolder, Affectable, Behavable
{
	public String name();
	public void setName(String newName);
	public String displayText();
	public void setDisplayText(String newDisplayText);
	public String description();
	public void setDescription(String newDescription);
	public void destroy();
	public boolean amDestroyed();
	public boolean sameAs(Interactable E);
}