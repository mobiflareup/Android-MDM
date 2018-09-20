package com.mobiocean.ui;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.provider.ContactsContract;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import com.mobiocean.R;
import com.mobiocean.util.CallHelper;
import com.mobiocean.util.RestApiCall;
import com.mobiocean.util.SOSContacts;
import com.mobiocean.util.SOSContacts.SosContactsClass;

import java.util.ArrayList;

public class SecurityFragment extends Fragment {

    public SecurityFragment() {

    }

    ArrayList<String> contacts = new ArrayList<String>();
    ArrayAdapter<String> customAdapter;
    protected static final String PREFS_NAME = "MyPrefsFile";
    public SharedPreferences settings;
    public SharedPreferences.Editor editor;
    private ListView listViewContacts;
    private Handler handler;
    private int oldSize = 0;
    public static final int PICK_CONTACT = 1;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.fragment_security, container, false);

        Button btnAdd = (Button) rootView.findViewById(R.id.btnAdd);
        Button btnUpdate = (Button) rootView.findViewById(R.id.btnUpdate);
        Button btnAddContact = (Button) rootView.findViewById(R.id.btnAddContact);
        Button updateFrequency = (Button) rootView.findViewById(R.id.updateFrequency);
        listViewContacts = (ListView) rootView.findViewById(R.id.listContacts);
        final EditText editNumber = (EditText) rootView.findViewById(R.id.editPhoneNumber);
        final EditText button_press_count = (EditText) rootView.findViewById(R.id.button_press_count);

        handler = new Handler();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB)
            settings = getActivity().getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE | Context.MODE_MULTI_PROCESS);
        else
            settings = getActivity().getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        editor = settings.edit();

        new GetSosContacts().execute();

        contacts = getContactsFromPrefarence();
        oldSize = contacts.size();

        customAdapter = new ArrayAdapter<String>(getActivity(), android.R.layout.simple_list_item_1, contacts);
        listViewContacts.setAdapter(customAdapter);

        button_press_count.setText(String.valueOf(SOSReceiver.REPEAT_COUNT));


        btnAdd.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                final String number = editNumber.getText().toString();
                if (!TextUtils.isEmpty(number) && !contacts.contains(number)) {
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            editNumber.setText("");
                            contacts.add(number);
                            customAdapter.notifyDataSetChanged();
                        }
                    });
                } else if (TextUtils.isEmpty(number)) {
                    Toast.makeText(getActivity(), "Please enter number", Toast.LENGTH_SHORT).show();
                } else
                    Toast.makeText(getActivity(), "Number Already there.", Toast.LENGTH_SHORT).show();

            }
        });

        btnAddContact.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
//                Intent intent = new Intent(Intent.ACTION_PICK, Contacts.People.CONTENT_URI);
//                startActivityForResult(intent, PICK_CONTACT);
                Intent intent = new Intent(Intent.ACTION_PICK, ContactsContract.Contacts.CONTENT_URI);
                intent.setType(ContactsContract.CommonDataKinds.Phone.CONTENT_TYPE);
                startActivityForResult(intent, PICK_CONTACT);
            }
        });

        btnUpdate.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
//				if(oldSize!=contacts.size())
                if (contacts.size() > 0 || contacts != null)
                    new updateSosContacts().execute();
                else
                    Toast.makeText(getActivity(), "Must have at least one contact to update", Toast.LENGTH_SHORT).show();
//				else
//					Toast.makeText(getActivity(), "Nothing for updation.",Toast.LENGTH_SHORT).show();

            }
        });

        updateFrequency.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                String count = button_press_count.getText().toString();
                try {
                    SOSReceiver.REPEAT_COUNT = Integer.parseInt(count);
                    Toast.makeText(getActivity(), "Updated Sucessfully", Toast.LENGTH_SHORT).show();
                }catch (Exception e){
                    SOSReceiver.REPEAT_COUNT = 5;
                }
            }
        });
        listViewContacts.setOnItemLongClickListener(new OnItemLongClickListener() {

            @Override
            public boolean onItemLongClick(AdapterView<?> arg0, View arg1,
                                           final int arg2, long arg3) {
                AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(getActivity());
                alertDialogBuilder.setMessage("Do you want to remove " + contacts.get(arg2) + " from your sos list ?")
                        .setCancelable(false)
                        .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                contacts.remove(arg2);
                                customAdapter.notifyDataSetChanged();
                            }
                        })
                        .setNegativeButton("No", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.cancel();
                            }
                        });
                AlertDialog alertDialog = alertDialogBuilder.create();
                alertDialog.show();
                return false;
            }
        });
        return rootView;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == Activity.RESULT_OK) {
            switch (requestCode) {
                case PICK_CONTACT:
                    Uri contactData = data.getData();
                    if(contactData!=null){
                        Cursor c = null;
                        try{
                            c = getActivity().getContentResolver().query(contactData, new String[]{
                                        ContactsContract.CommonDataKinds.Phone.NUMBER,
                                        ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME },
                                null, null, null);
                            if (c != null && c.moveToFirst()) {
                                String number = c.getString(0);
                                contacts.add(number);
                                customAdapter.notifyDataSetChanged();
                            }
                        }finally {
                            if (c != null) {
                                c.close();
                            }
                        }
                    }
                    Toast.makeText(getContext(), "Contact Added Successfully", Toast.LENGTH_LONG).show();
                    break;
            }
        }
    }

    private ArrayList<String> getContactsFromPrefarence() {
        ArrayList<String> contactList = new ArrayList<String>();
        int size = settings.getInt("contactSize", 0);
        for (int i = 0; i < size; i++) {
            contactList.add(settings.getString("contact" + i, ""));
        }
        return contactList;
    }

    private void saveToPrefarence(ArrayList<String> contactList) {

        editor.putInt("contactSize", contactList.size());
        for (int i = 0; i < contactList.size(); i++)
            editor.putString("contact" + i, contactList.get(i));

        editor.commit();
    }

    class updateSosContacts extends AsyncTask<Void, Void, Integer> {

        protected ProgressDialog ringProgressDialog;

        @Override
        protected void onPreExecute() {
            ringProgressDialog = ProgressDialog.show(getActivity(), "Please wait ...", "Updating ...", true);
            ringProgressDialog.setCancelable(false);

            super.onPreExecute();
        }

        @Override
        protected Integer doInBackground(Void... params) {

            SOSContacts mSosContacts = new SOSContacts();
            ArrayList<SosContactsClass> listSosContacts = mSosContacts.getListContacts();
            for (int i = 0; i < contacts.size(); i++) {
                SosContactsClass contact = mSosContacts.getNewmSosContacts();
                contact.setContactPersonName("");
                contact.setMobileNo(contacts.get(i));
                listSosContacts.add(contact);

            }
            mSosContacts.setListContacts(listSosContacts);
            mSosContacts.setAppID(CallHelper.Ds.structPC.iStudId);

            RestApiCall restApiCall = new RestApiCall();
            String result = restApiCall.sendSosContacts((MobiApplication) getActivity().getApplication(), mSosContacts);
            if (!TextUtils.isEmpty(result) && TextUtils.isDigitsOnly(result)) {
                int respose = Integer.parseInt(result);
                if (respose > 0) {
                    saveToPrefarence(contacts);
                    return 1;
                }
            }
            return 0;
        }

        @Override
        protected void onPostExecute(Integer result) {
            ringProgressDialog.dismiss();
            if (result == 1)
                Toast.makeText(getActivity(), "Updated", Toast.LENGTH_SHORT).show();
            else
                Toast.makeText(getActivity(), "Please try again.", Toast.LENGTH_SHORT).show();

            super.onPostExecute(result);
        }


    }

    class GetSosContacts extends AsyncTask<Void, Void, Void> {

        protected ProgressDialog ringProgressDialog;

        @Override
        protected void onPreExecute() {
            ringProgressDialog = ProgressDialog.show(getActivity(), "Please wait ...", "Updating ...", true);
            ringProgressDialog.setCancelable(false);

            super.onPreExecute();
        }

        @Override
        protected Void doInBackground(Void... params) {
            ringProgressDialog.dismiss();
            RestApiCall mRestApiCall = new RestApiCall();
            ArrayList<SosContactsClass> contactList = mRestApiCall.getSosContacts((MobiApplication) getActivity().getApplication(), CallHelper.Ds.structPC.iStudId);
            if (contactList != null && !contactList.isEmpty()) {
                ArrayList<String> listContact = new ArrayList<String>(contactList.size());
                for (int i = 0; i < contactList.size(); i++)
                    listContact.add(contactList.get(i).getMobileNo());
                saveToPrefarence(listContact);
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        if (listViewContacts != null) {
                            customAdapter.notifyDataSetChanged();
                            ArrayList<String> temp = getContactsFromPrefarence();
                            if (temp != null && temp.size() > 0) {
                                contacts.clear();
                                contacts.addAll(temp);
                                oldSize = contacts.size();
                            }
                        }
                    }
                });
            }
            return null;
        }

    }

}
