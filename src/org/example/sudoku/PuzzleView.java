package org.example.sudoku;

import android.view.View;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.Paint.FontMetrics;
import android.graphics.Paint.Style;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.animation.AnimationUtils;

public class PuzzleView extends View {
	private static final String TAG = "Sudoku";
    
	private final Game game;
	
	public PuzzleView(Context context) {
		super(context);
		this.game = (Game) context;
		setFocusable(true);
		setFocusableInTouchMode(true);
	};
	
	private float width;
	private float height;
	private int selX;
	private int selY;
	private final Rect selRect = new Rect();
	
	@Override
	protected void onSizeChanged(int w, int h, int oldw, int oldh) {
		width = w / 9f;
		height = h / 9f;
		getRect(selX, selY, selRect);
		Log.d(TAG, "onSizeChanged: (width, height) = ("+w+", "+h+")" );
		super.onSizeChanged(w, h, oldw, oldh);
	}
	
	private void getRect(int x, int y, Rect rect) {
		rect.set(
				(int) (x*width),       (int) (y*height),
				(int) (x*width+width), (int) (y*height+height)
				);
	}
	
	@Override
	protected void onDraw(Canvas canvas) {
		// draw the background
		Paint background = getPaint(R.color.puzzle_background);
        canvas.drawRect(0, 0, getWidth(), getHeight(), background);
        
        // draw the board
        
        // define the colours
        Paint dark   = getPaint(R.color.puzzle_dark);
        Paint hilite = getPaint(R.color.puzzle_hilite);
        Paint light  = getPaint(R.color.puzzle_light);
        
        // draw the grid lines
        for (int i=0; i<9; i++) {
        	Paint line_color = light;
        	if (i%3 == 0) {
        		// we use dark lines on the major grid
        		line_color = dark;
        	}
        	// horizontal
        	canvas.drawLine(0, i*height,   getWidth(), i*height,   line_color);
        	canvas.drawLine(0, i*height+1, getWidth(), i*height+1, hilite);
        	// vertical
        	canvas.drawLine(i*width,   0, i*width,   getHeight(),  line_color);
        	canvas.drawLine(i*width+1, 0, i*width+1, getHeight(),  hilite);
        };
        
        // draw the numbers
        Paint foreground = new Paint(Paint.ANTI_ALIAS_FLAG);
        setPaintColor(foreground, R.color.puzzle_foreground);
        foreground.setStyle(Style.FILL);
        foreground.setTextSize(height * 0.75f);
        foreground.setTextScaleX(width / height);
        foreground.setTextAlign(Paint.Align.CENTER);
        
        // draw the hints
        // draw the selection
	}
	
	private Paint getPaint(int colour_id) {
		Paint paint = new Paint();
		setPaintColor(paint, colour_id);
        return paint;
	}
	
	private void setPaintColor(Paint paint, int colour_id) {
		paint.setColor(getResources().getColor(colour_id));
	}
}
