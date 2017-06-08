package ua.com.akit.widget;

import android.util.Log;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Random;

/**
 * Created by Vernadskiy on 21.10.2016.
 */

public class InnerPhrases implements Serializable {

    private static final long serialVersionUID = 2641232848710680759L;
    private String fileName;
    private ArrayList<String> unknownedNative = new ArrayList<String>();
    private ArrayList<String> unknownedTranslate = new ArrayList<String>();
    private int currentPhrase = 0;
    private int size = 0;
    private String header;

    public InnerPhrases (String header, String fileName) {
        this.header = header;
        this.fileName = fileName;
    }

    public int Size () {
        return size;
    }

    String GetFilename () {
        return fileName;
    }

    public void Clear () {
        size = 0;
        unknownedNative.clear();
        unknownedTranslate.clear();
        currentPhrase = 0;
    }

    public String GetHeader () {
        return header;
    }

    public float GetLearned () {
        return (float)(size - unknownedTranslate.size()) / (float)size;
    }

    public void Add (String phraseNative, String phraseTranslate) {
        unknownedNative.add(phraseNative);
        unknownedTranslate.add(phraseTranslate);
        size++;
    }

    public void Learn () {
        unknownedNative.remove(currentPhrase);
        unknownedTranslate.remove(currentPhrase);
    }

    public String Next () {
        Log.w("svl", "InnerPhrases/Next " + unknownedNative.size());
        int number = (new Random().nextInt(unknownedTranslate.size()));
        currentPhrase = number;
        return unknownedNative.get(number) + "\n***\n" + unknownedTranslate.get(number);
    }

    public boolean IsLearned () {
        Log.w("svl", "InnerPhrases/IsLearned " + unknownedNative.size());
        if (unknownedTranslate.isEmpty()) return true;
        return false;
    }
}
