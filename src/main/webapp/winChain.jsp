<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="java.util.Map" %>

<!doctype html>
<html lang="en">
  <head>
    <title>App Engine Demo</title>
    <script src="//ajax.googleapis.com/ajax/libs/jquery/1.11.0/jquery.min.js"></script>
    <meta charset="utf-8">
    <link rel="stylesheet" href="./css/style.css">
  </head>

  <body>
  <section class="flex-sect">
    <div class="c6021">NCAA Basketball 2017-18 Season Win-Chain Search
    </div>
    <div class="c6621">From a list of all schools in any division of the NCAA, select a winning and a losing team. Then click 'Submit' to find the shortest chain of wins from the winning team to the losing team, thereby definitively proving the superiority of the winning team.
    </div>
    <form method="get" action="" class="form">
        <select id="winner" class="select" name=winning>
            <option value="">Select winning team</option>
            ${schoolsHTML}
        </select>
        <select id="loser" class="select" name=losing>
            <option value="">Select losing team</option>
            ${schoolsHTML}
        </select>
        <button class="button">Submit</button>
    </form>
    <div class="c4682">
        ${winPath}
    </div>
    <div class="c7313">by Neil Hulbert</div>
  </section>
  </body>

  <script>
    var temp = ${winnerSel};
    if (temp){
        var mySelect = document.getElementById('winner');

        for(var i, j = 0; i = mySelect.options[j]; j++) {
          if(i.value == temp) {
            mySelect.selectedIndex = j;
            break;
          }
        }
    }

    temp = ${loserSel};
    if (temp){
        mySelect = document.getElementById('loser');
        for(var i, j = 0; i = mySelect.options[j]; j++) {
          if(i.value == temp) {
            mySelect.selectedIndex = j;
            break;
          }
        }
    }
  </script>
</html>
