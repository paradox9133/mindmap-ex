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

import android.annotation.TargetApi;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.RotateDrawable;
import android.os.Build;

import org.xmind.core.IRelationship;
import org.xmind.core.ITopic;
import org.xmind.core.style.IStyle;
import org.xmind.ui.style.Styles;

import java.io.Serializable;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

import pl.edu.agh.mindmapex.R;
import pl.edu.agh.mindmapex.enums.Position;
import pl.edu.agh.mindmapex.gui.MainActivity;


public class Box implements Cloneable, Serializable, Comparable<Box> {
    @Override
    public int compareTo(Box another) {
        int res = 1;
        if (Objects.equals(topic.getStyleId(), another.topic.getStyleId())) {
            res = 0;
        }
        if (!topic.getTitleText().equals(another.topic.getTitleText())) {
            res = 1;
        }
        return res;
    }

    public boolean calculate = false;

    public void updateBox(Box b) {
        this.height = b.height;
        this.width = b.width;
        this.point = b.point;
        this.children = b.children;
        this.topic = b.topic;
        this.lines = b.lines;
        this.newNote = b.newNote;
        this.addBox = b.addBox;
        this.collapseAction = b.collapseAction;
        this.expandAction = b.expandAction;
        this.position = b.position;
        this.drawableShape = b.drawableShape;
    }

    public List<Box> getChildren() {
        return children;
    }

    public ConcurrentHashMap<Box, Line> getLines() {
        return lines;
    }

    public boolean isExpendable = false;
    public boolean isSelected = false;
    public ConcurrentHashMap<IRelationship, Box> relationships = new ConcurrentHashMap<>();

    public int getHeight() {
        return height;
    }

    public void setHeight(int height) {
        this.height = height;
    }

    public int getWidth() {
        return width;
    }

    public void setWidth(int width) {
        this.width = width;
    }

    public Point getPoint() {
        return point;
    }

    public void setPoint(Point point) {
        this.point = point;
    }

    protected int height = 110;
    protected int width = 150;
    public Point point;
    protected List<Box> children = new CopyOnWriteArrayList<>();
    protected ConcurrentHashMap<Box, Line> lines = new ConcurrentHashMap<>();
    public Drawable newNote;
    public Drawable addBox;
    public Drawable collapseAction;
    public Drawable expandAction;
    public Position position;
    public Drawable addNote;
    public ITopic topic;
    public Box parent;


    public void clear() {
        children.clear();
    }


    public Drawable getDrawableShape() {
        return drawableShape;
    }

    public void setDrawableShape(Drawable drawableShape) {
        this.drawableShape = drawableShape;
    }

    public Drawable drawableShape;

    public Box() {
    }

    public Drawable prepareDrawableShape() {
        IStyle style = MainActivity.workbook.getStyleSheet().findStyle(topic.getStyleId());
        int c = MainActivity.res.getColor(R.color.light_blue);
        int c2 = Color.WHITE;
        if (MainActivity.sheet1.getTheme() != null) {
            if (MainActivity.sheet1.getTheme().getName().equals("%classic")) {
                if (topic.isRoot()) {
                    c = MainActivity.res.getColor(R.color.lime_green);
                } else {
                    c = MainActivity.res.getColor(R.color.light_blue);
                }
            } else if (MainActivity.sheet1.getTheme().getName().equals("%simple")) {
                c = Color.WHITE;
            } else if (MainActivity.sheet1.getTheme().getName().equals("%bussiness")) {
                if (topic.isRoot()) {
                    c = MainActivity.res.getColor(R.color.copper);
                } else {
                    c = MainActivity.res.getColor(R.color.beige);
                }
            } else if (MainActivity.sheet1.getTheme().getName().equals("%academese")) {
                c = MainActivity.res.getColor(R.color.dark_gray);
                c2 = MainActivity.res.getColor(R.color.dark_gray);
            } else if (MainActivity.sheet1.getTheme().getName().equals("%comic")) {
                if (topic.isRoot()) {
                    c = MainActivity.res.getColor(R.color.orange);
                } else {
                    c = MainActivity.res.getColor(R.color.light_blue);
                }
            }

        }
        if (style != null && style.getProperty(Styles.FillColor) != null) {
            c = Color.parseColor(style.getProperty(Styles.FillColor));
        }
        int[] colors = {c2, c};
        String s = null;
        if (style != null) {
            s = style.getProperty(Styles.ShapeClass);
        }
        if (s == null) {
            ((GradientDrawable) drawableShape).setColors(colors);
            drawableShape.setBounds(point.x, point.y, point.x + width, point.y + height);
        } else if (s.equals(Styles.TOPIC_SHAPE_ELLIPSE)) {
            ((GradientDrawable) drawableShape).setColors(colors);
            drawableShape.setBounds(point.x, point.y, point.x + width, point.y + height);

        } else if (s.equals(Styles.TOPIC_SHAPE_ROUNDEDRECT)) {//dwie pierwsze warosci - polozenie lewego, gornego roku, pozostale pary zgodnie z ruchem wskazowek zegara;
            ((GradientDrawable) drawableShape).setColors(colors);
            drawableShape.setBounds(point.x, point.y, point.x + width, point.y + height);

        } else if (s.equals(Styles.TOPIC_SHAPE_DIAMOND)) {
            ((GradientDrawable) (((RotateDrawable) drawableShape).getDrawable())).setColors(colors);
            drawableShape.setBounds(point.x, point.y, point.x + width, point.y + width);

        } else if (s.equals(Styles.TOPIC_SHAPE_UNDERLINE)) {
            int c1;
            int c3;
            if (style != null && style.getProperty(Styles.FillColor) != null) {
                c1 = Color.WHITE;
                c3 = Color.parseColor(style.getProperty(Styles.FillColor));
            } else {
                c1 = Color.TRANSPARENT;
                c3 = Color.TRANSPARENT;
            }
            int[] colors2 = {c1, c3};
            drawableShape.setBounds(point.x, point.y, point.x + width, point.y + height);
            ((GradientDrawable) drawableShape).setColors(colors2);

        } else if (s.equals(Styles.TOPIC_SHAPE_NO_BORDER)) {
            int c1;
            int c4;
            if (style != null && style.getProperty(Styles.FillColor) != null) {
                c1 = Color.WHITE;
                c4 = Color.parseColor(style.getProperty(Styles.FillColor));
            } else {
                c1 = Color.TRANSPARENT;
                c4 = Color.TRANSPARENT;
            }
            int[] colors1 = {c1, c4};
            drawableShape.setBounds(point.x, point.y, point.x + width, point.y + height);
            ((GradientDrawable) drawableShape).setColors(colors1);
        } else {
            ((GradientDrawable) drawableShape).setColors(colors);
            drawableShape.setBounds(point.x, point.y, point.x + width, point.y + height);
        }
        return drawableShape;
    }

    //
//    public void Clone(Box box)
//    {
//        height = box.getHeight();
//        width = box.getWidth();
//        isSelected = box.isSelected();
//        point = box.getPoint();
//        parent = box.getParent();
//        children = box.getChildren();
//        shape = box.getShape();
//        color = box.getColor();
//        lines = box.getLines();
//        text = box.getText().clone();
//        notes = box.getNotes();
//        markers = box.getMarkers();
//        isVisible = box.isVisible;
//        isExpanded = box.isExpanded;
//    }
    public Box BoxClone() throws CloneNotSupportedException {
        return (Box) super.clone();
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    public void setActiveColor() {
        int[] colors = {Color.rgb(0, 51, 102), Color.rgb(0, 51, 102)};
        if (topic.getStyleId() != null && MainActivity.workbook.getStyleSheet().findStyle(topic.getStyleId()) != null && (MainActivity.workbook.getStyleSheet().findStyle(topic.getStyleId()).getProperty(Styles.ShapeClass) == null || !MainActivity.workbook.getStyleSheet().findStyle(topic.getStyleId()).getProperty(Styles.ShapeClass).equals(Styles.TOPIC_SHAPE_DIAMOND))) {
            ((GradientDrawable) drawableShape).setColors(colors);
        } else if (topic.getStyleId() != null && MainActivity.workbook.getStyleSheet().findStyle(topic.getStyleId()) != null && MainActivity.workbook.getStyleSheet().findStyle(topic.getStyleId()).getProperty(Styles.ShapeClass).equals(Styles.TOPIC_SHAPE_DIAMOND)) {
            ((GradientDrawable) ((RotateDrawable) drawableShape).getDrawable()).setColors(colors);
        } else if (isSelected) {
            ((GradientDrawable) drawableShape).setColors(colors);
        } else {
            int[] colors1 = {Color.WHITE, Color.BLUE};
            ((GradientDrawable) drawableShape).setColors(colors1);
        }
    }

    public void addChild(Box box) {
        children.add(box);
    }
}
