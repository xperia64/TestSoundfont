package com.example.testsoundfont.views;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.AnimRes;
import android.support.v7.app.AppCompatActivity;

import com.example.testsoundfont.App;
import com.example.testsoundfont.R;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.EActivity;

import lombok.Getter;
import lombok.Setter;

/**
 * Created by Administrator on 2016-03-08.
 */

@EActivity
public abstract class BaseActivity extends AppCompatActivity {

    @AfterViews
    public abstract void afterViews();

    protected void startActivity(Class<?> targetActivity, int flags, @AnimRes int enterAnim, @AnimRes int exitAnim) {
        Intent intent = new Intent(this, targetActivity);
        intent.setFlags(flags);
        startActivity(intent);
        overridePendingTransition(enterAnim, exitAnim);
    }

    protected void startActivity(Class<?> targetActivity, int flags) {
        startActivity(targetActivity, flags, R.anim.slide_in, R.anim.step_back);
    }

    public void startActivity(Class<?> targetActivity) {
        startActivity(targetActivity, 0);
    }

    public App getApp() {
        return App.getInstance();
    }
}
