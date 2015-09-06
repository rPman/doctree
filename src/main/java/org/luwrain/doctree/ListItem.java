/*
   Copyright 2012-2015 Michael Pozhidaev <michael.pozhidaev@gmail.com>
   Copyright 2015 Roman Volovodov <gr.rPman@gmail.com>

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

public class ListItem extends NodeImpl
{
    ListItem()
    {
	super(Node.LIST_ITEM);
    }

    public int getListItemIndexOfCurrentParaContainer()
    {
	return getIndexInParentSubnodes();
    }

    public boolean isListOfCurrentParaContainerOrdered()
    {
	return parentNode.type == Node.ORDERED_LIST;
    }
}
