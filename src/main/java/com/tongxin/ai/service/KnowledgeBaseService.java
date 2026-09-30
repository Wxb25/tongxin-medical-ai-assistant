package com.tongxin.ai.service;

import com.tongxin.ai.common.BusinessException;
import com.tongxin.ai.mapper.AiVectorStoreMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.document.Document;
import org.springframework.ai.transformer.splitter.TokenTextSplitter;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 知识库向量服务
 * 封装 Spring AI 的 VectorStore 写入/检索/删除操作。
 * - 文档切分：使用 TokenTextSplitter 按切分长文本为 chunk
 * - metadata：每个 chunk 都带上业务 docId，便于按 docId 删除
 * - 删除：通过 AiVectorStoreMapper 的原生 SQL 按 metadata->>'docId' 批量清理 chunk
 *
 * @author wyq
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class KnowledgeBaseService {

    private final VectorStore vectorStore;
    private final AiVectorStoreMapper aiVectorStoreMapper;
    private final TokenTextSplitter tokenTextSplitter = new TokenTextSplitter();

    /**
     * 将文档内容切分并向量化写入 VectorStore
     *
     * @param docId    业务文档唯一标识（与 knowledge_docs.doc_id 一致）
     * @param content  文档全文
     * @param title    文档标题
     * @param category 分类
     * @param source   来源
     */
    public void ingestDocument(String docId, String content, String title, String category, String source) {
        if (content == null || content.isBlank()) {
            throw new BusinessException("文档内容为空，无法向量化");
        }

        // 1. 构造 metadata（每个 chunk 都带上 docId，后续按 docId 删除）
        Map<String, Object> metadata = new HashMap<>();
        metadata.put("docId", docId);
        metadata.put("title", title);
        metadata.put("category", category);
        if (source != null) {
            metadata.put("source", source);
        }

        // 2. 构造 Spring AI Document，按 token 切分
        Document doc = new Document(content, metadata);
        List<Document> chunks = tokenTextSplitter.split(List.of(doc));

        // 3. 写入 VectorStore（PgVectorStore 会自动调用 EmbeddingModel 计算向量并落库）
        vectorStore.add(chunks);
        log.info("知识库向量写入完成: docId={}, chunks={}", docId, chunks.size());
    }

    /**
     * 按 docId 删除该文档对应的所有向量分块
     * Spring AI VectorStore 的 delete(Filter) 是 default 实现，可能抛 UnsupportedOperationException，
     * 这里直接走原生 SQL 删 ai_vector_store 表，最稳妥。
     */
    public void deleteByDocId(String docId) {
        int deleted = aiVectorStoreMapper.deleteByDocId(docId);
        log.info("知识库向量清理完成: docId={}, deletedChunks={}", docId, deleted);
    }

    /**
     * 相似度检索：根据用户问题检索知识库 top-K 相关文档片段
     * 用于 RAG，把检索到的 chunk content 拼进 ChatClient 的 prompt 上下文
     *
     * @param query 用户问题
     * @param topK  返回条数
     * @return 文档片段文本列表（已按相关性降序）
     */
    public List<String> searchRelevant(String query, int topK) {
        if (query == null || query.isBlank()) {
            return List.of();
        }
        try {
            SearchRequest request = SearchRequest.builder()
                    .query(query)
                    .topK(topK)
                    .similarityThreshold(0.5)
                    .build();
            List<Document> docs = vectorStore.similaritySearch(request);
            if (docs == null || docs.isEmpty()) {
                return List.of();
            }
            return docs.stream()
                    .map(Document::getText)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            log.warn("向量相似度检索失败，将退化为无 RAG 模式: query={}", query, e);
            return List.of();
        }
    }
}
