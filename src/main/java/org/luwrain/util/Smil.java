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

package org.luwrain.util;

import java.util.*;
import java.util.regex.*;
import java.net.*;
import java.io.*;
import java.nio.file.*;

import org.jsoup.*;
import org.jsoup.nodes.*;
import org.jsoup.select.*;
import org.jsoup.parser.*;

import org.luwrain.core.*;
import org.luwrain.doctree.AudioInfo;

public class Smil
{
    static public class Entry
    {
	public enum Type {SEQ, PAR, AUDIO, TEXT, FILE};

	private Type type;
	private String src = "";
	private String id = "";
	private AudioInfo audioInfo = null;
	Entry[] entries;

	Entry(Type type)
	{
	    NullCheck.notNull(type, "type");
	    this.type = type;
	}

	Entry (Type type,
	       String id, String src)
	{
	    NullCheck.notNull(type, "type");
	    NullCheck.notNull(id, "id");
	    NullCheck.notNull(src, "src");
	    this.type = type;
	    this.id = id;
	    this.src = src;
	}

	Entry (String id, String src,
AudioInfo audioInfo)
	{
	    NullCheck.notNull(id, "id");
	    NullCheck.notNull(src, "src");
	    NullCheck.notNull(audioInfo, "audioInfo");
	    this.type = Type.AUDIO;
	    this.id = id;
	    this.src = src;
	    this.audioInfo = audioInfo;
	}

	public void saveTextSrc(List<String> res)
	{
	    if (type == Type.TEXT &&
		src != null && !src.isEmpty())
		res.add(src);
	    if (entries != null)
	    for(Entry e: entries)
		e.saveTextSrc(res);
	}

	public void allSrcToUrls(URL base) throws MalformedURLException
	{
	    NullCheck.notNull(base, "base");
	    if (src != null && !src.isEmpty())
src = new URL(base, src).toString();
	    if (entries != null)
	    for(Entry e: entries)
		e.allSrcToUrls(base);
	}


	public Entry findById(String id)
	{
	    NullCheck.notNull(id, "id");
	    if (this.id != null && this.id.equals(id))
		return this;
	    if (entries == null)
		return null;
	    for(Entry e: entries)
	    {
		final Entry res = e.findById(id);
		if (res != null)
		    return res;
	    }
	    return null;
	}

	public AudioInfo getAudioInfo()
	{
	    return audioInfo;
	}

	public Type type(){return type;}
	public Entry[] entries(){return entries;}
	public String src() {return src;}
	public String id() {return id;}
    }

    static public class File extends Entry
    {
	File()
	{
	    super(Type.FILE);
	}
    }

    static public Entry fromUrl(URL url)
    {
	NullCheck.notNull(url, "url");
	try {
	if (url.getProtocol().equals("file"))
	{
	    final Path tmpFile = Files.createTempFile("lwr-smil", "");
	    try {
		Files.copy(url.openStream(), tmpFile, StandardCopyOption.REPLACE_EXISTING);
	    return fromPath(tmpFile);
	    }
	    finally {
		Files.delete(tmpFile);
	    }
	}
	}
	catch(IOException e)
	{
	    Log.error("docree-smil", "unable to read SMIL from file: URL:" + e.getClass().getName() + ":" + e.getMessage());
	    e.printStackTrace();
	    return null;
	}
	final org.jsoup.nodes.Document doc;
	try {
	    final Connection con=Jsoup.connect(url.toString());
	    con.userAgent(org.luwrain.doctree.loading.UrlLoader.USER_AGENT);
	    con.timeout(30000);
	    doc = con.get();
	}
	catch(Exception e)
	{
	    Log.error("doctree-smil", "unable to fetch SMIL from URL " + url.toString() + ":" + e.getClass().getName() + ":" + e.getMessage());
	    e.printStackTrace(); 
	    return null;
	}
	final Entry res = new Entry(Entry.Type.FILE);
	    res.entries = onNode(doc.body());
	return res;
    }

    static public Entry fromPath(Path path)
    {
	NullCheck.notNull(path, "path");
	final org.jsoup.nodes.Document doc;
	try {
	    doc = Jsoup.parse(Files.newInputStream(path), "utf-8", "", Parser.xmlParser());
	}
	catch(Exception e)
	{
	    Log.error("doctree-smil", "unable to parse " + path.toString() + ":" + e.getClass().getName() + ":" + e.getMessage());
	    e.printStackTrace(); 
	    return null;
	}
	final Entry res = new Entry(Entry.Type.FILE);
	    res.entries = onNode(doc.body());
	return res;
    }

    static private Entry[] onNode(Node node)
    {
	NullCheck.notNull(node, "node");
	final LinkedList<Entry> res = new LinkedList<Entry>();
	final LinkedList<org.luwrain.doctree.Run> runs = new LinkedList<org.luwrain.doctree.Run>();
	final List<Node> childNodes = node.childNodes();
	for(Node n: childNodes)
	{
	    final String name = n.nodeName();
	    if (n instanceof TextNode)
	    {
		final TextNode textNode = (TextNode)n;
		final String text = textNode.text();
		if (!text.trim().isEmpty())
		    Log.warning("smil", "unexpected text content:" + text);
		continue;
	    }
	    if (n instanceof Element)
	    {
		final Element el = (Element)n;
		switch(name.trim().toLowerCase())
		{
		case "seq":
		    res.add(new Entry(Entry.Type.SEQ));
		    res.getLast().id = el.attr("id");
		    res.getLast().entries = onNode(el);
		    break;
		case "par":
		    res.add(new Entry(Entry.Type.PAR));
		    res.getLast().id = el.attr("id");
		    res.getLast().entries = onNode(el);
		    break;
		case "audio":
		    res.add(onAudio(el));
		    break;
		case "text":
		    res.add(onText(el));
		    break;
		default:
		    Log.warning("smil", "unknown tag:" + name);
		}
		continue;
	    }
	}
	return res.toArray(new Entry[res.size()]);
    }

    static private Entry onAudio(Element el)
    {
	NullCheck.notNull(el, "el");
	final String id = el.attr("id");
	final String src = el.attr("src");
final String beginValue = el.attr("clip-begin");
final String endValue = el.attr("clip-end");
long beginPos = -1, endPos = -1;
if (beginValue != null)
beginPos = parseTime(beginValue);
if (endValue != null)
endPos = parseTime(endValue);
return new Entry(id, src, new AudioInfo(src, beginPos, endPos));
    }

    static private Entry onText(Element el)
    {
	NullCheck.notNull(el, "el");
	final String id = el.attr("id");
	final String src = el.attr("src");
	return new Entry(Entry.Type.TEXT, id, src);
    }

	static private final Pattern TIME_PATTERN = Pattern.compile("^npt=(?<sec>\\d+.\\d+)s$");
    static private long parseTime(String value)
    {
	final Matcher m = TIME_PATTERN.matcher(value);
	if(m.matches()) 
	{
	    try {
		float f = Float.parseFloat(m.group("sec"));
					   f *= 1000;
					   return new Float(f).longValue();
	    }
	    catch(NumberFormatException e)
	    {
		e.printStackTrace();
	    }
	}
	    return -1;
    }
}
