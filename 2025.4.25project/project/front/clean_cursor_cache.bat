@echo off
echo Cursor缓存清理工具
echo =====================
echo.

REM 显示开始消息
echo 准备清理Cursor缓存文件，以解决卡顿问题...
echo.
echo 请确保已经保存了所有重要的工作！
echo.
pause

REM 关闭所有运行中的Cursor实例
echo 正在关闭所有Cursor实例...
taskkill /f /im "Cursor.exe" 2>nul
if %errorlevel% equ 0 (
    echo Cursor已成功关闭。
) else (
    echo 未检测到运行中的Cursor实例，继续清理。
)
echo.

REM 设置缓存文件路径
set APPDATA_CACHE=%APPDATA%\Cursor
set LOCALAPPDATA_CACHE=%LOCALAPPDATA%\Cursor
set USERPROFILE_CACHE=%USERPROFILE%\.cursor

REM 清理AppData中的Cursor缓存
echo 正在清理AppData\Roaming中的Cursor缓存...
if exist "%APPDATA_CACHE%" (
    rmdir /s /q "%APPDATA_CACHE%\Cache"
    rmdir /s /q "%APPDATA_CACHE%\Code Cache"
    rmdir /s /q "%APPDATA_CACHE%\GPUCache"
    rmdir /s /q "%APPDATA_CACHE%\CachedData"
    rmdir /s /q "%APPDATA_CACHE%\CachedExtensions"
    rmdir /s /q "%APPDATA_CACHE%\logs"
    del /q "%APPDATA_CACHE%\Cookies*" 2>nul
    del /q "%APPDATA_CACHE%\*.log" 2>nul
    echo AppData\Roaming缓存清理完成。
) else (
    echo 未找到AppData\Roaming中的Cursor缓存文件夹。
)
echo.

REM 清理LocalAppData中的Cursor缓存
echo 正在清理LocalAppData中的Cursor缓存...
if exist "%LOCALAPPDATA_CACHE%" (
    rmdir /s /q "%LOCALAPPDATA_CACHE%\Cache"
    rmdir /s /q "%LOCALAPPDATA_CACHE%\Code Cache"
    rmdir /s /q "%LOCALAPPDATA_CACHE%\GPUCache"
    rmdir /s /q "%LOCALAPPDATA_CACHE%\logs"
    del /q "%LOCALAPPDATA_CACHE%\Cookies*" 2>nul
    del /q "%LOCALAPPDATA_CACHE%\*.log" 2>nul
    echo LocalAppData缓存清理完成。
) else (
    echo 未找到LocalAppData中的Cursor缓存文件夹。
)
echo.

REM 清理用户目录下的.cursor文件夹
echo 正在清理用户目录下的.cursor缓存...
if exist "%USERPROFILE_CACHE%" (
    rmdir /s /q "%USERPROFILE_CACHE%\Cache"
    rmdir /s /q "%USERPROFILE_CACHE%\GPUCache"
    rmdir /s /q "%USERPROFILE_CACHE%\logs"
    del /q "%USERPROFILE_CACHE%\*.log" 2>nul
    echo 用户目录缓存清理完成。
) else (
    echo 未找到用户目录下的.cursor缓存文件夹。
)
echo.

REM 清理VSCode集成相关缓存（如果存在）
echo 正在清理VSCode相关缓存...
set VSCODE_CURSOR_CACHE=%USERPROFILE%\.vscode\extensions\cursor-sa.cursor
if exist "%VSCODE_CURSOR_CACHE%" (
    rmdir /s /q "%VSCODE_CURSOR_CACHE%\dist\cache"
    echo VSCode相关缓存清理完成。
) else (
    echo 未找到VSCode相关的Cursor缓存。
)
echo.

echo 所有缓存清理操作已完成！
echo 请重新启动Cursor以获得更好的性能体验。
echo.
pause

REM 询问是否自动启动Cursor
echo 是否要立即启动Cursor？(Y/N)
choice /c YN /m ""
if %errorlevel% equ 1 (
    echo 正在启动Cursor...
    start "" "%LOCALAPPDATA%\Programs\Cursor\Cursor.exe"
) else (
    echo 请手动启动Cursor。
)
echo.
echo 感谢使用缓存清理工具！
timeout /t 3
exit 