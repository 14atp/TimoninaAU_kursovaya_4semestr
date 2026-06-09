package com.myapp.guidbuild.data.database;

public class GuideBlock {
    public static final int TYPE_TEXT = 0;
    public static final int TYPE_IMAGE = 1;

    public int type;
    public String text;     // для текстового блока
    public String imagePath; // для блока с картинкой

    public GuideBlock(int type, String text, String imagePath) {
        this.type = type;
        this.text = text;
        this.imagePath = imagePath;
    }
}