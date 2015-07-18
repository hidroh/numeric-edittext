package io.github.hidroh.numericedittext;

import android.content.Context;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.view.View;
import android.widget.EditText;

import java.text.DecimalFormatSymbols;
import java.text.NumberFormat;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;

/**
 * {@link android.widget.EditText} widget for numeric input, with support for realtime formatting.
 * Supported {@link android.text.InputType} flags are {@link android.text.InputType.TYPE_CLASS_NUMBER}
 * and {@link android.text.InputType.TYPE_NUMBER_FLAG_DECIMAL}.
 * Accepted input digits and symbols should be set via{@link android.R.styleable#TextView_digits}.
 */
public class NumericEditText extends EditText {
    private final char GROUPING_SEPARATOR = DecimalFormatSymbols.getInstance().getGroupingSeparator();
    private final char DECIMAL_SEPARATOR = DecimalFormatSymbols.getInstance().getDecimalSeparator();
    private final String LEADING_ZERO_FILTER_REGEX = "^0+(?!$)";

    private String mDefaultText = null;
    private String mPreviousText = "";
    private String mNumberFilterRegex = "[^\\d\\" + DECIMAL_SEPARATOR + "]";

    /**
     * Interface to notify listeners when numeric value has been changed or cleared
     */
    public interface NumericValueWatcher {
        /**
         * Fired when numeric value has been changed
         * @param newValue new numeric value
         */
        void onChanged(double newValue);

        /**
         * Fired when numeric value has been cleared (text field is empty)
         */
        void onCleared();
    }

    private List<NumericValueWatcher> mNumericListeners = new ArrayList<>();
    private final TextWatcher mTextWatcher = new TextWatcher() {
        private boolean validateLock = false;

        @Override
        public void afterTextChanged(Editable s) {
            if (validateLock) {
                return;
            }

            // valid decimal number should not have more than 2 decimal separators
            if (countMatches(s.toString(), String.valueOf(DECIMAL_SEPARATOR)) > 1) {
                validateLock = true;
                setText(mPreviousText); // cancel change and revert to previous input
                setSelection(mPreviousText.length());
                validateLock = false;
                return;
            }

            if (s.length() == 0) {
                handleNumericValueCleared();
                return;
            }

            setTextInternal(format(s.toString()));
            setSelection(getText().length());
            handleNumericValueChanged();
        }

        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            // do nothing
        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
            // do nothing
        }
    };

    private void handleNumericValueCleared() {
        mPreviousText = "";
        for (NumericValueWatcher listener : mNumericListeners) {
            listener.onCleared();
        }
    }

    private void handleNumericValueChanged() {
        mPreviousText = getText().toString();
        for (NumericValueWatcher listener : mNumericListeners) {
            listener.onChanged(getNumericValue());
        }
    }

    public NumericEditText(Context context, AttributeSet attrs) {
        super(context, attrs);

        addTextChangedListener(mTextWatcher);
        setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // disable moving cursor
                setSelection(getText().length());
            }
        });
    }

    /**
     * Add listener for numeric value changed events
     * @param watcher   listener to add
     */
    public void addNumericValueChangedListener(NumericValueWatcher watcher) {
        mNumericListeners.add(watcher);
    }

    /**
     * Remove all listeners to numeric value changed events
     */
    public void removeAllNumericValueChangedListeners() {
        while (!mNumericListeners.isEmpty()) {
            mNumericListeners.remove(0);
        }
    }

    /**
     * Set default numeric value and how it should be displayed, this value will be used if
     * {@link #clear} is called
     * @param defaultNumericValue   numeric value
     * @param defaultNumericFormat  display format for numeric value
     */
    public void setDefaultNumericValue(double defaultNumericValue, final String defaultNumericFormat) {
        mDefaultText = String.format(defaultNumericFormat, defaultNumericValue);
        setTextInternal(mDefaultText);
    }

    /**
     * Clear text field and replace it with default value set in {@link #setDefaultNumericValue} if
     * any
     */
    public void clear() {
        setTextInternal(mDefaultText != null ? mDefaultText : "");
        if (mDefaultText != null) {
            handleNumericValueChanged();
        }
    }

    /**
     * Return numeric value repesented by the text field
     * @return  numeric value or {@link Double.NaN} if not a number
     */
    public double getNumericValue() {
        String original = getText().toString().replaceAll(mNumberFilterRegex, "");
        try {
            return NumberFormat.getInstance().parse(original).doubleValue();
        } catch (ParseException e) {
            return Double.NaN;
        }
    }

    /**
     * Add grouping separators to string
     * @param original  original string, may already contains incorrect grouping separators
     * @return  string with correct grouping separators
     */
    private String format(final String original) {
        final String[] parts = original.split("\\" + DECIMAL_SEPARATOR, -1);
        String number = parts[0] // since we split with limit -1 there will always be at least 1 part
                .replaceAll(mNumberFilterRegex, "")
                .replaceFirst(LEADING_ZERO_FILTER_REGEX, "");

        // add grouping separators, need to reverse back and forth since Java regex does not support
        // right to left matching
        number = reverse(
                reverse(number).replaceAll("(.{3})", "$1" + GROUPING_SEPARATOR));
        // remove leading grouping separator if any
        number = removeStart(number, String.valueOf(GROUPING_SEPARATOR));

        // add fraction part if any
        if (parts.length > 1) {
            number += DECIMAL_SEPARATOR + parts[1];
        }

        return number;
    }

    /**
     * Change display text without triggering numeric value changed
     * @param text  new text to apply
     */
    private void setTextInternal(String text) {
        removeTextChangedListener(mTextWatcher);
        setText(text);
        addTextChangedListener(mTextWatcher);
    }

    private String reverse(String original) {
        if (original == null || original.length() <= 1) {
            return original;
        }
        return TextUtils.getReverse(original, 0, original.length()).toString();
    }

    private String removeStart(String str, String remove) {
        if (TextUtils.isEmpty(str)) {
            return str;
        }
        if (str.startsWith(remove)){
            return str.substring(remove.length());
        }
        return str;
    }

    private int countMatches(String str, String sub) {
        if (TextUtils.isEmpty(str)) {
            return 0;
        }
        int lastIndex = str.lastIndexOf(sub);
        if (lastIndex < 0) {
            return 0;
        } else {
            return 1 + countMatches(str.substring(0, lastIndex), sub);
        }
    }
}