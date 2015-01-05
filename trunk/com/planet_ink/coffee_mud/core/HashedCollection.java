
/*
EspressoMUD copyright 2015 Kejardon

Licensed under the Apache License, Version 2.0. You may obtain a copy of the license at
	http://www.apache.org/licenses/LICENSE-2.0
*/
package com.planet_ink.coffee_mud.core;

import java.util.HashMap;
import java.util.Iterator;

public class HashedCollection<A> extends HashMap<A,A> implements Iterable<A>
{

    @Override public Iterator<A> iterator() {
        return keySet().iterator();
    }

    public boolean contains(Object o) {
        return containsKey(o);
    }

    public boolean add(A e) {
        return super.put(e, e)==null;
    }
	
	@Override public A put(A key, A value) {
		throw new UnsupportedOperationException("HashedCollection should use add(Object e) instead of put.");
	}
}
