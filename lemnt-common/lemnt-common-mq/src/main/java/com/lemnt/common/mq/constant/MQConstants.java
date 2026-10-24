package com.lemnt.common.mq.constant;

import cn.hutool.core.text.StrPool;

/**
 * MQ常量
 *
 * @author wangxx
 * @version 1.0
 * @date 2025-11-03 11:50
 */
public interface MQConstants {

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
        String APPLY = Prefix.LEMNT + "apply" + TOPIC_SUFFIX;
        String REPORT = Prefix.LEMNT + "report" + TOPIC_SUFFIX;
        String PRICE = Prefix.LEMNT + "price" + TOPIC_SUFFIX;
        String BID = Prefix.LEMNT + "bid" + TOPIC_SUFFIX;
        String EXCEL_EXPORT_TASK = Prefix.SYSTEM + "excelexporttask" + TOPIC_SUFFIX;
        String EXCEL_IMPORT_TASK = Prefix.SYSTEM + "excelimporttask" + TOPIC_SUFFIX;
        String OPER_LOG_EVENT = Prefix.SYSTEM + "operlogevent" + TOPIC_SUFFIX;
        String TRACK_EVENT = Prefix.SYSTEM + "trackevent" + TOPIC_SUFFIX;
        String ADX_BID_REQUEST = Prefix.LEMNT + "adxbidrequest" + TAG_SUFFIX;
        String ADX_BID_RESPONSE = Prefix.LEMNT + "adxbidresponse" + TAG_SUFFIX;
        String RTB_CLICK_CALLBACK = Prefix.LEMNT + "rtbclickcallback" + TOPIC_SUFFIX;
        String RTB_IMPRESSION_CALLBACK = Prefix.LEMNT + "rtbimpressioncallback" + TOPIC_SUFFIX;
        String ORDER_BUDGET_EXPIRE = Prefix.LEMNT + "orderbudgetexpire" + TOPIC_SUFFIX;
        String CREATIVE_RECALL_EVENT = Prefix.ORDER + "creativerecallevent" + TOPIC_SUFFIX;
        String AIMEDIA_SYNC_EVENT = Prefix.ORDER + "aimedia" + TOPIC_SUFFIX;
        String AIUSERFILE_SYNC_EVENT = Prefix.ORDER + "aiuserfile" + TOPIC_SUFFIX;
    }

    /**
     * 消费组常量
     */
    interface Group {
        String APPLY = Prefix.LEMNT + "apply" + GROUP_SUFFIX;
        String REPORT = Prefix.LEMNT + "report" + GROUP_SUFFIX;
        String PRICE = Prefix.LEMNT + "price" + GROUP_SUFFIX;
        String BID = Prefix.LEMNT + "bid" + GROUP_SUFFIX;
        String SYSTEM_EXCEL_EXPORT_TASK = Prefix.SYSTEM + "excelexporttask" + GROUP_SUFFIX;
        String SYSTEM_EXCEL_IMPORT_TASK = Prefix.SYSTEM + "excelimporttask" + GROUP_SUFFIX;
        String SYSTEM_OPER_LOG_EVENT = Prefix.SYSTEM + "operlogevent" + GROUP_SUFFIX;
        String TRACK_EVENT = Prefix.LEMNT + "trackevent" + GROUP_SUFFIX;
        String ADX_BID_REQUEST = Prefix.LEMNT + "adxbidrequest" + GROUP_SUFFIX;
        String ADX_BID_RESPONSE = Prefix.LEMNT + "adxbidresponse" + GROUP_SUFFIX;
        String RTB_CLICK_CALLBACK = Prefix.LEMNT + "rtbclickcallback" + GROUP_SUFFIX;
        String RTB_IMPRESSION_CALLBACK = Prefix.LEMNT + "rtbimpressioncallback" + GROUP_SUFFIX;
        String DEEP_ZERO_TRACK_EVENT = Service.LEMNT + "trackevent" + StrPool.DASHED + "deepzero" + GROUP_SUFFIX;
        String KUAI_SHOU_TRACK_EVENT = Service.LEMNT + "trackevent" + StrPool.DASHED + "kuaishou" + GROUP_SUFFIX;
        String ORDER_BUDGET_EXPIRE = Prefix.LEMNT + "orderbudgetexpire" + GROUP_SUFFIX;
        String CREATIVE_RECALL_EVENT = Prefix.ORDER + "creativerecallevent" + GROUP_SUFFIX;
        String AIMEDIA_SYNC_EVENT = Prefix.ORDER + "aimedia" + GROUP_SUFFIX;
        String AIUSERFILE_SYNC_EVENT = Prefix.ORDER + "aiuserfile" + TOPIC_SUFFIX;
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
