package mafia;

import java.io.FileInputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Properties;

public abstract class Application {

   public static String ip;
   public static int port;
   public static String file = "applicationcontext.txt";
   
   //Application initialization
   public void init() {
	   
      Properties setting = new Properties();
      
      try {
         setting.load(new FileInputStream(file));
         ip = setting.getProperty("IP");
         port = Integer.parseInt(setting.getProperty("PORT"));
         
         System.out.println(" IP address : " + ip );
         System.out.println("PORT number : " + port);
         
         System.out.println(Application.getTime());
         
      } catch(IOException e) {
         System.out.println("Cannot find \"applicationcontext.ini)\"");
         System.out.println("End progrem");
         System.exit(0);
      }
   }
   
   //For overriding
   public abstract void start();
   
   //Return the current time in String
   public static String getTime() {
      SimpleDateFormat timeFormat = new SimpleDateFormat("[hh:mm:ss]");
      return timeFormat.format(new Date());
   }
}