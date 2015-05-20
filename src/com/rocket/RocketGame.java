package com.rocket;

import com.jcasey.R;

import android.app.Activity;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.Window;
import android.widget.Button;

public class RocketGame extends Activity {
	private GameLoop gameLoop;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.main); // set the content view or our widget
										// lookups will fail

		gameLoop = (GameLoop) findViewById(R.id.gameLoop);

		final Button btnRestart = (Button) findViewById(R.id.btnRestart);
		btnRestart.setOnClickListener(new OnClickListener() {
			@Override
			//Reset the game when click the Restart button
			public void onClick(View v) {
				gameLoop.reset();
				gameLoop.invalidate();
			}
		});
		final Button btnLeft = (Button) findViewById(R.id.btnLeft);
		btnLeft.setOnTouchListener(new OnTouchListener() {
			//Fire the left thruster when touch the button and stop when release.
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				int action = event.getActionMasked();
				switch (action) {

				case MotionEvent.ACTION_DOWN:
					gameLoop.leftPressed = true;
					break;
				case MotionEvent.ACTION_UP:
					gameLoop.leftPressed = false;
					break;
				}
				return false;
			}
		});
		final Button btnRight = (Button) findViewById(R.id.btnRight);
		btnRight.setOnTouchListener(new OnTouchListener() {
			//Fire the right thruster when touch the button and stop when release.
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				int action = event.getActionMasked();
				switch (action) {

				case MotionEvent.ACTION_DOWN:
					gameLoop.rightPressed = true;
					break;
				case MotionEvent.ACTION_UP:
					gameLoop.rightPressed = false;
					break;
				}
				return false;
			}
		});
		final Button btnMain = (Button) findViewById(R.id.btnMain);
		btnMain.setOnTouchListener(new OnTouchListener() {
			//Fire the main rocket when touch the button and stop when release.
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				int action = event.getActionMasked();
				switch (action) {

				case MotionEvent.ACTION_DOWN:
					gameLoop.mainPressed = true;
					
					break;
				case MotionEvent.ACTION_UP:
					gameLoop.mainPressed = false;
					break;
				}
				return false;
			}
		});
	}
}