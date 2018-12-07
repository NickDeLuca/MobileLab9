package edu.temple.stockapp;


import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.TextView;

import org.w3c.dom.Text;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;


/**
 * A simple {@link Fragment} subclass.
 */
public class Details extends Fragment {

    public static final String SYMBOL_KEY = "Symbol";
    public static final String FILENAME = "stockfile";

    final ArrayList<String> stocks = new ArrayList<String>();

    View fragView;
    TextView textName;
    TextView textCurrent;
    TextView textOpen;
    WebView web;
    WebViewClient client;
    Context parent;

    public Details() {
        // Required empty public constructor
    }

    public void onAttach(Context context){
        super.onAttach(context);
        this.parent = context;
    }

    public static Details newInstance(String stockSymbol){
        Details det = new Details();
        Bundle args = new Bundle();
        args.putString(SYMBOL_KEY, stockSymbol);
        det.setArguments(args);

        return det;
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_details, container, false);
        fragView = v;
        textName = (TextView) v.findViewById(R.id.textName);
        textCurrent = (TextView) v.findViewById(R.id.currentPrice);
        textOpen = (TextView) v.findViewById(R.id.openingPrice);
        web = (WebView) v.findViewById(R.id.webview);
        web.getSettings().setJavaScriptEnabled(true);


        try {
            FileInputStream file = parent.openFileInput(FILENAME);
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

            String finalData = sb.toString();

            if(!finalData.equals("")){
                String[] arr = finalData.split(",");
                for(int i = 0; i < arr.length; i++){
                    stocks.add(arr[i]);
                }
            }

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }


        if(getArguments() != null){
            String symbol = getArguments().getString(SYMBOL_KEY);

            for(int j = 0; j < stocks.size(); j++){
                String[] compData = stocks.get(j).split("\\|");
                if(compData[0].equals(symbol)){
                    textName.setText("Company Name: " + compData[1]);
                    textCurrent.setText("Current Price: $" + compData[2]);
                    textOpen.setText("Opening Price: $" + compData[3]);
                    web.loadUrl("https://macc.io/lab/cis3515/?symbol=" + symbol);
                    break;
                }
            }


        }
        else{
            textName.setText(R.string.noSelection);
        }


        return v;
    }

    public void changeStock(String symbol){

        for(int j = 0; j < stocks.size(); j++){
            String[] compData = stocks.get(j).split("\\|");
            if(compData[0].equals(symbol)){
                textName.setText("Company Name: " + compData[1]);
                textCurrent.setText("Current Price: $" + compData[2]);
                textOpen.setText("Opening Price: $" + compData[3]);
                web.loadUrl("https://macc.io/lab/cis3515/?symbol=" + symbol);
                break;
            }
        }

    }

}
