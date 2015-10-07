package com.itachi1706.droideggs;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;

import com.itachi1706.droideggs.GingerbreadEgg.PlatLogoActivityGINGERBREAD;
import com.itachi1706.droideggs.HoneycombEgg.PlatLogoActivityHONEYCOMB;
import com.itachi1706.droideggs.JellyBeanEgg.PlatLogoActivityJELLYBEAN;
import com.itachi1706.droideggs.KitKatEgg.PlatLogoActivityKITKAT;
import com.itachi1706.droideggs.LollipopEgg.PlatLogoActivityLOLLIPOP;
import com.itachi1706.droideggs.MNCEgg.PlatLogoActivityMNC;
import com.itachi1706.droideggs.MarshmallowEgg.PlatLogoActivityMARSHMALLOW;
import com.itachi1706.droideggs.UpdatingApp.AppUpdateChecker;

import java.util.ArrayList;

public class MainScreen extends AppCompatActivity {

    Button currentVer;
    ListView selectionList;
    static Activity staticAct;

    private ArrayList<SelectorObject> populatedList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_screen);

        currentVer = (Button) findViewById(R.id.btnCurrent);
        selectionList = (ListView) findViewById(R.id.lvEasterEggSelection);

        staticAct = this;

        currentVer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (Build.VERSION.SDK_INT > 22) //Marshmallow
                    startActivity(new Intent(MainScreen.this, PlatLogoActivityMARSHMALLOW.class));
                else if (Build.VERSION.SDK_INT >= 21) //Lollipop (21-22)
                    startActivity(new Intent(MainScreen.this, PlatLogoActivityLOLLIPOP.class));
                else if (Build.VERSION.SDK_INT == 19) //KitKat
                    startActivity(new Intent(MainScreen.this, PlatLogoActivityKITKAT.class));
                else if (Build.VERSION.SDK_INT >= 16) //Jelly Bean
                    startActivity(new Intent(MainScreen.this, PlatLogoActivityJELLYBEAN.class));
                else if (Build.VERSION.SDK_INT >= 14) //ICS
                    weird("ICE CREAM SANDWICH", "JELLY BEAN");
                else if (Build.VERSION.SDK_INT >= 11) //Honeycomb
                    startActivity(new Intent(MainScreen.this, PlatLogoActivityHONEYCOMB.class));
                else if (Build.VERSION.SDK_INT >= 9)  //Gingerbread
                    startActivity(new Intent(MainScreen.this, PlatLogoActivityGINGERBREAD.class));
                else
                    noEgg();
            }
        });

        //Check for updates
        new AppUpdateChecker(this, PreferenceManager.getDefaultSharedPreferences(this.getApplicationContext()), true).execute();
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
            selectionList.setOnItemClickListener(new SelectorOnClick(this));
        } else {
            SelectorAdapter adapter = new SelectorAdapter(this, R.layout.listview_selector, populatedList);
            selectionList.setAdapter(adapter);
            selectionList.setOnItemClickListener(new SelectorOnClick(this));
        }

    }

    public static void unableToAccessEasterEgg(final String SDK_VERSION){
        Snackbar.make(staticAct.findViewById(android.R.id.content), "Unable to launch (INVALID VERSION)", Snackbar.LENGTH_LONG)
                .setAction("WHY?", new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        new AlertDialog.Builder(staticAct).setMessage("We are unable to give you access to " +
                                "this easter egg due to incompatible Android Version. You require at least Android " +
                                SDK_VERSION + " to access this activity")
                                .setPositiveButton("AWWW :(", null).show();
                    }
                }).show();
    }

    private static void weird(final String expected, final String actual){
        Snackbar.make(staticAct.findViewById(android.R.id.content), "Unable to launch (INVALID VERSION)", Snackbar.LENGTH_LONG)
                .setAction("WAIT WHAT?", new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        new AlertDialog.Builder(staticAct).setMessage("We are unable to give you access to " +
                                "this easter egg due to incompatible Android Version. You require at least Android " +
                                actual + " to access this activity\n\nDev Note: Interestingly... this easter egg is for " +
                                expected + ", so I'm confused. LOL")
                                .setPositiveButton("WAIT WTF? O.o", null)
                                .setNegativeButton("AWWW :(", null).show();
                    }
                }).show();
    }

    private static void noEgg(){
        Snackbar.make(staticAct.findViewById(android.R.id.content), "No Eggs for you", Snackbar.LENGTH_LONG)
                .setAction("WAIT WHAT?", new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        new AlertDialog.Builder(staticAct).setMessage("Easter Eggs are only present in Android" +
                                " from Android 2.3 Gingerbread. Your Android Version is do not have an easter egg" +
                                " unfortunately :(")
                                .setPositiveButton("AWWW :(", null).show();
                    }
                }).show();
    }

    public static void eggComingSoon(){
        Snackbar.make(staticAct.findViewById(android.R.id.content), "Easter Egg Coming Soon", Snackbar.LENGTH_SHORT)
                .setAction("DISMISS", new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                    }
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
