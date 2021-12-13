package com.maul.audacious;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.View;

import com.jcraft.jsch.ChannelExec;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;

import java.io.IOException;
import java.io.InputStream;
import java.io.ByteArrayOutputStream;

import java.util.Properties;
import java.util.concurrent.CountDownLatch;

import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.SftpException;

public class AudaciousCore {
    public static void connect(String username, String password, String hostname, int port) {
        try {
            m_jsch = new JSch();
            m_session = m_jsch.getSession(username, hostname, port);
            m_session.setPassword(password);

            // Avoid asking for key confirmation
            Properties prop = new Properties();
            prop.put("StrictHostKeyChecking", "no");
            m_session.setConfig(prop);

            m_session.connect();

            sendCommand("mkdir /var/tmp/audtool-remote");

        } catch (JSchException e) {
            Log.w("AudaciousCore", e.getMessage());
        }
    }

    public static boolean isPlaying() {
        return sendCommand("audtool playback-status").equals("playing\n");
    }

    public static int getVolume() {
        tmp = sendCommand("audtool get-volume");

        return stringToInt(tmp);
    }

    public static String getCurrentSong() {
        return sendCommand("audtool current-song");
    }

    public static int getCurrentSongLength() {
        tmp = sendCommand("audtool current-song-length-seconds");

        return stringToInt(tmp);
    }

    public static int getCurrentSongOutputLength() {
        tmp = sendCommand("audtool current-song-output-length-seconds");

        return stringToInt(tmp);
    }

    public static String getCurrentSongFilename() {
        return sendCommand("audtool current-song-filename");
    }

    public static String getPlaylistSong(int arg) {
        String tmp = sendCommand("audtool playlist-song " + (arg + 1));
        return tmp.substring(0, tmp.length() - 1);
    }

    public static String getPlaylistSongLength(int arg){
        String tmp = sendCommand("audtool playlist-song-length " + (arg + 1));
        return tmp.substring(0, tmp.length() - 1);
    }

    public static void playlistDelete(int arg){
        sendCommand("audtool playlist-delete " + (arg + 1));
    }

    public static void playlistJump(int arg){
        sendCommand("audtool playlist-jump " + (arg + 1));
    }

    public static void playlistClear(){
        sendCommand("audtool playlist-clear");
    }

    public static int getPlaylistLength(){
        return stringToInt(sendCommand("audtool playlist-length"));
    }

    public static int getPlaylistPosition(){
        return stringToInt(sendCommand("audtool playlist-position")) - 1;
    }


    public static boolean isConnected() {
        return m_session.isConnected();
    }

    public static void setVolume(int arg) {
        sendCommand("audtool set-volume " + arg);
    }

    public static void setPlaybackSeek(int arg) {
        sendCommand("audtool playback-seek " + arg);
    }

    public static void playNext() {
        sendCommand("audtool playlist-advance");
    }

    public static void playPrevious() {
        sendCommand("audtool playlist-reverse");
    }

    public static void playPause() {
        sendCommand("audtool playback-playpause");
    }

    private static int stringToInt(String arg) {
        try {
                return Integer.parseInt(arg.substring(0, arg.length() - 1));
        } catch (Exception e) {
            if(arg != null)
                Log.w("AudaciousCore", "Failed to convert \"" + arg + "\" to integer.");
            return 0;
        }
    }

    public static String sendCommand(String command) {
        StringBuilder outputBuffer;
        ChannelExec channel = null;
        InputStream output;
        String result;
        try {
            outputBuffer = new StringBuilder();

            channel = (ChannelExec) m_session.openChannel("exec");
            channel.setCommand(command);
            output = channel.getInputStream();
            channel.connect();
            int readByte = output.read();

            while (readByte != 0xffffffff) {
                outputBuffer.append((char) readByte);
                readByte = output.read();
            }

            result = outputBuffer.toString();
        } catch (JSchException | IOException e) {
            Log.w("AudaciousCore", "Failed to execute command " + command + " due to" + e.toString());
            result = "";
        } finally {
            if (channel != null)
                channel.disconnect();
        }

        return result;
    }

    public static byte[] getImageRaw(String path) {
        if(path == null)
            return null;
        if(path.length() < 2)
            return null;

        String extension = path.substring(path.lastIndexOf(".") + 1, path.length() - 1);
        String name = path.substring(path.lastIndexOf("/") + 1, path.lastIndexOf(".")) + ".jpg";

        switch (extension) {
            case "flac":
                sendCommand("metaflac --export-picture-to=\"/var/tmp/audtool-remote/" + name + "\" \"" + path + "\"");
                break;
            case "mp3":
                String a = "ffmpeg -i \"" + path + "\" \"/var/tmp/audtool-remote/" + name + "\"";
                sendCommand(a);
                break;
            //case "m4a":
            //   break;
            default:
                break;
        }

        String file = "/var/tmp/audtool-remote/" + name;

        if (sendCommand("[ -f \"" + file + "\" ] && echo \"true\" || false").equals("true\n"))
            return ftpGet(file);

        if (sendCommand("[ -f \"" + path.substring(0, path.lastIndexOf("/") + 1) + "cover.jpg" + "\" ] && echo \"true\" || echo \"false\"").equals("true\n"))
            return ftpGet(path.substring(0, path.lastIndexOf("/") + 1) + "cover.jpg");

        String f = "";
        String cmd = sendCommand("ls \"" + path.substring(0, path.lastIndexOf("/") + 1) + "\"*.jpg");

        if (cmd.indexOf("\n") > 0)
            f = cmd.substring(0, cmd.indexOf("\n"));
        else {
            cmd = sendCommand("ls \"" + path.substring(0, path.lastIndexOf("/") + 1) + "\"*.jpeg");

            if (cmd.indexOf("\n") > 0)
                f = cmd.substring(0, cmd.indexOf("\n"));
            else {
                cmd = sendCommand("ls \"" + path.substring(0, path.lastIndexOf("/") + 1) + "\"*.png");

                if (cmd.indexOf("\n") > 0)
                    f = cmd.substring(0, cmd.indexOf("\n"));
            }
        }

        if (!f.equals(""))
            return ftpGet(f);

        return null;
    }

    private static byte[] ftpGet(String path) {
        ByteArrayOutputStream result = new ByteArrayOutputStream();
        ChannelSftp channel = null;

        try {
            channel = (ChannelSftp) m_session.openChannel("sftp");
            channel.connect();
            channel.get(path, result);

        } catch (JSchException e) {
            Log.w("AudaciousCore", "Failed connect through sftp");
            return null;
        } catch (SftpException e) {
            Log.w("AudaciousCore", "Failed to get " + path);
            return null;
        } finally {
            if (channel != null)
                channel.disconnect();
        }

        return result.toByteArray();
    }

    private static String tmp;
    private static JSch m_jsch;
    private static Session m_session;

}
