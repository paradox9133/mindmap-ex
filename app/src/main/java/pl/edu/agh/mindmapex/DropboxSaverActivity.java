package pl.edu.agh.mindmapex;


import android.Manifest;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.dropbox.client2.exception.DropboxException;

import java.util.ArrayList;

import pl.edu.agh.mindmapex.dropbox.DbxBrowser;
import pl.edu.agh.mindmapex.dropbox.DropboxHandler;
import pl.edu.agh.mindmapex.dropbox.ResultListener;

public class DropboxSaverActivity extends AppCompatActivity {

    public static final String FILE_TO_SAVE = "file_to_save";
    private FileArrayAdapter fileArrayAdapter;
    private DbxBrowser browser;
    private ProgressDialog progressDialog;
    private EditText fileNameEditText;
    private static final int REQUEST_WRITE_STORAGE = 112;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dropbox_saver);

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
    private void init() {
        DropboxHandler dbxHandler = ((App) getApplicationContext()).getDbxHandler();
        browser = new DbxBrowser(dbxHandler);

        ListView listView = (ListView) findViewById(R.id.files_list_view);
        fileArrayAdapter = new FileArrayAdapter(this);
        fileNameEditText = (EditText) findViewById(R.id.file_name_edit_text);

        listView.setAdapter(fileArrayAdapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                DbxBrowser.DbxFile file = (DbxBrowser.DbxFile) adapterView.getItemAtPosition(i);
                if (file.isDir()) {
                    progressDialog.show();
                    browser.changeDir(file, listFolderListener);
                } else {
                    showDialog(file);
                }
            }
        });

        progressDialog = ProgressDialog.show(this, "Loading", "Wait...", true, false);
        browser.goToRootDir(listFolderListener);
    }

    private void showDialog(final DbxBrowser.DbxFile file) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setCancelable(true);
        builder.setTitle("Do you want to overwrite file?");
        builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                Intent result = new Intent().putExtra(FILE_TO_SAVE, file);
                setResult(RESULT_OK, result);
                dialogInterface.dismiss();
                finish();
            }
        });

        builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.dismiss();
            }
        });
        builder.create().show();
    }

    public void saveFileButtonClicked(View view) {
        String fileName = fileNameEditText.getText().toString();
        if (fileName == null || fileName.isEmpty()) {
            Toast.makeText(DropboxSaverActivity.this, "File name cannot be empty", Toast.LENGTH_SHORT).show();
            return;
        }
        if (!fileName.endsWith(".xmind"))
            fileName = fileName + ".xmind";
        DbxBrowser.DbxFile newFile = browser.createNewFile(fileName);
        Intent result = new Intent().putExtra(FILE_TO_SAVE, newFile);
        setResult(RESULT_OK, result);
        finish();
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
            Toast.makeText(DropboxSaverActivity.this, "Loading directory failed", Toast.LENGTH_SHORT).show();
        }
    };

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
