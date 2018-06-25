package com.ssig.smartcap.fragment;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.hardware.Sensor;
import android.os.Bundle;
import android.support.annotation.LayoutRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.StringRes;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.PopupMenu;
import android.support.v7.widget.RecyclerView;
import android.text.InputType;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TabHost;
import android.widget.TabWidget;
import android.widget.TextView;
import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.ssig.sensorsmanager.SensorType;
import com.ssig.sensorsmanager.info.DeviceInfo;
import com.ssig.sensorsmanager.info.SensorInfo;
import com.ssig.smartcap.R;
import com.ssig.smartcap.adapter.AdapterSensorsGrid;
import com.ssig.smartcap.model.SensorsGridItem;
import com.ssig.smartcap.utils.Tools;

import java.util.*;

@SuppressWarnings("WeakerAccess")
public abstract class AbstractDeviceFragment extends AbstractMainFragment {

    private Map<SensorType.SensorGroup, AdapterSensorsGrid> adapterSensorsGridMap;
    @StringRes private final int preferencesFileName;

    protected AbstractDeviceFragment(@LayoutRes int layout, @StringRes int preferencesFileName) {
        super(layout);
        this.clearAdapterSensorsGridMap();
        this.preferencesFileName = preferencesFileName;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        this.initUI();
    }

    protected void clearAdapterSensorsGridMap(){
        if (this.adapterSensorsGridMap != null)
            this.adapterSensorsGridMap.clear();
        this.adapterSensorsGridMap = new HashMap<>();
    }

    public List<SensorsGridItem> getValidSensorGridItemList(){
        List<SensorsGridItem> sensorsGridItemList = new LinkedList<>();
        for (AdapterSensorsGrid adapterSensorsGrid : this.adapterSensorsGridMap.values()){
            for (SensorsGridItem sensorsGridItem : adapterSensorsGrid.getSensorsGridItems()){
                if(sensorsGridItem.isValid())
                    sensorsGridItemList.add(sensorsGridItem);
            }
        }
        return sensorsGridItemList;
    }


    @SuppressLint("DefaultLocale")
    protected void initDeviceInfo(View layoutDeviceInfo, DeviceInfo deviceInfo) {
        ((TextView)layoutDeviceInfo.findViewById(R.id.device_name_text)).setText(deviceInfo.getDeviceName());
        ((TextView)layoutDeviceInfo.findViewById(R.id.device_manufacturer_model_text)).setText(String.format("%s %s (%s)", deviceInfo.getManufacturer().toUpperCase(), deviceInfo.getMarketName(), deviceInfo.getModel()));
        ((TextView)layoutDeviceInfo.findViewById(R.id.android_version_text)).setText(String.format("%s (API %s)", deviceInfo.getAndroidVersion(), deviceInfo.getAndroidSDK()));
        ((TextView)layoutDeviceInfo.findViewById(R.id.device_uuid_text)).setText(deviceInfo.getDeviceKey());
    }

    protected void initDeviceSensors(View layoutAvailableSensors) {

        TabHost tabHostAvailableSensors = layoutAvailableSensors.findViewById(R.id.available_sensors_tab_host);
        tabHostAvailableSensors.setup();
        this.initTab(tabHostAvailableSensors, R.id.motion_sensors_tab, SensorType.SensorGroup.MOTION);
        this.initTab(tabHostAvailableSensors, R.id.position_sensors_tab, SensorType.SensorGroup.POSITION);
        this.initTab(tabHostAvailableSensors, R.id.environment_sensors_tab, SensorType.SensorGroup.ENVIRONMENT);

        TabWidget tabWidget = tabHostAvailableSensors.findViewById(android.R.id.tabs);
        for (int i = 0; i < tabWidget.getTabCount(); i++){
            View tabView = tabWidget.getChildTabViewAt(i);
            tabView.setPadding(0, 0, 0, 0);
            TextView textView = tabView.findViewById(android.R.id.title);
            textView.setTextSize(14);
        }

        final Button frequencyMenuButton = layoutAvailableSensors.findViewById(R.id.frequency_menu_button);
        frequencyMenuButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                PopupMenu popupMenu = new PopupMenu(Objects.requireNonNull(getContext()), frequencyMenuButton);
                popupMenu.inflate(R.menu.menu_frequencies);
                popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem item) {
                        int itemID = item.getItemId();
                        if (itemID == R.id.frequency_limited_to){
                            new MaterialDialog.Builder(getContext())
                                    .title(R.string.devices_dialog_frequency_title)
                                    .titleColorRes(R.color.colorPrimary)
                                    .icon(Tools.changeDrawableColor(Objects.requireNonNull(ContextCompat.getDrawable(getContext(), R.drawable.ic_tune)), getContext().getColor(R.color.colorPrimary)))
                                    .content(R.string.devices_dialog_frequency_content)
                                    .inputType(InputType.TYPE_CLASS_NUMBER)
                                    .neutralText(R.string.button_cancel)
                                    .neutralColorRes(R.color.colorGrey)
                                    .onNeutral(new MaterialDialog.SingleButtonCallback() {
                                        @Override
                                        public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                                            dialog.cancel();
                                        }
                                    })
                                    .positiveText(R.string.button_ok)
                                    .input(getString(R.string.devices_dialog_frequency_hint), null, false, new MaterialDialog.InputCallback() {
                                        @Override
                                        public void onInput(@NonNull MaterialDialog dialog, CharSequence input) {
                                            try{
                                                int frequency = Integer.valueOf(input.toString());
                                                setAllMaximumSensorFrequencies(frequency);
                                                dialog.getActionButton(DialogAction.POSITIVE).setEnabled(true);
                                            }catch (NumberFormatException e){
                                                dialog.getActionButton(DialogAction.POSITIVE).setEnabled(false);
                                            }

                                        }
                                    })
                                    .alwaysCallInputCallback()
                                    .show();

                        }else{
                            int percentage = 100;
                            switch (itemID){
                                case R.id.frequency_min: percentage = 0; break;
                                case R.id.frequency_medium: percentage = 50; break;
                                case R.id.frequency_max: percentage = 100; break;
                            }
                            setAllSensorFrequenciesPercentage(percentage);
                        }
                        return true;
                    }
                });
                popupMenu.show();
            }
        });

    }

    protected void initTab(TabHost tabHostAvailableSensors, int tabLayout, SensorType.SensorGroup sensorGroup){

        String tabName = sensorGroup.name().toLowerCase();
        tabName = String.format("%s%s", tabName.substring(0, 1).toUpperCase(), tabName.substring(1));

        TabHost.TabSpec tabSpec = tabHostAvailableSensors.newTabSpec(tabName);
        tabSpec.setContent(tabLayout);
        tabSpec.setIndicator(tabName, ContextCompat.getDrawable(Objects.requireNonNull(this.getContext()), R.drawable.ic_smartwatch_off));
        tabHostAvailableSensors.addTab(tabSpec);

        RecyclerView recyclerView = Objects.requireNonNull(this.getView()).findViewById(tabLayout);
        recyclerView.setLayoutManager(new GridLayoutManager(this.getContext(), 3, GridLayoutManager.VERTICAL, false));
        recyclerView.setHasFixedSize(true);

    }

    protected void configureAvailableSensors(DeviceInfo deviceInfo){
        this.configureRecyclerView((RecyclerView)Objects.requireNonNull(this.getView()).findViewById(R.id.motion_sensors_tab), deviceInfo, SensorType.SensorGroup.MOTION);
        this.configureRecyclerView((RecyclerView)Objects.requireNonNull(this.getView()).findViewById(R.id.position_sensors_tab), deviceInfo, SensorType.SensorGroup.POSITION);
        this.configureRecyclerView((RecyclerView)Objects.requireNonNull(this.getView()).findViewById(R.id.environment_sensors_tab), deviceInfo, SensorType.SensorGroup.ENVIRONMENT);
    }

    protected void configureRecyclerView(RecyclerView recyclerView, DeviceInfo deviceInfo, SensorType.SensorGroup sensorGroup){
        SharedPreferences sharedPreferences = Objects.requireNonNull(getContext()).getSharedPreferences(getString(preferencesFileName), Context.MODE_PRIVATE);
        List<SensorsGridItem> items = new ArrayList<>();
        for (Map.Entry<SensorType, SensorInfo> entry : deviceInfo.getSensorsInfo(sensorGroup).entrySet()) {
            SensorsGridItem sensorsGridItem;
            SensorInfo sensorInfo = entry.getValue();
            if (sensorInfo != null) {
                sensorsGridItem = new SensorsGridItem(sensorInfo);
                sensorsGridItem.setEnabled(sharedPreferences.getBoolean(sensorsGridItem.getSensorType().code() + getString(R.string.preference_sensor_enabled_suffix), true));
                sensorsGridItem.setFrequency(sharedPreferences.getInt(sensorsGridItem.getSensorType().code() + getString(R.string.preference_sensor_frequency_suffix), sensorsGridItem.getDefaultFrequency()));
            } else {
                sensorsGridItem = new SensorsGridItem(entry.getKey());
            }
            items.add(sensorsGridItem);
        }
        Collections.sort(items, new Comparator<SensorsGridItem>() {
            @Override
            public int compare(SensorsGridItem o1, SensorsGridItem o2) {
                Boolean b1 = o1.isValid();
                Boolean b2 = o2.isValid();
                int comp = b1.compareTo(b2);
                if (comp != 0)
                    return -comp;
                return o1.getSensorType().toString().compareTo(o2.getSensorType().toString());
            }
        });

        AdapterSensorsGrid adapterSensorsGrid = new AdapterSensorsGrid(getContext(), items, getString(preferencesFileName));
        recyclerView.setAdapter(adapterSensorsGrid);
        this.adapterSensorsGridMap.put(sensorGroup, adapterSensorsGrid);
    }

    protected void setAllMaximumSensorFrequencies(int maxFrequency){
        SharedPreferences sharedPreferences = Objects.requireNonNull(getContext()).getSharedPreferences(getString(preferencesFileName), Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        for (AdapterSensorsGrid adapterSensorsGrid : this.adapterSensorsGridMap.values()){
            for (SensorsGridItem sensorsGridItem : adapterSensorsGrid.getSensorsGridItems()){
                if(sensorsGridItem.isValid()) {
                    if ((sensorsGridItem.getReportingMode() == Sensor.REPORTING_MODE_CONTINUOUS || sensorsGridItem.getReportingMode() == Sensor.REPORTING_MODE_ON_CHANGE) && (sensorsGridItem.getMaxFrequency() > sensorsGridItem.getMinFrequency())) {
                        sensorsGridItem.setFrequency(Math.min(maxFrequency, sensorsGridItem.getMaxFrequency()));
                        editor.putInt(sensorsGridItem.getSensorType().code() + getContext().getString(R.string.preference_sensor_frequency_suffix), sensorsGridItem.getFrequency());
                    }
                }
            }
            adapterSensorsGrid.notifyDataSetChanged();
        }
        editor.apply();
    }

    protected void setAllSensorFrequenciesPercentage(int percentage){
        SharedPreferences sharedPreferences = Objects.requireNonNull(getContext()).getSharedPreferences(getString(preferencesFileName), Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        for (AdapterSensorsGrid adapterSensorsGrid : this.adapterSensorsGridMap.values()){
            for (SensorsGridItem sensorsGridItem : adapterSensorsGrid.getSensorsGridItems()){
                if(sensorsGridItem.isValid()) {
                    if ((sensorsGridItem.getReportingMode() == Sensor.REPORTING_MODE_CONTINUOUS || sensorsGridItem.getReportingMode() == Sensor.REPORTING_MODE_ON_CHANGE) && (sensorsGridItem.getMaxFrequency() > sensorsGridItem.getMinFrequency())) {
                        int frequency = sensorsGridItem.getMaxFrequency();
                        switch (percentage){
                            case 0: frequency = sensorsGridItem.getMinFrequency(); break;
                            case 25: frequency = (int) (0.25 * sensorsGridItem.getMaxFrequency()); break;
                            case 50: frequency = (int) (0.50 * sensorsGridItem.getMaxFrequency()); break;
                            case 75: frequency = (int) (0.75 * sensorsGridItem.getMaxFrequency()); break;
                            case 100: frequency = sensorsGridItem.getMaxFrequency(); break;
                        }
                        sensorsGridItem.setFrequency(frequency);
                        editor.putInt(sensorsGridItem.getSensorType().code() + getContext().getString(R.string.preference_sensor_frequency_suffix), sensorsGridItem.getFrequency());
                    }
                }
            }
            adapterSensorsGrid.notifyDataSetChanged();
        }
        editor.apply();
    }

    protected abstract void initUI();


}
