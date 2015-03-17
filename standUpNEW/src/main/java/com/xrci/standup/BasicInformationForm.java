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
import android.widget.EditText;
import android.widget.RadioButton;

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

        EditText nameEt = (EditText) findViewById(R.id.nameText);
        EditText organizationEt = (EditText) findViewById(R.id.organizationText);
        EditText emailEt = (EditText) findViewById(R.id.emailText);
        EditText ageEt = (EditText) findViewById(R.id.ageText);
        EditText weightEt = (EditText) findViewById(R.id.textWeight);
        RadioButton sexRbMale = (RadioButton) findViewById(R.id.radioMale);
        RadioButton sexRbFemale = (RadioButton) findViewById(R.id.radioFemale);
        String nameString = nameEt.getText().toString();
        String organizationString = organizationEt.getText().toString();
        String ageString = ageEt.getText().toString();
        String weightString = weightEt.getText().toString();
        String emailString = emailEt.getText().toString();
        pattern = Pattern.compile(EMAIL_PATTERN);
        String displayString;


        if (nameString.equals("") || organizationString.equals("")
                || ageString.equals("") || weightString.equals("") || !validate(emailString)) {

            // set dialog message
            //TODO email id is not validated.
            if (nameString.equals(""))
                displayString = "Enter your name.";
            else if (organizationString.equals(""))
                displayString = "Enter your organization name.";
            else if (ageString.equals(""))
                displayString = "Enter your age.";
            else if (weightString.equals(""))
                displayString = "Enter your weight.";
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

            SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putString(registrationName, nameString);
            editor.putString(registrationAge, ageString);
            editor.putString(registrationWeight, weightString);
            editor.putString(registrationEmail, emailString);
            editor.putString(registrationOrganization, organizationString);
            if (sexRbFemale.isSelected())
                editor.putString(registrationSex, "M");
            else
                editor.putString(registrationSex, "F");
            editor.putBoolean(registrationFormFilled, true);

            editor.commit();

            SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
            String fbid = preferences.getString("fbid", "1");
            SharedPreferences.Editor editor_userId = preferences.edit();
            AuthenticationModel authModel = new AuthenticationModel(nameString
                    , emailString, "facebook", fbid, Calendar.getInstance().getTime(), "M", organizationString, ageString, weightString);
            String response = authModel.verifyAuthentication();

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
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

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
