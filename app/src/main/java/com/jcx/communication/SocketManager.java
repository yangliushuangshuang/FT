package com.jcx.communication;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.ServerSocket;
import java.net.Socket;

import com.jcx.util.Util;

public class SocketManager {
	private ServerSocket server;
	public SocketManager(ServerSocket server){
		this.server = server;
	}
	public boolean ReceiveFile(HotSpotImp hotSpotImp){
		try{
			Socket name = server.accept();
			InputStream nameStream = name.getInputStream();
			InputStreamReader streamReader = new InputStreamReader(nameStream);
			BufferedReader br = new BufferedReader(streamReader);
			String[] fileInfo = br.readLine().split(Util.SPLITER);
			String fileName;
			long length;
			try {
				fileName = fileInfo[0];
				length = Long.parseLong(fileInfo[1]);
			}catch(ArrayIndexOutOfBoundsException e){
				e.printStackTrace();
				return false;
			}
			hotSpotImp.onRcvBegin(fileName,length);
			br.close();
			streamReader.close();
			nameStream.close();
			name.close();
			Socket data = server.accept();
			InputStream dataStream = data.getInputStream();
			String savePath = Util.RECEIVE_DIR+File.separator + fileName;
			FileOutputStream file = new FileOutputStream(savePath, false);
			byte[] buffer = new byte[Util.BLOCK_SIZE];
			int size = -1;
			while ((size = dataStream.read(buffer,0,Util.BLOCK_SIZE)) != -1){
				byte[] a = new byte[Util.HEAD_LEN];
				System.arraycopy(buffer, 0, a, 0, Util.HEAD_LEN);
				int rcvIndex = (int)Util.bytes2long(a);
				file.write(buffer, Util.HEAD_LEN, size-Util.HEAD_LEN);
				hotSpotImp.onUpdate(rcvIndex);
			}
			file.close();
			dataStream.close();
			data.close();
			return true;
		}catch(Exception e){
			return false;
		}
	}
	public boolean sendFile(File file, String ipAddress, int port,HotSpotImp hotSpotImp){
		try {
			Socket name = new Socket(ipAddress, port);
			OutputStream outputName = name.getOutputStream();
			OutputStreamWriter outputWriter = new OutputStreamWriter(outputName);
			BufferedWriter bwName = new BufferedWriter(outputWriter);
			bwName.write(file.getName()+ Util.SPLITER+file.length());
			hotSpotImp.onSendBegin();
			bwName.close();
			outputWriter.close();
			outputName.close();
			name.close();
			
			Socket data = new Socket(ipAddress, port);
			OutputStream outputData = data.getOutputStream();
			FileInputStream fileInput = new FileInputStream(file);
			int size = -1;
			byte[] buffer = new byte[Util.BLOCK_SIZE];
			int sendIndex=0;
			while((size = fileInput.read(buffer, Util.HEAD_LEN, Util.BLOCK_SIZE-Util.HEAD_LEN)) != -1){
				byte[] a = Util.long2bytes(++sendIndex);
				System.arraycopy(a,0,buffer,0,Util.HEAD_LEN);
				outputData.write(buffer, 0, size + Util.HEAD_LEN);
				hotSpotImp.onUpdate(sendIndex);
			}
			outputData.flush();
			outputData.close();
			fileInput.close();
			data.close();
			return true;
		} catch (Exception e) {
			return false;
		} 
	}
}
