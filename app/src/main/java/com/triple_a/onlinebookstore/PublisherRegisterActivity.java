package com.triple_a.onlinebookstore;

import android.app.Activity;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.Toast;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.ArrayList;

import components.Commands;
import components.Pair;
import components.Publisher;
import components.ServerInfo;

public class PublisherRegisterActivity extends AppCompatActivity {

    private EditText name;
    private EditText email;
    private EditText pass;
    private EditText passAgain;
    private EditText phone;
    private Spinner securityQuestion;
    private EditText securityAnswer;
    private Button signUp;
    private Pair<ArrayList<Integer>, ArrayList<String>> securityQuestionsList;
    private ProgressBar progressBar;
    private Publisher publisher;
    private int questionPosition;
    private boolean taskRunning;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_publisher_register);

        name = findViewById(R.id.publisher_name);
        email = findViewById(R.id.publisher_register_email);
        pass = findViewById(R.id.publisher_register_password);
        passAgain = findViewById(R.id.publisher_register_password_again);
        phone = findViewById(R.id.publisher_phone);
        securityQuestion = findViewById(R.id.publisher_security_question);
        securityAnswer = findViewById(R.id.publisher_security_answer);
        signUp = findViewById(R.id.publisher_register_btn);
        progressBar = findViewById(R.id.publisher_register_progressBar);
        questionPosition = -1;
        taskRunning = false;

        loadSecurityQuestions();
        securityQuestion.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                questionPosition = position;
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                questionPosition = -1;
            }
        });

        signUp.setOnClickListener(v -> {
            if (!taskRunning && isValidFields()) {
                publisher = new Publisher(name.getText().toString(),
                        email.getText().toString(),
                        pass.getText().toString(),
                        phone.getText().toString(),
                        securityQuestionsList.getFirst().get(questionPosition),
                        securityAnswer.getText().toString());
                AsyncTask<Publisher, Void, Boolean> task = new AddUserTask().execute(publisher);
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    private boolean isValidFields() {
        boolean f = true;
        String msg = "";
        if (name.getText().length() == 0) {
            f = false;
            msg += "\nEnter your name";
        }
        if (!Patterns.EMAIL_ADDRESS.matcher(email.getText().toString()).matches()) {
            f = false;
            msg += "\nEnter a valid email";
        }
        if (pass.getText().length() < 5) {
            f = false;
            msg += "\nPassword length should be al least 5";
        }
        if(!passAgain.getText().toString().equals(pass.getText().toString())){
            f = false;
            msg += "\nPassword Mismatch";
        }
        if (phone.getText().length() != 11) {
            f = false;
            msg += "\nPhone Number must be 11 digit.";
        }
        if (questionPosition == -1) {
            f = false;
            msg += "\nChoose a security question";
        }
        if (questionPosition != -1 && securityAnswer.getText().length() == 0) {
            f = false;
            msg += "\nGive a answer of your chosen security question";
        }
        if (!f) {
            Toast.makeText(this, msg.substring(1), Toast.LENGTH_LONG).show();
        }
        return f;
    }

    private void loadSecurityQuestions() {
        new loadQuestions().execute();
    }

    private class loadQuestions extends AsyncTask<Void, Void, Boolean> {
        @Override
        protected Boolean doInBackground(Void... voids) {
            Socket client = ServerInfo.getClientSocket();
            try {
                ObjectOutputStream oos = new ObjectOutputStream(client.getOutputStream());
                oos.flush();
                ObjectInputStream ois = new ObjectInputStream(client.getInputStream());
                oos.writeObject(Commands.LOAD_SECURITY_QUESTIONS);
                if (ois.readObject().equals(Boolean.FALSE))
                    return Boolean.FALSE;

                if (ois.readObject().equals(Boolean.FALSE)) {
                    Log.e("load security questions", (String) ois.readObject());
                    return Boolean.FALSE;
                } else {
                    Object o = ois.readObject();
                    securityQuestionsList = (Pair<ArrayList<Integer>, ArrayList<String>>) o;
                    /*
                    Pair oo = (Pair)o;
                    ArrayList ali = (ArrayList)oo.getFirst();
                    ArrayList als = (ArrayList)oo.getSecond();
                    ArrayList<Integer> arrayListInteger = new ArrayList<>();
                    for(Object i:ali)
                        arrayListInteger.add((Integer)i);
                    ArrayList<String> arrayListString = new ArrayList<>();
                    for(Object i:als)
                        arrayListString.add((String)i);
                    securityQuestionsList = new Pair<>(arrayListInteger, arrayListString);
                    */
                    return Boolean.TRUE;
                }
            } catch (IOException | ClassNotFoundException e) {
                e.printStackTrace();
                Log.e("load security questions", e.getStackTrace().toString());
            }
            return Boolean.FALSE;
        }

        @Override
        protected void onPostExecute(Boolean aBoolean) {
            super.onPostExecute(aBoolean);
            if (aBoolean) {
                String[] ara = new String[securityQuestionsList.getSecond().size()];
                securityQuestionsList.getSecond().toArray(ara);
                ArrayAdapter<String> adapter = new ArrayAdapter<>(
                        PublisherRegisterActivity.this, android.R.layout.simple_spinner_item, ara);
                adapter.setDropDownViewResource(R.layout.my_spinner_dropdown_item);
                securityQuestion.setAdapter(adapter);
            }
            else{
                Toast.makeText(PublisherRegisterActivity.this,
                        "Failed to load spinner. Recheck your internet connection and relaunch app",
                        Toast.LENGTH_LONG).show();
            }
        }
    }

    private class AddUserTask extends AsyncTask<Publisher, Void, Boolean> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            taskRunning = true;
            progressBar.setVisibility(View.VISIBLE);
        }

        @Override
        protected Boolean doInBackground(Publisher... publishers) {
            Socket client = ServerInfo.getClientSocket();
            try {
                ObjectOutputStream oos = new ObjectOutputStream(client.getOutputStream());
                oos.flush();
                ObjectInputStream ois = new ObjectInputStream(client.getInputStream());
                oos.writeObject(Commands.PUBLISHER_REGISTER);
                if (ois.readObject().equals(Boolean.FALSE))
                    return Boolean.FALSE;

                oos.writeObject(publishers[0]);
                oos.flush();

                if (ois.readObject().equals(Boolean.FALSE)) {
                    Log.e("add publisher", (String) ois.readObject());
                    return Boolean.FALSE;
                } else {
                    return Boolean.TRUE;
                }
            } catch (IOException | ClassNotFoundException e) {
                e.printStackTrace();
                Log.e("load security questions", e.getStackTrace().toString());
            }
            return Boolean.FALSE;
        }

        @Override
        protected void onPostExecute(Boolean aBoolean) {
            super.onPostExecute(aBoolean);
            taskRunning = false;
            progressBar.setVisibility(View.INVISIBLE);
            if (aBoolean) {
                Toast.makeText(PublisherRegisterActivity.this, "User Successfully added.", Toast.LENGTH_SHORT).show();

                Intent resultantIntent = new Intent();
                resultantIntent.putExtra(LoginAsActivity.USER_EMAIL_KEY, publisher.getUserEmail());
                resultantIntent.putExtra(LoginAsActivity.USER_PASSWORD_KEY, publisher.getPassword());
                setResult(Activity.RESULT_OK, resultantIntent);
                finish();
            } else
                Toast.makeText(PublisherRegisterActivity.this, "User registration failed.", Toast.LENGTH_SHORT).show();
        }
    }
}
