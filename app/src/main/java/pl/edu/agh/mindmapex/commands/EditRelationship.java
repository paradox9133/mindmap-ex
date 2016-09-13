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

import java.util.Properties;

import pl.edu.agh.mindmapex.common.Box;
import pl.edu.agh.mindmapex.interfaces.Command;

public class EditRelationship implements Command {
    Box box1;
    Box box2;
    IRelationship rel;
    String text;
    Properties after;
    String old_text;

    @Override
    public void execute(Properties properties) {
        after = (Properties) properties.clone();
        rel = (IRelationship) properties.get("relation");
        text = (String) properties.get("text");
        old_text = rel.getTitleText();

        rel.setTitleText(text);
        if (properties.containsKey("new_start")) {
            box2 = (Box) properties.get("new_start");
            box1 = (Box) properties.get("box");
            box1.relationships.remove(rel);
            box2.relationships.put(rel, box1);
        }

    }

    @Override
    public void undo() {
        if (after.containsKey("new_start")) {
            box2.relationships.remove(rel);
            box1.relationships.put(rel, box2);
        }
        rel.setStyleId(old_text);
    }

    @Override
    public void redo() {
        execute(after);
    }
}
