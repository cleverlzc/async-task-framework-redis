package com.example.async.dto;

import com.alibaba.fastjson2.JSONObject;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.io.Serializable;

/**
 * 异步任务提交请求DTO
 * 
 * 用于接收客户端提交的任务请求，包含任务类型、业务标识、业务参数等信息
 * 
 * @author RelayAgent
 * @since 1.0.0
 */
@Data
public class TaskSubmitRequest implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    /**
     * 任务类型
     * 必填，用于标识任务的业务类型，对应TaskHandler的getTaskType()
     * 
     * 示例：email, sms, report, data_sync
     */
    @NotBlank(message = "任务类型不能为空")
    private String taskType;
    
    /**
     * 业务标识
     * 可选，用于实现任务幂等性
     * 相同taskType + businessKey的任务只会执行一次
     * 
     * 示例：order:12345, user:67890
     */
    private String businessKey;
    
    /**
     * 业务参数
     * 必填，JSON格式的任务执行参数
     * 
     * 示例：{"to":"test@example.com","subject":"Test Email","content":"Hello"}
     */
    @NotNull(message = "业务参数不能为空")
    private JSONObject payload;
    
    /**
     * 延迟执行时间（毫秒）
     * 可选，默认为0（立即执行）
     * 
     * 示例：
     * - 0: 立即执行
     * - 5000: 5秒后执行
     * - 60000: 1分钟后执行
     */
    private Long delayMillis;
    
    /**
     * 超时时间（秒）
     * 可选，默认由TaskHandler的getTimeout()决定
     * 
     * 示例：
     * - 300: 5分钟超时
     * - 600: 10分钟超时
     */
    private Integer timeoutSeconds;
    
    /**
     * 最大重试次数
     * 可选，默认由TaskHandler的getMaxRetry()决定
     * 
     * 示例：
     * - 0: 不重试
     * - 3: 最多重试3次
     * - 5: 最多重试5次
     */
    private Integer maxRetry;
    
    /**
     * 任务优先级
     * 可选，默认为5（中等优先级）
     * 数字越小优先级越高，范围：1-10
     * 
     * 示例：
     * - 1: 最高优先级
     * - 5: 中等优先级
     * - 10: 最低优先级
     */
    private Integer priority;
    
    /**
     * 任务描述
     * 可选，用于记录任务的业务含义，方便查询和排查
     * 
     * 示例："发送订单确认邮件给用户"
     */
    private String description;
    
    /**
     * 获取延迟执行时间
     * 如果未设置，返回0（立即执行）
     * 
     * @return 延迟执行时间（毫秒）
     */
    public Long getDelayMillis() {
        return delayMillis != null ? delayMillis : 0L;
    }
    
    /**
     * 获取任务优先级
     * 如果未设置，返回5（中等优先级）
     * 
     * @return 任务优先级（1-10，数字越小优先级越高）
     */
    public Integer getPriority() {
        return priority != null ? priority : 5;
    }
    
    /**
     * 验证优先级范围
     * 
     * @return true if priority is valid (1-10)
     */
    public boolean isValidPriority() {
        return priority == null || (priority >= 1 && priority <= 10);
    }
}