package com.developer.soutos16lockscreen.activity

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.appcompat.app.AppCompatDelegate
import com.developer.soutos16lockscreen.R


class SplashActivity : AppCompatActivity() {

    companion object{
        lateinit  var sharedCheck : SharedPreferences
	lateinit var burasYeniDeğişken : Shared
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        //Dark mode disable app
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        sharedCheck = this.getSharedPreferences("check", Context.MODE_PRIVATE)
        Thread.sleep(800)


        if (sharedCheck.getBoolean("check",false)){
            val intent = Intent(this, MainActivity::class.java);
            startActivity(intent);
            finish()
        }else{
            val intent = Intent(this, SettingsActivity::class.java);
            startActivity(intent);
            finish()
        }

    };


}