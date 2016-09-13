package pl.edu.agh.mindmapex;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.dropbox.client2.exception.DropboxException;

import java.util.ArrayList;

import pl.edu.agh.mindmapex.dropbox.DbxBrowser;
import pl.edu.agh.mindmapex.dropbox.DropboxHandler;
import pl.edu.agh.mindmapex.dropbox.ResultListener;


public class DropboxBrowserActivity extends AppCompatActivity {

    public static final String SELECTED_FILE = "selected_file";
    private FileArrayAdapter fileArrayAdapter;
    private DbxBrowser browser;
    private ProgressDialog progressDialog;
    private static final int REQUEST_WRITE_STORAGE = 112;

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case REQUEST_WRITE_STORAGE: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    init();
                } else {
                    Toast.makeText(this, "The app was not allowed to write to your storage. Hence, it cannot function properly. Please consider granting it this permission", Toast.LENGTH_LONG).show();
                }
            }
        }

    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dropbox_browser);

        boolean hasPermission = (ContextCompat.checkSelfPermission(this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED);
        if (!hasPermission) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                    REQUEST_WRITE_STORAGE);
        } else {
            init();
        }
    }

    private void init() {
        DropboxHandler dbxHandler = ((App) getApplicationContext()).getDbxHandler();
        browser = new DbxBrowser(dbxHandler);

        ListView listView = (ListView) findViewById(R.id.files_list_view);
        fileArrayAdapter = new FileArrayAdapter(this);

        listView.setAdapter(fileArrayAdapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                DbxBrowser.DbxFile file = (DbxBrowser.DbxFile) adapterView.getItemAtPosition(i);
                if (file.isDir()) {
                    progressDialog.show();
                    browser.changeDir(file, listFolderListener);
                } else {
                    Intent result = new Intent().putExtra(SELECTED_FILE, file);
                    setResult(RESULT_OK, result);
                    finish();
                }
            }
        });

        progressDialog = ProgressDialog.show(this, "Loading", "Wait..", true, false);
        browser.goToRootDir(listFolderListener);
    }

    private ResultListener<DbxBrowser.DbxFile, DropboxException> listFolderListener = new ResultListener<DbxBrowser.DbxFile, DropboxException>() {
        @Override
        public void taskDone(DbxBrowser.DbxFile result) {
            fileArrayAdapter.listCurrentFolder();
            progressDialog.dismiss();
        }

        @Override
        public void taskFailed(DropboxException exception) {
            progressDialog.dismiss();
            Toast.makeText(DropboxBrowserActivity.this, "Cannot open directory. Check internet connection.", Toast.LENGTH_SHORT).show();
        }
    };

    public void newFolder(View view) {
        progressDialog.setTitle("Creating directory");
        progressDialog.show();
        browser.createNewDir("New directory", new ResultListener<DbxBrowser.DbxFile, DropboxException>() {
            @Override
            public void taskDone(DbxBrowser.DbxFile result) {
                fileArrayAdapter.listCurrentFolder();
                progressDialog.dismiss();
            }

            @Override
            public void taskFailed(DropboxException exception) {
                progressDialog.dismiss();
                Toast.makeText(DropboxBrowserActivity.this, "Cannot create new directory. Check internet connection.", Toast.LENGTH_SHORT).show();
                Log.i(getClass().getSimpleName(), "Error", exception);
            }
        });
    }

    private class FileArrayAdapter extends ArrayAdapter<DbxBrowser.DbxFile> {
        public FileArrayAdapter(Context context) {
            super(context, android.R.layout.simple_list_item_1, new ArrayList<DbxBrowser.DbxFile>());
        }

        public void listCurrentFolder() {
            clear();
            DbxBrowser.DbxFile parent = browser.getParentDir();
            if (parent != null)
                add(parent);
            addAll(browser.getCurrentDir().getContents());
            notifyDataSetChanged();
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            DbxBrowser.DbxFile file = getItem(position);
            View view = LayoutInflater.from(this.getContext()).inflate(android.R.layout.simple_list_item_1, parent, false);
            TextView textView = (TextView) view.findViewById(android.R.id.text1);
            textView.setText(file.getName());
            return view;
        }
    }

}
