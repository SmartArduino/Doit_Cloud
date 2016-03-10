package com.example.myzxingtest;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;

public class MyMainActivity extends Activity {

	private SharedPreferences _prefers;
	private static final String _PREF_NAME = "com.example.myzxingtest";

	public static final String KEY_DECODE_1D = "preferences_decode_1D";
	public static final String KEY_DECODE_QR = "preferences_decode_QR";
	public static final String KEY_DECODE_DATA_MATRIX = "preferences_decode_Data_Matrix";
	public static final String KEY_CUSTOM_PRODUCT_SEARCH = "preferences_custom_product_search";

	public static final String KEY_PLAY_BEEP = "preferences_play_beep";
	public static final String KEY_VIBRATE = "preferences_vibrate";
	public static final String KEY_COPY_TO_CLIPBOARD = "preferences_copy_to_clipboard";
	public static final String KEY_FRONT_LIGHT = "preferences_front_light";
	public static final String KEY_BULK_MODE = "preferences_bulk_mode";
	public static final String KEY_REMEMBER_DUPLICATES = "preferences_remember_duplicates";
	public static final String KEY_SUPPLEMENTAL = "preferences_supplemental";
	public static final String KEY_AUTO_FOCUS = "preferences_auto_focus";
	public static final String KEY_SEARCH_COUNTRY = "preferences_search_country";

	public static final String KEY_DISABLE_CONTINUOUS_FOCUS = "preferences_disable_continuous_focus";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		_prefers = preferences(this);
		_prefers.registerOnSharedPreferenceChangeListener(new MySharedPreferenceChangeListener());

	}

	protected SharedPreferences preferences() {
		return _prefers;
	}

	public static SharedPreferences preferences(Context context) {
		return context.getSharedPreferences(_PREF_NAME, MODE_PRIVATE);
	}

	protected class MySharedPreferenceChangeListener implements
			SharedPreferences.OnSharedPreferenceChangeListener {

		@Override
		public void onSharedPreferenceChanged(
				SharedPreferences sharedPreferences, String s) {
		}
	}

}
