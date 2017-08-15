package com.willvelida.numberfacts;

import android.content.Context;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.twitter.sdk.android.tweetcomposer.TweetComposer;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.Random;

public class MainActivity extends AppCompatActivity {

    // UI Variables
    EditText numberText;
    Spinner numberType;
    Button searchNumberButton;
    Button searchRandomButton;
    TextView resultTextView;
    Button tweetComposer;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // UI Elements on create
        numberText = (EditText) findViewById(R.id.numberEditText);
        numberType = (Spinner) findViewById(R.id.numberTypeSpinner);
        searchNumberButton = (Button) findViewById(R.id.searchButton);
        searchRandomButton = (Button) findViewById(R.id.searchRandomButton);
        resultTextView = (TextView) findViewById(R.id.resultTextView);
        tweetComposer = (Button) findViewById(R.id.twitterButton);

        tweetComposer.setEnabled(false);

        // Set spinner values to spinner
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this, R.array.strTypes, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        numberType.setAdapter(adapter);
    }

    // Find fact on based on entered number
    public void searchNumber(View view) {
        // Call our inputManager to get rid of keyboard
        inputManager();

        try {
            String encodedNumber = URLEncoder.encode(numberText.getText().toString(), "UTF-8");
            String encodedNumberType = URLEncoder.encode(numberType.getSelectedItem().toString(), "UTF-8");
            if (encodedNumber == "" || encodedNumber.isEmpty()) {
                Toast.makeText(getApplicationContext(), "Please enter a number!", Toast.LENGTH_LONG).show();
            } else {
                // We can now tweet facts!
                tweetComposer.setEnabled(true);
                DownloadTask task = new DownloadTask();
                String URLsent = "http://numbersapi.com/" + encodedNumber + "/" + encodedNumberType + "?json";
                task.execute(URLsent);
            }
        } catch (Exception e) {
            Toast.makeText(getApplicationContext(), "Fact not found!", Toast.LENGTH_LONG).show();
            e.printStackTrace();
        }

    }

    // Search fact based on random number
    public void searchRandom(View view) {
        tweetComposer.setEnabled(true);
        // Pick a random category
        String category = "";
        Random rand = new Random();
        int number = rand.nextInt(3) + 1;

        if (number == 1) {
            category = "math";
        } else if (number == 2) {
            category = "year";
        } else {
            category = "trivia";
        }

        // once a category is picked, get a random fact
        try {
            DownloadTask task = new DownloadTask();
            task.execute("http://numbersapi.com/random/" + category + "?json");
        } catch (Exception e) {
            Toast.makeText(getApplicationContext(), "Fact not found!", Toast.LENGTH_LONG).show();
            e.printStackTrace();
        }
    }

    // Input Manager Method: Clear keyboard when done with input
    public void inputManager() {
        InputMethodManager mgr = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        mgr.hideSoftInputFromWindow(numberText.getWindowToken(), 0);
    }

    // Compose Tweet
    public void composeTweet(View view) {
        try {
            new TweetComposer.Builder(MainActivity.this)
                    .text("Did you know " + resultTextView.getText())
                    .show();
        } catch (Exception e) {
            Toast.makeText(getApplicationContext(), "Sorry! We can't tweet this right now!", Toast.LENGTH_LONG).show();
            e.printStackTrace();
        }
    }

    // Download Task
    public class DownloadTask extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... urls) {
            // Initialize the result variable
            String result = "";
            // Create URL variables
            URL url;
            // Create HTTP URL connection
            HttpURLConnection urlConnection = null;

            try {
                url = new URL(urls[0]);
                urlConnection = (HttpURLConnection) url.openConnection();
                InputStream in = urlConnection.getInputStream();
                InputStreamReader reader = new InputStreamReader(in);
                int data = reader.read();
                while (data != -1) {
                    char current = (char) data;
                    result += current;
                    data = reader.read();
                }
                return result;
            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                Toast.makeText(getApplicationContext(), "Could not find fact!", Toast.LENGTH_LONG).show();
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);

            try {
                String message = "";
                JSONObject jsonObject = new JSONObject(result);
                String text = "";
                String number = "";
                String type = "";

                text = jsonObject.getString("text");
                number = jsonObject.getString("number");
                type = jsonObject.getString("type");

                if (text != "") {
                    resultTextView.setText(text);
                } else {
                    resultTextView.setText("Fact not found! Try a different number.");
                }


            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

}
