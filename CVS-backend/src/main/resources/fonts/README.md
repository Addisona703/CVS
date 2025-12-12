# 中文字体文件

为了支持PDF证书中的中文显示,需要在此目录放置中文字体文件。

## 推荐字体

1. **simhei.ttf** (黑体) - 推荐
2. **simsun.ttc** (宋体)
3. **msyh.ttf** (微软雅黑)

## 获取字体

### Windows系统
从 `C:\Windows\Fonts\` 目录复制字体文件到此目录

### Linux系统
```bash
# 安装字体
sudo apt-get install fonts-wqy-zenhei

# 或从系统字体目录复制
cp /usr/share/fonts/truetype/wqy/wqy-zenhei.ttc ./
```

### Mac系统
从 `/System/Library/Fonts/` 或 `/Library/Fonts/` 复制字体文件

## 注意事项

- 字体文件较大(通常10-20MB),不建议提交到Git仓库
- 已在 .gitignore 中忽略 *.ttf 和 *.ttc 文件
- 如果没有字体文件,系统会尝试使用系统字体
