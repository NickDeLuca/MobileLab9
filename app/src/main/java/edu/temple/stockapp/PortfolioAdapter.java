package edu.temple.stockapp;


import android.content.Context;
import android.graphics.Color;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import android.widget.Toast;

import org.w3c.dom.Text;

import java.util.List;

public class PortfolioAdapter extends ArrayAdapter {

    Context c;
    int count;
    List stocks;

    public PortfolioAdapter(Context context, int resource, List objects) {
        super(context, resource, objects);
        this.stocks = objects;
        this.c = context;
        this.count = objects.size();
    }

    @Override
    public int getCount() {
        return count;
    }

    @Override
    public Object getItem(int i) {
        return stocks.get(i);
    }


    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {

        String data = stocks.get(i).toString();


        String[] arr = data.split("\\|");


        String symbol = arr[0].toUpperCase();
        double current = Double.parseDouble(arr[2]);
        double open = Double.parseDouble(arr[3]);
        TextView t;

        if (view == null) {
            view = LayoutInflater.from(c).inflate(android.R.layout.simple_list_item_1, null);
            t = (TextView) view.findViewById(android.R.id.text1);
        } else {

            t = (TextView) view.findViewById(android.R.id.text1);
        }


        if(current < open){
            view.setBackgroundColor(Color.RED);
        }
        else{
            view.setBackgroundColor(Color.GREEN);
        }


        t.setText(symbol + "          $" + current);

        return view;
    }
}
