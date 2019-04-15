package com.example.stemcomp2019;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.os.Handler;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.speech.tts.TextToSpeech;
import android.support.constraint.ConstraintLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.TextView;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.ArrayList;
import java.util.Locale;
import java.util.Set;

public class MainActivity extends AppCompatActivity implements BTActivity {
    public static final String TAG = "MainActivity";
    private TextView tv;
    private TextView tv2;
    private SpeechRecognizer sr;
    private TextToSpeech tts;

    private String lastSerial = "";
    private Tag confirm = null;
    private Tag dest = null;
    private PathFinder pf = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        loadTags();
        View view = findViewById(R.id.main);
        tv = (TextView)findViewById(R.id.textView);
        tv2 = (TextView)findViewById(R.id.textView2);
        sr = SpeechRecognizer.createSpeechRecognizer(this);
        sr.setRecognitionListener(new listener());

        tts = new TextToSpeech(getApplicationContext(), new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
                if(status != TextToSpeech.ERROR) {
                    tts.setLanguage(Locale.US);
                }
            }
        });

        if (BluetoothService.btService == null) {
            BluetoothAdapter adapter = BluetoothAdapter.getDefaultAdapter();
            Set<BluetoothDevice> pairedDevices = adapter.getBondedDevices();
            if (pairedDevices.size() > 0) {
                for (BluetoothDevice device : pairedDevices) {
                    if (device.getName().equals("HC-05")) {
                        BTHandler handler = new BTHandler(MainActivity.this);
                        BluetoothService bt = new BluetoothService(handler, device);
                        bt.connect();
                    }
                }
            }
        }
        else {
            BTHandler handler = new BTHandler(MainActivity.this);
            BluetoothService bt = BluetoothService.btService;
            bt.setHandler(handler);
        }

        view.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent event) {
                TextView tv1 = (TextView)findViewById(R.id.textView);
                if (event.getAction() == android.view.MotionEvent.ACTION_DOWN) {
                    tv1.setText("Speak now");

                    Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
                    intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
                    intent.putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE,"voice.recognition.test");

                    intent.putExtra(RecognizerIntent.EXTRA_MAX_RESULTS,5);
                    sr.startListening(intent);
                    Log.i("111111","11111111");
                }
                return true;
            }
        });
    }

    class listener implements RecognitionListener {
        public void onReadyForSpeech(Bundle params) {

        }

        public void onBeginningOfSpeech() {
            Log.d(TAG, "onBeginningOfSpeech");
        }

        public void onRmsChanged(float rmsdB) {
            Log.d(TAG, "onRmsChanged");
        }

        public void onBufferReceived(byte[] buffer) {
            Log.d(TAG, "onBufferReceived");
        }

        public void onEndOfSpeech() {
            Log.d(TAG, "onEndofSpeech");
        }

        public void onError(int error) {
            Log.d(TAG,  "error " +  error);
        }

        public void onResults(Bundle results) {
            String str = "";
            Log.d(TAG, "onResults " + results);
            ArrayList data = results.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
            for (int i = 0; i < data.size(); i++) {
                Log.d(TAG, "result " + data.get(i));
                str += data.get(i);
            }
            tv.setText("Tap to speak");

            String result = String.valueOf(data.get(0)).toLowerCase();
            if (result.equals("add tag"))
                startAddTag();
            else if (result.length() >= 5 && result.substring(0, 5).equals("find ")) {
                String query = result.substring(5);
                ArrayList<Tag> tags = Tag.getAllTags();
                double[] confidences = Tag.getConfidences(query, tags);
                if (confidences.length > 0) {
                    int index = indexOfMax(confidences);
                    double confidence = confidences[index];
                    if (confidence == 1) {
                        confirm = null;
                        dest = tags.get(index);
                        tts.speak("Getting directions to " + dest.label + ". Scan a tag to get started.", TextToSpeech.QUEUE_FLUSH, null);
                    }
                    else {
                        confirm = tags.get(index);
                        tts.speak("Did you mean: " + confirm.label, TextToSpeech.QUEUE_FLUSH, null);
                    }
                }
            }
            else if (confirm != null) {
                if (result.equals("yes")) {
                    dest = confirm;
                    tts.speak("Getting directions to " + dest.label + ". Scan a tag to get started.", TextToSpeech.QUEUE_FLUSH, null);
                }
                confirm = null;
            }
            else if (dest != null && result.equals("cancel")) {
                dest = null;
                pf = null;
            }
        }

        public int indexOfMax(double[] arr) {
            int index = 0;
            for (int i = 0; i < arr.length; i++) {
                if (arr[i] > arr[index])
                    index = i;
            }
            return index;
        }

        public void onPartialResults(Bundle partialResults) {
            Log.d(TAG, "onPartialResults");
        }

        public void onEvent(int eventType, Bundle params) {
            Log.d(TAG, "onEvent " + eventType);
        }
    }

    public void startAddTag() {
        Intent intent = new Intent(this, AddTagActivity.class);
        startActivity(intent);
    }

    public void btMessage(String message) {
        Log.d(TAG, "Bluetooth message: " + message);
        Tag current = Tag.findTag(message);
        if (!message.equals(lastSerial) && current != null) {
            if (dest != null) {
                if (pf == null) {
                    if (current != dest) {
                        pf = new PathFinder(current, dest);
                        tts.speak("Scan another tag for more assistance.", TextToSpeech.QUEUE_FLUSH, null);
                    }
                    else {
                        tts.speak("You have arrived at " + dest.label + ".", TextToSpeech.QUEUE_FLUSH, null);
                        dest = null;
                    }
                }
                else {
                    String hint = pf.update(current);
                    if (!hint.equals(""))
                        tts.speak(hint, TextToSpeech.QUEUE_FLUSH, null);
                    if (current == dest) {
                        dest = null;
                        pf = null;
                    }
                }
            }
            else tts.speak(current.label, TextToSpeech.QUEUE_FLUSH, null);
        }
        lastSerial = message;
    }

    public void btStatus(String status) {
        Log.d(TAG, "Bluetooth status: " + status);
        tv2.setText(status);
    }

    public void loadTags() {
        String filename = "tags.txt";
        FileInputStream fis;
        ObjectInputStream in;
        try {
            fis = openFileInput(filename);
            in = new ObjectInputStream(fis);
            ArrayList<Tag> tags = (ArrayList<Tag>) in.readObject();
            Log.d(TAG, "Total tags: " + tags.size());
            Tag.setTags(tags);
            in.close();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
}
