package com.tongxin.ai.mapper;

import com.tongxin.ai.entity.po.AiVectorStore;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * <p>
 *  Mapper 接口
 * </p>
 * ai_vector_store 表由 Spring AI PgVectorStore 自动维护，本 Mapper 仅补充
 * 按业务 docId（写入向量时塞入 metadata）批量删除/查询 chunk 的原生 SQL，
 * 用于知识库删除接口清理对应文档的所有向量分块。
 *
 * @author wyq
 * @since 2026-09-02
 */
public interface AiVectorStoreMapper extends BaseMapper<AiVectorStore> {

    /**
     * 查询指定业务 docId 对应的所有向量分块 ID
     */
    @Select("SELECT id FROM ai_vector_store WHERE metadata->>'docId' = #{docId}")
    List<String> selectChunkIdsByDocId(@Param("docId") String docId);

    /**
     * 删除指定业务 docId 对应的所有向量分块
     */
    @Delete("DELETE FROM ai_vector_store WHERE metadata->>'docId' = #{docId}")
    int deleteByDocId(@Param("docId") String docId);
}
