package com.musat.smarthome;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class apiData {
    private boolean state;
    private String error_MSG;
    private String rx_time;

    private String sunrise_time;
    private String sunset_time;
    private String weather_data;
    private String temp_data;

    private String fire_data;
    private String gas_data;
    private String noise_data;

    private String solar_data;
    private String battery_data;


    public boolean get_state() {
        return state;
    }
    public String get_error_MSG() {
        return error_MSG;
    }
    public String get_rx_time() {
        return rx_time;
    }

    public String get_sunrise_time() {
        return sunrise_time;
    }
    public String get_sunset_time() {
        return sunset_time;
    }
    public String get_weather_data() {
        return weather_data;
    }
    public String get_temp_data() {
        return temp_data;
    }

    public String get_fire_data() {
        return fire_data;
    }
    public String get_gas_data() {
        return gas_data;
    }
    public String get_noise_data() {
        return noise_data;
    }

    public String get_solar_data() {
        return solar_data;
    }
    public String get_battery_data() {
        return battery_data;
    }


    public void set_state(boolean bool_data) {
        this.state = bool_data;
    }
    public void set_error_MSG(String text_data) {
        this.error_MSG = text_data;
    }
    public void set_rx_time(String text_data) {
        this.rx_time = text_data;
    }

    public void set_sunrise_time(String text_data) {
        this.sunrise_time = text_data;
}
    public void set_sunset_time(String text_data) {
        this.sunset_time = text_data;
    }
    public void set_weather_data(String text_data) {
        this.weather_data = text_data;
    }
    public void set_temp_data(String text_data) {
        this.temp_data = text_data;
    }

    public void set_fire_data(String text_data) {
        this.fire_data = text_data;
    }
    public void set_gas_data(String text_data) {
        this.gas_data = text_data;
    }
    public void set_noise_data(String text_data) {
        this.noise_data = text_data;
    }

    public void set_solar_data(String text_data) {
        this.solar_data = text_data;
    }
    public void set_battery_data(String text_data) {
        this.battery_data = text_data;
    }


    public String set_Data(String text_data) {
        SimpleDateFormat old_Format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        SimpleDateFormat new_Format = new SimpleDateFormat("a hh:mm");

        try {
            Date old_date = old_Format.parse(text_data);
            String new_date = new_Format.format(old_date);
            new_date = new_date.replace("AM", "오전");
            new_date = new_date.replace("PM", "오후");
            return new_date;
        } catch (ParseException e) {
            e.printStackTrace();
            return "";
        }
    }
}
