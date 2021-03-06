/*
   Copyright 2012-2016 Michael Pozhidaev <michael.pozhidaev@gmail.com>
   Copyright 2015-2016 Roman Volovodov <gr.rPman@gmail.com>

   This file is part of LUWRAIN.

   LUWRAIN is free software; you can redistribute it and/or
   modify it under the terms of the GNU General Public
   License as published by the Free Software Foundation; either
   version 3 of the License, or (at your option) any later version.

   LUWRAIN is distributed in the hope that it will be useful,
   but WITHOUT ANY WARRANTY; without even the implied warranty of
   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
   General Public License for more details.
*/

package org.luwrain.doctree;

import java.net.*;
import java.util.*;

import org.luwrain.core.NullCheck;
import org.luwrain.core.Log;

public class Document 
{
    static public final String DEFAULT_ITERATOR_INDEX_PROPERTY = "defaultiteratorindex";

    private final Properties props = new Properties();
    private String title;
    private String[] hrefs;

    private Node root;

    //    public HashMap<String,Node> idx=new HashMap<String,Node>();

    public Document(Node root)
    {
	NullCheck.notNull(root, "root");
	this.root = root;
	this.title = "";
    }

    public Document(String title, Node root)
    {
	NullCheck.notNull(root, "root");
	NullCheck.notNull(title, "title");
	this.root = root;
	this.title = title;
    }

    public void commit()
    {
	int deleted = 0;
	do {
	root.setEmptyMark();
deleted = root.prune();
Log.debug("doctree", "prune pass: " + deleted + " deleted");
	} while (deleted > 0);
	root.preprocess();
    }

    public void setProperty(String propName, String value)
    {
	NullCheck.notEmpty(propName, "propName");
	NullCheck.notNull(value, "value");
	props.setProperty(propName, value);
    }

    public String getProperty(String propName)
    {
	NullCheck.notEmpty(propName, "propName");
	final String res = props.getProperty(propName);
	return res != null?res:"";
    }

    public void setHrefs(String[] hrefs)
    {
	NullCheck.notNullItems(hrefs, "hrefs");
	this.hrefs = hrefs;
    }

    public URL getUrl()
    {
	final String value = getProperty("url");
	if (value.isEmpty())
	    return null;
	try {
	    return new URL(value);
	}
	catch(MalformedURLException e)
	{
	    return null;
	}
    }

    public String getTitle() 
    {
	return title != null?title:""; 
    }

    public Node getRoot()
    {
	return root; 
    }

    public String[] getHrefs()
    {
	return hrefs;
    }
}
