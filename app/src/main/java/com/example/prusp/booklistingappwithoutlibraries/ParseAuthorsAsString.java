package com.example.prusp.booklistingappwithoutlibraries;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Piotr Prus on 15.08.2017.
 */

public class ParseAuthorsAsString {

    private ParseAuthorsAsString() {
    }

    public static String formatAuthorsList(JSONArray authorsList) throws JSONException {

        String stringOfAuthorsList = null;

        if (authorsList.length() == 0) {
            return null;
        }
        for (int i = 0; i < authorsList.length(); i++) {
            if (i == 0) {
                stringOfAuthorsList = authorsList.getString(0);
            } else {
                stringOfAuthorsList += ", " + authorsList.getString(i);
            }
        }
        return stringOfAuthorsList;
    }

    public static List<Book> extractBooks(String json) {
        List<Book> books = new ArrayList<>();

        try {
            JSONObject jsonResponse = new JSONObject(json);

            if (jsonResponse.getInt("totalItems") == 0) {
                return books;
            }
            JSONArray jsonArray = jsonResponse.getJSONArray("items");

            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject bookObject = jsonArray.getJSONObject(i);

                JSONObject bookInfo = bookObject.getJSONObject("volumeInfo");

                String title = bookInfo.getString("title");
                JSONArray authors = new JSONArray();
                if (bookInfo.has("authors")) {
                    authors = bookInfo.getJSONArray("authors");
                }
                String authorsString = formatAuthorsList(authors);

                Book book = new Book(authorsString, title);
                books.add(book);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return books;
    }
}