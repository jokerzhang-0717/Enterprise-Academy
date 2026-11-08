/**
 * 统一 API 客户端
 * - 开发环境走 Vite 代理 /api → http://127.0.0.1:8080
 * - 自动附加 Bearer Token
 * - 统一解包 { code, message, data }
 * - 401 时触发全局登出回调
 */

const BASE_URL = import.meta.env.VITE_API_BASE_URL || '/api';

const TOKEN_KEY = 'emp_token';
const REFRESH_TOKEN_KEY = 'emp_refresh_token';
const USER_KEY = 'emp_user';
// Keep credentials scoped to this tab so separate accounts can coexist on the same origin.
const authStorage = () => window.sessionStorage;

export class ApiError extends Error {
  constructor(message, code, status, data) {
    super(message || '请求失败');
    this.name = 'ApiError';
    this.code = code;
    this.status = status;
    this.data = data;
  }
}

let unauthorizedHandler = null;
// 并发请求可能同时收到 401，只让第一个触发全局登出与提示，避免重复弹窗。
let sessionExpiredNotified = false;

/** 注册 401 处理（由 App.vue 注入，避免 API 层直接依赖路由） */
export const setUnauthorizedHandler = (handler) => {
  unauthorizedHandler = handler;
};

export const getToken = () => {
  try {
    return authStorage().getItem(TOKEN_KEY) || '';
  } catch {
    return '';
  }
};

export const setToken = (token) => {
  try {
    if (token) {
      authStorage().setItem(TOKEN_KEY, token);
      sessionExpiredNotified = false;
    } else {
      authStorage().removeItem(TOKEN_KEY);
    }
  } catch {
    /* ignore */
  }
};

export const getRefreshToken = () => {
  try {
    return authStorage().getItem(REFRESH_TOKEN_KEY) || '';
  } catch {
    return '';
  }
};

export const setRefreshToken = (token) => {
  try {
    if (token) authStorage().setItem(REFRESH_TOKEN_KEY, token);
    else authStorage().removeItem(REFRESH_TOKEN_KEY);
  } catch {
    /* ignore */
  }
};

export const getStoredUser = () => {
  try {
    const raw = authStorage().getItem(USER_KEY);
    return raw ? JSON.parse(raw) : null;
  } catch {
    return null;
  }
};

export const setStoredUser = (user) => {
  try {
    if (user) authStorage().setItem(USER_KEY, JSON.stringify(user));
    else authStorage().removeItem(USER_KEY);
  } catch {
    /* ignore */
  }
};

export const clearAuth = () => {
  setToken('');
  setRefreshToken('');
  setStoredUser(null);
};

const buildQuery = (query) => {
  if (!query) return '';
  const params = new URLSearchParams();
  Object.entries(query).forEach(([key, value]) => {
    if (value === undefined || value === null || value === '') return;
    if (Array.isArray(value)) {
      value.forEach((item) => params.append(key, item));
    } else {
      params.append(key, value);
    }
  });
  const text = params.toString();
  return text ? `?${text}` : '';
};

const parseBody = async (response) => {
  const text = await response.text();
  if (!text) return null;
  try {
    return JSON.parse(text);
  } catch {
    return text;
  }
};

let refreshPromise = null;

/**
 * 使用刷新令牌换发新的访问令牌。
 * 多个并发请求同时收到 401 时共享同一次刷新，避免刷新令牌被重复消费。
 */
const refreshSession = async () => {
  if (refreshPromise) return refreshPromise;
  refreshPromise = (async () => {
    const refreshToken = getRefreshToken();
    if (!refreshToken) return false;
    try {
      const response = await fetch(`${BASE_URL}/auth/refresh`, {
        method: 'POST',
        headers: { Accept: 'application/json', 'Content-Type': 'application/json' },
        body: JSON.stringify({ refreshToken }),
      });
      const payload = await parseBody(response);
      const data = payload && typeof payload === 'object' && 'code' in payload
        ? payload.data
        : payload;
      if (!response.ok || !data?.token) return false;
      setToken(data.token);
      setRefreshToken(data.refreshToken || refreshToken);
      return true;
    } catch {
      return false;
    } finally {
      refreshPromise = null;
    }
  })();
  return refreshPromise;
};

/**
 * 发起请求
 * @param {string} path 以 / 开头的接口路径（不含 /api 前缀）
 * @param {object} options
 * @param {string} [options.method]
 * @param {object} [options.query]
 * @param {any} [options.body] JSON body
 * @param {FormData} [options.formData] 表单上传
 * @param {boolean} [options.auth=true]
 * @param {AbortSignal} [options.signal]
 * @returns {Promise<any>} 已解包的 data
 */
export const request = async (path, options = {}) => {
  const {
    method = 'GET',
    query,
    body,
    formData,
    auth = true,
    retryAfterRefresh = false,
    signal,
    headers: extraHeaders = {},
  } = options;

  const headers = { Accept: 'application/json', ...extraHeaders };
  const token = getToken();
  if (auth && token) headers.Authorization = `Bearer ${token}`;

  let payloadBody;
  if (formData) {
    payloadBody = formData;
  } else if (body !== undefined) {
    headers['Content-Type'] = 'application/json';
    payloadBody = JSON.stringify(body);
  }

  let response;
  try {
    response = await fetch(`${BASE_URL}${path}${buildQuery(query)}`, {
      method,
      headers,
      body: payloadBody,
      signal,
    });
  } catch (error) {
    if (error?.name === 'AbortError') throw error;
    throw new ApiError('无法连接后端服务，请确认服务已启动', -1, 0, null);
  }

  const payload = await parseBody(response);

  // 访问令牌过期时先用刷新令牌静默续期，再重试一次原请求。
  if (response.status === 401 && auth && !retryAfterRefresh) {
    const refreshed = await refreshSession();
    if (refreshed) {
      return request(path, { ...options, retryAfterRefresh: true });
    }
  }

  if (response.status === 401) {
    clearAuth();
    if (!sessionExpiredNotified) {
      sessionExpiredNotified = true;
      if (unauthorizedHandler) unauthorizedHandler();
    }
  }

  if (!response.ok) {
    throw new ApiError(
      payload?.message || `请求失败（${response.status}）`,
      payload?.code ?? response.status,
      response.status,
      payload?.data ?? null,
    );
  }

  if (payload && typeof payload === 'object' && 'code' in payload) {
    if (payload.code !== 0) {
      throw new ApiError(payload.message || '请求失败', payload.code, response.status, payload.data);
    }
    return payload.data;
  }

  return payload;
};

export const http = {
  get: (path, query, options = {}) => request(path, { ...options, method: 'GET', query }),
  post: (path, body, options = {}) => request(path, { ...options, method: 'POST', body }),
  put: (path, body, options = {}) => request(path, { ...options, method: 'PUT', body }),
  patch: (path, body, options = {}) => request(path, { ...options, method: 'PATCH', body }),
  delete: (path, options = {}) => request(path, { ...options, method: 'DELETE' }),
  upload: (path, formData, options = {}) =>
    request(path, { ...options, method: 'POST', formData }),
};
