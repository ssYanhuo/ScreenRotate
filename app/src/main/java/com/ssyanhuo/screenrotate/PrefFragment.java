package com.ssyanhuo.screenrotate;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.view.View;
import android.widget.Toast;

import com.google.android.material.snackbar.Snackbar;


import androidx.annotation.Nullable;

public class PrefFragment extends PreferenceFragment {
    public void onCreate(Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        super.onCreate(savedInstanceState);

        //从xml文件加载选项
        addPreferencesFromResource(R.xml.pref_settings);
    }

    @Override
    public void onViewCreated(final View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        Preference grantPermissions = findPreference("Grant_Permissions");
        grantPermissions.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                Toast.makeText(getActivity(), "授予权限", Toast.LENGTH_SHORT).show();
                return true;
            }
        });
        Preference switchService = findPreference("Service_Enabled");
        switchService.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                Snackbar.make(view, "服务已启动", Snackbar.LENGTH_SHORT).show();
                return true;
            }
        });
        SharedPreferences userSettings= getActivity().getSharedPreferences("com.ssyanhuo.screenrotate_preferences", 0);
        boolean serviceEnabled = userSettings.getBoolean("Service_Enabled",false);
        if(serviceEnabled){
            startBackend();
        }
        switchService.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                if(preference.getKey().equals("Service_Enabled")){
                    if(String.valueOf(newValue).equals("true")){
                        startBackend();
                        return true;
                    }
                    else {
                        stopBackend();
                        return true;
                    }
                }
                return true;
            }
        });

    }
    public void startBackend(){
        Intent service = new Intent(getActivity(), BackendService.class);
        //判断SDK并启动后台服务
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
            getActivity().startForegroundService(service);
        }
        else {
            getActivity().startService(service);
        }
    }
    public void stopBackend(){
        Intent service = new Intent(getActivity(), BackendService.class);
        getActivity().stopService(service);
    }
}