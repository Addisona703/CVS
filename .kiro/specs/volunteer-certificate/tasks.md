·# Implementation Plan - 志愿证书申请与生成功能

## 任务清单

- [x] 1. 添加PDF生成依赖和配置






  - 在pom.xml中添加Apache PDFBox依赖（版本2.0.29）
  - 在application.yml中添加证书配置（模板路径、最小服务小时数）
  - 确保PDF模板文件cert_template.pdf位于src/main/resources/assets目录
  - _Requirements: 5.1, 5.2, 5.3, 5.4, 5.5_

- [x] 2. 实现资格验证策略模块





  - [x] 2.1 创建资格验证策略接口和结果类


    - 创建CertificateEligibilityStrategy接口
    - 创建EligibilityResult类，包含eligible、message、details字段
    - _Requirements: 7.1, 7.2, 7.3_
  
  - [x] 2.2 实现基于服务小时数的验证策略


    - 创建ServiceHoursEligibilityStrategy类
    - 实现validate方法：查询已完成的志愿服务（sign_in_time和sign_out_time都不为空）
    - 计算累计服务小时数（Duration.between计算时间差）
    - 与配置的最小小时数比较，返回验证结果
    - _Requirements: 7.1, 7.2, 7.3, 7.4_

- [x] 3. 实现PDF生成服务






  - [x] 3.1 创建PdfService接口和实现类

    - 创建PdfService接口，定义generateCertificatePdf方法
    - 创建PdfServiceImpl实现类
    - 注入模板路径配置
    - _Requirements: 5.1, 5.2, 5.3, 5.4, 5.5_
  
  - [x] 3.2 实现PDF模板填充逻辑


    - 使用PDDocument加载PDF模板（ClassPathResource）
    - 获取AcroForm表单对象
    - 填充name、username、serial、issueDate四个字段
    - 调用flatten()方法设置表单为只读
    - 将PDF保存为字节数组并返回
    - 使用try-with-resources确保资源正确关闭
    - 异常处理使用AssertUtils.throwException
    - _Requirements: 5.1, 5.2, 5.3, 5.4, 5.5_

- [x] 4. 扩展CertificateService






  - [x] 4.1 修改申请证书方法

    - 在applyCertificate方法开始处注入CertificateEligibilityStrategy
    - 调用eligibilityStrategy.validate(userId)验证资格
    - 使用AssertUtils.isTrue验证结果，不符合则抛出异常
    - 使用AssertUtils.isTrue检查是否有待审核申请


    - _Requirements: 1.1, 1.2, 7.1, 7.2, 7.3, 7.4, 7.5_
  
  - [x] 4.2 实现PDF生成方法

    - 实现generateCertificatePdf方法
    - 使用AssertUtils.notNull验证证书存在
    - 使用AssertUtils.isTrue验证证书状态为APPROVED
    - 获取用户信息并准备PDF数据（name, username, serial, issueDate）
    - 调用PdfService生成PDF字节数组
    - _Requirements: 3.1, 3.2, 3.3, 3.4, 3.5, 4.1, 4.2, 4.3, 4.4, 4.5, 5.1, 5.2, 5.3, 5.4, 5.5_
  
  - [x] 4.3 实现权限验证方法


    - 创建validateCertificateAccess私有方法
    - 使用AssertUtils.notNull验证证书存在
    - 管理员可访问所有证书
    - 使用AssertUtils.isTrue验证普通用户只能访问自己的证书
    - _Requirements: 3.1, 3.2, 3.3, 3.4, 3.5, 4.1, 4.2, 4.3, 4.4_


- [x] 5. 扩展CertificateController





  - [x] 5.1 添加预览证书接口


    - 创建previewCertificate方法，路径为GET /api/certificates/{id}/preview
    - 添加@PreAuthorize注解限制STUDENT和ADMIN角色
    - 调用validateCertificateAccess验证权限
    - 调用generateCertificatePdf生成PDF字节数组
    - 设置响应头Content-Type为application/pdf
    - 返回ResponseEntity<byte[]>
    - _Requirements: 3.1, 3.2, 3.3, 3.4, 3.5_
  
  - [x] 5.2 添加下载证书接口

    - 创建downloadCertificate方法，路径为GET /api/certificates/{id}/download
    - 添加@PreAuthorize注解限制STUDENT和ADMIN角色
    - 调用validateCertificateAccess验证权限
    - 调用generateCertificatePdf生成PDF字节数组
    - 设置响应头Content-Type为application/pdf
    - 设置Content-Disposition为attachment，文件名为"志愿服务证书-{学号}.pdf"
    - 返回ResponseEntity<byte[]>
    - _Requirements: 4.1, 4.2, 4.3, 4.4, 4.5_

- [x] 6. 前端实现 - 学生证书管理页面


  - [x] 6.1 创建证书API服务


    - 在CVS-frontend/src/api/目录创建certificate.js
    - 实现preview方法（responseType: 'blob'）
    - 实现download方法（responseType: 'blob'）
    - 复用现有的apply、getMyCertificates、approve、getPendingCertificates方法
    - _Requirements: 1.1, 1.2, 1.3, 1.4, 1.5, 3.1, 3.2, 3.3, 3.4, 3.5, 4.1, 4.2, 4.3, 4.4, 4.5_
  
  - [x] 6.2 创建学生证书管理页面


    - 在CVS-frontend/src/views/student/目录创建Certificate.vue
    - 显示证书列表（状态、申请时间、证书编号、拒绝原因）
    - 添加"申请证书"按钮
    - 实现applyCertificate方法，调用API申请证书
    - 对于APPROVED状态的证书，显示"预览"和"下载"按钮
    - 对于REJECTED状态的证书，显示拒绝原因
    - _Requirements: 1.1, 1.2, 1.3, 1.4, 1.5, 6.1, 6.2, 6.3, 6.4, 6.5_
  

  - [x] 6.3 实现证书预览功能
    - 实现previewCertificate方法
    - 调用certificateApi.preview获取PDF Blob
    - 使用window.URL.createObjectURL创建URL
    - 使用window.open在新窗口打开PDF
    - 添加错误处理和加载提示
    - _Requirements: 3.1, 3.2, 3.3, 3.4, 3.5_
  

  - [x] 6.4 实现证书下载功能
    - 实现downloadCertificate方法
    - 调用certificateApi.download获取PDF Blob
    - 创建临时a标签触发下载
    - 设置文件名为"志愿服务证书-{学号}.pdf"
    - 下载后清理URL对象
    - 添加错误处理和加载提示
    - _Requirements: 4.1, 4.2, 4.3, 4.4, 4.5_
  
  - [x] 6.5 添加路由配置



    - 在router/index.js中添加学生证书管理页面路由
    - 路径：/student/certificates
    - 添加到学生布局的导航菜单
    - _Requirements: 6.1, 6.2, 6.3, 6.4, 6.5_

- [x] 7. 前端实现 - 管理员证书审核页面





  - [x] 7.1 创建管理员证书审核页面


    - 在CVS-frontend/src/views/admin/目录创建CertificateReview.vue
    - 显示待审核证书列表（申请人、申请时间、服务统计）
    - 每条记录显示"通过"和"拒绝"按钮
    - _Requirements: 2.1, 2.2, 2.3, 2.4, 2.5_
  
  - [x] 7.2 实现审核通过功能

    - 实现approveCertificate方法
    - 调用certificateApi.approve，传递id和approved=true
    - 成功后刷新列表并显示成功提示
    - _Requirements: 2.1, 2.2, 2.3_
  
  - [x] 7.3 实现审核拒绝功能

    - 实现rejectCertificate方法
    - 弹出对话框要求输入拒绝原因
    - 调用certificateApi.approve，传递id、approved=false和rejectReason
    - 成功后刷新列表并显示成功提示
    - _Requirements: 2.1, 2.4, 2.5_
  
  - [x] 7.4 添加路由配置


    - 在router/index.js中添加管理员证书审核页面路由
    - 路径：/admin/certificate-review
    - 添加到管理员布局的导航菜单
    - _Requirements: 2.1, 2.2, 2.3, 2.4, 2.5_

- [ ] 8. 集成测试和验证
  - [ ] 8.1 验证PDF模板
    - 确认cert_template.pdf包含name、username、serial、issueDate四个表单字段
    - 使用PDF编辑工具检查字段名称是否正确
    - 测试PDF模板可以正常加载
    - _Requirements: 5.1, 5.2, 5.3, 5.4, 5.5_
  
  - [ ] 8.2 测试完整流程
    - 测试学生没有完成志愿服务时申请失败（显示累计时长不足）
    - 测试学生完成志愿服务后申请成功
    - 测试重复申请被阻止
    - 测试管理员审核通过流程
    - 测试管理员审核拒绝流程（需填写原因）
    - 测试学生预览已审核证书
    - 测试学生下载已审核证书
    - 验证PDF内容正确（姓名、学号、证书编号、发证日期）
    - _Requirements: 1.1, 1.2, 1.3, 1.4, 1.5, 2.1, 2.2, 2.3, 2.4, 2.5, 3.1, 3.2, 3.3, 3.4, 3.5, 4.1, 4.2, 4.3, 4.4, 4.5, 5.1, 5.2, 5.3, 5.4, 5.5, 6.1, 6.2, 6.3, 6.4, 6.5, 7.1, 7.2, 7.3, 7.4, 7.5_
  
  - [ ] 8.3 测试权限控制
    - 测试学生只能查看自己的证书
    - 测试学生无法访问他人证书
    - 测试管理员可以查看所有证书
    - 测试未审核证书无法预览和下载
    - _Requirements: 3.1, 3.2, 4.1, 4.2_
