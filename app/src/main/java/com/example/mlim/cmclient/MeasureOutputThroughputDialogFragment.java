package com.example.mlim.cmclient;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

public class MeasureOutputThroughputDialogFragment extends DialogFragment {
    private View m_view;

    /* The activity that creates an instance of this dialog fragment must
     * implement this interface in order to receive event callbacks.
     * Each method passes the DialogFragment in case the host needs to query it. */
    public interface MeasureOutputThroughputDialogListener {
        public void onMeasureOutputThroughputDialogConfirmClick(DialogFragment dialog);
        public void onMeasureOutputThroughputDialogCancelClick(DialogFragment dialog);
    }

    // Use this instance of the interface to deliver action events
    MeasureOutputThroughputDialogFragment.MeasureOutputThroughputDialogListener m_listener;

    // Override the Fragment.onAttach() method to instantiate the ServerInfoDialogListener
    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        // Verify that the host activity implements the callback interface
        try {
            // Instantiate the NoticeDialogListener so we can send events to the host
            m_listener = (MeasureOutputThroughputDialogFragment.MeasureOutputThroughputDialogListener) activity;
        } catch (ClassCastException e) {
            // The activity doesn't implement the interface, throw exception
            throw new ClassCastException(activity.toString()
                    + " must implement NoticeDialogListener");
        }
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        // Get the layout inflater
        LayoutInflater inflater = getActivity().getLayoutInflater();
        m_view = inflater.inflate(R.layout.dialog_target_node, null);

        TextView titleTextView = m_view.findViewById(R.id.targetNodeTitleTextView);
        titleTextView.setText(R.string.measure_output_throughput);

        // Inflate and set the layout for the dialog
        // Pass null as the parent view because its going in the dialog layout
        builder.setView(m_view)
                // Add action buttons
                .setPositiveButton(R.string.button_confirm, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        // Send the positive button event back to the host activity
                        m_listener.onMeasureOutputThroughputDialogConfirmClick(MeasureOutputThroughputDialogFragment.this);
                    }
                })
                .setNegativeButton(R.string.button_cancel, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        // Send the negative button event back to the host activity
                        m_listener.onMeasureOutputThroughputDialogCancelClick(MeasureOutputThroughputDialogFragment.this);
                    }
                });

        return builder.create();
    }

    public View getView()
    {
        return m_view;
    }
}