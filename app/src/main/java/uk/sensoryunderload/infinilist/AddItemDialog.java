package uk.sensoryunderload.infinilist;

import android.app.Dialog;
import android.app.AlertDialog;
import android.support.v4.app.DialogFragment;
import android.support.v7.widget.AppCompatEditText;
import android.view.View;
import android.os.Bundle;
import java.util.ArrayList;

public class AddItemFragment extends DialogFragment {
    // https://developer.android.com/guide/topics/ui/dialogs#java
    //
    static AddItemFragment newInstance(ListItem list) {
        AddItemFragment f = new AddItemFragment();

        // Supply num input as an argument.
        Bundle args = new Bundle();
        args.putString("itemAddress", list.getAddressString());
        f.setArguments(args);

        return f;
    }

    @Override
    //public Dialog onCreateDialog(final ListView lv) {
    public Dialog onCreateDialog(final Bundle savedInstanceState) {
        String addressString = getArguments().getString("itemAddress");
        String[] addressStringArray = addressString.split(",");
        ArrayList<Integer> address = new ArrayList<Integer>();
        for (int i = 0; i < addressStringArray.length; ++i) {
            address.add(java.lang.Integer.parseInt(addressStringArray[i]));
        }
        final ListItem list = ((ListView)getActivity()).goToAddress(address);

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
                ((ListView)getActivity()).addItem(list,
                                                  inputTitle.getText().toString(),
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
