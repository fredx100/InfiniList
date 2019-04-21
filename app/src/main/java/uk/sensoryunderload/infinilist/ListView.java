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

public class ListView extends AppCompatActivity {
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

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list_view);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        recyclerView = findViewById(R.id.recycler_view);

        liAdapter = new ListItemAdapter(currentList);
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
        ListItemAdapter liA = (ListItemAdapter)recyclerView.getAdapter();
        if (liA != null) {
            int pos = liA.retrievePosition();
            switch (item.getItemId()) {
                case R.id.delete:
                    removeItem(pos);
                    break;
                case R.id.addsub:
                    // TODO
                    break;
            }
        }
        return super.onContextItemSelected(item);
    }
    // Launches the "Add Item" dialog. Returns true if list is modified, false otherwise.
    // https://developer.android.com/guide/topics/ui/dialogs#java
    public void actionAddItem(final ListItem list) {
        AddItemDialog dialog = new AddItemDialog();
        dialog.show(getSupportFragmentManager(), "add item dialog");
    }

    public void addItem(String title, String content) {
        currentList.add(new ListItem (title, content));
        liAdapter.notifyItemInserted(currentList.size() - 1);
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
    public void onSaveInstanceState (Bundle state) {
        // TODO: save list data.
        // How to demark which list we're on?
        super.onSaveInstanceState(state);
    }
}
