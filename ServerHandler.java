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
            broadCast(Application.getTime() + "[" + id + "] ���� ��� ���̽��ϴ�.");
            System.out.println(Application.getTime() + clientAd + " has connected");
            System.out.println(Application.getTime() + "joined user: " + sendMap.size() + "/" + NUM + "��");
            broadCast("���� �ο� : " + sendMap.size() + "��");

            //chatting function
            String line = null;
            while ((line = br.readLine()) != null) {
               String msg = "[" + id + "]" + " " + line;
               ServerHandler.broadCast(msg);
            }
         }

         // Room full
         broadCast(Application.getTime() + "[" + id + "] ���� ��� ���̽��ϴ�.");
         System.out.println(Application.getTime() + "joined user: " + sendMap.size() + "/" + NUM + "��");
         broadCast("���� �ο� : " + sendMap.size() + "��");

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
         List<String> roleList = Arrays.asList("���Ǿ�", "�����", "�����", "�����", "��ȸ��");   //roles
         List<String> keyword = Arrays.asList("����", "��", "�⸰", "ȣ����", "�ź���", "�ھ˶�", "�����", "�䳢", "�����ú�", "�縷����"   //keywords
               , "��", "������", "����", "��", "����", "����", "������", "��ѱ�", "����", "�Ǵ�");
         List<String> userList = new ArrayList<>();   //users

         Set<Map.Entry<String, PrintWriter>> set = sendMap.entrySet();
         Iterator<Map.Entry<String, PrintWriter>> it = set.iterator();   

         while (it.hasNext()) {
            Map.Entry<String, PrintWriter> entry = it.next();
            String id = entry.getKey();
            userList.add(id);   //make user list
         }

         broadCast("���� ����Ʈ : " + roleList);
         broadCast("������ ����Ʈ : " + userList);
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
         informP("/to " + newUserList.get(4) + " " + " " +"[���� �Ұ�] ��ǥ�� ������ �����ڵ��� ä���� ���� (/vote id) ��ɾ �̿��� ����� �ݿ����ֽñ� �ٶ��ϴ�");
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
         
         broadCast(" "+ "====������ �����Դϴ�. ���� �ȿ� ����� ���ǾƸ� ã�Ƴ� �����Ͻʽÿ�====");
         
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

                     String msg = line.substring(6) + " ���� ��ǥ�Ǿ����ϴ�.";
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
            broadCast("[ ��� �����ڵ��� ��ǥ�� �Ϸ�Ǿ����ϴ� ]");
            

            broadCast("");
            broadCast("====���� �Ǿ����ϴ�====");
            

            try {
               Thread.sleep(2 * 1000);
            } catch (InterruptedException e) {
            }
            
            int result = voteResult(count);
            
            if(result < 0) {   //if there is same poll
               broadCast("[ ���� �㿡�� �ƹ��� ���� �ʾҽ��ϴ� ]");
            }
            else {
               dead = player[result];   //add dead user's id in dead
               deadNum++;
               
               broadCast("[ " + player[result] + " ���� �׾����ϴ� ]");
               round++;
            }
            

            try {
               Thread.sleep(2 * 1000);
            } catch (InterruptedException e) {
            }
            
            
            broadCast("");
            broadCast("====��ħ�� �Ǿ����ϴ�====");
            broadCast("");
            
            
            if (deadNum != 0) {
               
               //if the mafia dead
               if (dead.equals(newUserList.get(0))) {
                  broadCast("--------------Game End [����� �¸�] --------------");
                  broadCast("[ ���þ�� " + kw + "�Դϴ� ]");
                  broadCast("[ ���Ǿƴ� " + newUserList.get(0) + " �� �̾����ϴ� ]");
                  gameSet = 0;   //end game
                  break;
               }
               //if the prisoner dead
               else {
                  broadCast("--------------Game End [���Ǿ� �¸�] --------------");
                  broadCast("[ ���þ�� " + kw + "�Դϴ� ]");
                  broadCast("[ ���Ǿƴ� " + newUserList.get(0) + " �� �̾����ϴ� ]");
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
         System.out.println(Application.getTime() + "������ ����Ǿ����ϴ�");
         System.out.println(Application.getTime() + "������ �����մϴ�");
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
                  broadCast("[ ���ݺ��� 1�а� ����� �����մϴ� ]");
               }
               if(sec == 10) {
                  broadCast("[ ����� 10�� ���ҽ��ϴ� ]");
               }
               
               if (sec < 0) {
                  broadCast("[ ��ǥ �ð��� �Ǿ����ϴ� ]");
                  broadCast("[ ���ݺ��� 30�ʰ� ��ǥ�� �����մϴ� ]");
                  broadCast("[ ��ǥ�� ����� �̸��� �Է��Ͻʽÿ� ]");
                  
                  vote = 1;  //change "vote" to 1 -> for voting
                  sec = 30;
               }
            }
            else if(vote == 1) {
               if(sec == 10) {
                  broadCast("[ ��ǥ�� 10�� ���ҽ��ϴ� ]");
               }
               
               if (sec < 0) {
                  broadCast("[ ��ǥ�� ����Ǿ����ϴ� ]");
                  broadCast("[ ��ȸ�ڴ� ��ǥ ����� �ݿ��� �ֽʽÿ� ]");
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
            pw.println("����� ������ " + role + "�Դϴ�");
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
            pw.println("���þ� : " + keyword);
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