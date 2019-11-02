package uk.sensoryunderload.infinilist;

import android.os.Bundle;
import android.os.ParcelFileDescriptor;
import android.app.Activity;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;
import android.content.Intent;
import android.net.Uri;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import java.io.File;
import java.io.IOException;
import java.io.FileNotFoundException;
import java.util.ArrayList;

public class ListView extends AppCompatActivity
                      implements ListItemAdapter.ListControlListener {
    private ListItem topLevelList = new ListItem("InfiniList","");
    private ListItem currentList;
    private ListItemAdapter liAdapter;
    private ItemTouchHelper touchHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        loadLists(); // Sets topLevelList
        // Set current list to appropriate sub-list, if appropriate.
        ArrayList<Integer> address = new ArrayList<Integer>();
        if (savedInstanceState != null) {
            // TODO: get the address from the bundle.
        }
        currentList = topLevelList.goToAddress(address);
        setTitle();

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list_view);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        RecyclerView recyclerView = findViewById(R.id.recycler_view);

        liAdapter = new ListItemAdapter(currentList, this);
        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(getApplicationContext());
        recyclerView.setLayoutManager(mLayoutManager);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.setAdapter(liAdapter);

        // Setup ItemTouchHelper to handle item dragging
        touchHelper = new ItemTouchHelper(new ListCallback(this));
        touchHelper.attachToRecyclerView(recyclerView);

        registerForContextMenu(recyclerView);
    }

    private void loadLists() { loadLists("Main.todo"); }
    private void loadLists(String name) {
        File path = getApplicationContext().getFilesDir();
        File file = new File(path, name);
        if (file.exists()) {
            topLevelList.readFromFile(file);
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
        saveLists();
    }
    @Override
    public void move(int from, int to) {
        currentList.move(from, to);
        liAdapter.notifyItemMoved(from, to);
    }
    @Override
    public void startDrag(RecyclerView.ViewHolder viewHolder) {
        touchHelper.startDrag (viewHolder);
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
            case R.id.action_settings :
                break;

            case R.id.action_export :
                exportLists();
                break;

            case R.id.action_add :
                actionAddItem(currentList);
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
        saveLists();
    }

    private void removeItem(int position) {
        currentList.remove(position);
        liAdapter.notifyItemRemoved(position);
        saveLists();
    }

    private void saveLists() { saveLists("Main.todo"); }
    private void saveLists(String name) {
        File path = getApplicationContext().getFilesDir();
        File file = new File(path, name);
        topLevelList.writeToFile(file);
    }

    private static final int READ_REQUEST_CODE = 9987; // random!

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
            startActivityForResult(saveIntent, READ_REQUEST_CODE);
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent resultData) {
        if (requestCode == READ_REQUEST_CODE) {
            if (resultCode == Activity.RESULT_OK) {
                // The document selected by the user won't be returned in the intent.
                // Instead, a URI to that document will be contained in the return intent
                // provided to this method as a parameter.
                // Pull that URI using resultData.getData().
                Uri uri;
                if (resultData != null) {
                    uri = resultData.getData();
                    String lps = uri.getLastPathSegment();
                    Toast.makeText(this, "Exporting to " + lps.substring(lps.indexOf(':') + 1), Toast.LENGTH_LONG).show();

                    try {
                        ParcelFileDescriptor pfd = getContentResolver().
                                openFileDescriptor(uri, "w");
                        topLevelList.writeToDescriptor(pfd.getFileDescriptor());
                        // Let the document provider know you're done by closing the pfd.
                        pfd.close();
                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            } else {
                Toast.makeText(this, "File open intent failed.", Toast.LENGTH_LONG).show();
            }
        }
    }


    @Override
    public void onSaveInstanceState(Bundle state) {
        // TODO: save list data.
        // How to demark which list we're on?
        super.onSaveInstanceState(state);
    }

    ListItem goToAddress(ArrayList<Integer> address) {
        return topLevelList.goToAddress(address);
    }
}
