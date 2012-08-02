package sample.application.memopad;

import android.app.ListActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ListView;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.widget.SimpleCursorAdapter;
import android.content.Intent;

public class MemoList extends ListActivity {
	public static final String[] cols = {"title", "memo", android.provider.BaseColumns._ID };	//SQL�̗�
	public MemoDBHelper memos;
	
	/**
	 * ���j���[�́u�J���v���������Ƃ�
	 */
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		this.setContentView(R.layout.memolist);	//memolist.xml
		
		
		this.showMemos(this.getMemos());	//select���Ń������X�g���擾���A�\�����\�b�h�ɓn��
		
		ListView lv = (ListView)this.findViewById(android.R.id.list);	//����View�imemolist.xml��list�j�̎擾
		this.registerForContextMenu(lv);								//ListView�I�u�W�F�N�g�ɃR���e�L�X�g���j���[��o�^�H�H�H
	}


	/**
	 * select��
	 * 
	 * db.query("memoDB", MemoList.cols, null, null, null, null, null)�E�E�E ��1�����i�e�[�u�����j�A��2�����i�񖼁j�A��3�ȍ~�i�����H�j
	 * startManagingCursor()�E�E�E ��������(cursor)�������I�Ƀ��\�[�X������Ă����Android�֗̕��ȃ��\�b�h
	 */
	public Cursor getMemos() {
		this.memos = new MemoDBHelper(this);				//DB�I�u�W�F�N�g�̐�������
		SQLiteDatabase db = memos.getReadableDatabase();	//DB�I�u�W�F�N�g�̐���
		Cursor cursor = db.query("memoDB", MemoList.cols, null, null, null, null, null);	//DB�����iselect�j
		this.startManagingCursor(cursor);					//�Ƃ肠���������Ă���
		return cursor;
	}
	
	/**
	 * �������X�g�̕\��
	 * 
	 * from�E�E�E���X�g�ɕ\������f�[�^�x�[�X�̃t�B�[���h��
	 * to�E�E�E�\������r���[�̃��\�[�XID
	 * android.R.layout.simple_list_item_1�E�E�E�W�����C�A�E�g
	 */
	public void showMemos(Cursor cursor) {
		if(cursor != null){							//�������ʂ�����ꍇ
			String[] from = {"title"};				
			int[] to = {android.R.id.text1};
			SimpleCursorAdapter adapter = new SimpleCursorAdapter(
					this, android.R.layout.simple_list_item_1,
					cursor, from, to);
			setListAdapter(adapter);
		}
		memos.close();								//select���̃I�u�W�F�N�g�����
	}
	
	
	
	/**
	 * ���X�g(��Lselect���̌���)����P�̃�����I�������Ƃ�
	 * DB���烁�����e�����o���A���C���A�N�e�B�r�e�B�iMemopadActivity�j�Ƀf�[�^��n���āA���̃A�N�e�B�r�e�B�iMemoList�j���I������B
	 * 
	 * select���iid���������j
	 */
	@Override
	protected void onListItemClick(ListView l, View v, int position, long id) {
		super.onListItemClick(l, v, position, id);				//
		this.memos = new MemoDBHelper(this);					//DB�I�u�W�F�N�g�̐�������
		SQLiteDatabase db = this.memos.getWritableDatabase();	//DB�I�u�W�F�N�g�̐���
		Cursor cursor = db.query("memoDB", MemoList.cols, "_ID="+String.valueOf(id), null, null, null, null);	//DB�����iselect�j
		this.startManagingCursor(cursor);						//�Ƃ肠���������Ă���
		Integer idx = cursor.getColumnIndex("memo");
		cursor.moveToFirst();
		Intent i = new Intent();	//�ÖٓI�C���e���g
		
		i.putExtra("text", cursor.getString(idx));
		this.setResult(RESULT_OK, i);
		memos.close();									//select���̃I�u�W�F�N�g�����
		this.finish();	//���݂̃A�N�e�B�r�e�B�iMemoList�j�I�� --> MemopadActivity.onActivityResult()
	}



}
