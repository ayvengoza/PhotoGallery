package com.ayvengoza.photogallery;

import android.support.v4.app.Fragment;

public class PhotoGalleryActivity extends SingleFragmentActivity {

    @Override
    Fragment getFragment() {
        return PhotoGalleryFragment.newInstance();
    }
}
