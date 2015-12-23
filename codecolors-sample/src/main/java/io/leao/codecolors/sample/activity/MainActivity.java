package io.leao.codecolors.sample.activity;

import android.graphics.Color;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;

import io.leao.codecolors.CodeColors;
import io.leao.codecolors.app.CcAppCompatActivity;
import io.leao.codecolors.sample.R;

public class MainActivity extends CcAppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });

        Button button = (Button) findViewById(android.R.id.button1);
        button.setOnClickListener(new View.OnClickListener() {
            private int[] mColors = new int[]{Color.RED, Color.GREEN, Color.BLUE, Color.YELLOW};
            private int mCurrentColor = 0;

            public void onClick(View v) {
                int color = mColors[mCurrentColor++];
                if (mCurrentColor >= mColors.length) {
                    mCurrentColor = 0;
                }
                CodeColors.setColor(R.color.cc__color_primary_dark, color);
                CodeColors.setColor(R.color.cc__color_primary, color);
                CodeColors.setColor(R.color.cc__color_accent, color);
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
