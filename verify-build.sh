#!/bin/bash

# Experience Bar Fix - 构建验证脚本
# 此脚本用于验证构建产物是否符合预期

echo "=== Experience Bar Fix 构建验证 ==="

# 读取版本信息
if [ -f "gradle.properties" ]; then
    MOD_VERSION=$(grep "mod_version" gradle.properties | cut -d'=' -f2)
    echo "✓ 从 gradle.properties 读取版本: $MOD_VERSION"
else
    echo "❌ 无法找到 gradle.properties 文件"
    exit 1
fi

# 读取 archivesBaseName
if [ -f "build.gradle" ]; then
    ARCHIVES_BASE_NAME=$(grep "archivesBaseName" build.gradle | cut -d"'" -f2)
    echo "✓ 从 build.gradle 读取基础名称: $ARCHIVES_BASE_NAME"
else
    echo "❌ 无法找到 build.gradle 文件"
    exit 1
fi

# 构建预期的文件名
EXPECTED_JAR="$ARCHIVES_BASE_NAME-$MOD_VERSION.jar"
EXPECTED_PATH="build/libs/$EXPECTED_JAR"

echo "✓ 预期的 JAR 文件: $EXPECTED_JAR"
echo "✓ 预期的完整路径: $EXPECTED_PATH"

# 检查构建目录
if [ -d "build/libs" ]; then
    echo "✓ 构建目录存在"
    echo "当前构建产物:"
    ls -la build/libs/*.jar 2>/dev/null || echo "  (无 JAR 文件)"
    
    # 检查预期文件是否存在
    if [ -f "$EXPECTED_PATH" ]; then
        echo "✅ 找到预期的 JAR 文件: $EXPECTED_JAR"
        
        # 获取文件大小
        FILE_SIZE=$(stat -f%z "$EXPECTED_PATH" 2>/dev/null || stat -c%s "$EXPECTED_PATH" 2>/dev/null)
        echo "   文件大小: $FILE_SIZE 字节"
        
        # 检查 JAR 文件内容
        if command -v jar >/dev/null 2>&1; then
            echo "   检查 JAR 文件内容..."
            jar tf "$EXPECTED_PATH" | grep -E "(mods\.toml|ExperienceBarFixMod\.class)" | head -5
        fi
    else
        echo "❌ 未找到预期的 JAR 文件: $EXPECTED_JAR"
        echo "可能的原因:"
        echo "  - 构建尚未完成"
        echo "  - 版本号配置不匹配"
        echo "  - archivesBaseName 配置错误"
    fi
else
    echo "❌ 构建目录不存在，请先执行构建"
fi

# 检查 Git 标签
echo ""
echo "=== Git 标签检查 ==="
EXPECTED_TAG="v$MOD_VERSION"
if git tag -l | grep -q "$EXPECTED_TAG"; then
    echo "✓ 找到匹配的 Git 标签: $EXPECTED_TAG"
    
    # 检查标签是否指向当前提交
    TAG_COMMIT=$(git rev-parse "$EXPECTED_TAG")
    CURRENT_COMMIT=$(git rev-parse HEAD)
    
    if [ "$TAG_COMMIT" = "$CURRENT_COMMIT" ]; then
        echo "✅ 标签指向当前提交"
    else
        echo "⚠️  标签未指向当前提交"
        echo "   标签提交: $TAG_COMMIT"
        echo "   当前提交: $CURRENT_COMMIT"
    fi
else
    echo "⚠️  未找到匹配的 Git 标签: $EXPECTED_TAG"
    echo "现有标签:"
    git tag -l | sed 's/^/   /'
fi

echo ""
echo "=== 验证完成 ==="
