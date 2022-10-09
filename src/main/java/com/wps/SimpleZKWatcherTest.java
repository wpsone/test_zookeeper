package com.wps;

import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.ZooKeeper;

import java.io.IOException;

/**
 * 一个简单的ZooKeeper监听器应用程序编写规范
 */
public class SimpleZKWatcherTest {
    static ZooKeeper zk = null;

    public static void main(String[] args) throws IOException, KeeperException, InterruptedException {
        // TODO 注释： 第一步： 获取链接
        // 该抽象可以理解成：链接对象/会话对象/客户端
        // new ZooKeeper("bigdata02:2181,bigdata03:2181,bigdata04:2181", 5000, defaultWatcher)
        zk = new ZooKeeper("bigdata02:2181,bigdata03:2181,bigdata04:2181", 5000, new Watcher() {
            // TODO 注释： 第三步： 回调方法
            // 如果说这个方法被调用了，就是意味着这个客户端收到了系统的一个响应事件
            // zk 系统发送事件通知到 客户端，是要走网络传输的。 但是网络是不可靠的（网络有延迟，或者数据丢回）
            @Override
            public void process(WatchedEvent event) {

                //  编写业务回调逻辑，if else判断，是因为不同节点发生了不同的事件，必然会有不同的逻辑处理
                String path = event.getPath();
                Event.EventType type = event.getType();

                if (path.equals("/node_path") && type == Event.EventType.NodeDataChanged) {
                    //  业务回调

                    //  此处注册监听的目的，就是为了实现循环监听
                    try {
                        byte[] data = zk.getData("/node_path", true, null);
                        System.out.println("回调中获取到的结果："+new String(data));
                    } catch (KeeperException e) {
                        e.printStackTrace();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                } else if (path.equals("/node_path") && type == Event.EventType.NodeChildrenChanged) {
                    // NodeChildrenChanged 只关心当前节点的子节点的个数是否发生了变化，不关心子节点的数据有没有发生变化
                }
            }
        });

        // TODO  注释： 第二步： 注册监听，监听 /node_path 节点的数据是否改变
        // 两件事：获取节点的数据 + 给当前节点注册了一个监听
        // zookeeper服务端会发送一个事件通知 WatchedEvent 过来,然后客户端去回调对应的监听方法 process()
        byte[] data1 = zk.getData("/node_path", true, null);
        byte[] data2 = zk.getData("/node_path", new Watcher() {
            @Override
            public void process(WatchedEvent watchedEvent) {
                // 业务逻辑
            }
        }, null);
    }
}
