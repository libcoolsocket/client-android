package com.genonbeta.CoolSocket.test;

import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;

import com.genonbeta.CoolSocket.test.fragment.MessengerFragment;

public class HomeActivity extends AppCompatActivity
{
    @Override
    protected void onCreate(Bundle bundle)
    {
        super.onCreate(bundle);
        setContentView(R.layout.activity_main);
    }

    public boolean isMultiscreen()
    {
        FragmentManager supportFragmentManager = getSupportFragmentManager();
        return (supportFragmentManager.findFragmentById(R.id.main_pair_list_fragment) == null || supportFragmentManager.findFragmentById(R.id.main_template_list_fragment) == null) ? false : true;
    }

    public MessengerFragment getMessengerFragment()
    {
        return (MessengerFragment) getSupportFragmentManager().findFragmentById(R.id.main_messenger_fragment);
    }
}
