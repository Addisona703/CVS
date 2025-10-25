# Requirements Document

## Introduction

本文档定义了志愿证书申请与生成功能的需求。该功能允许完成志愿服务的学生申请志愿证书，系统自动验证资格并生成包含个人信息的PDF证书，支持在线预览和下载。

## Glossary

- **Certificate System**: 志愿证书管理系统，负责证书申请、验证、生成和管理的完整功能模块
- **Student User**: 学生用户，系统中角色为STUDENT的用户，可申请志愿证书
- **Admin User**: 管理员用户，系统中角色为ADMIN的用户，负责审核证书申请
- **Completed Service**: 已完成的志愿服务，指报名状态为已完成（签到和签退都已完成）的志愿活动
- **Certificate Template**: 证书模板，位于系统资源目录的PDF模板文件，包含需要填充的字段占位符
- **Certificate Number**: 证书编号，系统自动生成的唯一标识符，格式为"CERT-YYYYMMDD-序列号"
- **PDF Generator**: PDF生成器，负责将证书模板填充数据并生成最终PDF文件的组件
- **Service Record**: 服务记录，记录学生参与志愿活动的完整信息，包括活动详情和服务时长
- **Certificate Status**: 证书状态，包括PENDING（待审核）、APPROVED（已通过）、REJECTED（已拒绝）

## Requirements

### Requirement 1

**User Story:** 作为学生用户，我希望能够申请志愿证书，以便获得参与志愿服务的官方证明

#### Acceptance Criteria

1. WHEN Student User submits a certificate application, THE Certificate System SHALL verify that the Student User has at least one Completed Service
2. IF Student User has no Completed Service, THEN THE Certificate System SHALL reject the application with error message "您还没有完成任何志愿服务，暂时无法申请证书"
3. WHEN Student User has valid Completed Service, THE Certificate System SHALL create a certificate record with status PENDING
4. WHEN certificate record is created, THE Certificate System SHALL generate a unique Certificate Number in format "CERT-YYYYMMDD-{sequence}"
5. WHEN certificate is created, THE Certificate System SHALL record the application timestamp and Student User information

### Requirement 2

**User Story:** 作为管理员用户，我希望能够审核学生的证书申请，以便确保证书发放的合规性

#### Acceptance Criteria

1. WHEN Admin User requests pending certificate list, THE Certificate System SHALL retrieve all certificate records with status PENDING
2. WHEN Admin User approves a certificate, THE Certificate System SHALL update the certificate status to APPROVED
3. WHEN certificate is approved, THE Certificate System SHALL record the approver ID and approval timestamp
4. WHEN Admin User rejects a certificate, THE Certificate System SHALL update the certificate status to REJECTED
5. WHEN certificate is rejected, THE Certificate System SHALL require and store the rejection reason

### Requirement 3

**User Story:** 作为学生用户，我希望能够预览我的志愿证书，以便在下载前确认证书内容正确

#### Acceptance Criteria

1. WHEN Student User requests certificate preview, THE Certificate System SHALL verify that the certificate status is APPROVED
2. IF certificate status is not APPROVED, THEN THE Certificate System SHALL return error message "证书尚未通过审核，无法预览"
3. WHEN certificate is APPROVED, THE Certificate System SHALL load the Certificate Template from system resources
4. WHEN Certificate Template is loaded, THE PDF Generator SHALL fill the template with Student User name, username, Certificate Number, and issue date
5. WHEN PDF is generated, THE Certificate System SHALL return the PDF content as byte stream with content-type "application/pdf"

### Requirement 4

**User Story:** 作为学生用户，我希望能够下载我的志愿证书PDF文件，以便保存和使用

#### Acceptance Criteria

1. WHEN Student User requests certificate download, THE Certificate System SHALL verify that the certificate status is APPROVED
2. IF certificate status is not APPROVED, THEN THE Certificate System SHALL return error message "证书尚未通过审核，无法下载"
3. WHEN certificate is APPROVED, THE PDF Generator SHALL generate the certificate PDF with all required fields filled
4. WHEN PDF is generated, THE Certificate System SHALL return the PDF with filename "志愿服务证书-{学号}.pdf"
5. WHEN PDF is returned, THE Certificate System SHALL set Content-Disposition header to "attachment" to trigger download

### Requirement 5

**User Story:** 作为系统，我需要确保证书数据的准确性和完整性，以便生成有效的志愿证书

#### Acceptance Criteria

1. WHEN filling Certificate Template, THE PDF Generator SHALL replace "name" field with Student User real name from user record
2. WHEN filling Certificate Template, THE PDF Generator SHALL replace "username" field with Student User student ID from user record
3. WHEN filling Certificate Template, THE PDF Generator SHALL replace "serial" field with the generated Certificate Number
4. WHEN filling Certificate Template, THE PDF Generator SHALL replace "issueDate" field with approval date in format "YYYY年MM月DD日"
5. WHEN all fields are filled, THE PDF Generator SHALL ensure the output PDF maintains the original template layout and formatting

### Requirement 6

**User Story:** 作为学生用户，我希望能够查看我的证书申请记录，以便了解证书状态和历史

#### Acceptance Criteria

1. WHEN Student User requests certificate list, THE Certificate System SHALL retrieve all certificate records for the Student User
2. WHEN certificate records exist, THE Certificate System SHALL return records with certificate number, application date, status, and rejection reason if applicable
3. WHEN displaying certificate list, THE Certificate System SHALL show the most recent certificate first
4. WHEN Student User has no certificates, THE Certificate System SHALL return empty list with message "暂无证书记录"
5. WHEN certificate status is APPROVED, THE Certificate System SHALL display download and preview action buttons

### Requirement 7

**User Story:** 作为系统，我需要验证学生的志愿服务资格，以便确保只有符合条件的学生能够获得证书

#### Acceptance Criteria

1. WHEN verifying eligibility, THE Certificate System SHALL query signup_twb table for the Student User
2. WHEN querying signup_twb, THE Certificate System SHALL count only records where sign_in_time and sign_out_time are both not null
3. WHEN Completed Service count is greater than zero, THE Certificate System SHALL allow certificate application
4. WHEN Completed Service count is zero, THE Certificate System SHALL reject certificate application with error message
5. WHEN Student User has pending certificate application, THE Certificate System SHALL prevent duplicate applications until current application is processed
