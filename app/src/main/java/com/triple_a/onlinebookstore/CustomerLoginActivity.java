package com.triple_a.onlinebookstore;

import android.app.Activity;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.util.Patterns;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.TextView;
import android.widget.Toast;

import com.triple_a.onlinebookstore.utils.AlreadyLoggedIn;
import com.triple_a.onlinebookstore.utils.LogInInfoWriter;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.concurrent.ExecutionException;

import components.Commands;
import components.ServerInfo;
import components.User;
import components.UserType;

import static com.triple_a.onlinebookstore.LoginAsActivity.USER_EMAIL_KEY;
import static com.triple_a.onlinebookstore.PasswordRecoveryActivity.USER_TYPE_KEY;

public class CustomerLoginActivity extends AppCompatActivity {

    public static final int REGISTER_CODE = 1;
    public static final int FORGOT_PASS_CODE = 2;
    private TextView tvEmail;
    private TextView tvPassword;
    private CheckBox rememberMe;
    private Button signIn;
    private Button signUp;
    private Button forgotPass;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_customer_login);

        tvEmail = findViewById(R.id.customer_login_email);
        tvPassword = findViewById(R.id.customer_login_password);
        rememberMe = findViewById(R.id.customer_login_remember_me);
        signIn = findViewById(R.id.customer_signin_btn);
        signUp = findViewById(R.id.customer_signup_btn);
        forgotPass = findViewById(R.id.customer_forgot);

        signIn.setOnClickListener(view -> {
            signInUser();
        });
        signUp.setOnClickListener(v -> signUpUser());
        forgotPass.setOnClickListener(v -> recoverPassword());
    }

    private void signInUser() {
        if (!isValidFields()) {
            Toast.makeText(this, "Give proper value in fields.", Toast.LENGTH_SHORT).show();
            return;
        }

        User user = new User(null, tvEmail.getText().toString(), tvPassword.getText().toString(), UserType.CUSTOMER);

        AsyncTask<User, Void, Boolean> loginTask = new LoginTask().execute(user);
        try {
            if (loginTask.get()) {
                Intent intent = new Intent(this, CustomerHomeActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                intent.putExtra(USER_EMAIL_KEY, user.getUserEmail());

                if (rememberMe.isChecked())
                    LogInInfoWriter.write(
                            new AlreadyLoggedIn(true, user.getUserEmail(), user.getPassword(), user.getUserType())
                    );
                else
                    LogInInfoWriter.write(
                            new AlreadyLoggedIn(false, null, null, null)
                    );
                startActivity(intent);
                finish();
            } else {
                toastFailureMsg();
            }
        } catch (InterruptedException | ExecutionException e) {
            toastFailureMsg();
            e.printStackTrace();
        }
    }

    private void signUpUser() {
        startActivityForResult(new Intent(this, CustomerRegisterActivity.class), REGISTER_CODE);
    }

    private boolean isValidFields() {

        boolean f = true;
        if (!Patterns.EMAIL_ADDRESS.matcher(tvEmail.getText()).matches())
            f = false;
        else if (tvPassword.getText().length() == 0)
            f = false;
        return f;
    }

    class LoginTask extends AsyncTask<User, Void, Boolean> {
        @Override
        protected Boolean doInBackground(User... users) {
            Socket client = ServerInfo.getClientSocket();
            try {
                ObjectOutputStream oos = new ObjectOutputStream(client.getOutputStream());
                oos.flush();

                ObjectInputStream ois = new ObjectInputStream(client.getInputStream());

                oos.writeObject(Commands.VALIDATE_LOGIN);

                if (ois.readObject().equals(Boolean.FALSE)) {
                    Log.d("sign in error", (String) ois.readObject());
                    return Boolean.FALSE;
                }

                oos.writeObject(users[0]);
                if (ois.readObject().equals(Boolean.FALSE)) {
                    Log.d("sign in error", (String) ois.readObject());
                    return Boolean.FALSE;
                }
                return Boolean.TRUE;
            } catch (IOException | ClassNotFoundException e) {
                e.printStackTrace();
                return Boolean.FALSE;
            }
        }
    }

    private void toastFailureMsg() {
        Toast.makeText(this, "Can't sign in. Re-check email and password. Try again.", Toast.LENGTH_SHORT).show();
    }

    private void recoverPassword() {
        startActivityForResult(new Intent(this, PasswordRecoveryActivity.class)
                .putExtra(USER_TYPE_KEY, UserType.CUSTOMER), FORGOT_PASS_CODE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REGISTER_CODE) {
            if (resultCode == Activity.RESULT_OK) {
                tvEmail.setText(data.getStringExtra(LoginAsActivity.USER_EMAIL_KEY));
                tvPassword.setText(data.getStringExtra(LoginAsActivity.USER_PASSWORD_KEY));
                Toast.makeText(this, String.format("Now press \"%s\" button.",
                        getResources().getString(R.string.action_sign_in_short)), Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Error occurred", Toast.LENGTH_SHORT).show();
            }
        } else if (requestCode == FORGOT_PASS_CODE) {
            if (resultCode == Activity.RESULT_OK) {
                tvEmail.setText(data.getStringExtra(LoginAsActivity.USER_EMAIL_KEY));
                tvPassword.setText(data.getStringExtra(LoginAsActivity.USER_PASSWORD_KEY));
                Toast.makeText(this, String.format("Now press \"%s\" button.",
                        getResources().getString(R.string.action_sign_in_short)), Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Error occurred", Toast.LENGTH_SHORT).show();
            }
        }
    }
}
