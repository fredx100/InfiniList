package uk.sensoryunderload.infinilist;

import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.app.Dialog;
import android.app.AlertDialog;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;

import java.util.List;
import java.util.ArrayList;


public class ListView extends AppCompatActivity {
    private java.util.List<ListItem> currentList = new ArrayList<>();
    private java.util.List<ListItem> topLevelList = new ArrayList<>();
    private RecyclerView recyclerView;
    private ListItemAdapter liAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
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

        prepareListData();
    }

    private void prepareListData() {
        ListItem item = new ListItem("Finish InfiniList", "Asap");
        currentList.add(item);

        item = new ListItem("Plan InfiniList", "Prior to anything else");
        currentList.add(item);

        item = new ListItem("Stop eating biscuits", "After you've finished this pack.");
        currentList.add(item);
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

    // Launches the "Add Item" dialog. Returns true if list is modified, false otherwise.
    private boolean actionAddItem(List<ListItem> list) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Add Item");

        // Set the custom layout
        FrameLayout fl = findViewById(android.R.id.add_item_dialog);
        fl.addView(myView, new LayoutParams(MATCH_PARENT, WRAP_CONTENT));

        // Set up the buttons
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() { 
            @Override
            public void onClick(DialogInterface dialog, int which) {
                list.addItem (inputTitle.getText().toString(), inputDescription.getText().toString());
            }
        });
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });

        builder.show();
    }
}
