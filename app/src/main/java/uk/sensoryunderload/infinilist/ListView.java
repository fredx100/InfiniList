package uk.sensoryunderload.infinilist;

import android.app.Activity;
import android.app.AlertDialog;
import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.ParcelFileDescriptor;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

public class ListView extends AppCompatActivity
                      implements ListItemAdapter.ListControlListener {
    public static final String OPEN_LIST_ACTION = "uk.sensoryunderload.infinilist.widget.OPEN_LIST_ACTION";
    public static final String ADD_ITEM_ACTION = "uk.sensoryunderload.infinilist.widget.ADD_ITEM_ACTION";
    public static final String OPEN_TOP_LIST_ACTION = "uk.sensoryunderload.infinilist.widget.OPEN_TOP_LIST_ACTION";

    private ListItem topLevelList = new ListItem("InfiniList","");
    private ListItem currentList;
    private ListItemAdapter liAdapter;
    private ItemTouchHelper touchHelper;
    private boolean shownHelp;
    private boolean saveNeeded;
    private Boolean widgetUpdateNeeded;
    private ArrayList<Integer> widgetAddress = new ArrayList<Integer>();
    private boolean widgetAddressChanged;

    // Setting key values
    private static final String WIDGET_ADDRESS = "WidgetAddress";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        loadList(); // Sets topLevelList
        shownHelp = (topLevelList.size() != 0);
        currentList = topLevelList;
        loadSettings(getApplicationContext(), widgetAddress);
        Intent intent = getIntent();
        if (intent.getAction().equals(OPEN_LIST_ACTION)) {
            currentList = goToAddress(widgetAddress);
        }

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list_view);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        setTitle();

        ListRecyclerView recyclerView = findViewById(R.id.recycler_view);

        recyclerView.setListLayoutManager(new ListLayoutManager(getApplicationContext()));
        recyclerView.setItemAnimator(new DefaultItemAnimator());

        liAdapter = new ListItemAdapter(currentList, this);
        recyclerView.setAdapter(liAdapter);

        // Setup ItemTouchHelper to handle item dragging
        touchHelper = new ItemTouchHelper(new ListCallback(this));
        touchHelper.attachToRecyclerView(recyclerView);

        registerForContextMenu(recyclerView);
    }

    @Override
    protected void onPause() {
        if (saveNeeded) {
            saveLists();
        }
        if (widgetAddressChanged) {
            saveWidgetAddress();
            widgetUpdateNeeded = true;
        }
        if (widgetUpdateNeeded) {
            broadcastWidgetUpdate();
        }
        super.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (!shownHelp) {
            showHelp();
            shownHelp = true;
        }
        saveNeeded = false;
        widgetUpdateNeeded = false;
        widgetAddressChanged = false;
    }

    private boolean loadList() { return loadList("Main.todo", topLevelList, getApplicationContext()); }
    static boolean loadList(String fileName, ListItem list, Context context) {
        boolean exists = false;
        File path = context.getFilesDir();
        File file = new File(path, fileName);
        if (file.exists()) {
            list.readFromFile(file);
            exists = true;
        }

        return exists;
    }
    static boolean loadSettings(Context context, ArrayList<Integer> widgetAddress) {
        boolean exists = false;
        File path = context.getFilesDir();
        File file = new File(path, "settings");

        if (file.exists()) {
            exists = true;

            try {
                BufferedReader reader = new BufferedReader(new FileReader(file));

                String line = reader.readLine();
                if ((line != null) && !line.equals("")) {
                    String[] values = line.split(":");
                    if (values.length > 1) {
                        switch (values[0]) {
                            case WIDGET_ADDRESS :
                                readWidgetAddress(values[1], widgetAddress);
                                break;
                        }
                    }
                }

                reader.close();
            } catch(Exception e){
                exists = false;
            }
        }

        return exists;
    }

    static void readWidgetAddress(String value, ArrayList<Integer> widgetAddress) {
        if (widgetAddress != null) {
            widgetAddress.clear();
            for (String token : value.split(" ")) {
                try {
                    widgetAddress.add (Integer.parseInt(token));
                } catch (NumberFormatException e) {
                    Log.e("INFLIST-LOG", "(widgetAddress) " + token + " is not a number");
                }
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_list_view, menu);
        return true;
    }

    @Override
    public void onBackPressed() {
        if (currentList.hasParent()) {
            currentList = currentList.getParent();
            liAdapter.itemList = currentList;
            liAdapter.notifyDataSetChanged();
            setTitle();
        } else {
            super.onBackPressed();
        }
    }

    // ListControlListener implementation
    @Override
    public void descend(int position) {
        currentList = currentList.getChild(position);
        liAdapter.itemList = currentList;
        liAdapter.notifyDataSetChanged();
        setTitle();
    }
    @Override
    public void save() {
        saveOnPause();
    }
    @Override
    public void move(int from, int to) {
        currentList.move(from, to);
        updateWidgetAddress (currentList.getAddress(), to, from);
        liAdapter.notifyItemMoved(from, to);
    }
    @Override
    public void startDrag(RecyclerView.ViewHolder viewHolder) {
        touchHelper.startDrag (viewHolder);
    }
    @Override
    public void notifyStatusChange(int itemIndex) {
        updateWidgetAddress (currentList.getAddress(), itemIndex, itemIndex);
    }

    private void setTitle() {
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle(currentList.getTitle());
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        switch (id) {
//            case R.id.action_settings :
//                break;

            case R.id.action_import :
                importLists();
                break;

            case R.id.action_export :
                exportLists();
                break;

            case R.id.action_uncheck_all :
                actionUncheckAll();
                break;

            case R.id.action_delete_all :
                actionDeleteAll();
                break;

            case R.id.action_mark_widget :
                actionMarkWidget(currentList);
                break;

            case R.id.action_add :
                actionAddItem(currentList);
                break;

            case R.id.action_help :
                showHelp();
                break;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        int pos = liAdapter.retrievePosition();
        switch (item.getItemId()) {
            case R.id.delete:
                removeItem(pos);
                break;
            case R.id.addsub:
                actionAddItem(currentList.getChild(pos));
                break;
            case R.id.editsub:
                actionEditItem(currentList.getChild(pos));
                break;
            case R.id.mark_sub_widget:
                actionMarkWidget(currentList.getChild(pos));
                break;
        }
        return super.onContextItemSelected(item);
    }
    // https://developer.android.com/guide/topics/ui/dialogs#java
    // Launches the "Add Item" dialog.
    private void actionAddItem(ListItem list) {
        EditItemFragment dialog = EditItemFragment.newInstance(list, true);
        dialog.show(getSupportFragmentManager(), "add item dialog");
    }
    private void actionEditItem(ListItem list) {
        EditItemFragment dialog = EditItemFragment.newInstance(list, false);
        dialog.show(getSupportFragmentManager(), "edit item dialog");
    }
    private void showHelp() {
        HelpFragment dialog = new HelpFragment();
        dialog.show(getSupportFragmentManager(), "help dialog");
    }
    private void actionUncheckAll() {
        currentList.uncheckAllChildren();
        updateWidgetAddress (currentList.getAddress(), -1, -1);
        liAdapter.notifyDataSetChanged();
        saveOnPause();
    }
    private void actionDeleteAll() {
        AlertDialog.Builder alert = new AlertDialog.Builder(this);
        alert.setTitle(getString(R.string.delete_all));
        alert.setPositiveButton(getString(R.string.dialogPositiveButton), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                ArrayList<Integer> address = currentList.getAddress();
                for (int i = 0; i < currentList.getChildren().size(); ++i) {
                    updateWidgetAddress (address, -1, i);
                }
                currentList.getChildren().clear();
                liAdapter.notifyDataSetChanged();
                saveOnPause();
                dialog.dismiss();
            }
        });

        alert.setNegativeButton(getString(R.string.dialogNegativeButton), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
            }
        });

        alert.show();
    }
    void actionMarkWidget(ListItem list) {
        if (list.size() == 0) {
            Toast.makeText(this, "Refused: the list is empty.", Toast.LENGTH_LONG).show();
        } else {
            widgetAddress = list.getAddress();
            widgetAddressChanged = true;
            Toast.makeText(this, "Accepted.", Toast.LENGTH_LONG).show();
        }
    }

    // If append then append a new ListItem to list, else modify list.
    void editItem(ListItem list, String title, String content, boolean append) {
        if (append) {
            list.add(new ListItem (title, content));
        } else {
            list.setTitle(title);
            list.setContent(content);
        }

        if (list == currentList) {
            if (append) {
                liAdapter.notifyItemInserted(currentList.size() - 1);
            } else {
                setTitle();
            }
        } else if (list.getParent() == currentList) {
            liAdapter.notifyItemChanged(currentList.indexOf(list));
        }
        saveOnPause();

        // Notify widget of changes.
        if (append) {
            updateWidgetAddress (list.getAddress(), -1, -1);
        } else {
            updateWidgetAddress (list.getParent().getAddress(), -1, -1);
        }
    }

    private void removeItem(int position) {
        int childCount = currentList.getChild(position).size();
        if (childCount > 0) {
            AlertDialog.Builder alert = new AlertDialog.Builder(this);
            alert.setTitle(getResources().getQuantityString(R.plurals.delete_with_children, childCount, childCount));
            alert.setPositiveButton(getString(R.string.dialogPositiveButton), new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    currentList.remove(position);
                    liAdapter.notifyItemRemoved(position);
                    updateWidgetAddress (currentList.getAddress(), -1, position);
                    saveOnPause();
                    dialog.dismiss();
                }
            });

            alert.setNegativeButton(getString(R.string.dialogNegativeButton), new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                }
            });

            alert.show();
        } else {
            currentList.remove(position);
            liAdapter.notifyItemRemoved(position);
            updateWidgetAddress (currentList.getAddress(), -1, position);
            saveOnPause();
        }
    }

    private void saveOnPause() { saveNeeded = true; }
    private void saveNow() { saveLists(); }
    private void saveLists() { saveLists("Main.todo"); }
    private void saveLists(String name) {
        File path = getApplicationContext().getFilesDir();
        File file = new File(path, name);
        topLevelList.writeToFile(file);
    }
    private void saveWidgetAddress() {
        StringBuilder sb = new StringBuilder();
        boolean first = true;
        for (Integer i : widgetAddress) {
            if (!first)
                sb.append(' ');
            sb.append(i.toString());
            first = false;
        }
        saveSetting(WIDGET_ADDRESS, sb.toString());
    }
    private void saveSetting(String key, String value) {
        File path = getApplicationContext().getFilesDir();
        File file = new File(path, "settings");
        Charset charset = StandardCharsets.ISO_8859_1;
        ArrayList<String> newLines = new ArrayList<String>();

        try {
            if (file.exists()) {
                FileInputStream fis = new FileInputStream(file);
                BufferedReader br = new BufferedReader(new InputStreamReader(fis, charset));

                String line = null;
                String[] values;
                do {
                    line = br.readLine();
                    if ((line != null) && !line.equals("")) {
                        values = line.split(":");
                        if (!values[0].equals(key)) {
                            newLines.add(line);
                        }
                    }
                } while (line != null);

                fis.close();
            }

            // Append new setting
            newLines.add(key + ":" + value);

            FileWriter fw = new FileWriter(file);
            String nl = System.getProperty("line.separator");
            for (String newLine : newLines) {
                fw.write(newLine + nl);
            }
            fw.close();
        } catch (IOException e) {
            Log.e("INFLIST-LOG", "Error writing " + key + " to disk", e);
        }
    }

    void updateWidgetAddress (ArrayList<Integer> changedAddress,
                              int insertedAt, int removedFrom) {
        if (changedAddress.size() == widgetAddress.size()) {
            // If the address lengths match then the address of the
            // widget list cannot change. Instead, we check whether
            // the list displayed in the widget has been changed and
            // should be updated.
            boolean addressesMatch = true;
            for (int i = 0; i < widgetAddress.size(); ++i) {
                if (!changedAddress.get(i).equals(widgetAddress.get(i))) {
                    addressesMatch = false;
                    break;
                }
            }
            widgetUpdateNeeded |= addressesMatch;
        } else {
            widgetAddressChanged = updateWidgetAddress (widgetAddress, changedAddress, insertedAt, removedFrom);
        }
    }

    // An element has been insertedAt and/or removedFrom the respective
    // list positions (insertedAd == -1 => nothing inserted, etc.) in
    // list at address changedAddress. Update address as necessary.
    //
    // The object widgetUpdateNeeded is set to true if the list
    // displayed in the widget has changed.
    //
    // Return true if the address of the widget list is changed, false
    // otherwise.
    static boolean updateWidgetAddress (ArrayList<Integer> address,
                                        ArrayList<Integer> changedAddress,
                                        int insertedAt, int removedFrom) {
        boolean addressChanged = false;

        if (changedAddress.size() <= address.size()) {
            boolean intersects = true;

            for (int i = 0; i < changedAddress.size(); ++i) {
                if (!changedAddress.get(i).equals(address.get(i))) {
                    intersects = false;
                    break;
                }
            }

            if (intersects) {
                boolean lengthsDiffer = (changedAddress.size() != address.size());

                if (lengthsDiffer) {
                    // Check to see whether list address must change
                    if (removedFrom == address.get(changedAddress.size())) {
                        if (insertedAt == -1) {
                            // Target list deleted!
                            address.clear();
                        } else {
                            // Target list moved.
                            address.set(changedAddress.size(), insertedAt);
                        }
                        addressChanged = true;
                    } else {
                        Integer existing = address.get(changedAddress.size());
                        Integer difference = 0;
                        if ((insertedAt != -1) && (insertedAt <= existing)) {
                            ++difference;
                        }
                        if ((removedFrom != -1) && (removedFrom < existing)) {
                            --difference;
                        }

                        if (difference != 0) {
                            address.set(changedAddress.size(), address.get(changedAddress.size()) + difference);
                            addressChanged = true;
                        }
                    }
                }
            }
        }

        return addressChanged;
    }

    // Broadcast that widget list has changed and the widget should be
    // updated.
    private void broadcastWidgetUpdate() {
        Intent intent = new Intent(this, ListWidgetProvider.class);
        intent.setAction(AppWidgetManager.ACTION_APPWIDGET_UPDATE);
        int[] ids = AppWidgetManager.getInstance(getApplication())
                        .getAppWidgetIds(new ComponentName(getApplication(), ListWidgetProvider.class));
        intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, ids);
        sendBroadcast(intent);
    }

    private static final int EXPORT_REQUEST_CODE = 9987; // random!
    private static final int IMPORT_REQUEST_CODE = 9986; // random!

    void importLists() {
        // Create the text message with a string
        Intent importIntent = new Intent();
        importIntent.setAction(Intent.ACTION_GET_CONTENT);
        importIntent.setType("*/*");

        // Verify that the intent will resolve to an activity
        if (importIntent.resolveActivity(getPackageManager()) != null) {
            startActivityForResult(importIntent, IMPORT_REQUEST_CODE);
        }
    }

    private void exportLists() {
        DateFormat dateFormat = new SimpleDateFormat("yyyyMMdd");
        Date date = new Date();
        String fileName = "exported-" + dateFormat.format(date) + ".todo";

        // Create the text message with a string
        Intent saveIntent = new Intent();
        saveIntent.setAction(Intent.ACTION_CREATE_DOCUMENT);
        saveIntent.setType("text/todo");
        saveIntent.addCategory(Intent.CATEGORY_OPENABLE);
        saveIntent.putExtra(Intent.EXTRA_TITLE, fileName);

        // Verify that the intent will resolve to an activity
        if (saveIntent.resolveActivity(getPackageManager()) != null) {
            startActivityForResult(saveIntent, EXPORT_REQUEST_CODE);
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent resultData) {
        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == EXPORT_REQUEST_CODE) {
                exportToFile(resultData);
            } else if (requestCode == IMPORT_REQUEST_CODE) {
                importFromFile(resultData);
            }
        } else {
            Toast.makeText(this, "File open intent failed.", Toast.LENGTH_LONG).show();
        }
    }

    private void importFromFile(Intent resultData) {
        if (resultData != null) {
            Uri uri = resultData.getData();
            if (uri != null) {
                try {
                    ParcelFileDescriptor pfd = getContentResolver().
                        openFileDescriptor(uri, "r");
                    if (pfd != null) {
                        ListItem li = new ListItem("InfiniList", "");
                        li.readFromDescriptor(pfd.getFileDescriptor());
                        topLevelList = li;
                        currentList = li;
                        liAdapter.itemList = currentList;
                        liAdapter.notifyDataSetChanged();
                        setTitle();
                        // Let the document provider know you're done by closing the pfd.
                        pfd.close();
                        // Postponed saves/widget updates doesn't work as the app is
                        // "Resumed" after the dialog closes (which clears the relevant
                        // variables). Do the actual saving/updating here.
                        saveNow();
                        broadcastWidgetUpdate();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private void exportToFile(Intent resultData) {
        // The document selected by the user won't be returned in the intent.
        // Instead, a URI to that document will be contained in the return intent
        // provided to this method as a parameter.
        // Pull that URI using resultData.getData().
        if (resultData != null) {
            Uri uri = resultData.getData();
            if (uri != null) {
                String lps = uri.getLastPathSegment();
                if (lps != null) {
                    Toast.makeText(this, "Exporting to " + lps.substring(lps.indexOf(':') + 1), Toast.LENGTH_LONG).show();
                }

                try {
                    ParcelFileDescriptor pfd = getContentResolver().
                        openFileDescriptor(uri, "w");
                    if (pfd != null){
                        topLevelList.writeToDescriptor(pfd.getFileDescriptor());
                        // Let the document provider know you're done by closing the pfd.
                        pfd.close();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    ListItem goToAddress(ArrayList<Integer> address) {
        return topLevelList.goToAddress(address);
    }
}
