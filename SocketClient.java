package chap19.sec07;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;

import org.json.JSONObject;

public class SocketClient {
	// 필드
	ChatServer chatServer;
	Socket socket;
	String clientIP;
	String chatName;
	DataInputStream dis;
	DataOutputStream dos;
	
	// 생성자
	public SocketClient(ChatServer chatServer, Socket socket) {
		try {
			this.chatServer = chatServer;
			this.socket = socket;
			this.dis = new DataInputStream(socket.getInputStream());
			this.dos = new DataOutputStream(socket.getOutputStream());
			InetSocketAddress isa = (InetSocketAddress) socket.getRemoteSocketAddress();
			this.clientIP = isa.getHostName();
			receive();
		} catch (IOException e) {
			 e.printStackTrace();
		}
	}
	
	// 메소드: JSON 받기
	public void receive() {
		chatServer.threadPool.execute(()-> {
			try {
				while(true) {
					String receiveJSON = dis.readUTF();
					JSONObject jsonObject = new JSONObject(receiveJSON);
					String command = jsonObject.getString("command");
					
					switch (command) {
					case "incoming"	:
						this.chatName = jsonObject.getString("data");
						chatServer.sendToAll(this, "들어왔습니다");
						chatServer.addSocketClient(this);
						break;
						
					case "message": 
						String message = jsonObject.getString("data");
						chatServer.sendToAll(this, message);
						break;
					}
				}
			} catch (IOException e) {
				 chatServer.sendToAll(this, "나갔습니다");
				 chatServer.removeSocketclient(this);
			}
		});
	}
	
	// 메소드: JSON 보내기
	public void send(String json) {
		try {
			dos.writeUTF(json);
			dos.flush();
		} catch (IOException e) {
			 
		}
	}
	
	// 메소드: 연결종료
	public void close() {
		try {
			socket.close();
		} catch (IOException e) {
			 
		}
	}
	
	

}
