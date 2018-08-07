package com.example.jake1.designproject;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.View;

import java.io.IOException;

public class NavView extends View {

    private Paint blue_paintbrush_stroke;
    private Paint blue_paintbrush_blur_stroke;
    private Path path;

    private Bitmap mBitmap;
    private int mImageWidth;
    private int mImageHeight;
    private float mScaleFactor = 1.0f;

    private double[] mCoordinates;
    private double[] mFloor1;
    private double[] mFloor2;
    private double[] mFloor3;

    private int mFloorPath = 0;

    private Uri uriFloor1 = Uri.parse("android.resource://com.example.jake1.designproject/drawable/gr2");
    private Uri uriFloor2 = Uri.parse("android.resource://com.example.jake1.designproject/drawable/gr3");
    private Uri uriFloor3 = Uri.parse("android.resource://com.example.jake1.designproject/drawable/gr4");

    public NavView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        path = new Path();

        if (mCoordinates != null) {

            if (mFloorPath == 0 && mFloor1 != null) {
                makePath(mFloor1);
            }
            else if (mFloorPath == 1 && mFloor2 != null) {
                makePath(mFloor2);
            }
            else if (mFloorPath == 2 && mFloor3 != null) {
                makePath(mFloor3);
            }

        }

        if (mBitmap != null) {
            canvas.save();
            canvas.scale(mScaleFactor, mScaleFactor);
            canvas.drawBitmap(mBitmap, 0, 0, null);

            if (!path.isEmpty()) {

                blue_paintbrush_stroke = new Paint();
                blue_paintbrush_stroke.setColor(getResources().getColor(R.color.colorPath));
                blue_paintbrush_stroke.setStyle(Paint.Style.STROKE);
                blue_paintbrush_stroke.setStrokeCap(Paint.Cap.ROUND);
                blue_paintbrush_stroke.setStrokeWidth(10);

                blue_paintbrush_blur_stroke = new Paint();
                blue_paintbrush_blur_stroke.setColor(getResources().getColor(R.color.colorPathBlur));
                blue_paintbrush_blur_stroke.setStyle(Paint.Style.STROKE);
                blue_paintbrush_blur_stroke.setStrokeCap(Paint.Cap.ROUND);
                blue_paintbrush_blur_stroke.setStrokeWidth(20);

                canvas.drawPath(path, blue_paintbrush_stroke);
                canvas.drawPath(path, blue_paintbrush_blur_stroke);

            }

            canvas.restore();
        }

    }

    public void loadMap() {

        Uri uriMap = uriFloor1;

        Bitmap bitmap = null;
        try {
            bitmap = MediaStore.Images.Media.getBitmap(getContext().getContentResolver(), uriMap);
        }
        catch (IOException e) {
            e.printStackTrace();
        }


        mBitmap = bitmap.createScaledBitmap(bitmap, mImageWidth, mImageHeight, false);
        invalidate();

    }

    public void setImageWidth(int clNavMapWidth) {

        mImageWidth = clNavMapWidth;

    }

    public void setImageHeight(int clNavMapHeight) {

        mImageHeight = clNavMapHeight;

    }

    public void makePath( double[] coordinates){

        path.moveTo((float) coordinates[0], (float) coordinates[1]);

        for (int i = 2; i < coordinates.length; i += 2) {

            path.lineTo((float) coordinates[i], (float) coordinates[i+1]);
            path.moveTo((float) coordinates[i], (float) coordinates[i+1]);

        }

    }

    public void displayPath(double[] coordinates) {

        mCoordinates = coordinates;

        if (mCoordinates != null && mCoordinates.length % 3 == 0) {

            double feetX = 292.85106426594839358758271385385;
            double feetY = 263.82978762698053476358803049896;
            double metersX = 89.261004388261071085;
            double metersY = 80.415319268703669309;
            double bitmapX = mBitmap.getWidth();
            double bitmapY = mBitmap.getHeight();

            for (int i = 1; i < mCoordinates.length; i += 3) {

                mCoordinates[i] = mCoordinates[i] - 763696.698424;
                mCoordinates[i + 1] = (mCoordinates[i + 1] - 2147993.369232) * (-1);

                mCoordinates[i] = (mCoordinates[i] * bitmapX * 1.15) / metersX;
                mCoordinates[i + 1] = (mCoordinates[i + 1] * bitmapY * 1.07) / metersY;

            }

            int count1 = 0;
            int count2 = 0;
            int count3 = 0;
            double[] floor1 = null;
            double[] floor2 = null;
            double[] floor3 = null;

            for (int i = 0; i < mCoordinates.length; i += 3) {

                if (mCoordinates[i] == 0) {
                    count1++;
                }
                else if (mCoordinates[i] == 3.6576000000059139) {
                    count2++;
                }
                else if (mCoordinates[i] == 7.315199999997276) {
                    count3++;
                }

            }

            if (count1 != 0) {
                floor1 = new double[count1 * 2];
            }
            if (count2 != 0) {
                floor2 = new double[count2 * 2];
            }
            if (count3 != 0) {
                floor3 = new double[count3 * 2];
            }

            int placeHolder1 = 0;
            int placeHolder2 = 0;
            int placeHolder3 = 0;

            for (int i = 0; i < mCoordinates.length; i += 3) {

                if (mCoordinates[i] == 0) {
                    floor1[placeHolder1] = mCoordinates[i + 1];
                    floor1[placeHolder1 + 1] = mCoordinates[i + 2];
                    placeHolder1 += 2;
                }
                else if (mCoordinates[i] == 3.6576000000059139) {
                    floor2[placeHolder2] = mCoordinates[i + 1];
                    floor2[placeHolder2 + 1] = mCoordinates[i + 2];
                    placeHolder2 += 2;
                }
                else if (mCoordinates[i] == 7.315199999997276) {
                    floor3[placeHolder3] = mCoordinates[i + 1];
                    floor3[placeHolder3 + 1] = mCoordinates[i + 2];
                    placeHolder3 += 2;
                }

            }

            mFloor1 = floor1;
            mFloor2 = floor2;
            mFloor3 = floor3;

        }
        invalidate();

    }

    public void setFloorPath(int floorPath) {

        mFloorPath = floorPath;

    }

}
