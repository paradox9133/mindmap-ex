
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

import android.graphics.drawable.ColorDrawable;

import org.xmind.core.ITopic;
import org.xmind.core.style.IStyle;
import org.xmind.ui.style.Styles;

import java.util.HashMap;
import java.util.Properties;

import pl.edu.agh.mindmapex.R;
import pl.edu.agh.mindmapex.common.Box;
import pl.edu.agh.mindmapex.common.Line;
import pl.edu.agh.mindmapex.common.Point;
import pl.edu.agh.mindmapex.gui.MainActivity;
import pl.edu.agh.mindmapex.interfaces.Command;

public class RemoveBox implements Command {
    Properties before;
    Properties after;
    public HashMap<Box, Line> boxes;
    public HashMap<Box, Box> children = new HashMap<>();

    @Override
    public void execute(Properties properties) {
        before = (Properties) properties.clone();
        before.put("boxes", ((HashMap<Box, Line>) properties.get("boxes")).clone());
        after = (Properties) properties.clone();
        boxes = (HashMap<Box, Line>) properties.get("boxes");
        for (Box box : boxes.keySet()) {
            if (!box.topic.isRoot()) {
                box.topic.getParent().getAllChildren().remove(box.topic);
                box.topic.getParent().getChildren(ITopic.ATTACHED).remove(box.topic);
                box.parent.getChildren().remove(box);
                MainActivity.root.topic.remove(box.topic);
                children.put(box, box.parent);
            }
            box.parent.getLines().remove(box);

        }
    }

    @Override
    public void undo() {
        HashMap<Box, Line> boxes1 = (HashMap<Box, Line>) after.get("boxes");
        for (Box b : boxes1.keySet()) {
            b.prepareDrawableShape();
            if (!b.topic.isRoot()) {
                b.parent = children.get(b);
                children.get(b).topic.add(b.topic);
                IStyle s = MainActivity.workbook.getStyleSheet().findStyle(b.topic.getParent().getStyleId());
                int width = 1;
                if (s != null && s.getProperty(Styles.LineWidth) != null) {
                    width = Integer.parseInt(s.getProperty(Styles.LineWidth).substring(0, s.getProperty(Styles.LineWidth).length() - 2));
                }
                int color = MainActivity.res.getColor(R.color.light_gray);
                if (s != null && s.getProperty(Styles.LineColor) != null) {
                    color = Integer.parseInt(s.getProperty(Styles.LineColor));
                }
                String shape = null;
                if (s != null) {
                    shape = s.getProperty(Styles.LineClass);
                }
                Point start = null;
                Point end;
                if (b.getDrawableShape().getBounds().left <= MainActivity.root.getDrawableShape().getBounds().left) {
                    start = new Point(b.parent.getDrawableShape().getBounds().left, b.parent.getDrawableShape().getBounds().centerY());
                    end = new Point(b.getDrawableShape().getBounds().right, b.getDrawableShape().getBounds().centerY());
                } else {
                    start = new Point(b.parent.getDrawableShape().getBounds().right, b.parent.getDrawableShape().getBounds().centerY());
                    end = new Point(b.getDrawableShape().getBounds().left, b.getDrawableShape().getBounds().centerY());
                }
                Line line = new Line(shape, width, new ColorDrawable(color), start, end, true);
                b.parent.getLines().put(b, line);
                b.topic.getParent().add(b.topic);
                b.parent.addChild(b);
            }

        }
    }

    @Override
    public void redo() {
        execute(after);
    }
}
