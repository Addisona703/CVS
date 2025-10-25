<template>
  <div class="auth-layout">
    <div class="auth-wrapper">
      <section class="visual-pane">
        <div class="visual-placeholder"></div>
        <div class="visual-caption">
          <h1>学生志愿服务数字化</h1>
          <p>Connecting campuses with impactful volunteer opportunities.</p>
        </div>
      </section>

      <section class="form-pane">
        <div class="auth-card">
          <router-view />
        </div>
        <footer class="auth-footer">
          &copy; 2025 CVS系统 · All rights reserved.
        </footer>
      </section>
    </div>
  </div>
</template>

<script setup>
import { School } from '@element-plus/icons-vue'
</script>

<style lang="scss" scoped>
.auth-layout {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: clamp(32px, 6vw, 64px);
  background: #f3f4f6;
  font-family: var(--font-family-base);
  color: #0f172a;
  background-size: cover;
  background-image: url('@/assets/images/Wallpaper3.jpg');
}

.auth-wrapper {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: clamp(32px, 6vw, 72px);
  width: min(1280px, 92vw);
  height: clamp(620px, 78vh, 760px);
  background: transparent;
}

.visual-pane {
  display: flex;
  flex-direction: column;
  justify-content: center;
  align-items: center;
  text-align: center;
  gap: clamp(24px, 3vw, 32px);
  padding: clamp(24px, 4vw, 48px);
}

.visual-placeholder {
  width: min(420px, 100%);
  height: clamp(260px, 40vh, 340px);
  border-radius: 28px;
  background-image: url('@/assets/images/AuthIkon.jpg');
  background-repeat: no-repeat;
  background-size: cover;
  background-position: center;
  box-shadow: 0 20px 45px rgba(15, 23, 42, 0.22);
}

.visual-caption {
  display: flex;
  flex-direction: column;
  gap: 12px;
  align-items: center;
  text-align: center;
  max-width: 420px;
}

.visual-caption h1 {
  margin: 0;
  font-size: clamp(30px, 3.6vw, 42px);
  font-weight: 700;
  color: #1f2937;
}

.visual-caption p {
  margin: 0;
  font-size: clamp(15px, 2vw, 17px);
  line-height: 1.6;
  color: #374151;
}

.form-pane {
  display: flex;
  flex-direction: column;
  justify-content: center;
  gap: 24px;
  padding: clamp(24px, 4vw, 48px);
}

.auth-card {
  width: 100%;
  max-width: clamp(420px, 36vw, 520px);
  background: #ffffff;
  border-radius: 24px;
  box-shadow: 0 30px 60px rgba(15, 23, 42, 0.1);
  border: 1px solid rgba(148, 163, 184, 0.2);
  padding: clamp(28px, 4vw, 36px);
  overflow: hidden auto;
}

.auth-footer {
  font-size: 13px;
  color: #94a3b8;
  text-align: center;
}

:deep(.el-form) {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

:deep(.el-form-item) {
  margin-bottom: 0;
}

:deep(.el-input__wrapper),
:deep(.el-select .el-input__wrapper) {
  border-radius: 16px;
  border: 1px solid rgba(148, 163, 184, 0.4);
  padding: 12px 16px;
  background-color: #f9fafb;
  transition: border-color 0.2s ease, box-shadow 0.2s ease;

  &:hover {
    border-color: rgba(37, 99, 235, 0.55);
  }

  &.is-focus {
    border-color: rgba(37, 99, 235, 0.85);
    box-shadow: 0 12px 24px rgba(37, 99, 235, 0.15);
  }
}

:deep(.el-checkbox) {
  font-size: 14px;
  color: #4b5563;
}

:deep(.el-button.is-primary) {
  border-radius: 18px;
  font-weight: 600;
  background: #2563eb;
  border: none;
  box-shadow: 0 16px 32px rgba(37, 99, 235, 0.22);
  transition: transform 0.2s ease, box-shadow 0.2s ease;

  &:hover {
    transform: translateY(-1px);
    box-shadow: 0 20px 40px rgba(29, 78, 216, 0.28);
  }

  &:active {
    transform: translateY(1px);
  }
}

@media (max-width: 1024px) {
  .auth-wrapper {
    grid-template-columns: 1fr;
    gap: 32px;
    height: auto;
  }

  .visual-pane {
    padding-bottom: 0;
  }

  .form-pane {
    align-items: center;
    padding-top: 0;
  }
}

@media (max-width: 640px) {
  .auth-layout {
    padding: 24px;
  }

  .auth-card {
    width: 100%;
    padding: 24px;
    border-radius: 20px;
  }
}
</style>
