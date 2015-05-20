package com.rocket;

import com.jcasey.R;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.BitmapShader;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Shader;
import android.graphics.drawable.Drawable;
import android.media.MediaPlayer;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.View.OnTouchListener;

public class GameLoop extends SurfaceView implements Runnable,
		SurfaceHolder.Callback, OnTouchListener {

	public static final double INITIAL_TIME = 2;
	static final int REFRESH_RATE = 20;
	static final int GRAVITY = 1;
	static final double INITIAL_FUEL = 3;
	Thread main;

	Paint paint = new Paint();
	Paint landPaint = new Paint();
	Paint crushPaint = new Paint();
	Drawable lander, mainthrust, leftthrust, rightthrust, crush;
	Bitmap moon;
	Bitmap background;
	MediaPlayer landingPlay, crushPlay, thrustersPlay;
	int xcor[], ycor[], landingx, landingw, landingy;

	Canvas offscreen;
	Bitmap buffer;

	boolean mainPressed = false;
	boolean leftPressed = false;
	boolean rightPressed = false;
	boolean gameover = false;
	boolean touchground = false;
	Canvas canvas;

	float x, y;
	int width = 0;

	double t = INITIAL_TIME;
	double fuel = INITIAL_FUEL;

	Path path;

	public GameLoop(Context context) {
		super(context);

		init();
	}

	public GameLoop(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);

		init();
	}

	public GameLoop(Context context, AttributeSet attrs) {
		super(context, attrs);
		// Loading all images and sounds
		lander = getContext().getResources().getDrawable(R.drawable.lander);
		mainthrust = getContext().getResources().getDrawable(
				R.drawable.landermain);
		leftthrust = getContext().getResources().getDrawable(
				R.drawable.landertoright);
		rightthrust = getContext().getResources().getDrawable(
				R.drawable.landertoleft);
		crush = getContext().getResources().getDrawable(R.drawable.crush);
		moon = BitmapFactory.decodeResource(this.getResources(),
				R.drawable.moon);
		landingPlay = MediaPlayer.create(context, R.raw.landing);
		crushPlay = MediaPlayer.create(context, R.raw.crush);
		thrustersPlay = MediaPlayer.create(context, R.raw.thrusters);
		thrustersPlay.setLooping(true);
		init();
	}

	public void init() {
		// Create the terrain
		boolean land = false;
		int yl = 616;
		int xl = 0;
		xcor = new int[19];
		ycor = new int[19];
		xcor[0] = 0;
		ycor[0] = 616;
		path = new Path();
		path.lineTo(0, 616);
		// Create a random terrain
		for (int i = 1; i < 15; i++) {
			int xland = (int) (Math.random() * (100 + 1));
			int yland = (int) (500 + Math.random() * (200 + 1));
			// Create a place for landing
			if (xland > 70 && land == false) {
				yland = yl;
				land = true;
				landingx = xl;
				landingw = xland;
				landingy = yl;
			}
			if (xl < 600) {
				xl = xl + xland;
			} else {
				xl = 600;
			}
			yl = yland;
			path.lineTo(xl, yl);
			xcor[i] = xl;
			ycor[i] = yl;

		}
		// finishing the terrain
		path.lineTo(600, 481);
		xcor[15] = 600;
		ycor[15] = 481;
		path.lineTo(600, 750);
		xcor[16] = 600;
		ycor[16] = 750;
		path.lineTo(0, 750);
		xcor[17] = 0;
		ycor[17] = 750;
		path.lineTo(0, 616);
		xcor[18] = 0;
		ycor[18] = 616;

		setOnTouchListener(this);

		getHolder().addCallback(this);
	}

	@Override
	protected void onSizeChanged(int w, int h, int oldw, int oldh) {
		super.onSizeChanged(w, h, oldw, oldh);

		width = w;

		x = width / 2;
	}

	public void run() {
		while (true) {
			while (!gameover) {
				canvas = null;
				SurfaceHolder holder = getHolder();
				synchronized (holder) {
					canvas = holder.lockCanvas();
					// Draw the background color
					canvas.drawColor(Color.BLACK);
					// set all paint color
					paint.setColor(Color.RED);
					landPaint.setColor(Color.GREEN);
					crushPaint.setColor(Color.BLACK);
					// load the texture for the terrain
					Shader mShader = new BitmapShader(moon,
							Shader.TileMode.REPEAT, Shader.TileMode.MIRROR);
					paint.setShader(mShader);
					// Draw terrain
					canvas.drawPath(path, paint);
					// Draw the fuel Gauge
					canvas.drawRect(20, 30, 20 + (float) fuel * 30, 50,
							landPaint);
					// Draw the landing place
					canvas.drawRect(landingx + 5, landingy, landingx + landingw
							- 5, landingy - 5, landPaint);
					//Check is the bottom of the rocket touch the terrain
					for(int i=0; i<=50; i++){
						boolean bottom = contains(xcor, ycor, x - 25+i, y + 25);
						if (bottom){
							touchground = true;
						}
					}
					boolean landing = landing(xcor, ycor, x, y + 25);
					// If no control button is pressed stop playing thrusters
					// sound
					if (!mainPressed && !leftPressed && !rightPressed
							&& thrustersPlay.isPlaying()) {
						thrustersPlay.pause();
					}
					// If the rocket is touch the terrain
					if (touchground) {
						// If rocket is not landing it will crush
						if (!landing) {
							canvas.drawCircle(x, y - 25, 60, crushPaint);
							crush.setBounds((int) x - 25, (int) y - 25,
									(int) x + 25, (int) y + 25);
							crush.draw(canvas);
							t = INITIAL_TIME; // reset the time variable
							crushPlay.start();
							gameover = true;
						} else {
							// After landing user still can control the rocket
							control();
							lander.setBounds((int) x - 25, (int) y - 25,
									(int) x + 25, (int) y + 25);
							lander.draw(canvas);
							// After landing rocket will be refueled.
							fuel = INITIAL_FUEL;
							landingPlay.start();
							touchground = false;
						}
					} else {
						// Control the rocket by pressing the button
						control();
						// s = ut + 0.5 gt^2

						// not that the initial velocity (u) is zero so I have
						// not
						// put ut into the code below
						y = (int) y + (int) ((0.5 * (GRAVITY * t * t)));

						t = t + 0.01; // increment the parameter for synthetic
										// time
										// by a small amount
						lander.setBounds((int) x - 25, (int) y - 25,
								(int) x + 25, (int) y + 25);
						lander.draw(canvas);
					}
				}

				try {
					Thread.sleep(REFRESH_RATE);
				} catch (Exception e) {
				}

				finally {
					if (canvas != null) {
						holder.unlockCanvasAndPost(canvas);
					}
				}
			}
		}

	}

	// if still have fuel, control the rocket by pressing button
	private void control() {
		if (fuel > 0) {
			if (mainPressed) {
				t = INITIAL_TIME;
				if (y > 0) {
					y = y - 15;
				} else {
					y = 0;
				}
				mainthrust.setBounds((int) x - 25, (int) y - 25, (int) x + 25,
						(int) y + 25);
				mainthrust.draw(canvas);
				thrustersPlay.start();
				fuel = fuel - 0.01;
			} else if (leftPressed) {
				if (x < width) {
					x = x + 10;
				} else {
					x = 0;
				}
				leftthrust.setBounds((int) x - 25, (int) y - 25, (int) x + 25,
						(int) y + 25);
				leftthrust.draw(canvas);
				thrustersPlay.start();
				fuel = fuel - 0.01;
			} else if (rightPressed) {
				if (x > 0) {
					x = x - 10;
				} else {
					x = width;
				}
				rightthrust.setBounds((int) x - 25, (int) y - 25, (int) x + 25,
						(int) y + 25);
				rightthrust.draw(canvas);
				thrustersPlay.start();
				fuel = fuel - 0.01;
			}
		}

	}

	// To see is the rocket at a place can be landed
	private boolean landing(int[] xcor, int[] ycor, double x0, double y0) {
		for (int i = 0; i < xcor.length - 1; i++) {
			int x1 = xcor[i];
			int x2 = xcor[i + 1];

			int y1 = ycor[i];
			int y2 = ycor[i + 1];

			boolean cond1 = (x1 <= x0 - 25) && (x0 + 25 < x2); // is it in the
																// range?
			boolean even = (y1 == y2); // is the place even for land

			if (cond1 && even) {
				return true;
			}
		}
		return false;
	}

	public boolean contains(int[] xcor, int[] ycor, double x0, double y0) {
		int crossings = 0;

		for (int i = 0; i < xcor.length - 1; i++) {
			int x1 = xcor[i];
			int x2 = xcor[i + 1];

			int y1 = ycor[i];
			int y2 = ycor[i + 1];

			int dy = y2 - y1;
			int dx = x2 - x1;

			double slope = 0;
			if (dx != 0) {
				slope = (double) dy / dx;
			}

			boolean cond1 = (x1 <= x0) && (x0 < x2); // is it in the range?
			boolean cond2 = (x2 <= x0) && (x0 < x1); // is it in the reverse
														// range?
			boolean above = (y0 < slope * (x0 - x1) + y1); // point slope y - y1

			if ((cond1 || cond2) && above) {
				crossings++;
			}
		}
		return (crossings % 2 != 0); // even or odd
	}

	public void surfaceChanged(SurfaceHolder holder, int format, int width,
			int height) {

	}

	public void surfaceCreated(SurfaceHolder holder) {
		main = new Thread(this);
		if (main != null)
			main.start();

	}

	public void surfaceDestroyed(SurfaceHolder holder) {
		boolean retry = true;
		while (retry) {
			try {
				main.join();
				retry = false;
			} catch (InterruptedException e) {
				// try again shutting down the thread
			}
		}
	}
	
	//touch to control the rocket and restart the game, disabled as its not fun
	@Override
	public boolean onTouch(View v, MotionEvent event) {

		// x = event.getX();
		// y = event.getY();
		//
		// t = 3;
		//
		// gameover = false;
		//
		return true;
	}
	
	// reset the game when restart button is clicked
	public void reset() {
		gameover = false;
		touchground = false;

		x = width / 2;
		y = 0;
		t = INITIAL_TIME;
		fuel=INITIAL_FUEL;
		init();
		//stop playing sound and prepare for next time
		if (crushPlay.isPlaying()) {
			crushPlay.pause();
			crushPlay.seekTo(0);
		}
		if (landingPlay.isPlaying()) {
			landingPlay.pause();
			landingPlay.seekTo(0);
		}
	}
}
