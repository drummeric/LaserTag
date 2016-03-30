package android.widget;

import android.content.Context;
import android.util.AttributeSet;

import com.taserlag.lasertag.R;

public class StickyButton extends Button {

    private boolean stuck = false;

    public StickyButton(Context context) {
        super(context);
    }

    public StickyButton(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public StickyButton(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public StickyButton(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    public boolean isStuck() {
        return stuck;
    }

    public void setPressed(){
        stuck = true;
        this.setBackground(getResources().getDrawable(R.drawable.greenrectangle));
    }

    public void reset(){
        stuck = false;
        this.setBackground(getResources().getDrawable(R.drawable.rectangle));
    }

}
