package com.example.qrcodescanner;

import com.google.zxing.LuminanceSource;

public class RGBLuminanceSource extends LuminanceSource {
    private final byte[] luminances;

    public RGBLuminanceSource(int width, int height, int[] pixels) {
        super(width, height);

        luminances = new byte[width * height];
        for (int i = 0; i < pixels.length; i++) {
            int pixel = pixels[i];
            int r = (pixel >> 16) & 0xff;
            int g = (pixel >> 8) & 0xff;
            int b = pixel & 0xff;
            luminances[i] = (byte) ((r + g + b) / 3); // grayscale
        }
    }

    @Override
    public byte[] getRow(int y, byte[] row) {
        int width = getWidth();
        if (row == null || row.length < width) {
            row = new byte[width];
        }
        System.arraycopy(luminances, y * width, row, 0, width);
        return row;
    }

    @Override
    public byte[] getMatrix() {
        return luminances;
    }
}
