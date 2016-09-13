/*
 This file is part of MindMapDroid.

    MindMapDroid is free software; you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation; either version 2 of the License, or
    (at your option) any later version.

    MindMapDroid is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with MindMapDroid; if not, write to the Free Software
    Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA
*/
package pl.edu.agh.mindmapex.commands;

import org.xmind.core.IRelationship;

import java.util.LinkedList;
import java.util.Properties;

import pl.edu.agh.mindmapex.common.Box;
import pl.edu.agh.mindmapex.gui.MainActivity;
import pl.edu.agh.mindmapex.interfaces.Command;

public class AddRelationship implements Command {
    LinkedList<Box> boxes;
    public IRelationship relation;
    String s;
    Properties prop;

    @Override
    public void execute(Properties properties) {
        prop = (Properties) properties.clone();
        if (boxes == null) {
            boxes = (LinkedList<Box>) ((LinkedList<Box>) properties.get("boxes")).clone();
        }
        relation = MainActivity.workbook.createRelationship();
        relation.setEnd1Id(boxes.getFirst().topic.getId());
        relation.setEnd2Id(boxes.getLast().topic.getId());
        s = (String) properties.get("text");
        relation.setTitleText(s);
        MainActivity.sheet1.addRelationship(relation);
        boxes.getFirst().relationships.put(relation, boxes.getLast());

    }

    @Override
    public void undo() {

        MainActivity.sheet1.removeRelationship(relation);
        boxes.getFirst().relationships.remove(relation);


    }

    @Override
    public void redo() {
        execute(prop);
    }
}
