package com.triple_a.onlinebookstore;

import android.app.Activity;
import android.content.Intent;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

import components.Commands;
import components.ServerInfo;
import components.User;
import components.UserType;

import static com.triple_a.onlinebookstore.LoginAsActivity.USER_EMAIL_KEY;
import static com.triple_a.onlinebookstore.LoginAsActivity.USER_PASSWORD_KEY;
import static com.triple_a.onlinebookstore.PasswordRecoveryActivity.USER_TYPE_KEY;

public class SetPasswordAfterRecoveryActivity extends AppCompatActivity {

    private EditText newPass;
    private EditText againNewPass;
    private Button setPassBtn;
    private String userEmail;
    private UserType userType;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_set_password_after_recovery);

        userEmail = getIntent().getStringExtra(USER_EMAIL_KEY);
        userType = (UserType)getIntent().getExtras().get(USER_TYPE_KEY);

        newPass = findViewById(R.id.new_password);
        againNewPass  = findViewById(R.id.again_new_password);
        setPassBtn = findViewById(R.id.set_pass_btn);

        setPassBtn.setOnClickListener(v->{
            if(newPass.getText().length() >= 5 &&
                    newPass.getText().toString().equals(againNewPass.getText().toString())){
                new AsyncTask<Void, Void, Boolean>() {
                    @Override
                    protected Boolean doInBackground(Void... voids) {
                        Socket client = ServerInfo.getClientSocket();
                        try {
                            ObjectOutputStream oos = new ObjectOutputStream(client.getOutputStream());
                            oos.flush();
                            ObjectInputStream ois = new ObjectInputStream(client.getInputStream());
                            oos.writeObject(Commands.UPDATE_PASSWORD);
                            if (ois.readObject().equals(Boolean.FALSE))
                                return Boolean.FALSE;

                            oos.writeObject(new User(null, userEmail, newPass.getText().toString(), userType));
                            if (ois.readObject().equals(Boolean.FALSE)) {
                                Log.e("getUser", (String) ois.readObject());
                                return Boolean.FALSE;
                            } else {
                                return Boolean.TRUE;
                            }
                        } catch (IOException | ClassNotFoundException e) {
                            e.printStackTrace();
                            Log.e("getUser", e.getStackTrace().toString());
                        }
                        return false;
                    }

                    @Override
                    protected void onPostExecute(Boolean aBoolean) {
                        super.onPostExecute(aBoolean);
                        if(aBoolean){
                            Intent intent = new Intent();
                            intent.putExtra(USER_PASSWORD_KEY, newPass.getText().toString());
                            setResult(Activity.RESULT_OK, intent);
                            finish();
                        }
                        else
                            Toast.makeText(SetPasswordAfterRecoveryActivity.this, "Operation failed. try again", Toast.LENGTH_SHORT).show();
                    }
                }.execute();
            }
            else
                Toast.makeText(this,
                        "Password length should be >= 5 or password mismatch",
                        Toast.LENGTH_SHORT).show();
        });
    }


}
