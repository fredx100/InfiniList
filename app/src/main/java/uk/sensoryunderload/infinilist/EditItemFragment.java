package uk.sensoryunderload.infinilist;

import android.app.Dialog;
import android.app.AlertDialog;
import android.support.v4.app.DialogFragment;
import android.support.v7.widget.AppCompatEditText;
import android.view.View;
import android.os.Bundle;
import java.util.ArrayList;

public class EditItemFragment extends DialogFragment {
    // https://developer.android.com/guide/topics/ui/dialogs#java
    // Here, if "append" is true, then list is the list to be appended
    // to, else, "list" is the item to be edited.
    public static EditItemFragment newInstance(ListItem list, boolean append) {
        EditItemFragment f = new EditItemFragment();

        // Supply num input as an argument.
        Bundle args = new Bundle();
        args.putChar("mode", append ? 'A' : 'E');
        args.putString("itemAddress", list.getAddressString());
        f.setArguments(args);

        return f;
    }

    //public Dialog onCreateDialog(final ListView lv) {
    @Override
    public Dialog onCreateDialog(final Bundle savedInstanceState) {
        char appendChar = getArguments().getChar("mode"); // "A" => append, "E" => edit
        final boolean append = (appendChar == 'A'); // true => append, false => edit
        String addressString = getArguments().getString("itemAddress");
        ArrayList<Integer> address = new ArrayList<Integer>();
        if (!addressString.isEmpty()) {
            String[] addressStringArray = addressString.split(",");
            for (int i = 0; i < addressStringArray.length; ++i) {
                address.add(java.lang.Integer.parseInt(addressStringArray[i]));
            }
        }
        final ListItem list = ((ListView)getActivity()).goToAddress(address);

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        if (append) {
            builder.setTitle(R.string.editTitle_add);
        } else {
            builder.setTitle(R.string.editTitle_edit);
        }

        // Set the custom layout
        android.view.LayoutInflater inflater = getActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.add_item_dialog, null);
        builder.setView(view);
        final AppCompatEditText inputTitle = view.findViewById(R.id.inputTitle);
        final AppCompatEditText inputDescription = view.findViewById(R.id.inputDescription);

        int okButtonText = R.string.editPositiveButton_add;
        if (append) {
            // An Add dialog
            inputTitle.requestFocus();
        } else {
            // An Edit dialog
            okButtonText = R.string.editPositiveButton_edit;
            inputTitle.setText(list.getTitle());
            inputDescription.setText(list.getContent());
        }

        // Set up the buttons
        builder.setPositiveButton(okButtonText, new android.content.DialogInterface.OnClickListener() {
            @Override
            public void onClick(android.content.DialogInterface dialog, int which) {
                ((ListView)getActivity()).editItem(list,
                                                   inputTitle.getText().toString(),
                                                   inputDescription.getText().toString(),
                                                   append);
            }
        });
        builder.setNegativeButton(R.string.editNegativeButton, new android.content.DialogInterface.OnClickListener() {
            @Override
            public void onClick(android.content.DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });

        return builder.create();
    }
}
