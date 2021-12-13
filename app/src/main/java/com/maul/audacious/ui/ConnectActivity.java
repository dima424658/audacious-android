package com.maul.audacious.ui;

import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.View;

import  android.widget.EditText;
import  android.content.SharedPreferences;

import com.maul.audacious.AudaciousCore;
import com.maul.audacious.R;

public class ConnectActivity extends AppCompatActivity {

    private EditText address;
    private EditText port;
    private EditText username;
    private EditText password;
    private SharedPreferences prefs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_connect);

        DisplayMetrics dm = getBaseContext().getResources().getDisplayMetrics();
        getWindow().setLayout((int)(dm.widthPixels / 1.5), (int)(dm.widthPixels / 2.25));

        address = findViewById(R.id.address);
        port = findViewById(R.id.port);
        username = findViewById(R.id.username);
        password = findViewById(R.id.password);
        prefs = getSharedPreferences("connection", MODE_PRIVATE);

        String a = prefs.getString("hostname", "");
        int f = prefs.getInt("port", 0);

        address.setText(prefs.getString("hostname", ""));
        port.setText(Integer.toString(prefs.getInt("port", 22)));
        username.setText(prefs.getString("username", ""));

        findViewById(R.id.connect).setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                try {
                    AudaciousCore.connect(username.getText().toString(), password.getText().toString(), address.getText().toString(), Integer.parseInt(port.getText().toString()));
                    SharedPreferences.Editor editor = prefs.edit();
                    editor.putString("username", username.getText().toString());
                    editor.putString("password", password.getText().toString());
                    editor.putInt("port", Integer.parseInt(port.getText().toString()));
                    editor.putString("hostname", address.getText().toString());

                    AudaciousCore.connect(username.getText().toString(), password.getText().toString(), address.getText().toString(), Integer.parseInt(port.getText().toString()));

                    if(AudaciousCore.isConnected())
                        editor.commit();

                    finish();
                } catch (Exception e) {
                    Snackbar.make(findViewById(android.R.id.content), e.getMessage(), Snackbar.LENGTH_LONG).show();
                }
            }
        });
    }
}
