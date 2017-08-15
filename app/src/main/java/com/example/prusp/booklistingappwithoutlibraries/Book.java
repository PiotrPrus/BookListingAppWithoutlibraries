package com.example.prusp.booklistingappwithoutlibraries;

/**
 * Created by Piotr Prus on 15.08.2017.
 */

public class Book {

    String author;
    String title;

    public Book(String author, String title) {
        this.author = author;
        this.title = title;
    }

    public String getAuthor() {
        return author;
    }

    public String getTitle() {
        return title;
    }
}
