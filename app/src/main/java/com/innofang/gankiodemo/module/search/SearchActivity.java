package com.innofang.gankiodemo.module.search;

import android.content.Context;
import android.content.Intent;
import android.support.v4.app.Fragment;

import com.innofang.gankiodemo.R;
import com.innofang.gankiodemo.module.SingleFragmentActivity;

public class SearchActivity extends SingleFragmentActivity {

    public static Intent newIntent(Context context){
        return new Intent(context, SearchActivity.class);
    }

    @Override
    protected Fragment createFragment() {
        return SearchFragment.newInstance();
    }

    @Override
    protected int getLayoutResId() {
        return R.layout.activity_search;
    }

    @Override
    protected int getFragmentContainerId() {
        return R.id.fragment_container;
    }

}
