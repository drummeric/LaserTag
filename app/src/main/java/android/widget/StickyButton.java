package android.widget;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;

public class StickyButton extends Button {

    private Drawable defaultBackground;

    public StickyButton(Context context) {
        super(context);
        defaultBackground = this.getBackground();
    }

    public StickyButton(Context context, AttributeSet attrs) {
        super(context, attrs);
        defaultBackground = this.getBackground();
    }

    public StickyButton(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        defaultBackground = this.getBackground();
    }

    public StickyButton(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        defaultBackground = this.getBackground();
    }

    public void setPressed(){
        this.setBackgroundColor(Color.GRAY);
    }

    public void reset(){
        this.setBackground(defaultBackground);
    }


}
