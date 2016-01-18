package net.kernal.spiderman.queue;

import java.io.IOException;

import org.zbus.broker.Broker;
import org.zbus.mq.Consumer;
import org.zbus.mq.MqConfig;
import org.zbus.mq.Producer;
import org.zbus.net.Sync.ResultCallback;
import org.zbus.net.http.Message;

import net.kernal.spiderman.K;
import net.kernal.spiderman.Spiderman;

public class ZBusQueue<T> implements Queue<T> {

	private int beatPeriod = 5000;
	private Broker broker;
	private Producer producer;
	private Consumer consumer;
	private boolean serialize;
	
	public ZBusQueue(Broker broker, String mq, boolean serialize) {
		this.broker = broker;
	    final MqConfig cfg = new MqConfig(); 
	    cfg.setBroker(broker);
	    cfg.setMq(mq);
	    
	    this.producer = new Producer(cfg);
		try {
			this.producer.createMQ();
		} catch (IOException | InterruptedException e) {
			throw new RuntimeException(e);
		}
		
		this.consumer = new Consumer(cfg);
		this.serialize = serialize;
	}
	
	@SuppressWarnings("unchecked")
	public T take() {
		Message msg = null;
		while(true){
			try {
				// 啥时候可以把beatPeriod干掉，这个应该是底层去实现的。
				msg = consumer.recv(beatPeriod);
				if(msg != null) {
					break;
				}
			} catch (IOException | InterruptedException e) {
				throw new Spiderman.Exception("zbus consumer recv error", e);
			}
		}
		final byte[] data = msg.getBody();
		T t = null;
		if (serialize) {
			t = (T)K.deserialize(data);
		} else {
			t = (T)data;
		}
		
		return t;
	}

	public void append(T t) {
		byte[] data;
		if (serialize) {
			data = K.serialize(t);
		} else {
			data = (byte[])t;
		}
		Message msg = new Message();
		msg.setBody(data);
		try {
			producer.invokeAsync(msg, new ResultCallback<Message>() {
				public void onReturn(Message result) {
					// ignore
				}
			});
		} catch (IOException e) {
			throw new Spiderman.Exception("zbus producer invoke error", e);
		}
	}

	public void clear() {
		try {
			this.consumer.close();
			this.broker.close();
		} catch (IOException e) {
			throw new Spiderman.Exception("zbus client close error", e);
		}
	}

}
