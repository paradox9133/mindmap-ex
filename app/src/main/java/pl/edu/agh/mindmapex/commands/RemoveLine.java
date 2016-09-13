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

import java.util.Properties;

import pl.edu.agh.mindmapex.common.Box;
import pl.edu.agh.mindmapex.common.Line;
import pl.edu.agh.mindmapex.common.Point;
import pl.edu.agh.mindmapex.enums.Position;
import pl.edu.agh.mindmapex.interfaces.Command;


public class RemoveLine implements Command {
    public Box box;
    Properties properties1;
    Properties after;
    Line line;

    @Override
    public void redo() {
        execute(after);
    }

    @Override
    public void execute(Properties properties) {
        properties1 = (Properties) properties.clone();
        after = properties;
        box = (Box) properties.get("box");

    }

    @Override
    public void undo() {
        Box parent = (Box) properties1.get("parent");
        if (box.position == Position.LFET) {
            line.setStart(new Point(parent.getDrawableShape().getBounds().left, parent.getDrawableShape().getBounds().centerY()));
            line.setEnd(new Point(box.getDrawableShape().getBounds().right, box.getDrawableShape().getBounds().centerY()));
        } else {
            line.setStart(new Point(parent.getDrawableShape().getBounds().right, parent.getDrawableShape().getBounds().centerY()));
            line.setEnd(new Point(box.getDrawableShape().getBounds().left, box.getDrawableShape().getBounds().centerY()));
        }
        parent.getLines().put(box, line);
    }


}
