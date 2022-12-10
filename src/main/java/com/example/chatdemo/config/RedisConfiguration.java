package com.example.chatdemo.config;

import com.example.chatdemo.socket.SocketServer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.listener.ChannelTopic;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;
import org.springframework.data.redis.listener.adapter.MessageListenerAdapter;
import org.springframework.data.redis.serializer.StringRedisSerializer;

/**
 * @author yy
 * @data 2022/12/9 22:56
 */
@Configuration
public class RedisConfiguration {


    @Bean
    public StringRedisTemplate redisTemplate(RedisConnectionFactory redisConnectionFactory) {
        StringRedisTemplate template = new StringRedisTemplate();
        template.setConnectionFactory(redisConnectionFactory);
        template.setEnableTransactionSupport(true); //打开事务支持
        return template;
    }


    @Bean
    public MessageListenerAdapter messageListenerAdapter(SocketServer socketServer) {
        MessageListenerAdapter listener = new MessageListenerAdapter(socketServer, "onMessage");
        listener.setSerializer(new StringRedisSerializer());
        return listener;
    }

    @Bean
    public RedisMessageListenerContainer initRedisContainer(@Autowired RedisConnectionFactory redisConnectionFactory, @Autowired MessageListenerAdapter adapter) {

        RedisMessageListenerContainer container = new RedisMessageListenerContainer();
        container.addMessageListener(adapter, new ChannelTopic("chat"));
        container.setConnectionFactory(redisConnectionFactory);
        return container;
    }

}

