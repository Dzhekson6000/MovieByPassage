package screen;

import palette.EditBlock;
import tools.Alert;
import tools.ObbTools;
import tools.Recall;

import com.moviebypassage.R;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.res.AssetFileDescriptor;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnPreparedListener;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.widget.LinearLayout;

public class Game extends Activity implements OnPreparedListener,
 SurfaceHolder.Callback
{
	private MediaPlayer mediaPlayer;//����� ����� ��� �����
	private EditBlock editBlock;//���� � ������� � ��������
	private SharedPreferences settings;//���������: ������ � ������
	
	private boolean played = false;//true ���� ����� ���������, false ���� ���
	private int lvl = 1;//����� ������
	private int money = 0;//���������� �����
	
	ObbTools obb;//����� ��� ������ � ������� ����������
	Recall recall;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.game);
		
		obb = new ObbTools(this);//��� ������ � ������� ����������
		recall = new Recall(this);// ������ ������� ����������
		
		//��������� ���������
		settings = getSharedPreferences("MovieByPassage", 0);
		lvl = settings.getInt("lvl", 1);
		money = settings.getInt("money", 0);
		//������ ������ �����
		editBlock = new EditBlock(this, WordName(lvl), 0);//������ ��������
		editBlock.UpdateInfo(lvl, money);//��������� ���������� ����������
		
		((LinearLayout)findViewById(R.id.edit_block)).addView(editBlock);//��������� ���� � ������
		
		//������ ���� ��������� �����
		SurfaceHolder holder = ((SurfaceView)findViewById(R.id.surfaceView1)).getHolder();
		holder.addCallback(this);
		
		mediaPlayer = new MediaPlayer();//������ ������ ����� ������
		//------------��������� �����---------------------
		load_film();//��������� �����
		
		correctWordStart();//��������� ����� ��� �������� ������������ ��������� ����� 
		
		mediaPlayer.setOnPreparedListener(this);//����������. ���������� ����� ����� ����� ������
	}
	
	@Override
	public void onDestroy(){
		editBlock.onDestroy();
		obb.onDestroy();
		mediaPlayer.release();
		super.onDestroy();
	}

	private void correctWordStart() {
		//�������� �������� ������������ �����
		new Thread(new Runnable() {
			@Override
			public void run() {
				RightWord();
			}
		}).start();
	}
	
	//�������� ������
	private void load_film() {
		if(settings.getInt("recall", 0) == 0){
			if(((int)(Math.random()*5)) == 2){
				recall.show();
			}
			
		}
		
		mediaPlayer.reset();//������� ������ ������
		try {
			AssetFileDescriptor afd = obb.getAssetFileDescriptor(lvl+".3gp");
			mediaPlayer.setDataSource(afd.getFileDescriptor(), afd.getStartOffset(), afd.getDeclaredLength());
			mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
	        mediaPlayer.prepare();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	//������� ������ ������������ ��� �������������
	public void ReplayMovie(){
        if(!mediaPlayer.isPlaying()){mediaPlayer.start();}//���� ����� �� ������ ��������
        mediaPlayer.seekTo(0);//������������ � ������
        played = true;//�������� � ��� ��� ����� ���������
        //����� ��� ��������� � ������ ������
        new Thread(new Runnable() {
			@Override
			public void run() {
				try{
				stop_film();
				} catch(Exception e){
					
				}
			}
		}).start();        
	}
	
	//��� ������ ����� ���������� ��������
	@Override
	public void onPrepared(MediaPlayer mp) {
		ReplayMovie();
	}
	
	//������������� ������������ ������ �� ������ �������
	private void stop_film() {
		while(played){//���� ����� �� ��������� �� �� ��� �����
			try {
				Thread.sleep(200);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			//������������� ����� ���� ��� ��������� ������ 15 ������ 
			if(mediaPlayer != null && mediaPlayer.getCurrentPosition() > 15000 && mediaPlayer.isPlaying()){
				if(mediaPlayer != null)mediaPlayer.pause();//������������� �����
				played = false;//����� �� ���������
			} else if(!mediaPlayer.isPlaying()){//���� ����� �� ���������
				played = false;//��������� ������
			}
		}
	}

	//������� ����� �� ������ � �����
	@Override
	public boolean onTouchEvent(MotionEvent event) {
		ReplayMovie();
		return super.onTouchEvent(event);
	}
	
	//������� ����������� �����
	private void RightWord(){
		while(true){
			if(editBlock.WordCorrect()){
				if(lvl == 151){
					editBlock.MediaVictory();
					StartAlert("����� ����. ������� ���. �� ������� �����.");
					return;//���� ��������� �����
				}
				mediaPlayer.pause();//������������� �����
				editBlock.MediaCorrect();//������������� ���� ����������� �����
				//���������� ���� � ������������
				Intent correct = new Intent(Game.this,Correct.class);
				//������� ����������
				correct.putExtra("lvl", lvl+1);
				correct.putExtra("money", money+15);
				startActivityForResult(correct, 1);//������� ��� ��� ���������
				break;
			}
			if(editBlock.tips){
				editBlock.tips = false;
				Intent bonus = new Intent(Game.this,Bonus.class);
				startActivityForResult(bonus, 2);//������� ��� ��� ���������
			}
			try {
				Thread.sleep(200);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}
	
	//������� ���������� �� ����
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		Editor editor;
		if (resultCode == RESULT_OK) {
			switch (requestCode) {
			case 1:
				lvl +=1;
				money+=15;
				load_film();//��������� ����� �����
				//��������� ���� � ������� � �����������
				editBlock.UpdateWord(WordName(lvl), 0);
				editBlock.UpdateInfo(lvl, money);
				editBlock.invalidate();
				//��������� ���������
				editor = settings.edit();
				editor.putInt("lvl", lvl);
				editor.putInt("money", money);
				editor.commit();//������
				correctWordStart();//�������� ����� ����� �������� ������������ �����
				break;
			case 2:
				if (data == null) {return;}
				int activ = data.getIntExtra("activ", 0);
				if(activ == 1){
					if(money >= 5){
						editBlock.MediaMoney();
						editBlock.RemoveLetter();
						money-=5;
					} else {
						StartAlert("�� ������� �����");
					}
				} else if(activ == 2){
					if(money >= 15){
						editBlock.MediaMoney();
						editBlock.OpenLetter();
						money-=15;
					} else {
						StartAlert("�� ������� �����");
					}
				} else if(activ == 3){
					if(money >= 40){
						editBlock.MediaMoney();
						Intent poster = new Intent(Game.this,Poster.class);
						poster.putExtra("lvl", lvl);
						startActivity(poster);
						money-=40;
					} else {
						StartAlert("�� ������� �����");
					}
				} else if(activ == 4){
					if(money >= 60){
						editBlock.MediaMoney();
						Intent contents = new Intent(Game.this,Contents.class);
						contents.putExtra("lvl", lvl);
						startActivity(contents);
						money-=60;
					} else {
						StartAlert("�� ������� �����");
					}
				}
				
				editor = settings.edit();
				editor.putInt("money", money);
				editor.commit();//������
				
				editBlock.UpdateInfo(lvl, money);
				editBlock.invalidate();
				
				break;
			}
	  }
	}

	private void StartAlert(String text) {
		Intent alert = new Intent(Game.this,Alert.class);
		alert.putExtra("text", text);
		startActivity(alert);
	}
	
	//�������� ������� �� �������
	private String WordName(int lvl){
		switch (lvl) {
		case 1:
			return "���� ����";
		case 2:
			return "�������";
		case 3:
			return "�����������������";
		case 4:
			return "��������";
		case 5:
			return "������";
		case 6:
			return "�� ��� ������";
		case 7:
			return "�������� �";
		case 8:
			return "�������";
		case 9:
			return "���� ����";
		case 10:
			return "����� �������";
		case 11:
			return "���� � ר����";
		case 12:
			return "����� �����";
		case 13:
			return "�������";
		case 14:
			return "��������� �������";
		case 15:
			return "���� � �����";
		case 16:
			return "������ ���";
		case 17:
			return "�����";
		case 18:
			return "������ �����";
		case 19:
			return "�������";
		case 20:
			return "��������";
		case 21:
			return "���������� ����";
		case 22:
			return "��������� ����������";
		case 23:
			return "����� �����";
		case 24:
			return "������ �";
		case 25:
			return "����";
		case 26:
			return "���� ���������";
		case 27:
			return "����������";
		case 28:
			return "��������";
		case 29:
			return "�������";
		case 30:
			return "������";
		case 31:
			return "������";
		case 32:
			return "������ ������";
		case 33:
			return "������ �";
		case 34:
			return "����";
		case 35:
			return "�����";
		case 36:
			return "����������";
		case 37:
			return "����� ������";
		case 38:
			return "�¨����� ����";
		case 39:
			return "��������";
		case 40:
			return "������ ����";
		case 41:
			return "���";
		case 42:
			return "�������";
		case 43:
			return "�¨����� �����";
		case 44:
			return "��������";
		case 45:
			return "�����";
		case 46:
			return "������";
		case 47:
			return "����� ��������";
		case 48:
			return "����";
		case 49:
			return "������������";
		case 50:
			return "������� ��������";
		case 51:
			return "�¨����� �����";
		case 52:
			return "��� � ������";
		case 53:
			return "�����";
		case 54:
			return "����������";
		case 55:
			return "��� � �����";
		case 56:
			return "���� ���� ����";
		case 57:
			return "������ ���";
		case 58:
			return "����� �����";
		case 59:
			return "��������";
		case 60:
			return "����� ����������";
		case 61:
			return "����� ������ ������";
		case 62:
			return "�������� ������";
		case 63:
			return "������";
		case 64:
			return "������������";
		case 65:
			return "���� �� ������";
		case 66:
			return "�����";
		case 67:
			return "�������";
		case 68:
			return "������� ����";
		case 69:
			return "������� ����";
		case 70:
			return "���������";
		case 71:
			return "�����";
		case 72:
			return "������";
		case 73:
			return "���� ���";
		case 74:
			return "������� ����";
		case 75:
			return "�������";
		case 76:
			return "����� ����";
		case 77:
			return "�������������";
		case 78:
			return "���������� ����";
		case 79:
			return "�����";
		case 80:
			return "� ������� ����";
		case 81:
			return "����������";
		case 82:
			return "����� � �������";
		case 83:
			return "�������� �������";
		case 84:
			return "��������";
		case 85:
			return "��������� �����";
		case 86:
			return "������";
		case 87:
			return "�������� ����";
		case 88:
			return "��� � �����";
		case 89:
			return "��������";
		case 90:
			return "������ ����";
		case 91:
			return "����";
		case 92:
			return "�����������";
		case 93:
			return "������";
		case 94:
			return "������� ������";
		case 95:
			return "�������";
		case 96:
			return "������� �� �����";
		case 97:
			return "���������";
		case 98:
			return "������������ �����";
		case 99:
			return "�������� �������";
		case 100:
			return "��������";
		case 101:
			return "������� ��������";
		case 102:
			return "������� �� ��������";
		case 103:
			return "����� ������";
		case 104:
			return "����";
		case 105:
			return "������ ��� ����";
		case 106:
			return "������";
		case 107:
			return "����� ����������";
		case 108:
			return "���� ����������";
		case 109:
			return "��� ������ �������";
		case 110:
			return "���������� ������";
		case 111:
			return "�Ш����� ����";
		case 112:
			return "������";
		case 113:
			return "��˨��� ����";
		case 114:
			return "����������";
		case 115:
			return "�������� �����";
		case 116:
			return "��������";
		case 117:
			return "�������� �����";
		case 118:
			return "������� ����� �����";
		case 119:
			return "��� ���";
		case 120:
			return "���������� ������";
		case 121:
			return "������� �������";
		case 122:
			return "�����������";
		case 123:
			return "����� � ������";
		case 124:
			return "��������";
		case 125:
			return "����� ������";
		case 126:
			return "����� ���";
		case 127:
			return "������ �����";
		case 128:
			return "���������";
		case 129:
			return "����� ��������";
		case 130:
			return "���� � ���� �����";
		case 131:
			return "������� ����������";
		case 132:
			return "��������� ������";
		case 133:
			return "��������� � ��������";
		case 134:
			return "������������";
		case 135:
			return "����";
		case 136:
			return "������ ������";
		case 137:
			return "����� �� ��������";
		case 138:
			return "������� �������";
		case 139:
			return "������� ������� ����";
		case 140:
			return "������� ������";
		case 141:
			return "���������";
		case 142:
			return "����� � ���������";
		case 143:
			return "���������";
		case 144:
			return "��������� �����";
		case 145:
			return "������";
		case 146:
			return "���� � ���������";
		case 147:
			return "������� �� �����";
		case 148:
			return "����� �����";
		case 149:
			return "������";
		case 150:
			return "���� ����";
		case 151:
			return "���� ��������";
		}
		StartAlert("����� ������ �� �� ��������. ���������� ��������� � ����");
		return "����� ����";
	}
	
	@Override
	public void surfaceCreated(SurfaceHolder holder) {
		mediaPlayer.setDisplay(holder);//����������� ������ ����� ������ ����� ��� ����������
	}

	@Override
	public void surfaceChanged(SurfaceHolder holder, int format, int width,
			int height) {
		
	}

	@Override
	public void surfaceDestroyed(SurfaceHolder holder) {
		
	}
}
