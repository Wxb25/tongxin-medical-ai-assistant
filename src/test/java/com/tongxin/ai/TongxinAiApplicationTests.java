package com.tongxin.ai;

import org.junit.jupiter.api.Test;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;
import java.util.Map;

@SpringBootTest
//@ActiveProfiles("test")   // 激活 src/test/resources/application-test.yaml，避免缺 API Key 导致上下文启动失败
class TongxinAiApplicationTests {

    @Autowired
    private VectorStore vectorStore;

    @Test
    void testVectorStore() {
        // 1. 创建一个文档并存入向量库
        Document doc = new Document("高血压是一种常见的慢性病，需要长期管理。", Map.of("type", "disease"));
        vectorStore.add(List.of(doc));

        // 2. 执行相似性搜索
        List<Document> results = vectorStore.similaritySearch("什么是高血压？");

        // 3. 打印搜索结果
        results.forEach(System.out::println);
    }
}
