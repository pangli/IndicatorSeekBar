package com.zorro.seekbar;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {
    private IndicatorSeekBar indicatorSeekBar;
    private ClearCircleView clearCircleView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        indicatorSeekBar = findViewById(R.id.seek_bar);
        clearCircleView = findViewById(R.id.ClearCircleView);
        indicatorSeekBar.setOnProgressChangedListener(new IndicatorSeekBar.OnProgressChangedListenerAdapter() {
            @Override
            public void getProgressOnFinally(IndicatorSeekBar doubleSlideSeekBar, int progress, float
                    progressFloat, boolean fromUser) {
                super.getProgressOnFinally(doubleSlideSeekBar, progress, progressFloat, fromUser);
                Toast.makeText(MainActivity.this, progress + "", Toast.LENGTH_SHORT).show();
            }
        });
        clearCircleView.startWave();
    }
}
