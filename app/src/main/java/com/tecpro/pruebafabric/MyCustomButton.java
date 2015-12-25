package com.tecpro.pruebafabric;

import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.Typeface;
import android.os.Build;
import android.util.AttributeSet;
import android.widget.Button;

/**
 * Created by jjaimez on 5/12/15.
 */
public class MyCustomButton extends Button {

    public MyCustomButton(Context context) {
        super(context);
        this.setTypeface(Typeface.createFromAsset(context.getAssets(), "fonts/brandon.ttf"));
        this.setTransformationMethod(null);
    }

    public MyCustomButton(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.setTypeface(Typeface.createFromAsset(context.getAssets(), "fonts/brandon.ttf"));
        this.setTransformationMethod(null);
    }

    public MyCustomButton(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        this.setTypeface(Typeface.createFromAsset(context.getAssets(), "fonts/brandon.ttf"));
        this.setTransformationMethod(null);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public MyCustomButton(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        this.setTypeface(Typeface.createFromAsset(context.getAssets(), "fonts/brandon.ttf"));
        this.setTransformationMethod(null);
    }
}
