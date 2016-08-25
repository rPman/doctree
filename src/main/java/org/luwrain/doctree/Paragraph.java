/*
   Copyright 2012-2016 Michael Pozhidaev <michael.pozhidaev@gmail.com>
   Copyright 2015-2016 Roman Volovodov <gr.rPman@gmail.com>

   This file is part of the LUWRAIN.

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

public class Paragraph extends NodeImpl
{
    public Run[] runs = new Run[0];
    RowPart[] rowParts = new RowPart[0];

    /** Position of the first row in a document*/
    //    int topRowIndex = -1;

    Paragraph()
    {
	super(Node.Type.PARAGRAPH);
    }

    public boolean hasSingleLineOnly()
    {
	return height == 1;
    }

    @Override void commit()
    {
	subnodes = null;
	if (runs == null)
	    runs = new Run[0];
	if (titleRun != null)
	    titleRun.setParentNode(this);
	for(Run r: runs)
	{
	    r.setParentNode(this);
	    r.prepareText();
	}
    }

    @Override void setEmptyMark()
    {
	empty = true;
	if (runs == null || runs.length < 1)
	    return;
	for(Run r: runs)
	    if (!r.toString().trim().isEmpty())
		empty = false;
    }

    @Override void removeEmpty()
    {
	if (runs == null)
	    return;
	int k = 0;
	for(int i = 0;i < runs.length;++i)
	    if (runs[i].isEmpty() )
		++k; else
		runs[i - k] = runs[i];
	if (k > 0)
	{
	    final int count = runs.length - k;
	    Run[] newRuns = new Run[count];
	    for(int i = 0;i < count;++i)
		newRuns[i] = runs[i];
	    runs = newRuns;
	}
    }

    public RowPart[] getRowParts()
    {
	return rowParts != null?rowParts:new RowPart[0];
    }

    int getParaIndex()
    {
	return getIndexInParentSubnodes();
    }

    @Override public String toString()
    {
	if (runs == null)
	    return "";
	final StringBuilder sb = new StringBuilder();
	for(Run r: runs)
	    sb.append(r.toString());
	return sb.toString();
    }

    public Run[] runs()
    {
	return runs != null?runs:new Run[0];
    }
}