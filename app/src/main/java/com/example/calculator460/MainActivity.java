package com.example.calculator460;



import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import java.math.BigDecimal;
import java.math.RoundingMode;
/**
 * Provides the calculator interface and handles button input.
 *
 * Supports decimal numbers, addition, subtraction, multiplication,
 * division, negative numbers, and parentheses. Displays the developer's
 * name and CWU ID when the activity first opens.
 *
 * @author Heven Gebrehiwot
 */

public class MainActivity extends AppCompatActivity {

    /** Developer information shown when the calculator starts. */
    private static final String INITIAL_TEXT = "45684857\nHeven Gebrehiwot";

    /** Displays the current expression, calculation result, or error. */
    private TextView tvDisplay;

    /** Stores the expression currently being entered. */
    private StringBuilder expression = new StringBuilder();

    /** Indicates whether the developer information is being displayed. */
    private boolean showingInitial = true;

    /** Indicates whether the display contains a result or error. */
    private boolean showingResult = false;

    /**
     * Initializes the activity, loads the layout, and displays
     * the developer's name and CWU ID.
     *
     * @param savedInstanceState previously saved activity state,
     *                           or null for a new activity
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        tvDisplay = findViewById(R.id.tvDisplay);
        tvDisplay.setText(INITIAL_TEXT);
    }


    /**
     * Handles calculator button clicks.
     *
     * Routes AC, C, and equals to their corresponding methods.
     * Appends all other button values to the expression.
     *
     * @param v the button that was clicked
     */
    public void onKey(View v) {
        String key = ((Button) v).getText().toString();

        switch (key) {
            case "AC":
                clearAll();
                return;
            case "C":
                deleteLast();
                return;
            case "=":
                calculate();
                return;
            default:
                appendKey(key);
        }
    }

    /**
     * Appends a button value to the current expression.
     *
     * Clears the initial developer information on the first input.
     * After a calculation, an operator continues from the result,
     * while other input starts a new expression.
     *
     * @param key the number, decimal point, operator, or parenthesis
     */
    private void appendKey(String key) {
        boolean isOperator = "+-*/".contains(key);

        if (showingInitial) {
            expression.setLength(0);
            showingInitial = false;
        } else if (showingResult) {
            if (!isOperator) {
                expression.setLength(0);
            }
        }
        showingResult = false;

        expression.append(key);
        tvDisplay.setText(expression.toString());
    }

    /**
     * Clears the entire expression and resets the display to zero.
     */
    private void clearAll() {
        expression.setLength(0);
        showingInitial = false;
        showingResult = false;
        tvDisplay.setText("0");
    }

    /**
     * Deletes the last character from the current expression.
     *
     * Displays zero when no characters remain. Leaves the initial
     * developer information unchanged if no input has been entered.
     */
    private void deleteLast() {
        if (showingInitial) {
            return;
        }
        if (expression.length() > 0) {
            expression.setLength(expression.length() - 1);
        }
        showingResult = false;
        tvDisplay.setText(expression.length() == 0 ? "0" : expression.toString());
    }

    /**
     * Evaluates the current expression and displays its result.
     *
     * Rounds results to ten decimal places and removes trailing zeros.
     * Displays Error for invalid expressions, division by zero,
     * or nonfinite results.
     */

    private void calculate() {
        if (showingInitial || expression.length() == 0) {
            return;
        }
        try {
            double value = new Parser(expression.toString()).parse();
            if (Double.isNaN(value) || Double.isInfinite(value)) {
                throw new ArithmeticException("Bad result");
            }
            String text = new BigDecimal(value)
                    .setScale(10, RoundingMode.HALF_UP)
                    .stripTrailingZeros()
                    .toPlainString();
            expression.setLength(0);
            expression.append(text);
            tvDisplay.setText(text);
        } catch (RuntimeException e) {
            expression.setLength(0);
            tvDisplay.setText("Error");
        }
        showingResult = true;
    }

    /**
     * Evaluates mathematical expressions using recursive descent.
     *
     * Applies multiplication and division before addition and subtraction.
     * Supports decimal numbers, unary minus, and nested parentheses.
     */
    private static class Parser {

        /** Expression being evaluated. */
        private final String s;
        /** Index of the next character to process. */
        private int pos = 0;


        /**
         * Creates a parser for an expression.
         *
         * @param s the mathematical expression to evaluate
         */
        Parser(String s) {
            this.s = s;
        }

        /**
         * Evaluates the complete expression.
         *
         * @return the calculated value
         * @throws IllegalArgumentException if unprocessed characters remain
         */
        double parse() {
            double result = parseExpression();
            if (pos < s.length()) {
                throw new IllegalArgumentException("Unexpected: " + s.charAt(pos));
            }
            return result;
        }

        /**
         * Evaluates addition and subtraction from left to right.
         *
         * @return the value of the expression
         */
        private double parseExpression() {
            double value = parseTerm();
            while (pos < s.length() && (s.charAt(pos) == '+' || s.charAt(pos) == '-')) {
                char op = s.charAt(pos++);
                double right = parseTerm();
                value = (op == '+') ? value + right : value - right;
            }
            return value;
        }

        /**
         * Evaluates multiplication and division from left to right.
         *
         * @return the value of the term
         * @throws ArithmeticException if division by zero is attempted
         */
        private double parseTerm() {
            double value = parseFactor();
            while (pos < s.length() && (s.charAt(pos) == '*' || s.charAt(pos) == '/')) {
                char op = s.charAt(pos++);
                double right = parseFactor();
                if (op == '/' && right == 0) {
                    throw new ArithmeticException("Divide by zero");
                }
                value = (op == '*') ? value * right : value / right;
            }
            return value;
        }

        /**
         * Evaluates a number, a negative factor, or a parenthesized expression.
         *
         * @return the value of the factor
         * @throws IllegalArgumentException if a number or closing
         *                                  parenthesis is missing
         * @throws NumberFormatException if the number has an invalid format
         */
        private double parseFactor() {
            if (pos >= s.length()) {
                throw new IllegalArgumentException("Unexpected end");
            }
            char c = s.charAt(pos);
            if (c == '-') {
                pos++;
                return -parseFactor();
            }
            if (c == '(') {
                pos++;
                double value = parseExpression();
                if (pos >= s.length() || s.charAt(pos) != ')') {
                    throw new IllegalArgumentException("Missing )");
                }
                pos++;
                return value;
            }
            int start = pos;
            while (pos < s.length() && (Character.isDigit(s.charAt(pos)) || s.charAt(pos) == '.')) {
                pos++;
            }
            if (start == pos) {
                throw new IllegalArgumentException("Expected number");
            }
            return Double.parseDouble(s.substring(start, pos));
        }
    }
}