package org.zephyrsoft.sdbviewer;

import android.app.Activity;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.v4.app.Fragment;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.style.RelativeSizeSpan;
import android.text.style.StyleSpan;
import android.text.style.TypefaceSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import org.zephyrsoft.sdbviewer.model.Song;
import org.zephyrsoft.sdbviewer.parser.SongParser;

import java.util.List;

/**
 * A fragment representing a single Song detail screen.
 * This fragment is either contained in a {@link SongListActivity}
 * in two-pane mode (on tablets) or a {@link SongDetailActivity}
 * on handsets.
 */
public class SongDetailFragment extends Fragment {

    /**
     * The song this fragment is presenting.
     */
    private Song song;

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public SongDetailFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getArguments() != null && getArguments().containsKey(Constants.ARG_SONG)) {
            song = getArguments().getParcelable(Constants.ARG_SONG);

            Activity activity = this.getActivity();
            if (activity == null) {
                throw new IllegalStateException("activity not found");
            }
            CollapsingToolbarLayout appBarLayout = activity.findViewById(R.id.toolbar_layout);
            if (appBarLayout != null) {
                appBarLayout.setTitle(song.getTitle());
            }
        }
    }

    private boolean getBooleanPreference(String key, boolean defaultValue) {
        SharedPreferences sharedPreferences =
            PreferenceManager.getDefaultSharedPreferences(getContext());
        return sharedPreferences.getBoolean(key, defaultValue);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.song_detail, container, false);

        if (song != null) {
            boolean showTranslation = getBooleanPreference(inflater.getContext().getString(R.string.pref_show_translation), true);
            boolean showChords = getBooleanPreference(inflater.getContext().getString(R.string.pref_show_chords), true);

            List<SongParser.SongElement> parsedSong = SongParser.parseLyrics(song, showChords, showTranslation);
            boolean parsedSongContainsChords = false;
            for (SongParser.SongElement element : parsedSong) {
                if (element.getType() == SongParser.SongElementEnum.CHORDS) {
                    parsedSongContainsChords = true;
                    break;
                }
            }

            SpannableStringBuilder formatted = new SpannableStringBuilder();

            // TODO display lyrics / translation / chords as parsed
            for (SongParser.SongElement element : parsedSong) {
                int start = formatted.length();
                formatted.append(element.getElement());
                int end = formatted.length();

                if (element.getType() == SongParser.SongElementEnum.TRANSLATION) {
                    formatted.setSpan(new RelativeSizeSpan(0.65f), start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                    formatted.setSpan(new StyleSpan(Typeface.ITALIC), start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                }
                if (showChords && parsedSongContainsChords
                    && (element.getType() == SongParser.SongElementEnum.LYRICS || element.getType() == SongParser.SongElementEnum.CHORDS)) {
                    formatted.setSpan(new TypefaceSpan("monospace"), start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                    formatted.setSpan(new RelativeSizeSpan(0.8f), start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                }
            }

            ((TextView) rootView.findViewById(R.id.song_detail)).setText(formatted);
        }

        return rootView;
    }
}
