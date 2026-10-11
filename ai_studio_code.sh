# 1. Stage all files including the 52MB native APK
git add .
git add -f JARVIS-AI.apk
git commit -m "feat: Add native Android APK and automated GitHub Actions release"

# 2. Push to your repository
git push -u origin main
