package com.contacts.ram.loadcontactsandroid;

import android.Manifest;
import android.app.AlertDialog;
import android.app.LoaderManager;
import android.content.*;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.CursorWrapper;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor> {

    private static final int PERMISSION_CONTACTS = 1;
    private static final int LOADER_CONTACTS = 11;

    private SimpleCursorAdapter adapter;
    private ContentResolver contentResolver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        contentResolver = this.getContentResolver();

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_CONTACTS) != PackageManager.PERMISSION_GRANTED) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                requestPermissions(new String[]{Manifest.permission.READ_CONTACTS}, PERMISSION_CONTACTS);
            }
        } else {
            getLoaderManager().initLoader(LOADER_CONTACTS, null, this);
        }


        final String[] from = {ContactsContract.Contacts.DISPLAY_NAME};
        int[] to = {android.R.id.text1};

        adapter = new SimpleCursorAdapter(this, android.R.layout.simple_list_item_1, null, from, to, 0);

        final ListView listView = (ListView) findViewById(R.id.listView);
        listView.setAdapter(adapter);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {

                final CursorWrapper cursorWrapper = (CursorWrapper) adapter.getItem(i);
                String a = cursorWrapper.getString(0);

                String Phone_CONTACT_ID = ContactsContract.CommonDataKinds.Phone.CONTACT_ID;
                String NUMBER = ContactsContract.CommonDataKinds.Phone.NUMBER;

                String EmailCONTACT_ID = ContactsContract.CommonDataKinds.Email.CONTACT_ID;
                String DATA = ContactsContract.CommonDataKinds.Email.DATA;


                Cursor phoneCursor = contentResolver.query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null, Phone_CONTACT_ID + " = ?", new String[]{a}, null);
                final ArrayList<String> phoneNumbers = new ArrayList<>();

                while (phoneCursor.moveToNext()) {
                    phoneNumbers.add(phoneCursor.getString(phoneCursor.getColumnIndex(NUMBER)));
                }

                Cursor emailCursor = contentResolver.query(ContactsContract.CommonDataKinds.Email.CONTENT_URI, null, EmailCONTACT_ID + " = ?", new String[]{a}, null);
                final ArrayList<String> emailIds = new ArrayList<>();

                while (emailCursor.moveToNext()) {
                    emailIds.add(emailCursor.getString(emailCursor.getColumnIndex(DATA)));
                }

                phoneCursor.close();
                emailCursor.close();
                final ArrayList<String> allContactModes = new ArrayList<>();
                allContactModes.addAll(phoneNumbers);
                allContactModes.addAll(emailIds);

                final Intent sendIntent = new Intent(Intent.ACTION_VIEW);
                sendIntent.setData(Uri.parse("sms:"));
                sendIntent.putExtra("sms_body", "hello? what's up");

                final Intent intent = new Intent(Intent.ACTION_SEND);
                intent.setType("plain/text");
                intent.putExtra(Intent.EXTRA_SUBJECT, "hi");
                intent.putExtra(Intent.EXTRA_TEXT, "hello");


                if (allContactModes.size() == 1 && phoneNumbers.size() == 1) {
                    sendIntent.putExtra("address", phoneNumbers.get(0));
                    startActivity(sendIntent);
                } else if (allContactModes.size() == 1 && emailIds.size() == 1) {
                    intent.putExtra(Intent.EXTRA_EMAIL, new String[]{emailIds.get(0)});
                    Intent mailer = Intent.createChooser(intent, "Send mail...");
                    startActivity(mailer);
                } else {
                    final String[] allContactMode = allContactModes.toArray(new String[allContactModes.size()]);
                    AlertDialog.Builder alt_bld = new AlertDialog.Builder(getApplicationContext());
                    alt_bld.setItems(allContactMode, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int item) {
                            String clickedContact = allContactMode[item];
                            if (phoneNumbers.contains(clickedContact)) {
                                sendIntent.putExtra("address", clickedContact);
                                dialog.dismiss();// dismiss the alertbox after chose option
                                startActivity(sendIntent);
                            } else {
                                intent.putExtra(Intent.EXTRA_EMAIL, new String[]{clickedContact});
                                Intent mailer = Intent.createChooser(intent, "Send mail...");
                                dialog.dismiss();// dismiss the alertbox after chose option
                                startActivity(mailer);
                            }
                        }
                    });
                    AlertDialog alert = alt_bld.create();
                    alert.show();
                }

            }

        });


    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        switch (requestCode) {
            case PERMISSION_CONTACTS:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    getLoaderManager().initLoader(LOADER_CONTACTS, null, this);
                }
                break;
        }
    }

    private static final String[] PROJECTION = {ContactsContract.Contacts._ID, ContactsContract.Contacts.DISPLAY_NAME,
            ContactsContract.Contacts.HAS_PHONE_NUMBER};


    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle bundle) {

        String[] selectionArgs = new String[]{" "};
        if (id == LOADER_CONTACTS) {
            return new CursorLoader(this, ContactsContract.Contacts.CONTENT_URI, PROJECTION, ContactsContract.Contacts.DISPLAY_NAME + " != ? ", selectionArgs, ContactsContract.Contacts.DISPLAY_NAME);
        } else {
            return null;
        }
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor c) {
        adapter.swapCursor(c);
        adapter.notifyDataSetChanged();
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        adapter.swapCursor(null);
    }

}