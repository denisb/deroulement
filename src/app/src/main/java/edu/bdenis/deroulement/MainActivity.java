package edu.bdenis.deroulement;

// TODO: 16/08/2018 : ajouter une numérotation automatique des titres et contenus
// TODO: 16/08/2018 : gérer les plans se déroulant sur plusieurs séances
// TODO: 16/08/2018 : ajouter une url ou un page avec un contenu fourni in extenso pour chaque item de contenu
// TODO: 16/08/2018 : ajouter un splash screen de lancement
// TODO: 16/08/2018 : ajouter une aide ou description
// TODO: 16/08/2018 : ajouter un "qui sommes-nous ?"
// TODO: 16/08/2018 : ajouter des parametres pour le nombre de plans disponible
// TODO: 16/08/2018 : ajouter des parametres pour la taille des textes
// TODO: 16/08/2018 : enregistrer les temps passés sur chaque contenu (si on sait quoi faire avec ensuite), ou seulement tracer l'activité ...
// TODO: 16/08/2018 : améliorer icone
// TODO: 17/08/2018 : améliorer la gestion de la taille pour que cela tienne sur une ligne ...
// TODO: 17/08/2018 : refactoriser le code (chaines, cstes, code dupliqué, taille du code atomique, ...)
// TODO: 17/08/2018 : alléger les textes en enlevant espace au début/fin et le caractère distinctif
// TODO: 17/08/2018 : gagner de la place en réduisant haut et bas (nom appli et navigation) 
// TODO: 17/08/2018 : vérifier init à partir de rien (pas de fichier)
// TODO: 17/08/2018 : améliorer calcul temps restant en moyenne (considère qu'il faut prévoir de (re)faire tout le  premier element)

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
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
/* Context context = getApplicationContext();
CharSequence text = pSelectedfile.toString();
int duration = Toast.LENGTH_SHORT;
Toast toast = Toast.makeText(context, text, duration);
toast.show(); */
// ou juste : Toast.makeText(getApplicationContext(), "Message du moment" , Toast.LENGTH_SHORT).show();


import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import android.os.Vibrator;
//<! uses-permission android:name="android.permission.VIBRATE"/ ->

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
    private int indexPlan =0;
    private int indexContenu=1;
    private int indexSousTitre=1;
    private int nbContenus=2;
    private int nbSousTitres =2;
    private int charSousTitre=42;
    private int charContenu=45;
    private int decalageTps = 0;
    private int decalageTpsInit = 0;
    private int changeTps = 0;
    private int tpsPasse = 0;
    private int savedTpsPasse = 0;
    private boolean doubleClick = false;
    long startTime = 0;
    private float x1,x2;
    static final int MIN_DISTANCE = 10;
    Handler timerHandler = new Handler();
    Runnable doubleClickRunnable = new Runnable() {
        @Override
        public void run() {
            doubleClick = false;}};
    Runnable lightRunnable = new Runnable() {
        @Override
        public void run() {
            WindowManager.LayoutParams lp = getWindow().getAttributes();
            lp.screenBrightness = 0.0f;
            getWindow().setAttributes(lp);}};
    Runnable timerRunnable = new Runnable() {
        @Override
        public void run() {
            Vibrator vibs;
            long millis = System.currentTimeMillis() - startTime + tpsPasse;
            if (Math.abs(savedTpsPasse-(int) (millis))>1000*30) {
                SharedPreferences sharedPref;
                SharedPreferences.Editor editor;
                sharedPref = getPreferences(Context.MODE_PRIVATE);
                editor = sharedPref.edit();
                editor.putInt("saved_temps_passe"+indexPlan, (int) (millis));
                savedTpsPasse=(int) (millis);
                editor.commit();
                //Toast.makeText(getApplicationContext(), "times runinng", Toast.LENGTH_SHORT).show();
                }
            if (((millis/1000)%127)>117) {
                setTitle(getText(R.string.app_name)+"   \"J'adore quand un plan se déroule sans accroc\"");}
            else if (((millis/1000)%127)>107) {
                setTitle(getText(R.string.app_name));}
            else if (((millis/1000)%127)>97) {
                setTitle(getText(R.string.app_name)+"   \"On fait comme on a prévu. Quelque chose me dit qu'on va bien s'amuser !\"");}
            else if (((millis/1000)%127)>87) {
                setTitle(getText(R.string.app_name));}
            else if (((millis/1000)%127)>77) {
                setTitle(getText(R.string.app_name)+"   \"l’important, ce n’est pas la destination, mais le voyage en lui-même\"");}
            else {
                setTitle(getText(R.string.app_name));}
            int seconds = (int) (millis / 1000);
            int minutes = seconds / 60; //minutes passées depuis l'heure de début
            int h = 0; int m = 0; int hd=13, hf=15, md=30, mf=0;
            Calendar calendar = Calendar.getInstance();
            h = calendar.getTime().getHours();
            m = calendar.getTime().getMinutes();
            int dureeS = 90*60; //valeurs par défaut
            int dureeMS = 1000 * dureeS; //valeurs par défaut
            int rm = (dureeS-seconds) / 60; //minutes restantes jusqu'à l'heure de fin //valeurs par défaut
            Pattern regExHeure = Pattern.compile("([^0-9]*)([0-9]+)([^0-9]+)([0-9]+)([^0-9]*)");
            Matcher matchHeure = regExHeure.matcher(heureDebut);
            try{if (matchHeure.matches()) {
                    hd = Integer.parseInt(matchHeure.group(2));
                    md = Integer.parseInt(matchHeure.group(4));}
                else {
                    hd = 0; md = 0; heureDebut= "08h00";}}
            catch (Error e) {Toast.makeText(getApplicationContext(), "Erreur 002", Toast.LENGTH_SHORT).show();};
            matchHeure = regExHeure.matcher(heureFin);
            try{if (matchHeure.matches() && (matchHeure.groupCount()>2)) {
                hf = Integer.parseInt(matchHeure.group(2));
                mf = Integer.parseInt(matchHeure.group(4));}
            else {
                hf = 0; mf = 0; heureFin= "09h30";}}
            catch (Error e) {Toast.makeText(getApplicationContext(), "Erreur 003", Toast.LENGTH_SHORT).show();};
            if (((h>hd)||((h==hd)&&(m>=md)))&&((h<hf)||((h==hf)&&(m<=mf)))) {
                minutes = 60 * (h-hd) + m - md;
                dureeS = 60*(hf-hd)+mf-md ; //duree reelle en minute !
                dureeMS = 1000*60*dureeS+changeTps;  //duree modifiée/changée en ?!?
                if (dureeMS==0) {
                    Toast.makeText(getApplicationContext(), "Close to the end", Toast.LENGTH_SHORT).show();
                    dureeMS = 1000;}
                rm = dureeS - minutes;}
            Button boutonHeureDebut=(Button)findViewById(R.id.buttonHeureDebut);
            boutonHeureDebut.setX(20);
            boutonHeureDebut.setY(timerTextView.getY());
            boutonHeureDebut.setText(heureDebut+"+("+minutes+")");
            if (nbContenus==indexContenu) {
                timerTextView.setText(String.format("%dh%d (faut finir)",h,m));}
            else {
                timerTextView.setText(String.format("%dh%d (%d*%dmin)",h,m,nbContenus-indexContenu,rm/(nbContenus-indexContenu)));}
            timerTextView.setX(boutonHeureDebut.getX()+boutonHeureDebut.getWidth());
            Button boutonHeureFin=(Button)findViewById(R.id.buttonHeureFin);
            boutonHeureFin.setX(timerTextView.getX()+timerTextView.getWidth());
            boutonHeureFin.setY(timerTextView.getY());
            boutonHeureFin.setText(heureFin+("-("+rm+")"));
            titreTextView.setText(titreText);
            titreTextView.setX(Math.abs(220-Math.round((200*(millis+decalageTps+changeTps))/dureeMS)));
            indexContenu = -1;
            indexSousTitre = -1;
            for(int k = 0; k < listStrings.size(); k++) {
                String se =  listStrings.get(k);
                if (se.charAt(0)==charContenu) {
                    indexContenu++;
                    if (indexContenu==Math.round(((millis+decalageTps+changeTps)*nbContenus)/dureeMS)) {
                        contenuText = se;
                        soustitreTextView.setText(soustitreText);
                        soustitreTextView.setX(Math.abs(220-Math.round((200*nbSousTitres)*(millis+decalageTps+changeTps-((indexSousTitre*dureeMS)/nbSousTitres))/dureeMS )));
                        if (k==(listStrings.size()-1)) {
                            contenuTextView.setText(contenuText+" <<rien (fini)");}
                        else {
                            contenuTextView.setText(contenuText+" <<"+listStrings.get(k+1));}
                        if (Math.abs(millis+decalageTps+changeTps-((indexSousTitre*dureeMS)/nbSousTitres))<299) {
                            vibs = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
                            vibs.vibrate(250);}
                        contenuTextView.setX(Math.abs(220-Math.round((200*nbContenus)*(millis+decalageTps+changeTps-((indexContenu*dureeMS)/nbContenus))/dureeMS )));
                        if (Math.abs(millis+decalageTps+changeTps-((indexContenu*dureeMS)/nbContenus))<299) {
                            vibs = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
                            vibs.vibrate(100);}
                        break;}}
                if (se.charAt(0)==charSousTitre) {
                    indexSousTitre++;
                    soustitreText = se;}}
            if (indexContenu==-1) {
                indexContenu = 0;
                //Toast.makeText(getApplicationContext(), "Index de contenu ?", Toast.LENGTH_SHORT).show();
                setTitle(getText(R.string.app_name)+"   \"(Index de contenu ?)\"");}
            if (indexSousTitre==-1) {
                indexSousTitre = 0;
                //Toast.makeText(getApplicationContext(), "Index de titre ?", Toast.LENGTH_SHORT).show();
                setTitle(getText(R.string.app_name)+"   \"(Index de titre ?)\"");}
            if (decalageTps>0) {
                decalageTps = decalageTps - Math.min((3 * decalageTps / 100),25000);}
            else {
                decalageTps = decalageTps - Math.max((3 * decalageTps / 100),-25000);}
            timerHandler.postDelayed(this, 300);}};

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        float deltaX;
        timerHandler.removeCallbacks(lightRunnable);
        timerHandler.postDelayed(lightRunnable, 30000);
        WindowManager.LayoutParams lp = getWindow().getAttributes();
        lp.screenBrightness = 1.0f;
        getWindow().setAttributes(lp);
        switch(event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                x1 = event.getX();
                decalageTpsInit = decalageTps;
                if (doubleClick) {
                    if (x1<100) {
                        changeTps = 0;
                        tpsPasse = 0;
                        Toast.makeText(getApplicationContext(), "Reset", Toast.LENGTH_SHORT).show();}
                    else {
                        changeTps = changeTps + decalageTps;
                        Toast.makeText(getApplicationContext(), "Stop rewind", Toast.LENGTH_SHORT).show();}
                    SharedPreferences sharedPref;
                    SharedPreferences.Editor editor;
                    sharedPref = getPreferences(Context.MODE_PRIVATE);
                    editor = sharedPref.edit();
                    editor.putInt("saved_change_temps"+indexPlan,changeTps);
                    editor.commit();
                    decalageTps=0;}
                else {
                    doubleClick = true;
                    timerHandler.postDelayed(doubleClickRunnable, 500);}
                break;
            case MotionEvent.ACTION_MOVE:
                x2 = event.getX();
                deltaX = x2 - x1;
                if (Math.abs(deltaX) > MIN_DISTANCE) {
                    decalageTps = decalageTpsInit - (10000 * Math.round(deltaX / MIN_DISTANCE));}
                break;}
        return super.onTouchEvent(event);}

    private BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener
            = new BottomNavigationView.OnNavigationItemSelectedListener() {
        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
            SharedPreferences sharedPref;
            SharedPreferences.Editor editor;
            Uri tmpSelectedfile;
            String strUriPlanning;
            switch (item.getItemId()) {
                case R.id.navigation_previous:
                    mTextMessage.setText(getText(R.string.title_previous)+" (>"+((indexPlan +4)%5)+")");
                    indexPlan = (indexPlan +4) % 5;
                    sharedPref = getPreferences(Context.MODE_PRIVATE);
                    editor = sharedPref.edit();
                    editor.putInt("saved_index_planning", indexPlan);
                    editor.commit();
                    heureDebut = sharedPref.getString("saved_heure_debut"+ indexPlan, "8h00");
                    heureFin = sharedPref.getString("saved_heure_fin"+ indexPlan, "9h30");
                    strUriPlanning = sharedPref.getString("saved_uri_selected"+ indexPlan, "/");
                    tmpSelectedfile = Uri.parse(strUriPlanning);
                    chargePlanning(tmpSelectedfile, true); //avant
                    return true;
                case R.id.navigation_main:
                    mTextMessage.setText(getText(R.string.title_main)+" ("+ indexPlan +")");
                    Intent intent = new Intent().setType("text/plain").setAction(Intent.ACTION_GET_CONTENT);
                    startActivityForResult(Intent.createChooser(intent, "Select a file"), 12345);
                    return true;
                case R.id.navigation_next:
                    mTextMessage.setText(getText(R.string.title_next)+" (>"+((indexPlan +6)%5)+")");
                    indexPlan = (indexPlan +6) % 5;
                    sharedPref = getPreferences(Context.MODE_PRIVATE);
                    editor = sharedPref.edit();
                    editor.putInt("saved_index_planning", indexPlan);
                    editor.commit();
                    heureDebut = sharedPref.getString("saved_heure_debut"+ indexPlan, "8h00");
                    heureFin = sharedPref.getString("saved_heure_fin"+ indexPlan, "9h30");
                    strUriPlanning = sharedPref.getString("saved_uri_selected"+ indexPlan, "/");
                    tmpSelectedfile = Uri.parse(strUriPlanning);
                    chargePlanning(tmpSelectedfile, true); //apres
                    return true;}
            return false;}};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        WindowManager.LayoutParams lp = getWindow().getAttributes();
        lp.screenBrightness = 1.0f;
        getWindow().setAttributes(lp);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        timerHandler.postDelayed(lightRunnable, 30000);
        setContentView(R.layout.activity_main);
        mTextMessage = (TextView) findViewById(R.id.message);
        timerTextView = (TextView) findViewById(R.id.heure);
        titreTextView = (TextView) findViewById(R.id.titre);
        titreText = getString(R.string.text_titre);
        soustitreTextView = (TextView) findViewById(R.id.soustitre);
        soustitreText = getString(R.string.text_soustitre);
        contenuTextView = (TextView) findViewById(R.id.contenu);
        contenuText = getString(R.string.text_contenu);
        SharedPreferences sharedPref = getPreferences(Context.MODE_PRIVATE);
        indexPlan = sharedPref.getInt("saved_index_planning", 0);
        changeTps = sharedPref.getInt("saved_change_temps"+ indexPlan, 0);
        tpsPasse = sharedPref.getInt("saved_temps_passe"+ indexPlan, 0);
        heureDebut = sharedPref.getString("saved_heure_debut"+ indexPlan, "8h00");
        heureFin = sharedPref.getString("saved_heure_fin"+ indexPlan, "9h30");
        String strUriPlanning = sharedPref.getString("saved_uri_selected"+ indexPlan, "/");
        Uri tmpSelectedfile = Uri.parse(strUriPlanning);
        chargePlanning(tmpSelectedfile,true); //init
        mTextMessage.setText(getText(R.string.title_main)+" ("+ indexPlan +")");
        BottomNavigationView navigation = (BottomNavigationView) findViewById(R.id.navigation);
        navigation.setSelectedItemId(R.id.navigation_main);
        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);
        //setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE); //si execute, relance create ... => manifest (1 seul create)
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
                                    editor.putString("saved_heure_debut"+ indexPlan, heureDebut);
                                    editor.commit();}})
                            .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int whichButton) {/* Do Nothing */ ;}})
                            .show(); }}});
        Button boutonHeureFin=(Button)findViewById(R.id.buttonHeureFin);
        boutonHeureFin.setOnClickListener(new View.OnClickListener(){
            public void onClick(View view){
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
                                    editor.putString("saved_heure_fin"+ indexPlan, heureFin);
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
            Uri selectedfile = data.getData();
            SharedPreferences sharedPref = getPreferences(Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = sharedPref.edit();
            editor.putString("saved_uri_selected"+ indexPlan, selectedfile.toString());
            editor.commit();
            chargePlanning(selectedfile,false);
        }} //changement

    protected void chargePlanning(Uri pSelectedfile, Boolean saved_content) {
        int i=0; int j; String str_in;
        try {
            if (saved_content) {
                //Context context = getApplicationContext();
                //CharSequence text = pSelectedfile.toString();
                //int duration = Toast.LENGTH_SHORT;
                //Toast toast = Toast.makeText(context, text, duration);
                //toast.show();
                //is = cr.openInputStream(pSelectedfile);
                str_in = "TitreVide\n#SousTitreVide\n*ContenuVide";
                SharedPreferences sharedPref = getPreferences(Context.MODE_PRIVATE);
                str_in = sharedPref.getString("saved_file_content"+indexPlan, "Titre-Vide\n#SousTitre-Vide\n*Contenu-Vide");

            } else {
                ContentResolver cr = getContentResolver();
                InputStream is = null;
                is = cr.openInputStream(pSelectedfile);
                Scanner s = new Scanner(is).useDelimiter("\\A"); //début de chaine
                str_in = s.hasNext() ? s.next() : "";
                SharedPreferences sharedPref;
                SharedPreferences.Editor editor;
                sharedPref = getPreferences(Context.MODE_PRIVATE);
                editor = sharedPref.edit();
                editor.putString("saved_file_content"+indexPlan,str_in);
                editor.commit();}
        } catch (FileNotFoundException e) {
            mTextMessage.setText(getText(R.string.msg_erreur_lecture)+" ("+ indexPlan +")");
            Toast.makeText(getApplicationContext(), "Erreur 001", Toast.LENGTH_SHORT).show();
            str_in = "TitreVide\n#SousTitreVide\n*ContenuVide";
            e.printStackTrace();}

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
                    if ((str_in.charAt(i) == 10) || (str_in.charAt(i) == 13)||(i==(str_in.length()-1))) {
                        listStrings.add(str_in.substring(j, i));
                        break;}}}

            if (listStrings.size()==0) {
                soustitreText = "Pas de sous-titre";
                charSousTitre = '#';}
            else {
                soustitreText = listStrings.get(0);
                if (soustitreText.length()==0) {
                    charSousTitre = '#';}
                else {
                    charSousTitre = soustitreText.charAt(0);}}
            if (listStrings.size()>=2) {
                contenuText = listStrings.get(1);
                if (contenuText.length()==0) {
                    charContenu = '*';}
                else {
                    charContenu = contenuText.charAt(0);}}
            else {
                contenuText = "Pas de contenu";
                charContenu = '*';}
            indexStrings = 2;
            indexContenu = 1;
            indexSousTitre = 1;
            nbContenus = 0;
            nbSousTitres = 0;
            for(int k = 0; k < listStrings.size(); k++) {
                String se =  listStrings.get(k);
                if ((se.length()>0)&&(se.charAt(0)==charContenu)) {
                    nbContenus++;}
                if ((se.length()>0)&&(se.charAt(0)==charSousTitre)) {
                    nbSousTitres++;}}
            if (nbContenus==0) {
                nbContenus = 1;
                Toast.makeText(getApplicationContext(), "Pas de contenu ?", Toast.LENGTH_SHORT).show();}
            if (nbSousTitres==0) {
                nbSousTitres = 1;
                Toast.makeText(getApplicationContext(), "Pas de titre ?", Toast.LENGTH_SHORT).show();}
            Toast.makeText(getApplicationContext(), pSelectedfile.toString(), Toast.LENGTH_SHORT).show();
            String msgLecture = "Lect tit+ "+listStrings.size()+" l, "+nbSousTitres+" st, "+nbContenus+" ct.";
            Toast.makeText(getApplicationContext(), msgLecture, Toast.LENGTH_SHORT).show();
      return;}

}
