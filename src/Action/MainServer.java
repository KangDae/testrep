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
	private ServerSocket ss; // ���� ����
	private ArrayList<MainHandler> allUserList; // ��ü �����
	private ArrayList<MainHandler> WaitUserList; // ���� �����
	private ArrayList<Room> roomtotalList;// ��ü �渮��Ʈ
	
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
			conn = DriverManager.getConnection(url, user, password); // DB ����

			ss = new ServerSocket(9500);
			System.out.println("�����غ�Ϸ�");

			allUserList = new ArrayList<MainHandler>(); // ��ü �����
			WaitUserList = new ArrayList<MainHandler>(); // ���� �����
			roomtotalList = new ArrayList<Room>(); // ��ü �渮��Ʈ
			while (true) {
				Socket socket = ss.accept();
				MainHandler handler = new MainHandler(socket, allUserList, WaitUserList, roomtotalList, conn);// ������ ����
				handler.start();// ������ ����
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
