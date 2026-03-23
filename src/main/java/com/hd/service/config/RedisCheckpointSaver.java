//package com.hd.service.config;
//
//import com.fasterxml.jackson.core.JsonProcessingException;
//import com.fasterxml.jackson.databind.ObjectMapper;
//import org.bsc.langgraph4j.RunnableConfig;
//import org.bsc.langgraph4j.checkpoint.BaseCheckpointSaver;
//import org.bsc.langgraph4j.checkpoint.Checkpoint;
//import org.springframework.data.redis.core.StringRedisTemplate;
//
//import java.io.IOException;
//import java.util.Collection;
//import java.util.Optional;
//
///**
// * Redis 实现的 CheckpointSaver
// * 用于持久化 LangGraph4j 状态快照
// */
//public class RedisCheckpointSaver implements BaseCheckpointSaver {
//
//    private static final String KEY_PREFIX = "langgraph:checkpoint:";
//
//    private final StringRedisTemplate redisTemplate;
//    private final ObjectMapper objectMapper;
//
//    public RedisCheckpointSaver(StringRedisTemplate redisTemplate) {
//        this.redisTemplate = redisTemplate;
//        this.objectMapper = new ObjectMapper();
//    }
//
//    @Override
//    public Collection<Checkpoint> list(RunnableConfig config) {
//        // 可扩展：使用 Redis SCAN 获取该 threadId 下所有 checkpoint
//        return java.util.Collections.emptyList();
//    }
//
//    @Override
//    public Optional<Checkpoint> get(RunnableConfig config) {
//        String key = buildKey(config);
//        String json = redisTemplate.opsForValue().get(key);
//        if (json == null) {
//            return Optional.empty();
//        }
//        try {
//            Checkpoint checkpoint = objectMapper.readValue(json, Checkpoint.class);
//            return Optional.of(checkpoint);
//        } catch (IOException e) {
//            throw new RuntimeException("Failed to deserialize checkpoint", e);
//        }
//    }
//
//    @Override
//    public RunnableConfig put(RunnableConfig config, Checkpoint checkpoint) {
//        String key = buildKey(config);
//        try {
//            String json = objectMapper.writeValueAsString(checkpoint);
//            redisTemplate.opsForValue().set(key, json);
//        } catch (JsonProcessingException e) {
//            throw new RuntimeException("Failed to serialize checkpoint", e);
//        }
//        return config;
//    }
//
//    @Override
//    public boolean release(RunnableConfig config) {
//        String key = buildKey(config);
//        return Boolean.TRUE.equals(redisTemplate.delete(key));
//    }
//
//    private String buildKey(RunnableConfig config) {
//        return KEY_PREFIX + config.threadId().orElse("default");
//    }
//}
