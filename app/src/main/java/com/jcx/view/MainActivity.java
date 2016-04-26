package com.jcx.view;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.RelativeLayout;

import com.jcx.R;

/**
 * Created by Cui on 16-4-26.
 */
public class MainActivity extends AppCompatActivity {

    private RelativeLayout rl_allFiles;
    private RelativeLayout rl_null;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        initView();
    }

    private void initView() {
        rl_allFiles= (RelativeLayout) findViewById(R.id.rl_allFiles);
        rl_null= (RelativeLayout) findViewById(R.id.rl_null);
        rl_null.setVisibility(View.GONE);
        rl_allFiles.setOnClickListener(new onClickListener());
    }

    private class onClickListener implements OnClickListener{

        @Override
        public void onClick(View v) {
            Intent intent=new Intent(MainActivity.this,AllFilesActivity.class);
            startActivity(intent);
        }
    }

}
