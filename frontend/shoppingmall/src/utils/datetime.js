// src/utils/datetime.js

// 推荐使用功能更强大的日期库，如 date-fns-tz 或 Day.js（带时区插件）
// 以下是使用原生 Date 对象的简单示例

/**
 * 将UTC ISO字符串转换为用户本地时区的可读格式.
 * @param {string} utcIsoString - 例如 "2025-05-22T10:00:00Z"
 * @param {string} [locale=navigator.language] - 用于本地化的语言代码，如 'zh-CN'
 * @param {object} [options] - toLocaleString 的选项对象
 * @returns {string} 格式化后的本地时间字符串，或错误时的原始字符串
 */
export function formatUtcToLocal(utcIsoString, locale = navigator.language, options) {
  if (!utcIsoString) return '';
  const date = new Date(utcIsoString);
  if (isNaN(date.getTime())) {
    console.warn(`无效的日期字符串: ${utcIsoString}`);
    return utcIsoString; // 或者返回一个更友好的错误提示，如 '日期无效'
  }

  const defaultOptions = {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit',
    second: '2-digit',
    hour12: false,
    // timeZone: 'Asia/Shanghai' // 如果您想强制转为特定时区而不是浏览器本地时区
  };

  try {
    return date.toLocaleString(locale, { ...defaultOptions, ...options });
  } catch (e) {
    console.error('日期时间格式化错误:', e);
    return date.toLocaleString(locale, defaultOptions); // 降级使用默认选项
  }
}

// 您还可以添加其他时间处理相关的工具函数在这里