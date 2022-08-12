package com.szip.blewatch.base.View;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PorterDuff.Mode;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.widget.ImageView;

import com.szip.blewatch.base.R;


@SuppressLint("DrawAllocation")
public class CircularImageView extends ImageView {
	private boolean isCircular;
	private boolean isAllRadius;
	private float mRadius;

	public CircularImageView(Context context) {
		super(context);
	}

	public CircularImageView(Context context, AttributeSet attrs) {
		super(context, attrs);
		initConfig(context,attrs);
	}

	public CircularImageView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
	}

	private void initConfig(Context context, AttributeSet attrs){
		TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.CircularImageView);


		isAllRadius = a.getBoolean(R.styleable.CircularImageView_is_allRadius, false);
		isCircular = a.getBoolean(R.styleable.CircularImageView_is_circular, false);
		mRadius = a.getDimension(R.styleable.CircularImageView_radius, 0);

	}


	public void setCircular(boolean circular) {
		isCircular = circular;
	}

	@Override
	protected void onDraw(Canvas canvas) {

		Drawable drawable = getDrawable();

		if (drawable == null) {
			return;
		}

		if (getWidth() == 0 || getHeight() == 0) {
			return;
		}
		try{
			Bitmap b =  ((BitmapDrawable)drawable).getBitmap();
			if(null == b)
			{
				return;
			}

			Bitmap bitmap = b.copy(Config.ARGB_8888, true);

			int w = getWidth(), h = getHeight();
			Bitmap roundBitmap;
			if (isAllRadius){
				roundBitmap =  getCroppedBitmap(bitmap, w,h);
			}else
				roundBitmap =  getCroppedBitmap(bitmap, w);
			canvas.drawBitmap(roundBitmap, 0,0, null);
		}catch(ClassCastException e){

		}


	}

	private Bitmap getCroppedBitmap(Bitmap bmp, int radius) {
		Bitmap sbmp;
		if(bmp.getWidth() != radius || bmp.getHeight() != radius)
			sbmp = Bitmap.createScaledBitmap(bmp, radius, radius, false);
		else
			sbmp = bmp;
		Bitmap output = Bitmap.createBitmap(sbmp.getWidth(),
				sbmp.getHeight(), Config.ARGB_8888);
		Canvas canvas = new Canvas(output);

		final Paint paint = new Paint();
		final Rect rect = new Rect(0, 0, sbmp.getWidth(), sbmp.getHeight());
		final RectF rectF = new RectF(0,0,sbmp.getWidth(),sbmp.getHeight());

		if (isCircular){
			canvas.drawCircle(sbmp.getWidth() / 2+0.7f, sbmp.getHeight() / 2+0.7f,
					sbmp.getWidth() / 2+0.1f, paint);
		} else {
			canvas.drawRoundRect(rectF,mRadius,mRadius,paint);
		}
		paint.setXfermode(new PorterDuffXfermode(Mode.SRC_IN));
		canvas.drawBitmap(sbmp, rect, rect, paint);
		return output;
	}

	private Bitmap getCroppedBitmap(Bitmap bmp, int radius,int height) {
		Bitmap sbmp;
		if(bmp.getWidth() != radius || bmp.getHeight() != height)
			sbmp = Bitmap.createScaledBitmap(bmp, radius, height, false);
		else
			sbmp = bmp;
		Bitmap output = Bitmap.createBitmap(sbmp.getWidth(),
				sbmp.getHeight(), Config.ARGB_8888);
		Canvas canvas = new Canvas(output);

		final Paint paint = new Paint();
		final Rect rect = new Rect(0, 0, sbmp.getWidth(), sbmp.getHeight());
		final RectF rectF = new RectF(0,0,sbmp.getWidth(),sbmp.getHeight());

		canvas.drawRoundRect(rectF,mRadius,mRadius,paint);
		paint.setXfermode(new PorterDuffXfermode(Mode.SRC_IN));
		canvas.drawBitmap(sbmp, rect, rect, paint);
		return output;
	}
} 