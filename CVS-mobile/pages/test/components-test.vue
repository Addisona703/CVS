<template>
    <view class="test-page">
        <view class="container">
            <view class="section">
                <text class="section-title">按钮组件测试</text>
                <view class="button-group">
                    <u-button type="primary" text="主要按钮" :custom-style="{ width: '100%' }"></u-button>
                    <u-button type="default" text="次要按钮" :custom-style="{ width: '100%' }"></u-button>
                    <u-button type="error" text="危险按钮" :custom-style="{ width: '100%' }"></u-button>
                    <u-button type="primary" text="禁用按钮" disabled :custom-style="{ width: '100%' }"></u-button>
                </view>
            </view>

            <view class="section">
                <text class="section-title">输入框组件测试</text>
                <view class="input-group">
                    <u-form-item label="用户名">
                        <u-input placeholder="请输入用户名" border="surround"></u-input>
                    </u-form-item>
                    <u-form-item label="密码">
                        <u-input type="password" placeholder="请输入密码" border="surround"></u-input>
                    </u-form-item>
                    <u-form-item label="手机号">
                        <u-input type="number" placeholder="请输入手机号" border="surround"></u-input>
                    </u-form-item>
                </view>
            </view>

            <view class="section">
                <text class="section-title">卡片组件测试</text>
                <view class="card-demo">
                    <view class="card-header">
                        <text class="card-title">活动标题</text>
                        <text class="card-desc">这是活动的描述信息，可以包含多行文本内容。</text>
                    </view>
                    <view class="card-footer">
                        <u-button type="primary" text="报名" size="small"></u-button>
                    </view>
                </view>
            </view>

            <view class="section">
                <text class="section-title">单元格组件测试</text>
                <u-cell-group>
                    <u-cell title="单元格标题" value="说明文字" :is-link="true"></u-cell>
                    <u-cell title="带图标" icon="account" :is-link="true"></u-cell>
                    <u-cell title="带右侧内容" value="详细信息"></u-cell>
                </u-cell-group>
            </view>

            <view class="section">
                <text class="section-title">图标组件测试</text>
                <view class="icon-group">
                    <u-icon name="account" size="48"></u-icon>
                    <u-icon name="home" size="48"></u-icon>
                    <u-icon name="setting" size="48"></u-icon>
                    <u-icon name="bell" size="48"></u-icon>
                </view>
            </view>

            <view class="section">
                <text class="section-title">加载组件测试</text>
                <view class="loading-group">
                    <u-loading mode="circle" size="40"></u-loading>
                    <u-loading mode="spinner" size="40"></u-loading>
                </view>
            </view>

            <view class="section">
                <text class="section-title">引导页测试</text>
                <view class="button-group">
                    <u-button type="primary" text="预览引导页" @click="previewOnboarding" :custom-style="{ width: '100%' }"></u-button>
                    <u-button type="default" text="重置引导页状态" @click="resetOnboarding" :custom-style="{ width: '100%' }"></u-button>
                </view>
            </view>
        </view>
    </view>
</template>

<script>
import { resetOnboarding } from '@/utils/onboarding'

export default {
    data() {
        return {}
    },
    methods: {
        showToast() {
            uni.showToast({
                title: 'uView 组件测试',
                icon: 'success'
            })
        },
        previewOnboarding() {
            uni.navigateTo({
                url: '/pages/common/onboarding/onboarding'
            })
        },
        resetOnboarding() {
            resetOnboarding()
            uni.showToast({
                title: '引导页状态已重置',
                icon: 'success'
            })
        }
    }
}
</script>

<style lang="scss" scoped>
.test-page {
    min-height: 100vh;
    background-color: $uni-bg-color-grey;
}

.container {
    padding: $uni-spacing-md;
}

.section {
    margin-bottom: $uni-spacing-lg;

    .section-title {
        display: block;
        font-size: $uni-font-size-lg;
        font-weight: bold;
        color: $uni-text-color;
        margin-bottom: $uni-spacing-md;
    }
}

.button-group {
    display: flex;
    flex-direction: column;
    gap: $uni-spacing-sm;
}

.input-group {
    background-color: $uni-bg-color;
    border-radius: $uni-border-radius-base;
    padding: $uni-spacing-md;
}

.card-demo {
    background-color: $uni-bg-color;
    border-radius: $uni-border-radius-base;
    padding: $uni-spacing-lg;
    box-shadow: $uni-shadow-card;

    .card-header {
        margin-bottom: $uni-spacing-md;

        .card-title {
            display: block;
            font-size: $uni-font-size-lg;
            font-weight: bold;
            color: $uni-text-color;
            margin-bottom: $uni-spacing-xs;
        }

        .card-desc {
            display: block;
            font-size: $uni-font-size-sm;
            color: $uni-text-color-secondary;
            line-height: 1.6;
        }
    }

    .card-footer {
        display: flex;
        justify-content: flex-end;
    }
}

.icon-group {
    display: flex;
    gap: $uni-spacing-lg;
    padding: $uni-spacing-md;
    background-color: $uni-bg-color;
    border-radius: $uni-border-radius-base;
}

.loading-group {
    display: flex;
    gap: $uni-spacing-lg;
    padding: $uni-spacing-md;
    background-color: $uni-bg-color;
    border-radius: $uni-border-radius-base;
}
</style>
