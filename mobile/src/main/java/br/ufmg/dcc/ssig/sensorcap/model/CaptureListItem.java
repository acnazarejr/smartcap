package br.ufmg.dcc.ssig.sensorcap.model;

import android.annotation.SuppressLint;
import br.ufmg.dcc.ssig.sensorsmanager.data.CaptureData;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.TimeUnit;

public class CaptureListItem {

    public CaptureData captureData;
    public String captureDataUUID;
    public final boolean closed;

    public final String itemTitle;
    public String itemDuration;
    public final Long itemTimestampLong;
    public String itemTimestampText;
    public boolean itemSmartphoneEnable;
    public boolean itemSmartwatchEnable;
    public String itemDevices = null;

    public final boolean section;


    public CaptureListItem(boolean closedSection){
        this.itemTitle = closedSection ? "Closed Captures" : "Unclosed Captures";
        this.closed = closedSection;
        this.section = true;
        this.itemTimestampLong = -1L;
    }

    @SuppressLint({"SimpleDateFormat", "DefaultLocale"})
    public CaptureListItem(CaptureData captureData){

        this.captureData = captureData;
        this.captureDataUUID = captureData.getCaptureDataUUID();
        this.itemTitle = captureData.getSubjectInfo().getName();

        long duration = captureData.getEndTimestamp() - captureData.getStartTimestamp();
        final long hr = TimeUnit.MILLISECONDS.toHours(duration);
        final long min = TimeUnit.MILLISECONDS.toMinutes(duration - TimeUnit.HOURS.toMillis(hr));
        final long sec = TimeUnit.MILLISECONDS.toSeconds(duration - TimeUnit.HOURS.toMillis(hr) - TimeUnit.MINUTES.toMillis(min));
        this.itemDuration = String.format("%02d:%02d:%02d", hr, min, sec);

        this.itemTimestampLong = captureData.getStartTimestamp();
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss.SSS");
        this.itemTimestampText = simpleDateFormat.format(new Date(captureData.getStartTimestamp()));

        this.itemSmartphoneEnable = captureData.getHostDeviceData() != null;
        this.itemSmartwatchEnable = captureData.getClientDeviceData() != null;

        if (this.itemSmartphoneEnable && this.itemSmartwatchEnable){
            this.itemDevices = "Phone and Wear";
        }else if (this.itemSmartphoneEnable){
            this.itemDevices = "Phone only";
        }else if (this.itemSmartwatchEnable){
            this.itemDevices = "Wear only";
        }

        this.closed = captureData.isClosed();
        this.section = false;


    }

}
