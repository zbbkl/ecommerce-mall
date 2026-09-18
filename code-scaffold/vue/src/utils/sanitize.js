// 富文本 XSS 过滤：所有 v-html 渲染的用户/商家录入内容必须先过这里
// 白名单策略：DOMPurify 默认保留 p/strong/em/u/img/a 等安全标签，
// 剥离 <script>/<iframe>/事件属性（onerror/onload 等）/javascript: 链接
import DOMPurify from 'dompurify'

export function sanitizeHtml(html) {
  if (!html) return ''
  return DOMPurify.sanitize(html, {
    FORBID_TAGS: ['style', 'form', 'input'],
    FORBID_ATTR: ['target'],
    ALLOWED_URI_REGEXP: /^(?:(?:(?:f|ht)tps?|mailto|tel|callto|cid|xmpp):|[^a-z]|[a-z+.\-]+(?:[^a-z+.\-:]|$))/i
  })
}
