<template>
  <div class="admin-bank admin-fit-page">

    <!-- 页头 -->
    <div class="page-head">
      <div>
        <h1 class="page-title">题库管理</h1>
        <p class="page-sub">统一维护题库 · 组卷时复制为独立试卷，发布后内容锁定</p>
      </div>
      <div class="page-head-actions">
        <button class="btn btn--secondary" @click="openImportModal">
          <Upload class="btn-icon" /> 批量导入
        </button>
        <button class="btn btn--primary" @click="openAdd">
          <Plus class="btn-icon" /> 新增题目
        </button>
      </div>
    </div>

    <!-- 题型统计 -->
    <div class="stat-strip">
      <div class="stat-item">
        <span class="stat-value">{{ questions.length }}</span>
        <span class="stat-label">总题数</span>
      </div>
      <div class="stat-item">
        <span class="stat-value stat-value--green">{{ poolQuestionCount }}</span>
        <span class="stat-label">备用题</span>
      </div>
      <div class="stat-item">
        <span class="stat-value stat-value--blue">{{ typeCount('single') }}</span>
        <span class="stat-label">单选题</span>
      </div>
      <div class="stat-item">
        <span class="stat-value stat-value--purple">{{ typeCount('multiple') }}</span>
        <span class="stat-label">多选题</span>
      </div>
      <div class="stat-item">
        <span class="stat-value stat-value--gold">{{ typeCount('judgment') }}</span>
        <span class="stat-label">判断题</span>
      </div>
      <div class="stat-item">
        <span class="stat-value stat-value--green">{{ typeCount('application') }}</span>
        <span class="stat-label">问答题</span>
      </div>
    </div>

    <!-- 筛选工具栏 -->
    <div class="toolbar">
      <select v-model="examFilter" class="select">
        <option value="">全部考试</option>
        <option value="__standalone__">备用题库</option>
        <option v-for="exam in exams" :key="exam.id" :value="exam.id">
          {{ exam.title.slice(0, 24) }}{{ exam.title.length > 24 ? '…' : '' }}
        </option>
      </select>
      <select v-model="typeFilter" class="select">
        <option value="">全部题型</option>
        <option v-for="t in qTypeOptions" :key="t.id" :value="t.id">{{ t.label }}</option>
      </select>
      <div class="search-box">
        <span class="search-icon"></span>
        <input v-model.trim="keyword" type="text" class="search-input" placeholder="搜索题干关键词..." />
        <button
          v-if="keyword"
          type="button"
          class="search-clear"
          aria-label="清除搜索内容"
          title="清除搜索内容"
          @click="keyword = ''"
        >
          <Close />
        </button>
      </div>
      <span class="toolbar-result">{{ filteredQuestions.length }} 道题</span>
    </div>

    <!-- 题目列表 -->
    <div class="question-list">
      <article
        v-for="(q, idx) in pagedQuestions"
        :key="q.id"
        class="question-item"
        :class="{ 'question-item--locked': isQuestionLocked(q) }"
        @click="openEdit(q)"
      >
        <div class="q-index">{{ (page - 1) * PAGE_SIZE + idx + 1 }}</div>
        <div class="q-main">
          <div class="q-top">
            <span class="q-type-chip" :class="`q-type-chip--${q.type}`">{{ q.typeLabel }}</span>
            <span class="q-score">{{ q.score }} 分</span>
            <span class="q-exam">{{ examTitle(q.examId) }}</span>
            <span v-if="isQuestionLocked(q)" class="q-exam">已锁定</span>
            <span v-if="q.knowledgePoint" class="q-exam">{{ q.knowledgePoint }}</span>
            <span class="q-exam">难度：{{ q.difficultyLabel }}</span>
            <span v-if="q.answer" class="q-answer mono">答案:{{ q.answer }}</span>
          </div>
          <p class="q-title">{{ q.title }}</p>
        </div>
        <div class="q-actions" @click.stop>
          <button
            class="icon-btn"
            :title="isQuestionLocked(q) ? '考试已发布或已有作答，题目已锁定' : '编辑'"
            :disabled="isQuestionLocked(q)"
            @click="openEdit(q)"
          ><EditPen /></button>
          <button
            class="icon-btn icon-btn--danger"
            :title="isQuestionLocked(q) ? '考试已发布或已有作答，题目已锁定' : '删除'"
            :disabled="isQuestionLocked(q)"
            @click="removeQ(q)"
          ><Delete /></button>
        </div>
      </article>
      <p v-if="!filteredQuestions.length" class="empty-tip">没有匹配的题目,点击右上角「新增题目」添加。</p>
    </div>

    <AdminPager
      v-model:page="page"
      :total="filteredQuestions.length"
      :total-pages="totalPages"
    />

    <!-- 新增 / 编辑题目弹窗 -->
    <div v-if="modalOpen" class="modal-backdrop" @click.self="closeModal">
      <div class="modal">
        <div class="modal-head">
          <h2 class="modal-title">{{ editingId ? '编辑题目' : '新增题目' }}</h2>
          <button type="button" class="modal-close" @click="closeModal"><Close /></button>
        </div>
        <form class="modal-body" @submit.prevent="saveQ">
          <div class="field-row">
            <label class="field">
              <span class="field-label">题目归属</span>
              <select v-model="form.examId" class="field-input">
                <option value="">备用题库（推荐）</option>
                <option v-for="exam in exams" :key="exam.id" :value="exam.id">
                  {{ exam.title.slice(0, 28) }}{{ exam.title.length > 28 ? '…' : '' }}
                </option>
              </select>
            </label>
            <label class="field">
              <span class="field-label">题型</span>
              <select v-model="form.type" class="field-input" @change="switchType">
                <option v-for="t in qTypeOptions" :key="t.id" :value="t.id">{{ t.label }}</option>
              </select>
            </label>
          </div>
          <div class="field-row">
            <label class="field">
              <span class="field-label">分值</span>
              <input v-model.number="form.score" type="number" min="1" class="field-input" />
            </label>
            <label class="field">
              <span class="field-label">难度</span>
              <select v-model.number="form.difficulty" class="field-input">
                <option :value="1">简单</option>
                <option :value="2">中等</option>
                <option :value="3">困难</option>
              </select>
            </label>
          </div>
          <label class="field">
            <span class="field-label">知识点</span>
            <input v-model.trim="form.knowledgePoint" type="text" class="field-input" placeholder="如：数据库事务" />
          </label>
          <label class="field">
            <span class="field-label">题干</span>
            <textarea v-model.trim="form.title" rows="3" class="field-input field-textarea" placeholder="请输入题目内容"></textarea>
          </label>
          <template v-if="needOptions">
            <div v-for="opt in form.options" :key="opt.key" class="field">
              <span class="field-label">选项 {{ opt.key }}</span>
              <input
                v-model.trim="opt.text"
                type="text"
                class="field-input"
                :disabled="form.type === 'judgment'"
                :placeholder="`选项 ${opt.key} 内容`"
              />
            </div>
            <label class="field">
              <span class="field-label">答案</span>
              <input
                v-if="form.type === 'multiple'"
                v-model.trim="form.answer"
                type="text"
                class="field-input mono"
                placeholder="多选题答案,如 ABD"
              />
              <select v-else v-model="form.answer" class="field-input">
                <option value="" disabled>请选择正确答案</option>
                <option v-for="opt in form.options" :key="opt.key" :value="opt.key">{{ opt.key }} · {{ opt.text }}</option>
              </select>
            </label>
          </template>
          <p v-if="formError" class="form-error">{{ formError }}</p>
          <div class="modal-actions">
            <button type="button" class="btn btn--ghost" @click="closeModal">取消</button>
            <button type="submit" class="btn btn--primary" :disabled="saving">{{ saving ? '保存中…' : (editingId ? '保存修改' : '添加题目') }}</button>
          </div>
        </form>
      </div>
    </div>

    <!-- 批量导入题目弹窗 -->
    <div v-if="importModalOpen" class="modal-backdrop" @click.self="closeImportModal">
      <div class="modal modal--import">
        <div class="modal-head">
          <h2 class="modal-title">批量导入题目</h2>
          <button type="button" class="modal-close" @click="closeImportModal"><Close /></button>
        </div>
        <div class="modal-body import-body">
          <label class="field">
            <span class="field-label">导入到</span>
            <select v-model="importExamId" class="field-input">
              <option value="">备用题库（推荐，组卷时再选入考试）</option>
              <option v-for="exam in exams" :key="exam.id" :value="exam.id">
                {{ exam.title }}
              </option>
            </select>
            <span class="field-hint">导入到备用题库的题目不会自动关联考试，可在新建或编辑考试时统一组卷。</span>
          </label>

          <div class="field">
            <div class="field-label-row">
              <span class="field-label">题目内容 (JSON 或文本格式)</span>
              <div class="field-helper-btns">
                <button type="button" class="link-btn" @click="fillSampleJSON">填入示例 JSON</button>
                <button type="button" class="link-btn" @click="fillSampleText">填入示例纯文本</button>
              </div>
            </div>
            <textarea
              v-model="importRawText"
              rows="7"
              class="field-input field-textarea mono-input"
              placeholder="在此粘贴题目 JSON 数组，或按格式编写的题目文本..."
              @input="parseQuestions"
            ></textarea>
          </div>

          <!-- 文件上传快捷通道 -->
          <div class="file-drop-area" @click="triggerImportFile">
            <input
              ref="importFileInputRef"
              type="file"
              accept=".json,.txt"
              style="display: none"
              @change="handleImportFile"
            />
            <Upload class="file-drop-icon" />
            <span class="file-drop-text">点击上传本地 .json 或 .txt 题库文件进行快速解析</span>
          </div>

          <!-- 解析状态与预览条 -->
          <div v-if="parsedList.length" class="parsed-preview">
            <div class="parsed-status-badge">
              ✓ 已成功解析 {{ parsedList.length }} 道题目
            </div>
            <div class="parsed-list">
              <div v-for="(item, idx) in parsedList.slice(0, 3)" :key="idx" class="parsed-item">
                <span class="parsed-item-type">{{ item.typeLabel }}</span>
                <span class="parsed-item-title">{{ item.title }}</span>
                <span class="parsed-item-score">{{ item.score }}分</span>
              </div>
              <div v-if="parsedList.length > 3" class="parsed-more">
                ... 还有 {{ parsedList.length - 3 }} 道题目
              </div>
            </div>
          </div>
          <p v-else-if="importRawText.trim()" class="form-error">
            未检测到合规题目数据，请检查格式或点击上方「填入示例」参考。
          </p>

          <div class="modal-actions">
            <button type="button" class="btn btn--ghost" @click="closeImportModal">取消</button>
            <button
              type="button"
              class="btn btn--primary"
              :disabled="!parsedList.length || !importExamId || importing"
              @click="confirmImport"
            >
              {{ importing ? '导入中…' : `确认导入 (${parsedList.length} 题)` }}
            </button>
          </div>
        </div>
      </div>
    </div>

    <ConfirmDialog
      v-if="confirmState"
      :title="confirmState.title"
      :subtitle="confirmState.subtitle"
      :rows="confirmState.rows"
      :tip="confirmState.tip"
      :tone="confirmState.tone"
      :confirm-text="confirmState.confirmText"
      @cancel="cancelConfirm"
      @confirm="acceptConfirm"
    />

  </div>
</template>

<script setup>
import { computed, ref, watch } from 'vue';
import { Plus, EditPen, Delete, Close, Upload } from '@element-plus/icons-vue';
import { adminStore, loadQuestions } from '../../adminStore.js';
import {
  createAdminQuestion,
  deleteAdminQuestion,
  updateAdminQuestion
} from '../../api/index.js';
import { usePager } from '../../composables/usePager.js';
import { useConfirm } from '../../composables/useConfirm.js';
import { useToast } from '../../composables/useToast.js';
import AdminPager from '../../components/AdminPager.vue';
import ConfirmDialog from '../../components/ConfirmDialog.vue';

const { show: showToast } = useToast();

const questions = adminStore.questions;

const { confirmState, askConfirm, cancelConfirm, acceptConfirm } = useConfirm();
const exams = adminStore.exams;

const examFilter = ref('');
const typeFilter = ref('');
const keyword = ref('');

const qTypeOptions = [
  { id: 'single',      label: '单选题' },
  { id: 'multiple',    label: '多选题' },
  { id: 'judgment',    label: '判断题' },
  { id: 'application', label: '问答题' }
];

const typeCount = (type) => questions.filter(q => q.type === type).length;
const poolQuestionCount = computed(() => questions.filter((question) => !question.examId).length);
const isQuestionLocked = (question) => {
  if (!question.examId) return false;
  const exam = exams.find((item) => item.id === question.examId);
  return Boolean(exam && (exam.statusCode !== 1 || Number(exam.attemptCount || 0) > 0));
};
const examTitle = (examId) => {
  if (!examId) return '备用题库';
  const exam = exams.find(e => e.id === examId);
  return exam ? `所属:${exam.title.slice(0, 14)}${exam.title.length > 14 ? '…' : ''}` : '未关联考试';
};

const filteredQuestions = computed(() => {
  const kw = keyword.value.trim().toLowerCase();
  return questions.filter(q => {
    const hitExam = !examFilter.value
      || (examFilter.value === '__standalone__' ? !q.examId : q.examId === examFilter.value);
    const hitType = !typeFilter.value || q.type === typeFilter.value;
    const hitKw = !kw || `${q.title || ''} ${q.knowledgePoint || ''}`.toLowerCase().includes(kw);
    return hitExam && hitType && hitKw;
  });
});

const PAGE_SIZE = 5;
const { page, totalPages, paged: pagedQuestions, resetPage } = usePager(filteredQuestions, PAGE_SIZE);
watch([examFilter, typeFilter, keyword], resetPage);

/* ---------- 弹窗表单 ---------- */
const modalOpen = ref(false);
const editingId = ref(null);
const formError = ref('');
const saving = ref(false);

const emptyForm = () => ({
  examId: '',
  type: 'single',
  score: 5,
  knowledgePoint: '',
  difficulty: 2,
  title: '',
  options: [
    { key: 'A', text: '' }, { key: 'B', text: '' },
    { key: 'C', text: '' }, { key: 'D', text: '' }
  ],
  answer: ''
});
const form = ref(emptyForm());

const needOptions = computed(() => form.value.type !== 'application');

const openAdd = () => {
  editingId.value = null;
  formError.value = '';
  form.value = emptyForm();
  modalOpen.value = true;
};

const openEdit = (q) => {
  if (isQuestionLocked(q)) {
    showToast('该考试已发布或已有作答记录，题目已锁定。', 'info');
    return;
  }
  editingId.value = q.numericId ?? q.id;
  formError.value = '';
  form.value = {
    examId: q.examId,
    type: q.type,
    score: q.score,
    knowledgePoint: q.knowledgePoint || '',
    difficulty: q.difficulty || 2,
    title: q.title,
    options: (q.options && q.options.length) ? q.options.map(o => ({ ...o })) : emptyForm().options,
    answer: q.answer || ''
  };
  modalOpen.value = true;
};

const closeModal = () => {
  modalOpen.value = false;
  formError.value = '';
};

const switchType = () => {
  if (form.value.type === 'judgment') {
    form.value.options = [{ key: 'A', text: '正确' }, { key: 'B', text: '错误' }];
  } else if (form.value.type === 'application') {
    form.value.answer = '';
  } else if (!form.value.options.length) {
    form.value.options = emptyForm().options;
  }
};

const typeLabelMap = { single: '单选题', multiple: '多选题', judgment: '判断题', application: '问答题' };

/* 表单里题型是字符串，写入接口要 1~4；选项按 key 存进 question_option */
const typeCodeMap = { single: 1, multiple: 2, judgment: 3, application: 4 };

const toSavePayload = (source) => {
  const objective = source.type !== 'application';
  return {
    examId: source.examId ? Number(source.examId) : 0,
    type: typeCodeMap[source.type] ?? 1,
    score: Number(source.score) || 5,
    title: source.title.trim(),
    knowledgePoint: (source.knowledgePoint || '').trim(),
    difficulty: Number(source.difficulty) || 2,
    correctAnswer: objective ? String(source.answer || '').trim().toUpperCase() : '',
    options: objective
      ? source.options
          .filter(opt => (opt.text || '').trim())
          .map((opt, i) => ({ key: opt.key, content: opt.text.trim(), sortNo: i + 1 }))
      : []
  };
};

const saveQ = async () => {
  if (!form.value.title.trim()) { formError.value = '请输入题干。'; return; }
  if (needOptions.value) {
    const hasOption = form.value.options.some(o => o.text.trim());
    if (!hasOption) { formError.value = '请填写选项内容。'; return; }
    if (!form.value.answer.trim()) {
      formError.value = form.value.type === 'multiple' ? '请填写答案(如 ABD)。' : '请选择答案。';
      return;
    }
  }

  saving.value = true;
  try {
    const payload = toSavePayload(form.value);
    if (editingId.value) {
      await updateAdminQuestion(editingId.value, payload);
    } else {
      await createAdminQuestion(payload);
    }
    await loadQuestions();
    closeModal();
  } catch (err) {
    formError.value = err?.message || '保存题目失败，请重试。';
  } finally {
    saving.value = false;
  }
};

const removeQ = (q) => {
  if (isQuestionLocked(q)) {
    showToast('该考试已发布或已有作答记录，题目已锁定。', 'info');
    return;
  }
  const exam = adminStore.exams.find(e => e.id === q.examId);
  askConfirm({
    tone: 'danger',
    title: '删除该题目？',
    subtitle: `${q.typeLabel} · ${q.score} 分`,
    rows: [
      { label: '所属考试', value: exam ? exam.title : q.examId },
      { label: '正确答案', value: q.answer }
    ],
    tip: '删除后该题目及其选项将从题库和相关考试中移除，此操作不可恢复。',
    confirmText: '确认删除',
    action: async () => {
      try {
        await deleteAdminQuestion(q.numericId ?? q.id);
        await loadQuestions();
        showToast('题目已删除', 'success');
      } catch (err) {
        showToast(err?.message || '删除题目失败', 'error');
      }
    }
  });
};

/* ---------- 批量导入题目逻辑 ---------- */
const importModalOpen = ref(false);
const importExamId = ref('');
const importRawText = ref('');
const importFileInputRef = ref(null);
const parsedList = ref([]);
const importing = ref(false);

const openImportModal = () => {
  importExamId.value = '';
  importRawText.value = '';
  parsedList.value = [];
  importModalOpen.value = true;
};

const closeImportModal = () => {
  importModalOpen.value = false;
  importRawText.value = '';
  parsedList.value = [];
};

const fillSampleJSON = () => {
  const sample = [
    {
      type: 'single',
      typeLabel: '单选题',
      title: '在企业级微服务架构中，服务注册中心的核心功能是什么？',
      score: 5,
      options: [
        { key: 'A', text: '服务地址的管理、健康检查与动态发现' },
        { key: 'B', text: '分布式数据库事务持久化管理' },
        { key: 'C', text: '前端页面静态资源的 CDN 加速' },
        { key: 'D', text: '统一生成前端客户端调用代码' }
      ],
      answer: 'A'
    },
    {
      type: 'multiple',
      typeLabel: '多选题',
      title: '在高并发系统中，常见的流量削峰与防雪崩手段包含以下哪些？',
      score: 5,
      options: [
        { key: 'A', text: '分布式消息队列异步化处理' },
        { key: 'B', text: '令牌桶/漏桶算法限流' },
        { key: 'C', text: '服务熔断与降级保护' },
        { key: 'D', text: '完全关闭所有日志输出' }
      ],
      answer: 'ABC'
    },
    {
      type: 'judgment',
      typeLabel: '判断题',
      title: 'Vue 3 的 Composition API 可以有效提升复杂大型组件的逻辑复用与代码可维护性。',
      score: 5,
      options: [
        { key: 'A', text: '正确' },
        { key: 'B', text: '错误' }
      ],
      answer: 'A'
    }
  ];
  importRawText.value = JSON.stringify(sample, null, 2);
  parseQuestions();
};

const fillSampleText = () => {
  importRawText.value = `1. [单选题] Spring Boot 3 最低要求的 JDK 版本是？ (5分)
A. JDK 8
B. JDK 11
C. JDK 17
D. JDK 21
答案: C

2. [判断题] Redis 默认是单线程模型处理网络请求和命令执行。 (5分)
A. 正确
B. 错误
答案: A`;
  parseQuestions();
};

const parseQuestions = () => {
  const raw = importRawText.value.trim();
  if (!raw) {
    parsedList.value = [];
    return;
  }
  // 优先 JSON 解析
  if (raw.startsWith('[') || raw.startsWith('{')) {
    try {
      const parsed = JSON.parse(raw);
      const arr = Array.isArray(parsed) ? parsed : [parsed];
      parsedList.value = arr.map(item => ({
        type: item.type || 'single',
        typeLabel: typeLabelMap[item.type || 'single'] || '单选题',
        title: item.title || '',
        score: Number(item.score) || 5,
        options: item.options || [
          { key: 'A', text: '选项 A' },
          { key: 'B', text: '选项 B' }
        ],
        answer: String(item.answer || '').toUpperCase()
      })).filter(q => q.title.trim());
      return;
    } catch (e) {
      // 容错进入纯文本匹配
    }
  }

  // 文本解析
  const items = [];
  const blocks = raw.split(/\n\s*\n+/);
  for (const block of blocks) {
    const lines = block.split('\n').map(l => l.trim()).filter(Boolean);
    if (!lines.length) continue;
    let titleLine = lines[0].replace(/^\d+[\.、\s]*/, '');
    let type = 'single';
    let score = 5;
    if (titleLine.includes('多选')) type = 'multiple';
    else if (titleLine.includes('判断')) type = 'judgment';
    else if (titleLine.includes('问答')) type = 'application';

    titleLine = titleLine.replace(/\[(单选题?|多选题?|判断题?|问答题?)\]\s*/g, '');
    const scoreMatch = titleLine.match(/[\(（](\d+)分[\)）]/);
    if (scoreMatch) {
      score = parseInt(scoreMatch[1], 10);
      titleLine = titleLine.replace(/[\(（]\d+分[\)）]/, '').trim();
    }

    const options = [];
    let answer = '';
    for (let i = 1; i < lines.length; i++) {
      const line = lines[i];
      if (/^(答案|参考答案)[:：\s]*/i.test(line)) {
        answer = line.replace(/^(答案|参考答案)[:：\s]*/i, '').trim().toUpperCase();
      } else {
        const optMatch = line.match(/^([A-D])[\.、\s]+(.*)$/);
        if (optMatch) {
          options.push({ key: optMatch[1].toUpperCase(), text: optMatch[2].trim() });
        }
      }
    }

    if (type === 'judgment' && !options.length) {
      options.push({ key: 'A', text: '正确' }, { key: 'B', text: '错误' });
    }

    if (titleLine) {
      items.push({
        type,
        typeLabel: typeLabelMap[type] || '单选题',
        title: titleLine,
        score,
        options: options.length ? options : [
          { key: 'A', text: '选项 A' },
          { key: 'B', text: '选项 B' }
        ],
        answer: answer || 'A'
      });
    }
  }
  parsedList.value = items;
};

const triggerImportFile = () => {
  importFileInputRef.value?.click();
};

const handleImportFile = (e) => {
  const file = e.target.files?.[0];
  if (!file) return;
  const reader = new FileReader();
  reader.onload = (loadEvent) => {
    importRawText.value = String(loadEvent.target.result || '');
    parseQuestions();
    showToast(`文件【${file.name}】已载入，成功解析 ${parsedList.value.length} 道题目`, 'success');
  };
  reader.readAsText(file);
};

const confirmImport = async () => {
  if (!parsedList.value.length) {
    showToast('没有可导入的有效题目', 'error');
    return;
  }

  importing.value = true;
  const failures = [];
  for (const item of parsedList.value) {
    try {
      await createAdminQuestion(toSavePayload({ ...item, examId: importExamId.value }));
    } catch (error) {
      failures.push(error?.message || '未知错误');
    }
  }
  await loadQuestions();
  importing.value = false;

  const done = parsedList.value.length - failures.length;
  if (failures.length) {
    showToast(`导入完成：成功 ${done} 题，失败 ${failures.length} 题。${failures[0]}`, 'error');
  } else {
    showToast(`已成功导入 ${done} 道新题目！`, 'success');
    closeImportModal();
  }
};
</script>

<style scoped>
/* ============================================================
   AdminQuestionBank · 题库管理 · 纯原生 CSS
   ============================================================ */

* { margin: 0; padding: 0; box-sizing: border-box; }
button, input, select, textarea { font-family: inherit; }
button { cursor: pointer; }

.admin-bank {
  display: flex;
  flex-direction: column;
  gap: 16px;
  min-height: 100%;
}

/* ========== 页头 ========== */
.page-head {
  display: flex;
  align-items: flex-end;
  justify-content: space-between;
  gap: 16px;
  flex-wrap: wrap;
}

.page-title {
  font-size: 24px;
  font-weight: 700;
  color: var(--app-heading);
  letter-spacing: -0.02em;
}

.page-sub {
  margin-top: 4px;
  font-size: 13px;
  color: var(--app-text-4);
}

.btn {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  padding: 9px 16px;
  font-size: 13px;
  font-weight: 600;
  border-radius: 10px;
  border: 1px solid transparent;
  transition: background 0.15s, border-color 0.15s, transform 0.15s;
}
.btn:active { transform: translateY(1px); }
.btn-icon { width: 15px; height: 15px; }

.btn--primary {
  background: linear-gradient(135deg, var(--app-brand), var(--app-brand-deep));
  color: var(--app-brand-on);
  border-color: var(--app-brand-deep);
  box-shadow: 0 2px 6px var(--app-brand-glow);
}
.btn--primary:hover { background: linear-gradient(135deg, var(--app-brand-hi), var(--app-brand-deep)); }

.page-head-actions {
  display: flex;
  align-items: center;
  gap: 10px;
}

.btn--secondary {
  background: var(--app-blue-bg);
  color: var(--app-blue-fg);
  border-color: var(--app-blue-line);
}
.btn--secondary:hover {
  background: var(--app-blue-bg);
  color: var(--app-blue-fg);
  border-color: var(--app-blue-line);
}

.btn--ghost {
  background: var(--app-surface);
  color: var(--app-text-2);
  border-color: var(--app-text-6);
}
.btn--ghost:hover { border-color: var(--app-brand); color: var(--app-brand-ink); }

/* ========== 题型统计 ========== */
.stat-strip {
  display: grid;
  grid-template-columns: repeat(6, minmax(0, 1fr));
  gap: 12px;
}

.stat-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 2px;
  padding: 14px;
  background: var(--app-surface);
  border: 1px solid var(--app-border);
  border-radius: 12px;
}

.stat-value {
  font-size: 22px;
  font-weight: 800;
  color: var(--app-heading);
  font-variant-numeric: tabular-nums;
}
.stat-value--blue   { color: var(--app-accent-deep); }
.stat-value--purple { color: var(--app-violet-fg); }
.stat-value--gold   { color: var(--app-amber-fg); }
.stat-value--green  { color: var(--app-emerald-fg); }

.stat-label {
  font-size: 12px;
  color: var(--app-text-4);
}

/* ========== 工具栏 ========== */
.toolbar {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 12px 14px;
  background: var(--app-surface);
  border: 1px solid var(--app-border);
  border-radius: 12px;
  flex-wrap: wrap;
}

.select {
  padding: 9px 12px;
  font-size: 13px;
  color: var(--app-text-2);
  background: var(--app-surface-2);
  border: 1px solid var(--app-border);
  border-radius: 10px;
  outline: none;
  max-width: 260px;
  transition: border-color 0.15s;
}
.select:focus { border-color: var(--app-accent-strong); }

.search-box {
  display: flex;
  align-items: center;
  gap: 8px;
  flex: 1;
  min-width: 180px;
  padding: 0 12px;
  background: var(--app-surface-2);
  border: 1px solid var(--app-border);
  border-radius: 10px;
  transition: border-color 0.15s, box-shadow 0.15s;
}
.search-box:focus-within {
  border-color: var(--app-accent-strong);
  box-shadow: 0 0 0 3px var(--app-accent-a12);
}

.search-icon {
  position: relative;
  width: 14px;
  height: 14px;
  flex-shrink: 0;
}
.search-icon::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  width: 9px;
  height: 9px;
  border: 2px solid var(--app-text-4);
  border-radius: 50%;
}
.search-icon::after {
  content: '';
  position: absolute;
  bottom: 0;
  right: 0;
  width: 6px;
  height: 2px;
  background: var(--app-text-4);
  border-radius: 1px;
  transform: rotate(45deg);
  transform-origin: right center;
}

.search-input {
  flex: 1;
  min-width: 0;
  padding: 9px 0;
  font-size: 13px;
  color: var(--app-text);
  background: transparent;
  border: none;
  outline: none;
}

.search-clear {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  flex: 0 0 24px;
  width: 24px;
  height: 24px;
  padding: 0;
  color: var(--app-text-4);
  background: transparent;
  border: 0;
  border-radius: 50%;
  cursor: pointer;
}
.search-clear:hover { color: var(--app-text-2); background: var(--app-chip); }
.search-clear svg { width: 14px; height: 14px; }

.toolbar-result {
  margin-left: auto;
  font-size: 12px;
  color: var(--app-text-5);
}

/* ========== 题目列表 ========== */
.question-list {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.question-item {
  display: flex;
  align-items: flex-start;
  gap: 12px;
  padding: 14px;
  background: var(--app-surface);
  border: 1px solid var(--app-border);
  border-radius: 12px;
  cursor: pointer;
  transition: background 0.15s, box-shadow 0.15s, border-color 0.15s;
}
.question-item:hover {
  background: var(--app-accent-soft);
  border-color: var(--app-accent-border);
  box-shadow: inset 3px 0 0 var(--app-accent), 0 4px 16px var(--app-accent-a20);
}
.question-item:hover .q-title { color: var(--app-accent-deep); }
.question-item--locked { cursor: default; }
.question-item--locked:hover {
  background: var(--app-surface);
  border-color: var(--app-border);
  box-shadow: none;
}
.question-item--locked:hover .q-title { color: var(--app-text); }
/* 卡片底变成青底后,原本同底的序号块与单选 chip 会糊在一起,改用实底卡片色保住边界 */
.question-item:hover .q-index,
.question-item:hover .q-type-chip--single { background: var(--app-surface); }

.q-index {
  width: 26px;
  height: 26px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 12px;
  font-weight: 800;
  color: var(--app-accent-deep);
  background: var(--app-accent-soft);
  border-radius: 8px;
  flex-shrink: 0;
}

.q-main {
  flex: 1;
  min-width: 0;
}

.q-top {
  display: flex;
  align-items: center;
  gap: 8px;
  flex-wrap: wrap;
}

.q-type-chip {
  padding: 2px 8px;
  font-size: 10px;
  font-weight: 700;
  border-radius: 9999px;
}
.q-type-chip--single      { color: var(--app-accent-deep); background: var(--app-accent-soft); }
.q-type-chip--multiple    { color: var(--app-violet-fg); background: var(--app-violet-bg); }
.q-type-chip--judgment    { color: var(--app-amber-fg); background: var(--app-amber-bg); }
.q-type-chip--application { color: var(--app-emerald-fg); background: var(--app-emerald-bg); }

.q-score {
  font-size: 11px;
  font-weight: 600;
  color: var(--app-text-4);
}

.q-exam {
  font-size: 11px;
  color: var(--app-text-5);
}

.q-answer {
  font-size: 11px;
  font-weight: 700;
  color: var(--app-emerald-fg);
}

.q-title {
  margin-top: 6px;
  font-size: 13px;
  line-height: 1.6;
  color: var(--app-text);
  word-break: break-all;
}

.q-actions {
  display: inline-flex;
  gap: 6px;
  flex-shrink: 0;
}

.icon-btn {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 28px;
  height: 28px;
  color: var(--app-text-3);
  background: var(--app-surface-2);
  border: 1px solid var(--app-border);
  border-radius: 8px;
  transition: background 0.15s, color 0.15s, border-color 0.15s;
}
.icon-btn svg { width: 14px; height: 14px; }
.icon-btn:hover { background: var(--app-accent-soft); color: var(--app-accent-deep); border-color: var(--app-accent-border); }
.icon-btn--danger:hover { background: var(--app-rose-bg); color: var(--app-rose-fg); border-color: var(--app-rose-line); }
.icon-btn:disabled { opacity: 0.45; cursor: not-allowed; }
.icon-btn:disabled:hover {
  color: var(--app-text-3);
  background: var(--app-surface-2);
  border-color: var(--app-border);
}

.empty-tip {
  padding: 40px 0;
  text-align: center;
  font-size: 13px;
  color: var(--app-text-5);
}

/* ========== 弹窗 ========== */
.modal-backdrop {
  position: fixed;
  inset: 0;
  z-index: 100;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 20px;
  background: rgba(15, 30, 51, 0.45);
  backdrop-filter: blur(2px);
}

.modal {
  width: 100%;
  max-width: 560px;
  max-height: 88vh;
  overflow-y: auto;
  background: var(--app-surface);
  border-radius: 16px;
  box-shadow: 0 24px 48px rgba(15, 30, 51, 0.25);
  animation: modal-in 0.22s ease-out;
}

@keyframes modal-in {
  from { opacity: 0; transform: translateY(10px) scale(0.98); }
  to   { opacity: 1; transform: translateY(0) scale(1); }
}

.modal-head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 16px 20px;
  border-bottom: 1px solid var(--app-border-soft);
}

.modal-title {
  font-size: 16px;
  font-weight: 700;
  color: var(--app-heading);
}

.modal-close {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 30px;
  height: 30px;
  color: var(--app-text-4);
  background: transparent;
  border: none;
  border-radius: 8px;
  transition: background 0.15s, color 0.15s;
}
.modal-close svg { width: 16px; height: 16px; }
.modal-close:hover { background: var(--app-surface-3); color: var(--app-heading); }

.modal-body {
  display: flex;
  flex-direction: column;
  gap: 12px;
  padding: 20px;
}

.field-row {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 12px;
}

.field {
  display: flex;
  flex-direction: column;
  gap: 6px;
}

.field-label {
  font-size: 12px;
  font-weight: 600;
  color: var(--app-text-3);
}

.field-hint {
  color: var(--app-text-5);
  font-size: 11px;
  line-height: 1.5;
}

.field-input {
  width: 100%;
  padding: 10px 12px;
  font-size: 13px;
  color: var(--app-text);
  background: var(--app-surface-2);
  border: 1px solid var(--app-border);
  border-radius: 10px;
  outline: none;
  transition: border-color 0.15s, box-shadow 0.15s;
}
.field-input:focus {
  border-color: var(--app-accent-strong);
  box-shadow: 0 0 0 3px var(--app-accent-a12);
}

.field-textarea {
  resize: vertical;
  min-height: 64px;
  line-height: 1.6;
}

.mono { font-family: 'JetBrains Mono', ui-monospace, monospace; }

.form-error {
  font-size: 12px;
  color: var(--app-rose-fg);
}

.modal-actions {
  display: flex;
  justify-content: flex-end;
  gap: 10px;
  margin-top: 4px;
}

/* ========== 批量导入弹窗样式 ========== */
.modal--import {
  max-width: 600px;
}

.import-body {
  gap: 14px;
}

.field-label-row {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.field-helper-btns {
  display: flex;
  align-items: center;
  gap: 8px;
}

.link-btn {
  background: none;
  border: none;
  font-size: 11px;
  font-weight: 600;
  color: var(--app-blue-fg);
  cursor: pointer;
  padding: 0;
  text-decoration: underline;
}
.link-btn:hover {
  color: var(--app-blue-fg);
}

.mono-input {
  font-family: 'JetBrains Mono', ui-monospace, monospace;
  font-size: 12px;
  min-height: 120px;
  white-space: pre;
}

.file-drop-area {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  padding: 10px 14px;
  background: var(--app-surface-2);
  border: 1.5px dashed var(--app-border-strong);
  border-radius: 10px;
  cursor: pointer;
  color: var(--app-text-4);
  transition: all 0.15s ease;
}
.file-drop-area:hover {
  background: var(--app-surface-3);
  border-color: #2563EB;
  color: var(--app-blue-fg);
}
.file-drop-icon {
  width: 16px;
  height: 16px;
}
.file-drop-text {
  font-size: 12px;
  font-weight: 500;
}

.parsed-preview {
  display: flex;
  flex-direction: column;
  gap: 8px;
  padding: 12px;
  background: var(--app-emerald-bg);
  border: 1px solid var(--app-emerald-line);
  border-radius: 10px;
}

.parsed-status-badge {
  font-size: 12px;
  font-weight: 700;
  color: var(--app-emerald-fg);
}

.parsed-list {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.parsed-item {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 12px;
  color: var(--app-text);
  background: var(--app-surface);
  padding: 6px 10px;
  border-radius: 6px;
  border: 1px solid var(--app-emerald-line);
}

.parsed-item-type {
  font-size: 11px;
  font-weight: 700;
  padding: 2px 6px;
  border-radius: 4px;
  background: var(--app-blue-bg);
  color: var(--app-violet-fg);
  flex-shrink: 0;
}

.parsed-item-title {
  flex: 1;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.parsed-item-score {
  font-size: 11px;
  color: var(--app-text-4);
  font-weight: 600;
  flex-shrink: 0;
}

.parsed-more {
  font-size: 11px;
  color: var(--app-emerald-fg);
  font-style: italic;
  padding-left: 4px;
}

@media (max-width: 640px) {
  .stat-strip { grid-template-columns: repeat(3, minmax(0, 1fr)); }
  .field-row { grid-template-columns: 1fr; }
}
</style>
