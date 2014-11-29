package net.pla1.sda;

import android.app.DownloadManager;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Utils {
    public final static String TAG = "SDA";
    private final static String BASE_URL = "https://json.schedulesdirect.org/20140530/";
    final protected static char[] hexArray = "0123456789ABCDEF".toCharArray();

    public static boolean isBlank(String s) {
        if (s == null || s.trim().length() == 0 || "0".equals(s)) {
            return true;
        } else {
            return false;
        }
    }

    public static String getStatusFromPreferences(Context context) {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        StringBuilder sb = new StringBuilder();
        sb.append(sharedPreferences.getString("accountStatus", "")).append("\n");
        DbUtils db = new DbUtils(context);
        sb.append(db.getTableCounts());
        return sb.toString();
    }

    public static boolean isNotBlank(String s) {
        return !isBlank(s);
    }

    public static String validateCredentials(Context context) {
        return "TODO";
    }

    public static String setToken(Context context) {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        String username = sharedPreferences.getString("username", "");
        String password = sharedPreferences.getString("password", "");
        BufferedReader reader = null;
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-1");
            digest.update(password.getBytes("utf8"));
            byte[] digestBytes = digest.digest();
            String digestStr = bytesToHex(digestBytes);
            digestStr = digestStr.toLowerCase();
            HttpPost request = new HttpPost(BASE_URL + "token");
            request.setHeader("Accept", "application/json");
            request.setHeader("Content-type", "application/json");
            request.setHeader("User-Agent", "Patrick.Archibald@gmail.com");
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("username", username);
            jsonObject.put("password", digestStr);
            Log.i(TAG, "JSON input: " + jsonObject.toString());
            StringEntity entity = new StringEntity(jsonObject.toString());
            request.setEntity(entity);
            HttpResponse response = new DefaultHttpClient().execute(request);
            reader = new BufferedReader(new InputStreamReader(response.getEntity().getContent(), "UTF-8"));
            Gson gson = new Gson();
            Token token = gson.fromJson(reader, Token.class);
            sharedPreferences.edit().putString("token", token.getToken()).commit();
            Log.i(TAG, "Token: " + token.toString());
            return token.toString();
        } catch (Exception e) {
            e.printStackTrace();
            return e.getLocalizedMessage();
        } finally {
            close(reader);
        }
    }

    public static boolean checkStatus(Context context) {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        HttpGet request = new HttpGet(BASE_URL + "status");
        request.setHeader("Accept", "application/json");
        request.setHeader("Content-type", "application/json");
        request.setHeader("User-Agent", "Patrick.Archibald@gmail.com");
        String token = sharedPreferences.getString("token", "");
        request.setHeader("token", token);
        Log.i(TAG, "Token: " + token);
        BufferedReader reader = null;
        try {
            HttpResponse response = new DefaultHttpClient().execute(request);
            Log.i(TAG, "Response: " + response.getStatusLine());
            reader = new BufferedReader(new InputStreamReader(response.getEntity().getContent(), "UTF-8"));
            Gson gson = new Gson();
            Status status = gson.fromJson(reader, Status.class);
            Log.i(TAG, "Status object dump: " + Utils.toString(status));
            Log.i(TAG, "checkStatus Status: " + status.toString() + " IS READER CLOSED? " + reader.ready());
            if (status != null && status.getSystemStatus() != null && status.getSystemStatus().size() > 0) {
                if ("Online".equals(status.getSystemStatus().get(0).getStatus())) {
                    return true;
                } else {
                    return false;
                }
            } else {
                return false;
            }
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        } finally {
            close(reader);
        }
    }

    public static String getStatusFromServer(Context context) {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        HttpGet request = new HttpGet(BASE_URL + "status");
        request.setHeader("Accept", "application/json");
        request.setHeader("Content-type", "application/json");
        request.setHeader("User-Agent", "Patrick.Archibald@gmail.com");
        String token = sharedPreferences.getString("token", "");
        request.setHeader("token", token);
        Log.i(TAG, "Token: " + token);
        BufferedReader reader = null;
        try {
            HttpResponse response = new DefaultHttpClient().execute(request);
            Log.i(TAG, "Response: " + response.getStatusLine());
            reader = new BufferedReader(new InputStreamReader(response.getEntity().getContent(), "UTF-8"));
            //    for (String line = reader.readLine(); line != null; line = reader.readLine()) {
            //        Log.i(TAG, "Response line: " + line);
            //    }
            Gson gson = new Gson();
            Status status = gson.fromJson(reader, Status.class);
            Log.i(TAG, "Status object dump: " + Utils.toString(status));
            Log.i(TAG, "checkStatus Status: " + status.toString() + " IS READER CLOSED? " + reader.ready());
            if (status != null && status.getSystemStatus() != null && status.getSystemStatus().size() > 0) {
                return status.toString();
            } else {
                return "Status retrieve failed.";
            }
        } catch (Exception e) {
            e.printStackTrace();
            return e.getLocalizedMessage();
        } finally {
            close(reader);
        }
    }

    public static ArrayList<Station> getStations(Context context, String uri) {
        if (!checkStatus(context)) {
            setToken(context);
        }
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        String url = "https://json.schedulesdirect.org" + uri;
        Log.i(TAG, "URL: " + url);
        HttpGet request = new HttpGet(url);
        request.setHeader("Accept", "application/json");
        request.setHeader("Content-type", "application/json");
        request.setHeader("User-Agent", "Patrick.Archibald@gmail.com");
        String token = sharedPreferences.getString("token", "");
        request.setHeader("token", token);
        Log.i(TAG, "Token: " + token);
        BufferedReader reader = null;
        try {
            HttpResponse response = new DefaultHttpClient().execute(request);
            Log.i(TAG, "Response: " + response.getStatusLine());
            reader = new BufferedReader(new InputStreamReader(response.getEntity().getContent(), "UTF-8"));

            //       for (String line = reader.readLine(); line != null; line = reader.readLine()) {
            //           Log.i(TAG, "Response line: " + line);
            //       }


            Gson gson = new Gson();
            StationResponse stationResponse = gson.fromJson(reader, StationResponse.class);
            Log.i(TAG, stationResponse.toString());
            return stationResponse.getStationsWithChannelNumber();
        } catch (Exception e) {
            e.printStackTrace();
            return new ArrayList<Station>();
        } finally {
            close(reader);
        }

    }

    public static ArrayList<Schedule> downloadSchedule(Context context, Station station) {
        if (!checkStatus(context)) {
            setToken(context);
        }
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        String daysToDownload = sharedPreferences.getString("daysPreference", "2");
        String url = "https://json.schedulesdirect.org/20140530/schedules";
        Log.i(TAG, "URL: " + url);
        HttpPost request = new HttpPost(url);
        request.setHeader("Accept", "application/json");
        request.setHeader("Content-type", "application/json");
        request.setHeader("User-Agent", "Patrick.Archibald@gmail.com");
        String token = sharedPreferences.getString("token", "");
        request.setHeader("token", token);
        Log.i(TAG, "Token: " + token);
        JsonObject jsonObject = new JsonObject();
        JsonArray jsonArray = new JsonArray();
        jsonObject.addProperty("stationID", station.getStationID());
        jsonObject.addProperty("days", Integer.parseInt(daysToDownload));
        jsonArray.add(jsonObject);
        BufferedReader reader = null;
        try {
            StringEntity params = new StringEntity(jsonArray.toString());
            request.addHeader("content-type", "application/json");
            request.setEntity(params);
            HttpResponse response = new DefaultHttpClient().execute(request);
            Log.i(TAG, "Entity params: " + jsonObject.toString() + " Response: " + response.getStatusLine());
            reader = new BufferedReader(new InputStreamReader(response.getEntity().getContent(), "UTF-8"));

            //        for (String line = reader.readLine(); line != null; line = reader.readLine()) {
            //            Log.i(TAG, "Response line: " + line);
            //        }


            Gson gson = new Gson();
            ScheduleResponse scheduleResponse = gson.fromJson(reader, ScheduleResponse.class);
            ArrayList<Schedule> schedules = scheduleResponse.getPrograms();
            Log.i(TAG, scheduleResponse.getStationID() + " program quantity: " + schedules.size());
            int i = 1;
            for (Schedule schedule : schedules) {
                Log.i(TAG, i++ + " " + schedule.toString());
            }
            DbUtils db = new DbUtils(context);
            db.storeSchedule(station.getStationID(), schedules);
            return scheduleResponse.getPrograms();
        } catch (Exception e) {
            e.printStackTrace();
            return new ArrayList<Schedule>();
        } finally {
            close(reader);
        }

    }

    public static void downloadPrograms(Context context, Station station) {
        if (!checkStatus(context)) {
            setToken(context);
        }
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        String url = "https://json.schedulesdirect.org/20140530/programs";
        Log.i(TAG, "URL: " + url);
        HttpPost request = new HttpPost(url);
        request.setHeader("Accept", "application/json");
        request.setHeader("Content-type", "application/json");
        request.setHeader("User-Agent", "Patrick.Archibald@gmail.com");
        request.setHeader("Accept-Encoding", "deflate");
        String token = sharedPreferences.getString("token", "");
        request.setHeader("token", token);
        Log.i(TAG, "Token: " + token);
        BufferedReader reader = null;
        DbUtils db = new DbUtils(context);
        try {
            String programRequest = db.getProgramRequest(station.getStationID());
            if (Utils.isBlank(programRequest)) {
                Log.i(TAG, "Program request is blank. No download of programs is necessary. Station: " + station.toString());
                return;
            }
            StringEntity params = new StringEntity(programRequest);
            request.addHeader("content-type", "application/json");
            request.setEntity(params);
            HttpResponse response = new DefaultHttpClient().execute(request);
            Log.i(TAG, "Entity params: " + programRequest + " Response: " + response.getStatusLine());
            reader = new BufferedReader(new InputStreamReader(response.getEntity().getContent(), "UTF-8"));
            for (String line = reader.readLine(); line != null; line = reader.readLine()) {
                Log.i(TAG, "Response line: " + line);
                JSONObject jsonObject = new JSONObject(line);
                db.storeProgram(jsonObject);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            close(reader);
        }

    }


    public static ArrayList<Lineup> getLineups(Context context) {
        if (!checkStatus(context)) {
            setToken(context);
        }
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        HttpGet request = new HttpGet(BASE_URL + "lineups");
        request.setHeader("Accept", "application/json");
        request.setHeader("Content-type", "application/json");
        request.setHeader("User-Agent", "Patrick.Archibald@gmail.com");
        String token = sharedPreferences.getString("token", "");
        request.setHeader("token", token);
        Log.i(TAG, "Token: " + token);
        BufferedReader reader = null;
        try {
            HttpResponse response = new DefaultHttpClient().execute(request);
            Log.i(TAG, "Response: " + response.getStatusLine());
            reader = new BufferedReader(new InputStreamReader(response.getEntity().getContent(), "UTF-8"));

            //       for (String line = reader.readLine(); line != null; line = reader.readLine()) {
            //           Log.i(TAG, "Response line: " + line);
            //       }


            Gson gson = new Gson();
            Lineups lineups = gson.fromJson(reader, Lineups.class);
            for (Lineup lineup : lineups.getLineups()) {
                Log.i(TAG, "LINEUP: " + gson.toJson(lineup));
            }
            Log.i(TAG, lineups.getLineups().size() + " lineups");
            return lineups.getLineups();
        } catch (Exception e) {
            e.printStackTrace();
            return new ArrayList<Lineup>();
        } finally {
            close(reader);
        }
    }

    public static JsonObject lineupAction(Context context, String action, String uri) {
        Log.i(TAG, "lineupAction action: " + action + " URI: " + uri);
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        String url = "https://json.schedulesdirect.org" + uri;
        Log.i(TAG, "lineupAction Action: " + action + " uri: " + uri + " URL: " + url);
        HttpRequestBase request;
        if ("put".equals(action)) {
            request = new HttpPut(url);
        } else {
            request = new HttpDelete(url);
        }
        request.setHeader("Accept", "application/json");
        request.setHeader("Content-type", "application/json");
        request.setHeader("User-Agent", "Patrick.Archibald@gmail.com");
        String token = sharedPreferences.getString("token", "");
        request.setHeader("token", token);
        Log.i(TAG, "Token: " + token);
        BufferedReader reader = null;
        try {
            HttpResponse response = new DefaultHttpClient().execute(request);
            Log.i(TAG, "Response: " + response.getStatusLine());
            reader = new BufferedReader(new InputStreamReader(response.getEntity().getContent(), "UTF-8"));
            StringBuilder sb = new StringBuilder();
            for (String line = reader.readLine(); line != null; line = reader.readLine()) {
                Log.i(TAG, "Response line: " + line);
                sb.append(line);
            }
            JsonObject jsonObject = (JsonObject) new JsonParser().parse(sb.toString());
            return jsonObject;

        } catch (Exception e) {
            e.printStackTrace();

        } finally {
            close(reader);
        }
        return new JsonObject();
    }

    public static ArrayList<Headend> getHeadends(Context context) {
        if (!checkStatus(context)) {
            setToken(context);
        }
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        String postalCode = sharedPreferences.getString("postalCode", "");
        String token = sharedPreferences.getString("token", "");
        HttpGet request = new HttpGet(BASE_URL + "headends?country=USA&postalcode=" + postalCode);
        request.setHeader("Accept", "application/json");
        request.setHeader("Content-type", "application/json");
        request.setHeader("User-Agent", "Patrick.Archibald@gmail.com");
        request.setHeader("token", token);
        BufferedReader reader = null;
        try {
            HttpResponse response = new DefaultHttpClient().execute(request);
            reader = new BufferedReader(new InputStreamReader(response.getEntity().getContent(), "UTF-8"));
            StringBuilder sb = new StringBuilder();
            for (String line = reader.readLine(); line != null; line = reader.readLine()) {
                Log.i(TAG, "Response line: " + line);
                sb.append(line).append("\n");
            }
            Gson gson = new Gson();
            ArrayList<Headend> headends = new ArrayList<Headend>();
            JSONObject jsonObject = new JSONObject(sb.toString());
            Pattern pattern = Pattern.compile("\"([0-9a-zA-Z]+)\":\\{");
            Matcher matcher = pattern.matcher(sb.toString());
            while (matcher.find()) {
                String headendName = matcher.group(1);
                Log.i(TAG, "FOUND: " + headendName);
                JSONObject headendJsonObject = jsonObject.getJSONObject(headendName);
                Log.i(TAG, "HEADEND JSON OBJECT : " + headendJsonObject.toString());
                Headend headend = gson.fromJson(headendJsonObject.toString(), Headend.class);
                headend.setName(headendName);
                headends.add(headend);
            }
            for (Headend headend : headends) {
                Log.i(TAG, "HEADEND: " + headend.toString());
            }
            Log.i(TAG, headends.size() + " headends");
            return headends;
        } catch (Exception e) {
            e.printStackTrace();
            return new ArrayList<Headend>();
        } finally {
            close(reader);
        }
    }

    public static String bytesToHex(byte[] bytes) {
        char[] hexChars = new char[bytes.length * 2];
        for (int j = 0; j < bytes.length; j++) {
            int v = bytes[j] & 0xFF;
            hexChars[j * 2] = hexArray[v >>> 4];
            hexChars[j * 2 + 1] = hexArray[v & 0x0F];
        }
        return new String(hexChars);
    }

    public static String toString(Object object) {
        Gson gson = new Gson();
        return gson.toJson(object);
    }

    public static void close(Reader reader) {
        if (reader == null) {
            return;
        } else {
            try {
                reader.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public static void saveImageToDisk(Context context, Bitmap bitmap, Station station) {
        FileOutputStream out = null;
        try {
            String fileName = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES) + "/SDALogos/" + station.getCallsign() + ".png";
            File directory = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES) + "/SDALogos");
            directory.mkdirs();
            Log.i(TAG, "saveImageToDisk Station logo file name: " + fileName + " directory exists: " + directory.exists() + " URL: " + station.getLogoUrl());
            out = new FileOutputStream(fileName);
            //    bitmap = Bitmap.createScaledBitmap(bitmap, 100, 75, true);
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, out);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (out != null) {
                    out.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public static void saveImageToDisk(Context context, Station station) {
        if (isBlank(station.getLogoUrl())) {
            return;
        }
        DownloadManager mgr = (DownloadManager) context.getSystemService(Context.DOWNLOAD_SERVICE);
        Uri downloadUri = Uri.parse(station.getLogoUrl());
        DownloadManager.Request request = new DownloadManager.Request(downloadUri);
        request.setAllowedNetworkTypes(DownloadManager.Request.NETWORK_WIFI | DownloadManager.Request.NETWORK_MOBILE)
                .setAllowedOverRoaming(false)
                .setTitle(station.getCallsign())
                .setDescription("Station logo")
                .setDestinationInExternalPublicDir("/SDALogos", station.getCallsign() + ".png");
        mgr.enqueue(request);
    }

    public static Bitmap getImageFromDisk(Context context, Station station) {
        Uri uri = Uri.parse("file://" + Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES) + "/SDALogos/" + station.getCallsign() + ".png");
        try {
            Bitmap bitmap = BitmapFactory.decodeStream(context.getContentResolver().openInputStream(uri));
            return bitmap;
        } catch (FileNotFoundException e) {
        }
        return null;
    }

    public static String getString(JSONObject jsonObject, String fieldName) {
        try {
            return jsonObject.getString(fieldName);
        } catch (JSONException e) {
            return null;
        }
    }
}
