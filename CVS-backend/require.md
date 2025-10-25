我正在为一个高校志愿服务管理系统（CVS）开发 “二维码签到 / 签退 + 学生自评 + 老师可调” 功能。
系统是 Spring Boot 单体架构 + MyBatis-Plus，已有报名表 signup_twb，希望在原有基础上添加二维码签到签退逻辑。
请根据以下要求，生成完整的后端实现（Controller、Service、Mapper、Entity、SQL修改语句），代码风格遵循标准 Spring Boot 三层架构。
🎯 功能场景
老师可以生成签到或签退二维码（二维码内容为一个一次性 token 字符串）。
学生扫码后，前端将 token 发给后端接口完成签到或签退。
签退时，学生可提交自评（文字）和自评分（1~5 分）。
老师可在后台调整最终评分与评价，最终以老师评分为准，用于生成志愿服务记录或积分。
📋 设计要求
二维码 Token
每个二维码对应一个一次性 token（字符串，建议 UUID）。
Token 保存到 Redis（如 cvs:checkin:{token}），内容包括：
activityId
type（SIGN_IN / SIGN_OUT）
过期时间（3~5 分钟）
Token 使用一次后立即作废。
数据库修改
表名：signup_twb
新增字段：
student_rating INT 学生自评分
student_evaluation VARCHAR 学生自评文本
teacher_rating INT 老师评分
teacher_evaluation VARCHAR 老师评价
sign_in_time DATETIME 签到时间
sign_out_time DATETIME 签退时间
请生成对应的 SQL ALTER TABLE 语句。
接口设计
POST /api/checkin/token/{activityId} → 老师生成签到 token（返回 token）
POST /api/checkout/token/{activityId} → 老师生成签退 token
POST /api/checkin → 学生扫码签到（请求参数 { token }）
POST /api/checkout → 学生扫码签退（请求参数 { token, studentRating?, studentEvaluation? }）
PATCH /api/review/{signupId} → 老师修改评分与评价
逻辑要求
签到/签退前校验 token 有效性与活动对应关系。
签到时写入 sign_in_time，签退时写入 sign_out_time。
签退时允许学生提交自评和评分。
老师确认评分时，以老师评分为最终结果（可后续触发积分逻辑）。
Token 使用后要立即从 Redis 删除。
所有业务异常统一用 AssertUtils 抛出（与项目中一致）。
实现要求
Service 层继承 ServiceImpl。
可使用 StringRedisTemplate 操作 Redis。
🧾 输出内容
Signup 实体类新增字段定义。
CheckToken 对象定义（表示 Redis 存的二维码信息）。
Redis 工具类或 Service 代码（生成、验证、删除 token）。
CheckController 示例，包含上述 5 个接口。
CheckService 与实现类。
要求代码完整且可编译，不能省略关键字段与接口逻辑。