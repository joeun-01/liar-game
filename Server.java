package mafia;

import java.util.Scanner;

public class Server {
	public static void main(String[] args) {

		Scanner input = new Scanner(System.in);
		showMenu();

		System.out.printf("          >");
		int choice = input.nextInt();	//1 : game start, 0 : end program
		
		switch (choice) {
		case 1:
			System.out.printf("\n	[ Start Game ]\n\n");
			break;

		case 0:
			System.out.println("\n	[ End progrem ]");
			System.exit(0);
		}
		
		//Create TCP/IP application
		Application app = new AppServer();

		app.init();		// App initialization
		app.start();	// App execution
		input.close();
	}

	public static void showMenu() {
		System.out.printf("          ______________________________\n");
		System.out.printf("          |                            |\n");
		System.out.printf("          |        [Mafia game]        |\n");
		System.out.printf("          |  Find the mafia in prison  |\n");
		System.out.printf("          |                            |\n");
		System.out.printf("          |____________________________|\n");
		System.out.printf("\n");
		
		System.out.printf("          1 ¡æ Start the game          \n");
		System.out.printf("          0 ¡æ End the game           \n");
	}
}