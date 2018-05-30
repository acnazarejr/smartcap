package com.ssig.smartcap.mobile.adapter;

import android.content.Context;
import android.graphics.Typeface;
import android.hardware.Sensor;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SwitchCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;


import com.github.ivbaranov.mli.MaterialLetterIcon;
import com.ssig.smartcap.mobile.R;
import com.ssig.smartcap.mobile.model.SensorListItem;
import com.ssig.smartcap.mobile.utils.Tools;
import com.ssig.smartcap.mobile.utils.ViewAnimation;
import com.warkiz.widget.IndicatorSeekBar;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.List;



public class AdapterListSensor extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    public class SensorViewHolder extends RecyclerView.ViewHolder {

        public View layout;
        public View layoutExpand;
        public View layoutTitle;

        public MaterialLetterIcon icon;
        public TextView sensorType;
        public ImageButton buttonExpand;
        public SwitchCompat sensorEnabled;

        public TextView sensorModel;
        public TextView sensorVendor;
        public TextView sensorPower;
        public TextView sensorMaxRange;
        public TextView sensorResolution;
        public IndicatorSeekBar sensorFrequency;
        public TextView sensorMaxFrequency;
        public TextView sensorMinFrequency;
        public LinearLayout frequencyNotAvailable;
        public LinearLayout frequencyAvailable;
        public TextView frequencyNotAvailableText;



        public SensorViewHolder(View v) {
            super(v);

            layout = v.findViewById(R.id.lyt_parent);
            layoutTitle = v.findViewById(R.id.lyt_title);
            layoutExpand = v.findViewById(R.id.lyt_expand);

            icon = v.findViewById(R.id.icon);
            sensorType = v.findViewById(R.id.name);
            buttonExpand = v.findViewById(R.id.bt_expand);
            sensorEnabled = v.findViewById(R.id.sensor_enable);

            sensorModel = v.findViewById(R.id.sensor_model);
            sensorVendor = v.findViewById(R.id.sensor_vendor);
            sensorPower = v.findViewById(R.id.sensor_power);
            sensorMaxRange = v.findViewById(R.id.sensor_max_range);
            sensorResolution = v.findViewById(R.id.sensor_resolution);

            sensorFrequency = v.findViewById(R.id.sensor_frequency);
            sensorMaxFrequency = v.findViewById(R.id.sensor_max_frequency);
            sensorMinFrequency = v.findViewById(R.id.sensor_min_frequency);
            frequencyAvailable = v.findViewById(R.id.frequency_available);
            frequencyNotAvailable = v.findViewById(R.id.frequency_not_available);
            frequencyNotAvailableText = v.findViewById(R.id.frequency_not_available_text);
        }
    }


    private List<SensorListItem> sensors;
    private Context ctx;

    public AdapterListSensor(Context context, List<SensorListItem> sensors) {
        this.sensors = sensors;
        this.ctx = context;
    }


    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        RecyclerView.ViewHolder viewHolder;
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_sensor, parent, false);
        viewHolder = new SensorViewHolder(view);
        return viewHolder;
    }

    // Replace the contents of a view (invoked by the layout manager)
    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, final int position) {

        if (holder instanceof SensorViewHolder) {

            final SensorViewHolder view = (SensorViewHolder) holder;
            final SensorListItem sensorListItem = sensors.get(position);

            // CONFIGURE TITLE LAYOUT
            this.displayTextIcon(ctx, view.icon, sensorListItem.getSensorType().abbrev(), sensorListItem.enabled);
            view.sensorType.setText(sensorListItem.getSensorType().toString());

            // CONFIGURE EXPAND LAYOUT
            view.sensorModel.setText(sensorListItem.getModel() + " (" + "v" + String.valueOf(sensorListItem.getVersion()) + ")");
            view.sensorVendor.setText(sensorListItem.getVendor());

            DecimalFormatSymbols decimalFormatSymbols = new DecimalFormatSymbols();
            decimalFormatSymbols.setDecimalSeparator('.');
            DecimalFormat decimalFormat = new DecimalFormat("0.00", decimalFormatSymbols);
            String maxRange = decimalFormat.format(sensorListItem.getMaximumRange());
            String sensorUnit = sensorListItem.getSensorType().unit();
            if (sensorUnit != null){
                maxRange += " " + sensorUnit;
            }
            view.sensorPower.setText(decimalFormat.format(sensorListItem.getPower()) + " mA");
            view.sensorMaxRange.setText(maxRange);
            view.sensorResolution.setText(String.valueOf(sensorListItem.getResolution()));

            if ((sensorListItem.getReportingMode() == Sensor.REPORTING_MODE_CONTINUOUS || sensorListItem.getReportingMode() == Sensor.REPORTING_MODE_ON_CHANGE) && (sensorListItem.getMaxFrequency() > sensorListItem.getMinFrequency()) ){
                view.sensorMinFrequency.setText(String.valueOf(sensorListItem.getMinFrequency())  + " Hz");
                view.sensorMaxFrequency.setText(String.valueOf(sensorListItem.getMaxFrequency())  + " Hz");
                view.sensorFrequency.getBuilder()
                        .setMin(sensorListItem.getMinFrequency())
                        .setMax(sensorListItem.getMaxFrequency())
                        .setProgress(sensorListItem.getDefaultFrequency())
                        .apply();
            }else{
                view.frequencyAvailable.setVisibility(View.GONE);
                view.frequencyNotAvailable.setVisibility(View.VISIBLE);
                if (sensorListItem.getReportingMode() == Sensor.REPORTING_MODE_CONTINUOUS || sensorListItem.getReportingMode() == Sensor.REPORTING_MODE_ON_CHANGE){
                    view.frequencyNotAvailableText.setText("Fixed frequency of " + String.valueOf(sensorListItem.getMinFrequency()) + " Hz");
                } else {
                    view.frequencyNotAvailableText.setText("Not Available");
                }
            }



            view.layoutTitle.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    boolean show = toggleLayoutExpand(!sensorListItem.expanded, view.buttonExpand, view.layoutExpand);
                    sensors.get(position).expanded = show;
                }
            });

            view.buttonExpand.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    boolean show = toggleLayoutExpand(!sensorListItem.expanded, view.buttonExpand, view.layoutExpand);
                    sensors.get(position).expanded = show;
                }
            });

            view.sensorEnabled.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    displayTextIcon(ctx, view.icon, sensorListItem.getSensorType().abbrev(), isChecked);
                    sensors.get(position).expanded = isChecked;
                }
            });


            // void recycling view
            if(sensorListItem.expanded){
                view.layoutExpand.setVisibility(View.VISIBLE);
            } else {
                view.layoutExpand.setVisibility(View.GONE);
            }
            Tools.toggleArrow(sensorListItem.expanded, view.buttonExpand, false);

        }
    }

    private void displayTextIcon(Context ctx, MaterialLetterIcon icon, String text, boolean enabled){
        icon.setLetterSize(14);
        icon.setLetterTypeface(Typeface.defaultFromStyle(Typeface.BOLD));
        if (enabled)
            icon.setShapeColor(ContextCompat.getColor(ctx, R.color.green_600));
        else
            icon.setShapeColor(ContextCompat.getColor(ctx, R.color.red_600));
        icon.setLettersNumber(3);
        icon.setLetter(text);
    }

    private boolean toggleLayoutExpand(boolean show, View view, View lyt_expand) {
        Tools.toggleArrow(show, view);
        if (show) {
            ViewAnimation.expand(lyt_expand);
        } else {
            ViewAnimation.collapse(lyt_expand);
        }
        return show;
    }

    @Override
    public int getItemCount() {
        return sensors.size();
    }

}