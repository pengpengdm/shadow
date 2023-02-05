package com.shadow.codecoverage.core.service;

import com.shadow.codecoverage.core.config.AgentConfig;
import com.shadow.codecoverage.databuffer.DataBuffer;
import com.shadow.codecoverage.databuffer.consumer.DefaultConsumer;
import com.shadow.codecoverage.protoc.report.TraceCoverData;
import org.apache.rocketmq.client.producer.DefaultMQProducer;
import org.apache.rocketmq.client.producer.SendResult;
import org.apache.rocketmq.client.producer.SendStatus;
import org.apache.rocketmq.common.message.Message;
import org.kohsuke.MetaInfServices;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * @Classname ReportAtmCoverDataService
 * @Description TODO
 * @Date 2023/1/14 12:49
 * @Created by pepsi
 */
@MetaInfServices(BootService.class)
public class ReportAtmCoverDataService extends DefaultConsumer<TraceCoverData> implements BootService {

    private Logger logger = LoggerFactory.getLogger(ReportAtmCoverDataService.class);

    private DataBuffer<TraceCoverData> dataBuffer;

    private DefaultMQProducer producer;

    public void produce(TraceCoverData traceVal) {
        if (dataBuffer != null) {
            dataBuffer.produce(traceVal);
        }
    }

    @Override
    public void prepare() throws Throwable {
        producer = new DefaultMQProducer(AgentConfig.REPORT_ATM_COVERDATA_TOPIC);
        producer.setInstanceName("REPORT_ATM_COVERDATA_INSTANCE");
        producer.setNamesrvAddr(AgentConfig.ROCKETMQ_NAME_SRV_ADDR);
        producer.setVipChannelEnabled(false);
        producer.setRetryTimesWhenSendFailed(3);
        producer.setSendMsgTimeout(1000);
        try {
            producer.start();
            //日志
        } catch (Exception e) {
            //日志
            throw new Exception("fail to start atm producer");
        }
    }

    @Override
    public void boot() throws Throwable {
        try {
            dataBuffer = new DataBuffer<TraceCoverData>(2, 100);
            dataBuffer.consume(this, 1);
        } catch (Exception e) {
            //
        }
    }

    @Override
    public void complete() throws Throwable {

    }

    @Override
    public void shutdown() throws Throwable {
        if (producer != null) {
            producer.shutdown();
        }
        if (dataBuffer != null) {
            dataBuffer.shutdownConsumers();
        }
    }

    @Override
    public void consumer(List<TraceCoverData> data) {
        for (TraceCoverData traceVal : data) {
            Message message = new Message(AgentConfig.REPORT_ATM_COVERDATA_TOPIC, "ATM_TAG", traceVal.getTraceId(), traceVal.toByteArray());
            try {
                SendResult result = producer.send(message);
                if (SendStatus.SEND_OK.equals(result.getSendStatus())) {
                    //retry
                }
            } catch (Exception e) {
                //retry
            }
        }
    }
}
