package com.example.kimberley.evilhangman;


import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;

public class HistoryViewActivity extends AppCompatActivity{
    private ArrayList<String> listScores;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.highscores_layout);

        // Gets the entered name and score from the alertdialog at the end of the game
        Intent getHighScore = getIntent();
        String name = getHighScore.getExtras().getString("enteredName");
        int mistakes = getHighScore.getExtras().getInt("mistakes");
        String word = getHighScore.getExtras().getString("word");

        String item = name + " - Mistakes: " +mistakes + " - Word: " + word ;
        ListView scoreList = (ListView) findViewById(R.id.scoreList);
        listScores = new ArrayList<>();
        // Searches for names and scores in the saved file
        readScores();

        ArrayAdapter<String> listAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, listScores);
        scoreList.setAdapter(listAdapter);
        int listLength = scoreList.getAdapter().getCount();

        Boolean addedScore = false;

        // If user opens high scores via menu, just show the high scores
        if (name == null) readScores();
        // If high scores is opened via the end of the game
        else {
            if (listLength != 0){
                int i = 0;
                while ((i <listLength) && (!addedScore)){
                    // Splits the items, first on ":" second on "-" and last one on ","
                    String current = scoreList.getItemAtPosition(i).toString();
                    String[] splitFirst = current.split(": ");
                    String splitFirstString = Arrays.toString(splitFirst);
                    String[] splitSecond = splitFirstString.split(" - ");
                    String splitFinal = Arrays.toString(splitSecond);
                    String[] splitNameMistakesWord = splitFinal.split(", ");

                    // If the amount of mistakes lower than the one in the list, add at this place
                    if ((mistakes < Integer.parseInt(splitNameMistakesWord[2])) && (item.length() != 0)){
                        listScores.add(i,item);
                        item = "";
                        addedScore = true;
                    }// If the amount of mistakes higher than last on the list, add to the bottom
                    else if ((i == listLength - 1) && item.length() != 0 && mistakes > Integer.parseInt(splitNameMistakesWord[2])){
                        listScores.add(item);
                        item = "";
                        addedScore = Boolean.TRUE;
                    }
                    i++;
                }
            // If it is the first item on the list, just add it
            }else{
                listAdapter.add(item);
            }
            // Add the item to the list/file
            writeScores();}
    }


    private void writeScores() {
        // Opens file directory
        File file = getFilesDir();
        // Creates scores.txt
        File scoreFile = new File(file, "scores.txt");
        // Tries to add the items to the file
        try {
            FileUtils.writeLines(scoreFile, listScores);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void readScores(){
        // Opens the file directory and file
        File filesDir = getFilesDir();
        File scoreFile = new File(filesDir, "scores.txt");
        // Tries to find items to add to the file to show
        try {
            listScores = new ArrayList<>(FileUtils.readLines(scoreFile));
        } catch (IOException e) {
            listScores = new ArrayList<>();
        }
    }



}
