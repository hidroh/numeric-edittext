package io.github.hidroh.numericedittext.sample;

import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import io.github.hidroh.numericedittext.NumericEditText;


public class MainActivity extends ActionBarActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        final TextView textView = (TextView) findViewById(R.id.textView);
        final NumericEditText numericEditText = (NumericEditText) findViewById(R.id.numericEditText);
        numericEditText.addNumericValueChangedListener(new NumericEditText.NumericValueWatcher() {
            @Override
            public void onChanged(double newValue) {
                textView.setText(String.format("%1$,.2f %%", newValue / 100));
            }

            @Override
            public void onCleared() {
                textView.setText("-");
            }
        });
        findViewById(R.id.button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(MainActivity.this,
                        String.valueOf(numericEditText.getNumericValue()),
                        Toast.LENGTH_SHORT)
                        .show();
            }
        });
    }
}
