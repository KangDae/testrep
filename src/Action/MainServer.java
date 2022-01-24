package Action;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.ArrayList;

import DO.Room;

public class MainServer {
	private ServerSocket ss; 
	private ArrayList<MainHandler> allUserList; //전체 사용자
	private ArrayList<MainHandler> WaitUserList;//대기실 사용자
	private ArrayList<Room> roomtotalList;//전체방리스트
	
	String protocol =  "jdbc:mariadb://";
	String ip = "127.0.0.1";
	String port = "3306";
	String db ="userinfo";
	
	

	private Connection conn;
	private String driver = "org.mariadb.jdbc.Driver";
	private String url = String.format("%s%s:%s/%s", protocol,ip,port,db);
	private String user = "root";
	private String password = "7984";

	public MainServer() {

		try {
			Class.forName(driver);
			conn = DriverManager.getConnection(url, user, password);

			ss = new ServerSocket(9500);
			System.out.println("�����غ�Ϸ�");

			allUserList = new ArrayList<MainHandler>();  //전체 사용자
			WaitUserList = new ArrayList<MainHandler>(); //대기실 사용자
			roomtotalList = new ArrayList<Room>(); //전체 방 리스트
			while (true) {
				Socket socket = ss.accept();
				MainHandler handler = new MainHandler(socket, allUserList, WaitUserList, roomtotalList, conn);// 스레드 생성
				handler.start();// 스레드 시작
				allUserList.add(handler);
			} // while
		} catch (IOException io) {
			io.printStackTrace();
		} catch (SQLException e) {
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
	}

	public static void main(String[] args) {
		new MainServer();
	}
}
