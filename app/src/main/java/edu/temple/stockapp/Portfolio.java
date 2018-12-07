package edu.temple.stockapp;


import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;


/**
 * A simple {@link Fragment} subclass.
 */
public class Portfolio extends Fragment {

    Context parent;
    ListView lv;
    TextView addMessage;
    public static final String FILENAME = "stockfile";


    public Portfolio() {
        // Required empty public constructor
    }

    @Override
    public void onAttach(Context context){
        super.onAttach(context);
        this.parent = context;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View v = inflater.inflate(R.layout.fragment_portfolio, container, false);

        lv = v.findViewById(R.id.listview);
        addMessage = (TextView) v.findViewById(R.id.addMessage);
        final ArrayList<String> stocks = new ArrayList<String>();

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



        if(stocks.size() == 0){
            addMessage.setText(R.string.empty);
        }
        else{
            addMessage.setText("");
            PortfolioAdapter adapter = new PortfolioAdapter(parent, android.R.layout.simple_list_item_1, stocks);
            lv.setAdapter(adapter);

            lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> context, View view, int position, long id) {

                    String compData = stocks.get(position);
                    String[] arr = compData.split("\\|");
                    String stockSymbol = arr[0];

                    ((portfolioInt) parent).stockSelected(stockSymbol);

                }
            });
        }


        return v;
    }


    interface portfolioInt{
        void stockSelected(String stockSymbol);
    }

}
