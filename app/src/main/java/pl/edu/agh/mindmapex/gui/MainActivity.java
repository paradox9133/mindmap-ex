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

import android.Manifest;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.Path;
import android.graphics.PointF;
import android.os.Bundle;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Pair;
import android.view.ActionMode;
import android.view.GestureDetector;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.ScaleGestureDetector.SimpleOnScaleGestureListener;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.Toast;

import org.xmind.core.CoreException;
import org.xmind.core.INotes;
import org.xmind.core.IPlainNotesContent;
import org.xmind.core.IRelationship;
import org.xmind.core.ISheet;
import org.xmind.core.ITopic;
import org.xmind.core.IWorkbook;
import org.xmind.core.internal.Style;
import org.xmind.core.style.IStyle;
import org.xmind.core.style.IStyleSheet;
import org.xmind.ui.style.Styles;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Properties;

import pl.edu.agh.mindmapex.R;
import pl.edu.agh.mindmapex.WorkbookHandler;
import pl.edu.agh.mindmapex.commands.AddBox;
import pl.edu.agh.mindmapex.commands.AddLine;
import pl.edu.agh.mindmapex.commands.AddNote;
import pl.edu.agh.mindmapex.commands.AddRelationship;
import pl.edu.agh.mindmapex.commands.EditBox;
import pl.edu.agh.mindmapex.commands.EditRelationship;
import pl.edu.agh.mindmapex.commands.EditSheet;
import pl.edu.agh.mindmapex.commands.RemoveBox;
import pl.edu.agh.mindmapex.commands.RemoveLine;
import pl.edu.agh.mindmapex.commands.RemoveRelationship;
import pl.edu.agh.mindmapex.common.Box;
import pl.edu.agh.mindmapex.common.Line;
import pl.edu.agh.mindmapex.dropbox.ResultListener;
import pl.edu.agh.mindmapex.enums.Actions;
import pl.edu.agh.mindmapex.interfaces.Command;
import pl.edu.agh.mindmapex.local.LocalWorkbookManager;
import pl.edu.agh.mindmapex.utilities.AsyncInvalidate;
import pl.edu.agh.mindmapex.utilities.Callback;
import pl.edu.agh.mindmapex.utilities.DialogFactory;
import pl.edu.agh.mindmapex.utilities.Utils;
import tourguide.tourguide.Overlay;
import tourguide.tourguide.ToolTip;
import tourguide.tourguide.TourGuide;

public class MainActivity extends AppCompatActivity {

    public static final String TRANSX_KEY = "transx";
    public static final String TRANSY_KEY = "transy";
    public static final String ZOOMX_KEY = "zoomx";
    public static final String ZOOMY_KEY = "zoomy";
    public static final String PIVOTX_KEY = "pivotx";
    public static final String PIVOTY_KEY = "pivoty";
    public static final String FIRST_TIME_IN_APP_KEY = "FIRST_TIME_IN_APP_KEY";
    private GestureDetector gestureDetector;
    public static DrawView lay;
    public static ActionMode mActionMode;
    private GestureListener gestList = new GestureListener();
    public static Box root;
    public static Box boxEdited;
    public static LinkedList<Box> toEditBoxes = new LinkedList<>();
    public static Properties properties = new Properties();
    private Pair<Box, Actions> pair;

    public static LinkedList<Command> commandsUndo = new LinkedList<>();
    public LinkedList<Command> commandsRedo = new LinkedList<>();
    private static Menu menu;
    public static ProgressDialog progressDialog;
    public static HashMap<Path, Pair<IRelationship, Box>> allRelationship = new HashMap<>();


    public static int id = 1;

    private PointF mid = new PointF();
    private ScaleGestureDetector detector;

    public final static String BACKGROUNDCOLOR = "COLOR";
    public static int width;
    public static int height;

    public static String style;

    ///---------------------------------------
    public static ISheet sheet1;
    public static ITopic rootTopic;
    public static IWorkbook workbook;
    public static IStyleSheet styleSheet;
    public static Resources res;
    public static IStyle style1 = null;
    public static Boolean leftRelationship = true;
    private boolean mScaling;
    private SharedPreferences mSharedPrefs;
    private TourGuide mTourGuide;

    public void checkStyle(Box box) {

        IStyle boxStyle = styleSheet.findStyle(box.topic.getStyleId());
        if (boxStyle != null && boxStyle.getProperty(Styles.ShapeClass) != null) {
            if (boxStyle.getProperty(Styles.ShapeClass).equals(Styles.TOPIC_SHAPE_ELLIPSE)) {
                box.setDrawableShape(res.getDrawable(R.drawable.elipse));
            } else if (boxStyle.getProperty(Styles.ShapeClass).equals(Styles.TOPIC_SHAPE_ROUNDEDRECT)) {
                box.setDrawableShape(res.getDrawable(R.drawable.round_rect));
            } else if (boxStyle.getProperty(Styles.ShapeClass).equals(Styles.TOPIC_SHAPE_RECT)) {
                box.setDrawableShape(res.getDrawable(R.drawable.rect));
            } else if (boxStyle.getProperty(Styles.ShapeClass).equals(Styles.TOPIC_SHAPE_DIAMOND)) {
                box.setDrawableShape(res.getDrawable(R.drawable.diammond));
            } else if (boxStyle.getProperty(Styles.ShapeClass).equals(Styles.TOPIC_SHAPE_NO_BORDER) || boxStyle.getProperty(Styles.ShapeClass).equals(Styles.TOPIC_SHAPE_UNDERLINE)) {
                box.setDrawableShape(res.getDrawable(R.drawable.no_border));
            } else {
                box.setDrawableShape(res.getDrawable(R.drawable.round_rect));
            }
        } else {
            box.setDrawableShape(res.getDrawable(R.drawable.round_rect));
        }
    }

    WorkbookHandler handler;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        mSharedPrefs = PreferenceManager.getDefaultSharedPreferences(this);
        progressDialog = ProgressDialog.show(this, "Drawing", "Please wait...", true, false);
        if (WelcomeScreen.workbook != null) {
            workbook = WelcomeScreen.workbook;
        }
        res = getResources();
        if (handler == null) {
            handler = WorkbookHandler.createNewWorkbook();
        }


        if (workbook == null) {
            workbook = handler.getWorkbook();
            styleSheet = workbook.getStyleSheet();
            style1 = styleSheet.createStyle(IStyle.TOPIC);
            styleSheet.addStyle(style1, IStyleSheet.NORMAL_STYLES);
        }
        styleSheet = workbook.getStyleSheet();
        sheet1 = workbook.getPrimarySheet();
        res = getResources();
        if (style == null) {
            Intent intent = getIntent();
            style = intent.getStringExtra(WelcomeScreen.STYLE);
        }
        if (root == null) {


//            Display display = getWindowManager().getDefaultDisplay();
            setContentView(R.layout.main_content_view);
            lay = (DrawView) findViewById(R.id.main_draw_view);
            lay.post(new Runnable() {
                @Override
                public void run() {
                    rootTopic = sheet1.getRootTopic();
                    root = new Box();

//            Point size = new Point(lay.getWidth(), lay.getHeight());
//            width = size.x / 2;
//            height = size.y / 2;
                    root.setPoint(new pl.edu.agh.mindmapex.common.Point(lay.getWidth() / 2 - getResources().getDimensionPixelSize(R.dimen.init_box_size), lay.getHeight() / 2 - getResources().getDimensionPixelSize(R.dimen.init_box_size_height)));
                    lay.setZOrderOnTop(true);

                    if (style.equals("ReadyMap")) {

                        if (sheet1.getTheme() == null) {
                            root.setPoint(new pl.edu.agh.mindmapex.common.Point(width, height));
                            final HashMap<String, Box> boxes = new HashMap<>();
                            root.topic = rootTopic;
                            if (root.topic.getStyleId() != null) {
                                checkStyle(root);
                            } else {
                                root.setDrawableShape(res.getDrawable(R.drawable.round_rect));
                            }

                            root.topic.setFolded(false);

                            boxes.put(root.topic.getId(), root);
                            for (ITopic t : root.topic.getAllChildren()) {
                                Box b = new Box();
                                b.topic = t;
                                boxes.put(root.topic.getId(), root);
                                b.point = new pl.edu.agh.mindmapex.common.Point();
                                if (b.topic.getStyleId() != null) {
                                    checkStyle(b);
                                } else {
                                    b.setDrawableShape(res.getDrawable(R.drawable.round_rect));
                                }
                                b.parent = root;
                                root.addChild(b);
                                rootTopic.add(b.topic, 0, ITopic.ATTACHED);
                                Utils.fireAddSubtopic(b, boxes);
                                boxes.put(t.getId(), b);
                            }
                            Utils.findRelationships(boxes);
                        } else {
                            if (sheet1.getTheme().getName().equals("%classic") || sheet1.getTheme().getName().equals("%comic")) {
                                root.setPoint(new pl.edu.agh.mindmapex.common.Point(width, height));
                                root.topic = rootTopic;
                                final HashMap<String, Box> boxes = new HashMap<>();
                                if (root.topic.getStyleId() != null) {
                                    checkStyle(root);
                                } else {
                                    root.setDrawableShape(res.getDrawable(R.drawable.elipse));
                                }
                                root.topic.setFolded(false);
                                boxes.put(root.topic.getId(), root);

                                for (ITopic t : root.topic.getAllChildren()) {
                                    Box b = new Box();
                                    b.topic = t;
                                    b.point = new pl.edu.agh.mindmapex.common.Point();
                                    boxes.put(root.topic.getId(), root);
                                    if (b.topic.getStyleId() != null) {
                                        checkStyle(b);
                                    } else {
                                        b.setDrawableShape(res.getDrawable(R.drawable.round_rect));
                                    }
                                    b.parent = root;
                                    root.addChild(b);
                                    Utils.fireAddSubtopic(b, boxes);
                                    rootTopic.add(b.topic, 0, ITopic.ATTACHED);
                                    boxes.put(t.getId(), b);
                                }
                                Utils.findRelationships(boxes);
                            } else if (sheet1.getTheme().getName().equals("%simple")) {
                                root.setPoint(new pl.edu.agh.mindmapex.common.Point(width, height));
                                final HashMap<String, Box> boxes = new HashMap<>();
                                root.topic = rootTopic;
                                if (root.topic.getStyleId() != null) {
                                    checkStyle(root);
                                } else {
                                    root.setDrawableShape(res.getDrawable(R.drawable.elipse));
                                }
                                root.topic.setFolded(false);
                                boxes.put(root.topic.getId(), root);

                                for (ITopic t : root.topic.getAllChildren()) {
                                    Box b = new Box();
                                    b.topic = t;
                                    b.point = new pl.edu.agh.mindmapex.common.Point();
                                    boxes.put(root.topic.getId(), root);

                                    if (b.topic.getStyleId() != null) {
                                        checkStyle(b);
                                    } else {
                                        b.setDrawableShape(res.getDrawable(R.drawable.no_border));
                                    }
                                    b.parent = root;
                                    root.addChild(b);
                                    rootTopic.add(b.topic, 0, ITopic.ATTACHED);
                                    Utils.fireAddSubtopic(b, boxes);
                                    boxes.put(t.getId(), b);
                                }
                                Utils.findRelationships(boxes);
                            } else if (sheet1.getTheme().getName().equals("%bussiness")) {
                                root.setPoint(new pl.edu.agh.mindmapex.common.Point(width, height));
                                final HashMap<String, Box> boxes = new HashMap<>();
                                root.topic = rootTopic;
                                if (root.topic.getStyleId() != null) {
                                    checkStyle(root);
                                } else {
                                    root.setDrawableShape(res.getDrawable(R.drawable.round_rect));
                                }
                                root.topic.setFolded(false);


                                boxes.put(root.topic.getId(), root);

                                for (ITopic t : root.topic.getAllChildren()) {
                                    Box b = new Box();
                                    b.topic = t;
                                    b.point = new pl.edu.agh.mindmapex.common.Point();
                                    boxes.put(root.topic.getId(), root);
                                    if (b.topic.getStyleId() != null) {
                                        checkStyle(b);
                                    } else {
                                        b.setDrawableShape(res.getDrawable(R.drawable.rect));
                                    }

                                    b.parent = root;
                                    root.addChild(b);
                                    rootTopic.add(b.topic, 0, ITopic.ATTACHED);
                                    Utils.fireAddSubtopic(b, boxes);
                                    boxes.put(t.getId(), b);
                                }
                                Utils.findRelationships(boxes);
                            } else if (sheet1.getTheme().getName().equals("%academese")) {
                                root.setPoint(new pl.edu.agh.mindmapex.common.Point(width, height));
                                final HashMap<String, Box> boxes = new HashMap<>();
                                root.topic = rootTopic;
                                if (root.topic.getStyleId() != null) {
                                    checkStyle(root);
                                } else {
                                    root.setDrawableShape(res.getDrawable(R.drawable.rect));
                                }
                                root.topic.setFolded(false);
                                Style s = (Style) workbook.getStyleSheet().createStyle(IStyle.MAP);
                                s.setProperty(Styles.FillColor, Integer.toString(res.getColor(R.color.dark_gray), 16));
                                styleSheet.addStyle(s, IStyleSheet.NORMAL_STYLES);
                                sheet1.setStyleId(s.getId());
                                lay.setBackgroundColor(res.getColor(R.color.dark_gray));

                                boxes.put(root.topic.getId(), root);

                                for (ITopic t : root.topic.getAllChildren()) {
                                    Box b = new Box();
                                    b.topic = t;
                                    b.point = new pl.edu.agh.mindmapex.common.Point();
                                    boxes.put(root.topic.getId(), root);

                                    if (b.topic.getStyleId() != null) {
                                        checkStyle(b);
                                    } else {
                                        b.setDrawableShape(res.getDrawable(R.drawable.elipse));
                                    }
                                    b.parent = root;
                                    root.addChild(b);
                                    Utils.fireAddSubtopic(b, boxes);
                                    rootTopic.add(b.topic, 0, ITopic.ATTACHED);
                                    boxes.put(t.getId(), b);
                                }
                                Utils.findRelationships(boxes);
                            }
                        }
                    } else if (style.equals("Default")) {

                        rootTopic.setTitleText("Central Topic");
                        root.topic = rootTopic;
                        root.topic.setFolded(false);
                        root.setDrawableShape(res.getDrawable(R.drawable.round_rect));
                        IStyle style3 = styleSheet.createStyle(IStyle.TOPIC);
                        style3.setProperty(Styles.FillColor, "#CCE5FF");
                        style3.setProperty(Styles.ShapeClass, Styles.TOPIC_SHAPE_ROUNDEDRECT);
                        style3.setProperty(Styles.LineClass, Styles.BRANCH_CONN_STRAIGHT);
                        styleSheet.addStyle(style3, IStyleSheet.NORMAL_STYLES);
                        rootTopic.setStyleId(style3.getId());
                    } else if (style.equals("Classic")) {
                        rootTopic.setTitleText("Central Topic");
                        root.topic = rootTopic;
                        root.topic.setFolded(false);
                        root.setDrawableShape(res.getDrawable(R.drawable.elipse));
                        IStyle style2 = styleSheet.createStyle(IStyle.THEME);
                        style2.setName("%classic");
                        style2.setProperty(Styles.FillColor, String.valueOf(res.getColor(R.color.light_yellow)));
                        styleSheet.addStyle(style2, IStyleSheet.NORMAL_STYLES);
                        sheet1.setThemeId(style2.getId());
                        IStyle style3 = styleSheet.createStyle(IStyle.TOPIC);
                        style3.setProperty(Styles.FillColor, "#9ACD32");
                        style3.setProperty(Styles.ShapeClass, Styles.TOPIC_SHAPE_ELLIPSE);
                        style3.setProperty(Styles.LineClass, Styles.BRANCH_CONN_CURVE);
                        styleSheet.addStyle(style3, IStyleSheet.NORMAL_STYLES);
                        style2.setProperty(Style.TOPIC, style3.getId());
                        rootTopic.setStyleId(style3.getId());
                    } else if (style.equals("Simple")) {
                        rootTopic.setTitleText("Central Topic");
                        root.topic = rootTopic;
                        root.topic.setFolded(false);
                        root.setDrawableShape(res.getDrawable(R.drawable.elipse));
                        IStyle style2 = styleSheet.createStyle(IStyle.THEME);
                        style2.setName("%simple");
                        style2.setProperty(Styles.FillColor, String.valueOf(res.getColor(R.color.white)));
                        styleSheet.addStyle(style2, IStyleSheet.NORMAL_STYLES);
                        sheet1.setThemeId(style2.getId());
                        IStyle style3 = styleSheet.createStyle(IStyle.TOPIC);
                        style3.setProperty(Styles.FillColor, "#FFFFFF");
                        style3.setProperty(Styles.ShapeClass, Styles.TOPIC_SHAPE_ELLIPSE);
                        style3.setProperty(Styles.LineClass, Styles.BRANCH_CONN_CURVE);
                        styleSheet.addStyle(style3, IStyleSheet.NORMAL_STYLES);
                        style2.setProperty(Style.TOPIC, style3.getId());
                        rootTopic.setStyleId(style3.getId());
                    } else if (style.equals("Business")) {
                        rootTopic.setTitleText("Central Topic");
                        root.topic = rootTopic;
                        root.topic.setFolded(false);
                        root.setDrawableShape(res.getDrawable(R.drawable.round_rect));
                        IStyle style2 = styleSheet.createStyle(IStyle.THEME);
                        style2.setName("%business");
                        style2.setProperty(Styles.FillColor, String.valueOf(res.getColor(R.color.white)));
                        styleSheet.addStyle(style2, IStyleSheet.NORMAL_STYLES);
                        sheet1.setThemeId(style2.getId());
                        IStyle style3 = styleSheet.createStyle(IStyle.TOPIC);
                        style3.setProperty(Styles.FillColor, "#B87333");
                        style3.setProperty(Styles.ShapeClass, Styles.TOPIC_SHAPE_ROUNDEDRECT);
                        style3.setProperty(Styles.LineClass, Styles.BRANCH_CONN_CURVE);
                        styleSheet.addStyle(style3, IStyleSheet.NORMAL_STYLES);
                        style2.setProperty(Style.TOPIC, style3.getId());
                        rootTopic.setStyleId(style3.getId());
                    } else if (style.equals("Academese")) {
                        rootTopic.setTitleText("Central Topic");
                        root.topic = rootTopic;
                        root.topic.setFolded(false);
                        root.setDrawableShape(res.getDrawable(R.drawable.rect));
                        IStyle style2 = styleSheet.createStyle(IStyle.THEME);
                        style2.setProperty(Styles.FillColor, "#404040");
                        styleSheet.addStyle(style2, IStyleSheet.NORMAL_STYLES);
                        sheet1.setStyleId(style2.getId());
                        lay.setBackgroundColor(res.getColor(R.color.dark_gray));
                        IStyle style3 = styleSheet.createStyle(IStyle.TOPIC);
                        style3.setProperty(Styles.FillColor, "#404040");
                        style3.setProperty(Styles.ShapeClass, Styles.TOPIC_SHAPE_RECT);
                        style3.setProperty(Styles.LineClass, Styles.BRANCH_CONN_STRAIGHT);
                        style3.setProperty(Styles.LineColor, "#FFFFFF");
                        styleSheet.addStyle(style3, IStyleSheet.NORMAL_STYLES);
                        style2.setProperty(Style.TOPIC, style3.getId());
                        rootTopic.setStyleId(style3.getId());
                    }

                }
            });

        } else {
            setContentView(R.layout.main_content_view);
            lay = (DrawView) findViewById(R.id.main_draw_view);
            lay.setZOrderOnTop(true);
        }
        gestureDetector = new GestureDetector(this, gestList);
        Utils.lay = lay;
        if (lay != null) {
            lay.setOnTouchListener(new OnTouchListener() {
                @Override
                public boolean onTouch(View v, MotionEvent event) {

                    detector.onTouchEvent(event);

                    switch (event.getActionMasked()) {
                        case (MotionEvent.ACTION_OUTSIDE):
                            return true;
                        case (MotionEvent.ACTION_UP):
                            break;
                        case MotionEvent.ACTION_POINTER_UP:


                            if (!detector.isInProgress()) {
                                int count = event.getPointerCount(); // Number of 'fingers' in this time

                                Utils.getCoordsInView(lay, event, 1);
                                boxEdited = Utils.whichBox(lay, event);
                                float[] tab1 = Utils.getCoordsInView(lay, event, 0);
                                float[] tab = Utils.getCoordsInView(lay, event, 1);
                                if (count == 2 && boxEdited != null) {


                                    if (tab.length == 2) {
                                        if (mTourGuide != null)
                                            mTourGuide.cleanUp();
                                        Box box1 = new Box();
                                        box1.setPoint(new pl.edu.agh.mindmapex.common.Point((int) tab[0] - (box1.getWidth() / 2), (int) tab[1] - (box1.getHeight() / 2)));
                                        AddBox addBox = new AddBox();
                                        Properties properties = new Properties();
                                        properties.put("box", MainActivity.boxEdited);
                                        properties.put("new_box", box1);
                                        properties.put("root", root);
                                        properties.put("res", res);
                                        properties.put("style", style);
                                        addBox.execute(properties);
                                        MainActivity.addCommendUndo(addBox);
                                        editContent(box1, addBox);
                                        lay.updateBoxWithText(box1);

                                    }
                                }


                                break;
                            }
                        default:
                            break;
                    }

                    boolean response = gestureDetector.onTouchEvent(event);
                    lay.requestFocus();
                    InputMethodManager in = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                    in.hideSoftInputFromWindow(lay.getApplicationWindowToken(), 0);
                    return response;
                }
            });
            lay.setLayerType(View.LAYER_TYPE_SOFTWARE, null);
        }
        Utils.context = this;
        this.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayOptions(ActionBar.DISPLAY_SHOW_HOME | ActionBar.DISPLAY_SHOW_TITLE | ActionBar.DISPLAY_SHOW_CUSTOM);
        actionBar.show();

        detector = new ScaleGestureDetector(this, new SimpleOnScaleGestureListener() {
            @Override
            public boolean onScaleBegin(ScaleGestureDetector detector) {
                mScaling = true;
                return true;
            }

            @Override
            public boolean onScale(ScaleGestureDetector detector) {
//                float focusX = detector.getFocusX();
//                float focusY = detector.getFocusY();
////                lay.setPivotX(mid.x);
////                lay.setPivotY(mid.y);
////                               lay.pivotx = (int) (lastFocusX  + detector.getFocusX())/2;
////               lay.pivoty = (int) (lastFocusY+ detector.getFocusY())/2;
//          //     lay.pivotx = (int) mid.x;
//          //     lay.pivoty = (int) mid.y;
//            //    lay.canvas.translate(-focusX,-focusY);
                lay.pivotx = detector.getFocusX();
                lay.pivoty = detector.getFocusY();
//                lay.transx = (lay.pivotx);
//                lay.transy = (lay.pivoty);
//                lay.pivotx = (int) mid.x;
//                lay.pivoty = (int) mid.y;
//                lay.setPivotX(lastFocusX);
//                lay.setPivotY(lastFocusY);
                float SF = detector.getScaleFactor();
                lay.zoomx *= SF;
                lay.zoomy *= SF;
                //    lay.canvas.scale(SF, SF, mid.x, mid.y);
//                float focusShiftX = focusX - lastFocusX;
//                float focusShiftY = focusY - lastFocusY;
                //lay.canvas.translate(focusX + focusShiftX, focusY + focusShiftY);
                //  lastFocusX = focusX;
                // lastFocusY = focusY;
                //  lay.transy = detector.getFocusY();
                // lay.zoomx = Math.max(0.1f, Math.min(lay.zoomx, 5.0f));
                // lay.zoomy = Math.max(0.1f, Math.min(lay.zoomy, 5.0f));
                return true;

            }
        });
        progressDialog.dismiss();
        lay.setId(View.generateViewId());
        lay.setSaveEnabled(true);

        if (savedInstanceState != null) {
            lay.transx = savedInstanceState.getFloat(TRANSX_KEY);
            lay.transy = savedInstanceState.getFloat(TRANSY_KEY);
            lay.zoomx = savedInstanceState.getFloat(ZOOMX_KEY);
            lay.zoomy = savedInstanceState.getFloat(ZOOMY_KEY);
            lay.pivotx = savedInstanceState.getFloat(PIVOTX_KEY);
            lay.pivoty = savedInstanceState.getFloat(PIVOTY_KEY);

        }

    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putFloat(TRANSX_KEY, lay.transx);
        outState.putFloat(TRANSY_KEY, lay.transy);
        outState.putFloat(ZOOMX_KEY, lay.zoomx);
        outState.putFloat(ZOOMY_KEY, lay.zoomy);
        outState.putFloat(PIVOTX_KEY, lay.pivotx);
        outState.putFloat(PIVOTY_KEY, lay.pivoty);
    }

    @Override
    public void onPause() {
        super.onPause();
        lay.pouseThread();
    }

    private void midPoint(PointF point, MotionEvent event) {
        float tab[] = Utils.getCoordsInView(lay, event, 0);
        float tab1[] = Utils.getCoordsInView(lay, event, 0);
        float x = tab[0] + tab1[0];
        float y = tab[1] + tab1[1];
        mid.set(x / 2, y / 2);
    }

    @Override
    public void onResume() {
        super.onResume();
        if (lay != null && sheet1 != null && sheet1.getStyleId() != null && styleSheet.findStyle(sheet1.getStyleId()) != null && styleSheet.findStyle(sheet1.getStyleId()).getProperty(Styles.FillColor) != null) {
            lay.setBackgroundColor(Color.parseColor(styleSheet.findStyle(sheet1.getStyleId()).getProperty(Styles.FillColor)));
        }
        lay.resumeThread();
        lay.setZOrderOnTop(true);

        if (isFirstTimeInApp()) {
            View viewById = findViewById(R.id.tooltip_helper);
            Overlay overlay = new Overlay();
            overlay.disableClick(false);
            mTourGuide = TourGuide.init(MainActivity.this).with(TourGuide.Technique.Click)
//                    .setToolTip(new ToolTip().setTitle("Witaj!").setDescription("Click on box and somewhere in the darker area to create new box and connect each other"))
                    .setToolTip(new ToolTip().setTitle("Witaj, postępuj zgodnie z instrukcją by dodać swój pierwszy bloczek!").setDescription("Kliknij i przytrzymaj palec na bloczku startowym, nastepnie kliknij innym palcem w dowolne miejsce na ciemnym tle."))
                    .setOverlay(overlay)
                    .playOn(viewById);

        }

    }

    //tutaj rozpoznajemy przytrzymanie, jedno kliknięcie, dwa kliknięcia
    private class GestureListener extends GestureDetector.SimpleOnGestureListener {
        Box clicked = null;
        boolean click = false;

        @Override
        public boolean onSingleTapConfirmed(MotionEvent event) {
            pair = Utils.whichBoxAction(lay, event);
            Box box = Utils.whichBox(lay, event);
            if (box != null) {
                box.isSelected = true;
                MainActivity.boxEdited = box;
                if (!MainActivity.toEditBoxes.contains(box)) {
                    MainActivity.toEditBoxes.add(box);
                }
                if (MainActivity.toEditBoxes.size() == 2) {
                    menu.getItem(4).setVisible(true);
                    menu.getItem(1).setVisible(true);
                } else if (MainActivity.toEditBoxes.size() == 1) {
                    if (!MainActivity.toEditBoxes.getFirst().topic.isRoot()) {
                        menu.getItem(1).setVisible(true);
                    }
                    menu.getItem(4).setVisible(false);
                } else {
                    menu.getItem(4).setVisible(false);
                    menu.getItem(1).setVisible(false);
                }

                menu.getItem(2).setVisible(true);
                if (!box.topic.isRoot()) {
                    menu.getItem(3).setVisible(true);
                }
            } else if (box == null) {

                menu.getItem(1).setVisible(false);
                menu.getItem(4).setVisible(false);
                root.isSelected = false;
                if (MainActivity.boxEdited != null) {
                    MainActivity.boxEdited.isSelected = false;
                }
                for (int i = 0; i < MainActivity.toEditBoxes.size(); i++) {
                    MainActivity.toEditBoxes.get(i).isSelected = false;
                }
                menu.getItem(2).setVisible(false);
                menu.getItem(3).setVisible(false);
                MainActivity.toEditBoxes.clear();
            }
            if (pair != null) {
                if (pair.second == Actions.ADD_NOTE) {
                    final Dialog dialog = DialogFactory.boxContentDialog(MainActivity.this);
                    final Button btn = (Button) dialog.findViewById(R.id.dialogButtonOK);
                    final EditText et = (EditText) dialog.findViewById(R.id.editText);
                    et.requestFocus();
                    final Button btn2 = (Button) dialog.findViewById(R.id.button2);
                    btn2.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            dialog.dismiss();
                        }
                    });

                    btn.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                            imm.hideSoftInputFromWindow(et.getWindowToken(), 0);

                            String text = (et.getText().toString());
                            AddNote addNote = new AddNote();
                            Properties properties = new Properties();
                            properties.put("box", pair.first);
                            properties.put("text", text);
                            addNote.execute(properties);
                            addCommendUndo(addNote);
                            MainActivity.menu.getItem(5).setVisible(true);
                            dialog.dismiss();
                        }
                    });

                    final int MAX_LINES = 3;

                    //ogranicza do 3 linii widok w zawartości bloczka
                    et.addTextChangedListener(new TextWatcher() {
                        private int lines;

                        @Override
                        public void onTextChanged(CharSequence s, int start, int before, int count) {
                        }

                        @Override
                        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                            lines = Utils.countLines(s.toString());
                        }

                        @Override
                        public void afterTextChanged(Editable s) {
                            int counter = Utils.countLines(s.toString());

                            int diff = lines - counter;
                            if (diff > 0) {
                                //w gore
                                if (counter < MAX_LINES - 1 && et.getLayoutParams().height > 75) {
                                    LinearLayout.LayoutParams buttonLayoutParams = (LinearLayout.LayoutParams) btn.getLayoutParams();
                                    buttonLayoutParams.setMargins(buttonLayoutParams.leftMargin, buttonLayoutParams.topMargin - 30,
                                            buttonLayoutParams.rightMargin, buttonLayoutParams.bottomMargin);
                                    btn.setLayoutParams(buttonLayoutParams);
                                    btn2.setLayoutParams(buttonLayoutParams);
                                    et.getLayoutParams().height -= 30;
                                }
                            } else if (diff < 0) {
                                //w dol
                                if (counter < MAX_LINES && et.getLayoutParams().height < 135) {
                                    LinearLayout.LayoutParams buttonLayoutParams = (LinearLayout.LayoutParams) btn.getLayoutParams();
                                    buttonLayoutParams.setMargins(buttonLayoutParams.leftMargin, buttonLayoutParams.topMargin + 30,
                                            buttonLayoutParams.rightMargin, buttonLayoutParams.bottomMargin);
                                    btn.setLayoutParams(buttonLayoutParams);
                                    btn2.setLayoutParams(buttonLayoutParams);
                                    et.getLayoutParams().height += 30;
                                }
                            }
                        }
                    });

                    int k = Utils.countLines(et.getText().toString());
                    int ile = Math.min(MAX_LINES - 1, k);

                    et.getLayoutParams().height = 75 + ile * 30;
                    LinearLayout.LayoutParams buttonLayoutParams = (LinearLayout.LayoutParams) btn.getLayoutParams();
                    buttonLayoutParams.setMargins(buttonLayoutParams.leftMargin,
                            buttonLayoutParams.topMargin + 30 * ((k < 2) ? 0 : (k == 2) ? ile - 1 : ile),
                            buttonLayoutParams.rightMargin, buttonLayoutParams.bottomMargin);
                    btn.setLayoutParams(buttonLayoutParams);
                    btn2.setLayoutParams(buttonLayoutParams);

                    InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.showSoftInput(et, InputMethodManager.SHOW_IMPLICIT);

                    dialog.show();

                } else if (pair.second == Actions.NEW_NOTE) {
                    final Dialog dialog = DialogFactory.boxContentDialog(MainActivity.this);
                    final Button btn = (Button) dialog.findViewById(R.id.dialogButtonOK);
                    final EditText et = (EditText) dialog.findViewById(R.id.editText);
                    final Button btn2 = (Button) dialog.findViewById(R.id.button2);
                    btn2.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            dialog.dismiss();
                        }
                    });
                    et.requestFocus();

                    btn.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {

                            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                            imm.hideSoftInputFromWindow(et.getWindowToken(), 0);

                            String text = (et.getText().toString());
                            AddNote addNote = new AddNote();
                            Properties properties = new Properties();
                            properties.put("box", pair.first);
                            properties.put("text", text);
                            addNote.execute(properties);
                            addCommendUndo(addNote);
                            MainActivity.menu.getItem(5).setVisible(true);
                            lay.updateBoxWithText(pair.first);

                            dialog.dismiss();
                        }
                    });


                    et.setText(((IPlainNotesContent) pair.first.topic.getNotes().getContent(INotes.PLAIN)).getTextContent());

                    InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.showSoftInput(et, InputMethodManager.SHOW_IMPLICIT);

                    dialog.show();

                } else if (pair.second == Actions.COLLAPSE) {
                    Utils.fireSetVisible(pair.first, true);
                } else if (pair.second == Actions.EXPAND) {
                    Utils.fireSetVisible(pair.first, false);
                }
            }

            return true;
        }

        @Override
        public void onLongPress(MotionEvent e) {
            final Pair<Box, IRelationship> pair = Utils.whichRelationship(lay, e, 0);
            if (pair != null) {
                final Properties properties = new Properties();
                final Dialog dialog = new Dialog(MainActivity.this);
                dialog.setContentView(R.layout.dialog_edit_rel);
                dialog.setTitle("Content");
                final Button btn = (Button) dialog.findViewById(R.id.dialogButtonOK);
                final EditText et = (EditText) dialog.findViewById(R.id.editText);
                et.requestFocus();
                final Button btn2 = (Button) dialog.findViewById(R.id.button2);
                btn2.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        dialog.dismiss();
                    }
                });
                final ImageButton imgbtn = (ImageButton) dialog.findViewById(R.id.imageButton);
                if (pair.first.relationships.get(pair.second) != null && pair.first.drawableShape.getBounds().left < pair.first.relationships.get(pair.second).drawableShape.getBounds().left) {
                    imgbtn.setBackground(MainActivity.this.getResources().getDrawable(R.drawable.ic_action_forward));
                } else {
                    imgbtn.setBackground(MainActivity.this.getResources().getDrawable(R.drawable.ic_action_back));
                    leftRelationship = false;
                }
                imgbtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        if (leftRelationship) {
                            imgbtn.setBackground(MainActivity.this.getResources().getDrawable(R.drawable.ic_action_back));
                            leftRelationship = false;
                        } else {
                            imgbtn.setBackground(MainActivity.this.getResources().getDrawable(R.drawable.ic_action_forward));
                            leftRelationship = true;
                        }
                        if (properties.containsKey("next_start")) {
                            properties.remove("new_start");
                        } else {
                            properties.put("new_start", pair.first.relationships.get(pair.second));
                        }
                    }
                });
                et.requestFocus();

                btn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                        imm.hideSoftInputFromWindow(et.getWindowToken(), 0);

                        String text = (et.getText().toString());
                        EditRelationship editRelationship = new EditRelationship();
                        properties.put("box", pair.first);
                        properties.put("text", text);
                        properties.put("relation", pair.second);
                        editRelationship.execute(properties);
                        addCommendUndo(editRelationship);
                        //lay.invalidate();
                        dialog.dismiss();
                    }
                });

                final int MAX_LINES = 3;

                //ogranicza do 3 linii widok w zawartości bloczka
                et.addTextChangedListener(new TextWatcher() {
                    private int lines;

                    @Override
                    public void onTextChanged(CharSequence s, int start, int before, int count) {
                    }

                    @Override
                    public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                        lines = Utils.countLines(s.toString());
                    }

                    @Override
                    public void afterTextChanged(Editable s) {
                        int counter = Utils.countLines(s.toString());

                        int diff = lines - counter;
                        if (diff > 0) {
                            //w gore
                            if (counter < MAX_LINES - 1 && et.getLayoutParams().height > 75) {
                                LinearLayout.LayoutParams buttonLayoutParams = (LinearLayout.LayoutParams) btn.getLayoutParams();
                                buttonLayoutParams.setMargins(buttonLayoutParams.leftMargin, buttonLayoutParams.topMargin - 30,
                                        buttonLayoutParams.rightMargin, buttonLayoutParams.bottomMargin);
                                btn.setLayoutParams(buttonLayoutParams);
                                btn2.setLayoutParams(buttonLayoutParams);
                                et.getLayoutParams().height -= 30;
                            }
                        } else if (diff < 0) {
                            //w dol
                            if (counter < MAX_LINES && et.getLayoutParams().height < 135) {
                                LinearLayout.LayoutParams buttonLayoutParams = (LinearLayout.LayoutParams) btn.getLayoutParams();
                                buttonLayoutParams.setMargins(buttonLayoutParams.leftMargin, buttonLayoutParams.topMargin + 30,
                                        buttonLayoutParams.rightMargin, buttonLayoutParams.bottomMargin);
                                btn.setLayoutParams(buttonLayoutParams);
                                btn2.setLayoutParams(buttonLayoutParams);
                                et.getLayoutParams().height += 30;
                            }
                        }
                    }
                });

                et.setText(pair.second.getTitleText());
                int k = Utils.countLines(et.getText().toString());
                int ile = Math.min(MAX_LINES - 1, k);

                et.getLayoutParams().height = 75 + ile * 30;
                LinearLayout.LayoutParams buttonLayoutParams = (LinearLayout.LayoutParams) btn.getLayoutParams();
                buttonLayoutParams.setMargins(buttonLayoutParams.leftMargin,
                        buttonLayoutParams.topMargin + 30 * ((k < 2) ? 0 : (k == 2) ? ile - 1 : ile),
                        buttonLayoutParams.rightMargin, buttonLayoutParams.bottomMargin);
                btn.setLayoutParams(buttonLayoutParams);

                InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.showSoftInput(et, InputMethodManager.SHOW_IMPLICIT);

                dialog.show();

            }
        }


        public void updateChildrenConnections(Box box) {
            for (Line line : box.getLines().values()) {
                if (box.drawableShape.getBounds().left <= root.drawableShape.getBounds().centerX()) {
                    line.setStart(new pl.edu.agh.mindmapex.common.Point(box.drawableShape.getBounds().left, box.drawableShape.getBounds().centerY()));
                } else {
                    line.setStart(new pl.edu.agh.mindmapex.common.Point(box.drawableShape.getBounds().right, box.drawableShape.getBounds().centerY()));
                }
            }
            for (Box child : box.getChildren()) {
                updateChildrenConnections(child);
            }
        }


        @Override
        public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {

            boolean mIsScrolling = false;
            if (click && clicked != null && clicked.topic.getParent() == null && !clicked.topic.isRoot() && clicked.isSelected) {
                mIsScrolling = true;
                int newx = (int) (e2.getX() - lay.transx);
                int newy = (int) (e2.getY() - lay.transy);

                newx /= lay.zoomx;
                newy /= lay.zoomy;

                int x = (newx) - (clicked.point.x);
                int y = (newy) - (clicked.point.y);
                clicked.setPoint(new pl.edu.agh.mindmapex.common.Point(newx, newy));


                clicked.prepareDrawableShape();
                for (Box box : toEditBoxes) {
                    box.setPoint(new pl.edu.agh.mindmapex.common.Point(box.point.x + x, box.point.y + y));
                    box.prepareDrawableShape();
                }

                return false;
            } else if (click && clicked != null && clicked.isSelected) {

                mIsScrolling = true;
                int newx = (int) (e2.getX() - lay.transx);
                int newy = (int) (e2.getY() - lay.transy);

                newx /= lay.zoomx;
                newy /= lay.zoomy;

                // int x =   Math.abs(newx) - Math.abs(clicked.point.x);
//                int y =  Math.abs(newy) - Math.abs(clicked.point.y);
                int x = (newx) - (clicked.point.x);
                int y = (newy) - (clicked.point.y);
                for (Box box : toEditBoxes) {
                    box.setPoint(new pl.edu.agh.mindmapex.common.Point(box.point.x + x, box.point.y + y));
                    box.prepareDrawableShape();
                    if (box.getLines().size() > 0) {
                        for (Box b : box.getLines().keySet()) {
                            if (!box.topic.isRoot()) {
                                if (box.drawableShape.getBounds().left <= root.drawableShape.getBounds().centerX()) {
                                    box.getLines().get(b).setStart(new pl.edu.agh.mindmapex.common.Point(box.getDrawableShape().getBounds().left, box.getDrawableShape().getBounds().top + box.getHeight() / 2));
                                } else {
                                    box.getLines().get(b).setStart(new pl.edu.agh.mindmapex.common.Point(box.getDrawableShape().getBounds().right, box.getDrawableShape().getBounds().top + box.getHeight() / 2));
                                }
                                updateChildrenConnections(box);
                            } else {
                                if (b.drawableShape.getBounds().left <= root.drawableShape.getBounds().centerX()) {
                                    box.getLines().get(b).setStart(new pl.edu.agh.mindmapex.common.Point(box.getDrawableShape().getBounds().left, box.getDrawableShape().getBounds().top + box.getHeight() / 2));
                                } else {
                                    box.getLines().get(b).setStart(new pl.edu.agh.mindmapex.common.Point(box.getDrawableShape().getBounds().right, box.getDrawableShape().getBounds().top + box.getHeight() / 2));
                                }

                            }
                        }
                    }
                    if (box.parent != null) {
                        if (box.parent.getLines().get(box) != null) {
                            if (box.drawableShape.getBounds().left <= root.drawableShape.getBounds().centerX()) {
                                box.parent.getLines().get(box).setStart(new pl.edu.agh.mindmapex.common.Point(box.parent.getDrawableShape().getBounds().left, box.parent.getDrawableShape().getBounds().centerY()));
                                box.parent.getLines().get(box).setEnd(new pl.edu.agh.mindmapex.common.Point(box.getDrawableShape().getBounds().right, box.getDrawableShape().getBounds().top + box.getHeight() / 2));
                            } else {
                                box.parent.getLines().get(box).setEnd(new pl.edu.agh.mindmapex.common.Point(box.getDrawableShape().getBounds().left, box.getDrawableShape().getBounds().top + box.getHeight() / 2));
                                box.parent.getLines().get(box).setStart(new pl.edu.agh.mindmapex.common.Point(box.parent.getDrawableShape().getBounds().right, box.parent.getDrawableShape().getBounds().centerY()));
                            }

                        }
                    }
                }
                clicked.setPoint(new pl.edu.agh.mindmapex.common.Point(newx, newy));
                clicked.prepareDrawableShape();

                if (clicked.getLines().size() > 0) {
                    for (Box box : clicked.getLines().keySet()) {
                        if (!clicked.topic.isRoot()) {
                            if (clicked.drawableShape.getBounds().left <= root.drawableShape.getBounds().centerX()) {
                                clicked.getLines().get(box).setStart(new pl.edu.agh.mindmapex.common.Point(clicked.getDrawableShape().getBounds().left, clicked.getDrawableShape().getBounds().top + clicked.getHeight() / 2));
                            } else {
                                clicked.getLines().get(box).setStart(new pl.edu.agh.mindmapex.common.Point(clicked.getDrawableShape().getBounds().right, clicked.getDrawableShape().getBounds().top + clicked.getHeight() / 2));
                            }
                            updateChildrenConnections(clicked);
                        } else {
                            if (box.drawableShape.getBounds().left <= root.drawableShape.getBounds().centerX()) {
                                clicked.getLines().get(box).setStart(new pl.edu.agh.mindmapex.common.Point(clicked.getDrawableShape().getBounds().left, clicked.getDrawableShape().getBounds().top + clicked.getHeight() / 2));
                            } else {
                                clicked.getLines().get(box).setStart(new pl.edu.agh.mindmapex.common.Point(clicked.getDrawableShape().getBounds().right, clicked.getDrawableShape().getBounds().top + clicked.getHeight() / 2));
                            }

                        }
                    }
                }
                if (clicked.parent != null) {
                    if (clicked.parent.getLines().get(clicked) != null) {
                        if (clicked.drawableShape.getBounds().left <= root.drawableShape.getBounds().centerX()) {
                            clicked.parent.getLines().get(clicked).setStart(new pl.edu.agh.mindmapex.common.Point(clicked.parent.getDrawableShape().getBounds().left, clicked.parent.getDrawableShape().getBounds().centerY()));
                            clicked.parent.getLines().get(clicked).setEnd(new pl.edu.agh.mindmapex.common.Point(clicked.getDrawableShape().getBounds().right, clicked.getDrawableShape().getBounds().top + clicked.getHeight() / 2));
                        } else {
                            clicked.parent.getLines().get(clicked).setEnd(new pl.edu.agh.mindmapex.common.Point(clicked.getDrawableShape().getBounds().left, clicked.getDrawableShape().getBounds().top + clicked.getHeight() / 2));
                            clicked.parent.getLines().get(clicked).setStart(new pl.edu.agh.mindmapex.common.Point(clicked.parent.getDrawableShape().getBounds().right, clicked.parent.getDrawableShape().getBounds().centerY()));
                        }

                    }
                }
                return false;
            }


            if (!mScaling) {
                lay.transx -= distanceX;
                lay.transy -= distanceY;
            }
            return false;
        }

        @Override
        public boolean onDown(MotionEvent e) {
            mScaling = false;

            if (lay != null && lay.getMatrix() != null) {
                if (mActionMode != null && mActionMode.getTitle().toString().equalsIgnoreCase("move")) {
                    if (Utils.whichBox(lay, e) == clicked) {
                        click = true;
                    }
                } else {
                    clicked = Utils.whichBox(lay, e);
                    if (clicked != null) {
                        click = true;
                    }
                }
            }
            return true;
        }

        @Override
        public boolean onDoubleTap(MotionEvent e) {
            if (mActionMode != null && mActionMode.getTitle().toString().equalsIgnoreCase("move")) {
                return true;
            }

            if (Utils.whichBox(lay, e) != null) {
                editContent(Utils.whichBox(lay, e), null);
                return true;
            }

            return true;
        }
    }

    private boolean isFirstTimeInApp() {
        boolean firstTimeInAppKey = mSharedPrefs.getBoolean(FIRST_TIME_IN_APP_KEY, true);
        if (firstTimeInAppKey)
            mSharedPrefs.edit().putBoolean(FIRST_TIME_IN_APP_KEY, false).apply();

        return firstTimeInAppKey;
    }


    private void editContent(final Box myClicked, final AddBox addBox) {
        final Dialog dialog = DialogFactory.boxContentDialog(MainActivity.this);
        final Button btn = (Button) dialog.findViewById(R.id.dialogButtonOK);
        final EditText et = (EditText) dialog.findViewById(R.id.editText);
        et.requestFocus();
        final Button btn2 = (Button) dialog.findViewById(R.id.button2);
        btn2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (MainActivity.commandsUndo.size() > 1 && MainActivity.commandsUndo.getLast() instanceof AddBox) {
                    MainActivity.commandsUndo.getLast().undo();
                    MainActivity.commandsUndo.removeLast();
                    menu.getItem(5).setVisible(false);
                    //lay.invalidate();
                    //lay.revalidate();
                } else if (MainActivity.commandsUndo.size() == 1 && MainActivity.commandsUndo.getFirst() instanceof AddBox) {
                    MainActivity.commandsUndo.getFirst().undo();
                    MainActivity.commandsUndo.removeFirst();
                    menu.getItem(5).setVisible(false);
                    //lay.invalidate();
                    //  lay.revalidate();
                }
                dialog.dismiss();
            }
        });

        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(et.getWindowToken(), 0);
                String text = (et.getText().toString());
                EditBox editBox = new EditBox();
                Properties properties = new Properties();
                properties.put("box", myClicked);
                properties.put("box_text", text);
                editBox.execute(properties);
                MainActivity.menu.getItem(5).setVisible(true);
                if (addBox != null) {
                    addBox.name = myClicked.topic.getTitleText();
                }
                lay.updateBoxWithText(myClicked);
                dialog.dismiss();
            }
        });

        et.setText(myClicked.topic.getTitleText());

        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.showSoftInput(et, InputMethodManager.SHOW_IMPLICIT);

        dialog.show();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MainActivity.menu = menu;
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.map_menu, menu);
        menu.getItem(1).setVisible(false);
        menu.getItem(2).setVisible(false);
        menu.getItem(3).setVisible(false);
        menu.getItem(4).setVisible(false);
        menu.getItem(5).setVisible(false);
        menu.getItem(6).setVisible(false);
        return super.onCreateOptionsMenu(menu);
    }
    //}

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
        switch (item.getItemId()) {
            case R.id.action_settings:
                Intent intent = new Intent(MainActivity.this, EditSheetScreen.class);
                if (sheet1.getStyleId() != null && styleSheet.findStyle(sheet1.getStyleId()) != null && styleSheet.findStyle(sheet1.getStyleId()).getProperty(Styles.FillColor) != null) {
                    intent.putExtra(BACKGROUNDCOLOR, Color.parseColor(styleSheet.findStyle(sheet1.getStyleId()).getProperty(Styles.FillColor)));
                } else {
                    intent.putExtra(BACKGROUNDCOLOR, Color.WHITE);
                }
                startActivity(intent);
                return true;
            case R.id.action_undo:
                if (commandsUndo.size() == 1) {
                    commandsUndo.getFirst().undo();
                    if (commandsUndo.getFirst() instanceof EditBox) {
                        Callback call = new Callback() {
                            @Override
                            public void execute() {
                                lay.updateBoxWithText(((EditBox) commandsUndo.getFirst()).box);
                                for (Box b : ((EditBox) commandsUndo.getLast()).edited) {
                                    lay.updateBoxWithText(b);
                                }
                            }
                        };
                    } else if (commandsUndo.getFirst() instanceof EditSheet) {
                        lay.setBackgroundColor((Color.parseColor(MainActivity.styleSheet.findStyle(sheet1.getStyleId()).getProperty(Styles.FillColor))));
                    } else {
                        Callback call = new Callback() {
                            @Override
                            public void execute() {
                            }
                        };
                        try {
                            AsyncInvalidate async = new AsyncInvalidate(MainActivity.this);
                            async.setCallback(call);
                            async.execute();
                        } catch (Exception e1) {
                            e1.printStackTrace();
                        }
                    }
                    commandsRedo.add(commandsUndo.getFirst());
                    commandsUndo.removeFirst();
                    menu.getItem(6).setVisible(true);
                    menu.getItem(5).setVisible(false);
                } else {
                    commandsUndo.getLast().undo();
                    if (commandsUndo.getLast() instanceof EditBox) {
                        lay.updateBoxWithText(((EditBox) commandsUndo.getLast()).box);
                        for (Box b : ((EditBox) commandsUndo.getLast()).edited) {
                            lay.updateBoxWithText(b);
                        }
                    } else if (commandsUndo.getLast() instanceof EditSheet) {
                        lay.setBackgroundColor(Integer.parseInt(sheet1.getTheme().getProperty(Styles.FillColor)));
                    } else if (commandsUndo.getLast() instanceof AddBox || commandsUndo.getLast() instanceof RemoveLine || commandsUndo.getLast() instanceof RemoveBox) {
                    }
                    commandsRedo.add(commandsUndo.getLast());
                    menu.getItem(6).setVisible(true);
                    commandsUndo.removeLast();
                }
                return true;
            case R.id.action_new:
                IStyle boxEditedStyle = workbook.getStyleSheet().findStyle(boxEdited.topic.getStyleId());
                Intent intent1 = new Intent(MainActivity.this, EditBoxScreen.class);
                if (boxEditedStyle != null) {

                    intent1.putExtra(EditBoxScreen.BOX_COLOR, boxEditedStyle.getProperty(Styles.FillColor));
                    intent1.putExtra(EditBoxScreen.TEXT_COLOR, boxEdited.topic.getTitleText());
                    intent1.putExtra(EditBoxScreen.LINE_SHAPE, boxEditedStyle.getProperty(Styles.LineClass));
                    intent1.putExtra(EditBoxScreen.LINE_COLOR, boxEditedStyle.getProperty(Styles.LineColor));
                    intent1.putExtra(EditBoxScreen.BOX_SHAPE, boxEditedStyle.getProperty(Styles.ShapeClass));
                    intent1.putExtra(EditBoxScreen.LINE_THICKNESS, boxEditedStyle.getProperty(Styles.LineWidth));
                }
                startActivity(intent1);
                // lay.invalidateDrawable(boxEdited.drawableShape);
                return true;
            case R.id.new_line:
                Properties properties1 = new Properties();
                properties1.put("child", MainActivity.toEditBoxes.getFirst());
                if (MainActivity.toEditBoxes.size() == 2) {
                    properties1.put("parent", MainActivity.toEditBoxes.getLast());
                }
                AddLine addLine = new AddLine();
                addLine.execute(properties1);
                MainActivity.addCommendUndo(addLine);
                //   lay.invalidate();
                return true;
            case R.id.action_redo:
                if (commandsRedo.size() == 1) {
                    commandsRedo.getFirst().redo();
                    if (commandsRedo.getFirst() instanceof EditBox) {
                        //     Callback call = new Callback() {
                        //         @Override
                        //        public void execute() {
                        lay.updateBoxWithText(((EditBox) commandsRedo.getFirst()).box);
                        for (Box b : ((EditBox) commandsRedo.getLast()).edited) {
                            lay.updateBoxWithText(b);
                        }


                    } else if (commandsRedo.getFirst() instanceof EditSheet) {
                        lay.setBackgroundColor((Color.parseColor(MainActivity.styleSheet.findStyle(sheet1.getStyleId()).getProperty(Styles.FillColor))));
                    }
                    //    lay.invalidate();
                    commandsUndo.add(commandsRedo.getFirst());
                    commandsRedo.removeFirst();
                    menu.getItem(5).setVisible(true);
                    menu.getItem(6).setVisible(false);
                } else {
                    commandsRedo.getLast().redo();
                    if (commandsRedo.getLast() instanceof EditBox) {
                        lay.updateBoxWithText(((EditBox) commandsRedo.getLast()).box);
                        for (Box b : ((EditBox) commandsRedo.getLast()).edited) {
                            lay.updateBoxWithText(b);
                        }

                    } else if (commandsRedo.getLast() instanceof EditSheet) {
                        lay.setBackgroundColor(Integer.parseInt(sheet1.getTheme().getProperty(Styles.FillColor)));
                    }
                    //  lay.invalidate();
                    commandsUndo.add(commandsRedo.getLast());
                    menu.getItem(5).setVisible(true);
                    commandsRedo.removeLast();
                }
                return true;
            case R.id.action_trash:
                RemoveBox removeBox = new RemoveBox();
                Properties properties = new Properties();
                HashMap<Box, Line> boxes = new HashMap<>();
                boxes.put(MainActivity.boxEdited, MainActivity.boxEdited.parent.getLines().get(MainActivity.boxEdited));
                for (Box b : MainActivity.toEditBoxes) {
                    b.isSelected = false;
                    boxes.put(b, b.parent.getLines().get(b));
                    if (boxes.size() > 0) {
                        properties.put("boxes", boxes);
                        removeBox.execute(properties);
                        MainActivity.addCommendUndo(removeBox);
                    }
                    //  return true;
                }
                menu.getItem(4).setVisible(false);
                menu.getItem(1).setVisible(false);
                menu.getItem(2).setVisible(false);
                menu.getItem(3).setVisible(false);
                MainActivity.toEditBoxes.clear();
                //   lay.invalidate();
                return true;
            case R.id.new_rel:
                if (!MainActivity.toEditBoxes.getFirst().relationships.containsValue(MainActivity.toEditBoxes.getLast())) {
                    final Dialog dialog = DialogFactory.boxContentDialog(MainActivity.this);
                    final Button btn = (Button) dialog.findViewById(R.id.dialogButtonOK);
                    final EditText et = (EditText) dialog.findViewById(R.id.editText);
                    et.requestFocus();
                    final Button btn2 = (Button) dialog.findViewById(R.id.button2);
                    btn2.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            dialog.dismiss();
                        }
                    });

                    btn.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            Callback call = null;

                            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                            imm.hideSoftInputFromWindow(et.getWindowToken(), 0);
                            String text = (et.getText().toString());
                            AddRelationship addRel = new AddRelationship();
                            Properties p = new Properties();
                            p.put("boxes", MainActivity.toEditBoxes);
                            p.put("text", text);
                            addRel.execute(p);
                            MainActivity.addCommendUndo(addRel);
                            lay.drawRelationship(MainActivity.toEditBoxes.getFirst(), addRel.relation);
                            dialog.dismiss();

                        }
                    });

                    final int MAX_LINES = 3;

                    //ogranicza do 3 linii widok w zawartości bloczka
                    et.addTextChangedListener(new TextWatcher() {
                        private int lines;

                        @Override
                        public void onTextChanged(CharSequence s, int start, int before, int count) {
                        }

                        @Override
                        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                            lines = Utils.countLines(s.toString());
                        }

                        @Override
                        public void afterTextChanged(Editable s) {
                            int counter = Utils.countLines(s.toString());

                            int diff = lines - counter;
                            if (diff > 0) {
                                //w gore
                                if (counter < MAX_LINES - 1 && et.getLayoutParams().height > 75) {
                                    LinearLayout.LayoutParams buttonLayoutParams = (LinearLayout.LayoutParams) btn.getLayoutParams();
                                    buttonLayoutParams.setMargins(buttonLayoutParams.leftMargin, buttonLayoutParams.topMargin - 30,
                                            buttonLayoutParams.rightMargin, buttonLayoutParams.bottomMargin);
                                    btn.setLayoutParams(buttonLayoutParams);
                                    btn2.setLayoutParams(buttonLayoutParams);
                                    et.getLayoutParams().height -= 30;
                                }
                            } else if (diff < 0) {
                                //w dol
                                if (counter < MAX_LINES && et.getLayoutParams().height < 135) {
                                    LinearLayout.LayoutParams buttonLayoutParams = (LinearLayout.LayoutParams) btn.getLayoutParams();
                                    buttonLayoutParams.setMargins(buttonLayoutParams.leftMargin, buttonLayoutParams.topMargin + 30,
                                            buttonLayoutParams.rightMargin, buttonLayoutParams.bottomMargin);
                                    btn.setLayoutParams(buttonLayoutParams);
                                    btn2.setLayoutParams(buttonLayoutParams);
                                    et.getLayoutParams().height += 30;
                                }
                            }
                        }
                    });

//                    et.setText(pair.first.topic.getNotes().getContent(INotes.PLAIN).getFormat());
                    int k = Utils.countLines(et.getText().toString());
                    int ile = Math.min(MAX_LINES - 1, k);

                    et.getLayoutParams().height = 75 + ile * 30;
                    LinearLayout.LayoutParams buttonLayoutParams = (LinearLayout.LayoutParams) btn.getLayoutParams();
                    buttonLayoutParams.setMargins(buttonLayoutParams.leftMargin,
                            buttonLayoutParams.topMargin + 30 * ((k < 2) ? 0 : (k == 2) ? ile - 1 : ile),
                            buttonLayoutParams.rightMargin, buttonLayoutParams.bottomMargin);
                    btn.setLayoutParams(buttonLayoutParams);
                    btn2.setLayoutParams(buttonLayoutParams);

                    InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.showSoftInput(et, InputMethodManager.SHOW_IMPLICIT);

                    dialog.show();
                } else {
                    RemoveRelationship remRel = new RemoveRelationship();
                    Properties p = new Properties();
                    p.put("boxes", MainActivity.toEditBoxes);
                    remRel.execute(p);
                    MainActivity.addCommendUndo(remRel);
                }

            default:
                return super.onContextItemSelected(item);
        }
    }

    public static void addCommendUndo(Command command) {
        if (commandsUndo.size() == 10) {
            commandsUndo.removeFirst();
        }
        commandsUndo.add(command);
        menu.getItem(5).setVisible(true);
        if (boxEdited != null && command instanceof EditBox) {
            Utils.lay.updateBoxWithText(boxEdited);
        }
    }

    public static void changeShape(Box box) {
        IStyle s = workbook.getStyleSheet().findStyle(box.topic.getStyleId());
        if (s.getProperty(Styles.ShapeClass).equals(Styles.TOPIC_SHAPE_DIAMOND)) {
            box.setDrawableShape(res.getDrawable(R.drawable.diammond));
        } else if (s.getProperty(Styles.ShapeClass).equals(Styles.TOPIC_SHAPE_UNDERLINE)) {
            box.setDrawableShape(res.getDrawable(R.drawable.no_border));
        } else if (s.getProperty(Styles.ShapeClass).equals(Styles.TOPIC_SHAPE_NO_BORDER)) {
            box.setDrawableShape(res.getDrawable(R.drawable.no_border));
        } else if (s.getProperty(Styles.ShapeClass).equals(Styles.TOPIC_SHAPE_ELLIPSE)) {
            box.setDrawableShape(res.getDrawable(R.drawable.elipse));
        } else if (s.getProperty(Styles.ShapeClass).equals(Styles.TOPIC_SHAPE_RECT)) {
            box.setDrawableShape(res.getDrawable(R.drawable.rect));
        } else if (s.getProperty(Styles.ShapeClass).equals(Styles.TOPIC_SHAPE_ROUNDEDRECT)) {
            box.setDrawableShape(res.getDrawable(R.drawable.round_rect));
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case REQUEST_WRITE_STORAGE: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    new Thread(new Runnable() {
                        public void run() {
                            File initialDir = Environment.getExternalStorageDirectory();
                            File file = new File(initialDir.getAbsolutePath() + "/mindmap_backup.xmind");
                            LocalWorkbookManager.saveWorkbook(file, MainActivity.workbook, new ResultListener() {

                                @Override
                                public void taskDone(Object result) {
                                    Toast.makeText(MainActivity.this, "mindmap_backup saved on local disk.", Toast.LENGTH_SHORT).show();

                                    finish();
                                }

                                @Override
                                public void taskFailed(Exception exception) {
                                    Toast.makeText(MainActivity.this, "mindmap_backup failed to save.", Toast.LENGTH_SHORT).show();
                                }
                            });
                        }
                    }).start();

                    //reload my activity with permission granted or use the features what required the permission
                } else {
                    Toast.makeText(this, "The app was not allowed to write to your storage. Hence, it cannot function properly. Please consider granting it this permission", Toast.LENGTH_LONG).show();
                }
            }
        }

    }

    private static final int REQUEST_WRITE_STORAGE = 112;

    @Override
    public void onDestroy() {
        super.onDestroy();
        try {
            workbook.saveTemp();
        } catch (IOException | CoreException e) {
            e.printStackTrace();
        }

        boolean hasPermission = (ContextCompat.checkSelfPermission(this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED);
        if (!hasPermission) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                    REQUEST_WRITE_STORAGE);
        } else
            new Thread(new Runnable() {
                public void run() {
                    File initialDir = Environment.getExternalStorageDirectory();
                    File file = new File(initialDir.getAbsolutePath() + "/mindmap_backup.xmind");
                    LocalWorkbookManager.saveWorkbook(file, MainActivity.workbook, new ResultListener() {

                        @Override
                        public void taskDone(Object result) {
                            Toast.makeText(MainActivity.this, "mindmap_backup saved on local disk.", Toast.LENGTH_SHORT).show();
                        }

                        @Override
                        public void taskFailed(Exception exception) {
                            Toast.makeText(MainActivity.this, "mindmap_backup failed to save.", Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            }).start();
        lay.surfaceDestroyed(lay.getHolder());
        lay = null;
        style = null;
    }

}

