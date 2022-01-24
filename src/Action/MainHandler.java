package Action;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

import DO.Room;
import DO.User;

public class MainHandler extends Thread {
	private BufferedReader br;
	private PrintWriter pw;
	private Socket socket;
	private Connection conn;
	private PreparedStatement pstmt;
	private User user;

	private ArrayList<MainHandler> allUserList; // ��ü�����
	private ArrayList<MainHandler> waitUserList; // ���ǻ����
	private ArrayList<Room> roomtotalList; // ��ü �渮��Ʈ

	private Room priRoom; // ����ڰ� �ִ� ��
	private String fileName;

	// ����, ��ü�����,����,�渮��Ʈ,JDBC
	public MainHandler(Socket socket, ArrayList<MainHandler> allUserList, ArrayList<MainHandler> waitUserList,
			ArrayList<Room> roomtotalList, Connection conn) throws IOException {
		this.user = new User();
		this.priRoom = new Room();
		this.socket = socket;
		this.allUserList = allUserList;
		this.waitUserList = waitUserList;
		this.roomtotalList = roomtotalList;
		this.conn = conn;

		br = new BufferedReader(new InputStreamReader(socket.getInputStream()));
		pw = new PrintWriter(new OutputStreamWriter(socket.getOutputStream()));

		this.fileName = "";
	}

	@Override
	public void run() {
		// ������ �Է¹��� �������Ľ� -> ��� �����������
		try {

			String[] line = null;
			while (true) {
				line = br.readLine().split("\\|");

				if (line == null) {
					break;
				}
				if (line[0].compareTo(Protocol.REGISTER) == 0) // [ȸ������]
				{
					String userContent[] = line[1].split("%");

					String sql = "Insert into usercontent values(NEXTVAL(num),?,?,?,?,?,?)";
					pstmt = conn.prepareStatement(sql);
					pstmt.setString(1, userContent[0]);
					pstmt.setString(2, userContent[1]);
					pstmt.setString(3, userContent[2]);
					pstmt.setString(4, userContent[3]);
					pstmt.setString(5, userContent[4]);
					pstmt.setString(6, userContent[5]);
					int su = pstmt.executeUpdate(); // �׻� ��� ����(CRUD)���� ������ return
					System.out.println(su + "ȸ������[DB]");

				} else if (line[0].compareTo(Protocol.IDSEARCHCHECK) == 0) // ȸ������ ID �ߺ�üũ
				{
					System.out.println(line[0] + "/" + line[1]);
					String sql = "select * from usercontent where idname = '" + line[1] + "'";
					pstmt = conn.prepareStatement(sql);
					ResultSet rs = pstmt.executeQuery(sql);
					String name = null;
					int count = 0;
					while (rs.next()) {
						name = rs.getString("IDNAME");
						if (name.compareTo(line[1]) == 0) {
							count++;
						}
					}
					System.out.println(count);
					if (count == 0) // �ߺ��ȵǼ� ���԰���
					{
						pw.println(Protocol.IDSEARCHCHECK_OK + "|" + "MESSAGE");
						pw.flush();
					} else {
						pw.println(Protocol.IDSEARCHCHECK_NO + "|" + "MESSAGE");
						pw.flush();
					}
				} else if (line[0].compareTo(Protocol.IDSEARCH) == 0) // ID ã��
				{
					System.out.println("IDã��");
					String userContent[] = line[1].split("%");

					System.out.println(userContent[0]);
					System.out.println(userContent[1]);
					System.out.println(userContent[2]);
					System.out.println(userContent[3]);

					String sql = "select * from UserContent where (NAME = '" + userContent[0] + "' and age = '"
							+ userContent[1] + "' and email ='" + userContent[2] + "' and phoneNumber1 = '"
							+ userContent[3] + "')";

					pstmt = conn.prepareStatement(sql);
					ResultSet rs = pstmt.executeQuery(sql);
					String name = null;
					String id = null;
					int count = 0;
					while (rs.next()) {
						name = rs.getString("NAME");
						id = rs.getString("IDNAME");
						if (name.compareTo(userContent[0]) == 0) {
							count++;
						}
					}
					System.out.println(count);

					if (count == 0) // ID�� ����
					{
						pw.println(Protocol.IDSEARCH_NO + "|" + "��ϵ� ���̵� �����ϴ�.");
						pw.flush();
					} else { // ������ID ã��
						StringBuffer stb = new StringBuffer(id);
						stb.replace(stb.length() - 4, stb.length() - 1, "***");
						pw.println(Protocol.IDSEARCH_OK + "|" + "ID : " + stb.toString());
						pw.flush();
					}

				} else if (line[0].compareTo(Protocol.ENTERLOGIN) == 0) // [login]
				{

					boolean con = true; // ������ �α��εǾ��ִ��� �ȵǾ��ִ��� ����
					System.out.println("login");
					String userContent[] = line[1].split("%");

					System.out.println(userContent[0] + "/" + userContent[1]);

					for (int i = 0; i < waitUserList.size(); i++) {
						if ((waitUserList.get(i).user.getIdName()).compareTo(userContent[0]) == 0) {
							con = false;
						}
					}
					if (con) {
//						String sql = "select * from UserContent where (IDNAME = '" + userContent[0] + "' and PASSWORD = '"
//						+ userContent[1] + "')";

						String sql = "select * from UserContent where idname = '" + userContent[0]
								+ "' and password = '" + userContent[1] + "'";

						pstmt = conn.prepareStatement(sql);
						ResultSet rs = pstmt.executeQuery(sql);
						int count = 0;

						while (rs.next()) {
							user.setName(rs.getString("NAME"));
							user.setIdName(rs.getString("IDNAME"));
							user.setAge(rs.getString("AGE"));
							user.setPassword(rs.getString("PASSWORD"));
							user.setPryNumber(rs.getInt("priNumber"));
							user.setPhoneNumber(rs.getString("phoneNumber1"));
							user.setEmail(rs.getString("email"));

							count++;
						}

						System.out.println(count);

						if (count == 0) // ID,PW Ʋ����
						{
							pw.println(Protocol.ENTERLOGIN_NO + "|" + "�α��ο� �����Ͽ����ϴ�");
							pw.flush();

							user.setName("");
							user.setIdName("");
							user.setAge("");
							user.setPassword("");
							user.setPryNumber(0);
							user.setPhoneNumber("");
							user.setEmail("");

						} else { // �α��� �Ǿ�����
							waitUserList.add(this); // ���� �ο��� �߰�
							String userline = "";
							for (int i = 0; i < waitUserList.size(); i++) {
								userline += (waitUserList.get(i).user.getIdName() + ":");
							}

							for (int i = 0; i < waitUserList.size(); i++) {
								waitUserList.get(i).pw.println(
										Protocol.ENTERLOGIN_OK + "|" + user.getIdName() + "|���� �����Ͽ����ϴ�.|" + userline);
								waitUserList.get(i).pw.flush();
							}
							System.out.println("[���� �ο���] :" + waitUserList.size());

							System.out.println("[Room ����]");
							for (Room room : roomtotalList) {
								System.out.println(room.toString() + "����濡 �ο��� : " + room.roomInUserList.size());
							}
							System.out.println("[��ü Room ���� ]" + roomtotalList.size());

							// RoomtotalList ��ü ������ Message�� ���� �����ߵȴ�
							String roomListMessage = "";

							for (int i = 0; i < roomtotalList.size(); i++) {
								roomListMessage += (roomtotalList.get(i).getrID() + "%"
										+ roomtotalList.get(i).getTitle() + "%" + roomtotalList.get(i).getrPassword()
										+ "%" + roomtotalList.get(i).getUserCount() + "%"
										+ roomtotalList.get(i).getMasterName() + "%" + roomtotalList.get(i).getSubject()
										+ "%" + roomtotalList.get(i).getCondtionP() + "%"
										+ roomtotalList.get(i).roomInUserList.size() + "-");
							}

							System.out.println(roomListMessage);

							if (roomListMessage.length() != 0) {
								for (int i = 0; i < waitUserList.size(); i++) {
									waitUserList.get(i).pw.println(Protocol.ROOMMAKE_OK + "|" + roomListMessage);
									waitUserList.get(i).pw.flush();
								}
							}

						}

						System.out.println(user.toString());
					} else {
						pw.println(Protocol.ENTERLOGIN_NO + "|" + "�̹� �α��� ���Դϴ�.");
						pw.flush();
					}

				} else if (line[0].compareTo(Protocol.EXITWAITROOM) == 0) { // ���ǹ濡�� �α���������(logout);

					String thisName = waitUserList.get(waitUserList.indexOf(this)).user.getIdName(); // -���� �ٽ��ϱ�

					waitUserList.remove(this); //
					System.out.println("[���� �ο���] :" + waitUserList.size());

					String userline = "";
					for (int i = 0; i < waitUserList.size(); i++) {
						userline += (waitUserList.get(i).user.getIdName() + ":");
					}

					System.out.println("����� �ο� :" + userline);
					for (int i = 0; i < waitUserList.size(); i++) {
						waitUserList.get(i).pw
								.println(Protocol.EXITWAITROOM + "|" + thisName + "|���� �����Ͽ����ϴ�.|" + userline);// ���濡
						// Message
						// ����;
						waitUserList.get(i).pw.flush();
					}
					user.setName("");
					user.setIdName("");
					user.setAge("");
					user.setPassword("");
					user.setPryNumber(0);
					user.setPhoneNumber("");
					user.setEmail("");

				} else if (line[0].compareTo(Protocol.SENDMESSAGE) == 0) { // ���ǹ濡�� �޼���������

					for (int i = 0; i < waitUserList.size(); i++) {
						waitUserList.get(i).pw
								.println(Protocol.SENDMESSAGE_ACK + "|" + user.getIdName() + " |" + line[1]);// ���濡
																												// Message
																												// ����;
						waitUserList.get(i).pw.flush();
					}
				} else if (line[0].compareTo(Protocol.ROOMMAKE) == 0) { // �游���
					String userContent[] = line[1].split("%");

					String sql = "";
					Room tempRoom = new Room();
					if (userContent.length == 5) { // �������
						sql = "Insert into Room values(NEXTVAL(num),?,?,?,?,?,?)";
						pstmt = conn.prepareStatement(sql);

						pstmt.setString(1, userContent[0]); // title
						pstmt.setString(2, userContent[1]); // password
						pstmt.setString(3, userContent[2]); // count
						pstmt.setString(4, user.getIdName()); // �����̸�
						pstmt.setString(5, userContent[3]); // ����
						pstmt.setString(6, userContent[4]); // condition 1

						tempRoom.setTitle(userContent[0]);
						tempRoom.setrPassword(userContent[1]);
						tempRoom.setUserCount(userContent[2]);
						tempRoom.setMasterName(user.getIdName());
						tempRoom.setSubject(userContent[3]);
						tempRoom.setCondtionP(Integer.parseInt(userContent[4]));

						sql = "select * from Room where title = '" + userContent[0] + "' and password = '"
								+ userContent[1] + "' and  userCount= '" + userContent[2] + "' and  admin= '"
								+ user.getIdName() + "' and  subject= '" + userContent[3] + "'";

					} else { // ������
						sql = "Insert into Room values(NEXTVAL(num),?,'',?,?,?,?)";
						pstmt = conn.prepareStatement(sql);

						pstmt.setString(1, userContent[0]); // title
						pstmt.setString(2, userContent[1]); // count
						pstmt.setString(3, user.getIdName()); // �����̸�
						pstmt.setString(4, userContent[2]); // ����
						pstmt.setString(5, userContent[3]); // condition 0;

						tempRoom.setTitle(userContent[0]);
						tempRoom.setUserCount(userContent[1]);
						tempRoom.setMasterName(user.getIdName());
						tempRoom.setSubject(userContent[2]);
						tempRoom.setCondtionP(Integer.parseInt(userContent[3]));

						sql = "select * from Room where title = '" + userContent[0] + "' and  userCount= '"
								+ userContent[1] + "' and  admin= '" + user.getIdName() + "' and  subject= '"
								+ userContent[2] + "'";
					}

					int su = pstmt.executeUpdate(); // �׻� ��� ����(CRUD)���� ������ return
					System.out.println(su + "Room ����[DB]");

					pstmt = conn.prepareStatement(sql);
					ResultSet rs = pstmt.executeQuery(sql);

					int count = 0;
					int priNumber = 0;

					while (rs.next()) {
						count++;
						priNumber = rs.getInt("RID");
					}

					if (count != 0) {
						tempRoom.setrID(priNumber);
						tempRoom.roomInUserList.add(this);
						roomtotalList.add(tempRoom);
						priRoom = tempRoom; // ���� ���� ������
					}

					System.out.println("[Room ����]");
					for (Room room : roomtotalList) {
						System.out.println(room.toString() + "����濡 �ο��� : " + room.roomInUserList.size());
					}
					System.out.println("[��ü Room ���� ]" + roomtotalList.size());

					// RoomtotalList ��ü ������ Message�� ���� �����ߵȴ�
					String roomListMessage = "";

					for (int i = 0; i < roomtotalList.size(); i++) {
						roomListMessage += (roomtotalList.get(i).getrID() + "%" + roomtotalList.get(i).getTitle() + "%"
								+ roomtotalList.get(i).getrPassword() + "%" + roomtotalList.get(i).getUserCount() + "%"
								+ roomtotalList.get(i).getMasterName() + "%" + roomtotalList.get(i).getSubject() + "%"
								+ roomtotalList.get(i).getCondtionP() + "%" + roomtotalList.get(i).roomInUserList.size()
								+ "-");
					}

					System.out.println(roomListMessage);

					for (int i = 0; i < waitUserList.size(); i++) {
						if (waitUserList.get(i).user.getIdName().compareTo(tempRoom.getMasterName()) == 0) { // �游�������Դ�
																												// �ٷ�
																												// ä��ȭ������
							waitUserList.get(i).pw.println(Protocol.ROOMMAKE_OK1 + "|" + tempRoom.getMasterName());
							waitUserList.get(i).pw.flush();
						} else { // �ٸ� �������鿡�Դ� ���游 ���ΰ�ħ
							waitUserList.get(i).pw.println(Protocol.ROOMMAKE_OK + "|" + roomListMessage);
							waitUserList.get(i).pw.flush();
						}

					}

					waitUserList.remove(this); // ���濡�� ������
					System.out.println("���� �ο��� ������ �游�������" + waitUserList.size());

					String userline = "";
					for (int i = 0; i < waitUserList.size(); i++) {
						userline += (waitUserList.get(i).user.getIdName() + ":");
					}
					for (int i = 0; i < waitUserList.size(); i++) {
						waitUserList.get(i).pw.println(Protocol.ENTERLOGIN_OK + "|" + tempRoom.getMasterName() + "|����"
								+ tempRoom.getrID() + "�� ���� ��������ϴ�.|" + userline);
						waitUserList.get(i).pw.flush();
					}

					String path = "C:\\eclipse\\WorkSpace\\CooProject\\roomFolder\\" + priNumber;
					File folder = new File(path);

					if (folder.exists()) {
						try {
							System.out.println("������ �̹� �����մϴ�.");
						} catch (Exception e1) {
							e1.printStackTrace();
						}
					} else if (!folder.exists()) {
						folder.mkdir();
						System.out.println("������ �����Ǿ����ϴ�.");
					}

				} else if (line[0].compareTo(Protocol.ENTERROOM) == 0) { // [�� �����ư]

					String thisName = waitUserList.get(waitUserList.indexOf(this)).user.getIdName(); //

					int roomid = Integer.parseInt(line[1]); // ��ID

					int index = 0;
					for (int i = 0; i < roomtotalList.size(); i++) {
						if (roomtotalList.get(i).getrID() == roomid) {
							roomtotalList.get(i).roomInUserList.add(this); // �濡 ���� �ְ�
							priRoom = roomtotalList.get(i);
							index = i;
						}
					}

					String roomListMessage = "";
					for (int i = 0; i < roomtotalList.size(); i++) {
						roomListMessage += (roomtotalList.get(i).getrID() + "%" + roomtotalList.get(i).getTitle() + "%"
								+ roomtotalList.get(i).getrPassword() + "%" + roomtotalList.get(i).getUserCount() + "%"
								+ roomtotalList.get(i).getMasterName() + "%" + roomtotalList.get(i).getSubject() + "%"
								+ roomtotalList.get(i).getCondtionP() + "%" + roomtotalList.get(i).roomInUserList.size()
								+ "-");
					}

					System.out.println(roomListMessage);
					System.out.println(thisName);

					String roomMember = ""; // --->���� �渶�� �濡 ������ �־��ִ� �� �߰� �濡 ������ ������ �������;��� ����������� ã�ƾ���

					for (int i = 0; i < roomtotalList.get(index).roomInUserList.size(); i++) { // ��ȿ� ������ ����ŭ
						roomMember += (roomtotalList.get(index).roomInUserList.get(i).user.getIdName() + "%");
					}

					for (int i = 0; i < waitUserList.size(); i++) {
						if (waitUserList.get(i).user.getIdName().compareTo(thisName) == 0) { // ����»�����Դ�
																								// �ٷ�
																								// ä��ȭ������\
							waitUserList.get(i).pw.println(Protocol.ENTERROOM_OK1 + "|" + "message");
							waitUserList.get(i).pw.flush();
						} else { // �ٸ� �������鿡�Դ� ���游 ���ΰ�ħ
							waitUserList.get(i).pw.println(Protocol.ROOMMAKE_OK + "|" + roomListMessage); // �븮��Ʈ ���ΰ�ħ
							waitUserList.get(i).pw.flush();
						}

					}

					String folder = "C:\\eclipse\\WorkSpace\\CooProject\\roomFolder\\" + priRoom.getrID() + "\\";
					System.out.println(folder);
					// ���������� ���ϰ�ü ����
					File file = new File(folder);

					// ������� ������ ���� ���ϰ�ü�� ����Ʈ�� �޴´�.
					File[] list = file.listFiles();

					String fileList = "";
					// ����Ʈ���� ������ �ϳ��� ������
					for (File f : list) {
						// ������ ��츸 ���
						if (f.isFile()) {
							fileList += (f.getName() + "%");
						}
						System.out.println();
					}
					System.out.println("FileList : " + fileList);

					for (int i = 0; i < roomtotalList.get(index).roomInUserList.size(); i++) {
						roomtotalList.get(index).roomInUserList.get(i).pw.println(Protocol.ENTERROOM_USERLISTSEND + "|"
								+ roomMember + "|" + user.getIdName() + "���� �����ϼ̽��ϴ�.|" + fileList);
						roomtotalList.get(index).roomInUserList.get(i).pw.flush();
					}

					waitUserList.remove(this); // ���濡�� ������
					System.out.println("�����嵿�� �κ�  -->>[���� �ο��� ]" + waitUserList.size());

					String userline = ""; // ä��â��

					for (int i = 0; i < waitUserList.size(); i++) {
						userline += (waitUserList.get(i).user.getIdName() + ":" + "");
					}
					for (int i = 0; i < waitUserList.size(); i++) {
						waitUserList.get(i).pw.println(
								Protocol.EXITWAITROOM + "|" + thisName + "|���� " + roomid + "�濡 �����Ͽ����ϴ�.|" + userline);// ���濡
																														// �ٲ��ְ�
						// Message
						// ����;
						waitUserList.get(i).pw.flush();
					}

				} else if (line[0].compareTo(Protocol.EXITCHATTINGROOM) == 0) // �� ������ ��ư.
				{

					int roomIndex = 0;
					boolean con = true;

					for (int i = 0; i < roomtotalList.size(); i++) {
						if (roomtotalList.get(i).getrID() == priRoom.getrID()) {

							if (roomtotalList.get(i).roomInUserList.size() == 1) // ���Ë� �ڱⰡ �������� ��.
							{
								System.out.println("���ö� ���� �������϶�");
								roomtotalList.remove(priRoom);
								priRoom = new Room();
								con = false;

							} else { // �ּ� 2���� ��
								System.out.println("���ö� ���� �������ƴҶ�!! XXX");
								roomtotalList.get(i).roomInUserList.remove(this); // �濡 ���� ����
								priRoom = new Room();// ������� ����ְ�
								roomIndex = i;

							}

						}
					}

					if (con) // �����ִ¹濡 �ּ� 2���̻��϶�
					{
						String roomMember = ""; // --->���� �渶�� �濡 ������ �־��ִ� �� �߰� �濡 ������ ������ �������;��� ����������� ã�ƾ���

						for (int i = 0; i < roomtotalList.get(roomIndex).roomInUserList.size(); i++) { // ��ȿ� ������ ����ŭ
							roomMember += (roomtotalList.get(roomIndex).roomInUserList.get(i).user.getIdName() + "%");
						}

						System.out.println("Ư���濡 ����� : " + roomtotalList.get(roomIndex).roomInUserList.size());
						System.out.println(roomMember);
						for (int i = 0; i < roomtotalList.get(roomIndex).roomInUserList.size(); i++) {
							roomtotalList.get(roomIndex).roomInUserList.get(i).pw
									.println(Protocol.ENTERROOM_USERLISTSEND + "|" + roomMember + "|" + user.getIdName()
											+ "���� �����ϼ̽��ϴ�.");
							roomtotalList.get(roomIndex).roomInUserList.get(i).pw.flush();
						}
					}

					String roomListMessage = "";

					System.out.println(roomListMessage);

					waitUserList.add(this); // ���濡�� �߰�
					if (roomtotalList.size() > 0) {
						roomListMessage = "";
						for (int i = 0; i < roomtotalList.size(); i++) {
							roomListMessage += (roomtotalList.get(i).getrID() + "%" + roomtotalList.get(i).getTitle()
									+ "%" + roomtotalList.get(i).getrPassword() + "%"
									+ roomtotalList.get(i).getUserCount() + "%" + roomtotalList.get(i).getMasterName()
									+ "%" + roomtotalList.get(i).getSubject() + "%"
									+ roomtotalList.get(i).getCondtionP() + "%"
									+ roomtotalList.get(i).roomInUserList.size() + "-");
						}
					} else {
						roomListMessage = "-";
					}

					for (int i = 0; i < waitUserList.size(); i++) {
						waitUserList.get(i).pw.println(Protocol.ROOMMAKE_OK + "|" + roomListMessage); // �븮��Ʈ ���ΰ�ħ
						waitUserList.get(i).pw.flush();
					}

					System.out.println("����ǵ��� �κ�  -->>[���� �ο��� ]" + waitUserList.size());
					String userline = ""; // ä��â��
					for (int i = 0; i < waitUserList.size(); i++) {
						userline += (waitUserList.get(i).user.getIdName() + ":");
					}
					for (int i = 0; i < waitUserList.size(); i++) {
						waitUserList.get(i).pw.println(
								Protocol.EXITWAITROOM + "|" + user.getIdName() + "|���� ���ǿ��� �����Ͽ����ϴ�.|" + userline);// ���濡
																													// �ٲ��ְ�
						// Message
						// ����;
						waitUserList.get(i).pw.flush();
					}

				} else if (line[0].compareTo(Protocol.CHATTINGSENDMESSAGE) == 0) // ä�ù濡�� �޼��� ������
				{

					int roomUserSize = roomtotalList.get(roomtotalList.indexOf(priRoom)).roomInUserList.size();

					for (int i = 0; i < roomUserSize; i++) {
						roomtotalList.get(roomtotalList.indexOf(priRoom)).roomInUserList.get(i).pw
								.println(Protocol.CHATTINGSENDMESSAGE_OK + "|" + user.getIdName() + "|" + line[1]); // ä�ù�
																													// ����鿡��
																													// �޼���
						roomtotalList.get(roomtotalList.indexOf(priRoom)).roomInUserList.get(i).pw.flush();
					}

				} else if (line[0].compareTo(Protocol.CHATTINGFILESEND_SYN) == 0) // FIle���� ��ũ
				{

					fileName = line[1];
					System.out.println(fileName);
					pw.println(Protocol.CHATTINGFILESEND_SYNACK + "|" + "Message");
					pw.flush();

				} else if (line[0].compareTo(Protocol.CHATTINGFILESEND_FILE) == 0) { // 3

					System.out.println("�� ���� Size : " + line[1]);

					long filesize = Long.parseLong(line[1]);

					InputStream is = socket.getInputStream();

					// ������ ������½�Ʈ�� ��ü ����

					String path = "C:\\eclipse\\WorkSpace\\CooProject\\roomFolder\\" + priRoom.getrID() + "\\"
							+ fileName;

					FileOutputStream fos = new FileOutputStream(path);

					System.out.println("���� �ٿ�ε� ���� !!!");

					// ������ ���� ������ ���Ͽ� ����

					byte[] b = new byte[512];

					int n = 0;

					while ((n = is.read(b, 0, b.length)) > 0) {

						fos.write(b, 0, n);
						System.out.println("N:" + n);
						System.out.println(n + "bytes �ٿ�ε� !!!");
						n += n;
						if (n >= filesize)
							break;
					}

					fos.close();
					System.out.println("���� �ٿ�ε� �� !!!");

					String folder = "C:\\eclipse\\WorkSpace\\CooProject\\roomFolder\\" + priRoom.getrID() + "\\";
					// ���������� ���ϰ�ü ����
					File file = new File(folder);

					// ������� ������ ���� ���ϰ�ü�� ����Ʈ�� �޴´�.
					File[] list = file.listFiles();

					String fileList = "";
					// ����Ʈ���� ������ �ϳ��� ������
					for (File f : list) {
						// ������ ��츸 ���
						if (f.isFile()) {
							fileList += (f.getName() + "%");
						}
					}

					int roomUserSize = roomtotalList.get(roomtotalList.indexOf(priRoom)).roomInUserList.size();

					for (int i = 0; i < roomUserSize; i++) {
						roomtotalList.get(roomtotalList.indexOf(priRoom)).roomInUserList.get(i).pw
								.println(Protocol.CHATTINGFILESEND_FILEACK + "|" + fileList); // ä�ù�
																								// ����鿡��
																								// ���� ����
						roomtotalList.get(roomtotalList.indexOf(priRoom)).roomInUserList.get(i).pw.flush();
					}

				} else if (line[0].compareTo(Protocol.CHATTINGFILEDOWNLOAD_SYN) == 0) // ���� �ٿ�ε� ����
				{
					String folder = "C:\\eclipse\\WorkSpace\\CooProject\\roomFolder\\" + priRoom.getrID() + "\\";
					// ���������� ���ϰ�ü ����
					File file = new File(folder);

					// ������� ������ ���� ���ϰ�ü�� ����Ʈ�� �޴´�.
					File[] list = file.listFiles();

					File selectedFile = new File(folder);
					// ����Ʈ���� ������ �ϳ��� ������
					for (File f : list) {
						// ������ ��츸 ���
						if (f.isFile()) {
							if (f.getName().compareTo(line[1]) == 0) // ������ ������
							{
								selectedFile = f;
								System.out.println("�̰� ����ǳ�???1111");
							}
						}
					}

					System.out.println("���� ���� ��" + selectedFile.getName() + "/" + selectedFile.length());

					pw.println(Protocol.CHATTINGFILEDOWNLOAD_SEND + "|" + selectedFile.length());
					pw.flush();

					OutputStream os = socket.getOutputStream();

					System.out.println("���� ������ ���� !!!");
					// ���� ������ �Է� ��Ʈ�� ��ü ����
					String fileRouth = folder + selectedFile.getName();
					FileInputStream fis = new FileInputStream(fileRouth);

					long filesize = selectedFile.length();

					// ������ ������ ������
					byte[] b = new byte[512];
					int n;
					while ((n = fis.read(b, 0, b.length)) > 0) {
						os.write(b, 0, n);
						System.out.println(n + "bytes ���� !!!");
						n += n;
						if (n >= filesize)
							break;
					}

				}

			} // while

			br.close();
			pw.close();
			socket.close();

		} catch (

		IOException io) {
			io.printStackTrace();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
}
