package com.wis.bean;

/**
 * Created by ybbz on 16/8/28.
 */
public class Person {
    public int _id;
    public String name;
    public byte[] image;
    public String feature;

    public Person() {
    }

    public Person(String name, byte[] image, String feature) {
        this.name = name;
        this.image = image;
        this.feature = feature;
    }
}
