package com.example.prusp.booklistingappwithoutlibraries;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

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
    private static final String RESULTS = "booksResults";

    EditText editText;
    TextView noDataTextView;
    Button button;
    BookAdapter adapter;
    ListView listView;
    private int READ_TIMEOUT = 10000;
    private int CONNECT_TIMEOUT = 15000;
    private int PROPER_SERVER_RESPONSE = 200;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        editText = (EditText) findViewById(R.id.edit_text_view);
        noDataTextView = (TextView) findViewById(R.id.no_data_to_display_text_view);
        button = (Button) findViewById(R.id.search_button);
        adapter = new BookAdapter(this, -1);

        listView = (ListView) findViewById(R.id.list_view);
        listView.setAdapter(adapter);

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                hideVirtualKeyboard();
                if (isInternetAvailable()) {
                    BookAsyncTask task = new BookAsyncTask();
                    task.execute();
                } else {
                    Toast.makeText(MainActivity.this, R.string.check_internet, Toast.LENGTH_SHORT).show();
                }
            }
        });
        if (savedInstanceState != null) {
            Book[] values = (Book[]) savedInstanceState.getParcelableArray(RESULTS);
            adapter.addAll(values);
        }
    }

    private boolean isInternetAvailable() {
        ConnectivityManager connectivityManager =
                (ConnectivityManager) MainActivity.this.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = connectivityManager.getActiveNetworkInfo();
        return activeNetwork != null && activeNetwork.isConnectedOrConnecting();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        Book[] values = new Book[adapter.getCount()];
        for (int i = 0; i < values.length; i++) {
            values[i] = adapter.getItem(i);
        }
        outState.putParcelableArray(RESULTS, values);
    }

    private void hideVirtualKeyboard() {
        InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        inputMethodManager.hideSoftInputFromWindow((null == getCurrentFocus())
                ? null : getCurrentFocus().getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
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

            return ParsingUtil.extractBooks(jsonResponse);
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
                urlConnection.setReadTimeout(READ_TIMEOUT);
                urlConnection.setConnectTimeout(CONNECT_TIMEOUT);
                urlConnection.connect();
                if (urlConnection.getResponseCode() == PROPER_SERVER_RESPONSE) {
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
            if (books.isEmpty()) {
                noDataTextView.setVisibility(View.VISIBLE);
            } else {
                noDataTextView.setVisibility(View.GONE);
                adapter.addAll(books);
            }
        }
    }
}
