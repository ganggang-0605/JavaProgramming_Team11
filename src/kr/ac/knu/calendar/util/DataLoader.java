package kr.ac.knu.calendar.util;

import kr.ac.knu.calendar.model.AcademicSchedule;
import kr.ac.knu.calendar.model.ScheduleManager;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class DataLoader {
    public static void loadKnuSchedules(ScheduleManager manager) {
        String targetUrl = "https://www.knu.ac.kr/wbbs/wbbs/user/yearSchedule/index.action?menu_idx=43";
        try {
            URL url = new URL(targetUrl);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.setRequestProperty("User-Agent", "Mozilla/5.0");
            conn.setConnectTimeout(5000);

            if (conn.getResponseCode() == 200) {
                BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream(), "UTF-8"));
                StringBuilder sb = new StringBuilder();
                String line;
                while ((line = br.readLine()) != null) sb.append(line);
                br.close();

                String html = sb.toString();
                int start = html.indexOf("<div id=\"calendar\">");
                if (start != -1) html = html.substring(start);

                int idx = 0;
                while ((idx = html.indexOf("<li>", idx)) != -1) {
                    int end = html.indexOf("</li>", idx);
                    if (end == -1) break;

                    String item = html.substring(idx, end);
                    String date = parse(item, "<span class=\"day\">", "</span>");

                    int contentStart = item.indexOf("</span>");
                    if (contentStart != -1) {
                        contentStart += 7;
                        String content = item.substring(contentStart).replaceAll("<[^>]*>", "").trim();
                        content = content.replace("&nbsp;", " ").replace("&amp;", "&");

                        if (!date.isEmpty() && !content.isEmpty()) {
                            manager.addSchedule(new AcademicSchedule(date, content, "학부"));
                        }
                    }
                    idx = end;
                }
            }
        } catch (Exception e) {}
    }

    private static String parse(String src, String start, String end) {
        int s = src.indexOf(start);
        if (s == -1) return "";
        s += start.length();
        int e = src.indexOf(end, s);
        if (e == -1) return "";
        return src.substring(s, e).trim();
    }
}