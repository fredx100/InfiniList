package uk.sensoryunderload.infinilist;

import android.app.Dialog;
import android.app.AlertDialog;
import android.support.v4.app.DialogFragment;
import android.support.v7.widget.AppCompatEditText;
import android.view.View;
import android.os.Bundle;

public class AddItemDialog extends DialogFragment {
    // https://developer.android.com/guide/topics/ui/dialogs#java
    @Override
    //public Dialog onCreateDialog(final ListView lv) {
    public Dialog onCreateDialog(final Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle("Add Item");

        // Set the custom layout
        android.view.LayoutInflater inflater = getActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.add_item_dialog, null);
        builder.setView(view);
        final AppCompatEditText inputTitle = view.findViewById(R.id.inputTitle);
        final AppCompatEditText inputDescription = view.findViewById(R.id.inputDescription);

        // Set up the buttons
        builder.setPositiveButton("Add", new android.content.DialogInterface.OnClickListener() {
            @Override
            public void onClick(android.content.DialogInterface dialog, int which) {
                ((ListView)getActivity()).addItem(inputTitle.getText().toString(),
                                                  inputDescription.getText().toString());
            }
        });
        builder.setNegativeButton("Cancel", new android.content.DialogInterface.OnClickListener() {
            @Override
            public void onClick(android.content.DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });

        return builder.create();
    }
}
