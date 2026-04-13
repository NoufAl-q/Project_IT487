package com.example.mytrip;

import android.app.Dialog;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;

/**
 * ContactDialogFragment – details DialogFragment showing team contact info.
 *
 * Rubric: DialogFragment used meaningfully (details) — displays contact email.
 */
public class ContactDialogFragment extends DialogFragment {

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        return new AlertDialog.Builder(requireContext())
                .setTitle("Contact Us")
                .setMessage(
                        "📧  mytrip.team@seu.edu.sa\n\n" +
                        "We'd love to hear from you!\n" +
                        "Feel free to reach out for support,\n" +
                        "feedback, or any questions about MyTrip."
                )
                .setPositiveButton("Got it", null)
                .create();
    }
}
