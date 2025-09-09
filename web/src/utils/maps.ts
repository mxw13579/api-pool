// 基于 ChannelStatus.java
export const channelStatusMap: { [key: number]: { text: string, type: 'success' | 'warning' | 'danger' | 'info' } } = {
    1: { text: '启用', type: 'success' },
    2: { text: '手动禁用', type: 'warning' },
    3: { text: '自动禁用', type: 'danger' },
    0: { text: '未知', type: 'info' },
};

// 基于 ChannelType.java
export const channelTypeMap: { [key: number]: string } = {
    0: "未知类型",
    1: "OpenAI",
    2: "Midjourney",
    3: "Azure",
    4: "Ollama",
    5: "Midjourney Plus",
    6: "OpenAI Max",
    7: "OhMyGPT",
    8: "自定义",
    9: "AILS",
    10: "AIProxy",
    11: "PaLM",
    12: "API2GPT",
    13: "AIGC2D",
    14: "Anthropic",
    15: "百度",
    16: "智谱",
    17: "阿里",
    18: "讯飞",
    19: "360",
    20: "OpenRouter",
    21: "AIProxy Library",
    22: "FastGPT",
    23: "腾讯",
    24: "Gemini",
    25: "Moonshot",
    26: "智谱v4",
    27: "Perplexity",
    31: "灵犀万物",
    33: "AWS",
    34: "Cohere",
    35: "MiniMax",
    36: "SunoAPI",
    37: "Dify",
    38: "Jina",
    39: "Cloudflare",
    40: "SiliconFlow",
    41: "VertexAi",
    42: "Mistral",
    43: "DeepSeek",
    44: "MokaAI",
    45: "火山引擎",
    46: "百度V2",
    47: "Xinference",
    48: "Xai",
    49: "Coze",
    50: "Kling",
    51: "Jimeng",
    [-1]: "占位类型"
};


// 基于 ChannelType.java
export const proxyTypeMap: { [key: number]: string } = {
    0: "随机",
    1: "轮询",
};
