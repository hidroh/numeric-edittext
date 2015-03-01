# numeric-edittext

[![Build Status](https://travis-ci.org/hidroh/numeric-edittext.svg?branch=master)](https://travis-ci.org/hidroh/numeric-edittext)

A simple numeric EditText widget for Android that automatically formats input text using locale-specific decimal number format.

**Behaviour**

Input

    100000.75

Will be displayed as

    100,000.75

With numeric value of

    100000.75

**Usage**

Layout declaration:

    <io.github.hidroh.numericedittext.NumericEditText
        android:inputType="numberDecimal|number"
        android:layout_width="match_parent"
        android:layout_height="wrap_content" />

Add and remove numeric value watcher the same way `TextWatcher` works for `EditText`:

    numericEditText.addNumericValueChangedListener(new NumericEditText.NumericValueWatcher() {
            @Override
            public void onChanged(double newValue) {
                // handle on numeric value changed
            }

            @Override
            public void onCleared() {
                // handle value cleared
            }
        });

Get numeric value behind input value in `EditText`:

    numericEditText.getNumericValue();
