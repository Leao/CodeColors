package io.leao.codecolors.sample.activity;

import android.graphics.Color;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.widget.Toolbar;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.util.regex.Pattern;

import io.leao.codecolors.CodeColors;
import io.leao.codecolors.appcompat.app.CcAppCompatActivity;
import io.leao.codecolors.core.res.CcColorStateList;
import io.leao.codecolors.sample.R;
import io.leao.codecolors.sample.color.ColorCycler;

public class MainActivity extends CcAppCompatActivity {
    private ColorCycler mColorCycler = new ColorCycler();
    private Pattern mHexPattern = Pattern.compile("^#?([A-Fa-f0-9]{6}|[A-Fa-f0-9]{3})$");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        final FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, R.string.snackbar_regular, Snackbar.LENGTH_LONG)
                        .setAction("Action", new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                Toast.makeText(MainActivity.this, R.string.snackbar_action_regular, Toast.LENGTH_SHORT).show();
                            }
                        }).show();
            }
        });

        Button cycleButton = (Button) findViewById(android.R.id.button1);
        cycleButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                cycleColors();
            }
        });

        EditText colorEditText = (EditText) findViewById(android.R.id.text1);
        colorEditText.setOnEditorActionListener(new EditText.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    pickColor();
                    return true;
                }
                return false;
            }
        });

        Button pickButton = (Button) findViewById(android.R.id.button2);
        pickButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                pickColor();
            }
        });
    }

    private void cycleColors() {
        mColorCycler.cycle();
        animateColorsTo(
                mColorCycler.getPrimary(),
                mColorCycler.getPrimaryDark(),
                mColorCycler.getAccent(),
                mColorCycler.getAccentPressed());
    }

    private void animateColorsTo(Integer primary, Integer primaryDark, Integer accent, Integer accentPressed) {
        animateColorTo(R.color.cc__color_primary, primary).start();
        animateColorTo(R.color.cc__color_primary_dark, primaryDark).start();
        CcColorStateList.AnimationBuilder builder = animateColorTo(R.color.cc__color_accent, accent);
        if (accentPressed != null) {
            builder.setState(new int[]{android.R.attr.state_pressed}, Color.CYAN);
        } else {
            builder.removeState(new int[]{android.R.attr.state_pressed});
        }
        builder.start();
    }

    private CcColorStateList.AnimationBuilder animateColorTo(int resId, Integer color) {
        if (color != null) {
            return CodeColors.animate(resId).setColor(color);
        } else {
            return CodeColors.animate(resId).setColor(null);
        }
    }

    private void pickColor() {
        EditText colorEditText = (EditText) findViewById(android.R.id.text1);
        if (colorEditText != null) {
            String colorHex = colorEditText.getText().toString();
            if (mHexPattern.matcher(colorHex).matches()) {
                int color = Color.parseColor(ensureParsableColor(colorHex));
                animateColorsTo(color, color, color, null);
            } else {
                Snackbar.make(colorEditText, R.string.color_not_valid, Snackbar.LENGTH_SHORT).show();
            }
        }
    }

    private String ensureParsableColor(String color) {
        if (!color.startsWith("#")) {
            color = "#" + color;
        }
        if (color.length() < 7) {
            color += color.substring(1);
        }
        return color;
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
