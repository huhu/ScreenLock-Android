package com.eva.me.mysquarescreenlock.view;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.os.Handler;
import android.util.AttributeSet;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.eva.me.mysquarescreenlock.unlock.util.CoordinatesUtil;
import com.eva.me.mysquarescreenlock.unlock.util.LocalOnGestureListener;

public class FlingRelativeLayout extends RelativeLayout{
	public static final int GET_DIRECTION = 10086;

	private final String TAG = "FlingRelativeLayout";//TAG
	
	private Context mContext = null;
	
	private Bitmap dragView = null;//bitmap
	private final int initDragViewPos = 3000;//start pos
	private final int wholeDistance = 200;
	private final int distance = 10;
	private final long delay = 50l;
	private int dragViewX = initDragViewPos
			, dragViewY = initDragViewPos;       //bitmap pos
	
	private ImageView oriDraView;//original drag view in center
	private int startPosX = -1, startPosY = -1;//center pos
	
	private int curDirection = 0;//初始化的方向就是最开始的方向
	
	private GestureDetector gestureDetector = null;
	
	
	//CONSTRUCTORS
 	public FlingRelativeLayout(Context context) {
		super(context);
		initConstructor(context);
	}

	public FlingRelativeLayout(Context context, AttributeSet attrs) {
		super(context, attrs);
		initConstructor(context);
	}
	
	public FlingRelativeLayout(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		initConstructor(context);
	}

	private void initConstructor(Context context) {
		mContext = context;
		LocalOnGestureListener localOnGestureListener = new LocalOnGestureListener(); 
		gestureDetector = new GestureDetector(mContext, localOnGestureListener);
		localOnGestureListener.setHandler(mHandler);
		
		initDragBitmap();	
		
	}
	
	//init the drag view
	private void initDragBitmap() {	
		if (dragView == null) {
			dragView = BitmapFactory.decodeResource(mContext.getResources(),
					com.eva.me.mysquarescreenlock.R.drawable.ring);
		}
	}

	//init the origin drag view
	@Override
	protected void onFinishInflate() {
		super.onFinishInflate();
		Log.d(TAG, "onFinishInflate: ");
		oriDraView = (ImageView) findViewById(com.eva.me.mysquarescreenlock.R.id.ivOriginDragView);//initialize
	}

	//On Draw
	@Override
	protected void onDraw(Canvas canvas) {
		super.onDraw(canvas);
		Log.v(TAG, "===========OnDraw===========");
		initOnDraw(canvas);
	}

	//init switch logic
	private void initOnDraw(Canvas canvas) {
		// TODO Auto-generated method stub
		if (startPosX == -1 || startPosY == -1) {
			startPosX = oriDraView.getLeft();
			startPosY = oriDraView.getTop();
			Log.d(TAG,"initOnDraw ====>   startPosX: "+ startPosX +"startPosY: " +startPosY );
		}
		
		canvas.drawBitmap(dragView, dragViewX, dragViewY, null);
	}

	@Override
	public boolean onTouchEvent(MotionEvent event) {
		
		switch (event.getAction()) {
		case MotionEvent.ACTION_DOWN://这里是为了监听起点，起点不正确就是不可能会触发最后的onFling事件
			Log.v(TAG, "onTouchEvent: ACTION_DOWN");
			
			if (handleActionDown(event)) {
				return gestureDetector.onTouchEvent(event);
			}else {
				resetToInit();//如果没能在这个圈里面
				return false;
			}
//		case MotionEvent.ACTION_MOVE:
//			Log.v(TAG, "onTouchEvent: ACTION_MOVE");
//			//update pos
//			dragViewX = (int) event.getX();
//			dragViewY = (int) event.getY();
//			
//			handleActionMove(event);
//			return true;
//		
//		case MotionEvent.ACTION_UP:
//			handleActionUp();
//			return true;
//			
		default:
			break;
		}
//		return super.onTouchEvent(event);
		Log.d(TAG, "FlingRelativeLayout: onTouch");
		return gestureDetector.onTouchEvent(event);
	}

	/**
	 * @deprecated
	 * @param event
	 */
	private void handleActionUp() {
		// TODO Auto-generated method stub
		resetToInit();
	}

	/**
	 * @deprecated
	 * @param event
	 */
	//control action move event
	private void handleActionMove(MotionEvent event) {
		curDirection = CoordinatesUtil.getDirection(dragViewX, dragViewY);//get direction
		Log.e(TAG, "handleActionMove: curDirection: "+curDirection);
		invalidate();
	}

	//control action down event
	private boolean handleActionDown(MotionEvent event) {
		Rect rect = new Rect();
		oriDraView.getHitRect(rect);
		int x = (int) event.getX();
		int y = (int) event.getY();
		
		boolean isInRect = rect.contains(x, y);
		
		if(isInRect) {
			oriDraView.setVisibility(View.INVISIBLE);//===========================need to changed position, you should not invisible to early
			//调整坐标
			dragViewX = startPosX;
			dragViewY = startPosY;
		}
		
		Log.e(TAG, "handleActionDown:  isInRect: "+isInRect);
		return isInRect;
	}

	//reset everything to defalut
	private void resetToInit() {
		//reset to default pos
		dragViewX = initDragViewPos;
		dragViewY = initDragViewPos;
		
		oriDraView.setVisibility(View.VISIBLE);
		
		curDirection = 0;//让此时的坐标恢复到最初始的情况下
		LocalOnGestureListener.detectDirection = 0;//==============================================================detect
		
		invalidate();
	}

	private Handler mHandler = new Handler() {
		public void handleMessage(android.os.Message msg) {
			switch (msg.what) {
			case FlingRelativeLayout.GET_DIRECTION:
				Log.e(TAG, "mHandler -> handleMessage -> GET_DIRECTION : ");
				curDirection = LocalOnGestureListener.detectDirection;
				Log.e(TAG, "curDirection: "+curDirection);
				mHandler.postDelayed(flingDragViewThread, delay);
				invalidate();//每次收到这种事件的时候就会更新UI
				break;

			default:
				break;
			}
		};
	};
	
	private Runnable flingDragViewThread = new Runnable() {
		
		@Override
		public void run() {
			switch (curDirection) {
			case 0:
				resetToInit();
				Log.v(TAG, "flingDragViewThread -> run() : "+"curDirection: "+curDirection+" ... resetToInit");
				break;
				
			case 1:
				dragViewY -= distance;
				if ((startPosY-dragViewY) > wholeDistance ) {//移动了足够的距离
					resetToInit();
					Log.v(TAG, "flingDragViewThread -> run() : "+"curDirection: "+curDirection+" ... resetToInit ... 移动了足够的距离");
				} else {
					mHandler.postDelayed(flingDragViewThread, delay);
					invalidate();
					Log.v(TAG, "flingDragViewThread -> run() : "+"curDirection: "+curDirection+" ... invalidate ... ");
				}
				break;
				
			case 2:
				dragViewX += distance;
				if ((dragViewX-startPosX) > wholeDistance) {//移动了足够的距离
					resetToInit();
					Log.v(TAG, "flingDragViewThread -> run() : "+"curDirection: "+curDirection+" ... resetToInit ... 移动了足够的距离");
				} else {
					mHandler.postDelayed(flingDragViewThread, delay);
					invalidate();
					Log.v(TAG, "flingDragViewThread -> run() : "+"curDirection: "+curDirection+" ... invalidate ... ");
				}
				break;
				
			case 3:
				dragViewY += distance;
				if ((dragViewY-startPosY) > wholeDistance ) {//移动了足够的距离
					resetToInit();
					Log.v(TAG, "flingDragViewThread -> run() : "+"curDirection: "+curDirection+" ... resetToInit ... 移动了足够的距离");
				} else {
					mHandler.postDelayed(flingDragViewThread, delay);
					invalidate();
					Log.v(TAG, "flingDragViewThread -> run() : "+"curDirection: "+curDirection+" ... invalidate ... ");
				}
				break;
				
			case 4:
				dragViewX -= distance;
				if ((startPosX-dragViewX) > wholeDistance) {//移动了足够的距离
					resetToInit();
					Log.v(TAG, "flingDragViewThread -> run() : "+"curDirection: "+curDirection+" ... resetToInit ... 移动了足够的距离");
				} else {
					mHandler.postDelayed(flingDragViewThread, delay);
					invalidate();
					Log.v(TAG, "flingDragViewThread -> run() : "+"curDirection: "+curDirection+" ... invalidate ... ");
				}
				break;

			default:
				break;
			}
//			mHandler.postDelayed(flingDragViewThread, delay);
		}
	};
	
}
