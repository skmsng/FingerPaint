package sample.application.memopad;

import java.text.DateFormat;
import java.util.Date;

import android.app.Activity;
import android.os.Bundle;
import android.content.ContentValues;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.sqlite.SQLiteDatabase;
import android.text.Selection;
import android.widget.EditText;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

public class MemopadActivity extends Activity {
    /** Called when the activity is first created. */
	
    @Override	//�A�v�����N�������Ƃ�
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.setContentView(R.layout.main);	//main.xml�̎w����
        
        //�O��̉�ʂ��c��
        EditText et = (EditText) this.findViewById(R.id.editText1);	//���̉�ʁi�C���X�^���X�j�̎擾
        SharedPreferences pref = this.getSharedPreferences("MemoPrefs", MODE_PRIVATE);	//�O��̂��擾�H
        et.setText(pref.getString("memo", ""));	//�O��̂��Z�b�g
        et.setSelection(pref.getInt("cursor", 0));
    }
    
    @Override	//�z�[���{�^���A�Ⴄ�A�v�����N�������Ƃ�
    public void onStop(){
    	super.onStop();
    	EditText et = (EditText) this.findViewById(R.id.editText1);	//���̉�ʁi�C���X�^���X�j�̎擾
    	SharedPreferences pref = this.getSharedPreferences("MemoPrefs", MODE_PRIVATE);
    	SharedPreferences.Editor editor = pref.edit();
    	editor.putString("memo", et.getText().toString());	//memo�Ƃ������O�ŕۑ�
    	editor.putInt("cursor", Selection.getSelectionStart(et.getText()));
    	editor.commit();
    }
    
	public void saveMemo(){
		EditText et = (EditText)this.findViewById(R.id.editText1);
		String title;
		String memo = et.getText().toString();
		
		if(memo.trim().length()>0){
			if(memo.indexOf("\n") == -1){
				title = memo.substring(0, Math.min(memo.length(), 20));
			}else{
				title = memo.substring(0, Math.min(memo.indexOf("\n"),20));
			}
			String ts = DateFormat.getDateTimeInstance().format(new Date());
			MemoDBHelper memos = new MemoDBHelper(this);
			SQLiteDatabase db = memos.getWritableDatabase();
			ContentValues values = new ContentValues();
			values.put("title", title + "\n" + ts);
			values.put("memo", memo);
			db.insertOrThrow("memoDB", null, values);
			memos.close();
		}
	}

	@Override	//
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if(resultCode == RESULT_OK){
			EditText et = (EditText)findViewById(R.id.editText1);
			
			switch(requestCode){
			case 0:
				et.setText(data.getStringExtra("text"));
				break;
			}
		}
	}

	@Override	//���j���[�{�^�����������Ƃ�
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater mi = this.getMenuInflater();
		mi.inflate(R.menu.menu, menu);	//menu.xml
		return super.onCreateOptionsMenu(menu);
	}
	
	
	@Override	//�C�x���g�n���h���i���j���[����I�����ꂽ�Ƃ��j
	public boolean onOptionsItemSelected(MenuItem item) {		//�����Ń��j���[�ԍ����󂯎��
		EditText et = (EditText)findViewById(R.id.editText1);	//���̉�ʁi�C���X�^���X�̎擾�j�̎擾
		switch(item.getItemId()){								//
		case R.id.menu_save:
			saveMemo();
			break;
		case R.id.menu_open:
			Intent i = new Intent(this, MemoList.class);		//MemoList�N���X�̉�ʂɃC���e���g��n���@�����I�C���e���g�i�����̃A�v�����̃A�N�e�B�r�e�B���N���j
			startActivityForResult(i,0);
			break;
		case R.id.menu_new:
			et.setText("");										//���̉�ʁi�C���X�^���X�j��""���Z�b�g-->�o�O�H
			break;
		}
		return super.onOptionsItemSelected(item);
	}
	
	
}