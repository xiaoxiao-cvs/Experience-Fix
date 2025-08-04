@echo off
setlocal enabledelayedexpansion

REM Experience Bar Fix - 构建验证脚本 (Windows版)
REM 此脚本用于验证构建产物是否符合预期

echo === Experience Bar Fix 构建验证 ===

REM 读取版本信息
if exist "gradle.properties" (
    for /f "tokens=2 delims==" %%a in ('findstr "mod_version" gradle.properties') do set MOD_VERSION=%%a
    echo ✓ 从 gradle.properties 读取版本: !MOD_VERSION!
) else (
    echo ❌ 无法找到 gradle.properties 文件
    exit /b 1
)

REM 读取 archivesBaseName
if exist "build.gradle" (
    for /f "tokens=2 delims='" %%a in ('findstr "archivesBaseName" build.gradle') do set ARCHIVES_BASE_NAME=%%a
    echo ✓ 从 build.gradle 读取基础名称: !ARCHIVES_BASE_NAME!
) else (
    echo ❌ 无法找到 build.gradle 文件
    exit /b 1
)

REM 构建预期的文件名
set EXPECTED_JAR=!ARCHIVES_BASE_NAME!-!MOD_VERSION!.jar
set EXPECTED_PATH=build\libs\!EXPECTED_JAR!

echo ✓ 预期的 JAR 文件: !EXPECTED_JAR!
echo ✓ 预期的完整路径: !EXPECTED_PATH!

REM 检查构建目录
if exist "build\libs" (
    echo ✓ 构建目录存在
    echo 当前构建产物:
    if exist "build\libs\*.jar" (
        dir /b build\libs\*.jar
    ) else (
        echo   ^(无 JAR 文件^)
    )
    
    REM 检查预期文件是否存在
    if exist "!EXPECTED_PATH!" (
        echo ✅ 找到预期的 JAR 文件: !EXPECTED_JAR!
        
        REM 获取文件大小
        for %%F in ("!EXPECTED_PATH!") do set FILE_SIZE=%%~zF
        echo    文件大小: !FILE_SIZE! 字节
    ) else (
        echo ❌ 未找到预期的 JAR 文件: !EXPECTED_JAR!
        echo 可能的原因:
        echo   - 构建尚未完成
        echo   - 版本号配置不匹配
        echo   - archivesBaseName 配置错误
    )
) else (
    echo ❌ 构建目录不存在，请先执行构建
)

REM 检查 Git 标签
echo.
echo === Git 标签检查 ===
set EXPECTED_TAG=v!MOD_VERSION!

REM 检查是否有匹配的标签
git tag -l | findstr "!EXPECTED_TAG!" >nul
if !errorlevel! equ 0 (
    echo ✓ 找到匹配的 Git 标签: !EXPECTED_TAG!
    
    REM 检查标签是否指向当前提交
    for /f %%a in ('git rev-parse !EXPECTED_TAG!') do set TAG_COMMIT=%%a
    for /f %%a in ('git rev-parse HEAD') do set CURRENT_COMMIT=%%a
    
    if "!TAG_COMMIT!"=="!CURRENT_COMMIT!" (
        echo ✅ 标签指向当前提交
    ) else (
        echo ⚠️  标签未指向当前提交
        echo    标签提交: !TAG_COMMIT!
        echo    当前提交: !CURRENT_COMMIT!
    )
) else (
    echo ⚠️  未找到匹配的 Git 标签: !EXPECTED_TAG!
    echo 现有标签:
    git tag -l
)

echo.
echo === 验证完成 ===
pause
