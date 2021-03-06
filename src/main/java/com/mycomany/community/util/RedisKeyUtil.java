package com.mycomany.community.util;

public class RedisKeyUtil {

    private static final String SPLIT=":";
    private static String PREFIX_ENTITY_LIKE="like:entity";
    private static final String PREFIX_USER_LIKE ="like:user";
    private static final String PREFIX_FOLLOWEE ="followee";
    private static final String PREFIX_FOLLOWER ="follower";
    private static final String PREFIX_KAPTCHA ="kaptcha";
    private static final String PREFIX_TICKET ="ticket";
    private static final String PREFIX_USER ="user";

    private static final String PREFIX_UV= "uv";
    private static final String PREFIX_DAU= "dau";
    private static final String PREFIX_POST= "post";
    private static final String PREFIX_CODE="code";


    //like: entity:entityType:entityId ->set(userId)
    public static String getEntityLikeKey(int entityType,int entityId){
        return PREFIX_ENTITY_LIKE + SPLIT + entityType +SPLIT + entityId;
    }

    //like: user:userId -> int
    public static String getUserLikeKey(int userId){
        return PREFIX_USER_LIKE + SPLIT + userId;
    }

    //followee: userId:entityType -> zset(entityId,new Date)
    public static String getFolloweeKey(int userId,int entityType){
        return PREFIX_FOLLOWEE + SPLIT + userId + SPLIT + entityType;
    }

    // follower: entityType:entityId -> zset(userId, new Date)
    public static String getFollowerKey(int entityType, int entityId){
        return PREFIX_FOLLOWER + SPLIT + entityType + SPLIT + entityId;
    }

    // code verification
    public static String getKaptchaKey(String owner){
        return PREFIX_KAPTCHA + SPLIT + owner;
    }

    // login verification
    public static String getTicketKey(String ticket){
        return PREFIX_TICKET + SPLIT + ticket;
    }
    // user
    public static String getUserKey(int userId){
        return PREFIX_USER + SPLIT + userId;
    }

    // daily UV
    public static String getUVKey(String date){
        return PREFIX_UV + SPLIT + date;
    }

    public static String getUVKey(String startDate, String endDate){
        return  PREFIX_UV + SPLIT + startDate + endDate;
    }

    // daily active user.
    public static String getDAUKey(String date){
        return PREFIX_DAU + SPLIT + date;
    }

    public static String getDAUKey(String startDate, String endDate){
        return  PREFIX_DAU + SPLIT + startDate + endDate;
    }

    // count score of the discuss post
    public static String getPostScoreKey(){
        return PREFIX_POST + SPLIT + "score";
    }

    public static String getCodeKey(int userId){
        return PREFIX_CODE + SPLIT + userId;
    }


}
