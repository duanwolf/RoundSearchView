package com.sensetime.searchview;

import android.animation.IntEvaluator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.inputmethodservice.Keyboard;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.Nullable;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.lang.reflect.Field;

/**
 * TODO: document your custom view class.
 */
public class RoundCenterSearchView extends RelativeLayout {
    private String hint;
    private int textColor = Color.RED;
    private float testSize;
    private Drawable searchIcon;
    private int boundWidth;
    private int boundColor;
    private String text;
    private int innerColor;
    private float mTextWidth;
    private float mTextHeight;
    private int startWidth;
    private EditText content;
    private boolean expland = false;
    private int radius;

    public RoundCenterSearchView(Context context) {
        super(context);
        initEditText(context);
        init(null, 0);
    }

    public RoundCenterSearchView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initEditText(context);
        init(attrs, 0);
    }

    public RoundCenterSearchView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        initEditText(context);
        init(attrs, defStyle);
    }

    private void initEditText(Context context) {
        content = new EditText(context);
        content.setImeOptions(EditorInfo.IME_ACTION_SEARCH);
        content.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                    InputMethodManager imm = (InputMethodManager) v
                            .getContext().getSystemService(
                                    Context.INPUT_METHOD_SERVICE);
                    if (imm.isActive()) {
                        imm.hideSoftInputFromWindow(
                                v.getApplicationWindowToken(), 0);
                    }
                    return true;
                }
                return false;
            }
        });
        content.setSingleLine();
        int paddingLeft = getPaddingLeft();
        int paddingRight = getPaddingRight();
        int paddingTop = getPaddingTop();
        int paddingBottom = getPaddingBottom();
        LayoutParams layoutParams = new LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.MATCH_PARENT);
        layoutParams.addRule(RelativeLayout.CENTER_IN_PARENT);
        layoutParams.leftMargin = paddingLeft;
        layoutParams.rightMargin = paddingRight;
        layoutParams.topMargin = paddingTop;
        layoutParams.bottomMargin = paddingBottom;
        addView(content, layoutParams);
        startWidth = content.getMeasuredWidth();
    }

    private void init(AttributeSet attrs, int defStyle) {
        // Load attributes
        final TypedArray a = getContext().obtainStyledAttributes(
                attrs, R.styleable.RoundCenterSearchView, defStyle, 0);

        if (a.hasValue(R.styleable.RoundCenterSearchView_hint)) {
            hint = a.getString(R.styleable.RoundCenterSearchView_hint);
        } else {
            hint = "搜索";
        }

        textColor = a.getColor(R.styleable.RoundCenterSearchView_textColor, Color.GRAY);

        if (a.hasValue(R.styleable.RoundCenterSearchView_text)) {
            text = a.getString(R.styleable.RoundCenterSearchView_text);
        }

        if (a.hasValue(R.styleable.RoundCenterSearchView_yourSearchIcon)) {
            searchIcon = a.getDrawable(R.styleable.RoundCenterSearchView_yourSearchIcon);
        } else {
            searchIcon = getResources().getDrawable(android.R.drawable.ic_menu_search);
        }

        boundColor = a.getColor(R.styleable.RoundCenterSearchView_boundColor, 0xfff);

        boundWidth = a.getDimensionPixelSize(R.styleable.RoundCenterSearchView_boundWidth, 2);

        testSize = a.getDimension(R.styleable.RoundCenterSearchView_textSize, 13);

        radius = a.getDimensionPixelSize(R.styleable.RoundCenterSearchView_radius, 5);

        innerColor = a.getColor(R.styleable.RoundCenterSearchView_innerColor, getSolidColor());

        a.recycle();

        // Set up a default TextPaint object
        // Update TextPaint and text measurements from attributes
        content.setCompoundDrawablesWithIntrinsicBounds(searchIcon, null, null, null);
        content.setHint(hint);
        content.setTextColor(textColor);
        content.setTextSize(testSize);
        content.setBackground(null);
        content.setOnTouchListener(new OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                float x = event.getRawX();
                if (x > content.getRight() - content.getCompoundDrawables()[2].getBounds().width() * 2) {
                    content.setText("");
                }
                return true;
            }
        });
        content.setOnFocusChangeListener(new OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {
                    if (!expland)
                        explandAnimation();
                }
            }
        });
        try {
            Field f = TextView.class.getDeclaredField("mCursorDrawableRes");
            f.setAccessible(true);
            f.set(content, R.drawable.round_search_view_cursor);
        } catch (Exception e) {
            e.printStackTrace();
        }
        setFocusable(true);
        setFocusableInTouchMode(true);
        GradientDrawable drawable = new GradientDrawable();
        drawable.setCornerRadius(radius);
        drawable.setStroke(boundWidth, boundColor);
        drawable.setColor(innerColor);
        setBackground(drawable);
    }

    private void explandAnimation() {
        final int width = getMeasuredWidth() - getPaddingLeft() - getPaddingRight();
        ValueAnimator valueAnimator = ValueAnimator.ofInt(startWidth, width);
        valueAnimator.setDuration(200);
        valueAnimator.setInterpolator(new AccelerateDecelerateInterpolator());
        valueAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                float fraction = animation.getAnimatedFraction();
                IntEvaluator evaluator = new IntEvaluator();
                int nowWidth = evaluator.evaluate(fraction, startWidth, width);
                ViewGroup.LayoutParams layoutParams = content.getLayoutParams();
                layoutParams.width = nowWidth;
                content.requestLayout();
            }
        });
        valueAnimator.setTarget(content);
        valueAnimator.start();
        content.requestFocus();
        Drawable closeIcon = getResources().getDrawable(R.mipmap.ic_action_close);
        content.setCompoundDrawablesWithIntrinsicBounds(searchIcon, null,
                getResources().getDrawable(R.mipmap.ic_action_close), null);
        expland = true;
    }

    private void collsAnimation() {
        final int width = getMeasuredWidth() - getPaddingLeft() - getPaddingRight();
        ValueAnimator valueAnimator = ValueAnimator.ofInt(width, startWidth);
        valueAnimator.setDuration(200);
        valueAnimator.setInterpolator(new AccelerateDecelerateInterpolator());
        valueAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                float fraction = animation.getAnimatedFraction();
                IntEvaluator evaluator = new IntEvaluator();
                int nowWidth = evaluator.evaluate(fraction, width, startWidth);
                ViewGroup.LayoutParams layoutParams = content.getLayoutParams();
                layoutParams.width = nowWidth;
                content.requestLayout();
            }
        });
        valueAnimator.setTarget(content);
        valueAnimator.start();
        content.clearFocus();
        content.setCompoundDrawablesWithIntrinsicBounds(searchIcon, null, null, null);
        content.setHint(hint);
        expland = false;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        // TODO: consider storing these as member variables to reduce
        // allocations per draw cycle.

    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        int widthMeasureSpecMode = MeasureSpec.getMode(widthMeasureSpec);
        int heightMeasureSpecMode = MeasureSpec.getMode(heightMeasureSpec);
        Paint p = new Paint();
        p.setTextSize(testSize);

        Rect rect = new Rect();
        p.getTextBounds("搜索", 0, 2, rect);
        float width = rect.width();
        float height = rect.height();
        if (widthMeasureSpecMode == MeasureSpec.AT_MOST && heightMeasureSpecMode == MeasureSpec.AT_MOST) {
            setMeasuredDimension((int) (width + searchIcon.getIntrinsicWidth()), (int) height);
        } else if (widthMeasureSpecMode == MeasureSpec.AT_MOST) {
            setMeasuredDimension((int) (width + searchIcon.getIntrinsicWidth()), MeasureSpec.getSize(heightMeasureSpec));
        } else if (heightMeasureSpecMode == MeasureSpec.AT_MOST) {
            setMeasuredDimension(MeasureSpec.getSize(widthMeasureSpec),
                    Math.max(searchIcon.getIntrinsicHeight(), MeasureSpec.getSize(heightMeasureSpec)));
        }
    }

    public String getText() {
        return content.getText().toString();
    }

    public void setText(String text) {
        content.setText(text);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            if (!expland) {
                explandAnimation();
                InputMethodManager methodManager = (InputMethodManager)
                        getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
                methodManager.showSoftInput(content, 0);
            } else {

            }
            return true;
        }
        return false;
    }

    public void addOnTextChangerListener(TextWatcher watcher) {
        content.addTextChangedListener(watcher);
    }

    @Nullable
    @Override
    protected Parcelable onSaveInstanceState() {
        SaveState ss = new SaveState(super.onSaveInstanceState());
        ss.searchText = content.getText().toString();
        return ss;
    }

    static class SaveState extends BaseSavedState {

        public static final Creator<SaveState> CREATOR = new Creator<SaveState>() {

            @Override
            public SaveState createFromParcel(Parcel source) {
                return new SaveState(source);
            }

            @Override
            public SaveState[] newArray(int size) {
                return new SaveState[size];
            }
        };
        String searchText;


        public SaveState(Parcel source) {
            super(source);
            searchText = source.readString();
        }

        public SaveState(Parcelable superState) {
            super(superState);
        }

        @Override
        public void writeToParcel(Parcel out, int flags) {
            super.writeToParcel(out, flags);
            out.writeString(searchText);
        }
    }

/*    @Override
    protected void onRestoreInstanceState(Parcelable state) {
        SaveState ss = (SaveState) state;
        super.onRestoreInstanceState(ss.getSuperState());
        String str = ss.searchText;
        if (!TextUtils.isEmpty(str)) {
            final int width = getMeasuredWidth() - getPaddingLeft() - getPaddingRight();
            ViewGroup.LayoutParams params = content.getLayoutParams();
            params.width = width;
            content.setCompoundDrawablesWithIntrinsicBounds(R.mipmap.ic_search_icon, 0,0,0);
            content.invalidate();
        } else {
            content.requestFocus();
            content.setCompoundDrawablesWithIntrinsicBounds(R.mipmap.ic_search_icon, 0,0,0);
            content.setHint("搜索");
            content.invalidate();
        }
    }*/
}
