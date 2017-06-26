package com.example.gregor.telproof;

/**
 * Created by Gregor on 26-6-2017.
 */

public class BluetoothConnection {
    private String name;
    private int strength;

    public BluetoothConnection(String name, int strength){
        this.name = name;
        this.strength = strength;
    }

    public String getName(){
        return name;
    }

    public int getStrength(){
        return strength;
    }
}
