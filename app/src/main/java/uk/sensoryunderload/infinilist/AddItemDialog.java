package uk.sensoryunderload.infinilist;

import android.app.Dialog;
import android.app.AlertDialog;
import android.support.v4.app.DialogFragment;
import android.support.design.widget.TextInputEditText;
import android.view.View;

public class AddItemDialog extends DialogFragment {
    // https://developer.android.com/guide/topics/ui/dialogs#java
    @Override
    public Dialog onCreateDialog(final android.os.Bundle bundle) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle("Add Item");

        // Set the custom layout
        android.view.LayoutInflater inflater = getActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.add_item_dialog, null);
        builder.setView(view);
        final TextInputEditText inputTitle = view.findViewById(R.id.inputTitle);
        final TextInputEditText inputDescription = view.findViewById(R.id.inputDescription);

        // Set up the buttons
        builder.setPositiveButton("Add", new android.content.DialogInterface.OnClickListener() {
            @Override
            public void onClick(android.content.DialogInterface dialog, int which) {
                bundle.putString("title", inputTitle.getText().toString());
                bundle.putString("desc", inputDescription.getText().toString());
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
