package ua.com.akit.widget;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends Activity implements DownloadEvent {

    public static final String ALL_PHRASES_LEARNED__PHRASES = "Все фразы выучены.";
    public static final String DOWNLOAD_END__PHRASES = "Загрузка завершена";
    public static final String ERROR_CONNECTION__PHRASES = "Ошибка соединения, интернет включен?";
    public static final String DOWNLOAD_PHRASES__PHRASES = "Загрузка фраз...";
    public static final String LEARNED__PHRASES = "Вивчено: ";

    public static final String URL = "http://akit.pl/android/";

    Phrases phrases;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        try {
            super.onCreate(savedInstanceState);
            setContentView(R.layout.main);
            onBtnAgain(null);
        } catch (Exception e) {
            Log.w("svl", "Error: " + e.toString());
        }
    }

    @Override
    public void onResume () {
        super.onResume();
        try {
            App app = App.getInstance();
            if (app.IsDownloaded())
                SetTextLearned(app.GetCurrent(), app.GetLearned());
        } catch (Exception e) {
            Log.w("svl", "onResume Error: " + e.toString());
        }
    }

    @Override
    public void OnDownloaded () {
        InnerPhrases[] innerPhrases = App.getInstance().GetPhrases();
        TextView loadingText = (TextView)findViewById(R.id.loading_text);
        loadingText.setVisibility(View.GONE);
        Button btnAgain = (Button)findViewById(R.id.btn_again);
        btnAgain.setVisibility(View.GONE);
        if (innerPhrases == null || innerPhrases.length == 0) {
            btnAgain.setVisibility(View.VISIBLE);
            return;
        }
        for (int i = 0; i < innerPhrases.length; i++) {
            addNextDictionary(innerPhrases[i].GetHeader(), i, (int)(innerPhrases[i].GetLearned() * 100.0));

        }
    }

    public void onBtnAgain(View view) {
        phrases = new Phrases(getApplicationContext());
        phrases.SetOnExecuteEnd(this);
        phrases.execute(URL);
    }

    private void SetTextLearned (int id, String learned) {
        TextView textViewLearned = (TextView) findViewById(id);
        textViewLearned.setText(learned);
    }

    private void addNextDictionary (String header, int learnedTextId, int percentLearn) {
        LinearLayout linearLayout = new LinearLayout(getApplicationContext());
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        linearLayout.setLayoutParams(layoutParams);
        linearLayout.setOrientation(LinearLayout.VERTICAL);

        TextView name = new TextView(getApplicationContext());
        name.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        ));
        name.setText(header);
        name.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 20);
        linearLayout.addView(name);

        LinearLayout linearLayoutBot = new LinearLayout(getApplicationContext());
        linearLayoutBot.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        ));
        linearLayoutBot.setOrientation(LinearLayout.HORIZONTAL);
        linearLayout.addView(linearLayoutBot);

        TextView learnedText = new TextView(getApplicationContext());
        learnedText.setLayoutParams(layoutParams);
        String temp = "Вивчено: " + percentLearn + "%";
        learnedText.setText(temp);
        learnedText.setId(learnedTextId);
        learnedText.setPadding(0, 0, 5, 0);
        learnedText.setGravity(Gravity.CENTER);
        linearLayoutBot.addView(learnedText);

        Button btn = new Button(getApplicationContext());
        btn.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT
        ));
        btn.setOnClickListener(new MyOnClickListener(learnedTextId, header));
        btn.setText("Обрати");
        btn.setPadding(10, 4, 10, 4);
        btn.setBackgroundColor(0xffffffff);
        btn.setTextColor(0xff222222);
        btn.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 10);
        btn.setGravity(Gravity.CENTER);
        linearLayoutBot.addView(btn);

        LinearLayout main = (LinearLayout)findViewById(R.id.main);
        main.addView(linearLayout);
    }

    class MyOnClickListener implements View.OnClickListener {

        private int number;
        private String header;

        MyOnClickListener (int number, String header) {
            this.number = number;
            this.header = header;
        }

        @Override
        public void onClick (View v) {
            phrases.SetCurrentFile(number);
            App.getInstance().SetCurrent(number);
            Toast.makeText(getApplicationContext(), "Выбрано \"" + header + "\"", Toast.LENGTH_LONG).show();
        }
    }
}

interface DownloadEvent {
    void OnDownloaded ();
}