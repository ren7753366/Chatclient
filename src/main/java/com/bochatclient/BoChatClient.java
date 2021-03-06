package com.bochatclient;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.List;

import org.json.JSONObject;

import com.bochatclient.buffer.InputCircleBuffer;
import com.bochatclient.enter.QueryListBean;
import com.bochatclient.enter.TalkBean;
import com.bochatclient.enter.UserEnterBean;
import com.bochatclient.enter.UserInfoBean;
import com.bochatclient.enums.ErrorEnum;
import com.bochatclient.exception.BoException;
import com.bochatclient.listener.ErrorListener;
import com.bochatclient.listener.MsgListener;
import com.bochatclient.packet.PacketBase;
import com.bochatclient.utils.BeanUtil;

public class BoChatClient {
	private String mIp;
	private int mPort;
	private UserEnterBean loginBean;
	
	private Socket s = null;
	private DataOutputStream dos = null;
	private DataInputStream dis = null;
	private boolean bConnected = false;
	private boolean bStop = false;
	
	private MsgListener msgListener = null;
	private ErrorListener errorListener = null;

	private byte[] recvBuf = new byte[10240];
	
	private Object lock = new Object();

	Thread tRecv = new Thread(new RecvThread());

	Thread heartBeat = new Thread(new HeartBeatThread());
	
	InputCircleBuffer buffer = new InputCircleBuffer();
	
	public BoChatClient() {
	}
	
	/**
	 * 
	 * @param ip
	 * @param port
	 */
	public BoChatClient(String ip, int port) {
		this.mIp = ip;
		this.mPort = port;
	}
	
	public void setLoginBean(UserEnterBean loginBean) {
		this.loginBean = loginBean;
	}

	/**
	 * 
	 * @param msgListener
	 */
	public void setMsgListener(MsgListener msgListener) {
		this.msgListener = msgListener;
	}
	
	public void setErrorListener(ErrorListener errorListener) {
		this.errorListener = errorListener;
	}

	/**
	 * 
	 * @param ip
	 * @param port
	 */
	public void setIpPort(String ip, int port) {
		this.mIp = ip;
		this.mPort = port;
	}
	
//	public void reConnect() {
//		tRecv.stop();
//		heartBeat.stop();
//		disconnect();
//		connect();
//		
//		tRecv.start();
//		heartBeat.start();
//		
//	}
	
	public void start() {
		tRecv.start();
		heartBeat.start();
	}

	public void enterroom() {
		try {
			dos.write(getEnterPacket(loginBean));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * 
	 * @param msg	消息内容
	 * @param action	消息类型：0：对大家说；1：对某人公开说；2：对某人私聊
	 * @param toMasterId	action为1，2时 接收消息人
	 * @param toMasterNick
	 */
	public void sendMessage(String msg,int action,String toMasterId,String toMasterNick) {
		try {
			dos.write(getMsgPacket(msg,action,toMasterId,toMasterNick));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public void sendQueryList(String pno, String rpp ,String uType) {
		try {
			dos.write(getListPacket(pno, rpp, uType));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public void sendQueryUserMsg(String uid) {
		try {
			dos.write(getUserInfoPacket(uid));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void connect() {
		try {
			s = new Socket(mIp, mPort);
			dos = new DataOutputStream(s.getOutputStream());
			dis = new DataInputStream(s.getInputStream());
			System.out.println("~~~~~~~~连接成功~~~~~~~~!");
			bConnected = true;
		} catch (UnknownHostException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	public void disconnect() {
		try {
			//关闭连接
			dos.close();
			dis.close();
			s.close();
			
			//关闭线程
			bStop = true;
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private class RecvThread implements Runnable {
		public void run() {
			while (!bStop) {
				synchronized (lock) {
					try{
//						Thread.sleep(40);
						buffer.readFromInputStream(dis);
						List<PacketBase> packetList = buffer.getPacket();
						while(packetList!=null){
	//						System.out.println("-----------------end--------------"+System.currentTimeMillis());
							for(int i=0;i<packetList.size();i++){
								PacketBase packet = packetList.get(i);
								if(packet != null){
									if(packet != null){
										int retCode = packet.getRetcode();
										if(retCode==0){
											msgListener.onReciveMsg(packet);
										}else{
											errorListener.onError(1,ErrorEnum.getErrorMsg(retCode));
											
										}
									}
								}
							}
							packetList=buffer.getPacket();
						}
								
//						} else {
//							try {
//								Thread.sleep(80);
//							} catch (InterruptedException e) {
//							}
//						}
					}catch(SocketException se) {
						se.printStackTrace();
					}catch(IOException e) {
						e.printStackTrace();
					}catch(BoException be) {
						errorListener.onError(2,"系统异常");
						break;
					}catch(Exception be) {
					}
				}
				
			}
		}
	}

	private class HeartBeatThread implements Runnable {
		public void run() {
			while(!bStop) {
				try {
					Thread.sleep(10000);
				} catch (InterruptedException e1) {
					e1.printStackTrace();
				}
			
				if (bConnected) {
					try {
						dos.write(getHeartBeatPacket());
					} catch (SocketException se) {
						se.printStackTrace();
					} catch (IOException e) {
						e.printStackTrace();
					}
					System.out.println("heart ! ");
				}
			}
		}
	}

	public byte[] getHeartBeatPacket() {
		byte[] ret = new byte[9];
		ret[3] = 9;
		ret[4] = 3;
		ret[5] = 0;
		ret[8] = '1';
		return ret;
	}

	public byte[] getEnterPacket(UserEnterBean bean) {
		
		JSONObject job = new JSONObject(BeanUtil.beanToJson(bean));
		byte[] packetContent = job.toString().getBytes();
		
//		byte[] packetContent = (new Gson()).toJson(bean).getBytes();
		
		int len = packetContent.length;
		byte[] ret = new byte[8 + len];

		int headint = len + 8;
		ret[0] = (byte) (headint >> 24 & 0xff);
		ret[1] = (byte) (headint >> 16 & 0xff);
		ret[2] = (byte) (headint >> 8 & 0xff);
		ret[3] = (byte) (headint & 0xff);
		for (int i = 0; i < len; i++) {
			ret[8 + i] = packetContent[i];
		}
		System.out.println(new String(ret));
		return ret;
	}
	
	public byte[] getMsgPacket(String msg,int action,String toMasterId,String toMasterNick) {
		
		JSONObject job = new JSONObject(BeanUtil.beanToJson(new TalkBean(msg,action,toMasterId,toMasterNick)));
		byte[] packetContent = job.toString().getBytes();
		
//		byte[] packetContent =  (new Gson()).toJson(new TalkBean(msg,action,toMasterId,toMasterNick)).getBytes();
		
		int len = packetContent.length;
		byte[] ret = new byte[8 + len];

		int headint = len + 8;
		ret[0] = (byte) (headint >> 24 & 0xff);
		ret[1] = (byte) (headint >> 16 & 0xff);
		ret[2] = (byte) (headint >> 8 & 0xff);
		ret[3] = (byte) (headint & 0xff);
		
		ret[4] = (byte) action;
		ret[5] = 2;
		
		for (int i = 0; i < len; i++) {
			ret[8 + i] = packetContent[i];
		}
		return ret;
	}
	
	// 获取聊天室成员列表请求包
	public byte[] getListPacket(String pno, String rpp ,String uType) {
		
		JSONObject job = new JSONObject(BeanUtil.beanToJson(new QueryListBean(pno, rpp, uType)));
		byte[] packetContent = job.toString().getBytes();
		
//		byte[] packetContent = (new Gson()).toJson(new QueryListBean(loginBean.getRid(), loginBean.getUid(), pno, rpp)).getBytes();
		int len = packetContent.length;
		byte[] ret = new byte[8 + len];

		int headint = len + 8;
		ret[0] = (byte) (headint >> 24 & 0xff);
		ret[1] = (byte) (headint >> 16 & 0xff);
		ret[2] = (byte) (headint >> 8 & 0xff);
		ret[3] = (byte) (headint & 0xff);
		
		ret[4] = 1;
		ret[5] = 6;
		
		for (int i = 0; i < len; i++) {
			ret[8 + i] = packetContent[i];
		}
		return ret;
	}
	
	public byte[] getUserInfoPacket(String uid) {
		
		JSONObject job = new JSONObject(BeanUtil.beanToJson(new UserInfoBean(uid)));
		byte[] packetContent = job.toString().getBytes();
		
//		byte[] packetContent = (new Gson()).toJson(new UserMsgBean(loginBean.getRid(), loginBean.getUid(), uid)).getBytes();
		
		int len = packetContent.length;
		byte[] ret = new byte[8 + len];

		int headint = len + 8;
		ret[0] = (byte) (headint >> 24 & 0xff);
		ret[1] = (byte) (headint >> 16 & 0xff);
		ret[2] = (byte) (headint >> 8 & 0xff);
		ret[3] = (byte) (headint & 0xff);
		
		ret[4] = 4;
		ret[5] = 2;
		
		for (int i = 0; i < len; i++) {
			ret[8 + i] = packetContent[i];
		}
		return ret;
	}
	
}
