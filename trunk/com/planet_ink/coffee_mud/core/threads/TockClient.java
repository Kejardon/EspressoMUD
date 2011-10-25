package com.planet_ink.coffee_mud.core.threads;
import com.planet_ink.coffee_mud.core.interfaces.*;


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
public class TockClient implements Comparable<TockClient>
{
	public final Tickable clientObject;
//	public final Tickable.TickID tickID;	//Actually this'll just always be Tickable.TickID.Action I think
	public final long nextAction;

	public TockClient(Tickable newClientObject,
//						Tickable.TickID newTickID,
						long next)
	{
		clientObject=newClientObject;
//		tickID=newTickID;
		nextAction=next;
	}

	public boolean equals(Object obj)
	{
		if(obj instanceof TockClient)
			return compareTo((TockClient)obj)==0;
		return false;
	}

	public int compareTo(TockClient arg0)
	{
		if(clientObject != arg0.clientObject)
			return (int)(nextAction - arg0.nextAction);
		return 0;
	}
}
