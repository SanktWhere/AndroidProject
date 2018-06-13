package com.example.dmitriy.treeview;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import android.os.AsyncTask;
import android.widget.ArrayAdapter;
import android.widget.ListView;

public class MainActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        new ParseTask().execute();
    }

    class ParseTask extends AsyncTask<Void, Void, String> {
        String resultJson = "";
        HttpURLConnection urlConnection = null;
        BufferedReader reader = null;

        JSONArray sortByNames(JSONObject obj){
            JSONArray result = new JSONArray();
            try{
                JSONArray arrSort = obj.names();

                List<String> jsonValues = new ArrayList<String>();
                for (int i = 0; i < arrSort.length(); i++) {
                    jsonValues.add(arrSort.getString(i));
                }
                Collections.sort(jsonValues);

                for (int i = 0; i < arrSort.length(); i++) {
                    result.put(jsonValues.get(i));
                }
            } catch (JSONException e){
                e.printStackTrace();
            }
            return result;
        }

        void recursiveTraversalJSONObj(JSONObject obj, List<String> result){
            try {
                JSONArray  arrayNames = sortByNames(obj);

                for (int i = 0; i < arrayNames.length(); i++) {
                    if (obj.get(arrayNames.getString(i)) instanceof JSONArray){
                        JSONArray arrayObj = obj.getJSONArray(arrayNames.getString(i));
                        result.add("\t" + arrayNames.getString(i) + ":");
                        for (int j = 0; j < arrayObj.length(); j++){
                            if (arrayObj.get(j) instanceof String) {
                                result.add("\t\t"  +  arrayObj.get(j).toString());
                            } else {
                                recursiveTraversalJSONObj(arrayObj.getJSONObject(j), result);
                            }
                        }
                    } else if (obj.get(arrayNames.getString(i)) instanceof JSONObject){
                        result.add(arrayNames.getString(i) + ":");
                        recursiveTraversalJSONObj(obj.getJSONObject(arrayNames.getString(i)), result);
                    } else {
                        result.add("\t\t" + arrayNames.getString(i) + " : " + obj.getString(arrayNames.getString(i)));
                    }
                }
            } catch (JSONException e){
                e.printStackTrace();
            }
        }

        @Override
        protected String doInBackground(Void... params) {
            try {
                URL url = new URL("http://www.mocky.io/v2/56fa31e0110000f920a72134.json");
                urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setRequestMethod("GET");
                urlConnection.setConnectTimeout(1000);
                urlConnection.connect();

                InputStream inputStream = urlConnection.getInputStream();
                StringBuffer buffer = new StringBuffer();

                reader = new BufferedReader(new InputStreamReader(inputStream));

                String line;
                while ((line = reader.readLine()) != null) {
                    buffer.append(line);
                }
                resultJson = buffer.toString();

            } catch (Exception e) {
                e.printStackTrace();
            }
            return resultJson;
        }

        @Override
        protected void onPostExecute(String strJson) {
            List<String> result = new ArrayList<>();
            super.onPostExecute(strJson);

            JSONObject dataJsonObj = null;

            try {
                dataJsonObj = new JSONObject(strJson);
                recursiveTraversalJSONObj(dataJsonObj, result);

                String[] arraySting = new String[result.size()];
                for (int i = 0; i < result.size(); i++){
                    arraySting[i] = result.get(i);
                }

                ListView lvMain = findViewById(R.id.listView);

                ArrayAdapter<String> adapter = new ArrayAdapter<String>(MainActivity.this,
                        android.R.layout.simple_list_item_1, arraySting);

                lvMain.setAdapter(adapter);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }
}
