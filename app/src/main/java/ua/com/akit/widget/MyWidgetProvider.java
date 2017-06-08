package ua.com.akit.widget;



import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.util.Base64;
import android.util.Log;
import android.widget.RemoteViews;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.OutputStreamWriter;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;





public class MyWidgetProvider extends AppWidgetProvider implements DownloadEventWidget {


	@Override
	public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
		try {
			Log.w("svl", "MyWidgetProvider/onUpdate called");
			PhrasesWidget phrases = new PhrasesWidget(context);
			phrases.SetOnExecuteEnd(this);
			phrases.doInBackground();
			ComponentName thisWidget = new ComponentName(context, MyWidgetProvider.class);
			int[] allWidgetIds = appWidgetManager.getAppWidgetIds(thisWidget);

			for (int widgetId : allWidgetIds) {
				RemoteViews remoteViews = new RemoteViews(context.getApplicationContext().getPackageName(), R.layout.widget_layout);

				Intent intentNo = new Intent(context.getApplicationContext(), MyWidgetProvider.class);
				intentNo.setAction("ClickNo");
				PendingIntent pendingIntentNo = PendingIntent.getBroadcast(context.getApplicationContext(), 0, intentNo, 0);
				remoteViews.setOnClickPendingIntent(R.id.btn_no, pendingIntentNo);

				Intent intentYes = new Intent(context.getApplicationContext(), MyWidgetProvider.class);
				intentYes.setAction("ClickYes");
				PendingIntent pendingIntentYes = PendingIntent.getBroadcast(context.getApplicationContext(), 0, intentYes, 0);
				remoteViews.setOnClickPendingIntent(R.id.btn_yes, pendingIntentYes);

				Intent intentOpts = new Intent(context.getApplicationContext(), MyWidgetProvider.class);
				intentOpts.setAction("ClickOpts");
				PendingIntent pendingIntentOpts = PendingIntent.getBroadcast(context.getApplicationContext(), 0, intentOpts, 0);
				remoteViews.setOnClickPendingIntent(R.id.opts, pendingIntentOpts);

				appWidgetManager.updateAppWidget(widgetId, remoteViews);
			}

		} catch (Exception e) {
			e.printStackTrace();
			Log.w("svl", "MyWidgetProvider/onUpdate Error: " + e.toString() + "\n");
		}
	}

	@Override
	public void OnDownloaded (Context context) {
		clickNo(context);
	}

	@Override
	public void onReceive (Context context, Intent intent) {
		super.onReceive(context, intent);
		Log.w("svl", "onReceive Called: " + intent.getAction());
		if (intent.getAction().equals("ClickYes")) {
			Log.w("svl", "Clicked Yes");
			if (!App.getInstance().IsDownloaded()) {
				PhrasesWidget phrases = new PhrasesWidget(context);
				phrases.SetOnExecuteEnd(this);
				phrases.doInBackground();
			} else {
				clickYes(context);
			}
		} else if (intent.getAction().equals("ClickNo")) {
			Log.w("svl", "Clicked No");
			if (!App.getInstance().IsDownloaded()) {
				PhrasesWidget phrases = new PhrasesWidget(context);
				phrases.SetOnExecuteEnd(this);
				phrases.doInBackground();
			} else {
				clickNo(context);
			}
		} else if (intent.getAction().equals("ClickOpts")) {
			Log.w("svl", "Clicked Options");
			try {
				TurnOnApp(context);
			} catch (Exception e) {
				Log.w("svl", "Error " + e);
			}
		} else if (intent.getAction().equals("android.appwidget.action.APPWIDGET_DISABLED")) {
			Log.w("svl", "On Delete Widget");
			try {
				Write(context);
			} catch (Exception e) {
				Log.w("svl", "MyWidgetProvider/OnDelete Error " + e);
			}
		} else {
			ComponentName thisWidget = new ComponentName(context, MyWidgetProvider.class);
			AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context.getApplicationContext());
			int[] allWidgetIds = appWidgetManager.getAppWidgetIds(thisWidget);
			for (int widgetId : allWidgetIds) {
				RemoteViews remoteViews = new RemoteViews(context.getApplicationContext().getPackageName(), R.layout.widget_layout);
				remoteViews.setTextViewText(R.id.update, App.getInstance().GetPhrase(EventGetPhrase.No, context));
				remoteViews.setTextViewText(R.id.learned, App.getInstance().GetLearned());
				appWidgetManager.updateAppWidget(widgetId, remoteViews);
			}
		}
	}

	void TurnOnApp (Context context) {
		Intent intent = new Intent (context, MainActivity.class);
		intent.setFlags (Intent.FLAG_ACTIVITY_NEW_TASK);
		context.startActivity (intent);
	}

	private void clickYes (Context context) {
		ComponentName thisWidget = new ComponentName(context, MyWidgetProvider.class);
		AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context.getApplicationContext());
		int[] allWidgetIds = appWidgetManager.getAppWidgetIds(thisWidget);
		for (int widgetId : allWidgetIds) {
			RemoteViews remoteViews = new RemoteViews(context.getApplicationContext().getPackageName(), R.layout.widget_layout);
			remoteViews.setTextViewText(R.id.update, App.getInstance().GetPhrase(EventGetPhrase.Yes, context));
			remoteViews.setTextViewText(R.id.learned, App.getInstance().GetLearned());
			appWidgetManager.updateAppWidget(widgetId, remoteViews);
		}
	}

	private void clickNo (Context context) {
		try {
			ComponentName thisWidget = new ComponentName(context, MyWidgetProvider.class);
			AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context.getApplicationContext());
			int[] allWidgetIds = appWidgetManager.getAppWidgetIds(thisWidget);
			for (int widgetId : allWidgetIds) {
				RemoteViews remoteViews = new RemoteViews(context.getApplicationContext().getPackageName(), R.layout.widget_layout);
				remoteViews.setTextViewText(R.id.update, App.getInstance().GetPhrase(EventGetPhrase.No, context));
				remoteViews.setTextViewText(R.id.learned, App.getInstance().GetLearned());
				appWidgetManager.updateAppWidget(widgetId, remoteViews);
			}
		} catch (Exception e) {
			Log.w("svl", "MyWidgetProvider/clickNo Error: " + e);
		}
	}

	void Write (Context context) {
		try {
			InnerPhrases[] innerPhrases = App.getInstance().GetPhrases();
			for (int i = 0; i < innerPhrases.length; i++) {
				writeToFile(toString(innerPhrases[i]), innerPhrases[i].GetFilename(), context);
			}
		} catch (Exception e) {
			Log.w("svl", "PhrasesWidget/Write Error: " + e);
		}
	}

	private void writeToFile(String data, String path, Context context) {
		try {
			OutputStreamWriter outputStreamWriter = new OutputStreamWriter(context.openFileOutput(path, Context.MODE_PRIVATE));
			outputStreamWriter.write(data);
			outputStreamWriter.close();
		}
		catch (IOException e) {
			Log.w("svl", "File write failed: " + e.toString());
		}
	}

	private String toString( Serializable o ) throws IOException {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		ObjectOutputStream oos = new ObjectOutputStream( baos );
		oos.writeObject( o );
		oos.close();
		return Base64.encodeToString(baos.toByteArray(), Base64.DEFAULT);
	}

	class PhrasesWidget extends AsyncTask<String, Void, String> {

		private DownloadEventWidget downloadEvent;
		private Context context;

		PhrasesWidget (Context context) {
			this.context = context;
		}

		protected String doInBackground(String... params) {
			try {
				if (!App.getInstance().IsDownloaded()) {
					InnerPhrases[] innerPhrases;
					List<File> files = getListFiles(context.getFilesDir());
					Log.w("svl", "PhrasesWidget/doInBackground size: " + files.size());
					if (files.size() > 1) {
						innerPhrases = new InnerPhrases[files.size() - 1];
						int j = 0;
						for (int i = 0; i < files.size(); i++) {
							String fileContent = Phrases.readFromFile(files.get(i).getName(), context);
							//Log.w("svl", "PhrasesWidget " + fileContent);
							if (files.get(i).getName().equals("SvlCurrent") || files.get(i).getName().equals("SvlVersion")) {
								App.getInstance().SetCurrent(Integer.parseInt(fileContent));
								Log.w("svl", "PhrasesWidget/doInBackground current file");
								continue;
							}
							innerPhrases[j] = (InnerPhrases) Phrases.fromString(fileContent);
							j++;
						}
						App.getInstance().SetPhrases(innerPhrases);
					} else {
						TurnOnApp(context);
					}
				}
			} catch (Exception e) {
				Log.w("svl", "PhrasesWidget/doInBackground Error: " + e);
			}
			return null;
		}

		@Override
		protected void onPostExecute(String result) {
			try {
				downloadEvent.OnDownloaded(context);
				Log.w("svl", "PhrasesWidget/onPostExecute Загрузка завершена");
			} catch (Exception e) {
				Log.w("svl", "PhrasesWidget/onPostExecute Error: " + e);
			}
		}

		void SetOnExecuteEnd (DownloadEventWidget downloadEvent) {
			this.downloadEvent = downloadEvent;
		}

		private List<File> getListFiles(File parentDir) {
			ArrayList<File> inFiles = new ArrayList<File>();
			File[] files = parentDir.listFiles();
			for (File file : files) {
				if (file.isDirectory()) {
					inFiles.addAll(getListFiles(file));
				} else {
					inFiles.add(file);
				}
			}
			return inFiles;
		}
	}

	enum EventGetPhrase {
		Yes, No
	}
}

interface DownloadEventWidget {
	void OnDownloaded (Context context);
}