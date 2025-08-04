# GitHub Actions 使用指南

## 概述

本项目配置了多个 GitHub Actions 工作流来自动化构建、测试、发布和安全扫描流程。

## 工作流说明

### 1. 构建和发布 (build-and-release.yml)

#### 自动触发
- **推送到 main/develop 分支**: 自动构建开发版本
  - 版本格式: `1.0.0-dev+短commit哈希`
  - 自动创建预发布版本
  - 保留最新的5个开发版本，自动清理旧版本

- **拉取请求**: 仅构建，不发布

#### 手动触发
- 在 GitHub Actions 页面手动触发
- 可选择版本类型: patch/minor/major
- 可选择是否为预发布版本
- 使用 gradle.properties 中的版本号创建正式发布

### 2. 版本管理 (version-bump.yml)

手动触发的版本号管理工具：

- **功能**:
  - 自动计算新版本号（遵循语义化版本）
  - 更新 `gradle.properties` 中的版本
  - 更新 `CHANGELOG.md`
  - 创建版本标签
  - 提交并推送更改

- **使用方法**:
  1. 在 GitHub Actions 页面手动触发
  2. 选择版本增量类型（patch/minor/major）
  3. 可选择自定义提交信息

### 3. 安全扫描 (security-scan.yml)

自动安全检查流程：

- **触发条件**:
  - 推送到 main/develop 分支
  - 拉取请求
  - 每周一上午9点（UTC）定时扫描
  - 手动触发

- **扫描内容**:
  - 依赖项漏洞扫描
  - CodeQL 代码安全分析

## 版本号规范

项目采用语义化版本号 (Semantic Versioning)：

- **格式**: `MAJOR.MINOR.PATCH`
- **MAJOR**: 不兼容的API变更
- **MINOR**: 向后兼容的功能性新增
- **PATCH**: 向后兼容的问题修正

### 开发版本
- **格式**: `MAJOR.MINOR.PATCH-dev+COMMIT_HASH`
- **示例**: `1.0.0-dev+a1b2c3d`

## 发布流程

### 正式版本发布

1. **更新版本号**:
   ```bash
   # 方法1: 使用 GitHub Actions
   # 在 Actions 页面手动触发 "Version Management" 工作流
   
   # 方法2: 手动更新
   # 编辑 gradle.properties 中的 mod_version
   ```

2. **创建发布**:
   - 在 GitHub Actions 页面手动触发 "Build and Release" 工作流
   - 选择适当的版本类型
   - 选择是否为预发布版本

3. **验证发布**:
   - 检查生成的 JAR 文件
   - 验证发布说明
   - 测试下载链接

### 开发版本发布

开发版本会在每次推送到 main 或 develop 分支时自动创建。

## 配置说明

### 依赖项安全扫描

- 配置文件: `dependency-check-suppressions.xml`
- 用于抑制误报的安全警告
- 仅在确认漏洞不适用时才添加抑制规则

### 环境要求

- Java 17
- Gradle 7.0+
- Minecraft 1.20.1
- Forge 47.2.0+

## 故障排除

### 常见问题

1. **构建失败**:
   - 检查 Java 版本兼容性
   - 验证依赖项是否可用
   - 查看构建日志中的错误信息

2. **版本号问题**:
   - 确保 gradle.properties 中的版本号格式正确
   - 检查是否有语法错误

3. **权限问题**:
   - 确保 GitHub token 有足够权限
   - 检查仓库设置

### 获取帮助

如果遇到问题，请：
1. 查看 GitHub Actions 运行日志
2. 检查相关配置文件
3. 创建 Issue 描述问题

## 最佳实践

1. **提交信息**: 使用清晰、描述性的提交信息
2. **分支管理**: 
   - `main`: 稳定版本
   - `develop`: 开发版本
   - 功能分支: `feature/功能名称`
3. **测试**: 在提交前进行本地测试
4. **文档**: 及时更新 CHANGELOG.md
