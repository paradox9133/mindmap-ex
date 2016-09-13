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

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.nononsenseapps.filepicker.FilePickerActivity;

import org.xmind.core.CoreException;
import org.xmind.core.internal.Workbook;

import java.io.File;
import java.io.IOException;

import pl.edu.agh.mindmapex.App;
import pl.edu.agh.mindmapex.DropboxSaverActivity;
import pl.edu.agh.mindmapex.R;
import pl.edu.agh.mindmapex.dropbox.DbxBrowser;
import pl.edu.agh.mindmapex.dropbox.DropboxHandler;
import pl.edu.agh.mindmapex.dropbox.DropboxWorkbookManager;
import pl.edu.agh.mindmapex.dropbox.ResultListener;
import pl.edu.agh.mindmapex.local.LocalWorkbookManager;


public class EditSheetScreen extends AppCompatActivity {

    public static TextView backgroud;
    public static View backdroundColorEditScreen;
    public static int COLOR;
    public static String ACTIVITY_TYPE = "EDIT_SHEET";
    public static final int REQUEST_FILE = 1;
    private ProgressDialog progressDialog;
    private DropboxHandler dropboxHandler;
    private String source = null;
    public static Workbook workbook;
    private DropboxWorkbookManager dropboxWorkbookManager;


    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.edit_screen);
        final Intent intent = getIntent();
        COLOR = intent.getIntExtra(MainActivity.BACKGROUNDCOLOR, 1);
        backgroud = (TextView) findViewById(R.id.textViewBackgroundColor);
        backdroundColorEditScreen = findViewById(R.id.sheet_color);
        ((GradientDrawable) backdroundColorEditScreen.getBackground()).setColor(COLOR);
        backdroundColorEditScreen.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(EditSheetScreen.this, ColorPalette.class);
                intent.putExtra("ACTIVITY", ACTIVITY_TYPE);
                startActivity(intent);
            }
        });
        ImageButton b = (ImageButton) findViewById(R.id.imageButton);
        b.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                source = "file";

                Intent intent1 = new Intent(EditSheetScreen.this, FilePickerActivity.class);
                intent1.putExtra(FilePickerActivity.EXTRA_ALLOW_MULTIPLE, false);
                intent1.putExtra(FilePickerActivity.EXTRA_ALLOW_CREATE_DIR, true);
                intent1.putExtra(FilePickerActivity.EXTRA_MODE, FilePickerActivity.MODE_NEW_FILE);
                intent1.putExtra(FilePickerActivity.EXTRA_START_PATH, Environment.getExternalStorageDirectory().getPath());

                startActivityForResult(intent1, REQUEST_FILE);

            }
        });
        ImageButton b2 = (ImageButton) findViewById(R.id.imageButton3);
        b2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                source = "dropbox";
                if (!dropboxHandler.isLinked())
                    dropboxHandler.linkAccount(EditSheetScreen.this);
                else {
                    Intent intent1 = new Intent(EditSheetScreen.this, DropboxSaverActivity.class);
                    startActivityForResult(intent1, REQUEST_FILE);
                }
            }
        });
        dropboxHandler = ((App) getApplicationContext()).getDbxHandler();
        progressDialog = new ProgressDialog(EditSheetScreen.this);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        try {
            MainActivity.workbook.saveTemp();
        } catch (IOException | CoreException e) {
            e.printStackTrace();
        }
        if (requestCode == REQUEST_FILE) {
            if (resultCode == RESULT_OK) {
                if (source.equals("file")) {
                    File file = new File(data.getData().getPath());

                    LocalWorkbookManager.saveWorkbook(file, MainActivity.workbook, new ResultListener() {
                        @Override
                        public void taskDone(Object result) {
                            showToast("File saved.");
                            finish();
                        }

                        @Override
                        public void taskFailed(Exception exception) {
                            showToast("Failure.");
                        }
                    });
                } else {
                    DbxBrowser.DbxFile file1 = (DbxBrowser.DbxFile) data.getExtras().get(DropboxSaverActivity.FILE_TO_SAVE);
                    dropboxWorkbookManager = DropboxWorkbookManager.bindWorkbookToDropboxFile(MainActivity.workbook, file1, dropboxHandler);
                    saveWorkbook();
                }
            }
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }


    private ResultListener<DropboxWorkbookManager, Exception> loadFileListener = new ResultListener<DropboxWorkbookManager, Exception>() {
        @Override
        public void taskDone(DropboxWorkbookManager result) {
            dropboxWorkbookManager = result;
            progressDialog.dismiss();
            showToast("Skoroszyt wczytany");
        }

        @Override
        public void taskFailed(Exception exception) {
            showToast("Nieudane pobranie");
            Log.e("XXXXX", exception.getMessage());
        }
    };

    private void showToast(String text) {
        Toast.makeText(this, text, Toast.LENGTH_SHORT).show();
    }

    public void saveWorkbook() {
        if (dropboxWorkbookManager == null) {
            showToast("Brak skoroszytu");
            return;
        }
        progressDialog.setTitle("Zapisywanie");
        progressDialog.show();
        dropboxWorkbookManager.uploadWithOverwrite(new ResultListener<Void, Exception>() {
            @Override
            public void taskDone(Void nothing) {
                progressDialog.dismiss();
                showToast("Skoroszyt zapisany");
            }

            @Override
            public void taskFailed(Exception exception) {
                showToast("Nieudane zapisanie pliku");
                Log.e("XXXXX", exception.getMessage());
            }
        });
    }


}
