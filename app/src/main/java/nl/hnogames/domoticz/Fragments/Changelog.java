package nl.hnogames.domoticz.Fragments;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;

import it.gmariotti.changelibs.library.view.ChangeLogRecyclerView;
import nl.hnogames.domoticz.Interfaces.DomoticzFragmentListener;
import nl.hnogames.domoticz.R;
import nl.hnogames.domoticz.Utils.SharedPrefUtil;

public class Changelog extends DialogFragment implements DomoticzFragmentListener {

    private SharedPrefUtil mSharedPrefUtil;

    public Changelog() {
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        if (mSharedPrefUtil == null)
            mSharedPrefUtil = new SharedPrefUtil(getContext());

        LayoutInflater layoutInflater = (LayoutInflater) getActivity().getSystemService(
                Context.LAYOUT_INFLATER_SERVICE);
        ChangeLogRecyclerView chgList = (ChangeLogRecyclerView) layoutInflater.inflate(R.layout.changelog_fragment, null);
        int theme = R.style.MyDialogTheme;
        if (mSharedPrefUtil.darkThemeEnabled())
            theme = R.style.MyDarkDialogTheme;

        return new AlertDialog.Builder(getActivity(), theme)
                .setTitle("Changelog")
                .setView(chgList)
                .setPositiveButton(R.string.ok,
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int whichButton) {
                                dialog.dismiss();
                            }
                        }
                )
                .create();
    }

    @Override
    public void onConnectionOk() {

    }
}