package com.hardcodecoder.pulsemusic.fragments.settings;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.SwitchCompat;

import com.hardcodecoder.pulsemusic.R;
import com.hardcodecoder.pulsemusic.dialog.AccentsChooserDialogFragment;
import com.hardcodecoder.pulsemusic.dialog.ThemeChooserBottomSheetDialogFragment;
import com.hardcodecoder.pulsemusic.fragments.settings.base.SettingsBaseFragment;
import com.hardcodecoder.pulsemusic.themes.ThemeManagerUtils;
import com.hardcodecoder.pulsemusic.utils.AppSettings;
import com.hardcodecoder.pulsemusic.views.SettingsToggleableItem;

import java.util.Objects;

public class SettingsThemeFragment extends SettingsBaseFragment {

    public static final String TAG = SettingsThemeFragment.class.getSimpleName();
    private Context mContext;

    public static SettingsThemeFragment getInstance() {
        return new SettingsThemeFragment();
    }

    @Override
    public String getFragmentTag() {
        return TAG;
    }

    @Override
    public int getToolbarTitleForFragment() {
        return R.string.look_and_feel;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_settings_theme, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mContext = getActivity();

        updateThemeSection(view);

        SettingsToggleableItem desaturatedAccentSwitchLayout = view.findViewById(R.id.laf_enable_desaturated);
        SwitchCompat desaturatedAccentSwitch = desaturatedAccentSwitchLayout.findViewById(R.id.setting_toggleable_item_switch);

        boolean desaturatedAccents = false;
        if (null != getContext()) {
            desaturatedAccents = AppSettings.getAccentDesaturatedColor(getContext());
        }

        desaturatedAccentSwitch.setChecked(desaturatedAccents);
        desaturatedAccentSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            AppSettings.saveAccentDesaturatedColor(buttonView.getContext(), isChecked);
            if (ThemeManagerUtils.isDarkModeEnabled())
                applyTheme();
        });
        desaturatedAccentSwitchLayout.setOnClickListener(v -> desaturatedAccentSwitch.setChecked(!desaturatedAccentSwitch.isChecked()));

        view.findViewById(R.id.laf_select_accent_color).setOnClickListener(v -> {
            AccentsChooserDialogFragment dialogFragment = AccentsChooserDialogFragment.getInstance();
            dialogFragment.show(Objects.requireNonNull(getActivity()).getSupportFragmentManager(), AccentsChooserDialogFragment.TAG);
        });
    }

    private void updateThemeSection(View view) {
        view.findViewById(R.id.laf_select_dark_theme).setOnClickListener(v -> {
            ThemeChooserBottomSheetDialogFragment dialog = ThemeChooserBottomSheetDialogFragment.getInstance();
            dialog.show(Objects.requireNonNull(getActivity()).getSupportFragmentManager(), ThemeChooserBottomSheetDialogFragment.TAG);
        });

        // Get custom views
        SettingsToggleableItem enableDarkThemeLayout = view.findViewById(R.id.laf_enable_dark_mode);
        SwitchCompat enableDarkThemeSwitch = enableDarkThemeLayout.findViewById(R.id.setting_toggleable_item_switch);

        SettingsToggleableItem enableAutoThemeLayout = view.findViewById(R.id.laf_enable_auto_mode);
        SwitchCompat enableAutoThemeSwitch = enableAutoThemeLayout.findViewById(R.id.setting_toggleable_item_switch);

        // Configure state of views based on saved settings
        boolean darkModeEnable = AppSettings.isDarkModeEnabled(mContext);
        enableDarkThemeSwitch.setChecked(darkModeEnable);

        boolean autoModeEnable = AppSettings.isAutoThemeEnabled(mContext);
        enableAutoThemeSwitch.setChecked(autoModeEnable);
        enableDarkThemeLayout.setEnabled(!autoModeEnable);


        //Add listeners to switch views
        enableDarkThemeSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (ThemeManagerUtils.toggleDarkTheme(mContext, isChecked))
                applyTheme();
        });
        enableDarkThemeLayout.setOnClickListener(v -> {
            // Trigger switch enable/disable
            enableDarkThemeSwitch.setChecked(!enableDarkThemeSwitch.isChecked());
        });

        enableAutoThemeSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            enableDarkThemeLayout.setEnabled(!isChecked);
            if (ThemeManagerUtils.toggleAutoTheme(mContext, isChecked))
                applyTheme();
            else {
                // User does not want auto theme based on time of day
                // Revert to theme selected via darkThemeToggle
                if (enableDarkThemeSwitch.isChecked() && !ThemeManagerUtils.isDarkModeEnabled()) {
                    // User previously select dark theme so when auto theme is
                    // disabled apply dark theme if not already applied
                    applyTheme();
                } else if (!enableDarkThemeSwitch.isChecked() && ThemeManagerUtils.isDarkModeEnabled()) {
                    // User previously select light theme so when auto theme is
                    // disabled apply light theme if not already applied
                    applyTheme();
                }
            }
        });
        enableAutoThemeLayout.setOnClickListener(v -> {
            // Trigger switch enable/disable
            enableAutoThemeSwitch.setChecked(!enableAutoThemeSwitch.isChecked());
        });
    }

    private void applyTheme() {
        ThemeManagerUtils.init(getActivity(), true);
        requestActivityRestart();
    }
}
