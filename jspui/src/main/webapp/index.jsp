<%@ page language="java" import="java.util.*" pageEncoding="utf-8"%>
<%@ page import="java.time.LocalDateTime" %>
<%@ page import="java.time.temporal.ChronoUnit" %>
<%@ page import="java.time.format.DateTimeFormatter" %>
<%@ page import="java.util.stream.Collectors" %>

<!DOCTYPE html>
<html lang="zh-cn">
<head>
  <meta http-equiv="Content-Type" content="text/html; charset=utf-8"/>
  <meta name="viewport" content="width=device-width, initial-scale=1">
  <title>Deadline Reminder</title>
  <link rel="stylesheet" type="text/css" href="assets/normalize.css">
  <link rel="stylesheet" type="text/css" href="assets/style.css">
  <link rel="stylesheet" type="text/css" href="assets/number-pb.css">
  <style>
  body {
  	font-family: Verdana,Arial,Helvetica,sans-serif;
  }
  .top-banner a {
  	color: #FF534E;
  }
  </style>
  <script src="http://cdn.static.runoob.com/libs/jquery/1.10.2/jquery.min.js"></script>
  <script src="assets/jquery.min.js"></script>
  <script src="assets/jquery.velocity.min.js"></script>
  <script src="assets/number-pb.js"></script>
</head>
<script>
$(document).ready(function(){
	String.format = function() {
	  var s = arguments[0];
	  for (var i = 0; i < arguments.length - 1; i++) {       
	    var reg = new RegExp("\\{" + i + "\\}", "gm");             
	    s = s.replace(reg, arguments[i + 1]);
	  }
	  return s;
	}
	$("#button1").click(function(){
		$.ajax({ 
            type:"POST", 
            url:"/restconf/operations/tasklist:task-gene", 
            processData:false,
            contentType:"application/yang.data+json",              
            data:String.format("{\"input\":{\"name\":\"{0}\",\"deadline\":\"{1}\"}}", 
            		$("#name").val(), $("#deadline").val().replace('T',' ')),
            success:function(data){ 
            	console.log("data= ",data);
            } 
        }); 
    });
	$("#button2").click(function(){
		$.get("/restconf/operational/tasklist:task-registry/",function(data,status){
			if (data!=null){
			        var buffer = "";
			        for (var i = 0; i < data["task-registry"]["task-registry-entry"].length; i++){
			        	buffer = buffer + data["task-registry"]["task-registry-entry"][i]["name"]+';';
			        	buffer = buffer + data["task-registry"]["task-registry-entry"][i]["startpoint"]+';';
			        	buffer = buffer + data["task-registry"]["task-registry-entry"][i]["deadline"]+';';
			        }
			    	console.log(buffer);
			    	document.getElementById('passString').value = buffer;
			    	var formObj = document.getElementById('passForm');
			        formObj.submit();
			}
		}); 
    }); 
});
</script>

<body>
<%! Map<String, LocalDateTime> mapStartpoint = new HashMap<String, LocalDateTime>(); %>
<%! Map<String, LocalDateTime> mapDeadline = new HashMap<String, LocalDateTime>(); %>
<%! Map<String, String> mapString = new HashMap<String, String>(); %>

<%! LocalDateTime now = LocalDateTime.now(); %>
<%
    request.setCharacterEncoding("utf-8");
    String passString = request.getParameter("passString"); 
    
    if(passString!=null && passString.length() > 3){
        String[] tasks = passString.split(";");

    	for (int i = 0; i < tasks.length - 2; i += 3){
    		LocalDateTime startpoint = LocalDateTime.parse(tasks[i+1].replace(' ','T'));
    		LocalDateTime deadline = LocalDateTime.parse(tasks[i+2].replace(' ','T'));
    		mapStartpoint.put(tasks[i],startpoint);
    		mapDeadline.put(tasks[i],deadline);
    		mapString.put(tasks[i],tasks[i+1]);
        }
    }
%> 
<%!
String getTimeLeft(LocalDateTime deadtime, LocalDateTime now) {
	if (now.compareTo(deadtime) >= 0) {
		return "Time Over";
	}
	else {
		long diffYear = now.until(deadtime, ChronoUnit.YEARS);
		long diffMonth = now.until(deadtime, ChronoUnit.MONTHS);
		long diffDay = now.until(deadtime, ChronoUnit.DAYS);
		long diffHour = now.until(deadtime, ChronoUnit.HOURS);
		long diffMinute = now.until(deadtime, ChronoUnit.MINUTES);
		
		if (diffYear > 0) {return String.format("%s Years %s Months Left", diffYear,diffMonth-10*diffYear);}
		else if (diffMonth > 0) {return String.format("%s Months %s Days Left", diffMonth, now.plusMonths(diffMonth).until(deadtime, ChronoUnit.DAYS));}
		else if (diffDay > 0) {return String.format("%s Days %s Hours Left", diffDay, diffHour-24*diffDay);}
		else if (diffHour > 0) {return String.format("%s Hours %s Minutes Left",diffHour, diffMinute-60*diffHour);}
		else {return String.format("%s Minutes Left", diffMinute);}
	}
}
int getPercentage(LocalDateTime starttime, LocalDateTime deadtime, LocalDateTime now) {
	if (now.compareTo(deadtime) >= 0) {
		return 100;
	}
	else {
		long minutesLeft = now.until(deadtime, ChronoUnit.MINUTES);
		long minutesAll = starttime.until(deadtime, ChronoUnit.MINUTES);
		int res = (new Double(Math.floor((1-(minutesLeft/(double)minutesAll))*100))).intValue();
		return res;
	}
}
%>
  
<div class="container">
  <h1 style="margin-top:100px">Deadline Reminder V2.0.0</h1>
  <h3>Current time: <%= new java.util.Date() %></h3>
  <section>
    <form  method="post" action="index.jsp" id ="passForm"> 
      <input id=passString type = 'hidden' name="passString"> 
    </form>  
    <section>
    	<form class="form"> 
    		<p class="name"> 
	    		<label for="name">Task Name</label> 
        		<input type="text" id="name" /> 
    		</p> 
       		<p class="email"> 
	    		<label for="email">&nbsp;&nbsp;&nbsp;&nbsp;Deadline</label>
        		<input type="datetime-local" id="deadline" style="width:200px"/>  
    		</p> 
       		<p class="submit"> 
        		<input type="submit" id=button1 value="Submit" /> 
        		<input type="submit" id=button2 value="Show" /> 
    		</p> 
    	</form>
    </section>
  </section>
<% 
    int i = 0;
	
    if(!mapStartpoint.isEmpty()){
    	/*
    	mapString = mapString  
                .entrySet()  
                .stream()  
                .sorted(Map.Entry.<String, String> comparingByValue()  
                .reversed())  
                .collect(Collectors.toMap(c -> c.getKey(), c -> c.getValue()));  
		*/
    	for (String key : mapStartpoint.keySet()) {
        	i++;
        	DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        	LocalDateTime deadtime = mapDeadline.get(key);
        	LocalDateTime starttime = mapStartpoint.get(key);
        	LocalDateTime now = LocalDateTime.now();
        	String timeLeft = getTimeLeft(deadtime, now);
        	int percentage = getPercentage(starttime,deadtime,now);
            String title = String.format("[Deadline: %s, <font color=\"red\">%s</font>]",deadtime.format(formatter),timeLeft);
            out.println(String.format("<section id=\"task-%s\">",i));
            out.println("<article>");
            out.println(String.format("<h4 class=\"title\">%s %s</h4>",key,title));
            out.println("<div class=\"number-pb\">");
            out.println("<div class=\"number-pb-shown\"></div>");
            out.println("<div class=\"number-pb-num\">0</div>");
            out.println("</div>");
            out.println("</article>");
            out.println("</section>");
            
            out.println("<script>");
            out.println(String.format("var controlBar = $('#task-%s .number-pb').NumberProgressBar({curent:0})",i));
            if (percentage > 80){
            	out.println("controlBar.find('.number-pb-shown').toggleClass(\"dream\");");
            }
            else if((percentage > 50)){
            	out.println("controlBar.find('.number-pb-shown').toggleClass(\"sun\");");
            }
            out.println(String.format("controlBar.reach(%s);",percentage));
            out.println("</script>");
        }
    }
%>
  <p><a href="https://github.com/WangYuchenSJTU">Yuchen Wang</a></p>
  <div class="footer-banner" style="width:728px; margin:60px auto"></div>
</div>
</body>
</html>
