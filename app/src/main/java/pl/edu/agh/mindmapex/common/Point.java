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

public class Point implements Comparable<Point> {
    @Override
    public int compareTo(Point another) {
        if (x == another.x && y == another.y) {
            return 0;
        } else if (x < another.x && y < another.y) {
            return 1;
        } else {
            return -1;
        }
    }

    public int x;
    public int y;

    public Point() {
        x = 0;
        y = 0;

    }

    public Point(int x, int y) {
        this.x = x;
        this.y = y;
    }
}
