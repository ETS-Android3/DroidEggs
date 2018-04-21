package com.itachi1706.droideggs;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;

import com.itachi1706.appupdater.AppUpdateInitializer;

import java.util.ArrayList;

public class MainScreen extends AppCompatActivity {

    Button currentVer;
    ListView selectionList;
    static Activity staticAct;

    private ArrayList<SelectorObject> populatedList;
    private static final int RC_CURRENT_EGG = 2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_screen);

        currentVer = findViewById(R.id.btnCurrent);
        selectionList = findViewById(R.id.lvEasterEggSelection);

        staticAct = this;

        currentVer.setOnClickListener(v -> startActivityForResult(new Intent(MainScreen.this, CurrentEgg.class), RC_CURRENT_EGG));

        //Check for updates
        new AppUpdateInitializer(this, PreferenceManager.getDefaultSharedPreferences(this.getApplicationContext()),
                R.mipmap.ic_launcher, CommonVariables.BASE_SERVER_URL, true).checkForUpdate(true);
    }

    @Override
    public void onResume(){
        super.onResume();

        populatedList = new ArrayList<>();
        populatedList = PopulateSelector.populateSelectors(this);

        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this);
        boolean newSel = sp.getBoolean("check_version", false);

        if (newSel) {
            ArrayAdapter<String> tmpAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, getResources().getStringArray(R.array.legacy_version_with_egg));
            selectionList.setAdapter(tmpAdapter);
            selectionList.setOnItemClickListener(new SelectorOnClick());
        } else {
            SelectorAdapter adapter = new SelectorAdapter(this, R.layout.listview_selector, populatedList);
            selectionList.setAdapter(adapter);
            selectionList.setOnItemClickListener(new SelectorOnClick());
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == RC_CURRENT_EGG) {
            if (resultCode == RESULT_CANCELED) {
                String type = data.getStringExtra("class");
                switch (type) {
                    case "noaccess":
                        String t1 = data.getStringExtra("title");
                        unableToAccessEasterEgg(t1);
                        break;
                    case "comingsoon":
                        eggComingSoon();
                        break;
                    case "weird":
                        String t2 = data.getStringExtra("title");
                        String t3 = data.getStringExtra("body");
                        weird(t2, t3);
                        break;
                    case "noegg":
                    default: noEgg();
                }
            }
        }
    }

    public static void unableToAccessEasterEgg(final String SDK_VERSION){
        Snackbar.make(staticAct.findViewById(android.R.id.content), "Unable to launch (INVALID VERSION)", Snackbar.LENGTH_LONG)
                .setAction("WHY?", v -> new AlertDialog.Builder(staticAct).setMessage("We are unable to give you access to " +
                        "this easter egg due to incompatible Android Version. You require at least Android " +
                        SDK_VERSION + " to access this activity")
                        .setPositiveButton("AWWW :(", null).show()).show();
    }

    private static void weird(final String expected, final String actual){
        Snackbar.make(staticAct.findViewById(android.R.id.content), "Unable to launch (INVALID VERSION)", Snackbar.LENGTH_LONG)
                .setAction("WAIT WHAT?", v -> new AlertDialog.Builder(staticAct).setMessage("We are unable to give you access to " +
                        "this easter egg due to incompatible Android Version. You require at least Android " +
                        actual + " to access this activity\n\nDev Note: Interestingly... this easter egg is for " +
                        expected + ", so I'm confused. LOL")
                        .setPositiveButton("WAIT WTF? O.o", null)
                        .setNegativeButton("AWWW :(", null).show()).show();
    }

    private static void noEgg(){
        Snackbar.make(staticAct.findViewById(android.R.id.content), "No Eggs for you", Snackbar.LENGTH_LONG)
                .setAction("WAIT WHAT?", v -> new AlertDialog.Builder(staticAct).setMessage("Easter Eggs are only present in Android" +
                        " from Android 2.3 Gingerbread. Your Android Version is do not have an easter egg" +
                        " unfortunately :(")
                        .setPositiveButton("AWWW :(", null).show()).show();
    }

    public static void eggComingSoon(){
        Snackbar.make(staticAct.findViewById(android.R.id.content), "Easter Egg Coming Soon", Snackbar.LENGTH_SHORT)
                .setAction("DISMISS", v -> {

                }).show();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main_screen, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            startActivity(new Intent(this, MainSettings.class));
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
