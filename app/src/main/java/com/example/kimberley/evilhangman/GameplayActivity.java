package com.example.kimberley.evilhangman;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.text.InputFilter;
import android.text.InputType;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import static android.widget.Toast.makeText;


public class GameplayActivity extends AppCompatActivity {
    private static final int SETTINGS_INFO = 1;
    public static String gameMode = "";
    public static int wordLength = 4;
    public static int numberGuesses = 6;
    public static String currentGameMode;
    protected String enteredName;

    public ArrayList<String> wordList;
    public String guessedLetters = "";
    public String word;
    public String hiddenWord;
    public int mistakes = 0;

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        // Saves the user's current game state when device is turned to portrait/landscape mode
        savedInstanceState.putString("guessedLetters", guessedLetters);
        savedInstanceState.putString("word", word);
        savedInstanceState.putString("hiddenWord", hiddenWord);
        savedInstanceState.putInt("mistakes", mistakes);
        savedInstanceState.putString("currentGameMode", currentGameMode);
        savedInstanceState.putStringArrayList("wordList", wordList);
        super.onSaveInstanceState(savedInstanceState);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gameplay);
        updateSettings();

        // Checks whether there is a saved game when screen is rotated or when the app was simply backgrounded
        if (savedInstanceState != null){
            guessedLetters = savedInstanceState.getString("guessedLetters");
            hiddenWord = savedInstanceState.getString("hiddenWord");
            mistakes = savedInstanceState.getInt("mistakes");
            currentGameMode = savedInstanceState.getString("currentGameMode","evil");
            if (currentGameMode.equals("good")) {
                word = savedInstanceState.getString("word");
            }
            else if (currentGameMode.equals("evil")) {
                wordList = savedInstanceState.getStringArrayList("wordList");
            }
            updateDisplay();
        }
        // If not, then create a new game
        else{
            // Sets the gameMode to "static" while playing a game.
            // So when settings are changed, they won't influence the current game
            // But will only be updated when the user starts a new one.
            currentGameMode = gameMode;
            // Loads the words from the XML file and creates a list based on the word length
            String[] allWords = getResources().getStringArray(R.array.words);
            wordList = createList(allWords, wordLength);
            // If gameMode is good: pick a random word from the wordList and dash it.
            if (currentGameMode.equals("good") && word == null) {
                Toast.makeText(getApplicationContext(),"Good mode is on!", Toast.LENGTH_SHORT).show();
                setHiddenWord(chooseRandomWord(wordList));
            }
            // If evil: set the amount of dashes based on the wordLength
            else {
                Toast.makeText(getApplicationContext(), "Evil mode is on!", Toast.LENGTH_SHORT).show();
                setDashWord(wordLength);
            }
        }
        // Sets filters for input (max 1 character and all caps)
        EditText letterInput = (EditText)findViewById(R.id.letterInput);
        letterInput.setFilters(new InputFilter[]{new InputFilter.AllCaps(), new InputFilter.LengthFilter(1)});

        // Sets listener for enter key on keyboard. Comes in handy in landscape mode.
        letterInput.setOnKeyListener(new View.OnKeyListener() {
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if (event.getAction() == KeyEvent.ACTION_DOWN) {
                    switch (keyCode) {
                        case KeyEvent.KEYCODE_DPAD_CENTER:
                        case KeyEvent.KEYCODE_ENTER:
                            checkInput(null);
                            return true;
                        default:
                            break;
                    }
                }
                return false;
            }
        });
    }

    // Creates a list based on word length settings
    public ArrayList<String> createList(String[] allWords, int wordLength){
        ArrayList<String> wordList = new ArrayList<>();
        for (int i = 1; i < allWords.length ; i++) {
            if (allWords[i].length() == wordLength){
                wordList.add(allWords[i]);
            }
        }
        return wordList;
    }

    // Good: Generates a random word from the wordList
    public String chooseRandomWord(List<String> array){
        //Creates a random word
        Random r = new Random();
        word = array.get(r.nextInt(array.size()));
        return word;
    }

    // Good: Sets and displays the hint for the word
    public String setHiddenWord(String word){
        char hiddenWordChars[] = new char[word.length()];
        for (int i = 0; i < word.length(); i++) {
            hiddenWordChars[i] = '_';
        }
        hiddenWord = new String(hiddenWordChars);
        updateDisplay();
        return word;
    }

    // Evil: Sets and displays the hint for the word, only based on the wordLength in settings
    public String setDashWord(int wordLength){
        char hiddenWordChars[] = new char[wordLength];
        for (int i = 0; i < wordLength; i++){
            hiddenWordChars[i] = '_';
        }
        hiddenWord = new String(hiddenWordChars);
        updateDisplay();
        return hiddenWord;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_gameplay, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_newgame:
                Intent openNewGame = getIntent();
                finish();
                startActivity(openNewGame);
                return true;
            // Opens High scores
            case R.id.action_highscores:
                Intent openHighscores = new Intent(getApplicationContext(), HistoryViewActivity.class);
                openHighscores.putExtra("enterName", "");
                openHighscores.putExtra("score","");
                this.startActivity(openHighscores);
                break;
            //Opens Settings
            case R.id.action_settings:
                Intent openSettings = new Intent(getApplicationContext(), SettingsActivity.class);
                startActivityForResult(openSettings,SETTINGS_INFO);
                break;
            //Exits the game
            case R.id.action_exit:
                finish();
                break;
            default:
                return super.onOptionsItemSelected(item);
        }
        return true;
    }

    // Updates the settings for a game. Default settings are included. Also set in preferences.xml
    private void updateSettings() {
        SharedPreferences settingsPrefs = PreferenceManager.getDefaultSharedPreferences(this);

        String wordLengthString = settingsPrefs.getString("pref_word_length", "4");
        GameplayActivity.wordLength = Integer.parseInt(wordLengthString);

        String numberGuessesString = settingsPrefs.getString("pref_number_guesses", "6");
        GameplayActivity.numberGuesses = Integer.parseInt(numberGuessesString);

        if (settingsPrefs.getBoolean("pref_evil", true)) {
            gameMode = "evil";
        } else {
            gameMode = "good";
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == SETTINGS_INFO){
            updateSettings();
        }
    }

    private void updateDisplay(){
        // Updates the word hint display
        TextView displayWord = (TextView)findViewById(R.id.displayWord);
        displayWord.setText(hiddenWord);

        // Displays the guessed letters
        TextView guessLetters = (TextView) findViewById(R.id.guessedLetters);
        guessLetters.setText("Guessed Letters: " + guessedLetters);

        // Displays the amount of mistakes
        TextView error = (TextView) findViewById(R.id.errorMessage);
        error.setText("Guesses left: " + mistakes + "/ " + numberGuesses);

        // Changes hangman resource path based on mistakes and amount of guesses
        ImageView hangmanImage = (ImageView)findViewById(R.id.hangmanImage);
        double hman = (6/(float)numberGuesses)*mistakes;
        int h = getResources().getIdentifier("hangman" + ((int) hman), "drawable", getPackageName());
        hangmanImage.setImageResource(h);

        // Checks if player won / lost
        checkGameState();
    }

    public void checkInput(View view) {
        // Gets the letter filled in by a user
        EditText letterInput = (EditText) findViewById(R.id.letterInput);
        String letter = letterInput.getText().toString();

        // Clears the input field
        letterInput.setText("");

        // Validates if input is filled in and not a number
        if (letter.length() != 1 || !Character.isLetter(letter.charAt(0))) {
            makeText(getApplicationContext(), "Please fill in a valid character",
                    Toast.LENGTH_SHORT).show();
        }
        // Checks if letter is already guessed
        else if (guessedLetters != null && guessedLetters.contains(letter)) {
                makeText(getApplicationContext(), "You've already used this character",
                        Toast.LENGTH_SHORT).show();
        }
        // Otherwise it is a valid input
        else {
            guessedLetters += letter + " ";
            // If gameMode is good, check if the word contains the letter
            if (currentGameMode.equals("good")) {
                matchCheck(letter);
            }
            else {
                // If gameMode is evil, dash all the words (except for the input letter) in wordList
                ArrayList<String> evilList = new ArrayList<>();
                for (int i = 0; i <wordList.size(); i++){
                    // Add them to the evilList
                    evilList.add(dashWordsInList(wordList.get(i), hiddenWord,letter));
                }
                String dashedWordLongestList = setDashedWordFromLongestList(evilList);
                // Creates a new word list with the remaining words which are equal to the most frequent dashed word
                ArrayList<String> newEvilList = new ArrayList<>();
                for (int i = 0; i <wordList.size(); i++) {
                    if (dashWordsInList(wordList.get(i), hiddenWord, letter).equals(dashedWordLongestList)) {
                        newEvilList.add(wordList.get(i));
                    }
                }
                // Updates the word list
                setWordList(newEvilList);

                if (!hiddenWord.equals(dashedWordLongestList)){
                    hiddenWord = dashedWordLongestList;
                }
                else{
                    mistakes++;
                }
            }
            // Updates display calls checkGameState to see if user has won / lost
            updateDisplay();
        }
    }

    // Good: Looks for a match between the word and the input letter
    private void matchCheck(String letter){
        StringBuilder hiddenWordBuilder = new StringBuilder(hiddenWord);
        boolean match = false;
        for (int i = 0; i < word.length(); i++) { // Loops over word characters
            char c = word.charAt(i);
            if(c == letter.charAt(0)) { // If there is a match
                match = true;
                hiddenWordBuilder.setCharAt(i, c);
                hiddenWord = hiddenWordBuilder.toString();
            }
        }
        if(!match) { // There was no match so add a mistake
            mistakes++;
        }
    }

    // Sets/Updates the word list
    public void setWordList(ArrayList<String> newWordList){
        wordList = newWordList;
    }

    // Evil: Looks for the most frequent dashed word in the list
    public String setDashedWordFromLongestList(List<String> array){
        String word = "";
        int count = 0;
        // Compare all kind of dashed words in the list (eg. _B__ and __B_)
        for (int i = 0; i<array.size(); i++){
            String tempWord = array.get(i);
            int tempCount = 0;
            for (int j = 0; j<array.size(); j++){
                if(array.get(j).equals(tempWord))
                    tempCount++;
                if (tempCount>count){
                    word = tempWord;
                    count = tempCount;
                }
            }
        }
        // Returns the kind of dashed word which occurs the most
        // So the remaining list will be the longest
        return word;
    }

    // Evil: Dashes all the words in the list, except for the letter which has been filled in.
    public String dashWordsInList(String word, String hiddenWord, String letter){
        String dashedWord = "";
        for (int i = 0; i < word.length(); i++) {
            char c = word.charAt(i);
            if (c == letter.charAt(0)) {
                dashedWord = dashedWord + word.charAt(i);
            } else if (!hiddenWord.equals("_")) {   // if the hiddenWord already has a letter filled in
                dashedWord = dashedWord + hiddenWord.charAt(i);
            } else {
                dashedWord = dashedWord + "_";
            }
        }
        return dashedWord;
    }

    // Checks if the user is out of guesses or if the word is fully guessed
    private void checkGameState(){
        if (mistakes >= numberGuesses){
            youLost();
        }
        else if (!hiddenWord.contains("_")){
            youWin(word, mistakes);
        }
    }

    private void youWin(final String word, final int mistakes){
        endGame();

        // Defines the imageview
        ImageView hangmanImage = (ImageView) findViewById(R.id.hangmanImage);

        // Shows congratulated alertdialog and image in the background
        hangmanImage.setImageResource(R.drawable.happysmiley);
        AlertDialog.Builder winGame = new AlertDialog.Builder(this);
        winGame.setTitle("You win!");
        winGame.setMessage("Congrats, you've won the game! Please enter your name to commit your high score");
        // Puts an edittext to the alert dialog
        final EditText enterName = new EditText(this);
        enterName.setHint("Name");
        enterName.setInputType(InputType.TYPE_CLASS_TEXT);
        winGame.setView(enterName);

        winGame.setPositiveButton("Submit name", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                enteredName = enterName.getText().toString();
                if (enterName.length() != 0) {
                    Intent openHighScores = new Intent(getApplicationContext(), HistoryViewActivity.class);
                    openHighScores.putExtra("enteredName", enteredName);
                    openHighScores.putExtra("mistakes", mistakes);
                    if (currentGameMode.equals("evil")) openHighScores.putExtra("word", hiddenWord);
                    else if (currentGameMode.equals("good")) openHighScores.putExtra("word", word);

                    enterName.setText("");
                    startActivity(openHighScores);
                }
            }
        });
        AlertDialog wonAlert = winGame.create();
        wonAlert.show();
    }

    // Congratulates or consoles the player at the end of the game
    private void youLost(){
        endGame();
        // Defines the imageview
        ImageView hangmanImage = (ImageView) findViewById(R.id.hangmanImage);
        // Shows consoled alertdialog and image in the background
        AlertDialog.Builder loseGame = new AlertDialog.Builder(this);
        loseGame.setTitle("You lose!");
        // If gameMode is good, show the word and the sad smiley
        if (currentGameMode.equals("good")) {
            loseGame.setMessage("Too bad, you've lost. The correct word was: " + word);
            hangmanImage.setImageResource(R.drawable.sadsmiley);
        }
        // If evil, then only show the evil smiley
        else {
            loseGame.setMessage("Too bad, you've lost. The evil man has defeated you!");
            hangmanImage.setImageResource(R.drawable.evilsmiley);
            }
        loseGame.setPositiveButton("New Game", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // Starts a new game
                Intent openNewGame = getIntent();
                finish();
                startActivity(openNewGame);
            }
        });
        loseGame.setNegativeButton("Exit", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // Exits the game
                finish();
            }
        });
        AlertDialog commitLoss = loseGame.create();
        commitLoss.show();
    }

    private void endGame(){
        // Makes the keyboard, letterInput en guess button disappear
        hide_keyboard(this);
        EditText letterInput = (EditText) findViewById(R.id.letterInput);
        letterInput.clearFocus();
        letterInput.setVisibility(View.GONE);
        Button guessBtn = (Button) findViewById(R.id.guessBtn);
        guessBtn.setVisibility(View.GONE);
    }

    // Hides the keyboard
    private static void hide_keyboard(Activity activity) {
        InputMethodManager inputMethodManager = (InputMethodManager) activity.getSystemService(Activity.INPUT_METHOD_SERVICE);
        View view = activity.getCurrentFocus();
        if(view == null) {
            view = new View(activity);
        }
        inputMethodManager.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }
}