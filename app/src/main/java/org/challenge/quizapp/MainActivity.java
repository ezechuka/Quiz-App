package org.challenge.quizapp;

import android.content.DialogInterface;
import android.content.res.AssetManager;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.security.SecureRandom;

public class MainActivity extends AppCompatActivity {

    private static final String[] GOOD_COMMENTS = {"AWESOME", "DIVINE", "SUPERB", "GODLIKE", "BOSS",
        "SKILLFUL", "EXCELLENT", "NICE", "TALENTED", "KNOWLEDGE-SEEKER", "BRAVO", "PERFECT", "AMAZING"};
    private static final int FLAGS_IN_QUIZ = 10;
    private static String TAG = "Quiz App";
    private static int questionNumber = 0;
    private static int numOfCorrectAnswers;
    private boolean isEnabled = false;
    private int nextQuestionNumber = 1;
    private String assetFilename;
    private String correctAnswer;
    private TextView questionNumberTextView;
    private ImageView flagImageView;
    List<String> fileNameList;
    private TextView answerTextView;
    List<String> quizCountriesList;
    private SecureRandom random;
    private Handler handler;
    private LinearLayout[] optionsLinearLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        quizCountriesList = new ArrayList<>();
        fileNameList = new ArrayList<>();
        questionNumberTextView = (TextView) findViewById(R.id.questionNumberTextView);
        flagImageView = (ImageView) findViewById(R.id.flagImageView);
        random = new SecureRandom();
        handler = new Handler();
        answerTextView = (TextView) findViewById(R.id.answerTextView);
        
        optionsLinearLayout = new LinearLayout[2];
        optionsLinearLayout[0] = (LinearLayout) findViewById(R.id.optionsLinearLayout1);
        optionsLinearLayout[1] = (LinearLayout) findViewById(R.id.optionsLinearLayout2);

        for (int row = 0; row < 2; row++) {
            for (int column = 0; column < 2; column++) {
                Button button = (Button) optionsLinearLayout[row].getChildAt(column);
                button.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Button guessButton = (Button) view;
                        String guess = guessButton.getText().toString();
                        String correct = getCountryName(correctAnswer);

                        if (guess.equals(correct)) {
                            ++numOfCorrectAnswers;
                            answerTextView.setText(getCountryName(correctAnswer));
                            answerTextView.setTextColor(Color.rgb(0, 255, 0));

                            int randomIndex = random.nextInt(GOOD_COMMENTS.length - 1);
                            String comment = GOOD_COMMENTS[randomIndex];
                            Toast.makeText(MainActivity.this, comment, Toast.LENGTH_SHORT).show();
                            disableButtons(isEnabled);
                            pause();

                        } else {
                            answerTextView.setText("Incorrect!");
                            answerTextView.setTextColor(Color.rgb(255, 0, 0));
                            Toast.makeText(MainActivity.this, correct, Toast.LENGTH_SHORT).show();
                            disableButtons(isEnabled);
                            pause();
                        }

                        if (questionNumber == FLAGS_IN_QUIZ) {
                            //Log.e(TAG, ""+questionNumber);
                            AlertDialog.Builder resultDialog = new AlertDialog.Builder(MainActivity.this, R.style.AlertBackground);
                            resultDialog.setTitle(getString(R.string.quiz_results));
                            resultDialog.setMessage("Total Questions: " + FLAGS_IN_QUIZ + "\n" + "Answered Correctly: " + numOfCorrectAnswers
                                    + "\n" + "Failed Questions: " + (FLAGS_IN_QUIZ - numOfCorrectAnswers));
                            resultDialog.setPositiveButton(getString(R.string.restart_quiz), new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    restartQuiz();
                                }
                            });
                            resultDialog.setCancelable(false);
                            resultDialog.show();
                        }
                    }
                });
            }
        }

        restartQuiz();
    }

    private void disableButtons(boolean enable) {
        for (int row = 0; row < 2; row++) {
            for (int column = 0; column < 2; column++) {
                Button button = (Button) optionsLinearLayout[row].getChildAt(column);
                button.setEnabled(enable);
            }
        }
    }

    private void pause() {
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                showNextFlag();
                disableButtons(!isEnabled);
                nextQuestionNumber += 1;
            }
        }, 1500);
    }

    public void restartQuiz() {
        assetFilename = "flagImages";
        AssetManager assets = this.getAssets();
        fileNameList.clear();

        try {
            String[] paths = assets.list(assetFilename);
            Log.e(TAG, ""+paths);
            for (String path: paths) {
                fileNameList.add(path.replace(".png", ""));
            }
        } catch (IOException ioException) {
            Log.e(TAG, "Error loading file names", ioException);
        }
        
        numOfCorrectAnswers = 0;
        questionNumber = 0;
        quizCountriesList.clear();
        
        int flagCounter = 1;
        int numberOfFlags = fileNameList.size();
        Log.e(TAG, ""+numberOfFlags);
        
        while(flagCounter <= FLAGS_IN_QUIZ) {
            int randomIndex = random.nextInt(numberOfFlags);
            String filename = fileNameList.get(randomIndex);
            
            if (!quizCountriesList.contains(filename)) {
                quizCountriesList.add(filename);
                ++flagCounter;
            }
        }

       // questionNumberTextView.setText(getString(R.string.question_number, questionNumber, FLAGS_IN_QUIZ));
        showNextFlag();
    }

    private void showNextFlag() {
        String nextImage = null;
        answerTextView.setText("");
        if (questionNumber == FLAGS_IN_QUIZ)
            return;

        if (quizCountriesList.size() >= 1) {
            nextImage = quizCountriesList.remove(0);
            correctAnswer = nextImage;
        }

        questionNumberTextView.setText(getString(R.string.question_number, ++questionNumber, FLAGS_IN_QUIZ));

        AssetManager asset = this.getAssets();
        try (InputStream stream = asset.open(assetFilename + "/" + nextImage + ".png")) {
            Drawable flag = Drawable.createFromStream(stream, nextImage);
            flagImageView.setImageDrawable(flag);
        } catch (IOException ioException) {
            Log.e(TAG, "Error loading image files", ioException);
        }

        Collections.shuffle(fileNameList);
        int correct = fileNameList.indexOf(correctAnswer);
        fileNameList.add(fileNameList.remove(correct));

        for (int row = 0; row < 2; row++) {
            for (int column = 0; column < 2; column++) {
                Button optionButton = (Button) optionsLinearLayout[row].getChildAt(column);
                String filename = fileNameList.get(random.nextInt(fileNameList.size()));
                optionButton.setText(getCountryName(filename));
            }
        }

        int row = random.nextInt(2);
        int column = random.nextInt(2);
        Button correctButton = (Button) optionsLinearLayout[row].getChildAt(column);
        correctButton.setText(getCountryName(correctAnswer));
    }

    private String getCountryName(String name) {
        return name.replace("_", " ");
    }
}