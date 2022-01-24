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
 
    public static int SendMail(String email) {
    	
    	int number = 0;
         
        Properties p = System.getProperties();
        p.put("mail.smtp.starttls.enable", "true");     
        p.put("mail.smtp.host", "smtp.naver.com");      
        p.put("mail.smtp.auth","true");                 
        p.put("mail.smtp.port", "587");                               
        p.put("mail.smtp.ssl.protocols", "TLSv1.2");
       
           
        Authenticator auth = new MyAuthentication();
      
        Session session = Session.getDefaultInstance(p, auth);
        MimeMessage msg = new MimeMessage(session);
         
        try{
           
            msg.setSentDate(new Date());
            InternetAddress from = new InternetAddress() ;
            from = new InternetAddress("eownsl999@naver.com"); //발신 이메일
            
            msg.setFrom(from);
            	
            InternetAddress to = new InternetAddress(email);
            msg.setRecipient(Message.RecipientType.TO, to);// 수신이메일
            
            //메일제목
            msg.setSubject("[TEST]인증코드입니다", "UTF-8");
            
            
            // 메일내용       
            number = (int)(Math.random()*9999)+1000;
            msg.setText("인증번호 : " +number + " 입력해주세요", "UTF-8");
           
            msg.setHeader("content-Type", "text/html");

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
         
        String id = "eownsl999@naver.com";  //네이버 아이디
        String pw = "rkdeogns1!";        //비밀번호
 
        // ID�� ��й�ȣ�� �Է��Ѵ�.
        pa = new PasswordAuthentication(id, pw);
    }
 
    // �ý��ۿ��� ����ϴ� ��������
    public PasswordAuthentication getPasswordAuthentication() {
        return pa;
    }
} 
  
