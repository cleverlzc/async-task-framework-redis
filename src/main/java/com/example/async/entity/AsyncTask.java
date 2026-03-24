package com.example.async.entity;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.example.async.enums.TaskStatus;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 异步任务实体类
 * <p>
 * 对应数据库表：async_task
 * 用于存储异步任务的基本信息和执行状态
 * </p>
 *
 * @author RelayAgent
 * @since 1.0.0
 */
@Data
public class AsyncTask {

    /**
     * 任务ID（主键）
     */
    private Long id;

    /**
     * 任务类型
     * <p>
     * 用于标识不同类型的任务，如 "EMAIL", "SMS", "DATA_SYNC" 等
     * 与 TaskHandler 的 getTaskType() 对应
     * </p>
     */
    private String taskType;

    /**
     * 业务键
     * <p>
     * 用于业务层面的幂等性控制，确保相同业务不会重复提交任务
     * 例如：订单号、用户ID等
     * </p>
     */
    private String businessKey;

    /**
     * 任务载荷（JSON格式）
     * <p>
     * 存储任务执行所需的参数，使用 JSON 类型灵活支持不同业务场景
     * 示例：{"to": "user@example.com", "subject": "Hello", "body": "Content"}
     * </p>
     */
    private JSONObject payload;

    /**
     * 任务优先级
     * <p>
     * 数值越小优先级越高，范围：0-10
     * 0: 最高优先级（紧急任务）
     * 5: 普通优先级（默认值）
     * 10: 最低优先级（非紧急任务）
     * </p>
     */
    private Integer priority;

    /**
     * 任务状态
     * <p>
     * 0: PENDING - 待处理
     * 1: PROCESSING - 处理中
     * 2: SUCCESS - 成功
     * 3: FAILED - 失败（可重试）
     * 4: CANCELLED - 已取消
     * 5: TIMEOUT - 超时
     * </p>
     */
    private Integer status;

    /**
     * 当前重试次数
     */
    private Integer retryCount;

    /**
     * 最大重试次数
     * <p>
     * 任务失败后的最大重试次数，默认值为 3
     * 超过此次数后任务状态将不再变为 FAILED
     * </p>
     */
    private Integer maxRetry;

    /**
     * 执行节点标识
     * <p>
     * 标识执行该任务的服务器节点，用于分布式环境下的任务追踪
     * 格式：hostname:port 或自定义节点ID
     * </p>
     */
    private String executeNode;

    /**
     * 执行开始时间
     */
    private LocalDateTime executeStartTime;

    /**
     * 执行结束时间
     */
    private LocalDateTime executeEndTime;

    /**
     * 延迟执行时间（毫秒）
     * <p>
     * 任务提交后需要延迟多久才能执行
     * 0 表示立即执行
     * 与 Redis 延迟队列配合使用
     * </p>
     */
    private Long delayMillis;

    /**
     * 超时时间（秒）
     * <p>
     * 任务执行的超时时间，默认值为 300 秒（5分钟）
     * 超过此时间未完成的任务将被标记为 TIMEOUT 状态
     * </p>
     */
    private Integer timeoutSeconds;

    /**
     * 乐观锁版本号
     * <p>
     * 用于并发更新控制，防止多个节点同时处理同一任务
     * 每次更新时版本号递增，更新条件中需匹配当前版本号
     * </p>
     */
    private Integer version;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    private LocalDateTime updateTime;

    /**
     * 获取任务状态枚举
     *
     * @return TaskStatus 枚举对象
     */
    public TaskStatus getTaskStatus() {
        return TaskStatus.fromCode(this.status);
    }

    /**
     * 设置任务状态
     *
     * @param taskStatus TaskStatus 枚举对象
     */
    public void setTaskStatus(TaskStatus taskStatus) {
        this.status = taskStatus.getCode();
    }

    /**
     * 获取载荷字符串
     *
     * @return JSON 字符串
     */
    public String getPayloadString() {
        return payload != null ? payload.toJSONString() : null;
    }

    /**
     * 设置载荷字符串
     *
     * @param payloadString JSON 字符串
     */
    public void setPayloadString(String payloadString) {
        this.payload = payloadString != null ? JSON.parseObject(payloadString) : null;
    }

    /**
     * 获取载荷中的指定字段值
     *
     * @param key 字段名
     * @return 字段值
     */
    public Object getPayloadValue(String key) {
        return payload != null ? payload.get(key) : null;
    }

    /**
     * 检查任务是否可以重试
     *
     * @return true-可重试，false-不可重试
     */
    public boolean isRetryable() {
        return getTaskStatus().isRetryable() && retryCount < maxRetry;
    }

    /**
     * 检查任务是否为最终状态
     *
     * @return true-最终状态，false-非最终状态
     */
    public boolean isFinalStatus() {
        return getTaskStatus().isFinal();
    }

    /**
     * 计算任务执行时长（毫秒）
     *
     * @return 执行时长，如果未开始或未结束则返回 null
     */
    public Long getDurationMillis() {
        if (executeStartTime != null && executeEndTime != null) {
            return java.time.Duration.between(executeStartTime, executeEndTime).toMillis();
        }
        return null;
    }

    /**
     * 检查任务是否超时
     *
     * @param currentTimestamp 当前时间戳（毫秒）
     * @return true-已超时，false-未超时
     */
    public boolean isTimeout(long currentTimestamp) {
        if (executeStartTime == null || timeoutSeconds == null) {
            return false;
        }
        long startTime = executeStartTime.atZone(java.time.ZoneId.systemDefault()).toInstant().toEpochMilli();
        long timeoutMillis = timeoutSeconds * 1000L;
        return currentTimestamp - startTime > timeoutMillis;
    }
}
