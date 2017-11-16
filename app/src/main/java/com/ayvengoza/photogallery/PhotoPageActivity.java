package com.ayvengoza.photogallery;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;

/**
 * Created by ayven on 16.11.2017.
 */

public class PhotoPageActivity extends SingleFragmentActivity {
    private static final String EXTRA_URI = "extra_uri";
    public static Intent newIntent(Context context, Uri photoPageUri){
        Intent i = new Intent(context, PhotoPageActivity.class);
        i.setData(photoPageUri);
        return i;
    }
    @Override
    Fragment getFragment() {
        return PhotoPageFragment.newInstance(getIntent().getData());
    }
}
