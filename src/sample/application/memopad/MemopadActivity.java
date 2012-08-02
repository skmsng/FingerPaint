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
        this.setContentView(R.layout.main);	//main.xml
        
        //�O��̉��(onStop�ŕۑ��������)��\��
        EditText et = (EditText) this.findViewById(R.id.editText1);						//����View�imain.xml��editText1�j�̎擾
        SharedPreferences pref = this.getSharedPreferences("MemoPrefs", MODE_PRIVATE);	//SharedPreferences�I�u�W�F�N�g�𐶐�
        et.setText(pref.getString("memo", ""));											//"memo"�ŕۑ�����String�^�̒l���擾���āAEditText�ɃZ�b�g
        et.setSelection(pref.getInt("cursor", 0));										//"cursor"�ŕۑ�����int�^�̒l���擾���āA�J�[�\���̈ʒu���ړ�
    }
    
    /**
     * SharedPreferences�E�E�Eget���\�b�h�Ńf�[�^���擾����
     * pref.getString('key','key�ɑ΂���l���Ȃ��ꍇ�̒l')
     * 
     * SharedPreferences.Editor�E�E�Eput���\�b�h�Ńf�[�^��ۑ�����
     * editor.putString('key','value')�E�E�Evalue�Ɋi�[����l
     * editor.commit()�E�E�E�i�[�����f�[�^�̕ۑ�
     * 
     * getSharedPreferences("�C�ӂ̖��O", ���[�h)
     */
    @Override	//�z�[���{�^���A�Ⴄ�A�v�����N�������Ƃ�
    public void onStop(){
    	super.onStop();
    	EditText et = (EditText) this.findViewById(R.id.editText1);						//����View�ieditText1�j�̎擾
    	SharedPreferences pref = this.getSharedPreferences("MemoPrefs", MODE_PRIVATE);	//SharedPreferences�I�u�W�F�N�g�𐶐�
    	SharedPreferences.Editor editor = pref.edit();									//Editor�I�u�W�F�N�g�𐶐�
    	editor.putString("memo", et.getText().toString());								//�e�L�X�g�{�b�N�X�̕�������擾���A"memo"�Ƃ������O�Ŋi�[
    	editor.putInt("cursor", Selection.getSelectionStart(et.getText()));				//�e�L�X�g�{�b�N�X�̃J�[�\���ʒu���擾���A"cursor"�Ƃ������O�Ŋi�[
    	editor.commit();																//editor�Ɋi�[���ꂽ�f�[�^��ۑ�
    }
    
    
    /**
	 * ���j���[�{�^�����������Ƃ�
	 * MenuInflater�E�E�EXML���烁�j���[�����
	 */
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater mi = this.getMenuInflater();		//�I�u�W�F�N�g�̐���
		mi.inflate(R.menu.menu, menu);					//menu.xml�̓��e�Ń��j���[���ڂ����
		return super.onCreateOptionsMenu(menu);
	}
	
	/**
	 * ���j���[����I�����ꂽ�Ƃ�(�C�x���g�n���h��)
	 */
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {		//�����ŉ����ꂽ���j���[���ڔԍ����󂯎��
		EditText et = (EditText)findViewById(R.id.editText1);	//����View�ieditText1�j�̎擾
		switch(item.getItemId()){								
		case R.id.menu_save:									//�ۑ�
			saveMemo();
			break;
		case R.id.menu_open:									//�J��
			Intent i = new Intent(this, MemoList.class);		//MemoList�N���X�̉�ʂɃC���e���g��n���@�����I�C���e���g�i�����̃A�v�����̃A�N�e�B�r�e�B���N���j
			startActivityForResult(i,0);						//MemoList�̋N���A��2������onActivityResult()�̑�1�����֓n�����
			break;
		case R.id.menu_new:										//�V�K�쐬
			et.setText("");										//���̉�ʁieditText1�j��""���Z�b�g�i�J���j
			break;
		}
		return super.onOptionsItemSelected(item);
	}
	
    
    /**
     * ���j���[����u�ۑ��v���������Ƃ�
     * 
     * trim()�E�E�E�󔒂���菜����������
     * indexOf(��������)�E�E�E���������̈ʒu�i���������������Ƃ���-1�j
     * substring(num1,num2)�E�E�Enum1�Ԗځ`num2�Ԗڂ܂ł̕�����
     * Math.min(num1,num2)�E�E�E2�̒l�̂������������̒l
     * 
     * getDateTimeInstance()�E�E�E�f�t�H���g�̓��t/�����t�H�[�}�b�g
     * format()�E�E�EDate�^����t/����������ɕϊ�
     * 
     * getWritableDatabase()�E�E�EDB�I�u�W�F�N�g�𐶐��iSQLiteOpenHelper�̃��\�b�h�j
     * ContentValues�E�E�E�񖼂ƒl���i�[����i�e�[�u���̒��g�j
     * db.insertOrThrow("memoDB", null, values)�E�E�E��1�����i�e�[�u�����j�Ƒ�3�����i�f�[�^�j��n����insert
     */
	public void saveMemo(){
		EditText et = (EditText)this.findViewById(R.id.editText1);				//����View�ieditText1�j�̎擾
		String title;
		String memo = et.getText().toString();									//�e�L�X�g�{�b�N�X�̕�������擾
		
		if(memo.trim().length()>0){												//�󔒈ȊO�̕���������ꍇ
			if(memo.indexOf("\n") == -1){										//���s���Ȃ��ꍇ
				title = memo.substring(0, Math.min(memo.length(), 20));			//�ŏ���20�������^�C�g��
			}else{																//���s������ꍇ
				title = memo.substring(0, Math.min(memo.indexOf("\n"),20));		//�ŏ��̉��s�܂ł��^�C�g��
			}
			String ts = DateFormat.getDateTimeInstance().format(new Date());	//�ۑ������𕶎���Ŋi�[
			
			MemoDBHelper memos = new MemoDBHelper(this);		//DB�I�u�W�F�N�g�̐�������
			SQLiteDatabase db = memos.getWritableDatabase();	//DB�I�u�W�F�N�g�̐���
			ContentValues values = new ContentValues();			//�e�[�u���̃I�u�W�F�N�g�̐���
			values.put("title", title + "\n" + ts);				//��=title�@�f�[�^=�^�C�g��+�ۑ�����
			values.put("memo", memo);							//��=memo �f�[�^=�������e
			db.insertOrThrow("memoDB", null, values);			//DB�ɓo�^�iinsert�j
			memos.close();										//�I�u�W�F�N�g�����
		}
	}

	
	
	
	/**
	 * ���j���[����u�J���v�������AMemoList�A�N�e�B�r�e�B�̏������I�������
	 * 
	 * �I�����ꂽ�������J��
	 */
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if(resultCode == RESULT_OK){
			EditText et = (EditText)findViewById(R.id.editText1);
			
			switch(requestCode){							//�g���@�\
			case 0:
				et.setText(data.getStringExtra("text"));
				break;
			}
		}
	}

	
	
	
}