package myapp;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.*;

public class MainServlet extends HttpServlet{
    static WinCache winCache = new WinCache();

    public void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws IOException, ServletException {
        if (!req.getRequestURI().equals("/")) {
            resp.sendError(HttpServletResponse.SC_NOT_FOUND);
            return;
        }
        if (winCache.isReady() || (!winCache.isBuilding() && winCache.retrieveDataToMemory())) {
            Integer winner = null, loser = null;
            String winnerStr = req.getParameter("winning");
            String loserStr = req.getParameter("losing");
            try {
                if (winnerStr != null && loserStr != null) {
                    winner = Integer.parseInt(winnerStr);
                    loser = Integer.parseInt(loserStr);
                }
                String winPath = "";
                if (winner != null && loser != null && !winner.equals(loser)) {
                    winPath = findPath(winner, loser);
                    req.setAttribute("winnerSel", winner);
                    req.setAttribute("loserSel", loser);
                }

                req.setAttribute("winPath", winPath);
                req.setAttribute("schoolsHTML", winCache.getSchoolListHTML());
            } catch (NumberFormatException e) {
            }
        }

        req.getRequestDispatcher("/winChain.jsp").forward(req, resp);
    }

    private class SchoolDate{
        int school;
        int[] date;

        public SchoolDate(int school, int[] date){
            this.school = school;
            this.date = date;
        }
    }

    private String findPath(int fromCode, int toCode){
        Map<Integer, SchoolDate> schoolParents = new HashMap<>();
        Set<Integer> visited = new HashSet<>();

        Queue<Integer> queue = new LinkedList<>();
        queue.offer(fromCode);
        visited.add(fromCode);
        schoolParents.put(fromCode, new SchoolDate(-1, null));

        while (!queue.isEmpty()){
            Integer next = queue.poll();
            if (next.equals(toCode)) {
                return retracePath(next, schoolParents);
            }
            Map<Integer, int[]> res = winCache.getWinMap().get(next);
            if (res != null) for (Map.Entry<Integer, int[]> entry : res.entrySet()) {
                Integer toAdd = entry.getKey();
                if (!visited.contains(toAdd)) {
                    schoolParents.put(toAdd, new SchoolDate(next, entry.getValue()));
                    queue.offer(toAdd);
                    visited.add(toAdd);
                }
            }
        }

        return "Could not find a winning path between the schools";
    }

    private String retracePath(Integer last, Map<Integer, SchoolDate> schoolParents){
        Stack<SchoolDate> stack = new Stack<>();
        SchoolDate next = schoolParents.get(last);
        while (next.school != -1){
            stack.push(next);
            next = schoolParents.get(next.school);
        }

        StringBuilder out = new StringBuilder();
        while (!stack.empty()) {
            SchoolDate sd = stack.pop();
            out.append(winCache.getCodeSchoolMap().get(sd.school)+ " on " + sd.date[0]+"/"+sd.date[1]+"/"+sd.date[2] + " beat <br />");
        }
        out.append(winCache.getCodeSchoolMap().get(last));

        return out.toString();
    }
}
