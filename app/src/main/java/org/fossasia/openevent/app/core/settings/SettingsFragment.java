package org.fossasia.openevent.app.core.settings;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.gms.oss.licenses.OssLicensesActivity;
import com.google.android.gms.oss.licenses.OssLicensesMenuActivity;
import com.takisoft.fix.support.v7.preference.PreferenceFragmentCompat;

import org.fossasia.openevent.app.BuildConfig;
import org.fossasia.openevent.app.R;
import org.fossasia.openevent.app.common.Constants;
import org.fossasia.openevent.app.ui.ViewUtils;

public class SettingsFragment extends PreferenceFragmentCompat {

    public static SettingsFragment newInstance() {
        return new SettingsFragment();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = super.onCreateView(inflater, container, savedInstanceState);
        view.setBackgroundColor(getResources().getColor(R.color.color_top_surface));
        return view;
    }

    @Override
    public void onCreatePreferencesFix(@Nullable Bundle bundle, String rootKey) {
        PreferenceManager manager = getPreferenceManager();
        manager.setSharedPreferencesName(Constants.FOSS_PREFS);

        setPreferencesFromResource(R.xml.preferences, rootKey);

        findPreference("rate_us").setOnPreferenceClickListener(preference -> {
            Uri uri = Uri.parse("market://details?id=" + BuildConfig.APPLICATION_ID);
            Intent playStoreIntent = new Intent(Intent.ACTION_VIEW, uri);
            try {
                startActivity(playStoreIntent);
            } catch (ActivityNotFoundException e) {
                startActivity(new Intent(Intent.ACTION_VIEW,
                    Uri.parse("http://play.google.com/store/apps/details?id=" + BuildConfig.APPLICATION_ID)));
            }
            return true;
        });

        findPreference("acknowledgement").setOnPreferenceClickListener(preference -> {
            startActivity(new Intent(getActivity(), OssLicensesMenuActivity.class));
            return true;
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        ViewUtils.setTitle(this, getString(R.string.device_settings));
    }
}
