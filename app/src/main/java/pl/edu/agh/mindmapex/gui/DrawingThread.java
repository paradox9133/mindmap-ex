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
package pl.edu.agh.mindmapex.gui;

import android.content.Context;
import android.graphics.Canvas;
import android.view.SurfaceHolder;

public class DrawingThread extends Thread {
    public final SurfaceHolder holder;
    public Canvas canvas = new Canvas();
    Context context;
    private boolean running = false;
    private DrawView lay;

    public DrawingThread(SurfaceHolder holder, DrawView lay) {
        this.holder = holder;
        this.lay = lay;
    }

    public void setRunning(boolean b) {
        running = b;
    }

    public void setSurfaceSize(int width, int height) {
        synchronized (holder) {
            int mCanvasWidth = width;
            int mCanvasHeight = height;
        }
    }


    @Override
    public void run() {
        // PAINT
        while (running) {
            if (!holder.getSurface().isValid())
                continue;

            canvas = null;
            try {
                canvas = holder.lockCanvas(null);
                if (canvas != null) {
                    synchronized (holder) {
                        lay.Mydraw(canvas);
                        lay.postInvalidate();
                    }
                }
            } finally {
                if (canvas != null) {
                    holder.unlockCanvasAndPost(canvas);
                }
            }
        }
    }

}
