package uk.sensoryunderload.infinilist;

import android.app.Dialog;
import android.app.AlertDialog;
import androidx.fragment.app.DialogFragment;
import androidx.appcompat.widget.AppCompatEditText;
import android.view.View;
import android.os.Bundle;

public class HelpFragment extends DialogFragment {
    @Override
    public Dialog onCreateDialog(final Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        // Set the custom layout
        android.view.LayoutInflater inflater = getActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.help_dialog, null);
        builder.setView(view)
               .setTitle(R.string.help_title)
               .setPositiveButton(R.string.help_dismiss, new android.content.DialogInterface.OnClickListener() {
            @Override
            public void onClick(android.content.DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });

        return builder.create();
    }
}
