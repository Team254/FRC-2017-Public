package com.team254.cheezdroid;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.preference.PreferenceManager;
import android.util.Pair;

public class Preferences {

    private Context m_context;
    SharedPreferences m_prefs;

    private Pair<Integer, Integer> m_h_ranges;
    private Pair<Integer, Integer> m_s_ranges;
    private Pair<Integer, Integer> m_v_ranges;

    public Preferences(Context context) {
        m_context = context;
        m_prefs = PreferenceManager.getDefaultSharedPreferences(context);
    }

    private void setInt(String key, int value) {
        SharedPreferences.Editor editor = m_prefs.edit();
        editor.putInt(key, value);
        editor.commit();
    }

    private int getInt(String key, int defaultValue) {
        return m_prefs.getInt(key, defaultValue);
    }

    public void setThresholdHRange(int min, int max) {
        setInt(m_context.getString(R.string.threshold_h_min_key), min);
        setInt(m_context.getString(R.string.threshold_h_max_key), max);
        m_h_ranges = new Pair<>(min, max);
    }

    public void setThresholdSRange(int min, int max) {
        setInt(m_context.getString(R.string.threshold_s_min_key), min);
        setInt(m_context.getString(R.string.threshold_s_max_key), max);
        m_s_ranges = new Pair<>(min, max);
    }

    public void setThresholdVRange(int min, int max) {
        setInt(m_context.getString(R.string.threshold_v_min_key), min);
        setInt(m_context.getString(R.string.threshold_v_max_key), max);
        m_v_ranges = new Pair<>(min, max);
    }

    public Pair<Integer, Integer> getThresholdHRange() {
        if (m_h_ranges == null) {
            Resources res = m_context.getResources();
            m_h_ranges = new Pair<>(getInt(m_context.getString(R.string.threshold_h_min_key), res.getInteger(R.integer.default_h_min)),
                    getInt(m_context.getString(R.string.threshold_h_max_key), res.getInteger(R.integer.default_h_max)));
        }
        return m_h_ranges;
    }

    public Pair<Integer, Integer> getThresholdSRange() {
        if (m_s_ranges == null) {
            Resources res = m_context.getResources();
            m_s_ranges = new Pair<>(getInt(m_context.getString(R.string.threshold_s_min_key), res.getInteger(R.integer.default_s_min)),
                    getInt(m_context.getString(R.string.threshold_s_max_key), res.getInteger(R.integer.default_s_max)));
        }
        return m_s_ranges;
    }

    public Pair<Integer, Integer> getThresholdVRange() {
        if (m_v_ranges == null) {
            Resources res = m_context.getResources();
            m_v_ranges = new Pair<>(getInt(m_context.getString(R.string.threshold_v_min_key), res.getInteger(R.integer.default_v_min)),
                    getInt(m_context.getString(R.string.threshold_v_max_key), res.getInteger(R.integer.default_v_max)));
        }
        return m_v_ranges;
    }

    public void restoreDefaults() {
        Resources res = m_context.getResources();
        setThresholdHRange(res.getInteger(R.integer.default_h_min), res.getInteger(R.integer.default_h_max));
        m_h_ranges = null;
        setThresholdSRange(res.getInteger(R.integer.default_s_min), res.getInteger(R.integer.default_s_max));
        m_s_ranges = null;
        setThresholdVRange(res.getInteger(R.integer.default_v_min), res.getInteger(R.integer.default_v_max));
        m_v_ranges = null;

    }

}
