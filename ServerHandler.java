package mafia;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;

import javax.swing.JOptionPane;

public class ServerHandler implements Runnable {

   // Room user number
   static final int NUM = 5;

   //Use for end game
   //0 : end, 1: start
   public int gameSet = 0;
   
   private Socket S;   //socket that connected client
   private String clientAd;   //client address
   private String id;   //client id
   
   //have client id as a key
   static HashMap<String, PrintWriter> sendMap = new HashMap<>();   
   static HashMap<String, String> userMap = new HashMap<String, String>();
   
   //save socket
   public ServerHandler(Socket socket) {
      S = socket;
   }

   @Override
   public void run() {
      
      try {
         PrintWriter pw = new PrintWriter(new OutputStreamWriter(S.getOutputStream()));
         BufferedReader br = new BufferedReader(new InputStreamReader(S.getInputStream()));

         //save client access information
         id = br.readLine();
         sendMap.put(id, pw);

         
         if (sendMap.size() < 5) {
            //wait for other users
            broadCast(Application.getTime() + "[" + id + "] 님이 들어 오셨습니다.");
            System.out.println(Application.getTime() + clientAd + " has connected");
            System.out.println(Application.getTime() + "joined user: " + sendMap.size() + "/" + NUM + "명");
            broadCast("현재 인원 : " + sendMap.size() + "명");

            //chatting function
            String line = null;
            while ((line = br.readLine()) != null) {
               String msg = "[" + id + "]" + " " + line;
               ServerHandler.broadCast(msg);
            }
         }

         // Room full
         broadCast(Application.getTime() + "[" + id + "] 님이 들어 오셨습니다.");
         System.out.println(Application.getTime() + "joined user: " + sendMap.size() + "/" + NUM + "명");
         broadCast("현재 인원 : " + sendMap.size() + "명");

         try {
            Thread.sleep(3 * 1000);
         } catch (InterruptedException e) {
         }
         ;

         broadCast(" ");
         broadCast("--------------Game Start--------------");
         broadCast(" ");
         gameSet = 1;   //set game start
         
         
         
         //make the roles and keywords list
         List<String> roleList = Arrays.asList("마피아", "모범수", "모범수", "모범수", "사회자");   //roles
         List<String> keyword = Arrays.asList("사자", "개", "기린", "호랑이", "거북이", "코알라", "고양이", "토끼", "나무늘보", "사막여우"   //keywords
               , "곰", "원숭이", "고릴라", "양", "늑대", "고라니", "독수리", "비둘기", "참새", "판다");
         List<String> userList = new ArrayList<>();   //users

         Set<Map.Entry<String, PrintWriter>> set = sendMap.entrySet();
         Iterator<Map.Entry<String, PrintWriter>> it = set.iterator();   

         while (it.hasNext()) {
            Map.Entry<String, PrintWriter> entry = it.next();
            String id = entry.getKey();
            userList.add(id);   //make user list
         }

         broadCast("역할 리스트 : " + roleList);
         broadCast("참여자 리스트 : " + userList);
         broadCast("");

         for (int i = 0; i < userList.size(); i++) {
            userMap.put("" + i, userList.get(i));
         }
         
         
         int randNum[] = new int[4];
         int num = 0;
         while (num != 4) {
            randNum[num] = (int) (Math.random() * 4);   //get four random number
            
            for (int i = 0; i < num; i++) {
               if (randNum[i] == randNum[num]) {   //remove duplicate
                  num--;
                  break;
               }
            }
            num++;
         }

         
         //Restore users list in random order
         List<String> newUserList = new ArrayList<>();
         for (int i = 0; i < 4; i++) {
            newUserList.add(userList.get(randNum[i]));
         }
         newUserList.add(userList.get(4));   //host is last user

         
         //Choose keyword randomly
         int randomT = (int) (Math.random() * 20);
         String kw = keyword.get(randomT);
         
         
         //Tell the user the roles and keywords
         for (int i = 0; i < 4; i++) 
         {
            inform("/to " + newUserList.get(i) + " " + roleList.get(i));
            if(i != 0) {
               informKey("/to " + newUserList.get(i) + " " + kw);   //notice keyword except mafia
            }
         }
         inform("/to " + newUserList.get(4) + " " + roleList.get(4));   //host
         informKey("/to " + newUserList.get(4) + " " + kw);
         informP("/to " + newUserList.get(4) + " " + " " +"[역할 소개] 투표가 끝나면 참가자들의 채팅을 보고 (/vote id) 명령어를 이용해 결과를 반영해주시길 바랍니다");
         broadCast("");
         
         
   //-----------------------Job assignment completed--------------------------------

         // game
         
         String line = null;
         String dead = null;
         int deadNum=0;   //for check if there is a dead user
         int round = 0;
         int end = 0;
         
         int[] count = new int[4];   //save poll
         
         //save user id in player[]
         String[] player = new String[5];
         int size = newUserList.size();
         player = newUserList.toArray(new String[size]);
         
         broadCast(" "+ "====주제는 동물입니다. 감옥 안에 숨어든 마피아를 찾아내 제거하십시오====");
         
         while (gameSet != 0) {
            broadCast("");
            //broadCast("(for check) Game start - round " + round + ", size " + size);
            
            //start timer(conversation 60sec, vote 30sec)
            timerStart();
            
            /*
             * Vote user to kill
             * end : for check if the vote is end / size : the number of player
             * round : increase when user die(use for check alive users)
             */ 
            
            while (end < size - round - 1) {   //wait until all alive users vote

               //read users chatting
               while ((line = br.readLine()) != null) 
               {
                  //voting function (/vote id)
                  if (line.indexOf("/vote") != -1) {

                     String msg = line.substring(6) + " 님이 투표되었습니다.";
                     ServerHandler.broadCast(msg);

                     //update poll
                     if (line.substring(6).equals(player[0])) {
                        count[0] += 1;
                     }
                     else if (line.substring(6).equals(player[1])) {
                        count[1] += 1;
                     } 
                     else if (line.substring(6).equals(player[2])) {
                        count[2] += 1;
                     } 
                     else if (line.substring(6).equals(player[3])) {
                        count[3] += 1;
                     }
                     end++; //for check vote
                  }
                  //normal message
                  else {
                     String msg = "[" + id + "]" + " " + line;
                     ServerHandler.broadCast(msg);
                  }
                  
                  //all players voted
                  if(end == size-round-1) {
                     break;
                  }
               }
            }
            broadCast("");
            broadCast("[ 모든 참가자들의 투표가 완료되었습니다 ]");
            

            broadCast("");
            broadCast("====밤이 되었습니다====");
            

            try {
               Thread.sleep(2 * 1000);
            } catch (InterruptedException e) {
            }
            
            int result = voteResult(count);
            
            if(result < 0) {   //if there is same poll
               broadCast("[ 오늘 밤에는 아무도 죽지 않았습니다 ]");
            }
            else {
               dead = player[result];   //add dead user's id in dead
               deadNum++;
               
               broadCast("[ " + player[result] + " 님이 죽었습니다 ]");
               round++;
            }
            

            try {
               Thread.sleep(2 * 1000);
            } catch (InterruptedException e) {
            }
            
            
            broadCast("");
            broadCast("====아침이 되었습니다====");
            broadCast("");
            
            
            if (deadNum != 0) {
               
               //if the mafia dead
               if (dead.equals(newUserList.get(0))) {
                  broadCast("--------------Game End [모범수 승리] --------------");
                  broadCast("[ 제시어는 " + kw + "입니다 ]");
                  broadCast("[ 마피아는 " + newUserList.get(0) + " 님 이었습니다 ]");
                  gameSet = 0;   //end game
                  break;
               }
               //if the prisoner dead
               else {
                  broadCast("--------------Game End [마피아 승리] --------------");
                  broadCast("[ 제시어는 " + kw + "입니다 ]");
                  broadCast("[ 마피아는 " + newUserList.get(0) + " 님 이었습니다 ]");
                  gameSet = 0;   //end game
                  break;
               }
            }
            
            //count & end initialization
            initCount(count);
            end = 0;
            
            try {
               Thread.sleep(3 * 1000);
            } catch (InterruptedException e) {
            }
         }
         broadCast("Game Over");
         System.out.println(Application.getTime() + "게임이 종료되었습니다");
         System.out.println(Application.getTime() + "서버를 종료합니다");
         System.exit(0);
         br.reset();
         pw.close();
         br.close();
         
         
      } catch (IOException e) {
         e.printStackTrace();
      }
   }
   
   //get the result of vote
   private int voteResult(int[] count) {
      int maxIndex = 0, max = 0;
      
      //find max poll
      for(int i=0; i<4; i++) {
         if(count[i] > max) {
            max = count[i];
            maxIndex = i;
         }
      }
      
      //check if there is same poll
      for(int i=0; i<4; i++) {
         if((i != maxIndex) && (count[i] == max)) {
            return -1;   //nobody will be die
         }
      }
      
      return maxIndex;   //index of user will be die
   }
   
   //reset count
   private void initCount(int[] count){
      for(int i=0; i<4; i++) {
         count[i] = 0;
      }
   }
   
   //timer
   private void timerStart() {
      Timer time = new Timer();

      time.scheduleAtFixedRate(new TimerTask() {
         public int sec = 60;
         public int vote = 0;

         public void run() {

            if(vote == 0) {
               if(sec == 60) {
                  broadCast("[ 지금부터 1분간 토론을 시작합니다 ]");
               }
               if(sec == 10) {
                  broadCast("[ 토론이 10초 남았습니다 ]");
               }
               
               if (sec < 0) {
                  broadCast("[ 투표 시간이 되었습니다 ]");
                  broadCast("[ 지금부터 30초간 투표를 시작합니다 ]");
                  broadCast("[ 투표할 사람의 이름을 입력하십시오 ]");
                  
                  vote = 1;  //change "vote" to 1 -> for voting
                  sec = 30;
               }
            }
            else if(vote == 1) {
               if(sec == 10) {
                  broadCast("[ 투표가 10초 남았습니다 ]");
               }
               
               if (sec < 0) {
                  broadCast("[ 투표가 종료되었습니다 ]");
                  broadCast("[ 사회자는 투표 결과를 반영해 주십시오 ]");
                  time.cancel();
               }
            }
            sec--;
         }
      }, 30, 1000);

   }
   
   //Return the current time in String
   public static String getTime() {
      SimpleDateFormat timeFormat = new SimpleDateFormat("[hh:mm:ss]");
      return timeFormat.format(new Date());
   }

   //Send message to all users
   public static void broadCast(String message) {
      synchronized (sendMap) {

         for (PrintWriter clientpw : ServerHandler.sendMap.values()) {
            clientpw.println(message);
            clientpw.flush();
         }
      }
   }

   //informs all users of there roles
   public void inform(String text) {
      int start = text.indexOf(" ") + 1;
      int end = text.indexOf(" ", start);

      if (end != -1) {
         String id = text.substring(start, end);
         String role = text.substring(end + 1);

         PrintWriter pw = ServerHandler.sendMap.get(id);

         if (pw != null) {
            pw.println("당신의 역할은 " + role + "입니다");
            pw.flush();
         }

      }
   }
   
   //informs the keyword to all users except mafia
   public void informKey(String text) {
      int start = text.indexOf(" ") + 1;
      int end = text.indexOf(" ", start);

      if (end != -1) {
         String id = text.substring(start, end);
         String keyword = text.substring(end + 1);

         PrintWriter pw = ServerHandler.sendMap.get(id);

         if (pw != null) {
            pw.println("제시어 : " + keyword);
            pw.flush();
         }
      }
   }
   
   //delivery message to a specific user
   public void informP(String text) {
      int start = text.indexOf(" ") + 1;
      int end = text.indexOf(" ", start);

      if (end != -1) {
         String id = text.substring(start, end);
         String msg =text.substring(end + 1);

         PrintWriter pw = ServerHandler.sendMap.get(id);

         if (pw != null) {
            pw.println(msg);
            pw.flush();
         }
      }
   }

}