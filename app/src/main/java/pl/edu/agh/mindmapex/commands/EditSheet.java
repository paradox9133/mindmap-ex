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

import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.util.Log;

import org.xmind.core.ISheet;
import org.xmind.core.style.IStyle;
import org.xmind.ui.style.Styles;

import java.util.Properties;

import pl.edu.agh.mindmapex.gui.MainActivity;
import pl.edu.agh.mindmapex.interfaces.Command;

public class EditSheet implements Command {
    ISheet sheet;
    Properties properties1 = new Properties();
    Properties after;

    @Override
    public void execute(Properties properties) {
        after = (Properties) properties.clone();
        sheet = (ISheet) properties.get("sheet");
        if (properties.containsKey("color")) {
            if (MainActivity.styleSheet.findStyle(sheet.getStyleId()) != null && MainActivity.styleSheet.findStyle(sheet.getStyleId()).getProperty(Styles.FillColor) != null) {
                properties1.put("color", MainActivity.styleSheet.findStyle(sheet.getStyleId()).getProperty(Styles.FillColor));
                Log.w("", MainActivity.styleSheet.findStyle(sheet.getStyleId()).getProperty(Styles.FillColor));
            } else {
                properties1.put("color", "#FFFFFF");
            }

            ColorDrawable c = (ColorDrawable) properties.get("color");
            String colorHex = "#" + (!Integer.toString(Color.red(c.getColor()), 16).equals("0") ? Integer.toString(Color.red(c.getColor()), 16) : "00")
                    + (!Integer.toString(Color.green(c.getColor()), 16).equals("0") ? Integer.toString(Color.green(c.getColor()), 16) : "00")
                    + (!Integer.toString(Color.blue(c.getColor()), 16).equals("0") ? Integer.toString(Color.blue(c.getColor()), 16) : "00");
            if (sheet.getStyleId() != null && MainActivity.styleSheet.findStyle(sheet.getStyleId()) != null) {
                MainActivity.styleSheet.findStyle(sheet.getStyleId()).setProperty(Styles.FillColor, colorHex);
            } else {
                IStyle style = MainActivity.styleSheet.createStyle(IStyle.MAP);
                style.setProperty(Styles.FillColor, colorHex);
                sheet.setStyleId(style.getId());
            }
        }

    }

    @Override
    public void undo() {
        //  sheet = (Sheet)properties1.get("sheet");
        if (properties1.containsKey("color")) {
            String c = (String) properties1.get("color");
            MainActivity.styleSheet.findStyle(sheet.getStyleId()).setProperty(Styles.FillColor, String.valueOf(c));
        }
    }

    @Override
    public void redo() {
        execute(after);
    }

}
