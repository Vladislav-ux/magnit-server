package ru.magnit.demo;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class Main {
    public static void main(String[] args) {
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
        try {
            Date date = formatter.parse("2001-10-28");
            System.out.println(date);
        } catch (ParseException e) {
            e.printStackTrace();
        }
    }
}
