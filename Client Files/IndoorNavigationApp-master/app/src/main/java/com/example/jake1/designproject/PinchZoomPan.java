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
import android.util.DisplayMetrics;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;
import java.io.IOException;

public class PinchZoomPan extends View {

    private Paint blue_paintbrush_stroke;
    private Paint blue_paintbrush_blur_stroke;
    private Paint blue_paintbrush_fill;
    private Paint blue_paintbrush_blur_fill;
    private Path path;

    private double[] mCoordinates;
    private double[] mFloor1;
    private double[] mFloor2;
    private double[] mFloor3;
    private int mFloorPath;
    private int mStartingFloor;

    private Uri uriFloor1 = Uri.parse("android.resource://com.example.jake1.designproject/drawable/gr2");
    private Uri uriFloor2 = Uri.parse("android.resource://com.example.jake1.designproject/drawable/gr3");
    private Uri uriFloor3 = Uri.parse("android.resource://com.example.jake1.designproject/drawable/gr4");

    private Bitmap mBitmap;
    private int mImageWidth;
    private int mImageHeight;

    private float mPositionX;
    private float mPositionY;
    private float mLastTouchX;
    private float mLastTouchY;

    private static final int INVALID_POINTER_ID = -1;
    private int mActivePointerID = INVALID_POINTER_ID;

    private ScaleGestureDetector mScaleDetector;
    private float mScaleFactor = 1.0f;
    private final static float mMinZoom = 1.0f;
    private final static float mMaxZoom = 5.0f;

    public PinchZoomPan(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);

        mScaleDetector = new ScaleGestureDetector(context, new ScaleListener());

    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {

        //the scale detector should inspect all the touch events
        mScaleDetector.onTouchEvent(event);

        final int action = event.getAction();

        switch (action & MotionEvent.ACTION_MASK) {
            case MotionEvent.ACTION_DOWN: {
                //get x and y cords of where we touch screen
                final float x = event.getX();
                final float y = event.getY();

                //remember where touch event started
                mLastTouchX = x;
                mLastTouchY = y;

                //save the ID of this pointer
                mActivePointerID = event.getPointerId(0);

                break;
            }
            case MotionEvent.ACTION_MOVE: {

                //find the index of the active pointer and fetch its position
                final int pointerIndex = event.findPointerIndex(mActivePointerID);
                final float x = event.getX(pointerIndex);
                final float y = event.getY(pointerIndex);

                if (!mScaleDetector.isInProgress()) {

                    //calculate distance in x and y directions
                    final float distanceX = x - mLastTouchX;
                    final float distanceY = y - mLastTouchY;

                    mPositionX += distanceX;
                    mPositionY += distanceY;

                    //redraw canvas call onDraw method
                    invalidate();
                }

                //remember this touch position for next move event
                mLastTouchX = x;
                mLastTouchY = y;

                break;
            }

            case MotionEvent.ACTION_UP: {

                mActivePointerID = INVALID_POINTER_ID;

                break;
            }

            case MotionEvent.ACTION_CANCEL: {

                mActivePointerID = INVALID_POINTER_ID;

                break;
            }

            case MotionEvent.ACTION_POINTER_UP: {

                //Extract the index of the pointer that left the screen
                final int pointerIndex = (action & MotionEvent.ACTION_POINTER_INDEX_MASK) >>MotionEvent.ACTION_POINTER_INDEX_SHIFT;
                final int pointerId = event.getPointerId(pointerIndex);
                if (pointerId == mActivePointerID) {
                    //our active pointer is going up. Choose another active pointer and adjust
                    final int newPointerIndex = pointerIndex == 0 ? 1 : 0;
                    mLastTouchX = event.getX(newPointerIndex);
                    mLastTouchY = event.getY(newPointerIndex);
                    mActivePointerID = event.getPointerId(newPointerIndex);
                }

                break;
            }
        }

        return true;
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

            if ((mPositionX * -1) < 0) {
                mPositionX = 0;
            }
            else if ((mPositionX * -1) > mImageWidth * mScaleFactor - getWidth()) {
                mPositionX = (mImageWidth * mScaleFactor - getWidth()) * -1;
            }

            if ((mPositionY * -1) < 0) {
                mPositionY = 0;
            }
            else if ((mPositionY * -1) > mImageHeight * mScaleFactor - getHeight()) {
                mPositionY = (mImageHeight * mScaleFactor - getHeight()) * -1;
            }

            if ((mImageHeight * mScaleFactor) < getHeight()) {
                mPositionY = 0;
            }

            canvas.translate(mPositionX, mPositionY);
            canvas.scale(mScaleFactor, mScaleFactor);
            canvas.drawBitmap(mBitmap, 0, 0, null);

            if (!path.isEmpty()) {

                blue_paintbrush_stroke = new Paint();
                blue_paintbrush_stroke.setColor(getResources().getColor(R.color.colorPath));
                blue_paintbrush_stroke.setStyle(Paint.Style.STROKE);
                blue_paintbrush_stroke.setStrokeCap(Paint.Cap.ROUND);
                blue_paintbrush_stroke.setStrokeWidth(20);

                blue_paintbrush_blur_stroke = new Paint();
                blue_paintbrush_blur_stroke.setColor(getResources().getColor(R.color.colorPathBlur));
                blue_paintbrush_blur_stroke.setStyle(Paint.Style.STROKE);
                blue_paintbrush_blur_stroke.setStrokeCap(Paint.Cap.ROUND);
                blue_paintbrush_blur_stroke.setStrokeWidth(35);

                blue_paintbrush_fill = new Paint();
                blue_paintbrush_fill.setColor(getResources().getColor(R.color.colorPath));
                blue_paintbrush_fill.setStyle(Paint.Style.FILL);

                blue_paintbrush_blur_fill = new Paint();
                blue_paintbrush_blur_fill.setColor(getResources().getColor(R.color.colorPathBlur));
                blue_paintbrush_blur_fill.setStyle(Paint.Style.FILL);

                if (mStartingFloor == 0) {

                    if (mFloorPath == 0) {
                        canvas.drawCircle((float) mCoordinates[1], (float) mCoordinates[2], 30, blue_paintbrush_fill);
                        canvas.drawCircle((float) mCoordinates[1], (float) mCoordinates[2], 40, blue_paintbrush_blur_fill);
                    }

                }
                else if (mStartingFloor == 1) {

                    if (mFloorPath == 1) {
                        canvas.drawCircle((float) mCoordinates[1], (float) mCoordinates[2], 30, blue_paintbrush_fill);
                        canvas.drawCircle((float) mCoordinates[1], (float) mCoordinates[2], 40, blue_paintbrush_blur_fill);
                    }

                }
                else if (mStartingFloor == 2) {

                    if (mFloorPath == 2) {
                        canvas.drawCircle((float) mCoordinates[1], (float) mCoordinates[2], 30, blue_paintbrush_fill);
                        canvas.drawCircle((float) mCoordinates[1], (float) mCoordinates[2], 40, blue_paintbrush_blur_fill);
                    }

                }

                canvas.drawPath(path, blue_paintbrush_stroke);
                canvas.drawPath(path, blue_paintbrush_blur_stroke);

            }
            canvas.restore();

        }
    }

    public void loadImageOnCanvas(int floorNum) {

        Uri uriMap = null;

        if (floorNum == 0) {
            uriMap = uriFloor1;
        }
        else if (floorNum == 1) {
            uriMap = uriFloor2;
        }
        else if (floorNum == 2) {
            uriMap = uriFloor3;
        }

        Bitmap bitmap = null;
        try {
            bitmap = MediaStore.Images.Media.getBitmap(getContext().getContentResolver(), uriMap);
        }
        catch (IOException e) {
            e.printStackTrace();
        }

        float aspectRatio = (float) bitmap.getHeight()/(float) bitmap.getWidth();
        DisplayMetrics displayMetrics = getResources().getDisplayMetrics();
        //control how zoomed in map is on startup
        mImageWidth = (int) (displayMetrics.widthPixels + (displayMetrics.widthPixels*0.9));
        mImageHeight = Math.round(mImageWidth * aspectRatio);
        mBitmap = bitmap.createScaledBitmap(bitmap, mImageWidth, mImageHeight, false);
        invalidate();

    }

    private class ScaleListener extends ScaleGestureDetector.SimpleOnScaleGestureListener {
        @Override
        public boolean onScale(ScaleGestureDetector scaleGestureDetector) {

            mScaleFactor *= scaleGestureDetector.getScaleFactor();

            //don't want the image to get too large or small
            mScaleFactor = Math.max(mMinZoom, Math.min(mScaleFactor, mMaxZoom));

            invalidate();

            return true;
        }
    }

    public void makePath( double[] coordinates){

        path.moveTo((float) coordinates[0], (float) coordinates[1]);

        for (int i = 2; i < coordinates.length; i += 2) {

            path.lineTo((float) coordinates[i], (float) coordinates[i+1]);
            path.moveTo((float) coordinates[i], (float) coordinates[i+1]);

        }

    }

    public void popCoordinates(double[] coordinates) {

        if (coordinates != null && coordinates.length % 3 == 0) {

            double feetX = 292.85106426594839358758271385385;
            double feetY = 263.82978762698053476358803049896;
            double metersX = 89.261004388261071085;
            double metersY = 80.415319268703669309;
            double bitmapX = mBitmap.getWidth();
            double bitmapY = mBitmap.getHeight();

            for (int i = 1; i < coordinates.length; i += 3) {

                coordinates[i] = coordinates[i] - 763696.698424;
                coordinates[i + 1] = (coordinates[i + 1] - 2147994.369232) * (-1);

                coordinates[i] = (coordinates[i] * bitmapX * 1.15) / metersX;
                coordinates[i + 1] = (coordinates[i + 1] * bitmapY * 1.05) / metersY;

            }

        }

            mCoordinates = coordinates;

        if (mCoordinates != null && mCoordinates.length % 3 == 0) {

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

            if (mCoordinates[0] == 0) {
                mStartingFloor = 0;
            }
            else if (mCoordinates[0] == 3.6576000000059139) {
                mStartingFloor = 1;
            }
            else if (mCoordinates[0] == 7.315199999997276) {
                mStartingFloor = 2;
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
