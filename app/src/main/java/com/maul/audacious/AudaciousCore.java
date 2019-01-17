package com.maul.audacious;

import com.jcraft.jsch.ChannelExec;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;

import java.io.InputStream;
import java.io.ByteArrayOutputStream;

import java.util.Properties;

import com.jcraft.jsch.ChannelSftp;

public class AudaciousCore {
    public static void connect(String username, String password, String hostname, int port) throws Exception {

        m_jsch = new JSch();
        m_session = m_jsch.getSession(username, hostname, 22);
        m_session.setPassword(password);

        // Avoid asking for key confirmation
        Properties prop = new Properties();
        prop.put("StrictHostKeyChecking", "no");
        m_session.setConfig(prop);

        m_session.connect();

        sendCommand("mkdir /var/tmp/audtool-remote");

        return;
    }

    public static String sendCommand(String command) throws Exception {
        StringBuilder outputBuffer = new StringBuilder();

        ChannelExec channel = (ChannelExec) m_session.openChannel("exec");
        channel.setCommand(command);
        InputStream commandOutput = channel.getInputStream();
        channel.connect();
        int readByte = commandOutput.read();

        while (readByte != 0xffffffff) {
            outputBuffer.append((char) readByte);
            readByte = commandOutput.read();
        }

        channel.disconnect();

        return outputBuffer.toString();
    }

    public static byte[] getImage(String path) throws  Exception {
        ByteArrayOutputStream imageStream = new ByteArrayOutputStream();

        String extension = path.substring(path.lastIndexOf(".") + 1);
        String name = path.substring(path.lastIndexOf("/") + 1, path.lastIndexOf(".")) + ".jpg";

        switch (extension) {
            case "flac":
                sendCommand("metaflac --export-picture-to=\"/var/tmp/audtool-remote/" + name + "\" \"" + path + "\"");
                break;
            case "mp3":
                    String a = "ffmpeg -i \"" + path + "\" \"/var/tmp/audtool-remote/" + name + "\"";
                    sendCommand(a);
                break;
            case "m4a":
                break;
            default:
                break;
        }

        String file = "/var/tmp/audtool-remote/" + name;

        if (sendCommand("[ -f \"" + file + "\" ] && echo \"true\" || false") == "true\n") {
            ChannelSftp channel = (ChannelSftp) m_session.openChannel("sftp");
            channel.connect();
            channel.get(file, imageStream);
            channel.disconnect();

            return imageStream.toByteArray();
        }


        if (sendCommand("[ -f \"" + path.substring(0, path.lastIndexOf("/") + 1) + "cover.jpg" + "\" ] && echo \"true\" || echo \"false\"").equals("true\n")) {
            ChannelSftp channel = (ChannelSftp) m_session.openChannel("sftp");
            channel.connect();
            channel.get(path.substring(0, path.lastIndexOf("/") + 1) + "cover.jpg", imageStream);
            channel.disconnect();

            return imageStream.toByteArray();
        }

        return null;
    }

    private static JSch m_jsch;
    private static Session m_session;


}
