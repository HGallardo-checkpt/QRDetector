package com.securityandsafetythings.examples.helloworld.pojos;

import android.graphics.Bitmap;
import android.graphics.RectF;
import android.util.Pair;

import java.util.ArrayList;
import java.util.List;

public class DetectionResult {
   public Bitmap bitmap;
   public ArrayList<Pair<RectF,String>> detectionResult ;
}
