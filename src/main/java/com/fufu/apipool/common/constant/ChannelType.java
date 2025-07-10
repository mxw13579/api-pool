package com.fufu.apipool.common.constant;


import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;

/**
 * 通道类型枚举
 * @author lizelin
 */

@Getter
public enum ChannelType {
    /**
     * 未知类型
     */
    UNKNOWN(0, "unknown", "未知类型"),
    /**
     * OpenAI
     */
    OPENAI(1, "openai", "OpenAI"),
    /**
     * Midjourney
     */
    MIDJOURNEY(2, "midjourney", "Midjourney"),
    /**
     * Azure
     */
    AZURE(3, "azure", "Azure"),
    /**
     * Ollama
     */
    OLLAMA(4, "ollama", "Ollama"),
    /**
     * Midjourney Plus
     */
    MIDJOURNEY_PLUS(5, "midjourney_plus", "Midjourney Plus"),
    /**
     * OpenAI Max
     */
    OPENAI_MAX(6, "openai_max", "OpenAI Max"),
    /**
     * OhMyGPT
     */
    OHMYGPT(7, "ohmygpt", "OhMyGPT"),
    /**
     * 自定义
     */
    CUSTOM(8, "custom", "自定义"),
    /**
     * AILS
     */
    AILS(9, "ails", "AILS"),
    /**
     * AIProxy
     */
    AIPROXY(10, "aiproxy", "AIProxy"),
    /**
     * PaLM
     */
    PALM(11, "palm", "PaLM"),
    /**
     * API2GPT
     */
    API2GPT(12, "api2gpt", "API2GPT"),
    /**
     * AIGC2D
     */
    AIGC2D(13, "aigc2d", "AIGC2D"),
    /**
     * Anthropic
     */
    ANTHROPIC(14, "anthropic", "Anthropic"),
    /**
     * 百度
     */
    BAIDU(15, "baidu", "百度"),
    /**
     * 智谱
     */
    ZHIPU(16, "zhipu", "智谱"),
    /**
     * 阿里
     */
    ALI(17, "ali", "阿里"),
    /**
     * 讯飞
     */
    XUNFEI(18, "xunfei", "讯飞"),
    /**
     * 360
     */
    _360(19, "360", "360"),
    /**
     * OpenRouter
     */
    OPENROUTER(20, "openrouter", "OpenRouter"),
    /**
     * AIProxy Library
     */
    AIPROXY_LIBRARY(21, "aiproxy_library", "AIProxy Library"),
    /**
     * FastGPT
     */
    FASTGPT(22, "fastgpt", "FastGPT"),
    /**
     * 腾讯
     */
    TENCENT(23, "tencent", "腾讯"),
    /**
     * Gemini
     */
    GEMINI(24, "gemini", "Gemini"),
    /**
     * Moonshot
     */
    MOONSHOT(25, "moonshot", "Moonshot"),
    /**
     * 智谱v4
     */
    ZHIPU_V4(26, "zhipu_v4", "智谱v4"),
    /**
     * Perplexity
     */
    PERPLEXITY(27, "perplexity", "Perplexity"),
    /**
     * 灵犀万物
     */
    LINGYIWANWU(31, "lingyiwanwu", "灵犀万物"),
    /**
     * AWS
     */
    AWS(33, "aws", "AWS"),
    /**
     * Cohere
     */
    COHERE(34, "cohere", "Cohere"),
    /**
     * MiniMax
     */
    MINIMAX(35, "minimax", "MiniMax"),
    /**
     * SunoAPI
     */
    SUNO_API(36, "suno_api", "SunoAPI"),
    /**
     * Dify
     */
    DIFY(37, "dify", "Dify"),
    /**
     * Jina
     */
    JINA(38, "jina", "Jina"),
    /**
     * Cloudflare
     */
    CLOUDFLARE(39, "cloudflare", "Cloudflare"),
    /**
     * SiliconFlow
     */
    SILICON_FLOW(40, "silicon_flow", "SiliconFlow"),
    /**
     * VertexAi
     */
    VERTEX_AI(41, "vertex_ai", "VertexAi"),
    /**
     * Mistral
     */
    MISTRAL(42, "mistral", "Mistral"),
    /**
     * DeepSeek
     */
    DEEPSEEK(43, "deepseek", "DeepSeek"),
    /**
     * MokaAI
     */
    MOKA_AI(44, "moka_ai", "MokaAI"),
    /**
     * 火山引擎
     */
    VOLC_ENGINE(45, "volc_engine", "火山引擎"),
    /**
     * 百度V2
     */
    BAIDU_V2(46, "baidu_v2", "百度V2"),
    /**
     * Xinference
     */
    XINFERENCE(47, "xinference", "Xinference"),
    /**
     * Xai
     */
    XAI(48, "xai", "Xai"),
    /**
     * Coze
     */
    COZE(49, "coze", "Coze"),
    /**
     * Kling
     */
    KLING(50, "kling", "Kling"),
    /**
     * Jimeng
     */
    JIMENG(51, "jimeng", "Jimeng"),
    /**
     * Dummy
     */
    DUMMY(-1, "dummy", "占位类型");

    /**
     * 类型编码
     */
    private final int code;
    /**
     * 英文标识
     */
    private final String identifier;
    /**
     * 中文说明
     */
    private final String description;

    ChannelType(int code, String identifier, String description) {
        this.code = code;
        this.identifier = identifier;
        this.description = description;
    }

    @JsonValue
    public int getCode() {
        return code;
    }
    /**
     * 根据code获取枚举
     */
    @JsonCreator
    public static ChannelType fromCode(int code) {
        for (ChannelType type : ChannelType.values()) {
            if (type.code == code) {
                return type;
            }
        }
        return UNKNOWN;
    }
}
