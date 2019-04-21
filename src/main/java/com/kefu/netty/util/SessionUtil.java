package com.kefu.netty.util;

import com.kefu.netty.attribute.Attributes;
import com.kefu.netty.session.Session;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import io.netty.channel.Channel;
import io.netty.channel.group.ChannelGroup;

/**
 * Session会话工具类
 *
 * @author feng
 * @date 2019-04-21
 */
public class SessionUtil {

    /**
     * userId -> Channel 的映射
     */
    private static final Map<String, Channel> userIdChannelMap = new ConcurrentHashMap<>();

    /**
     * groupId -> ChannelGroup 的映射
     */
    private static final Map<String, ChannelGroup> groupIdChannelGroupMap = new ConcurrentHashMap<>();

    /**
     * 登录成功后缓存【用户 -> 用户连接】的映射关系
     *
     * @param session 会话信息
     * @param channel 连接
     */
    public static void bindSession(Session session, Channel channel) {
        userIdChannelMap.put(session.getUserId(), channel);
        channel.attr(Attributes.SESSION).set(session);
    }

    /**
     * 退出登录或断开连接后清除【用户 -> 用户连接】的映射关系
     *
     * @param channel 连接
     */
    public static void unBindSession(Channel channel) {
        if (hasLogin(channel)) {
            userIdChannelMap.remove(getSession(channel).getUserId());
            channel.attr(Attributes.SESSION).set(null);
        }
    }

    /**
     * 根据连接判断用户是否已经登录
     *
     * @param channel 连接
     * @return true 则表示已登录
     */
    public static boolean hasLogin(Channel channel) {
        return channel.hasAttr(Attributes.SESSION);
    }

    /**
     * 根据连接获取用户会话信息
     *
     * @param channel 连接
     * @return 用户会话信息
     */
    public static Session getSession(Channel channel) {
        return channel.attr(Attributes.SESSION).get();
    }

    /**
     * 根据用户ID获取用户连接
     *
     * @param userId 用户ID
     * @return 用户连接
     */
    public static Channel getChannel(String userId) {
        return userIdChannelMap.get(userId);
    }

    /**
     * 缓存【群组ID -> 群组内所有成员连接】的映射关系
     *
     * @param groupId      群组ID
     * @param channelGroup 群组内所有成员连接
     */
    public static void bindChannelGroup(String groupId, ChannelGroup channelGroup) {
        groupIdChannelGroupMap.put(groupId, channelGroup);
    }

    /**
     * 根据群组ID获取群组内所有成员的连接
     *
     * @param groupId 群组ID
     * @return 群组内所有成员的连接
     */
    public static ChannelGroup getChannelGroup(String groupId) {
        return groupIdChannelGroupMap.get(groupId);
    }

}
