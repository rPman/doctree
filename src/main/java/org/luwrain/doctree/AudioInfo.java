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

import java.util.regex.*;
import java.net.*;

import org.luwrain.core.NullCheck;

public class AudioInfo
{
    private String src;
private long beginPos;
private long endPos;

    public AudioInfo(String src,
long beginPos)
    {
	NullCheck.notNull(src, "src");
	this.src = src;
	this.beginPos = beginPos;
	this.endPos = -1;
    }

    public AudioInfo(String src)
    {
	NullCheck.notNull(src, "src");
	this.src = src;
	this.beginPos = -1;
	this.endPos = -1;
    }

    public AudioInfo(String src,
		     long beginPos, long endPos)
    {
	NullCheck.notNull(src, "src");
	this.src = src;
	this.beginPos = beginPos;
	this.endPos = endPos;
    }

    public String src() {return src;}
    public long beginPosMsec() {return beginPos;}
    public long endPosMsec() {return endPos;}

    public boolean covers(String audioFileUrl, long msec)
    {
	if (!src.equals(audioFileUrl))
	    return false;
	if (endPos < 0)
	    return msec >= beginPos;
	return msec >= beginPos && msec <= endPos;
    }

    public boolean covers(String audioFileUrl, long msec, URL baseUrl)
    {
	NullCheck.notNull(baseUrl, "baseUrl");
	try {
	    if (!(new URL(baseUrl, src).toString()).equals(new URL(baseUrl,audioFileUrl).toString()))
		return false;
	}
	catch(MalformedURLException e)
	{
	    e.printStackTrace();
	    return false;
	}
	if (endPos < 0)
	    return msec >= beginPos;
	return msec >= beginPos && msec <= endPos;
    }


    @Override public String toString()
    {
	return "Audio: " + src + " (from " + beginPos + ", to " + endPos + ")";
    }
}
