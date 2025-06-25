package com.example.pertemuan5_shafa;

public class GempaModel {
    String tanggal, jam, magnitude, wilayah;
    double lat, lon;
    private double jarakKeUser;
    public GempaModel(String tanggal, String jam, String magnitude, String wilayah, double lat, double lon) {
        this.tanggal = tanggal;
        this.jam = jam;
        this.magnitude = magnitude;
        this.wilayah = wilayah;
        this.lat = lat;
        this.lon = lon;
    }

    public String getTanggal() {
        return tanggal;
    }

    public String getJam() {
        return jam;
    }

    public String getMagnitude() {
        return magnitude;
    }

    public String getWilayah() {
        return wilayah;
    }

    public double getLat() {
        return lat;
    }

    public double getLon() {
        return lon;
    }

    public void setJarakKeUser(double jarakKeUser) {
        this.jarakKeUser = jarakKeUser;
    }
    public double getJarakKeUser (){
        return jarakKeUser;
    }

}
