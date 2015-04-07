package com.xrci.standup;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Fragment;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.Spinner;

import com.xrci.standup.utility.AuthenticationModel;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Calendar;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class BasicInformationForm extends Activity {
    private Pattern pattern;
    private Matcher matcher;
    public static String registrationName = "registrationName";

    public static String registrationAge = "registrationAge";
    public static String registrationWeight = "registrationWeight";
    public static String registrationHeight = "registrationHeight";
    public static String registrationEmail = "registrationEmail";
    public static String registrationOrganization = "registrationOrganization";
    public static String registrationSex = "registrationSex";
    public static String registrationFormFilled = "registrationFormFilled";


    private static final String EMAIL_PATTERN =
            "^[_A-Za-z0-9-\\+]+(\\.[_A-Za-z0-9-]+)*@"
                    + "[A-Za-z0-9-]+(\\.[A-Za-z0-9]+)*(\\.[A-Za-z]{2,})$";

    /**
     * Validate hex with regular expression
     *
     * @param hex hex for validation
     * @return true valid hex, false invalid hex
     */
    public boolean validate(final String hex) {

        matcher = pattern.matcher(hex);
        return matcher.matches();

    }


    public void registerBasicInformation(View view) {

        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        EditText nameEt = (EditText) findViewById(R.id.nameText);
//        EditText organizationEt = (EditText) findViewById(R.id.organizationText);
        Spinner organizationList = (Spinner)findViewById(R.id.organizationList);
//        organizationList.setOnItemSelectedListener(BasicInformationForm.this);





        EditText emailEt = (EditText) findViewById(R.id.emailText);
        EditText ageEt = (EditText) findViewById(R.id.ageText);
        EditText weightEt = (EditText) findViewById(R.id.textWeight);
        EditText heightEt = (EditText) findViewById(R.id.textHeight);

        RadioButton sexRbMale = (RadioButton) findViewById(R.id.radioMale);
        RadioButton sexRbFemale = (RadioButton) findViewById(R.id.radioFemale);

        String nameString = nameEt.getText().toString();

        String organizationString = organizationList.getSelectedItem().toString();
        String ageString = ageEt.getText().toString();
        String weightString = weightEt.getText().toString();
        String heightString = heightEt.getText().toString();
//        OrganizationModel organizationModel = new OrganizationModel();
//        ArrayList<String> orgList = organizationModel.getOrgaizationList();
        String emailString = emailEt.getText().toString();
        pattern = Pattern.compile(EMAIL_PATTERN);
        String displayString;


        if (nameString.equals("") || organizationString.equals("")
                || ageString.equals("") || weightString.equals("") || !validate(emailString) || heightString.equals("")) {

            // set dialog message
            //TODO email id is not validated.
            if (nameString.equals(""))
                displayString = "Enter your name.";
            else if (organizationString.equals(""))
                displayString = "Select your organization name. Make sure you are connected to internet.";
            else if (ageString.equals(""))
                displayString = "Enter your age.";
            else if (weightString.equals(""))
                displayString = "Enter your weight.";
            else if (heightString.equals(""))
                displayString = "Enter your height.";

            else
                displayString = "Please enter a valid email id.";


            Log.i("check", "name " + nameString + "org " + organizationString + "a" + ageString + "w " + weightString);

            AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(
                    this);

            // set title
            alertDialogBuilder.setTitle("Set Details Again");

            alertDialogBuilder
                    .setMessage(displayString)
                    .setCancelable(true)
                    .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            // if this button is clicked, just close
                            // the dialog box and do nothing
                            dialog.cancel();
                        }
                    });

            // create alert dialog
            AlertDialog alertDialog = alertDialogBuilder.create();
            // show it
            alertDialog.show();

        } else {

//            SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putString(registrationName, nameString);
            editor.putString(registrationAge, ageString);
            editor.putString(registrationWeight, weightString);
            editor.putString(registrationHeight, heightString);
            editor.putString(registrationEmail, emailString);
            editor.putString(registrationOrganization, organizationString);
            if (sexRbFemale.isChecked())
                editor.putString(registrationSex, "F");
            else
                editor.putString(registrationSex, "M");
            editor.putBoolean(registrationFormFilled, true);

            editor.commit();

            SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
            String fbid = preferences.getString("fbid", "1");
            SharedPreferences.Editor editor_userId = preferences.edit();
            AuthenticationModel authModel = new AuthenticationModel(nameString
                    , emailString, "facebook", fbid, Calendar.getInstance().getTime(), "M", organizationString, ageString, weightString, heightString);
            String response = authModel.verifyAuthentication();
            Log.i("check", "authentication response is " + response);

            AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(
                    this);


            if (response.equals(PostData.INVALID_RESPONSE) || response.equals(PostData.EXCEPTION) ) {

                alertDialogBuilder.setTitle("Internet Connection Unavailable");

                alertDialogBuilder
                        .setMessage("Check your internet connection and try again.")
                        .setCancelable(true)
                        .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                // if this button is clicked, just close
                                // the dialog box and do nothing
                                dialog.cancel();
                            }
                        });

                // create alert dialog
                AlertDialog alertDialog = alertDialogBuilder.create();
                // show it
                alertDialog.show();


            } else if (response.equals(PostData.INVALID_PAYLOAD)) {


                alertDialogBuilder.setTitle("Oops");

                alertDialogBuilder
                        .setMessage("This is embarrassing . Something went wrong.")
                        .setCancelable(true)
                        .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                // if this button is clicked, just close
                                // the dialog box and do nothing
                                dialog.cancel();
                            }
                        });

                // create alert dialog
                AlertDialog alertDialog = alertDialogBuilder.create();
                // show it
                alertDialog.show();

            } else{
                editor_userId.putInt("userId", setUserId(response));
                editor_userId.commit();

                finish();
                Intent intent = new Intent(this, MainActivity.class);
                startActivity(intent);
            }

        }

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_basic_information_form);
        if (savedInstanceState == null) {
            getFragmentManager().beginTransaction()
                    .add(R.id.container, new PlaceholderFragment())
                    .commit();
        }




    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_basic_information_form, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
//        int id = item.getItemId();
//
//        //noinspection SimplifiableIfStatement
//        if (id == R.id.action_settings) {
//            return true;
//        }

        return super.onOptionsItemSelected(item);
    }

    /**
     * A placeholder fragment containing a simple view.
     */
    public static class PlaceholderFragment extends Fragment {

        public PlaceholderFragment() {
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {


            View rootView = inflater.inflate(R.layout.fragment_basic_information_form, container, false);
            try {

                Spinner dropdown = (Spinner) rootView.findViewById(R.id.organizationList);
                String[] items = new String[]{"XIL", "XRCI"};

                ArrayAdapter<String> adapter = new ArrayAdapter<String>(getActivity(), android.R.layout.simple_spinner_dropdown_item, items);
                dropdown.setAdapter(adapter);


                SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getActivity().getApplicationContext());
                if (sharedPreferences.getString(registrationOrganization, "") != "") {
                    String myString = sharedPreferences.getString(registrationOrganization, ""); //the value you want the position for

                    ArrayAdapter checkAdapter = (ArrayAdapter) dropdown.getAdapter(); //cast to an ArrayAdapter

                    int spinnerPosition = checkAdapter.getPosition(myString);

//set the default according to value
                    dropdown.setSelection(spinnerPosition);
                }


                EditText nameEt = (EditText) rootView.findViewById(R.id.nameText);
                nameEt.setText(sharedPreferences.getString(registrationName, ""));
//            EditText organizationEt = (EditText) rootView.findViewById(R.id.organizationText);
//            organizationEt.setText(sharedPreferences.getString(registrationOrganization, ""));

                EditText emailEt = (EditText) rootView.findViewById(R.id.emailText);
                emailEt.setText(sharedPreferences.getString(registrationEmail, ""));
                EditText ageEt = (EditText) rootView.findViewById(R.id.ageText);
                ageEt.setText(sharedPreferences.getString(registrationAge, ""));
                EditText weightEt = (EditText) rootView.findViewById(R.id.textWeight);
                weightEt.setText(sharedPreferences.getString(registrationWeight, ""));

                EditText heightEt = (EditText) rootView.findViewById(R.id.textHeight);
                heightEt.setText(sharedPreferences.getString(registrationHeight, ""));

                RadioButton sexRbMale = (RadioButton) rootView.findViewById(R.id.radioMale);
                RadioButton sexRbFemale = (RadioButton) rootView.findViewById(R.id.radioFemale);
                if (sharedPreferences.getString(registrationSex, "") == "M") {
                    sexRbMale.setChecked(true);
                } else if (sharedPreferences.getString(registrationSex, "") == "F")
                    sexRbFemale.setChecked(true);
            } catch (Exception e) {
                Log.i("BasicInformationForm", "Exception in BasicInformationForm " +  e.getMessage());
            }
            return rootView;
        }
    }


    public int setUserId(String response) {
        try {
            JSONObject jsonObject = new JSONObject(response);
            return jsonObject.getInt("id");
        } catch (JSONException e) {
            Log.i("check", "user id cannot be created because " + e.getMessage());
        }


        return 0;
    }


}
