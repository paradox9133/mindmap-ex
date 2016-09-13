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

import org.xmind.core.INotes;
import org.xmind.core.IPlainNotesContent;

import java.util.Properties;

import pl.edu.agh.mindmapex.common.Box;
import pl.edu.agh.mindmapex.gui.MainActivity;
import pl.edu.agh.mindmapex.interfaces.Command;

public class AddNote implements Command {
    Properties before;
    Properties after;
    Box box;

    @Override
    public void undo() {
        String note = (String) before.get("text");
        IPlainNotesContent plainContent = (IPlainNotesContent) MainActivity.workbook.createNotesContent(INotes.PLAIN);
        plainContent.setTextContent(note);
        box.topic.getNotes().setContent(INotes.PLAIN, plainContent);
    }

    @Override
    public void redo() {
        execute(after);
    }

    @Override
    public void execute(Properties properties) {
        before = (Properties) properties.clone();
        after = (Properties) properties.clone();
        box = (Box) properties.get("box");
        if (box.topic.getNotes().getContent(INotes.PLAIN) != null) {
            before.put("text", ((IPlainNotesContent) box.topic.getNotes().getContent(INotes.PLAIN)).getTextContent());
        } else {
            before.put("text", "");
        }
        String note = (String) properties.get("text");
        IPlainNotesContent plainContent = (IPlainNotesContent) MainActivity.workbook.createNotesContent(INotes.PLAIN);
        plainContent.setTextContent(note);
        box.topic.getNotes().setContent(INotes.PLAIN, plainContent);
    }


}
