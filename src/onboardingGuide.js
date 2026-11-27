
export const guideDetails = {
  platform: {
    step: '新员工手册 · 01',
    title: '认识云舒艺境',
    intro: '这是公司的学习与发展平台，入职后的课程、考试和学习记录都可以在这里管理。',
    sections: [
      { title: '课程学习', description: '进入“课程学习中心”浏览推荐课程和岗位相关课程，课程进度会自动记录。' },
      { title: '考试与测评', description: '在“考试与测评中心”查看待参加考试、考试时间和历史成绩，请留意考试开放与截止时间。' },
      { title: '通知与学习看板', description: '通过“消息中心”查看公司通知；在个人中心核对个人资料，在“个人学习看板”查看学习记录与进度。' },
    ],
    tip: '建议先完善个人资料，再查看推荐课程和最新通知。',
  },
  checklist: {
    step: '新员工手册 · 02',
    title: '完成入职必办事项',
    intro: '花几分钟完成基础设置，后续学习、考勤和部门通知才能准确关联到你的账号。',
    sections: [
      { title: '核对个人资料', description: '检查姓名、工号、岗位、所属部门等信息；如有错误，请联系管理员更新。' },
      { title: '熟悉考勤流程', description: '了解每日签到入口和考勤要求；如需培训假或请假，通过考勤与培训假页面提交申请。' },
      { title: '关注部门信息', description: '确认账号所属部门正确，以便接收对应的学习安排和部门通知。' },
    ],
    tip: '账号信息如无法自行修改，请联系管理员，不要使用他人账号代办。',
  },
  growth: {
    step: '新员工手册 · 03',
    title: '开启学习与成长',
    intro: '按照自己的节奏循序渐进完成学习任务，让学习记录和能力成长清晰可见。',
    sections: [
      { title: '从推荐课程开始', description: '优先学习平台推荐的课程；可以从首页“正在学习的课程”或课程中心进入学习。' },
      { title: '按时参加考核', description: '查看考试详情中的开放时间、截止时间和考试要求，在规定时间内完成测评。' },
      { title: '积累学习成果', description: '课程学时、考试记录和已获得的专业认证会沉淀在个人学习档案中，可随时查看。' },
    ],
    tip: '按自己的节奏持续学习，遇到课程或考试问题可以先查看通知，或联系培训管理员。',
  },
};

export const guideSteps = [guideDetails.platform, guideDetails.checklist, guideDetails.growth];
