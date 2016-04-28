package com.bignerdranch.android.criminalintent;

import android.app.Dialog;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;

/**
 * Created by Rasul on 28.04.2016.
 */
public class ImageDialog extends DialogFragment {

    private static final String ARG_IMAGE_PATH = "imagePath";

    public static ImageDialog newInstance(String imagePath) {
        ImageDialog dialog = new ImageDialog();
        Bundle args = new Bundle();
        args.putString(ARG_IMAGE_PATH, imagePath);
        dialog.setArguments(args);
        return dialog;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        View v = LayoutInflater.from(getActivity()).inflate(R.layout.dialog_image, null);

        String imagePath = getArguments().getString(ARG_IMAGE_PATH);
        if (imagePath != null) {
            ImageView image = (ImageView) v.findViewById(R.id.dialog_image);
            image.setImageBitmap(PictureUtils.getScaledBitmap(imagePath, getActivity()));
        }

        return new AlertDialog.Builder(getActivity())
                .setView(v)
                //.setTitle("")
                .setPositiveButton(android.R.string.ok, null)
                .create();
    }
}
