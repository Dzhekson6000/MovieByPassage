package screen;

import tools.ImagesContainer;

import com.moviebypassage.R;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;

public class main extends Activity {
	
	public static ImagesContainer images;
	
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.menu);
		
		//��������� �����������
		images = new ImagesContainer(this);
	}
	
	//������� �����
	public void onClickButtomPlay(View view){
		Intent intent = new Intent(main.this, game.class);
	    startActivity(intent);
	}
}
