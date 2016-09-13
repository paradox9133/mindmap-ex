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
package pl.edu.agh.mindmapex.common;

import android.graphics.Path;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;

import org.xmind.ui.style.Styles;

import java.io.Serializable;

import pl.edu.agh.mindmapex.enums.Position;
import pl.edu.agh.mindmapex.gui.MainActivity;


public class Line implements Serializable, Cloneable {
    public String shape;
    private int thickness;
    private ColorDrawable color;
    private Point start;
    private Point end;
    private boolean isVisible;
    public Position position;
    public Box box;
    public int off = 30;

    public Drawable deleteLine;

    public Path getPath() {
        return path;
    }

    public void setPath(Path path) {
        this.path = path;
    }

    private Path path = new Path();

    public void preparePath() {
        path.reset();
        path.moveTo(start.x, start.y);
        if (shape == null && MainActivity.sheet1.getTheme() != null && (MainActivity.sheet1.getTheme().getName().equals("%classic") || MainActivity.sheet1.getTheme().getName().equals("%simple") || MainActivity.sheet1.getTheme().getName().equals("%bussiness") || MainActivity.sheet1.getTheme().getName().equals("%business") || MainActivity.sheet1.getTheme().getName().equals("%comic"))) {
            if (box.drawableShape.getBounds().left >= MainActivity.root.drawableShape.getBounds().centerX()) {
                path.cubicTo(start.x, start.y, (end.x + start.x) / 2, (start.y + end.y) / 2 + 200, end.x, end.y);
            } else {
                path.cubicTo(start.x, start.y, (end.x + start.x) / 2, (start.y + end.y) / 2 + 200, end.x, end.y);
                //path.quadTo(start.x, start.y, end.x, end.y);
            }
        } else if (shape == null) {
            path.lineTo(end.x, end.y);
        } else if (shape.equals(Styles.BRANCH_CONN_STRAIGHT)) {
            path.lineTo(end.x, end.y);
        } else if (shape.equals(Styles.BRANCH_CONN_CURVE)) {
            if (box.drawableShape.getBounds().left >= MainActivity.root.drawableShape.getBounds().centerX()) {
                //  path.quadTo(start.x, start.y, end.x, end.y);
                path.cubicTo(start.x, start.y, (end.x + start.x) / 2, (start.y + end.y) / 2 + 200, end.x, end.y);
            } else {
                path.cubicTo(start.x, start.y, (end.x + start.x) / 2, (start.y + end.y) / 2 + 200, end.x, end.y);
                //path.quadTo(start.x, start.y, end.x, end.y);
            }
        } else {
            path.lineTo(end.x, end.y);
        }


    }

    public Line(String shape, int thickness, ColorDrawable color, Point start,
                Point end, boolean isVisible) {
        super();
        this.shape = shape;
        this.thickness = thickness;
        this.color = color;
        this.start = start;
        this.end = end;
        this.isVisible = isVisible;
    }

    public boolean isVisible() {
        return isVisible;
    }

    public void setVisible(boolean isVisible) {
        this.isVisible = isVisible;
    }

    public int getThickness() {
        return thickness;
    }

    public void setThickness(int thickness) {
        this.thickness = thickness;
    }

    public ColorDrawable getColor() {
        return color;
    }

    public void setColor(ColorDrawable color) {
        this.color = color;
    }

    public Point getStart() {
        return start;
    }

    public void setStart(Point start) {
        this.start = start;
    }

    public Point getEnd() {
        return end;
    }

    public void setEnd(Point end) {
        this.end = end;
    }


    public Line Clone() throws CloneNotSupportedException {
        return (Line) super.clone();
    }


}
