package com.shadow.codecoverage.implant;

import java.util.BitSet;
import java.util.concurrent.locks.ReentrantLock;

/**
 * @Classname Implant
 * @Description TODO
 * @Date 2023/1/15 22:14
 * @Created by pepsi
 */
public class Implant {

    private static final SelfCallBarrier selfCallBarrier = new SelfCallBarrier();


    private static ImplantHandler implantHandler;

    /***
     *
     * @param listenerId
     * @param methodId
     * @param isThrown
     */
    public static void implantMethodOnReturn(int listenerId, int methodId, int isThrown) {
        //ignore
    }

    /**
     * 方法结束结算覆盖行，所以 implantMethodOnReturn 一般用不上。
     *
     * @param listenerId
     * @param methodId
     * @param coverLines
     */
    public static void recordMethodCoverLines(int listenerId, int methodId, BitSet coverLines) {
        try {
            if (implantHandler == null) {
                return;
            }
            implantHandler.recordMethodCoverLines(listenerId, methodId, coverLines);
        } catch (Throwable throwable) {
            //ignore
        }

    }

    /**
     * @param listenerId
     * @param methodId
     * @throws Throwable
     */
    public static void implantMethodOnBefore(final int listenerId, final int methodId) throws Throwable {
        try {
            if (implantHandler == null) {
                return;
            }
            implantHandler.handleOnBefore(listenerId, methodId);
        } catch (Throwable cause) {
            //ignore
        }
    }


    /**
     * @param argumentArray
     * @param listenerId
     * @param javaClassName
     * @param javaMethodName
     * @param javaMethodDesc
     * @param target
     * @throws Throwable
     */
    public static void implantPluginMethodOnBefore(final Object[] argumentArray,
                                                   final int listenerId,
                                                   final String javaClassName,
                                                   final String javaMethodName,
                                                   final String javaMethodDesc,
                                                   final Object target) throws Throwable {
        final Thread thread = Thread.currentThread();
        if (selfCallBarrier.isEnter(thread)) {
            return;
        }
        final SelfCallBarrier.Node node = selfCallBarrier.enter(thread);
        try {
            if (null == implantHandler) {
                return;
            }
            implantHandler.handleOnPluginMethodBefore(
                    listenerId,
                    argumentArray,
                    javaClassName,
                    javaMethodName,
                    javaMethodDesc,
                    target);
        } catch (Throwable cause) {
            //
        } finally {
            selfCallBarrier.exit(thread, node);
        }
    }

    /**
     * @param listenerId
     * @throws Throwable
     */
    public static void implantPluginMethodOnReturn(final int listenerId) throws Throwable {
        final Thread thread = Thread.currentThread();
        if (selfCallBarrier.isEnter(thread)) {
            return;
        }
        final SelfCallBarrier.Node node = selfCallBarrier.enter(thread);
        try {
            if (null == implantHandler) {
                return;
            }
            implantHandler.handleOnPluginMethodReturn(listenerId);
        } catch (Throwable cause) {
            //;
        } finally {
            selfCallBarrier.exit(thread, node);
        }
    }

    /**
     * 返回结果
     */
    public static class Ret {

        public static final int RET_STATE_NONE = 0;
        public static final int RET_STATE_RETURN = 1;
        public static final int RET_STATE_THROWS = 2;
        private static final Ret RET_NONE = new Ret(RET_STATE_NONE, null);
        /**
         * 返回状态(0:NONE;1:RETURN;2:THROWS)
         */
        public final int state;
        /**
         * 应答对象
         */
        public final Object respond;

        /**
         * 构造返回结果
         *
         * @param state   返回状态
         * @param respond 应答对象
         */
        private Ret(int state, Object respond) {
            this.state = state;
            this.respond = respond;
        }

        public static Ret newInstanceForNone() {
            return RET_NONE;
        }

        public static Ret newInstanceForReturn(Object object) {
            return new Ret(RET_STATE_RETURN, object);
        }

        public static Ret newInstanceForThrows(Throwable throwable) {
            return new Ret(RET_STATE_THROWS, throwable);
        }

    }

    /**
     * 本地线程
     */
    public static class SelfCallBarrier {

        static final int THREAD_LOCAL_ARRAY_LENGTH = 512;
        final Node[] nodeArray = new Node[THREAD_LOCAL_ARRAY_LENGTH];

        SelfCallBarrier() {
            cleanAndInit();
        }

        // 删除节点
        void delete(final Node node) {
            node.pre.next = node.next;
            if (null != node.next) {
                node.next.pre = node.pre;
            }
            // help gc
            node.pre = (node.next = null);
        }

        // 插入节点
        void insert(final Node top, final Node node) {
            if (null != top.next) {
                top.next.pre = node;
            }
            node.next = top.next;
            node.pre = top;
            top.next = node;
        }

        Node createTopNode() {
            return new Node(null, new ReentrantLock());
        }

        void cleanAndInit() {
            for (int i = 0; i < THREAD_LOCAL_ARRAY_LENGTH; i++) {
                nodeArray[i] = createTopNode();
            }
        }

        int abs(int val) {
            return val < 0
                    ? val * -1
                    : val;
        }

        boolean isEnter(Thread thread) {
            final Node top = nodeArray[abs(thread.hashCode()) % THREAD_LOCAL_ARRAY_LENGTH];
            Node node = top;
            try {
                // spin for lock
                while (!top.lock.tryLock()) ;
                while (null != node.next) {
                    node = node.next;
                    if (thread == node.thread) {
                        return true;
                    }
                }
                return false;
            } finally {
                top.lock.unlock();
            }
        }

        Node enter(Thread thread) {
            final Node top = nodeArray[abs(thread.hashCode()) % THREAD_LOCAL_ARRAY_LENGTH];
            final Node node = new Node(thread);
            try {
                while (!top.lock.tryLock()) ;
                insert(top, node);
            } finally {
                top.lock.unlock();
            }
            return node;
        }

        void exit(Thread thread, Node node) {
            final Node top = nodeArray[abs(thread.hashCode()) % THREAD_LOCAL_ARRAY_LENGTH];
            try {
                while (!top.lock.tryLock()) ;
                delete(node);
            } finally {
                top.lock.unlock();
            }
        }

        public static class Node {
            private final Thread thread;
            private final ReentrantLock lock;
            private Node pre;
            private Node next;

//            Node() {
//                this(null);
//            }

            Node(final Thread thread) {
                this(thread, null);
            }

            Node(final Thread thread, final ReentrantLock lock) {
                this.thread = thread;
                this.lock = lock;
            }

        }

    }

    public static void initHandler(ImplantHandler handler) {
        implantHandler = handler;
    }
}
