package com.bcil.demoassettrack.custom;

import android.content.Context;
import android.graphics.Typeface;
import android.util.AttributeSet;

/**
 * Created by NG on 01-Feb-2018.
 */

public class CustomTextViewThin extends android.support.v7.widget.AppCompatTextView {

  public CustomTextViewThin(Context context, AttributeSet attrs, int defStyle) {
    super(context, attrs, defStyle);
    init();
  }

  public CustomTextViewThin(Context context, AttributeSet attrs) {
    super(context, attrs);
    init();
  }

  public CustomTextViewThin(Context context) {
    super(context);
    init();
  }

  private void init() {
    if (!isInEditMode()) {
      Typeface tf = Typeface.createFromAsset(getContext().getAssets(), "Gotham-Book.otf");
      setTypeface(tf);
    }

  }


}


