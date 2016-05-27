package com.example.xyzreader.remote;

import android.content.Context;
import android.graphics.Typeface;

import java.net.MalformedURLException;
import java.net.Proxy;
import java.net.URL;

public class Config {
    public static final URL BASE_URL;
public static Typeface typefaceMedium;
    public static     Typeface typefaceBold;
    public static Typeface typefaceRegular;
    static {
        URL url = null;
        try {
            url = new URL("https://dl.dropboxusercontent.com/u/231329/xyzreader_data/data.json" );
        } catch (MalformedURLException ignored) {
            // TODO: throw a real error
        }

        BASE_URL = url;
    }
    public static void initFonts(Context c){
        typefaceRegular=  Typeface.createFromAsset(c.getResources().getAssets(), "Roboto-Regular.ttf");
        typefaceMedium=  Typeface.createFromAsset(c.getResources().getAssets(), "Roboto-Medium.ttf");
        typefaceBold=  Typeface.createFromAsset(c.getResources().getAssets(), "Roboto-Bold.ttf");
    }

}
