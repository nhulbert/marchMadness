package myapp;

import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.*;

public class WinCache {
    private boolean ready = false;
    private boolean isBuilding = false;
    private Map<Integer, Map<Integer, int[]>> winMap = null;
    private Map<Integer, String> codeSchoolMap = null;
    private String schoolListHTML = "";

    private class SchoolCode implements Comparable{
        String school;
        int code;

        public SchoolCode(String school, int code){
            this.school = school;
            this.code = code;
        }

        @Override
        public int compareTo(Object o) {
            SchoolCode oth = (SchoolCode)o;

            return school.compareTo(oth.school);
        }
    }

    private int dateComparator(int[] l1, int[] l2){
        if (l1.length != 3 || l2.length != 3){
            return 0;
        }

        int ind = 2;

        int res = 0;
        for (int i=0; i<3; i++) {
            res = Integer.compare(l1[(ind+i)%3], l2[(ind+i)%3]);
            if (res != 0) return res;
        }

        return 0;
    }

  public boolean retrieveDataToMemory(){
    isBuilding = true;
    Map<Integer, Map<Integer, int[]>> winMapTemp = new HashMap<>();
    Map<Integer, String> codeSchoolMapTemp = new HashMap<>();

    int gameRecsParsed = 0;
    Connection con = Jsoup.connect("http://web1.ncaa.org/stats/StatsSrv/careersearch");
      Document doc = null;
      try {
          doc = con.get();
      } catch (IOException e) {
          isBuilding = false;
          return false;
      }

      Elements schools = doc.getElementsByTag("select").get(1).getElementsByTag("option");
    List<Integer> years = new ArrayList<>();
    years.add(2018);

    Iterator<Element> schoolsIt = schools.iterator();
    if (schoolsIt.hasNext()) schoolsIt.next(); // iterate past "all schools" option
    List<SchoolCode> scList = new ArrayList<>();
    while (schoolsIt.hasNext()){
      Element school  = schoolsIt.next();
      String schoolName = school.text();
      int orgId = Integer.parseInt(school.attr("value"));
      codeSchoolMapTemp.put(orgId, schoolName);
      scList.add(new SchoolCode(schoolName, orgId));
      for (Integer year : years){
        con = Jsoup.connect("http://web1.ncaa.org/stats/exec/records");
          try {
              doc = con.data("doWhat", "display").
                      data("coachId", "0").
                      data("orgId", Integer.toString(orgId)).
                      data("academicYear", Integer.toString(year)).
                      data("sportCode", "MBB").
                      data("rptWeek", "0").post();
          } catch (IOException e) {
              isBuilding = false;
              return false;
          }
          Elements games = doc.getElementsByTag("tbody").get(3).select("tr.text");
        for (Element game : games){
          Integer oppId = null;
          try {
            oppId = Integer.parseInt(game.child(0).child(0).attr("href").split("\\(|\\)")[1]);
          }
          catch (IndexOutOfBoundsException e){
          }

          if (oppId != null) {
            String date = game.child(1).text();
            int[] dateInt = null;
            try{
              String[] dateSplit = date.split("/");
              if (dateSplit.length != 3){
                throw new NumberFormatException();
              }

              int[] di = new int[3];
              for (int i=0; i<3; i++){
                di[i] = Integer.parseInt(dateSplit[i]);
              }
              dateInt = di;
            }
            catch(NumberFormatException e){
              // TODO: possibly tally unusable dates to print;
            }
            if (dateInt != null) {
              Integer score = Integer.parseInt(game.child(2).text());
              Integer oppScore = Integer.parseInt(game.child(3).text());

              int outcome = Integer.compare(score, oppScore);
              if (outcome != 0){
                int winner, loser;
                if (outcome > 0){
                  winner = orgId;
                  loser = oppId;
                }
                else{
                  winner = oppId;
                  loser = orgId;
                }
                Map<Integer, int[]> res = winMapTemp.get(winner);
                if (res == null) {
                  res = new HashMap<>();
                  res.put(loser, dateInt);
                  winMapTemp.put(winner, res);
                }
                else {
                  int[] resDate = res.get(loser);
                  if (resDate == null || dateComparator(dateInt, resDate) > 0){
                    res.put(loser, dateInt);
                  }
                }
              }
              gameRecsParsed++;
            }
          }
        }
        System.out.println("retrieved data for "+schoolName+" in "+Integer.toString(year)+", "+Integer.toString(gameRecsParsed)+" games parsed");//, unique games: "+Integer.toString(gameMap.size())+", difference: "+Integer.toString(gameRecsParsed-gameMap.size()));
      }
    }
    Collections.sort(scList);
    StringBuilder sb = new StringBuilder();
    for (SchoolCode sc : scList) {
      sb.append("<option value=\"");
      sb.append(sc.code);
      sb.append("\">");
      sb.append(sc.school);
      sb.append("</option>\n");
    }
    schoolListHTML = sb.toString();
    System.out.println("Game Records Parsed: "+Integer.toString(gameRecsParsed));

    winMap = Collections.unmodifiableMap(winMapTemp);
    codeSchoolMap = Collections.unmodifiableMap(codeSchoolMapTemp);
    isBuilding = false;
    ready = true;
    return true;
  }

    public boolean isReady() {
        return ready;
    }

    public boolean isBuilding() {
        return isBuilding;
    }

    public Map<Integer, Map<Integer, int[]>> getWinMap() {
        return winMap;
    }

    public Map<Integer, String> getCodeSchoolMap() {
        return codeSchoolMap;
    }

    public String getSchoolListHTML() {
        return schoolListHTML;
    }
}
