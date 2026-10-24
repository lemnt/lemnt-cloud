package com.lemnt.common.kafka.constant;

import cn.hutool.core.text.StrPool;

/**
 * MQ常量
 *
 * @author wangxx
 * @version 1.0
 * @date 2025-11-03 11:50
 */
public interface KafkaConstants {

    /**
     * topic 后缀
     */
    String TOPIC_SUFFIX = StrPool.DASHED + "topic";

    /**
     * group 后缀
     */
    String GROUP_SUFFIX = StrPool.DASHED + "consumer" + StrPool.DASHED + "group";

    /**
     * tag 后缀
     */
    String TAG_SUFFIX = StrPool.DASHED + "tag";

    interface Service {
        String ORDER = "order";
        String SYSTEM = "system";
        String ACCOUNT = "account";
        String LEMNT = "lemnt";
    }

    /**
     * 业务前缀
     */
    interface Prefix {
        String ORDER = Service.ORDER + StrPool.DASHED;
        String SYSTEM = Service.SYSTEM + StrPool.DASHED;
        String ACCOUNT = Service.ACCOUNT + StrPool.DASHED;
        String LEMNT = Service.LEMNT + StrPool.DASHED;
    }

    /**
     * Topic 常量
     */
    interface Topic {
        String ADX_BID_REQUEST = Prefix.LEMNT + "adxbidrequest" + TOPIC_SUFFIX;
        String ADX_BID_RESPONSE = Prefix.LEMNT + "adxbidresponse" + TOPIC_SUFFIX;
        String RTB_CLICK_CALLBACK = Prefix.LEMNT + "rtbclickcallback" + TOPIC_SUFFIX;
        String RTB_IMPRESSION_CALLBACK = Prefix.LEMNT + "rtbimpressioncallback" + TOPIC_SUFFIX;
        String TRACK_EVENT_LOG = Prefix.LEMNT + "trackeventlog" + TOPIC_SUFFIX;
    }

    /**
     * 消费组常量
     */
    interface Group {
        String ADX_BID_REQUEST = Prefix.LEMNT + "adxbidrequest" + GROUP_SUFFIX;
        String ADX_BID_RESPONSE = Prefix.LEMNT + "adxbidresponse" + GROUP_SUFFIX;
    }

    /**
     * Tag 常量
     */
    interface Tag {
        String ORDER = Service.ORDER + TAG_SUFFIX;
        String SYSTEM = Service.SYSTEM + TAG_SUFFIX;
        String ACCOUNT = Service.ACCOUNT + TAG_SUFFIX;
        String LEMNT = Service.LEMNT + TAG_SUFFIX;
        String DEEP_ZERO_TRACK_EVENT = Service.LEMNT + "trackevent" + StrPool.DASHED + "deepzero" + TAG_SUFFIX;
        String KUAI_SHOU_TRACK_EVENT = Service.LEMNT + "trackevent" + StrPool.DASHED + "kuaishou" + TAG_SUFFIX;
    }
}
