package uk.sensoryunderload.infinilist;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;

import java.io.File;
import java.util.ArrayList;

public class ListView extends AppCompatActivity implements ListItemAdapter.DescendClickListener {
    private ListItem topLevelList = new ListItem("InfiniList","");
    private ListItem currentList;
    private RecyclerView recyclerView;
    private ListItemAdapter liAdapter;

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

        recyclerView = findViewById(R.id.recycler_view);

        liAdapter = new ListItemAdapter(currentList, this);
        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(getApplicationContext());
        recyclerView.setLayoutManager(mLayoutManager);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.setAdapter(liAdapter);

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

    @Override
    public void descendClick(int position) {
        currentList = currentList.getChild(position);
        liAdapter.itemList = currentList;
        liAdapter.notifyDataSetChanged();
        setTitle();
    }

    private void setTitle() {
        if (getActionBar() != null) {
            getActionBar().setTitle(currentList.getTitle());
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

            case R.id.action_add :
                actionAddItem(currentList);
                break;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        int pos = liAdaptor.retrievePosition();
        switch (item.getItemId()) {
            case R.id.delete:
                removeItem(pos);
                break;
            case R.id.addsub:
                ListItem temp = currentList;
                currentList = currentList.getChild(pos);
                int currentListSize = currentList.size();
                actionAddItem(currentList);
                // TODO: This is wrong! The dialog is asynchronous and
                // so non-blocking. That is, we won't have the result of
                // the dialog here and so will never update the adaptor
                // correctly.
                boolean added = (currentListSize != currentList.size());
                currentList = temp;
                if (added) {
                    liAdapter.notifyItemChanged(pos);
                }
                break;
        }
        return super.onContextItemSelected(item);
    }
    // Launches the "Add Item" dialog.
    // https://developer.android.com/guide/topics/ui/dialogs#java
    public void actionAddItem(ListItem list) {
        AddItemFragment dialog = AddItemFragment.newInstance(list);
        dialog.show(getSupportFragmentManager(), "add item dialog");
    }

    public void addItem(ListItem list, String title, String content) {
        list.add(new ListItem (title, content));
        if (list == currentList) {
            liAdapter.notifyItemInserted(currentList.size() - 1);
        } else if (list.getParent() == currentList) {
            liAdapter.notifyItemInserted(currentList.indexOf(list));
        }
        saveLists();
    }

    public void removeItem(int position) {
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

    @Override
    public void onSaveInstanceState(Bundle state) {
        // TODO: save list data.
        // How to demark which list we're on?
        super.onSaveInstanceState(state);
    }

    public ListItem goToAddress(ArrayList<Integer> address) {
        return topLevelList.goToAddress(address);
    }
}
