package com.genonbeta.CoolSocket.test;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import com.genonbeta.CoolSocket.test.fragment.PairListFragment;

public class PairFinderActivity extends AppCompatActivity
{
    @Override
    protected void onCreate(Bundle bundle)
	{
        super.onCreate(bundle);
        setContentView(R.layout.activity_pairs);
    }
}
