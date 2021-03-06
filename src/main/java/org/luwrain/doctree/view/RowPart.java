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

package org.luwrain.doctree.view;

import org.luwrain.core.*;
import org.luwrain.doctree.*;

class RowPart
{
    /** The run this part is associated with*/
    final Run run;

    /** Starting position in the text of the corresponding run*/
    final int posFrom;

    /** Ending position in the text of the corresponding run*/
    final int posTo;

    /** Index in the corresponding paragraph*/
    final int relRowNum;

    /** Absolute row index in the document*/
    int absRowNum = 0;

    RowPart(Run run, int posFrom, int posTo,
	    int relRowNum)
    {
	NullCheck.notNull(run, "run");
	this.run = run;
	this.posFrom = posFrom;
	this.posTo = posTo;
	this.relRowNum = relRowNum;
    }

    String getText()
    {
	return run.text().substring(posFrom, posTo);
    }

    //Checks relRowNum and parents of runs
    boolean onTheSameRow(RowPart rowPart)
    {
	NullCheck.notNull(rowPart, "rowPart");
	return run.getParentNode() == rowPart.run.getParentNode() && relRowNum == rowPart.relRowNum;
    }
}
