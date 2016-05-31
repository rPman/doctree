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

package org.luwrain.doctree.filters;

import java.util.*;
import java.util.Map.Entry;
import java.io.*;
import java.math.BigInteger;
import java.nio.file.*;

import org.luwrain.doctree.*;
import org.apache.poi.xwpf.usermodel.*;

import org.luwrain.core.NullCheck;

public class DocX
{
    private Path path;
    private String wholeText;
    private final HashMap<BigInteger, HashMap<Integer, Integer>> listInfo = new HashMap<BigInteger, HashMap<Integer, Integer>>();
    private int lastLvl = -1;

    public DocX(Path path)
    {
	NullCheck.notNull(path, "path");
	this.path = path;
    }

    public org.luwrain.doctree.Document read()
    {
    	try
    	{
	    final InputStream s = Files.newInputStream(path);
	    XWPFDocument doc = new XWPFDocument(s);
	    final org.luwrain .doctree.Document res = transform(doc);
	    res.setFormat("DOCX");
	    res.setUrl(path.toUri().toURL());
	    s.close();
	    return res;
    	} catch (IOException e)
    	{
	    e.printStackTrace();
	    return null;
	}
    }

    private org.luwrain.doctree.Document transform(XWPFDocument doc)
    {
	NullCheck.notNull(doc, "doc");
	wholeText = "";
	final LinkedList<NodeImpl> subnodes = new LinkedList<NodeImpl>();
	transformNodes(subnodes, doc.getBodyElements());
	final NodeImpl root = NodeFactory.newNode(Node.Type.ROOT);
	root.subnodes = subnodes.toArray(new NodeImpl[subnodes.size()]);
	return new org.luwrain.doctree.Document(root);
    }

    private void transformNodes(LinkedList<NodeImpl> subnodes, List<IBodyElement> range)
    {
	NullCheck.notNull(subnodes, "subnodes");
	NullCheck.notNull(range, "range");
	for (IBodyElement p: range)
	    if (p instanceof XWPFTable)
		subnodes.add(transformTable((XWPFTable) p)); else
		parseParagraph(subnodes, p);
    }

    private NodeImpl transformTable(XWPFTable table)
    {
	NullCheck.notNull(table, "table");
	final NodeImpl res = NodeFactory.newNode(Node.Type.TABLE);
	final LinkedList<NodeImpl> rows = new LinkedList<NodeImpl>();
	wholeText+=table.getText();
	for (final XWPFTableRow row: table.getRows())
	{ // для каждой строки таблицы
	    final NodeImpl rowNode = NodeFactory.newNode(Node.Type.TABLE_ROW);
	    rows.add(rowNode);
	    final LinkedList<NodeImpl> cells = new LinkedList<NodeImpl>();
	    for (final XWPFTableCell cell: row.getTableCells())
	    { // для каждой ячейки таблицы
		final NodeImpl cellNode = NodeFactory.newNode(Node.Type.TABLE_CELL);
		final LinkedList<NodeImpl> nodes = new LinkedList<NodeImpl>();
		cells.add(cellNode);
		transformNodes(nodes, cell.getBodyElements());
		cellNode.subnodes = nodes.toArray(new NodeImpl[nodes.size()]);
		checkNodesNotNull(cellNode.subnodes);
	    }
	    rowNode.subnodes = cells.toArray(new NodeImpl[cells.size()]);
	    checkNodesNotNull(rowNode.subnodes);
	} // for(trows);
	res.subnodes = rows.toArray(new NodeImpl[rows.size()]);
	checkNodesNotNull(res.subnodes);
	return res;
    }

    private void parseParagraph(LinkedList<NodeImpl> subnodes, IBodyElement element)
    {
	String className = element.getClass().getSimpleName();
	String paraText = "";
	if (element.getClass() == XWPFParagraph.class)
	{ // все есть параграф
	    final XWPFParagraph paragraph = (XWPFParagraph) element;
	    wholeText+=paragraph.getText();
	    if (paragraph.getNumIlvl() != null)
	    { // параграф с установленным уровнем - элемент списка
		// создаем элементы структуры Node и добавляем текущую ноду в
		// список потомка
		final NodeImpl node = NodeFactory.newNode(Node.Type.LIST_ITEM);
		subnodes.add(node);
		//
		BigInteger listId = paragraph.getNumID();
		int listLvl = paragraph.getNumIlvl().intValue();
		// если это новый список, то добавим пустой подсписок его
		// счетчиков
		if (!listInfo.containsKey(listId))
		    listInfo.put(listId, new HashMap<Integer, Integer>());
		// если уровень списка уменьшился, то очищаем счетчики выше
		// уровнем
		if (lastLvl > listLvl)
		{
		    for (Entry<Integer, Integer> lvls : listInfo.get(listId).entrySet())
			if (lvls.getKey() > listLvl)
			    listInfo.get(listId).put(lvls.getKey(), 1);
		}
		lastLvl = listLvl;
		// если в списке счетчиков значения нет, то иннициализируем его
		// 0 (позже он будет обязательно увеличен на 1)
		if (!listInfo.get(listId).containsKey(listLvl))listInfo.get(listId).put(listLvl, 0);
		// так как это очередной элемент списка, то увеличиваем его
		// счетчик на 1
		listInfo.get(listId).put(listLvl,listInfo.get(listId).get(listLvl) + 1);
		// формируем строку-номер
		String numstr = "";
		for (int lvl = 0; lvl <= listLvl; lvl++) numstr += listInfo.get(listId).get(lvl) + ".";
		paraText = paragraph.getText().trim();
		LinkedList<NodeImpl> item_subnodes = new LinkedList<NodeImpl>();
		item_subnodes.add(NodeFactory.newPara(paraText));
		node.subnodes = item_subnodes.toArray(new NodeImpl[item_subnodes.size()]);
	    } else
	    {
		paraText = paragraph.getText().trim();
		subnodes.add(NodeFactory.newPara(paraText));
	    }
	} else
	{
	    subnodes.add(NodeFactory.newPara(paraText));
	}
    }

    private void checkNodesNotNull(NodeImpl[] nodes)
    {
	if (nodes == null)
	    throw new NullPointerException("nodes is null");
	for (int i = 0; i < nodes.length; ++i)
	    if (nodes[i] == null)
		throw new NullPointerException("nodes[" + i + "] is null");
    }
}
