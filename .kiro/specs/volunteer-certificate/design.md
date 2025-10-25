# Design Document - 志愿证书申请与生成功能

## Overview

本设计文档描述了志愿证书申请与生成功能的技术实现方案。该功能基于现有的CVS系统架构，扩展证书管理模块，增加PDF证书生成能力。系统将验证学生的志愿服务资格，通过管理员审核后，自动生成包含个人信息的PDF证书供学生下载。

核心流程：
1. 学生提交证书申请 → 系统验证资格（必须有已完成的志愿服务）
2. 管理员审核申请 → 通过/拒绝
3. 审核通过后 → 学生可预览和下载PDF证书

## Architecture

### 系统架构图

```
┌─────────────────────────────────────────────────────────────┐
│                        前端层 (Vue3)                         │
│  ┌──────────────┐  ┌──────────────┐  ┌──────────────┐      │
│  │ 学生证书页面  │  │ 管理员审核页面│  │ 证书预览组件  │      │
│  └──────────────┘  └──────────────┘  └──────────────┘      │
└─────────────────────────────────────────────────────────────┘
                            │ HTTP/REST API
┌─────────────────────────────────────────────────────────────┐
│                        后端层 (Spring Boot)                  │
│  ┌──────────────────────────────────────────────────────┐   │
│  │          CertificateController (已存在)               │   │
│  │  + previewCertificate()  (新增)                      │   │
│  │  + downloadCertificate() (新增)                      │   │
│  └──────────────────────────────────────────────────────┘   │
│  ┌──────────────────────────────────────────────────────┐   │
│  │          CertificateService (扩展)                    │   │
│  │  + validateEligibility()  (新增)                     │   │
│  │  + generateCertificatePdf() (新增)                   │   │
│  └──────────────────────────────────────────────────────┘   │
│  ┌──────────────────────────────────────────────────────┐   │
│  │          PdfService (新增)                            │   │
│  │  + fillPdfTemplate()                                 │   │
│  │  + generatePdfBytes()                                │   │
│  └──────────────────────────────────────────────────────┘   │
└─────────────────────────────────────────────────────────────┘
                            │
┌─────────────────────────────────────────────────────────────┐
│                    数据层 (MySQL)                            │
│  cert_twb (已存在)  │  signup_twb (已存在)                  │
└─────────────────────────────────────────────────────────────┘
```


### 技术选型

**PDF生成库选择：Apache PDFBox**

理由：
- 开源免费，Apache 2.0许可证
- 支持PDF模板填充（AcroForm表单字段）
- 纯Java实现，与Spring Boot无缝集成
- 文档完善，社区活跃
- 相比iText（商业许可）更适合本项目

依赖配置：
```xml
<dependency>
    <groupId>org.apache.pdfbox</groupId>
    <artifactId>pdfbox</artifactId>
    <version>2.0.29</version>
</dependency>
```

## Components and Interfaces

### 1. Controller层扩展

**CertificateController (扩展现有类)**

新增接口：

```java
@GetMapping("/{id}/preview")
@PreAuthorize("hasAnyRole('STUDENT', 'ADMIN')")
@Operation(summary = "预览志愿证书")
public ResponseEntity<byte[]> previewCertificate(
    @PathVariable Long id,
    @AuthenticationPrincipal UserPrincipal principal)

@GetMapping("/{id}/download")
@PreAuthorize("hasAnyRole('STUDENT', 'ADMIN')")
@Operation(summary = "下载志愿证书")
public ResponseEntity<byte[]> downloadCertificate(
    @PathVariable Long id,
    @AuthenticationPrincipal UserPrincipal principal)
```

职责：
- 验证用户权限（只能访问自己的证书，管理员可访问所有）
- 调用Service层生成PDF
- 设置正确的HTTP响应头（Content-Type, Content-Disposition）
- 返回PDF字节流


### 2. Service层设计

**CertificateService (扩展现有接口)**

新增方法：

```java
/**
 * 生成证书PDF
 * @param certificateId 证书ID
 * @return PDF字节数组
 */
byte[] generateCertificatePdf(Long certificateId);

/**
 * 获取证书统计信息（用于填充PDF或显示）
 * @param userId 用户ID
 * @return 统计信息（总时长、活动数等）
 */
CertificateStatistics getCertificateStatistics(Long userId);
```

**CertificateServiceImpl (扩展现有实现)**

核心逻辑：

1. **资格验证逻辑（基于累计服务小时数）**

使用策略模式设计，便于后续扩展和修改门槛规则：

```java
/**
 * 证书资格验证策略接口
 */
public interface CertificateEligibilityStrategy {
    /**
     * 验证用户是否有资格申请证书
     * @param userId 用户ID
     * @return 验证结果
     */
    EligibilityResult validate(Long userId);
}

/**
 * 验证结果
 */
@Data
public class EligibilityResult {
    private boolean eligible;
    private String message;
    private Map<String, Object> details;
}

/**
 * 基于服务小时数的资格验证策略（默认实现）
 */
@Component
public class ServiceHoursEligibilityStrategy implements CertificateEligibilityStrategy {
    
    @Autowired
    private SignupMapper signupMapper;
    
    @Autowired
    private ActivityMapper activityMapper;
    
    @Value("${certificate.eligibility.min-hours:2}")
    private double minRequiredHours;
    
    @Override
    public EligibilityResult validate(Long userId) {
        // 1. 查询所有已完成的志愿服务
        List<Signup> completedSignups = signupMapper.selectList(
            new LambdaQueryWrapper<Signup>()
                .eq(Signup::getUserId, userId)
                .isNotNull(Signup::getSignInTime)
                .isNotNull(Signup::getSignOutTime)
        );
        
        // 2. 计算累计服务小时数
        double totalHours = 0.0;
        for (Signup signup : completedSignups) {
            LocalDateTime signIn = signup.getSignInTime();
            LocalDateTime signOut = signup.getSignOutTime();
            if (signIn != null && signOut != null) {
                long minutes = Duration.between(signIn, signOut).toMinutes();
                totalHours += minutes / 60.0;
            }
        }
        
        // 3. 构建验证结果
        EligibilityResult result = new EligibilityResult();
        result.setEligible(totalHours >= minRequiredHours);
        
        Map<String, Object> details = new HashMap<>();
        details.put("totalHours", totalHours);
        details.put("minRequiredHours", minRequiredHours);
        details.put("completedActivities", completedSignups.size());
        result.setDetails(details);
        
        if (!result.isEligible()) {
            result.setMessage(String.format(
                "您的累计志愿服务时长为%.1f小时，需要至少%.1f小时才能申请证书",
                totalHours, minRequiredHours
            ));
        }
        
        return result;
    }
}
```

2. **申请证书时增加资格验证（使用AssertUtils工具类）**

```java
@Service
@RequiredArgsConstructor
public class CertificateServiceImpl extends ServiceImpl<CertificateMapper, Certificate> 
    implements CertificateService {
    
    private final CertificateMapper certificateMapper;
    private final UserMapper userMapper;
    private final SignupMapper signupMapper;
    private final PdfService pdfService;
    private final CertificateEligibilityStrategy eligibilityStrategy;
    
    @Override
    @Transactional
    public CertificateVO applyCertificate(CertificateCreateDTO request, Long userId) {
        // 1. 验证资格（使用策略模式）
        EligibilityResult eligibilityResult = eligibilityStrategy.validate(userId);
        AssertUtils.isTrue(eligibilityResult.isEligible(), eligibilityResult.getMessage());
        
        // 2. 检查是否有待审核的申请
        long pendingCount = certificateMapper.selectCount(
            new LambdaQueryWrapper<Certificate>()
                .eq(Certificate::getUserId, userId)
                .eq(Certificate::getStatus, CertificateStatus.PENDING)
        );
        AssertUtils.isTrue(pendingCount == 0, "您有待审核的证书申请，请等待审核完成");
        
        // 3. 生成证书编号
        String certificateNumber = generateCertificateNumber();
        
        // 4. 创建证书记录
        Certificate certificate = new Certificate();
        certificate.setUserId(userId);
        certificate.setPurpose(request.getPurpose());
        certificate.setStartDate(request.getStartDate());
        certificate.setEndDate(request.getEndDate());
        certificate.setStatus(CertificateStatus.PENDING);
        certificate.setCertificateNumber(certificateNumber);
        
        certificateMapper.insert(certificate);
        
        // 5. 返回VO
        return convertToVO(certificate);
    }
}
```


3. **PDF生成逻辑（使用AssertUtils）**
```java
@Override
public byte[] generateCertificatePdf(Long certificateId) {
    // 1. 获取证书记录
    Certificate certificate = certificateMapper.selectById(certificateId);
    AssertUtils.notNull(certificate, "证书不存在");
    
    // 2. 验证证书状态
    AssertUtils.isTrue(
        certificate.getStatus() == CertificateStatus.APPROVED,
        "证书尚未通过审核，无法生成"
    );
    
    // 3. 获取用户信息
    User user = userMapper.selectById(certificate.getUserId());
    AssertUtils.notNull(user, "用户不存在");
    
    // 4. 准备PDF数据
    Map<String, String> data = new HashMap<>();
    data.put("name", user.getName());
    data.put("username", user.getUsername());
    data.put("serial", certificate.getCertificateNumber());
    data.put("issueDate", formatDate(certificate.getApprovedAt()));
    
    // 5. 调用PdfService生成PDF
    return pdfService.generateCertificatePdf(data);
}

private String formatDate(LocalDateTime dateTime) {
    if (dateTime == null) return "";
    return dateTime.format(DateTimeFormatter.ofPattern("yyyy年MM月dd日"));
}
```

**PdfService (新增服务)**

```java
public interface PdfService {
    /**
     * 生成志愿证书PDF
     * @param data 证书数据（name, username, serial, issueDate）
     * @return PDF字节数组
     */
    byte[] generateCertificatePdf(Map<String, String> data);
}
```

**PdfServiceImpl (新增实现)**

```java
@Service
@Slf4j
public class PdfServiceImpl implements PdfService {
    
    @Value("${certificate.template.path:classpath:assets/cert_template.pdf}")
    private String templatePath;
    
    @Override
    public byte[] generateCertificatePdf(Map<String, String> data) {
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            // 1. 加载PDF模板
            PDDocument document = PDDocument.load(
                new ClassPathResource("assets/cert_template.pdf").getInputStream()
            );
            
            // 2. 获取PDF表单
            PDAcroForm acroForm = document.getDocumentCatalog().getAcroForm();
            if (acroForm == null) {
                throw new BusinessException("PDF模板不包含表单字段");
            }
            
            // 3. 填充表单字段
            fillFormField(acroForm, "name", data.get("name"));
            fillFormField(acroForm, "username", data.get("username"));
            fillFormField(acroForm, "serial", data.get("serial"));
            fillFormField(acroForm, "issueDate", data.get("issueDate"));
            
            // 4. 设置表单为只读（防止修改）
            acroForm.flatten();
            
            // 5. 保存到字节数组
            document.save(baos);
            document.close();
            
            return baos.toByteArray();
            
        } catch (IOException e) {
            log.error("生成PDF证书失败", e);
            AssertUtils.throwException("证书生成失败，请稍后重试");
            return null; // 不会执行到这里
        }
    }
    
    private void fillFormField(PDAcroForm acroForm, String fieldName, String value) {
        try {
            PDField field = acroForm.getField(fieldName);
            if (field != null) {
                field.setValue(value);
            } else {
                log.warn("PDF模板中未找到字段: {}", fieldName);
            }
        } catch (IOException e) {
            log.error("填充PDF字段失败: {}", fieldName, e);
        }
    }
}
```


### 3. 前端组件设计

**学生端 - 证书管理页面**

路径：`CVS-frontend/src/views/student/Certificate.vue`

功能：
- 显示证书申请列表（状态、申请时间、证书编号）
- 申请新证书按钮
- 预览证书（审核通过后）
- 下载证书（审核通过后）
- 查看拒绝原因（被拒绝时）

关键方法：
```javascript
// 申请证书
const applyCertificate = async () => {
  try {
    await certificateApi.apply({ purpose: '志愿服务证明' })
    ElMessage.success('申请成功，请等待审核')
    loadCertificates()
  } catch (error) {
    ElMessage.error(error.message || '申请失败')
  }
}

// 预览证书
const previewCertificate = async (id) => {
  try {
    const response = await certificateApi.preview(id)
    // 创建Blob对象并在新窗口打开
    const blob = new Blob([response.data], { type: 'application/pdf' })
    const url = window.URL.createObjectURL(blob)
    window.open(url)
  } catch (error) {
    ElMessage.error('预览失败')
  }
}

// 下载证书
const downloadCertificate = async (id, username) => {
  try {
    const response = await certificateApi.download(id)
    const blob = new Blob([response.data], { type: 'application/pdf' })
    const url = window.URL.createObjectURL(blob)
    const link = document.createElement('a')
    link.href = url
    link.download = `志愿服务证书-${username}.pdf`
    link.click()
    window.URL.revokeObjectURL(url)
  } catch (error) {
    ElMessage.error('下载失败')
  }
}
```

**管理员端 - 证书审核页面**

路径：`CVS-frontend/src/views/admin/CertificateReview.vue`

功能：
- 显示待审核证书列表
- 查看申请人信息和志愿服务记录
- 通过/拒绝审核
- 填写拒绝原因

关键方法：
```javascript
// 审核通过
const approveCertificate = async (id) => {
  try {
    await certificateApi.approve({ id, approved: true })
    ElMessage.success('审核通过')
    loadPendingCertificates()
  } catch (error) {
    ElMessage.error('操作失败')
  }
}

// 审核拒绝
const rejectCertificate = async (id, reason) => {
  try {
    await certificateApi.approve({ id, approved: false, rejectReason: reason })
    ElMessage.success('已拒绝申请')
    loadPendingCertificates()
  } catch (error) {
    ElMessage.error('操作失败')
  }
}
```


**API Service层**

路径：`CVS-frontend/src/api/certificate.js`

```javascript
import request from '@/utils/request'

export default {
  // 申请证书
  apply(data) {
    return request.post('/api/certificates', data)
  },
  
  // 获取我的证书列表
  getMyCertificates(params) {
    return request.get('/api/certificates/my', { params })
  },
  
  // 预览证书
  preview(id) {
    return request.get(`/api/certificates/${id}/preview`, {
      responseType: 'blob'
    })
  },
  
  // 下载证书
  download(id) {
    return request.get(`/api/certificates/${id}/download`, {
      responseType: 'blob'
    })
  },
  
  // 审核证书
  approve(data) {
    return request.post('/api/certificates/approve', data)
  },
  
  // 获取待审核证书列表
  getPendingCertificates(params) {
    return request.get('/api/certificates/pending', { params })
  }
}
```

## Data Models

### 数据库表结构

**cert_twb (已存在，无需修改)**

```sql
CREATE TABLE `cert_twb` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `user_id` bigint NOT NULL COMMENT '用户ID',
  `purpose` varchar(500) NOT NULL COMMENT '申请目的',
  `start_date` date NOT NULL COMMENT '服务开始日期',
  `end_date` date NOT NULL COMMENT '服务结束日期',
  `status` varchar(20) NOT NULL DEFAULT 'PENDING' COMMENT '证明状态',
  `reject_reason` text COMMENT '拒绝原因',
  `approver_id` bigint DEFAULT NULL COMMENT '审批人ID',
  `approved_at` datetime DEFAULT NULL COMMENT '审批时间',
  `certificate_number` varchar(50) DEFAULT NULL COMMENT '证明编号',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_status` (`status`),
  KEY `idx_certificate_number` (`certificate_number`)
);
```

### DTO对象

**CertificateStatistics (新增)**

```java
@Data
public class CertificateStatistics {
    /**
     * 已完成的志愿活动数量
     */
    private Long completedActivityCount;
    
    /**
     * 总志愿服务时长（小时）
     */
    private Double totalServiceHours;
    
    /**
     * 最早服务日期
     */
    private LocalDate earliestServiceDate;
    
    /**
     * 最近服务日期
     */
    private LocalDate latestServiceDate;
}
```


## Error Handling

### 业务异常处理

**异常类型及处理策略：**

1. **资格不足异常**
   - 场景：学生没有完成任何志愿服务
   - 错误码：400 Bad Request
   - 错误信息：`您还没有完成任何志愿服务，暂时无法申请证书`
   - 处理：前端显示友好提示，引导用户参加志愿活动

2. **重复申请异常**
   - 场景：学生有待审核的证书申请
   - 错误码：400 Bad Request
   - 错误信息：`您有待审核的证书申请，请等待审核完成`
   - 处理：前端禁用申请按钮，显示待审核状态

3. **证书未审核异常**
   - 场景：尝试预览/下载未通过审核的证书
   - 错误码：403 Forbidden
   - 错误信息：`证书尚未通过审核，无法预览/下载`
   - 处理：前端只在审核通过后显示预览/下载按钮

4. **PDF生成失败异常**
   - 场景：PDF模板加载失败或字段填充失败
   - 错误码：500 Internal Server Error
   - 错误信息：`证书生成失败，请稍后重试`
   - 处理：记录详细日志，前端提示用户稍后重试

5. **权限不足异常**
   - 场景：用户尝试访问他人的证书
   - 错误码：403 Forbidden
   - 错误信息：`无权访问该证书`
   - 处理：Controller层验证用户权限

### 异常处理实现

```java
// CertificateServiceImpl中的权限验证（使用AssertUtils）
private void validateCertificateAccess(Long certificateId, Long userId, UserRole role) {
    Certificate certificate = certificateMapper.selectById(certificateId);
    AssertUtils.notNull(certificate, "证书不存在");
    
    // 管理员可以访问所有证书
    if (role == UserRole.ADMIN) {
        return;
    }
    
    // 普通用户只能访问自己的证书
    AssertUtils.isTrue(
        certificate.getUserId().equals(userId),
        "无权访问该证书"
    );
}
```

### 日志记录策略

```java
// 关键操作日志
log.info("用户 {} 申请志愿证书", userId);
log.info("管理员 {} 审核证书 {}，结果：{}", approverId, certificateId, approved);
log.info("用户 {} 下载证书 {}", userId, certificateId);

// 错误日志
log.error("生成PDF证书失败，证书ID: {}", certificateId, e);
log.error("加载PDF模板失败", e);
```


## Testing Strategy

### 单元测试

**CertificateServiceTest**

测试用例：
1. `testValidateEligibility_WithCompletedService_ReturnsTrue()`
   - 验证有已完成服务的学生资格验证通过

2. `testValidateEligibility_WithoutCompletedService_ReturnsFalse()`
   - 验证没有已完成服务的学生资格验证失败

3. `testApplyCertificate_WithoutEligibility_ThrowsException()`
   - 验证不符合资格的申请抛出异常

4. `testApplyCertificate_WithPendingApplication_ThrowsException()`
   - 验证重复申请抛出异常

5. `testGenerateCertificatePdf_WithApprovedCertificate_ReturnsBytes()`
   - 验证已审核证书可以生成PDF

6. `testGenerateCertificatePdf_WithPendingCertificate_ThrowsException()`
   - 验证未审核证书无法生成PDF

**PdfServiceTest**

测试用例：
1. `testGenerateCertificatePdf_WithValidData_ReturnsPdfBytes()`
   - 验证PDF生成功能正常

2. `testGenerateCertificatePdf_WithMissingTemplate_ThrowsException()`
   - 验证模板缺失时的异常处理

3. `testFillFormField_WithValidField_FillsSuccessfully()`
   - 验证表单字段填充功能

### 集成测试

**CertificateControllerTest**

测试用例：
1. `testApplyCertificate_AsStudent_Success()`
   - 测试学生申请证书接口

2. `testPreviewCertificate_WithApprovedCertificate_ReturnsPdf()`
   - 测试证书预览接口

3. `testDownloadCertificate_WithApprovedCertificate_ReturnsPdf()`
   - 测试证书下载接口

4. `testPreviewCertificate_WithPendingCertificate_ReturnsForbidden()`
   - 测试未审核证书预览权限

5. `testApproveCertificate_AsAdmin_Success()`
   - 测试管理员审核接口

### 手动测试清单

**PDF模板测试：**
- [ ] 验证cert_template.pdf包含正确的表单字段（name, username, serial, issueDate）
- [ ] 验证字段名称与代码中的字段名完全匹配
- [ ] 验证PDF模板可以正常加载
- [ ] 验证填充后的PDF格式正确，字段位置准确

**功能测试：**
- [ ] 学生没有完成志愿服务时，申请证书失败
- [ ] 学生完成志愿服务后，可以成功申请证书
- [ ] 学生有待审核申请时，无法重复申请
- [ ] 管理员可以查看待审核证书列表
- [ ] 管理员审核通过后，证书状态更新为APPROVED
- [ ] 管理员拒绝时，必须填写拒绝原因
- [ ] 学生只能预览/下载已审核通过的证书
- [ ] 预览功能在新窗口打开PDF
- [ ] 下载功能保存PDF文件，文件名格式正确
- [ ] PDF内容包含正确的姓名、学号、证书编号、发证日期

**权限测试：**
- [ ] 学生只能查看自己的证书
- [ ] 学生无法访问他人的证书
- [ ] 管理员可以查看所有证书
- [ ] 未登录用户无法访问证书接口


## Implementation Details

### PDF模板准备

**模板要求：**
1. PDF文件位置：`CVS-backend/src/main/resources/assets/cert_template.pdf`
2. 必须包含AcroForm表单字段：
   - `name`: 真实姓名
   - `username`: 学号
   - `serial`: 证书编号
   - `issueDate`: 发证日期

**创建PDF模板步骤：**
1. 使用Adobe Acrobat或其他PDF编辑工具打开现有模板
2. 添加文本字段（Text Field）到需要填充的位置
3. 设置字段名称为：name, username, serial, issueDate
4. 设置字段属性（字体、大小、对齐方式）
5. 保存为PDF表单文件

**验证模板：**
```bash
# 使用PDFBox命令行工具验证表单字段
java -jar pdfbox-app-2.0.29.jar PDFDebugger cert_template.pdf
# 查看 Interactive -> AcroForm -> Fields
```

### 配置文件更新

**application.yml**

```yaml
# 证书配置
certificate:
  template:
    path: classpath:assets/cert_template.pdf
  number:
    prefix: CERT
    format: "yyyyMMdd"
```

### 依赖注入关系

```
CertificateController
    ├── CertificateService (已存在)
    │   ├── CertificateMapper
    │   ├── UserMapper
    │   ├── SignupMapper
    │   └── PdfService (新增)
    └── UserPrincipal (Spring Security)

PdfService (新增)
    └── ResourceLoader (Spring)
```

### API接口规范

**预览证书**
```
GET /api/certificates/{id}/preview

Request Headers:
  Authorization: Bearer {token}

Response:
  Content-Type: application/pdf
  Body: PDF字节流

Status Codes:
  200 OK - 成功返回PDF
  403 Forbidden - 证书未审核或无权访问
  404 Not Found - 证书不存在
```

**下载证书**
```
GET /api/certificates/{id}/download

Request Headers:
  Authorization: Bearer {token}

Response:
  Content-Type: application/pdf
  Content-Disposition: attachment; filename="志愿服务证书-{学号}.pdf"
  Body: PDF字节流

Status Codes:
  200 OK - 成功返回PDF
  403 Forbidden - 证书未审核或无权访问
  404 Not Found - 证书不存在
```

### 性能考虑

1. **PDF生成缓存**
   - 考虑对已生成的PDF进行缓存（可选）
   - 证书内容不变时，避免重复生成
   - 使用Redis缓存，key: `cert:pdf:{certificateId}`

2. **资源管理**
   - 确保PDDocument正确关闭，避免内存泄漏
   - 使用try-with-resources语句
   - 限制并发PDF生成请求数量

3. **模板加载优化**
   - 首次加载时缓存模板到内存
   - 避免每次生成都从磁盘读取

### 安全考虑

1. **防止路径遍历攻击**
   - 模板路径使用固定配置，不接受用户输入

2. **防止PDF注入**
   - 对填充的字段值进行验证和转义
   - 限制字段长度

3. **访问控制**
   - 严格验证用户只能访问自己的证书
   - 管理员权限单独验证

4. **审计日志**
   - 记录所有证书下载操作
   - 记录审核操作和审核人


## Deployment Considerations

### 环境配置

**开发环境 (application-dev.yml)**
```yaml
certificate:
  template:
    path: classpath:assets/cert_template.pdf
  number:
    prefix: CERT-DEV
```

**生产环境 (application-prod.yml)**
```yaml
certificate:
  template:
    path: classpath:assets/cert_template.pdf
  number:
    prefix: CERT
```

### 资源文件部署

确保PDF模板文件正确打包到JAR中：
```xml
<!-- pom.xml -->
<build>
    <resources>
        <resource>
            <directory>src/main/resources</directory>
            <includes>
                <include>**/*.pdf</include>
                <include>**/*.yml</include>
                <include>**/*.xml</include>
            </includes>
        </resource>
    </resources>
</build>
```

### 依赖版本管理

```xml
<properties>
    <pdfbox.version>2.0.29</pdfbox.version>
</properties>

<dependency>
    <groupId>org.apache.pdfbox</groupId>
    <artifactId>pdfbox</artifactId>
    <version>${pdfbox.version}</version>
</dependency>
```

### 监控指标

建议监控的指标：
- PDF生成成功率
- PDF生成平均耗时
- 证书申请数量（按天/周/月）
- 审核通过率
- 下载次数统计

## Migration Plan

### 数据迁移

无需数据迁移，cert_twb表已存在且结构满足需求。

### 功能启用步骤

1. **后端部署**
   - 添加PDFBox依赖
   - 部署PdfService实现
   - 扩展CertificateService和Controller
   - 确保cert_template.pdf在resources/assets目录

2. **前端部署**
   - 添加证书管理页面（学生端）
   - 添加证书审核页面（管理员端）
   - 更新API接口调用

3. **测试验证**
   - 验证PDF模板字段正确
   - 测试完整的申请-审核-下载流程
   - 验证权限控制

4. **灰度发布**
   - 先对部分用户开放功能
   - 收集反馈并优化
   - 全量发布

### 回滚方案

如果出现问题，可以：
1. 禁用新增的预览/下载接口
2. 保留原有的申请和审核功能
3. 修复问题后重新启用

## Future Enhancements

### 短期优化（1-2个月）

1. **批量下载**
   - 管理员可以批量导出证书
   - 生成ZIP压缩包

2. **证书模板管理**
   - 支持多种证书模板
   - 管理员可以上传和切换模板

3. **电子签章**
   - 在PDF上添加电子签章
   - 增强证书的权威性

### 长期规划（3-6个月）

1. **区块链存证**
   - 将证书哈希上链
   - 提供证书真伪验证

2. **二维码验证**
   - 在证书上生成二维码
   - 扫码验证证书真实性

3. **证书分享**
   - 生成证书分享链接
   - 支持社交媒体分享

4. **多语言证书**
   - 支持中英文双语证书
   - 国际化支持

## References

- Apache PDFBox官方文档: https://pdfbox.apache.org/
- PDF表单字段规范: https://www.adobe.com/devnet/acrobat/pdfs/pdf_reference_1-7.pdf
- Spring Boot文件处理: https://docs.spring.io/spring-boot/docs/current/reference/html/features.html#features.developing-web-applications.spring-mvc.static-content
- MyBatis-Plus条件构造器: https://baomidou.com/pages/10c804/

---

**设计文档版本**: v1.0  
**最后更新**: 2024-10-24  
**作者**: CVS Team


### 资格验证策略扩展示例

**未来可能的其他策略实现：**

```java
/**
 * 基于活动数量的资格验证策略
 */
@Component
@ConditionalOnProperty(name = "certificate.eligibility.strategy", havingValue = "activity-count")
public class ActivityCountEligibilityStrategy implements CertificateEligibilityStrategy {
    
    @Value("${certificate.eligibility.min-activities:3}")
    private int minRequiredActivities;
    
    @Override
    public EligibilityResult validate(Long userId) {
        // 验证逻辑：至少参加N个活动
        // ...
    }
}

/**
 * 组合策略：同时满足多个条件
 */
@Component
@ConditionalOnProperty(name = "certificate.eligibility.strategy", havingValue = "combined")
public class CombinedEligibilityStrategy implements CertificateEligibilityStrategy {
    
    @Autowired
    private List<CertificateEligibilityStrategy> strategies;
    
    @Override
    public EligibilityResult validate(Long userId) {
        // 所有策略都必须通过
        for (CertificateEligibilityStrategy strategy : strategies) {
            EligibilityResult result = strategy.validate(userId);
            if (!result.isEligible()) {
                return result;
            }
        }
        return EligibilityResult.success();
    }
}
```

**配置文件切换策略：**

```yaml
# application.yml
certificate:
  eligibility:
    # 策略类型：service-hours（默认）、activity-count、combined
    strategy: service-hours
    # 最小服务小时数
    min-hours: 2
    # 最小活动数量（当strategy=activity-count时生效）
    min-activities: 3
```

这样设计的好处：
1. 符合开闭原则：新增策略不需要修改现有代码
2. 配置灵活：通过配置文件切换策略
3. 易于测试：每个策略独立测试
4. 便于维护：策略逻辑清晰分离
