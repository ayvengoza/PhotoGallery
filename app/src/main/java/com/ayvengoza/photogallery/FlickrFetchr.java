package com.ayvengoza.photogallery;

import android.net.Uri;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by ang on 02.11.17.
 */

public class FlickrFetchr {
    private static final String TAG = "FlickFetch";
    private static final String API_KEY = "b6d53e03aa72f61d87b4214eb900e8e5";
    private static final String API_SECRET = "a1c24b42356cc3bf";
    private static final String FETCH_RECENT_METHOD = "flickr.photos.getRecent";
    private static final String SEARCH_METHOD = "flickr.photos.search";
    private static final Uri ENDPOINT = Uri
            .parse("https://api.flickr.com/services/rest/")
            .buildUpon()
            .appendQueryParameter("api_key", API_KEY)
            .appendQueryParameter("format", "json")
            .appendQueryParameter("nojsoncallback", "1")
            .appendQueryParameter("extras", "url_s")
            .build();

    public byte[] getUrlBytes(String urlSpec) throws IOException {
        URL url = new URL(urlSpec);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();

        try {
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            InputStream in = connection.getInputStream();
            if (connection.getResponseCode() != HttpURLConnection.HTTP_OK) {
                throw new IOException(connection.getResponseMessage() +
                        ": with " + urlSpec);
            }
            int bytesRead = 0;
            byte[] buffer = new byte[1024];
            while ((bytesRead = in.read(buffer)) > 0) {
                out.write(buffer, 0, bytesRead);
            }
            out.close();
            return out.toByteArray();
        } finally {
            connection.disconnect();
        }
    }

    public String getUrlString(String urlSpec) throws IOException {
        return new String(getUrlBytes(urlSpec));
    }

    public List<GalleryItem> downloadGalleryItems(String url){
        List<GalleryItem> items = new ArrayList<>();
        try {
            String jsonString = getUrlString(url);
            Log.i(TAG, "Received JSON: " + jsonString);
            JSONObject jsonBody = new JSONObject(jsonString);
            parseItems(items, jsonBody);
        } catch (IOException e) {
            Log.e(TAG, "Failed to fetch items", e);
        } catch (JSONException e) {
            Log.e(TAG, "Failed to parse JSON: " + e);
        }
        return items;
    }

    public List<GalleryItem> fetchResentPhotos(int page){
        String url = buildUri(FETCH_RECENT_METHOD, null, page);
        return downloadGalleryItems(url);
    }

    public List<GalleryItem> searchPhotos(String query, int page){
        String url = buildUri(SEARCH_METHOD, query, page);
        return downloadGalleryItems(url);
    }

    private void parseItems(List<GalleryItem> items, JSONObject jsonBody) throws JSONException {
        JSONObject photosJsonObject = jsonBody.getJSONObject("photos");
        JSONArray photoJsonArray = photosJsonObject.getJSONArray("photo");

        GsonBuilder builder = new GsonBuilder();
        Gson gson = builder.create();
        GalleryItem[] itemArr = gson.fromJson(photoJsonArray.toString(), GalleryItem[].class);
        items.addAll(Arrays.asList(itemArr));

    }

    private String buildUri(String method, String query, int page){
        Uri.Builder uriBuilder = ENDPOINT.buildUpon()
                .appendQueryParameter("method", method)
                .appendQueryParameter("page" , String.valueOf(page));

        if(method.equals(SEARCH_METHOD)){
            uriBuilder.appendQueryParameter("text", query);
        }

        return uriBuilder.build().toString();
    }
}
