
package org.luwrain.doctree.books; 

import java.net.*;
import java.util.*;
import java.io.*;
import java.nio.file.*;

import org.luwrain.core.NullCheck;
import org.luwrain.core.Log;
import org.luwrain.doctree.*;
import org.luwrain.util.*;

class Daisy2 implements Book
{
    private final HashMap<URL, Document> docs = new HashMap<URL, Document>();
    private final HashMap<URL, Smil.Entry> smils = new HashMap<URL, Smil.Entry>();
    private Document nccDoc;

    @Override public Document[] getDocuments()
    {
	final LinkedList<Document> res = new LinkedList<Document>();
	for(Map.Entry<URL, Document> e: docs.entrySet())
	    res.add(e.getValue());
	return res.toArray(new Document[res.size()]);
    }

    @Override public Map<URL, Document> getDocumentsWithUrls()
    {
	return docs;
    }

    @Override public Document getStartingDocument()
    {
	return nccDoc;
    }

    @Override public Document getDocument(String href)
    {
	NullCheck.notNull(href, "href");
	URL url, noRefUrl;
	try {
	    url = new URL(href);
	    noRefUrl = new URL(url.getProtocol(), url.getHost(), url.getPort(), url.getFile());
	}
	catch(MalformedURLException e)
	{
	    e.printStackTrace();
	    return null;
	}
	if (smils.containsKey(noRefUrl))
	{
	    final Smil.Entry entry = smils.get(noRefUrl);
	    final Smil.Entry requested = entry.findById(url.getRef());
	    if (requested != null)
	    {
		if (requested.type() == Smil.Entry.Type.TEXT)
		return getDocument(requested.src()); else
		{
		    Log.warning("doctree-daisy", "URL " + href + " points to a SMIL entry, but its type is " + requested.type());
		    return null;
		}
	    }
	} //smils;
    if (docs.containsKey(noRefUrl))
    {
	final Document res = docs.get(noRefUrl);
	return res;
    }
	return null;
    }

    @Override public Document openHref(String href)
    {
	return null;
    }

    @Override public AudioInfo findAudioForId(String id)
    {
	NullCheck.notNull(id, "id");
	Log.debug("doctree-daisy", "searching audio for " + id);
	for(Map.Entry<URL, Smil.Entry> e: smils.entrySet())
	{
	    final Smil.Entry entry = findSmilEntryWithText(e.getValue(), id);
	    if (entry != null)
	    {
		System.out.println(entry.id());
		final LinkedList<AudioInfo> infos = new LinkedList<AudioInfo>();
		collectAudioStartingAtEntry(entry, infos);
		System.out.println("" + infos.size() + " collected");
		if (infos.size() > 0)
		    return infos.getFirst();
	    }
	}
	return null;
    }

@Override public     String findTextForAudio(String audioFileUrl, long msec)
    {
	NullCheck.notNull(audioFileUrl, "audioFileUrl");
	Log.debug("doctree-daisy", "text for " + audioFileUrl + " at " + msec);
	for(Map.Entry<URL, Smil.Entry> e: smils.entrySet())
	{
	    final Smil.Entry entry = findSmilEntryWithAudio(e.getValue(), audioFileUrl, msec);
	    if (entry != null)
	    {
		System.out.println(entry.id());
		final LinkedList<String> links = new LinkedList<String>();
		collectTextStartingAtEntry(entry, links);
		System.out.println("" + links.size() + " linkscollected");
		if (links.size() > 0)
		    return links.getFirst();
	    }
	}
	return null;
    }

    void init(Document nccDoc)
    {
	NullCheck.notNull(nccDoc, "nccDoc");
	final String[] allHrefs = nccDoc.getHrefs();
	final LinkedList<String> textSrcs = new LinkedList<String>();
	for(String h: allHrefs)
	    try {
URL url = new URL(h);
url = new URL(url.getProtocol(), url.getHost(), url.getPort(), url.getFile());
		if (url.getFile().toLowerCase().endsWith(".smil"))
		    loadSmil(url, textSrcs); else
		    textSrcs.add(url.toString());
	    }
	    catch(MalformedURLException e)
	    {
		e.printStackTrace();
	    }
	Log.debug("doctree-daisy", "" + smils.size() + " SMIL(s) loaded");

	for(String s: textSrcs)
	    try {
URL url = new URL(s);
url = new URL(url.getProtocol(), url.getHost(), url.getPort(), url.getFile());
		    loadDoc(url);
	    }
	    catch(MalformedURLException e)
	    {
		e.printStackTrace();
	    }
	Log.debug("doctree-daisy", "" + docs.size() + " documents loaded");
	this.nccDoc = nccDoc;
    }

    private void loadSmil(URL url, LinkedList<String> textSrcs)
    {
	if (smils.containsKey(url))
	    return;
	Log.debug("doctree-daisy", "reading SMIL " + url.toString());
	final Smil.Entry smil = Smil.fromUrl(url, url);
	smils.put(url, smil);
	smil.saveTextSrc(textSrcs);
    }

    private void loadDoc(URL url)
    {
	if (docs.containsKey(url))
	    return;
	Result res;
	try {
	    res = Factory.fromUrl(url, "", "");
	}
	catch(Exception e)
	{
	    Log.error("doctree-daisy", "unable to read a document from URL " + url.toString());
	    e.printStackTrace();
	    return;
	}
	if (res.type() != Result.Type.OK)
	{
	    Log.warning("doctree-daisy", "unable to load a document by URL " + url + ":" + res.toString());
	    return;
	}
	if (res.book() != null)
	{
	    Log.debug("doctree-daisy", "the URL " + url + "references a book, not including to current one");
	    return;
	}
	docs.put(url, res.doc());
    }

    static private Smil.Entry findSmilEntryWithText(Smil.Entry entry, String src)
    {
	NullCheck.notNull(entry, "entry");
	NullCheck.notNull(src, "src");
	switch(entry.type() )
	{
	case TEXT:
	    return (entry.src() != null && entry.src().equals(src))?entry:null;
	case AUDIO:
	    return null;
	case FILE:
	case SEQ:
	    if (entry.entries() == null)
		return null;
	    for (int i = 0;i < entry.entries().length;++i)
	    {
		final Smil.Entry res = findSmilEntryWithText(entry.entries()[i], src);
		if (res == null)
		    continue;
		if (res != entry.entries()[i])
		    return res;
		//		System.out.println("res.id=" + res.id());
		if (i == 0)
		    return entry;
		return entry.entries()[i];
	    }
	    return null;
	case PAR:
	    if (entry.entries() == null)
		return null;
	    for(Smil.Entry e: entry.entries())
	    {
		final Smil.Entry res = findSmilEntryWithText(e, src);
		if (res != null)
		    return entry;
	    }
	    return null;
	default:
	    Log.warning("doctree-daisy", "unknown SMIL entry type:" + entry.type());
	    return null;
	}
    }

    static private Smil.Entry findSmilEntryWithAudio(Smil.Entry entry, String audioFileUrl, long msec)
    {
	NullCheck.notNull(entry, "entry");
	NullCheck.notNull(audioFileUrl, "audioFileUrl");
	switch(entry.type() )
	{
	case AUDIO:
	    return entry.getAudioInfo().covers(audioFileUrl, msec)?entry:null;
	case TEXT:
	    return null;
	case FILE:
	case SEQ:
	    if (entry.entries() == null)
		return null;
	    for (int i = 0;i < entry.entries().length;++i)
	    {
		final Smil.Entry res = findSmilEntryWithAudio(entry.entries()[i], audioFileUrl, msec);
		if (res == null)
		    continue;
		if (res != entry.entries()[i])
		    return res;
		//		System.out.println("res.id=" + res.id());
		if (i == 0)
		    return entry;
		return entry.entries()[i];
	    }
	    return null;
	case PAR:
	    if (entry.entries() == null)
		return null;
	    for(Smil.Entry e: entry.entries())
	    {
		final Smil.Entry res = findSmilEntryWithAudio(e, audioFileUrl, msec);
		if (res != null)
		    return entry;
	    }
	    return null;
	default:
	    Log.warning("doctree-daisy", "unknown SMIL entry type:" + entry.type());
	    return null;
	}
    }

    static private void collectAudioStartingAtEntry(Smil.Entry entry, LinkedList<AudioInfo> audioInfos)
    {
	NullCheck.notNull(entry, "entry");
	NullCheck.notNull(audioInfos, "audioInfos");
	switch(entry.type())
	{
	case AUDIO:
	    audioInfos.add(entry.getAudioInfo());
	    return;
	case TEXT:
	    return;
	case PAR:
	    if (entry.entries() != null)
		for(Smil.Entry e: entry.entries())
		    collectAudioStartingAtEntry(e, audioInfos);
	    return;
	case FILE:
	case SEQ:
	    if (entry.entries() != null &&
		 entry.entries().length >= 1)
		collectAudioStartingAtEntry(entry.entries()[0], audioInfos);
	    return;
	default:
	    Log.warning("doctree-daisy", "unknown SMIL entry type:" + entry.type());
	}
    }

    static private void collectTextStartingAtEntry(Smil.Entry entry, LinkedList<String> links)
    {
	NullCheck.notNull(entry, "entry");
	NullCheck.notNull(links, "links");
	switch(entry.type())
	{
	case AUDIO:
	    return;
	case TEXT:
	    links.add(entry.src());
	    return;
	case PAR:
	    if (entry.entries() != null)
		for(Smil.Entry e: entry.entries())
		    collectTextStartingAtEntry(e, links);
	    return;
	case FILE:
	case SEQ:
	    if (entry.entries() != null &&
		 entry.entries().length >= 1)
		collectTextStartingAtEntry(entry.entries()[0], links);
	    return;
	default:
	    Log.warning("doctree-daisy", "unknown SMIL entry type:" + entry.type());
	}
    }

}