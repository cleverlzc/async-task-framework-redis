package com.example.async.dto;

import com.alibaba.fastjson2.JSONObject;
import com.example.async.enums.TaskStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 任务查询响应DTO
 * 用于返回任务查询结果给客户端
 * 
 * @author AsyncTask Framework
 * @since 1.0.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TaskQueryResponse implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 任务ID
     */
    private Long id;

    /**
     * 任务类型（用于路由到对应的处理器）
     */
    private String taskType;

    /**
     * 业务键（用于幂等性控制）
     */
    private String businessKey;

    /**
     * 业务参数（JSON格式）
     */
    private JSONObject payload;

    /**
     * 任务优先级（1-10，数字越小优先级越高）
     */
    private Integer priority;

    /**
     * 任务状态
     * 0: PENDING 待处理
     * 1: PROCESSING 处理中
     * 2: SUCCESS 成功
     * 3: FAILED 失败
     * 4: CANCELLED 已取消
     * 5: TIMEOUT 超时
     */
    private Integer status;

    /**
     * 任务状态描述
     */
    private String statusDesc;

    /**
     * 重试次数
     */
    private Integer retryCount;

    /**
     * 最大重试次数
     */
    private Integer maxRetry;

    /**
     * 延迟执行时间（毫秒）
     */
    private Long delayMillis;

    /**
     * 执行超时时间（秒）
     */
    private Integer timeoutSeconds;

    /**
     * 执行节点
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
     * 执行耗时（毫秒）
     */
    private Long duration;

    /**
     * 错误信息
     */
    private String errorMsg;

    /**
     * 任务描述
     */
    private String description;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    private LocalDateTime updateTime;

    /**
     * 版本号（乐观锁）
     */
    private Integer version;

    /**
     * 是否可重试
     * 判断条件：状态为失败 且 重试次数小于最大重试次数
     */
    public boolean isRetryable() {
        if (status == null || retryCount == null || maxRetry == null) {
            return false;
        }
        return TaskStatus.fromCode(status).isRetryable() && retryCount < maxRetry;
    }

    /**
     * 是否为最终状态
     * 判断条件：状态为成功、取消或超时
     */
    public boolean isFinalStatus() {
        if (status == null) {
            return false;
        }
        return TaskStatus.fromCode(status).isFinal();
    }

    /**
     * 是否正在执行
     */
    public boolean isProcessing() {
        return TaskStatus.PROCESSING.getCode().equals(status);
    }

    /**
     * 是否执行成功
     */
    public boolean isSuccess() {
        return TaskStatus.SUCCESS.getCode().equals(status);
    }

    /**
     * 是否执行失败
     */
    public boolean isFailed() {
        return TaskStatus.FAILED.getCode().equals(status);
    }

    /**
     * 是否已取消
     */
    public boolean isCancelled() {
        return TaskStatus.CANCELLED.getCode().equals(status);
    }

    /**
     * 是否超时
     */
    public boolean isTimeout() {
        return TaskStatus.TIMEOUT.getCode().equals(status);
    }

    /**
     * 是否待处理
     */
    public boolean isPending() {
        return TaskStatus.PENDING.getCode().equals(status);
    }

    /**
     * 获取任务状态枚举
     */
    public TaskStatus getTaskStatus() {
        if (status == null) {
            return null;
        }
        return TaskStatus.fromCode(status);
    }

    /**
     * 设置任务状态
     */
    public void setTaskStatus(TaskStatus taskStatus) {
        if (taskStatus != null) {
            this.status = taskStatus.getCode();
            this.statusDesc = taskStatus.getDescription();
        }
    }

    /**
     * 构建任务查询响应（从实体转换）
     * 
     * @param id 任务ID
     * @param taskType 任务类型
     * @param businessKey 业务键
     * @param payload 业务参数
     * @param priority 优先级
     * @param status 状态
     * @param retryCount 重试次数
     * @param maxRetry 最大重试次数
     * @param delayMillis 延迟时间
     * @param timeoutSeconds 超时时间
     * @param executeNode 执行节点
     * @param executeStartTime 开始时间
     * @param executeEndTime 结束时间
     * @param errorMsg 错误信息
     * @param description 描述
     * @param createTime 创建时间
     * @param updateTime 更新时间
     * @param version 版本号
     * @return TaskQueryResponse
     */
    public static TaskQueryResponse build(
            Long id, String taskType, String businessKey, JSONObject payload,
            Integer priority, Integer status, Integer retryCount, Integer maxRetry,
            Long delayMillis, Integer timeoutSeconds, String executeNode,
            LocalDateTime executeStartTime, LocalDateTime executeEndTime,
            String errorMsg, String description, LocalDateTime createTime,
            LocalDateTime updateTime, Integer version) {

        TaskStatus taskStatus = TaskStatus.fromCode(status);
        Long duration = null;
        if (executeStartTime != null && executeEndTime != null) {
            duration = java.time.Duration.between(executeStartTime, executeEndTime).toMillis();
        }

        return TaskQueryResponse.builder()
                .id(id)
                .taskType(taskType)
                .businessKey(businessKey)
                .payload(payload)
                .priority(priority)
                .status(status)
                .statusDesc(taskStatus.getDescription())
                .retryCount(retryCount)
                .maxRetry(maxRetry)
                .delayMillis(delayMillis)
                .timeoutSeconds(timeoutSeconds)
                .executeNode(executeNode)
                .executeStartTime(executeStartTime)
                .executeEndTime(executeEndTime)
                .duration(duration)
                .errorMsg(errorMsg)
                .description(description)
                .createTime(createTime)
                .updateTime(updateTime)
                .version(version)
                .build();
    }
}
