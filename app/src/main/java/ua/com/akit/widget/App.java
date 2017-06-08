package ua.com.akit.widget;

import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

/**
 * Created by Vernadskiy on 25.10.2016.
 */

public class App extends Application {

    private static App singleton;

    private InnerPhrases[] innerPhrases;
    private int currentPhrases = -1;

    public static App getInstance() {
        return singleton;
    }

    @Override
    public final void onCreate() {
        super.onCreate();
        singleton = this;
    }

    boolean IsDownloaded () {
        if (innerPhrases != null) {
            return true;
        }
        return false;
    }

    int GetCurrent () {
        return currentPhrases;
    }

    void SetCurrent (int currentPhrases) {
        this.currentPhrases = currentPhrases;
    }

    void SetPhrases (InnerPhrases[] innerPhrases) {
        this.innerPhrases = innerPhrases;
    }

    InnerPhrases[] GetPhrases () {
        return innerPhrases;
    }

    String GetLearned () {
        try {
            if (currentPhrases != -1)
                return MainActivity.LEARNED__PHRASES + ((int) Math.round(innerPhrases[currentPhrases].GetLearned() * 100.0)) + "%";
        } catch (Exception e) {
            Log.w("svl", "PhrasesWidget/GetLearned Error: " + e.toString());
        }
        return MainActivity.LEARNED__PHRASES + "100%";
    }

    String GetPhrase (MyWidgetProvider.EventGetPhrase eventGetPhrase, Context context) {
        try {
            if (currentPhrases != -1) {
                if (innerPhrases[currentPhrases].IsLearned()) {                                 // if there arent any phrases - open app
                    TurnOnApp(context);
                    return MainActivity.ALL_PHRASES_LEARNED__PHRASES;
                }
                if (eventGetPhrase == MyWidgetProvider.EventGetPhrase.Yes) {
                    innerPhrases[currentPhrases].Learn();
                    if (!innerPhrases[currentPhrases].IsLearned()) {
                        return innerPhrases[currentPhrases].Next();
                    }
                    TurnOnApp(context);
                    return MainActivity.ALL_PHRASES_LEARNED__PHRASES;
                } else {
                    return innerPhrases[currentPhrases].Next();
                }
            }
            TurnOnApp(context);
            return MainActivity.ALL_PHRASES_LEARNED__PHRASES;
        } catch (Exception e) {
            Log.w("svl", "PhrasesWidget/GetPhrase Error: " + e.toString());
        }
        return "Error";
    }

    private void TurnOnApp (Context context) {
        Intent intent = new Intent (context, MainActivity.class);
        intent.setFlags (Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity (intent);
    }
}
