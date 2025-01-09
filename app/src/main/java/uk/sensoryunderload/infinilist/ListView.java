package uk.sensoryunderload.infinilist;

import android.app.Activity;
import android.app.AlertDialog;
import android.appwidget.AppWidgetManager;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.ParcelFileDescriptor;
import android.util.Log;
import android.view.ActionMode;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MenuInflater;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.FileProvider;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.ItemTouchHelper;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

public class ListView extends AppCompatActivity
                      implements ListItemAdapter.ListControlListener,
                                 ListSelectionTracker.SelectionController {
  public static final String OPEN_LIST_ACTION = "uk.sensoryunderload.infinilist.widget.OPEN_LIST_ACTION";
  public static final String ADD_ITEM_ACTION = "uk.sensoryunderload.infinilist.widget.ADD_ITEM_ACTION";
  public static final String OPEN_TOP_LIST_ACTION = "uk.sensoryunderload.infinilist.widget.OPEN_TOP_LIST_ACTION";
  private static final String SELECTION_TRACKER_URI = "my-selection-uri";

  private ListItem topLevelList = new ListItem("InfiniList","");
  private ListItem currentList;
  private ListItem copiedList = new ListItem("Copied List", "");
  private ListItemAdapter liAdapter;
  private ItemTouchHelper touchHelper;
  private boolean shownHelp;
  private boolean saveNeeded;
  private Boolean widgetUpdateNeeded;
  private ArrayList<Integer> widgetAddress = new ArrayList<Integer>();
  private boolean widgetAddressChanged;
  private ListRecyclerView recyclerView;

  // For multiple selection and action mode
  private ListSelectionTracker selectionTracker;
  private ActionMode actionMode;
  private ActionMode.Callback listActionModeCallback;

  // Setting key values
  private static final String WIDGET_ADDRESS = "WidgetAddress";

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    loadList(); // Sets topLevelList
    shownHelp = (topLevelList.size() != 0);
    currentList = topLevelList;
    loadSettings(getApplicationContext(), widgetAddress);
    Intent intent = getIntent();
    String appendedItem = "";
    if (intent.getAction().equals(OPEN_LIST_ACTION)) {
      currentList = goToAddress(widgetAddress);
    } else if (intent.getAction().equals(Intent.ACTION_VIEW)) {
      if (importList(intent)) {
        appendedItem = currentList.getChild(currentList.size() - 1).getTitle();
      }
    }

    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_list_view);
    Toolbar toolbar = findViewById(R.id.toolbar);
    setSupportActionBar(toolbar);
    setTitle();

    recyclerView = findViewById(R.id.recycler_view);

    recyclerView.setListLayoutManager(new ListLayoutManager(getApplicationContext()));
    recyclerView.setItemAnimator(new DefaultItemAnimator());

    liAdapter = new ListItemAdapter(currentList, this);
    recyclerView.setAdapter(liAdapter);

    // Setup ItemTouchHelper to handle item dragging
    touchHelper = new ItemTouchHelper(new ListCallback(this));
    touchHelper.attachToRecyclerView(recyclerView);

    registerForContextMenu(recyclerView);

    // Setup selection tracker
    selectionTracker = new ListSelectionTracker(this);

    listActionModeCallback = new ActionMode.Callback() {
      // Called when the action mode is created.
      @Override
      public boolean onCreateActionMode(ActionMode mode, Menu menu) {
        // Inflate a menu resource providing selection mode menu items.
        MenuInflater inflater = mode.getMenuInflater();
        inflater.inflate(R.menu.multi_select_menu, menu);
        return true;
      }

      // Called each time the action mode is shown. Always called after
      // onCreateActionMode, and might be called multiple times if the mode
      // is invalidated.
      @Override
      public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
        getSupportActionBar().hide();
        return false; // Return false if nothing is done.
      }

      // Called when the user selects an action menu item.
      @Override
      public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
        ArrayList<Integer> selection = selectionTracker.getSelection();
        boolean handled = false;
        boolean cut = false;
        switch (item.getItemId()) {
          case R.id.cut:
            cut = true;
          case R.id.copy:
            copy(currentList, selection);
            handled = true;
            if (!cut) {
              Toast.makeText(getApplicationContext(),
                             getResources().getQuantityString(R.plurals.toast_copied, selection.size(), selection.size()),
                             Toast.LENGTH_LONG).show();
              break;
            }
          case R.id.delete:
            // delete selection
            if (selection.size() == 1) {
              removeItem(selection.get(0), cut);
            } else if (!selection.isEmpty()) {
              if (cut) {
                // No warning on delete as user can paste
                deleteSelection(selection);
                handled = true;
                selectionTracker.clear();
              } else {
                // Warn on delete of multiple
                actionDeleteGroup(selection);
              }
            }
            break;
        }
        if (handled) {
          endSelectionMode();
        }
        return handled;
      }

      // Called when the user exits the action mode.
      @Override
      public void onDestroyActionMode(ActionMode mode) {
        actionMode = null;
        selectionTracker.deactivate(); // Disable multi-select mode
        liAdapter.notifyDataSetChanged();
        getSupportActionBar().show();
      }
    };

    if (savedInstanceState != null) {
      selectionTracker.onRestoreInstanceState(savedInstanceState);
    }
    if (!appendedItem.isEmpty()) {
      recyclerView.smoothScrollToPosition(currentList.size() - 1);
      Toast.makeText(this, getString(R.string.toast_item_appended, appendedItem), Toast.LENGTH_LONG).show();
    }
  }

  protected void onSaveInstanceState(Bundle outState) {
    super.onSaveInstanceState(outState);
    selectionTracker.onSaveInstanceState(outState);
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
  public boolean onPrepareOptionsMenu(Menu menu) {
    menu.findItem(R.id.action_paste).setEnabled(!copiedList.isEmpty());
    return super.onPrepareOptionsMenu(menu);
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
    if (selectionTracker.active()) {
      Toast.makeText(this, getString(R.string.toast_cant_descend_in_sel_mode), Toast.LENGTH_LONG).show();
    } else {
      currentList = currentList.getChild(position);
      liAdapter.itemList = currentList;
      liAdapter.notifyDataSetChanged();
      setTitle();
    }
  }
  @Override
  public void save() {
    saveOnPause();
  }
  @Override
  public void move(int from, int to) {
    currentList.move(from, to);
    updateWidget(currentList.getAddress(), to, from);
    selectionTracker.shiftSelections(from, to);
    liAdapter.notifyItemMoved(from, to);
  }
  @Override
  public void startDrag(RecyclerView.ViewHolder viewHolder) {
    touchHelper.startDrag(viewHolder);
  }
  @Override
  public void notifyStatusChange(int itemIndex) {
    updateWidget(currentList.getAddress(), itemIndex, itemIndex);
  }
  @Override
  public MenuInflater getMainMenuInflater() {
    return this.getMenuInflater();
  }
  @Override
  public ListItem getCopiedList() {
    return copiedList;
  }
  @Override
  public boolean inSelectionMode() {
    return selectionTracker.active();
  }
  @Override
  public void toggleSelect(int pos) {
    selectionTracker.toggleSelect(pos);
    liAdapter.notifyItemChanged(pos);
  }
  @Override
  public boolean isSelected(int pos) {
    return selectionTracker.isSelected(pos);
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
      case R.id.action_share :
        shareList(currentList);
        break;

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
        actionDeleteGroup(null);
        break;

      case R.id.action_paste :
        actionPaste(currentList);
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

    // Remove highlight. This must be done here in case current item
    // is moved/recycled and lost prior to onContextMenuClosed.
    ListItemAdapter.ListItemViewHolder viewHolder = (ListItemAdapter.ListItemViewHolder) recyclerView.findViewHolderForAdapterPosition(pos);
    if (viewHolder != null) {
      viewHolder.deHighlight();
    }

    boolean handled = true;
    boolean cut = false;
    switch (item.getItemId()) {
      case R.id.addsub:
        actionAddItem(currentList.getChild(pos));
        break;
      case R.id.edit:
        actionEditItem(currentList.getChild(pos));
        break;
      case R.id.move_to_top:
        move(pos, 0);
        break;
      case R.id.move_to_bottom:
        move(pos, currentList.size() - 1);
        break;
      case R.id.cut:
        cut = true;
      case R.id.copy:
        ArrayList<Integer> positionAsList = new ArrayList<Integer>();
        positionAsList.add(pos);
        copy(currentList, positionAsList);
        if (!cut) {
          Toast.makeText(getApplicationContext(),
                         getResources().getQuantityString(R.plurals.toast_copied, 1, 1),
                         Toast.LENGTH_LONG).show();
          break;
        }
      case R.id.delete:
        removeItem(pos, cut);
        break;
      case R.id.paste_into:
        actionPaste(currentList.getChild(pos));
        break;
      case R.id.mark_sub_widget:
        actionMarkWidget(currentList.getChild(pos));
        break;
      case R.id.select:
        if (selectionTracker.isSelected(pos)) {
            selectionTracker.deselect(pos);
        } else {
          if (actionMode == null) {
            actionMode = this.startActionMode(listActionModeCallback);
            selectionTracker.activate(); // Enable multi-select mode
          }
          if (viewHolder != null) {
            selectionTracker.select(pos);
            liAdapter.notifyItemChanged(pos);
          }
        }
        break;
      case R.id.share :
        shareList(currentList.getChild(pos));
        break;
      default:
        handled = super.onContextItemSelected(item);
    }

    return handled;
  }

  @Override
  public void onContextMenuClosed(Menu menu) {
    int pos = liAdapter.retrievePosition();

    // Remove highlight. This must be done here in case context menu
    // is backed-out-of with no action taken (i.e.
    // onContextItemSelected is not triggered).
    ListItemAdapter.ListItemViewHolder viewHolder = (ListItemAdapter.ListItemViewHolder) recyclerView.findViewHolderForAdapterPosition(pos);
    if (viewHolder != null) {
      viewHolder.deHighlight();
    }
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
    updateWidget (currentList.getAddress(), -1, -1);
    liAdapter.notifyDataSetChanged();
    saveOnPause();
  }
  private void deleteSelection (ArrayList<Integer> selection) {
    ArrayList<Integer> address = currentList.getAddress();
    for (int i = (currentList.getChildren().size() - 1); i >= 0; --i) {
      if (selection.contains (i)) {
        updateWidget(address, -1, (int) i);
        currentList.remove((int) i);
      }
    }
    liAdapter.notifyDataSetChanged();
    saveOnPause();
  }
  // The following deletes *all* items in this list or just those
  // selected, depending on whether the selection is empty or not.
  private void actionDeleteGroup(ArrayList<Integer> selection) {
    AlertDialog.Builder alert = new AlertDialog.Builder(this);

    if ((selection == null) || selection.isEmpty()) {
      alert.setTitle(getString(R.string.delete_all));
      alert.setPositiveButton(getString(R.string.dialogPositiveButton), new DialogInterface.OnClickListener() {
        @Override
        public void onClick(DialogInterface dialog, int which) {
          ArrayList<Integer> address = currentList.getAddress();
          for (int i = 0; i < currentList.getChildren().size(); ++i) {
            updateWidget(address, -1, i);
          }
          currentList.getChildren().clear();
          liAdapter.notifyDataSetChanged();
          saveOnPause();
          dialog.dismiss();
        }
      });
    } else {
      // We need a new copy of the selection in case the delete dialog
      // persists after the selection is cleared.
      ArrayList<Integer> stableSelection = new ArrayList<Integer>(selection);

      alert.setTitle(getString(R.string.selection_delete));
      alert.setPositiveButton(getString(R.string.dialogPositiveButton), new DialogInterface.OnClickListener() {
        @Override
        public void onClick(DialogInterface dialog, int which) {
          deleteSelection(stableSelection);
          selectionTracker.clear();
          endSelectionMode();
          dialog.dismiss();
        }
      });
    }

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
      Toast.makeText(this, getString(R.string.toast_widget_refused), Toast.LENGTH_LONG).show();
    } else {
      widgetAddress = list.getAddress();
      widgetAddressChanged = true;
      Toast.makeText(this, getString(R.string.toast_widget_accepted), Toast.LENGTH_LONG).show();
    }
  }
  void actionPaste(ListItem targetList) {
    int oldSize = targetList.size();
    for (ListItem copiedItem : copiedList.getChildren()) {
      targetList.add (new ListItem(copiedItem));
    }

    // Decide which items to refresh
    if (targetList == currentList) {
      // We've appended to the currently displayed list
      liAdapter.notifyItemRangeInserted(oldSize, copiedList.size());
    } else {
      // We've appended to a sublist of the current list.
      liAdapter.notifyItemChanged(currentList.getChildren().indexOf(targetList));
    }
    copiedList.clear();
  }

  // If "add" then add a new ListItem to list (at start or end
  // depending on "atStart"), else modify list.
  void editItem(ListItem list, String title, String content, boolean add, boolean atStart) {
    if (add) {
      if (atStart) {
        list.insert(new ListItem (title, content));
      } else {
        list.add(new ListItem (title, content));
      }
    } else {
      list.setTitle(title);
      list.setContent(content);
    }

    if (list == currentList) {
      if (add) {
        if (atStart) {
          liAdapter.notifyItemInserted(0);
          recyclerView.smoothScrollToPosition(0);
        } else {
          liAdapter.notifyItemInserted(currentList.size() - 1);
          recyclerView.smoothScrollToPosition(currentList.size() - 1);
        }
      } else {
        setTitle();
      }
    } else if (list.getParent() == currentList) {
      liAdapter.notifyItemChanged(currentList.indexOf(list));
    }
    saveOnPause();

    // Notify widget of changes.
    if (add) {
      if (atStart) {
        updateWidget (list.getAddress(), 0, -1);
      } else {
        updateWidget (list.getAddress(), list.size() - 1, -1);
      }
    } else {
      updateWidget (list.getParent().getAddress(), -1, -1);
    }
  }

  private void removeItem(int position, boolean force) {
    int childCount = currentList.getChild(position).size();
    if (!force && (childCount > 0)) {
      AlertDialog.Builder alert = new AlertDialog.Builder(this);
      alert.setTitle(getResources().getQuantityString(R.plurals.delete_with_children, childCount, childCount));
      alert.setPositiveButton(getString(R.string.dialogPositiveButton), new DialogInterface.OnClickListener() {
        @Override
        public void onClick(DialogInterface dialog, int which) {
          selectionTracker.shiftSelections(position, null);
          currentList.remove(position);
          liAdapter.notifyItemRemoved(position);
          updateWidget (currentList.getAddress(), -1, position);
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
      selectionTracker.shiftSelections(position, null);
      currentList.remove(position);
      liAdapter.notifyItemRemoved(position);
      updateWidget (currentList.getAddress(), -1, position);
      saveOnPause();
    }
  }

  private void saveOnPause() { saveNeeded = true; }
  private void saveNow() { saveLists(); }
  private void saveLists() { saveList(topLevelList, "Main.todo"); }
  private void saveList(ListItem list, String name) {
    File path = getApplicationContext().getFilesDir();
    File file = new File(path, name);
    list.writeToFile(file);
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

  // An element has been insertedAt and/or removedFrom the respective
  // list positions (insertedAt == -1 => nothing inserted, etc.) in
  // list at address changedAddress. Update address as necessary.
  //
  // widgetUpdateNeeded is set to true if the list displayed in the
  // widget has changed.
  //
  // widgetAddressChanged is set to true if the widget address has
  // changed.
  void updateWidget (ArrayList<Integer> changedAddress,
      int insertedAt, int removedFrom) {
    boolean changedLengthMatches = (changedAddress.size() == widgetAddress.size());
    boolean changedLength1More = (changedAddress.size() == (widgetAddress.size() + 1));
    if (changedLengthMatches || changedLength1More) {
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
      if (addressesMatch) {
        boolean subItemCountChanged = ((insertedAt == -1) != (removedFrom == -1));
        widgetUpdateNeeded |= (changedLengthMatches || subItemCountChanged);
      }
    } else {
      widgetAddressChanged = updateWidgetAddress (widgetAddress, changedAddress, insertedAt, removedFrom);
    }
  }

  // Return true if the address of the widget list is changed, false
  // otherwise.
  static boolean updateWidgetAddress (ArrayList<Integer> widgetAddress,
      ArrayList<Integer> changedAddress,
      int insertedAt, int removedFrom) {
    boolean addressChanged = false;

    if (changedAddress.size() <= widgetAddress.size()) {
      boolean intersects = true;

      for (int i = 0; i < changedAddress.size(); ++i) {
        if (!changedAddress.get(i).equals(widgetAddress.get(i))) {
          intersects = false;
          break;
        }
      }

      if (intersects) {
        boolean lengthsDiffer = (changedAddress.size() != widgetAddress.size());

        if (lengthsDiffer) {
          // Check to see whether list address must change
          if (removedFrom == widgetAddress.get(changedAddress.size())) {
            if (insertedAt == -1) {
              // Target list deleted!
              widgetAddress.clear();
            } else {
              // Target list moved.
              widgetAddress.set(changedAddress.size(), insertedAt);
            }
            addressChanged = true;
          } else {
            Integer existing = widgetAddress.get(changedAddress.size());
            Integer difference = 0;
            if ((insertedAt != -1) && (insertedAt <= existing)) {
              ++difference;
            }
            if ((removedFrom != -1) && (removedFrom < existing)) {
              --difference;
            }

            if (difference != 0) {
              widgetAddress.set(changedAddress.size(), widgetAddress.get(changedAddress.size()) + difference);
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

    startActivityForResult(importIntent, IMPORT_REQUEST_CODE);
  }

  private void exportLists() {
    DateFormat dateFormat = new SimpleDateFormat("yyyyMMdd");
    Date date = new Date();
    String fileName = "exported-" + dateFormat.format(date) + ".todo";

    // Create the text message with a string
    Intent saveIntent = new Intent();
    saveIntent.setAction(Intent.ACTION_CREATE_DOCUMENT);
    saveIntent.setType("text/plain");
    saveIntent.addCategory(Intent.CATEGORY_OPENABLE);
    saveIntent.putExtra(Intent.EXTRA_TITLE, fileName);

    startActivityForResult(saveIntent, EXPORT_REQUEST_CODE);
  }

  private void shareList(ListItem item) {
    String fileName = item.getTitle();

    // Write file to storage
    File filesDir = getApplicationContext().getFilesDir();
    File shareDir = new File(filesDir, "shared/");
    if (!shareDir.exists()) {
      shareDir.mkdir();
    }
    File file = new File(shareDir, "tempShare.todo");
    item.writeToFile(file);

    Uri fileUri = FileProvider.getUriForFile(getApplicationContext(),
                                             "uk.sensoryunderload.infinilist.fileprovider",
                                             file);

    // Create the text message with a string
    Intent sendIntent = new Intent(Intent.ACTION_SEND);
    sendIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
    sendIntent.putExtra(Intent.EXTRA_STREAM, fileUri);
    sendIntent.setType("text/plain");

    startActivity(Intent.createChooser(sendIntent, getString(R.string.share_title)));
  }

  @Override
  public void onActivityResult(int requestCode, int resultCode, Intent resultData) {
    if (resultCode == Activity.RESULT_OK) {
      if (requestCode == EXPORT_REQUEST_CODE) {
        exportToFile(resultData);
      } else if (requestCode == IMPORT_REQUEST_CODE) {
        importFromFile(resultData);
      }
    }
  }

  private void importFromFile(Intent resultData) {
    if (resultData != null) {
      Uri uri = resultData.getData();
      if (uri != null) {
        try {
          ParcelFileDescriptor pfd = getContentResolver().openFileDescriptor(uri, "r");
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
        try {
          ParcelFileDescriptor pfd = getContentResolver().
            openFileDescriptor(uri, "w");
          if (pfd != null){
            topLevelList.writeToDescriptor(pfd.getFileDescriptor());
            // Let the document provider know you're done by closing the pfd.
            pfd.close();
          }
          Toast.makeText(this, getString(R.string.toast_export), Toast.LENGTH_LONG).show();
        } catch (IOException e) {
          e.printStackTrace();
          Toast.makeText(this, getString(R.string.toast_export_failed), Toast.LENGTH_LONG).show();
        }
      }
    }
  }

  ListItem goToAddress(ArrayList<Integer> address) {
    return topLevelList.goToAddress(address);
  }

  // Copy references to the selected items into copiedList
  void copy(ListItem containingList, ArrayList<Integer> items) {
    copiedList.clear();
    for (Integer i : items) {
      copiedList.add(new ListItem(containingList.getChild(i)));
    }

    String text = copiedList.toString();

    ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
    ClipData clip = ClipData.newPlainText("Copied List", text);
    clipboard.setPrimaryClip(clip);
  }

  // SelectionController implementation
  @Override
  public void endSelectionMode() {
    if (actionMode != null)
      actionMode.finish();
  }
  @Override
  public void setSelectionTitle(int count) {
    if (actionMode != null)
      actionMode.setTitle(getString(R.string.selection_title, count, currentList.size()));
  }

  private String getContentName(ContentResolver resolver, Uri uri){
    Cursor cursor = resolver.query(uri, null, null, null, null);
    cursor.moveToFirst();
    int nameIndex = cursor.getColumnIndex(android.provider.MediaStore.MediaColumns.DISPLAY_NAME);
    if (nameIndex >= 0) {
        return cursor.getString(nameIndex);
    } else {
        return null;
    }
  }

  private boolean importList(Intent intent) {
    String scheme = intent.getScheme();
    ContentResolver resolver = getContentResolver();

    Uri uri = intent.getData();
    // Log intent type
    if (scheme.compareTo(ContentResolver.SCHEME_CONTENT) == 0) {
      String name = getContentName(resolver, uri);
      Log.v("tag" , "Content intent detected: " + intent.getAction() + " : " + intent.getDataString() + " : " + intent.getType() + " : " + name);
    } else if (scheme.compareTo(ContentResolver.SCHEME_FILE) == 0) {
      String name = uri.getLastPathSegment();
      Log.v("tag" , "File intent detected: " + intent.getAction() + " : " + intent.getDataString() + " : " + intent.getType() + " : " + name);
    } else if (scheme.compareTo("http") == 0) {
      String name = uri.getLastPathSegment();
      Log.v("tag" , "HTTP intent detected: " + intent.getAction() + " : " + intent.getDataString() + " : " + intent.getType() + " : " + name);
    }
    InputStream input = null;
    try {
      input = resolver.openInputStream(uri);
    } catch (FileNotFoundException e) {
      return false;
    }

    ListItem temp = new ListItem();
    temp.readFromInputStream(input);
    currentList.add(temp);
    saveOnPause();
    updateWidget (currentList.getAddress(), currentList.size() - 1, -1);
    return true;
  }
}
