package org.example.sudoku;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.Paint.FontMetrics;
import android.graphics.Paint.Style;
import android.os.Bundle;
import android.os.Parcelable;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.AnimationUtils;

public class PuzzleView extends View {
	private static final String TAG = "Sudoku";
	
	private static final String SELX = "selX";
	private static final String SELY = "selY";
	private static final String VIEW_STATE = "viewState";
	private static final int ID = 42;
    
	private final Game game;
	
	public PuzzleView(Context context) {
	    super(context);
	    this.game = (Game) context;
	    setFocusable(true);
	    setFocusableInTouchMode(true);
	    
	    puzzle_background_paint = getPaint(R.color.puzzle_background);
		puzzle_dark_paint       = getPaint(R.color.puzzle_dark);
		puzzle_hilite_paint     = getPaint(R.color.puzzle_hilite);
		puzzle_light_paint      = getPaint(R.color.puzzle_light);
		puzzle_selected_paint   = getPaint(R.color.puzzle_selected);
		puzzle_hint_0_paint     = getPaint(R.color.puzzle_hint_0);
		puzzle_hint_1_paint     = getPaint(R.color.puzzle_hint_1);
		puzzle_hint_2_paint     = getPaint(R.color.puzzle_hint_2);
		
		puzzle_foreground_paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        setPaintColor(puzzle_foreground_paint, R.color.puzzle_foreground);
        puzzle_foreground_paint.setStyle(Style.FILL);
        puzzle_foreground_paint.setTextSize(height * 0.75f);
        puzzle_foreground_paint.setTextScaleX(width / height);
        puzzle_foreground_paint.setTextAlign(Paint.Align.CENTER);
        
        // measure the font height in advance
        FontMetrics fm = puzzle_foreground_paint.getFontMetrics();
        font_height = fm.ascent + fm.descent;
        
        // this is used for loading state
        setId(ID);
	}
	
	@Override
	protected Parcelable onSaveInstanceState() {
		Parcelable p = super.onSaveInstanceState();
		Log.d(TAG, "onSaveInstanceState");
		Bundle bundle = new Bundle();
		bundle.putInt(SELX, selX);
		bundle.putInt(SELY, selY);
		bundle.putParcelable(VIEW_STATE, p);
		return bundle;
	}
	
	@Override
	protected void onRestoreInstanceState(Parcelable state) {
		Log.d(TAG, "onRestoreInstanceState");
		Bundle bundle = (Bundle) state;
		select(bundle.getInt(SELX), bundle.getInt(SELY));
		super.onRestoreInstanceState(bundle.getParcelable(VIEW_STATE));
		return ;
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
	
	private Paint puzzle_background_paint;
	private Paint puzzle_dark_paint;
	private Paint puzzle_hilite_paint;
	private Paint puzzle_light_paint;
	private Paint puzzle_foreground_paint;
	private Paint puzzle_selected_paint;
	private Paint puzzle_hint_0_paint;
	private Paint puzzle_hint_1_paint;
	private Paint puzzle_hint_2_paint;
	
	private float font_height;
	
	@Override
	protected void onDraw(Canvas canvas) {
		// draw the background
        canvas.drawRect(0, 0, getWidth(), getHeight(), puzzle_background_paint);
        
        // draw the board
        
        // draw the grid lines
        for (int i=0; i<9; i++) {
        	Paint line_paint = puzzle_light_paint;
        	if (i%3 == 0) {
        		// we use dark lines on the major grid
        		line_paint = puzzle_dark_paint;
        	}
        	// horizontal
        	canvas.drawLine(0, i*height,   getWidth(), i*height,   line_paint);
        	canvas.drawLine(0, i*height+1, getWidth(), i*height+1, puzzle_hilite_paint);
        	// vertical
        	canvas.drawLine(i*width,   0, i*width,   getHeight(),  line_paint);
        	canvas.drawLine(i*width+1, 0, i*width+1, getHeight(),  puzzle_hilite_paint);
        };
        
        // draw the numbers
        
        // draw the number in the box
        
        // centre in X - in the middle
        float x = width / 2;
        
        // centre in Y - take into account the font height
        float y = (height - font_height) / 2;
        
        for (int i=0; i<9; i++) {
        	for (int j=0; j<9; j++) {
        		canvas.drawText(this.game.getTileString(i, j), i*width+x, j*height+y, puzzle_foreground_paint);	
        	}
        }
        
        // draw the hints
        
        if (Prefs.getHints(getContext())) {
	        Paint hint_paints[] = {
	        		puzzle_hint_0_paint,
	        		puzzle_hint_1_paint,
	        		puzzle_hint_2_paint,
	        };
	        
	        Rect r = new Rect();
	        for (int i=0; i<9; i++) {
	        	for (int j=0; j<9; j++) {
	        		int movesleft = 9 - game.getUsedTiles(i, j).length;
	        		if (movesleft < hint_paints.length) {
	        			getRect(i, j, r);
	        			Paint hint = hint_paints[movesleft];
	        			canvas.drawRect(r, hint);
	        		}
	        	}
	        }
        }
        
        // draw the selection
        Log.d(TAG, "selRect="+selRect);
        
        canvas.drawRect(selRect, puzzle_selected_paint);
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
