package com.securityandsafetythings.examples.helloworld.interfaces;

import android.graphics.Bitmap;
import android.util.Pair;
import com.google.mlkit.vision.barcode.common.Barcode;

import java.util.List;

public interface OnQRTranslateListener {
    void setResult(Pair<Bitmap, List<Barcode>> result);

}
