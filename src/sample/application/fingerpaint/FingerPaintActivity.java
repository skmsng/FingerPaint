/*
 * Bitmap.Config.ARGB_8888は、32ビットのARGBデータでBitmapを作成する事を示しています。
 * Bitmap.Configは、Bitmapのピクセルフォーマットの指定
 * 
 * Canvas canvas = new Canvas(bitmap);
 * 画面ではなく画像に描画を行う
 * 
 * paintとpath
 * 
 */

package sample.application.fingerpaint;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.DecimalFormat;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
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
		super.onCreate(savedInstanceState);
		this.setContentView(R.layout.fingerpaint);
		
		ImageView iv = (ImageView)this.findViewById(R.id.imageView1);
		Display disp = ((WindowManager)this.getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay();
		
		//ディスプレイの大きさを取得
		this.w = disp.getWidth();
		this.h = disp.getHeight();
		
		//各インスタンスの生成
		this.bitmap = Bitmap.createBitmap(this.w, this.h, Bitmap.Config.ARGB_8888);
		this.paint = new Paint();
		this.path = new Path();
		this.canvas = new Canvas(this.bitmap);
		
		//各種設定
		this.paint.setStrokeWidth(5);
		this.paint.setStyle(Paint.Style.STROKE);
		this.paint.setStrokeJoin(Paint.Join.ROUND);	//Join型(enum)で3パターン限定（本来はint型）
		this.paint.setStrokeCap(Paint.Cap.ROUND);
		this.canvas.drawColor(Color.WHITE);
		
		iv.setImageBitmap(this.bitmap);	//描画(bitmap)を表示
		iv.setOnTouchListener(this);
	}

//	@Override
	public boolean onTouch(View v, MotionEvent event){
		float x = event.getX();	//押したx座標
		float y = event.getY();	//押したy座標
		
		switch(event.getAction()){
		case MotionEvent.ACTION_DOWN:
			this.path.reset();
			this.path.moveTo(x, y);
			this.x1 = x;
			this.y1 = y;
			break;
		case MotionEvent.ACTION_MOVE:
			this.path.quadTo(this.x1, this.y1, x, y);	//曲線（直線は"lineTo"）
			this.x1 = x;
			this.y1 = y;
			this.canvas.drawPath(this.path, this.paint);	//描画
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
		//描画(bitmap)の表示
		ImageView iv = (ImageView)this.findViewById(R.id.imageView1);
		iv.setImageBitmap(this.bitmap);	
		
		return true;
	}
	
	//p131
	void save(){
		SharedPreferences prefs = this.getSharedPreferences("FingarPaintPreferences",MODE_PRIVATE);
		int imageNumber = prefs.getInt("imageNumber", 1);
		File file = null;
		
		//外部メディアがマウントされている場合
		if(this.externalMediaChecker()){
			String path = Environment.getExternalStorageDirectory()+"/mypaint/";	//SDカードのルート取得
			File outDir = new File(path);
			
			//"/mypaint/"ディレクトリがない場合は作る
			if(!outDir.exists()){
				outDir.mkdir();		
			}
			
			DecimalFormat form = new DecimalFormat("0000");	//書式設定
			do{
				file = new File(path+"img"+form.format(imageNumber)+".png");
				imageNumber++;
			}while(file.exists());
			
			//画像をpng形式に変換できた場合
			if(this.writeImage(file)){
				this.scanMedia(file.getPath());	//画像を保存した後、メディアスキャン（キャラリーに登録）
				SharedPreferences.Editor editor = prefs.edit();
				editor.putInt("imageNumber", imageNumber);
				editor.commit();
			}
		}
	}
	
	//外部メディアのマウントチェック
	boolean externalMediaChecker(){
		boolean result = false;
		String status = Environment.getExternalStorageState();
		if(status.equals(Environment.MEDIA_MOUNTED)){
			result = true;
		}
		return result;
	}
	
	//画像をpng形式に変換
	boolean writeImage(File file){
		FileOutputStream fo = null;
		try{
			fo = new FileOutputStream(file);
			this.bitmap.compress(CompressFormat.PNG, 100, fo);
			fo.flush();
			fo.close();
		}catch(FileNotFoundException e){
			return false;
		}catch(IOException e){
			System.out.println(e.getLocalizedMessage());
			return false;
		}catch(Exception e){
			return false;
		}finally{
			try {
				fo.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return true;
	}
	
	//画像をスキャンしてギャラリーに反映させる
	MediaScannerConnection mc;
	void scanMedia(final String fp){
		this.mc = new MediaScannerConnection(this, new MediaScannerConnection.MediaScannerConnectionClient(){
			public void onScanCompleted(String path, Uri uri){
				FingerPaintActivity.this.disconnect();
			}
			public void onMediaScannerConnected(){
				FingerPaintActivity.this.scanFile(fp);
			}
		});
		this.mc.connect();
	}
	
	void disconnect(){
		this.mc.disconnect();
	}
	void scanFile(String fp){
		this.mc.scanFile(fp, "image/png");
	}
	

	//メニュー
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
			Intent intent = new Intent(this, FilePicker.class);
			this.startActivityForResult(intent, 0);
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
					((ImageView)findViewById(R.id.imageView1)).setImageBitmap(FingerPaintActivity.this.bitmap);
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
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		this.bitmap = this.loadImage(data.getStringExtra("fn"));
		this.canvas = new Canvas(this.bitmap);
		ImageView iv = (ImageView)this.findViewById(R.id.imageView1);
		iv.setImageBitmap(this.bitmap);
	}
	
	//
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
