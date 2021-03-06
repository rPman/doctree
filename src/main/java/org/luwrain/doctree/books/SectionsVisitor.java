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

package org.luwrain.doctree.books;

import java.util.*;

import org.luwrain.core.*;
import org.luwrain.doctree.*;

class SectionsVisitor implements Visitor
{
    private final LinkedList<Book.Section> sections = new LinkedList<Book.Section>();

    @Override public void visit(Section node)
    {
	NullCheck.notNull(node, "node");
	final LinkedList<String> hrefs = new LinkedList<String>();
	collectHrefs(node, hrefs);
	if (!hrefs.isEmpty())
	    sections.add(new Book.Section(node.getSectionLevel(), node.getCompleteText().trim(), hrefs.getFirst()));
    }

    Book.Section[] getBookSections()
    {
	return sections.toArray(new Book.Section[sections.size()]);
    }

    @Override public void visitNode(Node node) {}
    @Override public void visit(ListItem node) {}
    @Override public void visit(Paragraph node) {}

    @Override public void visit(TableCell node) {}
    @Override public void visit(Table node) {}
    @Override public void visit(TableRow node) {}

    static private void collectHrefs(Node node, LinkedList<String> hrefs)
    {
	NullCheck.notNull(node, "node");
	NullCheck.notNull(hrefs, "hrefs");
	if (node instanceof Paragraph)
	{
	    final Paragraph para = (Paragraph)node;
	    if (para.runs != null)
		for(Run r: para.runs)
		    if (r.href() != null && !r.href().isEmpty())
			hrefs.add(r.href());
	} else
	    for(Node n: node.getSubnodes())
		    collectHrefs(n, hrefs);
    }
}
