package edu.temple.stockapp;


import android.content.Context;
import android.os.FileObserver;
import android.os.Handler;
import android.os.Message;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;


public class StockFileObs extends FileObserver {

    public static final String FILENAME = "stockfile";
    Handler handler;
    Context parent;

    public StockFileObs(String path, Handler handler, Context parent) {
        super(path);
        this.handler = handler;
        this.parent = parent;
    }

    @Override
    public void onEvent(int event, String path) {

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
            Message msg = Message.obtain();
            msg.obj = finalData;
            handler.sendMessage(msg);


        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }
}
