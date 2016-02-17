package android.widget;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;

public class StickyButton extends Button {

    private Drawable defaultBackground;
    private int stickyColor = Color.GRAY;
    private boolean stuck = false;

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

    public void setStickyColor(int color) {
        this.stickyColor = color;
    }

    public boolean isStuck() {
        return stuck;
    }

    public void setPressed(){
        stuck = true;
        this.setBackgroundColor(stickyColor);
    }

    public void reset(){
        stuck = false;
        this.setBackground(defaultBackground);
    }

}
