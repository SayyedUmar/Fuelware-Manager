package com.fuelware.app.fw_manager.utils;

import android.app.Activity;
import android.app.Dialog;
import android.app.DownloadManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.res.Resources;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Environment;
import android.support.v7.app.AlertDialog;
import android.text.InputFilter;
import android.text.Spanned;
import android.view.Gravity;
import android.widget.TextView;

import com.fuelware.app.fw_manager.R;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static android.content.Context.DOWNLOAD_SERVICE;

public class MyUtils {

    private MyUtils() {}

    private static class SingletonHelper{
        private static final MyUtils INSTANCE = new MyUtils();
    }

    private static MyUtils getInstance(){
        return MyUtils.SingletonHelper.INSTANCE;
    }

    public static boolean hasInternetConnection(Context context) {
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(
                Context.CONNECTIVITY_SERVICE);

        NetworkInfo wifiNetwork = cm.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
        NetworkInfo mobileNetwork = cm.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);
        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();

        if (wifiNetwork != null && wifiNetwork.isConnected()) {
            return true;
        } else if (mobileNetwork != null && mobileNetwork.isConnected()) {
            return true;
        } else if (activeNetwork != null && activeNetwork.isConnected()) {
            return true;
        }

        return false;
    }

    public static void showOkAlert(Activity activity, String title, String message) {
        AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        builder.setTitle(title);
        builder.setMessage(message);
        builder.setCancelable(false);
        builder.setIcon(R.mipmap.ic_launcher);
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        Dialog dialog = builder.show();

        TextView titleView = dialog.findViewById(android.R.id.message);
        if (titleView != null) {
            titleView.setGravity(Gravity.CENTER);
        }
    }

    public static String dateToString (String currentFormat, String expectedFormat, String dateString) {
        SimpleDateFormat parser = new SimpleDateFormat(currentFormat);
        SimpleDateFormat formatter = new SimpleDateFormat(expectedFormat);
        try {
            Date convertedDate = parser.parse(dateString);
            return formatter.format(convertedDate);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "";
    }

    public static String dateToString (Date date, String expectedFormat) {
        SimpleDateFormat formatter = new SimpleDateFormat(expectedFormat);
        try {
            return formatter.format(date);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "";
    }

    public static int dpToPx(int dp) {
        return (int) (dp * Resources.getSystem().getDisplayMetrics().density);
    }

    public static int pxToDp(int px) {
        return (int) (px / Resources.getSystem().getDisplayMetrics().density);
    }


    public static void showOkAlert1(Activity activity, String title, String message, Dialog.OnCancelListener listener) {
        AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        builder.setTitle(title);
        builder.setMessage(message);
        builder.setCancelable(false);
        builder.setIcon(R.mipmap.ic_launcher);
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        Dialog dialog = builder.show();

        TextView titleView = (TextView) dialog.findViewById(android.R.id.message);
        if (titleView != null) {
            titleView.setGravity(Gravity.CENTER);
        }
    }


    public static String toTitleCase(String str) {

        if (str == null) {
            return null;
        }

        boolean space = true;
        StringBuilder builder = new StringBuilder(str);
        final int len = builder.length();

        for (int i = 0; i < len; ++i) {
            char c = builder.charAt(i);
            if (space) {
                if (!Character.isWhitespace(c)) {
                    // Convert to title case and switch out of whitespace mode.
                    builder.setCharAt(i, Character.toTitleCase(c));
                    space = false;
                }
            } else if (Character.isWhitespace(c)) {
                space = true;
            } else {
                builder.setCharAt(i, Character.toLowerCase(c));
            }
        }

        return builder.toString();
    }


    public static double parseDouble (String text) {
        DecimalFormatSymbols symbols = new DecimalFormatSymbols();
        symbols.setDecimalSeparator('.');
        DecimalFormat format = new DecimalFormat("0.#");
        format.setDecimalFormatSymbols(symbols);
        double f = 0;
        try {
            f = format.parse(text).doubleValue();
        } catch (Exception e) {e.printStackTrace();}
        return f;
    }

    public static double parseFloat (String text) {
        DecimalFormatSymbols symbols = new DecimalFormatSymbols();
        symbols.setDecimalSeparator('.');
        DecimalFormat format = new DecimalFormat("0.#");
        format.setDecimalFormatSymbols(symbols);
        float f = 0;
        try {
            f = format.parse(text).floatValue();
        } catch (Exception e) {e.printStackTrace();}
        return f;
    }

    public static int parseInteger (String text) {
        DecimalFormatSymbols symbols = new DecimalFormatSymbols();
        symbols.setDecimalSeparator('.');
        DecimalFormat format = new DecimalFormat("0.#");
        format.setDecimalFormatSymbols(symbols);
        int f = 0 ;
        try {
            f = format.parse(text).intValue();
        } catch (Exception e) {e.printStackTrace();}
        return f;
    }

    public static long parseLong (String text) {
        DecimalFormatSymbols symbols = new DecimalFormatSymbols();
        symbols.setDecimalSeparator('.');
        DecimalFormat format = new DecimalFormat("0.#");
        format.setDecimalFormatSymbols(symbols);
        long f = 0 ;
        try {
            f = format.parse(text).longValue();
        } catch (Exception e) {e.printStackTrace();}
        return f;
    }

    public static String parseToString (double val) {
        return String.format("%.2f", val);
    }

    public static String parseToString (float val) {
        return String.format("%.2f", val);
    }

    public static String formatCurrency (String text) {
        String test = text;
        NumberFormat format = NumberFormat.getCurrencyInstance(new Locale("en", "in"));
        try {
            test = format.format(Double.parseDouble(test));
            return test;
        } catch (Exception e) { e.printStackTrace(); }
        return test;
    }

    public static String formatCurrency (Double value) {
        NumberFormat format = NumberFormat.getCurrencyInstance(new Locale("en", "in"));
        String test = format.format(value);
        return test;
    }

    public static String formatCurrencyWithRs (String text) {
        String test = text;
        NumberFormat format = NumberFormat.getCurrencyInstance(new Locale("en", "in"));
        try {
            test = format.format(Double.parseDouble(test));
            return test;
        } catch (Exception e) {e.printStackTrace();}
        return test;
    }

    public static class InputFilterMinMax implements InputFilter {

        private double min, max;
        private Pattern mPattern;

        public InputFilterMinMax(double min, double max) {
            this.min = min;
            this.max = max;
            mPattern = Pattern.compile("([1-9]{1}[0-9]{0,2}([0-9]{3})*(\\.[0-9]{0,2})?|[1-9]{1}[0-9]{0,}(\\.[0-9]{0,2})?|0(\\.[0-9]{0,2})?|(\\.[0-9]{1,2})?)");
        }

        public InputFilterMinMax(String min, String max) {
            this.min = Integer.parseInt(min);
            this.max = Integer.parseInt(max);
            mPattern = Pattern.compile("([1-9]{1}[0-9]{0,2}([0-9]{3})*(\\.[0-9]{0,2})?|[1-9]{1}[0-9]{0,}(\\.[0-9]{0,2})?|0(\\.[0-9]{0,2})?|(\\.[0-9]{1,2})?)");
        }

        @Override
        public CharSequence filter(CharSequence source, int start, int end, Spanned dest, int dstart, int dend) {
            try {
//                String newVal = dest.toString().substring(0, dstart) + dest.toString().substring(dend);
//                newVal = newVal.substring(0, dstart) + source.toString() + newVal.substring(dstart);
                String formatedSource = source.subSequence(start, end).toString();
                String destPrefix = dest.subSequence(0, dstart).toString();
                String destSuffix = dest.subSequence(dend, dest.length()).toString();
                String result = destPrefix + formatedSource + destSuffix;
                result = result.replace(",", ".");

                Matcher matcher = mPattern.matcher(result);
                double input = Double.parseDouble(result);
                if (isInRange(min, max, input) && matcher.matches())
                    return null;
            } catch (Exception e) { e.printStackTrace(); }
            return "";
        }

        private boolean isInRange(double a, double b, double c) {
            return b > a ? c >= a && c <= b : c >= b && c <= a;
        }
    }

    public static class InputFilterMinMaxForLitre implements InputFilter {

        private double min, max;
        private Pattern mPattern;

        public InputFilterMinMaxForLitre(double min, double max) {
            this.min = min;
            this.max = max;
            mPattern = Pattern.compile("([1-9]{1}[0-9]{0,2}([0-9]{3})*(\\.[0-9]{0,2})?|[1-9]{1}[0-9]{0,}(\\.[0-9]{0,2})?|0(\\.[0-9]{0,2})?|(\\.[0-9]{1,2})?)");
        }

        public InputFilterMinMaxForLitre(String min, String max) {
            this.min = Integer.parseInt(min);
            this.max = Integer.parseInt(max);
            mPattern = Pattern.compile("([1-9]{1}[0-9]{0,2}([0-9]{3})*(\\.[0-9]{0,2})?|[1-9]{1}[0-9]{0,}(\\.[0-9]{0,2})?|0(\\.[0-9]{0,2})?|(\\.[0-9]{1,2})?)");
        }

        @Override
        public CharSequence filter(CharSequence source, int start, int end, Spanned dest, int dstart, int dend) {
            try {
//                String newVal = dest.toString().substring(0, dstart) + dest.toString().substring(dend);
//                newVal = newVal.substring(0, dstart) + source.toString() + newVal.substring(dstart);
                String formatedSource = source.subSequence(start, end).toString();
                String destPrefix = dest.subSequence(0, dstart).toString();
                String destSuffix = dest.subSequence(dend, dest.length()).toString();
                String result = destPrefix + formatedSource + destSuffix;
                result = result.replace(",", ".");

                Matcher matcher = mPattern.matcher(result);
                double input = Double.parseDouble(result);
                if (input == 0 && matcher.matches()) {
                    return null;
                } else if (isInRange(min, max, input) && matcher.matches())
                    return null;
            } catch (Exception e) { e.printStackTrace(); }
            return "";
        }

        private boolean isInRange(double a, double b, double c) {
            return b > a ? c >= a && c <= b : c >= b && c <= a;
        }
    }

    public static boolean isValidPassword(final String password) {

        Pattern pattern;
        Matcher matcher;
        final String PASSWORD_PATTERN = "^(?=.*[0-9])(?=.*[a-zA-Z])(?=.*[@#$%^&+=!])(?=\\S+$).{4,}$";
        pattern = Pattern.compile(PASSWORD_PATTERN);
        matcher = pattern.matcher(password);

        return matcher.matches();

    }


    public static void downloadPDF(Context context, String pdfURL) {
        try {
            DownloadManager.Request request = new DownloadManager.Request(Uri.parse(pdfURL.trim()));
            request.allowScanningByMediaScanner();
            request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
            request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, "receipts_pdf.pdf");
            DownloadManager dm = (DownloadManager) context.getSystemService(DOWNLOAD_SERVICE);
            dm.enqueue(request);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
