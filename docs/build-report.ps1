param(
  [string]$OutputPath = "docs/大学生求职系统项目文档.docx"
)

$ErrorActionPreference = "Stop"
Add-Type -AssemblyName System.IO.Compression.FileSystem
Add-Type -AssemblyName System.Drawing

$root = Resolve-Path (Join-Path $PSScriptRoot "..")
$work = "D:\tmp\campus-career-docx"
if (Test-Path $work) {
  Remove-Item -LiteralPath $work -Recurse -Force
}
New-Item -ItemType Directory -Path $work, "$work\_rels", "$work\word", "$work\word\_rels", "$work\word\media" | Out-Null

function XmlEscape([string]$value) {
  if ($null -eq $value) { return "" }
  return [System.Security.SecurityElement]::Escape($value)
}

function Paragraph([string]$text, [int]$fontSize = 24, [bool]$bold = $false, [string]$align = "left") {
  $b = if ($bold) { "<w:b/>" } else { "" }
  return "<w:p><w:pPr><w:jc w:val=`"$align`"/></w:pPr><w:r><w:rPr><w:rFonts w:ascii=`"Microsoft YaHei`" w:hAnsi=`"Microsoft YaHei`" w:eastAsia=`"Microsoft YaHei`"/><w:sz w:val=`"$fontSize`"/>$b</w:rPr><w:t>$(XmlEscape $text)</w:t></w:r></w:p>"
}

function Heading([string]$text, [int]$level) {
  $size = if ($level -eq 1) { 32 } elseif ($level -eq 2) { 28 } else { 25 }
  $style = "Heading$level"
  return "<w:p><w:pPr><w:pStyle w:val=`"$style`"/><w:spacing w:before=`"220`" w:after=`"120`"/></w:pPr><w:r><w:rPr><w:rFonts w:ascii=`"Microsoft YaHei`" w:hAnsi=`"Microsoft YaHei`" w:eastAsia=`"Microsoft YaHei`"/><w:b/><w:sz w:val=`"$size`"/></w:rPr><w:t>$(XmlEscape $text)</w:t></w:r></w:p>"
}

function Cell([string]$text, [int]$width = 2200, [bool]$bold = $false) {
  $b = if ($bold) { "<w:b/>" } else { "" }
  return "<w:tc><w:tcPr><w:tcW w:w=`"$width`" w:type=`"dxa`"/></w:tcPr><w:p><w:r><w:rPr><w:rFonts w:ascii=`"Microsoft YaHei`" w:hAnsi=`"Microsoft YaHei`" w:eastAsia=`"Microsoft YaHei`"/><w:sz w:val=`"20`"/>$b</w:rPr><w:t>$(XmlEscape $text)</w:t></w:r></w:p></w:tc>"
}

function TableXml($rows) {
  $xml = "<w:tbl><w:tblPr><w:tblStyle w:val=`"TableGrid`"/><w:tblW w:w=`"9360`" w:type=`"dxa`"/><w:tblBorders><w:top w:val=`"single`" w:sz=`"4`" w:color=`"CBD5E1`"/><w:left w:val=`"single`" w:sz=`"4`" w:color=`"CBD5E1`"/><w:bottom w:val=`"single`" w:sz=`"4`" w:color=`"CBD5E1`"/><w:right w:val=`"single`" w:sz=`"4`" w:color=`"CBD5E1`"/><w:insideH w:val=`"single`" w:sz=`"4`" w:color=`"CBD5E1`"/><w:insideV w:val=`"single`" w:sz=`"4`" w:color=`"CBD5E1`"/></w:tblBorders></w:tblPr>"
  for ($i = 0; $i -lt $rows.Count; $i++) {
    $row = $rows[$i]
    $xml += "<w:tr>"
    foreach ($value in $row) {
      $xml += Cell $value 2340 ($i -eq 0)
    }
    $xml += "</w:tr>"
  }
  return $xml + "</w:tbl>"
}

function ImageXml([string]$path, [string]$rid, [string]$caption, [int]$index) {
  $img = [System.Drawing.Image]::FromFile($path)
  try {
    $targetCx = 5486400
    $targetCy = [int64]($targetCx * $img.Height / $img.Width)
  } finally {
    $img.Dispose()
  }
  return @"
<w:p><w:r><w:drawing><wp:inline distT="0" distB="0" distL="0" distR="0"><wp:extent cx="$targetCx" cy="$targetCy"/><wp:docPr id="$index" name="$(XmlEscape $caption)"/><wp:cNvGraphicFramePr><a:graphicFrameLocks noChangeAspect="1"/></wp:cNvGraphicFramePr><a:graphic><a:graphicData uri="http://schemas.openxmlformats.org/drawingml/2006/picture"><pic:pic><pic:nvPicPr><pic:cNvPr id="$index" name="$(XmlEscape $caption)"/><pic:cNvPicPr/></pic:nvPicPr><pic:blipFill><a:blip r:embed="$rid"/><a:stretch><a:fillRect/></a:stretch></pic:blipFill><pic:spPr><a:xfrm><a:off x="0" y="0"/><a:ext cx="$targetCx" cy="$targetCy"/></a:xfrm><a:prstGeom prst="rect"><a:avLst/></a:prstGeom></pic:spPr></pic:pic></a:graphicData></a:graphic></wp:inline></w:drawing></w:r></w:p>
$(Paragraph $caption 20 $false "center")
"@
}

$body = ""
$body += Paragraph "《Web开发技术》期末大作业项目文档" 38 $true "center"
$body += Paragraph "项目名称：大学生求职系统" 26 $true "center"
$body += Paragraph "技术栈：Vue 3 + Spring Boot 3 + MySQL" 22 $false "center"

$body += Heading "1. 需求分析" 1
$body += Paragraph "本系统面向校园求职场景。学生需要集中浏览校招和实习岗位、维护个人简历、提交岗位投递并跟踪审核状态；公司人员和管理员需要发布岗位、维护岗位信息、查看投递记录并完成审核。系统以轻量化方式完成学生端和后台端的业务闭环。"
$body += Heading "1.1 目标用户" 2
$body += Paragraph "1. 学生用户：注册登录、浏览岗位、筛选岗位、维护简历、投递岗位、查看和撤回投递。"
$body += Paragraph "2. 公司人员/管理员：登录后台、发布岗位、编辑和删除岗位、审核学生投递、查看统计数据。"
$body += Heading "1.2 功能清单" 2
$body += Paragraph "基础功能包括用户注册登录退出、岗位查询、岗位详情、简历维护、岗位投递、投递记录、岗位管理、投递审核。"
$body += Paragraph "进阶功能包括关键词和城市筛选、后台统计看板、角色权限区分、响应式布局、操作反馈和错误提示。"

$body += Heading "2. 系统设计" 1
$body += Paragraph "系统采用前后端分离架构：Vue 前端页面通过 fetch 调用 REST API，Spring Boot 后端处理业务逻辑并通过 Spring Data JPA 访问 MySQL 数据库。"
$body += Paragraph "模块划分：用户模块、岗位模块、简历模块、投递模块、统计模块。"
$body += Paragraph "学生投递流程：登录 -> 浏览岗位 -> 查看任职要求 -> 填写投递留言 -> 提交投递 -> 查看投递状态。"
$body += Paragraph "后台管理流程：公司人员或管理员登录 -> 发布/维护岗位 -> 查看投递 -> 修改审核状态 -> 统计数据汇总。"

$body += Heading "3. 数据库设计" 1
$body += Paragraph "数据库名称为 campus_career，包含 users、resumes、jobs、job_applications 四张核心表。用户与简历是一对一关系，用户与投递是一对多关系，岗位与投递是一对多关系。"
$body += TableXml @(
  @("表名", "主要字段", "关系", "说明"),
  @("users", "id, username, password_hash, role, full_name, email, phone, major, graduation_year, created_at", "一对一 resumes；一对多 job_applications", "保存学生、公司人员和管理员账号"),
  @("resumes", "id, user_id, expected_city, expected_position, expected_salary, education, skills, project_experience, portfolio_url, updated_at", "多对一 users", "保存学生简历信息"),
  @("jobs", "id, title, company, city, job_type, salary_min, salary_max, education_requirement, description, requirement_text, status, created_at", "一对多 job_applications", "保存岗位信息"),
  @("job_applications", "id, user_id, job_id, status, cover_letter, applied_at", "多对一 users；多对一 jobs", "保存岗位投递和审核状态")
)

$body += Heading "4. 界面原型与运行截图" 1
$body += Paragraph "系统页面包含首页、职位介绍、我的简历、我的投递、登录注册、后台概览、岗位管理、投递管理等核心页面。页面采用响应式布局，在电脑和移动端均可浏览。"

$screens = @(
  @("home.png", "图1 首页：展示平台定位、轮播图和热门岗位"),
  @("jobs.png", "图2 职位介绍页：支持岗位筛选、任职要求查看和投递入口"),
  @("login.png", "图3 登录注册页：区分求职者、公司人员和管理员入口"),
  @("service.png", "图4 服务内容页：说明平台核心功能和使用流程"),
  @("contact.png", "图5 联系页：展示联系信息和反馈表单")
)

$rels = ""
$imageIndex = 1
foreach ($screen in $screens) {
  $source = Join-Path $root "frontend\output\screens\$($screen[0])"
  if (Test-Path $source) {
    $mediaName = "image$imageIndex.png"
    Copy-Item $source (Join-Path $work "word\media\$mediaName")
    $rid = "rId$imageIndex"
    $rels += "<Relationship Id=`"$rid`" Type=`"http://schemas.openxmlformats.org/officeDocument/2006/relationships/image`" Target=`"media/$mediaName`"/>"
    $body += ImageXml $source $rid $screen[1] $imageIndex
    $imageIndex++
  }
}

$body += Heading "5. 技术说明" 1
$body += Paragraph "前端使用 Vue 3、Vue Router、Vite 和原生 CSS。登录 token 保存在 localStorage 中，接口调用统一封装在 api.js。后端使用 Spring Boot 3、Spring Web、Spring Data JPA 和 MySQL，使用 X-Token 请求头识别会话，后台接口校验 COMPANY 或 ADMIN 角色，学生端简历和投递接口校验 JOB_SEEKER 或 STUDENT 角色。"

$body += Heading "6. 使用说明" 1
$body += Paragraph "1. 准备 MySQL，创建数据库 campus_career，账号密码默认 root / 666666。若本机配置不同，请修改 backend/src/main/resources/application.yml。"
$body += Paragraph "2. 启动后端：cd backend，然后执行 mvn spring-boot:run。后端地址为 http://localhost:8087。"
$body += Paragraph "3. 启动前端：cd frontend，执行 npm install 和 npm run dev。前端地址为 http://localhost:5173。"
$body += Paragraph "4. 演示账号：学生 student / student123；公司人员 company / company123；管理员 admin / admin123。"

$body += Heading "7. 测试与总结" 1
$body += TableXml @(
  @("测试项", "操作", "预期结果", "结论"),
  @("登录", "输入演示账号和密码", "成功进入对应前台或后台", "通过"),
  @("岗位筛选", "输入关键词或城市", "展示匹配岗位", "通过"),
  @("保存简历", "编辑简历并提交", "页面提示保存成功", "通过"),
  @("投递岗位", "选择开放岗位并提交留言", "生成投递记录", "通过"),
  @("岗位管理", "后台新增或编辑岗位", "岗位列表更新", "通过"),
  @("投递审核", "后台修改投递状态", "学生端显示新状态", "通过")
)
$body += Paragraph "项目亮点：前后端分离清晰，覆盖学生端和后台端，数据库关系完整，角色权限明确，页面支持响应式布局，符合校园求职业务场景。"
$body += Paragraph "不足与优化方向：当前未实现简历附件上传，后续可增加文件上传、面试邀约、就业趋势图表和更完整的 JWT/Spring Security 权限体系。"

$styles = @"
<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
<w:styles xmlns:w="http://schemas.openxmlformats.org/wordprocessingml/2006/main">
  <w:style w:type="paragraph" w:default="1" w:styleId="Normal"><w:name w:val="Normal"/><w:rPr><w:rFonts w:ascii="Microsoft YaHei" w:hAnsi="Microsoft YaHei" w:eastAsia="Microsoft YaHei"/><w:sz w:val="24"/></w:rPr></w:style>
  <w:style w:type="paragraph" w:styleId="Heading1"><w:name w:val="heading 1"/><w:basedOn w:val="Normal"/><w:next w:val="Normal"/><w:qFormat/><w:pPr><w:outlineLvl w:val="0"/></w:pPr><w:rPr><w:b/><w:sz w:val="32"/></w:rPr></w:style>
  <w:style w:type="paragraph" w:styleId="Heading2"><w:name w:val="heading 2"/><w:basedOn w:val="Normal"/><w:next w:val="Normal"/><w:qFormat/><w:pPr><w:outlineLvl w:val="1"/></w:pPr><w:rPr><w:b/><w:sz w:val="28"/></w:rPr></w:style>
  <w:style w:type="table" w:styleId="TableGrid"><w:name w:val="Table Grid"/><w:tblPr><w:tblBorders><w:top w:val="single" w:sz="4" w:color="CBD5E1"/><w:left w:val="single" w:sz="4" w:color="CBD5E1"/><w:bottom w:val="single" w:sz="4" w:color="CBD5E1"/><w:right w:val="single" w:sz="4" w:color="CBD5E1"/><w:insideH w:val="single" w:sz="4" w:color="CBD5E1"/><w:insideV w:val="single" w:sz="4" w:color="CBD5E1"/></w:tblBorders></w:tblPr></w:style>
</w:styles>
"@

$document = @"
<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
<w:document xmlns:w="http://schemas.openxmlformats.org/wordprocessingml/2006/main" xmlns:r="http://schemas.openxmlformats.org/officeDocument/2006/relationships" xmlns:wp="http://schemas.openxmlformats.org/drawingml/2006/wordprocessingDrawing" xmlns:a="http://schemas.openxmlformats.org/drawingml/2006/main" xmlns:pic="http://schemas.openxmlformats.org/drawingml/2006/picture">
  <w:body>
    $body
    <w:sectPr><w:pgSz w:w="11906" w:h="16838"/><w:pgMar w:top="1440" w:right="1440" w:bottom="1440" w:left="1440" w:header="720" w:footer="720" w:gutter="0"/></w:sectPr>
  </w:body>
</w:document>
"@

$contentTypes = @"
<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
<Types xmlns="http://schemas.openxmlformats.org/package/2006/content-types">
  <Default Extension="rels" ContentType="application/vnd.openxmlformats-package.relationships+xml"/>
  <Default Extension="xml" ContentType="application/xml"/>
  <Default Extension="png" ContentType="image/png"/>
  <Override PartName="/word/document.xml" ContentType="application/vnd.openxmlformats-officedocument.wordprocessingml.document.main+xml"/>
  <Override PartName="/word/styles.xml" ContentType="application/vnd.openxmlformats-officedocument.wordprocessingml.styles+xml"/>
</Types>
"@

$rootRels = @"
<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
<Relationships xmlns="http://schemas.openxmlformats.org/package/2006/relationships"><Relationship Id="rId1" Type="http://schemas.openxmlformats.org/officeDocument/2006/relationships/officeDocument" Target="word/document.xml"/></Relationships>
"@

$docRels = @"
<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
<Relationships xmlns="http://schemas.openxmlformats.org/package/2006/relationships"><Relationship Id="rIdStyles" Type="http://schemas.openxmlformats.org/officeDocument/2006/relationships/styles" Target="styles.xml"/>$rels</Relationships>
"@

[System.IO.File]::WriteAllText((Join-Path $work "[Content_Types].xml"), $contentTypes, [System.Text.UTF8Encoding]::new($false))
[System.IO.File]::WriteAllText((Join-Path $work "_rels\.rels"), $rootRels, [System.Text.UTF8Encoding]::new($false))
[System.IO.File]::WriteAllText((Join-Path $work "word\document.xml"), $document, [System.Text.UTF8Encoding]::new($false))
[System.IO.File]::WriteAllText((Join-Path $work "word\styles.xml"), $styles, [System.Text.UTF8Encoding]::new($false))
[System.IO.File]::WriteAllText((Join-Path $work "word\_rels\document.xml.rels"), $docRels, [System.Text.UTF8Encoding]::new($false))

$output = Join-Path $root $OutputPath
if (Test-Path $output) {
  Remove-Item -LiteralPath $output -Force
}
[System.IO.Compression.ZipFile]::CreateFromDirectory($work, $output)
Write-Host "Created $output"
