### 后台：
1. 设计数据库表结构：  
用户（账号、密码、昵称、学号……）  
课表（学号、时间、课程名、地点、考试日期……）  
待办事项（时间、地点、事务具体描述……）  
节日（时间……）  
……
2. 用MySQL搭建数据库，并合理处理各表间的关系
3. 根据用户提供的学号和密码，模拟登陆教务系统（不保存密码），抓取课表和期末考试时间并导入数据库，并提供给andriod客户端获取该数据json格式的API
4. 解析android客户端提供的json格式的用户注册信息，并存入数据库，实现登录时判断用户名和密码是否正确
### 安卓：
1. 设计APP主界面并写好XML文件
2. 实现用户注册界面，获取用户个人信息提交给后台
  
ps：先实现不需要与客户端（服务器端）建立连接的部分