<template>
  <div class="chat-page">
    <!-- 会话列表 -->
    <div class="sessions">
      <el-button type="primary" class="new-btn" @click="createSession">
        <el-icon><Plus /></el-icon> 新建会话
      </el-button>
      <div class="session-list">
        <div
          v-for="s in sessions"
          :key="s.sessionId"
          class="session-item"
          :class="{ active: currentSessionId === s.sessionId }"
          @click="selectSession(s.sessionId)"
        >
          <el-icon :size="16"><ChatLineRound /></el-icon>
          <span class="title">{{ s.title }}</span>
          <el-icon class="del" @click.stop="deleteSession(s.sessionId)"><Delete /></el-icon>
        </div>
        <div v-if="sessions.length === 0" class="empty">暂无会话</div>
      </div>
    </div>

    <!-- 对话区 -->
    <div class="chat-area">
      <div class="chat-header" v-if="currentSessionId || draftMode">
        <div class="header-left">
          <div class="header-avatar"><el-icon :size="18"><ChatDotRound /></el-icon></div>
          <div>
            <div class="header-title">AI 医疗助手</div>
            <div class="header-sub">已启用知识库检索</div>
          </div>
        </div>
      </div>
      <div class="messages" ref="messagesRef">
        <div v-if="!currentSessionId && !draftMode" class="welcome">
          <div class="welcome-icon"><el-icon :size="36"><ChatDotRound /></el-icon></div>
          <h3>同心医院 AI 助手</h3>
          <p>选择或新建会话开始对话</p>
        </div>
        <div v-for="msg in messages" :key="msg.id" class="msg" :class="msg.role">
          <el-avatar :size="32" :icon="msg.role === 'user' ? User : ChatDotRound" :class="msg.role === 'user' ? 'user-avatar' : 'ai-avatar'" />
          <div class="bubble">
            <div class="content">{{ msg.content }}</div>
          </div>
        </div>
        <div v-if="streaming" class="msg assistant">
          <el-avatar :size="32" :icon="ChatDotRound" class="ai-avatar" />
          <div class="bubble">
            <div class="content">{{ streamingContent || '思考中...' }}<span class="cursor">|</span></div>
          </div>
        </div>
      </div>
      <div class="input-bar" v-if="currentSessionId || draftMode">
        <div class="input-wrapper">
          <el-input
            v-model="input"
            type="textarea"
            :rows="2"
            placeholder="输入您的问题，Ctrl + Enter 发送"
            :disabled="streaming"
            resize="none"
            @keydown.enter.ctrl.prevent="send"
          />
        </div>
        <el-button type="primary" :loading="streaming" @click="send" class="send-btn">
          <el-icon><Promotion /></el-icon>发送
        </el-button>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, nextTick } from 'vue'
import { ElMessage } from 'element-plus'
import { chatApi } from '@/api'
import type { ChatSession, ChatMessage } from '@/types'
import { useUserStore } from '@/stores/user'
import { Plus, Delete, User, ChatDotRound, ChatLineRound, Promotion } from '@element-plus/icons-vue'

const userStore = useUserStore()
const sessions = ref<ChatSession[]>([])
const currentSessionId = ref('')
const draftMode = ref(false)
const messages = ref<ChatMessage[]>([])
const input = ref('')
const useRag = ref(true)
const streaming = ref(false)
const streamingContent = ref('')
const messagesRef = ref<HTMLElement>()

const scrollBottom = async () => {
  await nextTick()
  if (messagesRef.value) {
    messagesRef.value.scrollTop = messagesRef.value.scrollHeight
  }
}

const loadSessions = async () => {
  try {
    const res = await chatApi.mySessions({ pageNum: 1, pageSize: 50 })
    sessions.value = res.records
  } catch { /* ignore */ }
}

const createSession = () => {
  draftMode.value = true
  currentSessionId.value = ''
  messages.value = [{
    id: -1,
    sessionId: 0,
    role: 'assistant',
    content: WELCOME_CONTENT,
    createdAt: new Date().toISOString()
  }]
  scrollBottom()
}

const WELCOME_CONTENT = '你好！我是同心医院 AI 医疗助手。\n\n我可以帮你：\n• 在线问诊：描述症状，获取健康建议\n• 查询医生：了解各科室医生信息和专长\n• 预约挂号：查看医生排班并在线预约\n• 用药咨询：药品信息和使用指导\n\n请告诉我你需要什么帮助？'

const selectSession = async (sessionId: string) => {
  draftMode.value = false
  currentSessionId.value = sessionId
  messages.value = []
  try {
    const res = await chatApi.messages(sessionId, { pageNum: 1, pageSize: 100 })
    messages.value = res.records
    if (messages.value.length === 0) {
      messages.value = [{
        id: -1,
        sessionId: 0,
        role: 'assistant',
        content: WELCOME_CONTENT,
        createdAt: new Date().toISOString()
      }]
    }
    scrollBottom()
  } catch { /* ignore */ }
}

const deleteSession = async (sessionId: string) => {
  await chatApi.deleteSession(sessionId)
  ElMessage.success('已删除')
  await loadSessions()
  if (currentSessionId.value === sessionId) {
    currentSessionId.value = ''
    draftMode.value = false
    messages.value = []
  }
}

const send = async () => {
  const text = input.value.trim()
  if (!text) return
  input.value = ''
  streaming.value = true
  streamingContent.value = ''
  try {
    // 草稿模式：先发第一条消息时才真正创建会话
    if (draftMode.value) {
      const res = await chatApi.createSession({ title: text.slice(0, 20) })
      draftMode.value = false
      currentSessionId.value = res.sessionId
      await loadSessions()
    }
    await streamChat(currentSessionId.value, text, useRag.value)
  } catch (e: any) {
    ElMessage.error(e?.message || '对话失败')
  } finally {
    streaming.value = false
    streamingContent.value = ''
    if (currentSessionId.value) {
      selectSession(currentSessionId.value)
    }
  }
}

const streamChat = async (sessionId: string, message: string, useRag: boolean): Promise<void> => {
  const resp = await fetch('/api/chat/stream', {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
      ...(userStore.token ? { Authorization: `Bearer ${userStore.token}` } : {})
    },
    body: JSON.stringify({ sessionId, message, useRag })
  })
  if (!resp.ok) {
    const err = await resp.text()
    throw new Error(err || `HTTP ${resp.status}`)
  }
  const reader = resp.body!.getReader()
  const decoder = new TextDecoder()
  let buffer = ''
  while (true) {
    const { done, value } = await reader.read()
    if (done) break
    buffer += decoder.decode(value, { stream: true })
    const events = buffer.split('\n\n')
    buffer = events.pop() || ''
    for (const evt of events) {
      const lines = evt.split('\n')
      for (const line of lines) {
        if (line.startsWith('data:')) {
          const data = line.slice(5).trim()
          if (data === '[DONE]' || data === '') continue
          streamingContent.value += data
          scrollBottom()
        }
      }
    }
  }
}

onMounted(loadSessions)
</script>

<style lang="scss" scoped>
.chat-page {
  display: flex;
  gap: 16px;
  height: calc(100vh - 108px);
}

/* 会话列表 */
.sessions {
  width: 240px;
  background: #fff;
  border: 1px solid var(--tx-border-light);
  border-radius: var(--tx-radius);
  padding: 16px;
  display: flex;
  flex-direction: column;
  box-shadow: var(--tx-shadow);
  flex-shrink: 0;
}
.new-btn {
  margin-bottom: 14px;
}
.session-list {
  flex: 1;
  overflow-y: auto;
}
.session-item {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 10px 12px;
  border-radius: 8px;
  cursor: pointer;
  margin-bottom: 2px;
  transition: background 0.15s;
  color: var(--tx-text-secondary);
  &:hover { background: var(--tx-border-light); }
  &.active {
    background: var(--tx-primary-bg);
    color: var(--tx-primary);
    font-weight: 500;
  }
  .title {
    flex: 1;
    font-size: 13px;
    overflow: hidden;
    text-overflow: ellipsis;
    white-space: nowrap;
  }
  .del {
    opacity: 0;
    transition: opacity 0.15s;
    font-size: 14px;
  }
  &:hover .del {
    opacity: 1;
    color: var(--tx-danger);
  }
}
.empty {
  text-align: center;
  color: var(--tx-text-light);
  font-size: 13px;
  padding: 24px 0;
}

/* 对话区 */
.chat-area {
  flex: 1;
  background: #fff;
  border: 1px solid var(--tx-border-light);
  border-radius: var(--tx-radius);
  display: flex;
  flex-direction: column;
  box-shadow: var(--tx-shadow);
  min-width: 0;
}
.chat-header {
  padding: 14px 20px;
  border-bottom: 1px solid var(--tx-border-light);
}
.header-left {
  display: flex;
  align-items: center;
  gap: 10px;
}
.header-avatar {
  width: 36px;
  height: 36px;
  border-radius: 10px;
  background: var(--tx-primary-bg);
  color: var(--tx-primary);
  display: flex;
  align-items: center;
  justify-content: center;
}
.header-title {
  font-size: 15px;
  font-weight: 600;
  color: var(--tx-text);
}
.header-sub {
  font-size: 12px;
  color: var(--tx-text-light);
  margin-top: 1px;
}

/* 消息区 */
.messages {
  flex: 1;
  overflow-y: auto;
  padding: 24px;
  display: flex;
  flex-direction: column;
  gap: 20px;
  background: #fafbfc;
}
.welcome {
  margin: auto;
  text-align: center;
  color: var(--tx-text-light);
}
.welcome-icon {
  width: 64px;
  height: 64px;
  border-radius: 16px;
  background: var(--tx-primary-bg);
  color: var(--tx-primary);
  display: flex;
  align-items: center;
  justify-content: center;
  margin: 0 auto 16px;
}
.welcome h3 {
  font-size: 18px;
  color: var(--tx-text);
  margin-bottom: 6px;
}
.welcome p {
  font-size: 14px;
}

.msg {
  display: flex;
  gap: 10px;
  max-width: 80%;
  &.user {
    align-self: flex-end;
    flex-direction: row-reverse;
  }
  &.assistant {
    align-self: flex-start;
  }
}
.user-avatar {
  background: #e2e8f0;
  color: var(--tx-text-secondary);
}
.ai-avatar {
  background: var(--tx-primary-bg);
  color: var(--tx-primary);
}
.bubble {
  padding: 12px 16px;
  border-radius: 12px;
  font-size: 14px;
  line-height: 1.7;
}
.assistant .bubble {
  background: #fff;
  border: 1px solid var(--tx-border-light);
  color: var(--tx-text);
  border-top-left-radius: 4px;
}
.user .bubble {
  background: var(--tx-primary);
  color: #fff;
  border-top-right-radius: 4px;
}
.content {
  white-space: pre-wrap;
  word-break: break-word;
}
.cursor {
  animation: blink 1s infinite;
  font-weight: 300;
}
@keyframes blink {
  50% { opacity: 0; }
}

/* 输入区 */
.input-bar {
  display: flex;
  gap: 12px;
  padding: 16px 20px;
  border-top: 1px solid var(--tx-border-light);
  align-items: flex-end;
}
.input-wrapper {
  flex: 1;
  :deep(.el-textarea__inner) {
    border-radius: 10px;
    resize: none;
  }
}
.send-btn {
  height: 40px;
  padding: 0 20px;
}
</style>
