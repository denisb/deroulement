package edu.bdenis.deroulement;

import android.app.AlertDialog;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.InputType;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Scanner;

public class MainActivity extends AppCompatActivity {
    private TextView mTextMessage;
    private TextView timerTextView;
    private TextView titreTextView;
    private String heureDebut="9h45";
    private String heureFin="11h15";
    private String titreText;
    private TextView soustitreTextView;
    private String soustitreText;
    private TextView contenuTextView;
    private String contenuText;
    private static final List<String> listStrings = new ArrayList<String>();
    private int indexStrings=0;
    private int indexContenu=1;
    private int indexSousTitre=1;
    private int nbContenus=2;
    private int nbSousTitres =2;
    private int charSousTitre=42;
    private int charContenu=45;
    private int decalageTps = 0;
    private int decalageTpsInit = 0;
    long startTime = 0;
    private float x1,x2;
    static final int MIN_DISTANCE = 10;
    Handler timerHandler = new Handler();
    Runnable timerRunnable = new Runnable() {
        @Override
        public void run() {
            long millis = System.currentTimeMillis() - startTime;
            int seconds = (int) (millis / 1000);
            int minutes = seconds / 60; //minutes passées depuis l'heure de début
            int h = 0; int m = 0;
            Calendar calendar = Calendar.getInstance();
            h = calendar.getTime().getHours();
            m = calendar.getTime().getMinutes();
            int rm = (90*60-seconds) / 60; //minutes restantes jusqu'à l'heure de fin
            Button boutonHeureDebut=(Button)findViewById(R.id.buttonHeureDebut);
            boutonHeureDebut.setX(20);
            boutonHeureDebut.setY(timerTextView.getY());
            boutonHeureDebut.setText(heureDebut+"+("+minutes+")");
            timerTextView.setText(String.format("%dh%d (%d*%dmin)",h,m,nbContenus-indexContenu,rm/(nbContenus-indexContenu)));
            timerTextView.setX(boutonHeureDebut.getX()+boutonHeureDebut.getWidth());
            Button boutonHeureFin=(Button)findViewById(R.id.buttonHeureFin);
            boutonHeureFin.setX(timerTextView.getX()+timerTextView.getWidth());
            boutonHeureFin.setY(timerTextView.getY());
            boutonHeureFin.setText(heureFin+("-("+rm+")"));
            titreTextView.setText(titreText);
            titreTextView.setX(220-Math.round((200*(millis+decalageTps))/(90*60*1000)));
            indexContenu = -1;
            indexSousTitre = -1;
            for(int k = 0; k < listStrings.size(); k++) {
                String se =  listStrings.get(k);
                if (se.charAt(0)==charContenu) {
                    indexContenu++;
                    if (indexContenu==Math.round(((millis+decalageTps)*nbContenus)/(90*60*1000))) {
                        contenuText = se;
                        soustitreTextView.setText(soustitreText);
                        soustitreTextView.setX(220-Math.round((200*nbSousTitres)*(millis+decalageTps-((indexSousTitre*90*60*1000)/nbSousTitres))/(90*60*1000) ));
                        contenuTextView.setText(contenuText+" <<"+listStrings.get(k+1));
                        contenuTextView.setX(220-Math.round((200*nbContenus)*(millis+decalageTps-((indexContenu*90*60*1000)/nbContenus))/(90*60*1000) ));
                        break;}}
                if (se.charAt(0)==charSousTitre) {
                    indexSousTitre++;
                    soustitreText = se;}}
            decalageTps = (95 * decalageTps) / 100;
            timerHandler.postDelayed(this, 50);}};

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch(event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                x1 = event.getX();
                decalageTpsInit = decalageTps;
                break;
            case MotionEvent.ACTION_MOVE:
                x2 = event.getX();
                float deltaX = x2 - x1;
                if (Math.abs(deltaX) > MIN_DISTANCE) {
                    decalageTps=decalageTpsInit-(5000*Math.round(deltaX/MIN_DISTANCE));}
                break;}
        return super.onTouchEvent(event);}

    private BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener
            = new BottomNavigationView.OnNavigationItemSelectedListener() {
        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
            switch (item.getItemId()) {
                case R.id.navigation_home:
                    mTextMessage.setText(R.string.title_home);
                    return true;
                case R.id.navigation_main:
                    mTextMessage.setText(R.string.title_main);
                    return true;
                case R.id.navigation_preferences:
                    mTextMessage.setText(R.string.title_preferences);
                    Intent intent = new Intent().setType("text/plain").setAction(Intent.ACTION_GET_CONTENT);
                    startActivityForResult(Intent.createChooser(intent, "Select a file"), 12345);
                    return true;}
            return false;}};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mTextMessage = (TextView) findViewById(R.id.message);
        timerTextView = (TextView) findViewById(R.id.heure);
        titreTextView = (TextView) findViewById(R.id.titre);
        titreText = getString(R.string.text_titre);
        soustitreTextView = (TextView) findViewById(R.id.soustitre);
        soustitreText = getString(R.string.text_soustitre);
        contenuTextView = (TextView) findViewById(R.id.contenu);
        contenuText = getString(R.string.text_contenu);
        SharedPreferences sharedPref = getPreferences(Context.MODE_PRIVATE); //getActivity().
        heureDebut = sharedPref.getString("saved_heure_debut", "8h00");
        heureFin = sharedPref.getString("saved_heure_fin", "9h30");
        String strUriPlanning = sharedPref.getString("saved_uri_selected", "/");
        Uri tmpSelectedfile = Uri.parse(strUriPlanning);
        chargePlanning(tmpSelectedfile);
        BottomNavigationView navigation = (BottomNavigationView) findViewById(R.id.navigation);
        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        Button boutonHeureDebut=(Button)findViewById(R.id.buttonHeureDebut);
        boutonHeureDebut.setOnClickListener(new View.OnClickListener(){
            public void onClick(View view){
                if (view.getId()==R.id.buttonHeureDebut){
                    final EditText inputDialog = new EditText(MainActivity.this);
                    new AlertDialog.Builder(MainActivity.this)
                            .setTitle("Changer l'heure")
                            .setMessage("Heure de début ?")
                            .setView(inputDialog)
                            .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int whichButton) {
                                    heureDebut = inputDialog.getText().toString();
                                    SharedPreferences sharedPref = getPreferences(Context.MODE_PRIVATE);
                                    SharedPreferences.Editor editor = sharedPref.edit();
                                    editor.putString("saved_heure_debut", heureDebut);
                                    editor.commit();}})
                            .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int whichButton) {/* Do Nothing */ ;}})
                            .show(); }}});
        Button boutonHeureFin=(Button)findViewById(R.id.buttonHeureFin);
        boutonHeureFin.setOnClickListener(new View.OnClickListener(){ // Notre classe anonyme
            public void onClick(View view){ // et sa méthode !
                if (view.getId()==R.id.buttonHeureFin){
                    final EditText inputDialog = new EditText(MainActivity.this);
                    new AlertDialog.Builder(MainActivity.this)
                            .setTitle("Changer l'heure")
                            .setMessage("Heure de fin ?")
                            .setView(inputDialog)
                            .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int whichButton) {
                                    heureFin = inputDialog.getText().toString();
                                    SharedPreferences sharedPref = getPreferences(Context.MODE_PRIVATE);
                                    SharedPreferences.Editor editor = sharedPref.edit();
                                    editor.putString("saved_heure_fin", heureFin);
                                    editor.commit();}})
                            .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int whichButton) {/* Do Nothing */ ;}})
                            .show(); }}});
        startTime = System.currentTimeMillis();
        timerHandler.postDelayed(timerRunnable, 0);}

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        int i=0; int j;
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode==12345 && resultCode==RESULT_OK) {
            Uri selectedfile = data.getData(); //The uri with the location of the file
            SharedPreferences sharedPref = getPreferences(Context.MODE_PRIVATE); //getActivity().
            SharedPreferences.Editor editor = sharedPref.edit();
            editor.putString("saved_uri_selected", selectedfile.toString());
            editor.commit();
            chargePlanning(selectedfile);}}

    protected void chargePlanning(Uri pSelectedfile) {
        int i=0; int j;
        ContentResolver cr = getContentResolver();
        InputStream is = null;
        try {
            is = cr.openInputStream(pSelectedfile);
            Scanner s = new Scanner(is).useDelimiter("\\A");
            String str_in = s.hasNext() ? s.next() : "";
            for(; i<str_in.length();i++) {
                if (str_in.charAt(i)>32) {
                    break;}}
            j=i;
            for(; i<str_in.length();i++) {
                if ((str_in.charAt(i)==10)||(str_in.charAt(i)==13)) {
                    titreText = str_in.substring(j,i);
                    break;}}
            listStrings.clear();
            while (i<str_in.length()) {
                for (i++; i < str_in.length(); i++) {
                    if (str_in.charAt(i) > 32) {
                        break;}}
                j = i;
                for (; i < str_in.length(); i++) {
                    if ((str_in.charAt(i) == 10) || (str_in.charAt(i) == 13)) {
                        listStrings.add(str_in.substring(j, i));
                        break;}}}
            soustitreText = listStrings.get(0);
            charSousTitre = soustitreText.charAt(0);
            contenuText = listStrings.get(1);
            charContenu = contenuText.charAt(0);
            indexStrings = 2;
            indexContenu = 1;
            indexSousTitre = 1;
            nbContenus = 0;
            nbSousTitres = 0;
            for(int k = 0; k < listStrings.size(); k++) {
                String se =  listStrings.get(k);
                if (se.charAt(0)==charContenu) {
                    nbContenus++;}
                if (se.charAt(0)==charSousTitre) {
                    nbSousTitres++;}}
        } catch (FileNotFoundException e) {
            mTextMessage.setText("echec lecture");
            e.printStackTrace();}
      return;}

}
