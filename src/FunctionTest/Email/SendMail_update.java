package FunctionTest.Email;

import java.util.Date;
import java.util.Properties;
import javax.mail.Authenticator;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
 
public class SendMail_update {
 
    public static int sendmail(String email) {
    	
    	int number = 0;
         
        Properties p = System.getProperties();
        p.put("mail.smtp.starttls.enable", "true");     // gmail�� true ����
        p.put("mail.smtp.host", "smtp.naver.com");      // smtp ���� �ּ�
        p.put("mail.smtp.auth","true");                 // gmail�� true ����
        p.put("mail.smtp.port", "587");                 // ���̹� ��Ʈ
        p.put("mail.smtp.port", "587");                 // ���̹� ��Ʈ
        p.put("mail.smtp.ssl.protocols", "TLSv1.2");
       
           
        Authenticator auth = new MyAuthentication();
        //session ���� ��  MimeMessage����
        Session session = Session.getDefaultInstance(p, auth);
        MimeMessage msg = new MimeMessage(session);
         
        try{
            //���������ð�
            msg.setSentDate(new Date());
            InternetAddress from = new InternetAddress() ;
            from = new InternetAddress("eownsl999@naver.com"); //�߽��� ���̵�
            // �̸��� �߽���
            msg.setFrom(from);
            // �̸��� ������
            InternetAddress to = new InternetAddress("eownsl999@naver.com");
            msg.setRecipient(Message.RecipientType.TO, to);
            // �̸��� ����
            msg.setSubject("[TEST]������ȣ�Դϴ�.", "UTF-8");
            // �̸��� ����
            
            number = (int)(Math.random()*9999)+1000;
            msg.setText("������ȣ : " +number + "������ȣ�� �Է����ּ���!!", "UTF-8");
            // �̸��� ���
            msg.setHeader("content-Type", "text/html");
            //���Ϻ�����
            javax.mail.Transport.send(msg, msg.getAllRecipients());
             
        }catch (AddressException addr_e) {
            addr_e.printStackTrace();
        }catch (MessagingException msg_e) {
            msg_e.printStackTrace();
        }catch (Exception msg_e) {
            msg_e.printStackTrace();
        }
        
        return number;
    }
}
 
class MyAuthentication extends Authenticator {
      
    PasswordAuthentication pa;
    public MyAuthentication(){
         
        String id = "eownsl999@naver.com";  //���̹� �̸��� ���̵�
        String pw = "password";        //���̹� ��й�ȣ
 
        // ID�� ��й�ȣ�� �Է��Ѵ�.
        pa = new PasswordAuthentication(id, pw);
    }
 
    // �ý��ۿ��� ����ϴ� ��������
    public PasswordAuthentication getPasswordAuthentication() {
        return pa;
    }
} 
  