package pl.edu.agh.mindmapex;

import android.app.Application;
import pl.edu.agh.mindmapex.dropbox.DropboxHandler;

public class App extends Application{
    public DropboxHandler dbxHandler;

    public DropboxHandler getDbxHandler(){
        if(dbxHandler==null)
            dbxHandler = new DropboxHandler(this);
        return dbxHandler;
    }
}
