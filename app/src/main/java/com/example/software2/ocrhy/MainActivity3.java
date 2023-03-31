package com.example.software2.ocrhy;

import android.annotation.SuppressLint;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.speech.RecognizerIntent;
import android.speech.tts.TextToSpeech;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import net.objecthunter.exp4j.Expression;
import net.objecthunter.exp4j.ExpressionBuilder;

import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Locale;

public class MainActivity3 extends AppCompatActivity {
    private Button button;
    public TextView txtScreen;
    public Button button2;
    public TextToSpeech textToSpeech;
    public TextView txtInput;
    private boolean lastNumeric;

    // Represent that current state is in error or not
    private boolean stateError;
    private final int REQ_CODE_SPEECH_INPUT = 100;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main3);
        setNumericOnClickListener();
        setOperatorOnClickListener();
        txtScreen = findViewById(R.id.txtScreen);
        txtInput = findViewById(R.id.txtInput);

        ImageButton button2 = findViewById(R.id.btnSpeak);
        textToSpeech = new TextToSpeech(getApplicationContext(), new TextToSpeech.OnInitListener() {

            @Override
            public void onInit(int status) {
                if (status != TextToSpeech.ERROR) {
                    textToSpeech.setLanguage(Locale.US);
                    textToSpeech.setSpeechRate(1f);
                    Toast.makeText(MainActivity3.this, "Opening the calculator......  just tap on the screen and say what you want to calculate. And Press the volume up button to return the main menu", Toast.LENGTH_SHORT).show();
                    textToSpeech.speak("Opening the calculator......  just tap on the screen and say what you want to calculate or say what you want ", TextToSpeech.QUEUE_FLUSH, null);

                }
            }

        });
    }


    private void setNumericOnClickListener() {
        // Create a common OnClickListener
        new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                // Just append/set the text of clicked button
                Button button = (Button) v;
                if (stateError) {
                    // If current state is Error, replace the error message
                    txtScreen.setText(button.getText());
                    stateError = false;

                } else {
                    // If not, already there is a valid expression so append to it
                    txtScreen.append(button.getText());
                }

                // Set the flag
                lastNumeric = true;

            }
        };
    }

    private void setOperatorOnClickListener() {
        // Create a common OnClickListener for operators

        new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // If the current state is Error do not append the operator
                // If the last input is number only, append the operator
                if (lastNumeric && !stateError) {
                    Button button = (Button) v;
                    txtScreen.append(button.getText());
                    lastNumeric = false;
                }
            }
        };

        // Clear button
        findViewById(R.id.btnClear).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                txtScreen.setText("");  // Clear the screen
                txtInput.setText("");  // Clear the input
                // Reset all the states and flags
                lastNumeric = false;
                stateError = false;
            }
        });

        findViewById(R.id.btnSpeak).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (stateError) {
                    // If current state is Error, replace the error message
                    txtScreen.setText("Try Again");
                    stateError = false;
                } else {
                    // If not, already there is a valid expression so append to it
                    promptSpeechInput();
                }
                // Set the flag
                lastNumeric = true;

            }
        });
    }

    private void promptSpeechInput() {
        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault());
        intent.putExtra(RecognizerIntent.EXTRA_PROMPT,
                getString(R.string.speech_prompt));
        try {
            startActivityForResult(intent, REQ_CODE_SPEECH_INPUT);
        } catch (ActivityNotFoundException a) {
            Toast.makeText(getApplicationContext(),
                    getString(R.string.speech_not_supported),
                    Toast.LENGTH_SHORT).show();
        }
    }

    private void onEqual() {
        // If the current state is error, nothing to do.
        // If the last input is a number only, solution can be found.
        if (lastNumeric && !stateError) {
            // Read the expression
            final String inputNumber = txtInput.getText().toString();
            txtScreen.setText(inputNumber);
            // Create an Expression (A class from exp4j library)
            Expression expression = null;

            try {
                expression = null;
                try {
                    expression = new ExpressionBuilder(inputNumber).build();
                    double result = expression.evaluate();
                    txtScreen.setText(Double.toString(result).replaceAll("\\.0*$", ""));
                    Toast.makeText(MainActivity3.this, "Answer is", Toast.LENGTH_SHORT).show();
                    textToSpeech.speak("Answer is " + txtScreen.getText().toString(), TextToSpeech.QUEUE_FLUSH, null);
                    textToSpeech.speak("tap on the screen and say what you want", TextToSpeech.QUEUE_ADD,null);
                    textToSpeech.setSpeechRate(1f);

                } catch (Exception e) {
                    txtScreen.setText("Error, tap on the screen and say again");
                    textToSpeech.speak("Error, tap on the screen and say again", TextToSpeech.QUEUE_FLUSH, null);
                    onPause();

                }
            } catch (ArithmeticException ex) {
                // Display an error message
                txtScreen.setText("Error");
                textToSpeech.speak("Error, tap on the screen and say again", TextToSpeech.QUEUE_FLUSH, null);
                stateError = true;
                lastNumeric = true;

            }

        }
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, final Intent data) {
        super.onActivityResult(requestCode, resultCode, data);


        switch (requestCode) {
            case REQ_CODE_SPEECH_INPUT: {
                if (resultCode == RESULT_OK && null != data) {

                    final ArrayList<String> result = data
                            .getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);

                    String change = result.toString();
                    txtInput.setText(result.get(0));
                    if (txtInput.getText().toString().equals("read")) {
                        Intent intent = new Intent(getApplicationContext(), MainActivity2.class);
                        startActivity(intent);
                    }
                    if (txtInput.getText().toString().equals("weather")) {
                        Intent intent = new Intent(getApplicationContext(), MainActivity5.class);
                        startActivity(intent);
                        txtInput.setText(null);
                    } else {
                        textToSpeech.speak( "Do not understand just tap on the screen Say again", TextToSpeech.QUEUE_FLUSH, null);
                    }
                    if (txtInput.getText().toString().equals("time and date")) {
                        Intent intent = new Intent(getApplicationContext(), MainActivity4.class);
                        startActivity(intent);
                    }
                    if (txtInput.getText().toString().equals("location")) {
                        Intent intent = new Intent(getApplicationContext(), MainActivity8.class);
                        startActivity(intent);
                        txtInput.setText(null);
                    }
                    if (txtInput.getText().toString().equals("battery")) {
                        Intent intent = new Intent(getApplicationContext(), MainActivity6.class);
                        startActivity(intent);
                        txtInput.setText(null);
                    }
                    else if(txtInput.getText().toString().equals("exit")) {
                        finishAffinity();
                        super.onPause();
                    }
                    else {
                        textToSpeech.speak("Do not understand tap on the screen Say again", TextToSpeech.QUEUE_FLUSH, null);
                    }



                    // english-lang
                    change = change.replace("x", "*");
                    change = change.replace("X", "*");
                    change = change.replace("add", "+");
                    change = change.replace("sub", "-");
                    change = change.replace("to", "2");
                    change = change.replace(" plus ", "+");
                    change = change.replace("two", "2");
                    change = change.replace(" minus ", "-");
                    change = change.replace(" times ", "*");
                    change = change.replace(" into ", "*");
                    change = change.replace(" in2 ", "*");
                    change = change.replace(" multiply by ", "*");
                    change = change.replace(" divide by ", "/");
                    change = change.replace("divide", "/");
                    change = change.replace("equal", "=");
                    change = change.replace("equals", "=");


                    if (change.contains("=")) {
                        change = change.replace("=", "");
                        txtInput.setText(change);
                        onEqual();

                    } else {
                        txtInput.setText(change);
                        onEqual();
                    }
                }

                break;
            }
        }
    }
    public boolean onTouchEvent(MotionEvent touchEvent) {
        switch (touchEvent.getAction()) {
            case MotionEvent.ACTION_DOWN:
                touchEvent.getX();
                float y1 = touchEvent.getY();
                break;
            case MotionEvent.ACTION_UP:
                float x2 = touchEvent.getX();
                float x1 = touchEvent.getX();
                float y2 = touchEvent.getY();
                if (x1 < x2) {
                    Intent i = new Intent(MainActivity3.this, MainActivity.class);
                    startActivity(i);

                } else {
                    if (x1 > x2) {
                        Intent i = new Intent(MainActivity3.this, MainActivity.class);
                        startActivity(i);
                    }
                }
                break;
       } return false;
    }


    public boolean onKeyDown(int keyCode, @Nullable KeyEvent event) {
        if(keyCode == KeyEvent.KEYCODE_VOLUME_UP){
            textToSpeech.speak("You are in main menu. just swipe right and say what you want", TextToSpeech.QUEUE_FLUSH, null);
            Intent intent = new Intent(getApplicationContext(), MainActivity.class);
            startActivity(intent);
            final Handler handler = new Handler(Looper.getMainLooper());
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    textToSpeech.speak("you are in main menu. just swipe right and say what you want", TextToSpeech.QUEUE_FLUSH, null);

                }
            },1000);

        }
        return true;
    }
    public void onDestroy(){
        if (txtInput.getText().toString().equals("exit")){
            finish();
        }
        super.onDestroy();
    }

    public void onPause () {
        if (textToSpeech != null) {
            textToSpeech.stop();
        }
        super.onPause();
    }

}