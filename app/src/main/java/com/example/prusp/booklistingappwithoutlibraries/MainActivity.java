package com.example.prusp.booklistingappwithoutlibraries;

import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private static final String BASE_URL = "https://www.googleapis.com/books/v1/volumes?q=search+";

    EditText editText;
    Button button;
    BookAdapter adapter;
    ListView listView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        editText = (EditText) findViewById(R.id.edit_text_view);
        button = (Button) findViewById(R.id.search_button);
        adapter = new BookAdapter(this, -1);

        listView = (ListView) findViewById(R.id.list_view);
//        Parcelable state = listView.onSaveInstanceState();
        listView.setAdapter(adapter);
//        listView.onRestoreInstanceState(state);

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                BookAsyncTask task = new BookAsyncTask();
                task.execute();
            }
        });

    }

    private class BookAsyncTask extends AsyncTask<URL, Void, List<Book>> {
        @Override
        protected List<Book> doInBackground(URL... urls) {
            String jsonResponse = "";
            URL url = null;
            try {
                url = new URL(getFullUrlForRequest());
            } catch (MalformedURLException e) {
                e.printStackTrace();
            }
            try {
                jsonResponse = makeHttpRequest(url);
            } catch (IOException e) {
                e.printStackTrace();
            }

            return ParseAuthorsAsString.extractBooks(jsonResponse);
        }

        private String makeHttpRequest(URL url) throws IOException {
            String jsonResponse = "";

            if (url == null) {
                return jsonResponse;
            }

            HttpURLConnection urlConnection = null;
            InputStream inputStream = null;

            try {
                urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setRequestMethod("GET");
                urlConnection.setReadTimeout(10000);
                urlConnection.setConnectTimeout(15000);
                urlConnection.connect();
                if (urlConnection.getResponseCode() == 200) {
                    inputStream = urlConnection.getInputStream();
                    jsonResponse = readFromStream(inputStream);
                } else {
                    Log.e("MainActivity", "Cannot connect to given URL: " + urlConnection.getResponseCode());
                }
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                if (urlConnection != null) {
                    urlConnection.disconnect();
                }
                if (inputStream != null) {
                    inputStream.close();
                }

            }
            return jsonResponse;

        }

        private String readFromStream(InputStream inputStream) throws IOException {
            StringBuilder sb = new StringBuilder();
            if (inputStream != null) {
                InputStreamReader inputStreamReader = new InputStreamReader(inputStream, Charset.forName("UTF-8"));
                BufferedReader reader = new BufferedReader(inputStreamReader);
                String line = reader.readLine();
                while (line != null) {
                    sb.append(line);
                    line = reader.readLine();
                }
            }
            return sb.toString();
        }

        private String getFullUrlForRequest() {
            String endUrl = editText.getText().toString().trim().replaceAll("\\s+", "+");
            return (BASE_URL + endUrl);
        }

        @Override
        protected void onPostExecute(List<Book> books) {
            if (books == null) {
                return;
            }
            updateUi(books);
        }

        private void updateUi(List<Book> books) {
            adapter.clear();
            adapter.addAll(books);
        }
    }
}
