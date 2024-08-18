package uk.sensoryunderload.infinilist;

import android.os.Bundle;

import java.util.ArrayList;

final class ListSelectionTracker {
  interface SelectionController {
    void endSelectionMode();
    void setSelectionTitle(int count);
  }

  private boolean active;
  private ArrayList<Integer> selection;
  private SelectionController selectionController;

  ListSelectionTracker(SelectionController selController) {
    active = false;
    selectionController = selController;
    selection = new ArrayList<Integer>();
  }

  boolean active() {
    return active;
  }
  void activate() {
    active = true;
  }
  void deactivate() {
    active = false;
    selection.clear();
  }
  void clear() {
    selection.clear();
  }

  void select(int pos) {
    if (active &&
        !selection.contains(pos)) {
      selection.add(pos);
      selectionController.setSelectionTitle(selection.size());
    }
  }

  void deselect(int pos) {
    if (active &&
        selection.contains(pos)) {
      selection.remove(selection.indexOf(pos));

      if (selection.isEmpty()) {
        selectionController.endSelectionMode();
      } else {
        selectionController.setSelectionTitle(selection.size());
      }
    }
  }

  void toggleSelect(int pos) {
    if (active) {
      if (selection.contains(pos)) {
        selection.remove(selection.indexOf(pos));
      } else {
        selection.add(pos);
      }

      if (selection.isEmpty()) {
        selectionController.endSelectionMode();
      } else {
        selectionController.setSelectionTitle(selection.size());
      }
    }
  }

  boolean isSelected(int pos) {
    return active && selection.contains(pos);
  }

  ArrayList<Integer> getSelection() {
    return selection;
  }

  void shiftSelections(Integer removedFrom, Integer insertedAt) {
    if (!active)
      return;

    ArrayList<Integer> newSelection = new ArrayList<Integer>();

    boolean deleted = false;
    for (Integer selectedId : selection) {
      deleted = false;
      int newSelectedId = selectedId;
      if ((removedFrom != null) && (selectedId > removedFrom))
        --newSelectedId;
      if ((insertedAt != null) && (selectedId > insertedAt))
        ++newSelectedId;
      if ((removedFrom != null) &&
          (selectedId == removedFrom)) {
        if (insertedAt != null) {
          newSelectedId = insertedAt;
        } else {
          deleted = true;
        }
      }
      if (!deleted) {
        newSelection.add(newSelectedId);
      }
    }

    selection = newSelection;
  }

  protected void onSaveInstanceState(Bundle outState) {
    outState.putShort("selectionModeActive", (short) (active ? 1 : 0));
    outState.putIntegerArrayList("selectionModeSelection", selection);
  }

  void onRestoreInstanceState(Bundle inState) {
    active = (inState.getShort("selectionModeActive") == (short) 1);
    if (active) {
      selection = inState.getIntegerArrayList("selectionModeSelection");
    }
  }
}

