package com.example.stemcomp2019;

import android.content.Context;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import java.io.FileOutputStream;
import java.io.ObjectOutputStream;

public class AddTagActivity extends AppCompatActivity implements BTActivity {
    public static final String TAG = "AddTagActivity";

    private TextView countText;
    private TextView serialText;
    private TextView dirText;
    private EditText labelText;

    private int tagsAdded = 0;
    private Tag tag = null;
    private String serial;
    private int dir;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_tag);
        countText = (TextView) findViewById(R.id.tagCount);
        serialText = (TextView) findViewById(R.id.tagSerial);
        dirText = (TextView) findViewById(R.id.tagDir);
        labelText = (EditText) findViewById(R.id.labelText);
        BTHandler handler = new BTHandler(AddTagActivity.this);
        BluetoothService bt = BluetoothService.btService;
        bt.setHandler(handler);

        serial = "";
        dir = -1;
    }

    public void setNorth(View v) {
        dirText.setText("Direction: north");
        dir = 1;
    }

    public void setSouth(View v) {
        dirText.setText("Direction: south");
        dir = 3;
    }

    public void setEast(View v) {
        dirText.setText("Direction: east");
        dir = 0;
    }

    public void setWest(View v) {
        dirText.setText("Direction: west");
        dir = 2;
    }

    public void addTag0(View v) {
        if (serial.length() > 0 && dir != -1) {
            String label = labelText.getText().toString();
            tag = Tag.addTag(serial, label, dir, tag);
            saveTags();
            tagsAdded++;
            countText.setText("Tags added: " + tagsAdded);
        }
    }

    public void startOver(View v) {
        tagsAdded = 0;
        countText.setText("Tags added: 0");
        tag = null;
    }

    public void goBack(View v) {
        startMain();
    }

    public void startMain() {
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
    }

    public void btMessage(String message) {
        Log.d(TAG, "Bluetooth message: " + message);
        serialText.setText("Serial: " + message);
        serial = message;
    }

    public void btStatus(String status) {
        Log.d(TAG, "Bluetooth status: " + status);
    }

    public void saveTags() {
        String filename = "tags.txt";
        FileOutputStream fos;
        ObjectOutputStream out;
        try {
            fos = openFileOutput(filename, Context.MODE_PRIVATE);
            out = new ObjectOutputStream(fos);
            out.writeObject(Tag.getAllTags());
            out.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
