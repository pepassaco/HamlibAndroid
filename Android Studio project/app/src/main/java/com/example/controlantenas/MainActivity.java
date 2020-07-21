package com.example.controlantenas;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.preference.PreferenceManager;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.net.SocketException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;
import java.util.concurrent.ExecutionException;



public class MainActivity extends AppCompatActivity implements SharedPreferences.OnSharedPreferenceChangeListener{


    Button boton, mueve, sat;
    TextView response, titulo, pasos;
    EditText az, el;
    Toolbar toolbar;




    public static String msg = "p";
    public static String resp = "No Response Yet\nTry Sending Some Data.";
    public static String server_address = "192.168.0.1";
    public static Integer server_port = 4533;
    public static Boolean Abort = false;
    public static LongOperation lo = null;

    static String responseCode = "";

    public static Socket socket = null;

    public static final String MY_PREFS_NAME = "MyPrefsFile";

    public static final String API_KEY = "RKKZB6-V6TRS9-WQHMA8-4C25";
    public static final String URL_1 = "https://www.n2yo.com/rest/v1/satellite/radiopasses/";
    public static final String URL_2 = "/40.437/-3.713/600/1/10/&apiKey=";
    public static final String NOAA_15 = "25338";
    public static final String NOAA_18 = "28654";
    public static final String NOAA_19 = "33591";
    public static final String METEOR_M2 = "40069";








    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        Log.i("[BLUETOOTH]", "Creating listeners");
        response = (TextView) findViewById(R.id.response);
        titulo = (TextView) findViewById(R.id.titulo);
        pasos = (TextView) findViewById(R.id.pasos);
        boton = (Button) findViewById(R.id.B1);
        mueve = (Button) findViewById(R.id.B2);
        sat = (Button) findViewById(R.id.B3);
        az = (EditText) findViewById(R.id.az);
        el = (EditText) findViewById(R.id.el);

        // Attaching the layout to the toolbar object
        toolbar = (Toolbar) findViewById(R.id.tool_bar);
        // Setting toolbar as the ActionBar with setSupportActionBar() call
        setSupportActionBar(toolbar);

        boton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                actualizaMensaje(false);
                sendMessage();
            }
        });

        mueve.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                actualizaMensaje(true);
                sendMessage();
            }
        });

        sat.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                actualizaSats();
            }
        });

        setupSharedPreferences();
        sendMessage();


    }




    private class LongOperation extends AsyncTask<String, Void, String> {


        @Override
        protected String doInBackground(String... params) {

            socket = null;
            SocketAddress address = new InetSocketAddress(server_address, server_port);

            socket = new Socket();


            try {
                socket.connect(address, 1500);
            } catch (IOException e) {
                Log.d("time","no worky X");
                e.printStackTrace();
            }
            try {
                socket.setSoTimeout(250);
            } catch (SocketException e) {
                Log.d("timeout","server took too long to respond");

                e.printStackTrace();
                return "Can't Connect";
            }
            OutputStream out = null;
            try {
                out = socket.getOutputStream();
            } catch (IOException e) {
                e.printStackTrace();
            }
            PrintWriter output = new PrintWriter(out);


            output.print(msg);
            output.flush();

//read
            String str = "waiting";
            BufferedReader br = null;
            try {
                br = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            } catch (IOException e) {
                e.printStackTrace();
            }
            try {
                Log.d("test","trying to read from server");

                String line;
                str = "";
                while ((line = br.readLine()) != null) {
                    Log.d("read line",line);
                    str = str + line;
                    str = str + "\r\n";
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            if (str != null) {
                Log.d("test","trying to print what was just read");
                System.out.println(str);
            }


//read
            output.close();

//read
            try {
                br.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
//read
            try {
                socket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }

            Log.d("tag", "done server");
            return str;
        }

        @Override
        protected void onPostExecute(String result) {

            // might want to change "executed" for the returned string passed
            // into onPostExecute() but that is upto you
            Abort = false;
            Log.d("Set Abort",Abort.toString());
            Log.d("tag","post ex");
            resp = result;
            TextView textView = (TextView) findViewById(R.id.response);
            textView.setText(resp);

        }

        @Override
        protected void onPreExecute() {


            SharedPreferences.Editor editor = getSharedPreferences(MY_PREFS_NAME, MODE_PRIVATE).edit();
            editor.putString("server", server_address);
            editor.putInt("port", server_port);
            editor.commit();
        }

        @Override
        protected void onProgressUpdate(Void... values) {}


        protected void onCancelled(){
            Log.d("cancel","ca");
            try {
                socket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            Abort = false;
        }


    }
    /** Called when the user taps the Send button */
    public void sendMessage() {

        //String message = "_";
        TextView textView = (TextView) findViewById(R.id.response);
        textView.setText("Loading...");
        //msg = message;
        Log.d("msg",msg);

        Log.d("Check Abort",Abort.toString());
        if(Abort) {
            lo.cancel(false);
            Log.d("Aborting",Abort.toString());
        }
        else {
            lo = new LongOperation();
            lo.execute();
        }
        Abort = true;

    }

    public void actualizaMensaje(boolean f){
        if(f){
            this.msg= "P " + az.getText().toString() + " " + el.getText().toString();
        }else{
            this.msg= "p";
        }
    }


    public void actualizaSats(){
        pasos.setText("Cargando...");
        pasos.setText(siguientesPasos());

    }




    private String getResponse(String endpoint) {
        HttpHelper helper = new HttpHelper();
        String result = "";
        try {
            result = helper.execute(endpoint).get();
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }
        responseCode = HttpHelper.getResponseCode();
        return result;
    }

    public String siguientesPasos(){
        String N15 = getResponse(URL_1+NOAA_15+URL_2+API_KEY);
        String N18 = getResponse(URL_1+NOAA_18+URL_2+API_KEY);
        String N19 = getResponse(URL_1+NOAA_19+URL_2+API_KEY);
        String MN2 = getResponse(URL_1+METEOR_M2+URL_2+API_KEY);

        try {
            JSONObject J1N15 = new JSONObject(N15);
            JSONObject J1N18 = new JSONObject(N18);
            JSONObject J1N19 = new JSONObject(N19);
            JSONObject J1MN2 = new JSONObject(MN2);

            Log.d("msg",J1N15.toString());

            JSONArray JN15Ar = new JSONArray(J1N15.getString("passes"));
            JSONArray JN18Ar = new JSONArray(J1N18.getString("passes"));
            JSONArray JN19Ar = new JSONArray(J1N19.getString("passes"));
            JSONArray JMN2Ar = new JSONArray(J1MN2.getString("passes"));

            Log.d("msg",JN15Ar.toString());

            JSONObject JN15 = JN15Ar.getJSONObject(0);
            JSONObject JN18 = JN18Ar.getJSONObject(0);
            JSONObject JN19 = JN19Ar.getJSONObject(0);
            JSONObject JMN2 = JMN2Ar.getJSONObject(0);

            Log.d("msg",JN15.toString());

            long AOS_N15 = Long.parseLong(JN15.getString("startUTC"),10) * (long) 1000;
            long AOS_N18 = Long.parseLong(JN18.getString("startUTC"),10) * (long) 1000;
            long AOS_N19 = Long.parseLong(JN19.getString("startUTC"),10) * (long) 1000;
            long AOS_MN2 = Long.parseLong(JMN2.getString("startUTC"),10) * (long) 1000;

            Date tAOS_N15 = new Date(AOS_N15);
            Date tAOS_N18 = new Date(AOS_N18);
            Date tAOS_N19 = new Date(AOS_N19);
            Date tAOS_MN2 = new Date(AOS_MN2);


            long LOS_N15 = Long.parseLong(JN15.getString("endUTC"),10) * (long) 1000;
            long LOS_N18 = Long.parseLong(JN18.getString("endUTC"),10) * (long) 1000;
            long LOS_N19 = Long.parseLong(JN19.getString("endUTC"),10) * (long) 1000;
            long LOS_MN2 = Long.parseLong(JMN2.getString("endUTC"),10) * (long) 1000;

            Date tLOS_N15 = new Date(LOS_N15);
            Date tLOS_N18 = new Date(LOS_N18);
            Date tLOS_N19 = new Date(LOS_N19);
            Date tLOS_MN2 = new Date(LOS_MN2);


            SimpleDateFormat format1 = new SimpleDateFormat("MMM dd HH:mm:ss");
            format1.setTimeZone(TimeZone.getTimeZone("CET"));

            SimpleDateFormat format2 = new SimpleDateFormat("HH:mm:ss");
            format2.setTimeZone(TimeZone.getTimeZone("CET"));

            Log.d("date", format1.format(AOS_N15));

            return ("Siguientes pasos:\n\nNOAA 15:\nAOS: " + format2.format(tAOS_N15) + "    LOS:  " + format2.format(tLOS_N15) + "\n\nNOAA 18:\nAOS: " + format2.format(tAOS_N18) + "    LOS:  " + format2.format(tLOS_N18) + "\n\nNOAA 19:\nAOS: " + format2.format(tAOS_N19) + "    LOS:  " + format2.format(tLOS_N19) + "\n\nMeteor M2:\nAOS: " + format2.format(tAOS_MN2) + "    LOS:  " + format2.format(tLOS_MN2));

        } catch (JSONException e) {
            e.printStackTrace();
            Log.d("msg","Petou");
        }
        return null;




    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.settings_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            Intent intent = new Intent(MainActivity.this, SettingsActivity.class);
            startActivity(intent);
            return true;
        }else if (id == R.id.action_about) {
            Intent intent = new Intent(MainActivity.this, AboutActivity.class);
            startActivity(intent);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {

        if (key.equals("display_text")) {
            //setTextVisible(sharedPreferences.getBoolean("display_text",true));
        } else if (key.equals("port")) {
            actualizaPuerto(sharedPreferences);
        } else if (key.equals("server"))  {
            actualizaServidor(sharedPreferences);
        }
    }

    private void setupSharedPreferences() {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        //actualizaServidor(sharedPreferences);
        //actualizaPuerto(sharedPreferences);
        sharedPreferences.registerOnSharedPreferenceChangeListener(this);
    }

    private void actualizaServidor(SharedPreferences sharedPreferences) {
        server_address = sharedPreferences.getString(getString(R.string.pref_server_key), "192.168.0.1");
    }

    private void actualizaPuerto(SharedPreferences sharedPreferences) {
        server_port = Integer.parseInt(sharedPreferences.getString(getString(R.string.pref_port_key), "4533"));
    }

}
