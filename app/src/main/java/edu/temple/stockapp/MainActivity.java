package edu.temple.stockapp;

import android.app.Dialog;
import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.webkit.WebView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

public class MainActivity extends AppCompatActivity implements Portfolio.portfolioInt {

    boolean singlePane;
    FragmentManager fm;
    Details det;
    FloatingActionButton fab;
    public static final String FILENAME = "stockfile";
    File stockfile;
    RequestQueue queue;
    Thread thread;
    Timer time;
    StockFileObs observer;
    TextView addMessage;

    final String API = "http://dev.markitondemand.com/MODApis/Api/v2/Quote/json/?symbol=";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        singlePane = findViewById(R.id.container2) == null;
        addMessage = (TextView) findViewById(R.id.addMessage);

        fm = getSupportFragmentManager();
        fm.beginTransaction()
                .replace(R.id.container1, new Portfolio())
                .commit();
        if(!singlePane){
            det = new Details();
            fm.beginTransaction()
                    .replace(R.id.container2, det)
                    .commit();


        }

        queue = Volley.newRequestQueue(MainActivity.this);

        stockfile = new File(getFilesDir(), FILENAME);
        if ( stockfile.exists() == false ){
            try {
                stockfile.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        final Handler obsHandler = new Handler(new Handler.Callback() {
            @Override
            public boolean handleMessage(Message msg) {
                //make updates to details pane and portfolio pane
                String alldata = (String) msg.obj;
                String[] compSplit = alldata.split(",");

                fm.beginTransaction()
                        .replace(R.id.container1, new Portfolio())
                        .commit();

                if(!alldata.equals("")){
                    if(singlePane){
                        boolean details = findViewById(R.id.listview) == null;
                        if(details){
                            TextView compName = (TextView) findViewById(R.id.textName);
                            TextView current = (TextView) findViewById(R.id.currentPrice);
                            TextView open = (TextView) findViewById(R.id.openingPrice);

                            String company = compName.getText().toString();
                            for(int i = 0; i < compSplit.length; i++){
                                String[] arr = compSplit[i].split("\\|");
                                if(company.equals(arr[1])){
                                    current.setText("Current Price: $" + arr[2]);
                                    open.setText("Opening Price: $" + arr[3]);
                                    break;
                                }
                            }
                        }
                        else{
                            ListView lv = findViewById(R.id.listview);
                            ArrayList<String> stocklist = new ArrayList<>();
                            for(int i = 0; i < compSplit.length; i++){
                                stocklist.add(compSplit[i]);
                            }
                            lv.setAdapter(new PortfolioAdapter(MainActivity.this, android.R.layout.simple_list_item_1, stocklist));

                        }
                    }
                    else{
                        ListView lv = findViewById(R.id.listview);
                        ArrayList<String> stocklist = new ArrayList<>();
                        for(int i = 0; i < compSplit.length; i++){
                            stocklist.add(compSplit[i]);
                        }
                        lv.setAdapter(new PortfolioAdapter(MainActivity.this, android.R.layout.simple_list_item_1, stocklist));

                        TextView compName = (TextView) findViewById(R.id.textName);
                        TextView current = (TextView) findViewById(R.id.currentPrice);
                        TextView open = (TextView) findViewById(R.id.openingPrice);

                        String company = compName.getText().toString();
                        if(!company.equals(R.string.noSelection)){
                            for(int i = 0; i < compSplit.length; i++){
                                String[] arr = compSplit[i].split("\\|");
                                if(company.equals(arr[1])){
                                    current.setText("Current Price: $" + arr[2]);
                                    open.setText("Opening Price: $" + arr[3]);
                                    break;
                                }
                            }
                        }


                    }
                }



                return false;
            }
        });

        observer = new StockFileObs(stockfile.getPath(), obsHandler, MainActivity.this);



        //Start worker thread here updating file
        thread = new Thread(){
            @Override
            public void run(){

                if(!thread.isInterrupted()){
                    time = new Timer();
                    time.scheduleAtFixedRate(new TimerTask() {

                        String allStocksForUpdate = "";

                        @Override
                        public void run() {
                            Log.e("TTT", "Thread ran.");

                            //update file here
                            final StockFileObs threadObs = new StockFileObs(stockfile.getPath(), obsHandler, MainActivity.this);
                            final ArrayList<String> stocks = new ArrayList<String>();


                            String finalData = getPortfolioData();
                            final ArrayList<String> allSymbols = new ArrayList<>();

                            if(!finalData.equals("")){
                                String[] arr = finalData.split(",");

                                for(int i = 0; i < arr.length; i++){
                                    stocks.add(arr[i]);
                                }

                                for(int j = 0; j < stocks.size(); j++){
                                    String[] compData = stocks.get(j).split("\\|");
                                    allSymbols.add(compData[0]);
                                }



                                for(int k = 0; k < allSymbols.size(); k++){
                                    final String symbol = allSymbols.get(k);

                                    String url = API + symbol;

                                    JsonObjectRequest threadReq = new JsonObjectRequest(Request.Method.GET, url, null, new Response.Listener<JSONObject>() {

                                        @Override
                                        public void onResponse(JSONObject response) {

                                            try {
                                                String stockinfo = symbol + "|" + response.getString("Name") + "|" +
                                                        response.getDouble("LastPrice") + "|" + response.getDouble("Open") + ",";

                                                Log.e("SSS", stockinfo);
                                                allStocksForUpdate += stockinfo;

                                                Log.e("ALL", allStocksForUpdate);

                                                if(allStocksForUpdate.split(",").length == allSymbols.size()){

                                                    threadObs.startWatching();
                                                    if(!allStocksForUpdate.equals("")){
                                                        FileOutputStream stream = new FileOutputStream(stockfile, false);
                                                        OutputStreamWriter writer = new OutputStreamWriter(stream);
                                                        writer.write(allStocksForUpdate);
                                                        writer.flush();
                                                        allStocksForUpdate = "";
                                                        Log.e("UPDATE", "HEREHERE");
                                                    }
                                                    else{
                                                        Log.e("ELSE", "EMPTY");
                                                    }
                                                    threadObs.stopWatching();
                                                }

                                            } catch (JSONException e) {
                                                e.printStackTrace();
                                            } catch (FileNotFoundException e) {
                                                e.printStackTrace();
                                            } catch (IOException e) {
                                                e.printStackTrace();
                                            }

                                        }
                                        }, new Response.ErrorListener() {

                                            @Override
                                            public void onErrorResponse(VolleyError error) {
                                            }
                                        });

                                    queue.add(threadReq);

                                }

                            }

                        }

                    }, 10000, 60000);
                }

            }
        };
        thread.start();



        fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                final Dialog dialog = new Dialog(MainActivity.this);
                dialog.setContentView(R.layout.fab_dialog);
                dialog.show();
                Button cancel = (Button) dialog.findViewById(R.id.cancel);
                Button add = (Button) dialog.findViewById(R.id.add);
                final EditText text = (EditText) dialog.findViewById(R.id.editText);

                cancel.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        dialog.hide();
                    }
                });

                add.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        final String symbol = text.getText().toString();

                        boolean duplicate = false;


                        String finalData = getPortfolioData();
                        final ArrayList<String> stocks = new ArrayList<String>();


                        if(!finalData.equals("")) {
                            String[] arr = finalData.split(",");

                            for (int i = 0; i < arr.length; i++) {
                                stocks.add(arr[i]);
                            }

                            for (int j = 0; j < stocks.size(); j++) {
                                String[] compData = stocks.get(j).split("\\|");
                                if(compData[0].toUpperCase().equals(symbol.toUpperCase())){
                                    duplicate = true;
                                }
                            }
                        }

                        if(!duplicate){
                            String url = API + symbol;

                            observer.startWatching();

                            JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET, url, null, new Response.Listener<JSONObject>() {

                                @Override
                                public void onResponse(JSONObject response) {
                                    try {
                                        if(!response.getString("Name").isEmpty()){
                                            String stockinfo = symbol.toUpperCase() + "|" + response.getString("Name") + "|" +
                                                    response.getDouble("LastPrice") + "|" + response.getDouble("Open") + ",";

                                            try {

                                                FileOutputStream stream = new FileOutputStream(stockfile, true);
                                                OutputStreamWriter writer = new OutputStreamWriter(stream);
                                                writer.append(stockinfo);
                                                writer.flush();
                                                observer.stopWatching();
                                            }
                                            catch (FileNotFoundException e) {
                                                e.printStackTrace();
                                            }
                                            catch (IOException e){
                                                e.printStackTrace();
                                            }
                                            dialog.hide();
                                        }
                                    } catch (JSONException e) {
                                        e.printStackTrace();
                                    }

                                    try{
                                        if(!response.getString("Message").isEmpty()){
                                            Toast.makeText(MainActivity.this, R.string.symbolError, Toast.LENGTH_LONG).show();
                                        }
                                    } catch (JSONException e) {
                                        e.printStackTrace();
                                    }
                                }
                                }, new Response.ErrorListener() {

                                    @Override
                                    public void onErrorResponse(VolleyError error) {
                                    }
                                });

                            queue.add(jsonObjectRequest);
                        }
                        else{
                            Toast.makeText(MainActivity.this, R.string.duplicateError, Toast.LENGTH_LONG).show();
                        }
                    }


                });
            }
        });
    }

    @Override
    public void stockSelected(String stockSymbol){

        if(singlePane){
            Details det = Details.newInstance(stockSymbol);
            fm.beginTransaction()
                    .replace(R.id.container1, det)
                    .addToBackStack(null)
                    .commit();

        }
        else{
            det.changeStock(stockSymbol);
        }

    }

    @Override
    protected void onDestroy() {
        //stop thread here
        time.cancel();
        thread.interrupt();
        super.onDestroy();
    }


    public String getPortfolioData(){
        String finalData = "";

        try {
            FileInputStream file = openFileInput(FILENAME);
            InputStreamReader reader = new InputStreamReader(file);
            BufferedReader bufferedReader = new BufferedReader(reader);
            StringBuilder sb = new StringBuilder();
            String data;
            while ((data = bufferedReader.readLine()) != null )
            {
                sb.append(data);
            }
            reader.close();

            bufferedReader.close();

            finalData = sb.toString();


        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return finalData;
    }



}
