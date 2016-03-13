
package org.luwrain.doctree;

import java.util.*;
import java.net.*;

public interface Book
{
    Document[] getDocuments();
    Map<URL, Document> getDocumentsWithUrls();
    Document getStartingDocument();
    Document openHref(String href);
    AudioInfo findAudioForId(String ids);
    String findTextForAudio(String audioFileUrl, long msec);
    //Expecting that href is absolute
    Document getDocument(String href);
}
