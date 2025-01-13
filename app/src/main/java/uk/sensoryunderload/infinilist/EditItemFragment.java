package uk.sensoryunderload.infinilist;

import android.app.Dialog;
import android.app.AlertDialog;
import androidx.fragment.app.DialogFragment;
import androidx.appcompat.widget.AppCompatEditText;
import androidx.appcompat.widget.AppCompatCheckBox;
import android.view.View;
import android.os.Bundle;
import java.util.ArrayList;

public class EditItemFragment extends DialogFragment {
    // https://developer.android.com/guide/topics/ui/dialogs#java
    // Here, if "add" is true, then list is the list to be appended
    // to, else, "list" is the item to be edited.
    public static EditItemFragment newInstance(ListItem list, boolean add) {
        EditItemFragment f = new EditItemFragment();

        // Supply num input as an argument.
        Bundle args = new Bundle();
        args.putChar("mode", add ? 'A' : 'E');
        args.putString("itemAddress", list.getAddressString());
        f.setArguments(args);

        return f;
    }

    @Override
    public Dialog onCreateDialog(final Bundle savedInstanceState) {
        char addChar = getArguments().getChar("mode"); // "A" => add, "E" => edit
        final boolean add = (addChar == 'A'); // true => add, false => edit
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
        if (add) {
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
        final AppCompatCheckBox checkBoxAtTop = view.findViewById(R.id.checkBoxAtTop);

        int okButtonText = R.string.editPositiveButton_add;
        if (add) {
            // An Add dialog
            inputTitle.requestFocus();
        } else {
            // An Edit dialog
            okButtonText = R.string.dialogPositiveButton;
            inputTitle.setText(list.getTitle());
            inputDescription.setText(list.getContent());
            checkBoxAtTop.setVisibility(View.GONE);
        }

        // Set up the buttons
        builder.setPositiveButton(okButtonText, new android.content.DialogInterface.OnClickListener() {
            @Override
            public void onClick(android.content.DialogInterface dialog, int which) {
                ((ListView)getActivity()).editItem(list,
                                                   inputTitle.getText().toString(),
                                                   inputDescription.getText().toString(),
                                                   add, checkBoxAtTop.isChecked());
            }
        });
        builder.setNegativeButton(R.string.cancel, new android.content.DialogInterface.OnClickListener() {
            @Override
            public void onClick(android.content.DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });

        AlertDialog dialog = builder.create();
        dialog.setCanceledOnTouchOutside (false);

        return dialog;
    }
}
