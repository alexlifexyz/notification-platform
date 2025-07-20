# 前端开发指南

## 🎯 **项目概述**

### 技术栈推荐
- **框架**: React 18+ / Vue 3+ / Angular 15+
- **UI库**: Ant Design / Element Plus / Angular Material
- **状态管理**: Redux Toolkit / Pinia / NgRx
- **路由**: React Router / Vue Router / Angular Router
- **HTTP客户端**: Axios / Fetch API
- **构建工具**: Vite / Webpack / Angular CLI
- **类型检查**: TypeScript

### 项目结构
```
src/
├── components/          # 通用组件
│   ├── Layout/         # 布局组件
│   ├── Table/          # 表格组件
│   ├── Form/           # 表单组件
│   └── Modal/          # 模态框组件
├── pages/              # 页面组件
│   ├── Template/       # 模板管理
│   ├── RecipientGroup/ # 收件人组管理
│   ├── Notification/   # 通知审计
│   └── Settings/       # 系统设置
├── services/           # API服务
│   ├── api.ts         # API配置
│   ├── template.ts    # 模板API
│   ├── group.ts       # 收件人组API
│   └── notification.ts # 通知API
├── utils/              # 工具函数
│   ├── request.ts     # 请求封装
│   ├── i18n.ts        # 国际化
│   └── constants.ts   # 常量定义
├── types/              # 类型定义
│   ├── api.ts         # API类型
│   └── common.ts      # 通用类型
└── assets/             # 静态资源
    ├── styles/        # 样式文件
    └── images/        # 图片资源
```

## 🔧 **开发规范**

### 1. 代码规范
```typescript
// 组件命名：PascalCase
const TemplateManagement: React.FC = () => {
  // Hook使用
  const [loading, setLoading] = useState(false);
  const [data, setData] = useState<TemplateDto[]>([]);
  
  // 事件处理函数：handle + 动作
  const handleSearch = useCallback(() => {
    // 实现搜索逻辑
  }, []);
  
  return (
    <div className="template-management">
      {/* JSX内容 */}
    </div>
  );
};
```

### 2. API调用规范
```typescript
// services/template.ts
import { request } from '@/utils/request';
import type { TemplateDto, PageResult, TemplateQueryRequest } from '@/types/api';

export const templateApi = {
  // 查询模板列表
  query: (params: TemplateQueryRequest): Promise<PageResult<TemplateDto>> => {
    return request.post('/api/admin/templates/query', params);
  },
  
  // 获取模板详情
  getById: (id: number): Promise<TemplateDto> => {
    return request.get(`/api/admin/templates/${id}`);
  },
  
  // 创建模板
  create: (data: Partial<TemplateDto>): Promise<TemplateDto> => {
    return request.post('/api/admin/templates', data);
  },
  
  // 更新模板
  update: (id: number, data: Partial<TemplateDto>): Promise<TemplateDto> => {
    return request.put(`/api/admin/templates/${id}`, data);
  },
  
  // 删除模板
  delete: (id: number): Promise<void> => {
    return request.delete(`/api/admin/templates/${id}`);
  }
};
```

### 3. 错误处理规范
```typescript
// utils/request.ts
import axios from 'axios';
import { message } from 'antd';
import { i18n } from './i18n';

const request = axios.create({
  baseURL: process.env.REACT_APP_API_BASE_URL,
  timeout: 10000,
});

// 请求拦截器
request.interceptors.request.use(
  (config) => {
    const token = localStorage.getItem('token');
    if (token) {
      config.headers.Authorization = `Bearer ${token}`;
    }
    return config;
  },
  (error) => Promise.reject(error)
);

// 响应拦截器
request.interceptors.response.use(
  (response) => {
    const { data } = response;
    if (data.success) {
      return data.data;
    } else {
      // 业务错误处理
      message.error(i18n.t(data.message) || data.message);
      return Promise.reject(new Error(data.message));
    }
  },
  (error) => {
    // HTTP错误处理
    if (error.response) {
      const { status } = error.response;
      switch (status) {
        case 401:
          message.error(i18n.t('error.unauthorized'));
          // 跳转登录页
          window.location.href = '/login';
          break;
        case 403:
          message.error(i18n.t('error.forbidden'));
          break;
        case 404:
          message.error(i18n.t('error.not.found'));
          break;
        case 500:
          message.error(i18n.t('error.server'));
          break;
        default:
          message.error(i18n.t('error.unknown'));
      }
    } else {
      message.error(i18n.t('error.network'));
    }
    return Promise.reject(error);
  }
);

export { request };
```

## 🌐 **国际化实现**

### 1. 配置国际化
```typescript
// utils/i18n.ts
import i18n from 'i18next';
import { initReactI18next } from 'react-i18next';

// 语言资源
const resources = {
  zh: {
    translation: {
      'template.management': '模板管理',
      'template.create': '新建模板',
      'template.edit': '编辑模板',
      'template.delete': '删除模板',
      'template.code': '模板代码',
      'template.name': '模板名称',
      'channel.inapp': '站内信',
      'channel.sms': '短信',
      'channel.email': '邮件',
      'channel.im': '企业IM',
      'status.enabled': '启用',
      'status.disabled': '禁用',
      'operation.search': '搜索',
      'operation.reset': '重置',
      'operation.create': '新建',
      'operation.edit': '编辑',
      'operation.delete': '删除',
      'operation.view': '查看',
      'operation.test': '测试',
      'confirm.delete': '确定要删除这个模板吗？',
      'message.success': '操作成功',
      'message.delete.success': '删除成功',
      'error.template.not.found': '模板不存在',
      'error.template.code.exists': '模板代码已存在',
      'error.network': '网络连接失败',
      'error.server': '服务器错误',
      'error.unauthorized': '未授权访问',
      'error.forbidden': '权限不足',
      'error.not.found': '资源不存在'
    }
  },
  en: {
    translation: {
      'template.management': 'Template Management',
      'template.create': 'Create Template',
      'template.edit': 'Edit Template',
      'template.delete': 'Delete Template',
      'template.code': 'Template Code',
      'template.name': 'Template Name',
      'channel.inapp': 'In-App',
      'channel.sms': 'SMS',
      'channel.email': 'Email',
      'channel.im': 'IM',
      'status.enabled': 'Enabled',
      'status.disabled': 'Disabled',
      'operation.search': 'Search',
      'operation.reset': 'Reset',
      'operation.create': 'Create',
      'operation.edit': 'Edit',
      'operation.delete': 'Delete',
      'operation.view': 'View',
      'operation.test': 'Test',
      'confirm.delete': 'Are you sure to delete this template?',
      'message.success': 'Operation successful',
      'message.delete.success': 'Deleted successfully',
      'error.template.not.found': 'Template not found',
      'error.template.code.exists': 'Template code already exists',
      'error.network': 'Network connection failed',
      'error.server': 'Server error',
      'error.unauthorized': 'Unauthorized access',
      'error.forbidden': 'Insufficient permissions',
      'error.not.found': 'Resource not found'
    }
  }
};

i18n
  .use(initReactI18next)
  .init({
    resources,
    lng: localStorage.getItem('language') || 'zh',
    fallbackLng: 'zh',
    interpolation: {
      escapeValue: false
    }
  });

export { i18n };
```

### 2. 使用国际化
```typescript
// 在组件中使用
import { useTranslation } from 'react-i18next';

const TemplateManagement: React.FC = () => {
  const { t, i18n } = useTranslation();
  
  // 切换语言
  const changeLanguage = (lang: string) => {
    i18n.changeLanguage(lang);
    localStorage.setItem('language', lang);
  };
  
  return (
    <div>
      <h1>{t('template.management')}</h1>
      <Button onClick={() => changeLanguage('en')}>English</Button>
      <Button onClick={() => changeLanguage('zh')}>中文</Button>
    </div>
  );
};
```

## 📱 **响应式设计实现**

### 1. CSS媒体查询
```css
/* styles/responsive.css */
.template-management {
  padding: 24px;
}

/* 平板适配 */
@media (max-width: 1024px) {
  .template-management {
    padding: 16px;
  }
  
  .toolbar {
    flex-direction: column;
    gap: 16px;
  }
  
  .search-form {
    flex-wrap: wrap;
  }
}

/* 手机适配 */
@media (max-width: 768px) {
  .template-management {
    padding: 12px;
  }
  
  .table-container {
    overflow-x: auto;
  }
  
  .table {
    min-width: 800px;
  }
  
  /* 表格转卡片布局 */
  .table-mobile {
    display: none;
  }
  
  .card-list {
    display: block;
  }
  
  .template-card {
    background: white;
    border-radius: 8px;
    padding: 16px;
    margin-bottom: 12px;
    box-shadow: 0 2px 8px rgba(0,0,0,0.1);
  }
}
```

### 2. 响应式组件
```typescript
// components/ResponsiveTable.tsx
import { useMediaQuery } from '@/hooks/useMediaQuery';

interface ResponsiveTableProps {
  data: TemplateDto[];
  columns: any[];
}

const ResponsiveTable: React.FC<ResponsiveTableProps> = ({ data, columns }) => {
  const isMobile = useMediaQuery('(max-width: 768px)');
  
  if (isMobile) {
    return (
      <div className="card-list">
        {data.map(item => (
          <div key={item.id} className="template-card">
            <div className="card-header">
              <h3>{item.templateName}</h3>
              <span className={`status ${item.isEnabled ? 'enabled' : 'disabled'}`}>
                {item.isEnabled ? t('status.enabled') : t('status.disabled')}
              </span>
            </div>
            <div className="card-content">
              <p><strong>{t('template.code')}:</strong> {item.templateCode}</p>
              <p><strong>{t('channel')}:</strong> {item.channelCode}</p>
              <p><strong>{t('created.at')}:</strong> {item.createdAt}</p>
            </div>
            <div className="card-actions">
              <Button size="small">{t('operation.view')}</Button>
              <Button size="small">{t('operation.edit')}</Button>
              <Button size="small">{t('operation.delete')}</Button>
            </div>
          </div>
        ))}
      </div>
    );
  }
  
  return <Table dataSource={data} columns={columns} />;
};
```

## 🎨 **UI组件库使用**

### 1. Ant Design示例
```typescript
// pages/Template/TemplateList.tsx
import { Table, Button, Form, Input, Select, Modal, message } from 'antd';
import { PlusOutlined, SearchOutlined, ReloadOutlined } from '@ant-design/icons';

const TemplateList: React.FC = () => {
  const [form] = Form.useForm();
  const [loading, setLoading] = useState(false);
  const [data, setData] = useState<TemplateDto[]>([]);
  
  const columns = [
    {
      title: t('template.code'),
      dataIndex: 'templateCode',
      key: 'templateCode',
    },
    {
      title: t('template.name'),
      dataIndex: 'templateName',
      key: 'templateName',
    },
    {
      title: t('channel'),
      dataIndex: 'channelCode',
      key: 'channelCode',
      render: (value: string) => t(`channel.${value.toLowerCase()}`),
    },
    {
      title: t('status'),
      dataIndex: 'isEnabled',
      key: 'isEnabled',
      render: (enabled: boolean) => (
        <span className={`status-tag ${enabled ? 'enabled' : 'disabled'}`}>
          {t(enabled ? 'status.enabled' : 'status.disabled')}
        </span>
      ),
    },
    {
      title: t('operation'),
      key: 'action',
      render: (_, record: TemplateDto) => (
        <div className="action-buttons">
          <Button type="link" size="small" onClick={() => handleView(record)}>
            {t('operation.view')}
          </Button>
          <Button type="link" size="small" onClick={() => handleEdit(record)}>
            {t('operation.edit')}
          </Button>
          <Button type="link" size="small" onClick={() => handleTest(record)}>
            {t('operation.test')}
          </Button>
          <Button type="link" size="small" danger onClick={() => handleDelete(record)}>
            {t('operation.delete')}
          </Button>
        </div>
      ),
    },
  ];
  
  return (
    <div className="template-list">
      <div className="page-header">
        <h1>{t('template.management')}</h1>
      </div>
      
      <div className="toolbar">
        <Form form={form} layout="inline" onFinish={handleSearch}>
          <Form.Item name="templateCode" label={t('template.code')}>
            <Input placeholder={t('placeholder.template.code')} />
          </Form.Item>
          <Form.Item name="channelCode" label={t('channel')}>
            <Select placeholder={t('placeholder.channel')} allowClear>
              <Select.Option value="IN_APP">{t('channel.inapp')}</Select.Option>
              <Select.Option value="SMS">{t('channel.sms')}</Select.Option>
              <Select.Option value="EMAIL">{t('channel.email')}</Select.Option>
              <Select.Option value="IM">{t('channel.im')}</Select.Option>
            </Select>
          </Form.Item>
          <Form.Item>
            <Button type="primary" htmlType="submit" icon={<SearchOutlined />}>
              {t('operation.search')}
            </Button>
          </Form.Item>
          <Form.Item>
            <Button icon={<ReloadOutlined />} onClick={handleReset}>
              {t('operation.reset')}
            </Button>
          </Form.Item>
        </Form>
        
        <Button type="primary" icon={<PlusOutlined />} onClick={handleCreate}>
          {t('template.create')}
        </Button>
      </div>
      
      <Table
        columns={columns}
        dataSource={data}
        loading={loading}
        rowKey="id"
        pagination={{
          showSizeChanger: true,
          showQuickJumper: true,
          showTotal: (total, range) => 
            t('pagination.total', { total, start: range[0], end: range[1] })
        }}
      />
    </div>
  );
};
```

## 🚀 **部署和构建**

### 1. 环境配置
```bash
# .env.development
REACT_APP_API_BASE_URL=http://localhost:8081/notification-admin
REACT_APP_ENV=development

# .env.production
REACT_APP_API_BASE_URL=https://admin.company.com/notification-admin
REACT_APP_ENV=production
```

### 2. 构建脚本
```json
{
  "scripts": {
    "dev": "vite",
    "build": "vite build",
    "build:test": "vite build --mode test",
    "preview": "vite preview",
    "lint": "eslint src --ext .ts,.tsx",
    "lint:fix": "eslint src --ext .ts,.tsx --fix"
  }
}
```

这份前端开发指南为开发团队提供了完整的开发规范和最佳实践，确保前后端协作顺畅，代码质量高，用户体验好。
