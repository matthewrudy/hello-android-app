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
	}
	
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
        
        // draw the number in the box
        
        // measure the font height
        FontMetrics fm = foreground.getFontMetrics();
        float font_height = fm.ascent + fm.descent;
        
        // centre in X - in the middle
        float x = width / 2;
        
        // centre in Y - take into account the font height
        float y = (height - font_height) / 2;
        
        for (int i=0; i<9; i++) {
        	for (int j=0; j<9; j++) {
        		canvas.drawText(this.game.getTileString(i, j), i*width+x, j*height+y, foreground);	
        	}
        }
        
        // draw the hints
        int hint_colours[] = {
        		R.color.puzzle_hint_0,
        		R.color.puzzle_hint_1,
        		R.color.puzzle_hint_2,
        };
        
        Rect r = new Rect();
        for (int i=0; i<9; i++) {
        	for (int j=0; j<9; j++) {
        		int movesleft = 9 - game.getUsedTiles(i, j).length;
        		if (movesleft < hint_colours.length) {
        			getRect(i, j, r);
        			Paint hint = getPaint(hint_colours[movesleft]);
        			canvas.drawRect(r, hint);
        		}
        	}
        }
        
        // draw the selection
        Log.d(TAG, "selRect="+selRect);
        
        Paint selected = getPaint(R.color.puzzle_selected);
        canvas.drawRect(selRect, selected);
	}
	
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		Log.d(TAG, "onKeyDown: (keycode, event)=("+keyCode+", "+event+")");
		switch (keyCode) {
		
		// move the selection
		case KeyEvent.KEYCODE_DPAD_UP:
			select(selX, selY-1);
			break;
		case KeyEvent.KEYCODE_DPAD_DOWN:
			select(selX, selY+1);
			break;
		case KeyEvent.KEYCODE_DPAD_LEFT:
			select(selX-1, selY);
			break;
		case KeyEvent.KEYCODE_DPAD_RIGHT:
			select(selX+1, selY);
			break;	
	    
	    // enter a number
		case KeyEvent.KEYCODE_1:
			setSelectedTile(1); break;
		case KeyEvent.KEYCODE_2:
			setSelectedTile(2); break;
		case KeyEvent.KEYCODE_3:
			setSelectedTile(3); break;
		case KeyEvent.KEYCODE_4:
			setSelectedTile(4); break;
		case KeyEvent.KEYCODE_5:
			setSelectedTile(5); break;
		case KeyEvent.KEYCODE_6:
			setSelectedTile(6); break;
		case KeyEvent.KEYCODE_7:
			setSelectedTile(7); break;
		case KeyEvent.KEYCODE_8:
			setSelectedTile(8); break;
		case KeyEvent.KEYCODE_9:
			setSelectedTile(9); break;
			
	    // a zero or space clears the square
		case KeyEvent.KEYCODE_0:
		case KeyEvent.KEYCODE_SPACE:
			setSelectedTile(0); break;
			
		case KeyEvent.KEYCODE_ENTER:
		case KeyEvent.KEYCODE_DPAD_CENTER:
			game.showKeypadOrError(selX, selY);
			break;
	    
		default:
			return super.onKeyDown(keyCode, event);
		}
		return true;
	}
	
	@Override
	public boolean onTouchEvent(MotionEvent event) {
		if (event.getAction() != MotionEvent.ACTION_DOWN)
			return super.onTouchEvent(event);
		
		select(
		  (int) (event.getX() / width),
		  (int) (event.getY() / height)
		  );
	    game.showKeypadOrError(selX, selY);
	    Log.d(TAG, "onTouchEvent: (x, y) = ("+selX+", "+selY+")");
	    return true;
	}
	
	public void setSelectedTile(int tile) {
		if (game.setTileIfValid(selX, selY, tile)) {
			// any change may affect the hints
			invalidate();
		} else {
			// its not valid mate
			Log.d(TAG, "setSelectedTile: invalid - "+tile);
			startAnimation(AnimationUtils.loadAnimation(game, R.anim.shake));
		}
	}
	private void select(int x, int y) {
		invalidate(selRect);
		selX = Math.min(Math.max(x, 0), 8);
		selY = Math.min(Math.max(y, 0), 8);
		getRect(selX, selY, selRect);
		invalidate(selRect);
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
