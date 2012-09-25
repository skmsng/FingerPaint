package sample.application.fingerpaint;

import java.io.File;
import java.io.FileOutputStream;
import java.text.DecimalFormat;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Path;
import android.media.MediaScannerConnection;
import android.os.Bundle;
import android.os.Environment;
import android.view.Display;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.WindowManager;
import android.widget.ImageView;
import android.net.Uri;


public class FingerPaintActivity extends Activity implements OnTouchListener{
	
	public Canvas  canvas;
	public Paint   paint;
	public Path    path;
	public Bitmap  bitmap;
	public Float   x1;
	public Float   y1;
	public Integer w;
	public Integer h;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
//		super.onCreate(savedInstanceState);
//		this.setContentView(R.layout.main);
		
		ImageView iv = (ImageView)this.findViewById(R.id.imageView1);
		Display disp = ((WindowManager)this.getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay();
		
		//ディスプレイの大きさを取得
		this.w = disp.getWidth();
		this.h = disp.getHeight();
		this.bitmap = Bitmap.createBitmap(this.w, this.h, Bitmap.Config.ARGB_8888);
		this.paint = new Paint();
		this.path = new Path();
		this.canvas = new Canvas(this.bitmap);
		
		this.paint.setStrokeWidth(5);
		this.paint.setStyle(Paint.Style.STROKE);
		this.paint.setStrokeJoin(Paint.Join.ROUND);	//本来はint型だけど0~2の３パターンに限定したいのでJoin型にしている
		this.paint.setStrokeCap(Paint.Cap.ROUND);
		this.canvas.drawColor(Color.WHITE);
		iv.setImageBitmap(this.bitmap);
		iv.setOnTouchListener(this);
	}

//	@Override
	public boolean onTouch(View v, MotionEvent event){
		float x = event.getX();
		float y = event.getY();
		
		switch(event.getAction()){
		case MotionEvent.ACTION_DOWN:
			this.path.reset();
			this.path.moveTo(x, y);
			this.x1 = x;
			this.y1 = y;
			break;
		case MotionEvent.ACTION_MOVE:
			this.path.quadTo(this.x1, this.y1, x, y);
			this.x1 = x;
			this.y1 = y;
			this.canvas.drawPath(this.path, this.paint);
			this.path.reset();
			this.path.moveTo(x, y);
			break;
		case MotionEvent.ACTION_UP:
			if(x==this.x1 && y==this.y1){
				this.y1 = this.y1+1;
			}
			this.path.quadTo(this.x1, this.y1, x, y);
			this.canvas.drawPath(this.path, this.paint);
			this.path.reset();
			break;
		}
		ImageView iv = (ImageView)this.findViewById(R.id.imageView1);
		iv.setImageBitmap(this.bitmap);
		
		return true;
	}
	
	//p131
	void save(){
		SharedPreferences prefs = this.getSharedPreferences("FingarPaintPreferences",MODE_PRIVATE);
		int imageNumber = prefs.getInt("imageNumber", 1);
		File file = null;
		
		if(this.externalMediaChecker()){
			DecimalFormat form = new DecimalFormat("0000");
			String path = Environment.getExternalStorageDirectory()+"/mypaint/";
			File outDir = new File(path);
			if(!outDir.exists())outDir.mkdir();
			
			do{
				file = new File(path+"img"+form.format(imageNumber)+".png");
				imageNumber++;
			}while(file.exists());
			if(this.writeImage(file)){
				this.scanMedia(file.getPath());	//p135追記
				SharedPreferences.Editor editor = prefs.edit();
				editor.putInt("imageNumber", imageNumber);
				editor.commit();
			}
		}
	}
	
	//p132
	boolean writeImage(File file){
		try{
			FileOutputStream fo = new FileOutputStream(file);
			this.bitmap.compress(CompressFormat.PNG, 100, fo);
			fo.flush();
			fo.close();
		}catch(Exception e){
			System.out.println(e.getLocalizedMessage());
			return false;
		}
		return true;
	}
	
	boolean externalMediaChecker(){
		boolean result = false;
		String status = Environment.getExternalStorageState();
		if(status.equals(Environment.MEDIA_MOUNTED)){
			result = true;
		}
		return result;
	}

	//p133
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater mi = getMenuInflater();
		mi.inflate(R.menu.menu, menu);
		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch(item.getItemId()){
		case R.id.menu_save:
			this.save();
			break;
		case R.id.menu_open:
			Intent intent = new Intent(this.bitmap FilePicker.class);
			startActivityForResult(intent, 0);
			break;
		case R.id.menu_color_change:
			final String[] items = getResources().getStringArray(R.array.ColorName);
			final int[] colors = getResources().getIntArray(R.array.Color);
			AlertDialog.Builder ab = new AlertDialog.Builder(this);
			ab.setTitle(R.string.menu_color_change);
			ab.setItems(items, new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int item) {
					paint.setColor(colors[item]);
				}
			});
			ab.show();
			break;
		case R.id.menu_new:
			ab = new AlertDialog.Builder(this);
			ab.setTitle(R.string.menu_new);
			ab.setMessage(R.string.confirm_new);
			ab.setPositiveButton(R.string.button_ok, new DialogInterface.OnClickListener(){
				public void onClick(DialogInterface dialog, int which){
					canvas.drawColor(Color.WHITE);
					((ImageView)findViewById(R.id.imageView1)).setImageBitmap(bitmap);
				}
			});
			ab.setNegativeButton(R.string.button_cancel, new DialogInterface.OnClickListener(){
				public void onClick(DialogInterface dialog, int which){
					
				}
			});
			ab.show();
			break;
		}
		return super.onOptionsItemSelected(item);
	}
	
	//p135
	MediaScannerConnection mc;
	void scanMedia(final String fp){
		mc = new MediaScannerConnection(this, new MediaScannerConnection.MediaScannerConnectionClient(){
			public void onScanCompleted(String path, Uri uri){
				disconnect();
			}
			public void onMediaScannerConnected(){
				scanFile(fp);
			}
		});
		mc.connect();
	}
	
	void scanFile(String fp){mc.scanFile(fp, "image/png");}
	void disconnect(){mc.disconnect();}
	
	//p139
	Bitmap loadImage(String path){
		boolean landscape = false;
		Bitmap bm;
		
		BitmapFactory.Options options = new BitmapFactory.Options();
		options.inJustDecodeBounds = true;
		BitmapFactory.decodeFile(path, options);
		int oh = options.outHeight;
		int ow = options.outWidth;
		
		if(ow>oh){
			landscape = true;
			oh = options.outWidth;
			ow = options.outHeight;
		}
		
		options.inJustDecodeBounds = false;
		options.inSampleSize = Math.max(ow/w, oh/h);
		bm = BitmapFactory.decodeFile(path, options);
		
		if(landscape){
			Matrix matrix = new Matrix();
			matrix.setRotate(90.0f);
			bm = Bitmap.createBitmap(bm, 0, 0, bm.getWidth(), bm.getHeight(), matrix, false);
		}
		
		bm = Bitmap.createScaledBitmap(bm, (int)(w), (int)(w*(((double)oh)/((double)ow))), false);
		Bitmap offBitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
		Canvas offCanvas = new Canvas(offBitmap);
		offCanvas.drawBitmap(bm, 0, (h-bm.getHeight())/2, null);
		bm = offBitmap;
		return bm;
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		bitmap = loadImage(data.getStringExtra("fn"));
		canvas = new Canvas(bitmap);
		ImageView iv = (ImageView)this.findViewById(R.id.imageView1);
		iv.setImageBitmap(bitmap);
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if(keyCode == KeyEvent.KEYCODE_BACK){
			AlertDialog.Builder ab = new AlertDialog.Builder(this);
			ab.setTitle(R.string.title_exit);
			ab.setMessage(R.string.confirm_new);
			ab.setPositiveButton(R.string.button_ok, new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int which){
					finish();
				}
			});
			ab.setNegativeButton(R.string.button_cancel, new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int which) {
					
				}
			});
			ab.show();
			return true;
		}
		return super.onKeyDown(keyCode, event);
	}
}
