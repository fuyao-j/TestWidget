package ua.com.akit.widget;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.util.Base64;
import android.util.Log;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.File;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStreamWriter;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

/**
 * Created by Vernadskiy on 16.07.2016.
 */
class Phrases extends AsyncTask<String, Void, String> {

    private final HttpClient httpClient = new DefaultHttpClient();
    //private RemoteViews remoteViews = new RemoteViews(context.getPackageName(), R.layout.main);
    private Context context;
    private DownloadEvent downloadEvent;


    Phrases (Context context) {
        this.context = context;
    }

    protected String doInBackground(String... urls) {
        String line = "Error";
        String[] paths;
        InnerPhrases[] innerPhrases = new InnerPhrases[0];
        boolean isLastVersion = false;
        ArrayList<String> filesOnServer = new ArrayList<String>();
        HttpResponse response;
        BufferedReader reader;

        try {
            if (!App.getInstance().IsDownloaded()) {

                try {
                    File file = new File(context.getFilesDir(), "SvlVersion");
                    String strVersion = "";
                    if (file.exists())
                        strVersion = Phrases.readFromFile("SvlVersion", context);
                    response = httpClient.execute(new HttpGet(urls[0] + "files.txt"));
                    reader = new BufferedReader(new InputStreamReader(response.getEntity().getContent(), "UTF-8"));
                    boolean isFirst = true;
                    int counter = 0;
                    String lastLine = "";
                    while ((line = reader.readLine()) != null) {
                        if (isFirst && line.equals(strVersion)) {
                            isLastVersion = true;
                            break;
                        } else if (isFirst) {
                            isFirst = false;
                            continue;
                        }
                        if (counter % 2 == 0) {
                            lastLine = line;
                        } else {
                            lastLine += "#" + line;
                            filesOnServer.add(lastLine);
                        }
                        isFirst = false;
                        counter++;
                    }
                } catch (Exception e) {
                    Log.w("svl", "Phrases/doInBackground error " + e);
                    // TODO load from file and end (if file doesnt exist - repeat btn)
                }

                if (isLastVersion) {
                    // check files
                    List<File> files = getListFiles(context.getFilesDir());
                    if (files.size() > 1) {
                        innerPhrases = new InnerPhrases[files.size() - 1];
                        int j = 0;
                        for (int i = 0; i < files.size(); i++) {
                            String fileContent = Phrases.readFromFile(files.get(i).getName(), context);
                            if (files.get(i).getName().equals("SvlCurrent") || files.get(i).getName().equals("SvlVersion")) {
                                continue;
                            }
                            innerPhrases[j] = (InnerPhrases) Phrases.fromString(fileContent);
                            j++;
                        }
                        App.getInstance().SetPhrases(innerPhrases);
                        return "";
                    }
                } else {
                    innerPhrases = new InnerPhrases[filesOnServer.size()];
                    paths = new String[filesOnServer.size()];
                    for (int i = 0; i < innerPhrases.length; i++) {
                        String[] temp = filesOnServer.get(i).split("#");
                        innerPhrases[i] = new InnerPhrases(temp[1], temp[0] + ".obb");
                        paths[i] = temp[0] + ".obb";
                    }

                    for (int i = 0; i < paths.length; i++) {
                        innerPhrases[i].Clear();
                        File file = new File(context.getFilesDir(), paths[i]);
                        Log.w("svl", context.getFilesDir().getPath());
                /*if (file.exists()) {
                    file.delete();
                }*/
                        if (file.exists()) {
                            String fileContent = readFromFile(paths[i], context);
                            innerPhrases[i] = (InnerPhrases) fromString(fileContent);
                        } else {
                            response = httpClient.execute(new HttpGet(urls[0] + filesOnServer.get(i).split("#")[0]));
                            reader = new BufferedReader(new InputStreamReader(response.getEntity().getContent(), "UTF-8"));
                            int countPhrases = 0;
                            String[] splitedPhrases = new String[2];
                            while ((line = reader.readLine()) != null) {
                                Log.w("svl", "Phrases/doInBackground line" + i + ": " + line);
                                splitedPhrases[countPhrases] = line;
                                countPhrases++;
                                if(countPhrases % 2 == 0){
                                    innerPhrases[i].Add(splitedPhrases[0], splitedPhrases[1]);
                                    countPhrases = 0;
                                }

                            }
                            if (file.createNewFile())
                                writeToFile(toString(innerPhrases[i]), paths[i], context);
                        }
                    }
                }


                App.getInstance().SetPhrases(innerPhrases);
            }
            return "";
        } catch (Exception e) {
            Log.w("svl", "Phrases/doInBackground Error: " + e.toString());
        }
        return "Error";
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

    void SetOnExecuteEnd (DownloadEvent downloadEvent) {
        this.downloadEvent = downloadEvent;
    }

    void SetCurrentFile (int currentPhrases) {
        writeToFile(String.valueOf(currentPhrases), "SvlCurrent", context);
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

    static String readFromFile(String path, Context context) {

        String ret = "";

        try {
            InputStream inputStream = context.openFileInput(path);

            if ( inputStream != null ) {
                InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
                BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
                String receiveString;
                StringBuilder stringBuilder = new StringBuilder();
                while ((receiveString = bufferedReader.readLine()) != null) {
                    stringBuilder.append(receiveString);
                }
                inputStream.close();
                ret = stringBuilder.toString();
            }
        }
        catch (FileNotFoundException e) {
            Log.w("svl", "File not found: " + e.toString());
        } catch (IOException e) {
            Log.w("svl", "Can not read file: " + e.toString());
        }

        return ret;
    }

    static Object fromString( String s ) throws IOException ,
            ClassNotFoundException {
        byte [] data = Base64.decode(s, Base64.DEFAULT);
        ObjectInputStream ois = new ObjectInputStream(
                new ByteArrayInputStream(  data ) );
        Object o  = ois.readObject();
        ois.close();
        return o;
    }

    private String toString( Serializable o ) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream( baos );
        oos.writeObject( o );
        oos.close();
        return Base64.encodeToString(baos.toByteArray(), Base64.DEFAULT);
    }

    @Override
    protected void onPostExecute(String result) {
        try {
            if (!result.equals("Error")) {
                //Toast.makeText(context, MainActivity.DOWNLOAD_END__PHRASES, Toast.LENGTH_LONG).show();
            } else {
                Toast.makeText(context, MainActivity.ERROR_CONNECTION__PHRASES, Toast.LENGTH_LONG).show();
            }
            downloadEvent.OnDownloaded();
            Log.w("svl", "Загрузка завершена");
        } catch (Exception e) {
            Log.w("svl", "Phrases/onPostExecute Error: " + e);
        }
    }

    @Override
    protected void onPreExecute() {
        try {
            Toast.makeText(context, MainActivity.DOWNLOAD_PHRASES__PHRASES, Toast.LENGTH_SHORT).show();
            Log.w("svl", "Загрузка фраз...");
        } catch (Exception e) {
            Log.w("svl", "Phrases/onPreExecute Error: " + e);
        }
    }
}
