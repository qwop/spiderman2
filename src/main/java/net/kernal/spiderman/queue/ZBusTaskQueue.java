package net.kernal.spiderman.queue;

import java.io.IOException;

import org.zbus.mq.Consumer;
import org.zbus.mq.MqConfig;
import org.zbus.mq.Producer;
import org.zbus.net.http.Message;

import net.kernal.spiderman.K;
import net.kernal.spiderman.task.Task;

public class ZBusTaskQueue implements TaskQueue {

	private int timeout = 5000;
	private Producer producer;
	private Consumer consumer;
	public ZBusTaskQueue(MqConfig mqCfg, int timeout) {
		this.producer = new Producer(mqCfg.getBroker(), mqCfg.getMq());
		this.consumer = new Consumer(mqCfg);
		this.timeout = timeout;
	}
	
	public Task poll() {
		try {
			Message msg = consumer.recv(timeout);
			if (msg == null) {
				return null;
			}
			byte[] data = msg.getBody();
			Task task = (Task)K.deserialize(data);
			return task;
		} catch (IOException | InterruptedException e) {
			e.printStackTrace();
		}
		return null;
	}

	public void put(Task task) {
		byte[] data = K.serialize(task);
		Message msg = new Message();
		msg.setAck(true);
		msg.setBody(data);
		try {
			producer.sendAsync(msg);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public long size() {
		return 0;
	}
	
}
