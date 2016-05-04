package com.bignerdranch.android.criminalintent;

import android.Manifest;
import android.app.Activity;
import android.content.ContentUris;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.StringDef;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.ShareCompat;
import android.support.v4.content.PermissionChecker;
import android.text.Editable;
import android.text.TextWatcher;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.UUID;



public class CrimeFragment extends Fragment {
    private static final String ARG_CRIME_ID = "crime_id";
    private static final String DIALOG_DATE_FRAGMENT = "DialogDateFragment";
    private static final int REQUEST_DATE_CODE = 0;
    private static final int REQUEST_TIME_CODE = 1;
    public static final int REQUEST_CONTACT = 2;
    private static final String DIALOG_TIME_FRAGMENT = "DialogTimeFragment";
    private static final String TAG = "CriminalIntent";
    private static final int PERMISSIONS_REQUEST_READ_CONTACTS = 100;
    private static final int REQUEST_PHOTO = 3;
    private static final String DIALOG_IMAGE_FRAGMENT = "DialogImageFragment";

    private Crime mCrime;
    private EditText mTitleField;
    private Button mDateButton;
    private CheckBox mSolvedCheckBox;
    private Button mTimeButton;
    private Button mReportButton;
    private Button mSuspectButton;
    private Button mCallButton;
    private ImageButton mPhotoButton;
    private ImageView mPhotoView;
    private File mPhotoFile;

    private Callbacks mCallbacks;
    public interface Callbacks {
        void onCrimeUpdated(Crime crime);
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        mCallbacks = (Callbacks) activity;
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mCallbacks = null;
    }

    public static CrimeFragment newInstance(UUID crimeId) {
        Bundle args = new Bundle();
        args.putSerializable(ARG_CRIME_ID, crimeId);
        CrimeFragment fragment = new CrimeFragment();
        fragment.setArguments(args);
        return fragment;
    }

    public static UUID getCrimeId(Intent data) {
        return (UUID) data.getSerializableExtra(ARG_CRIME_ID);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        UUID crimeId = (UUID) getArguments().getSerializable(ARG_CRIME_ID);
        mCrime = CrimeLab.get(getActivity()).getCrime(crimeId);
        mPhotoFile = CrimeLab.get(getActivity()).getPhotoFile(mCrime);
        setResult();
        setHasOptionsMenu(true);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.fragment_crime, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.menu_remove_crime) {
            CrimeLab.get(getActivity()).removeCrime(mCrime.getId());
            getActivity().finish();
            return true;
        } else {
            return super.onOptionsItemSelected(item);
        }
    }

    public void setResult() {
        Intent intent = new Intent();
        intent.putExtra(ARG_CRIME_ID, mCrime.getId());
        getActivity().setResult(Activity.RESULT_OK, intent);
    }

    @Override
    public void onPause() {
        super.onPause();
        CrimeLab.get(getActivity()).updateCrime(mCrime);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_crime, container, false);

        mTitleField = (EditText) v.findViewById(R.id.crime_title);
        mTitleField.setText(mCrime.getTitle());
        mTitleField.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                mCrime.setTitle(s.toString());
                updateCrime();
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        mDateButton = (Button) v.findViewById(R.id.crime_date);
        mDateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FragmentManager fm = getFragmentManager();
                DatePickerFragment dialog = DatePickerFragment.newInstance(mCrime.getDate());
                dialog.setTargetFragment(CrimeFragment.this, REQUEST_DATE_CODE);
                dialog.show(fm, DIALOG_DATE_FRAGMENT);
            }
        });

        mTimeButton = (Button) v.findViewById(R.id.crime_time);
        mTimeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FragmentManager fm = getFragmentManager();
                TimePickerFragment dialog = TimePickerFragment.newInstance(mCrime.getDate());
                dialog.setTargetFragment(CrimeFragment.this, REQUEST_TIME_CODE);
                dialog.show(fm, DIALOG_TIME_FRAGMENT);
            }
        });

        updateDateTime(mCrime.getDate());

        mSolvedCheckBox = (CheckBox) v.findViewById(R.id.crime_solved);
        mSolvedCheckBox.setChecked(mCrime.isSolved());
        mSolvedCheckBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                mCrime.setSolved(isChecked);
                updateCrime();
            }
        });

        mReportButton = (Button) v.findViewById(R.id.crime_report);
        mReportButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ShareCompat.IntentBuilder builder = ShareCompat.IntentBuilder.from(getActivity());
                builder.setType("text/plain");
                builder.setText(getCrimeReport());
                builder.setSubject(getString(R.string.crime_report_subject));
                builder.setChooserTitle(R.string.send_report);
                builder.startChooser();
            }
        });

        final Intent pickContactIntent = new Intent(Intent.ACTION_PICK, ContactsContract.Contacts.CONTENT_URI);
        mSuspectButton = (Button) v.findViewById(R.id.crime_suspect);
        mSuspectButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivityForResult(pickContactIntent, REQUEST_CONTACT);
            }
        });

        if (mCrime.getSuspect() != null) {
            mSuspectButton.setText(mCrime.getSuspect());
        }

        PackageManager pm = getActivity().getPackageManager();
        if (pm.resolveActivity(pickContactIntent, PackageManager.MATCH_DEFAULT_ONLY) == null) {
            mSuspectButton.setEnabled(false);
        }

        mCallButton = (Button) v.findViewById(R.id.crime_call);
        mCallButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Uri phoneUri = Uri.parse(String.format("tel:%s", mCrime.getSuspectPhone()));
                Intent i = new Intent(Intent.ACTION_DIAL, phoneUri);
                startActivity(i);
            }
        });

        if (mCrime.getSuspectPhone() == null) {
            mCallButton.setEnabled(false);
        }

        mPhotoButton = (ImageButton) v.findViewById(R.id.crime_camera);
        final Intent captureImageIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        boolean canTakePhoto = mPhotoFile != null && captureImageIntent.resolveActivity(pm) != null;
        mPhotoButton.setEnabled(canTakePhoto);
        if (canTakePhoto) {
            Uri uri = Uri.fromFile(mPhotoFile);
            captureImageIntent.putExtra(MediaStore.EXTRA_OUTPUT, uri);
        }
        mPhotoButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivityForResult(captureImageIntent, REQUEST_PHOTO);
            }
        });

        mPhotoView = (ImageView) v.findViewById(R.id.crime_photo);

        mPhotoView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                File photo = CrimeLab.get(getActivity()).getPhotoFile(mCrime);
                String imagePath = photo != null ? photo.getPath() : null;
                ImageDialog dialog = ImageDialog.newInstance(imagePath);
                dialog.setTargetFragment(CrimeFragment.this, -1);
                dialog.show(getFragmentManager(), DIALOG_IMAGE_FRAGMENT);
            }
        });

        ViewTreeObserver observer = mPhotoView.getViewTreeObserver();
        observer.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                Log.d(TAG, "Photo view width: " + mPhotoView.getWidth());
                Log.d(TAG, "Photo view height: " + mPhotoView.getHeight());
                Log.d(TAG, "Photo view image: " + mPhotoView.getDrawable());
                if (mPhotoView.getDrawable() == null) {
                    updatePhotoView();
                }
            }
        });


        return v;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode != Activity.RESULT_OK) {
            return;
        }

        if (requestCode == REQUEST_DATE_CODE) {
            mCrime.setDate(DatePickerFragment.getDate(data));
            updateDateTime(mCrime.getDate());
            updateCrime();
        }

        if (requestCode == REQUEST_TIME_CODE) {
            Time time = TimePickerFragment.getTime(data);
            Date crimeDate = mCrime.getDate();
            Calendar c = GregorianCalendar.getInstance();
            c.setTime(crimeDate);
            c.set(Calendar.HOUR, time.getHour());
            c.set(Calendar.MINUTE, time.getMinute());
            mCrime.setDate(c.getTime());
            updateDateTime(mCrime.getDate());
            updateCrime();
        }

        if (requestCode == REQUEST_CONTACT && data != null) {
            Uri contactUri = data.getData();
            Log.d(TAG, contactUri.toString());
            String[] queryFields = new String[]{ContactsContract.Contacts.DISPLAY_NAME,
                    ContactsContract.Contacts._ID};

            Cursor c = getActivity().getContentResolver()
                    .query(contactUri, queryFields, null, null, null);
            String id = null;
            try {
                if (c.getCount() == 0) {
                    return;
                }

                c.moveToFirst();
                String suspect = c.getString(0);
                mCrime.setSuspect(suspect);
                mSuspectButton.setText(suspect);

                // getting phone
                id = c.getString(c.getColumnIndex(ContactsContract.Contacts._ID));
                Log.d(TAG, id);
            } finally {
                c.close();
            }

            if (id != null) {
                try {
                    Log.d(TAG, "SDK ver. " + Build.VERSION.SDK_INT);
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && PermissionChecker.checkSelfPermission(getActivity(), Manifest.permission.READ_CONTACTS) != PackageManager.PERMISSION_GRANTED) {
                        requestPermissions(new String[]{Manifest.permission.READ_CONTACTS}, PERMISSIONS_REQUEST_READ_CONTACTS);
                    }
                    c = getActivity().getContentResolver().query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null,
                            ContactsContract.CommonDataKinds.Phone.CONTACT_ID + "=?", new String[]{id},
                            null);
                } catch (Exception e) {
                    Log.e(TAG, e.toString());
                }
                try {
                    if (c.getCount() == 0) {
                        return;
                    }
                    c.moveToFirst();
                    String phone = c.getString(c.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
                    Log.d(TAG, phone);
                    mCrime.setSuspectPhone(phone);
                } catch (Exception e) {
                    Log.e(TAG, e.toString());
                } finally {
                    c.close();
                }
            }

        }

        if (requestCode == REQUEST_PHOTO) {
            updatePhotoView();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        //super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        Log.d(TAG, String.valueOf(requestCode));
        Log.d(TAG, grantResults.toString());
    }

    private void updateDateTime(Date date) {
        SimpleDateFormat df = new SimpleDateFormat("EEEE, MMM dd, yyyy");
        mDateButton.setText(df.format(date));

        df = new SimpleDateFormat("HH:mm");
        mTimeButton.setText(df.format(date));
    }

    private String getCrimeReport() {
        String solvedString = null;
        if (mCrime.isSolved()) {
            solvedString = getString(R.string.crime_report_solved);
        } else {
            solvedString = getString(R.string.crime_report_unsolved);
        }


        String dateFormat = "dd.MM.yyyy";
        String formattedDate = DateFormat.format(dateFormat, mCrime.getDate()).toString();

        String suspect = mCrime.getSuspect();
        if (suspect != null) {
            suspect = getString(R.string.crime_report_suspect, suspect);
        } else {
            suspect = getString(R.string.crime_report_no_suspect);
        }

        String report = getString(R.string.crime_report_info, mCrime.getTitle(),
                formattedDate, solvedString, suspect);

        return report;
    }

    private void updatePhotoView() {
        if (mPhotoFile == null || !mPhotoFile.exists()) {
            mPhotoView.setImageDrawable(null);
        } else {
            Log.d(TAG, "updatePhotoView: photo view width = " + mPhotoView.getWidth());
            mPhotoView.setImageBitmap(PictureUtils.getScaledBitmap(mPhotoFile.getPath(),
                    mPhotoView.getWidth(), mPhotoView.getHeight()));
        }
    }

    private void updateCrime() {
        CrimeLab.get(getActivity()).updateCrime(mCrime);
        mCallbacks.onCrimeUpdated(mCrime);
    }
}
