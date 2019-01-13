package com.maul.audacious;

import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;

import  android.widget.EditText;
import  android.content.SharedPreferences;

public class ConnectActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_connect);

        findViewById(R.id.connect).setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                try {
                    EditText address = findViewById(R.id.address);
                    EditText password = findViewById(R.id.password);
                    EditText username = findViewById(R.id.username);
                    EditText port = findViewById(R.id.port);

                    AudaciousCore.connect(username.getText().toString(), password.getText().toString(), address.getText().toString(), Integer.parseInt(port.getText().toString()));
                    SharedPreferences prefs = getSharedPreferences("connection", MODE_PRIVATE);
                    SharedPreferences.Editor editor = prefs.edit();
                    editor.putString("username", username.getText().toString());
                    editor.putString("password", password.getText().toString());
                    editor.putInt("port", Integer.parseInt(port.getText().toString()));
                    editor.putString("hostname", address.getText().toString());
                    editor.commit();

                    MainActivity.m_connected = true;
                    finish();
                } catch (Exception e) {
                    Snackbar.make(findViewById(android.R.id.content), e.getMessage(), Snackbar.LENGTH_LONG).show();
                    MainActivity.m_connected = false;
                    return;
                }
            }
        });
    }
}
