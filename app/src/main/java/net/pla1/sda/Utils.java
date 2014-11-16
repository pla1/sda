package net.pla1.sda;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;

import com.google.gson.Gson;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
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
            BufferedReader reader = new BufferedReader(new InputStreamReader(response.getEntity().getContent(), "UTF-8"));
            Gson gson = new Gson();
            Token token = gson.fromJson(reader, Token.class);
            sharedPreferences.edit().putString("token", token.getToken()).commit();
            Log.i(TAG, "Token: " + token.toString());
            return token.toString();
        } catch (Exception e) {
            e.printStackTrace();
            return e.getLocalizedMessage();
        }
    }

    public static String checkStatus(Context context) {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        HttpGet request = new HttpGet(BASE_URL + "status");
        request.setHeader("Accept", "application/json");
        request.setHeader("Content-type", "application/json");
        request.setHeader("User-Agent", "Patrick.Archibald@gmail.com");
        String token = sharedPreferences.getString("token", "");
        request.setHeader("token", token);
        Log.i(TAG, "Token: " + token);
        try {
            HttpResponse response = new DefaultHttpClient().execute(request);
            Log.i(TAG, "Response: " + response.getStatusLine());
            BufferedReader reader = new BufferedReader(new InputStreamReader(response.getEntity().getContent(), "UTF-8"));
            for (String line = reader.readLine(); line != null; line = reader.readLine()) {
                Log.i(TAG, "Response line: " + line);
            }
            Gson gson = new Gson();
            Status status = gson.fromJson(reader, Status.class);
            Log.i(TAG, "checkStatus Status: " + status.toString() + " IS READER CLOSED? " + reader.ready());
            if (status != null && status.getSystemStatus() != null && status.getSystemStatus().size() > 0) {
                return status.getSystemStatus().get(0).getStatus();
            } else {
                return "Status retrieve failed.";
            }
        } catch (Exception e) {
            e.printStackTrace();
            return e.getLocalizedMessage();
        }
    }

    public static String getHeadends(Context context) {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        String postalCode = sharedPreferences.getString("postalCode", "");
        String token = sharedPreferences.getString("token", "");
        HttpGet request = new HttpGet(BASE_URL + "headends?country=USA&postalcode=" + postalCode);
        request.setHeader("Accept", "application/json");
        request.setHeader("Content-type", "application/json");
        request.setHeader("User-Agent", "Patrick.Archibald@gmail.com");
        request.setHeader("token", token);
        try {
            HttpResponse response = new DefaultHttpClient().execute(request);
            BufferedReader reader = new BufferedReader(new InputStreamReader(response.getEntity().getContent(), "UTF-8"));
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
            return sb.toString();
        } catch (Exception e) {
            e.printStackTrace();
            return e.getLocalizedMessage();
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
}
