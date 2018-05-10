# spring-boot整合阿里druid
---
> ***前言1：druid简介***
druid是阿里巴巴开源、java语言的数据库连接池，除连接池功能外，druid还提供了强大的监控和扩展功能。官方号称为`java语言最好的数据库连接池`，其github地址：https://github.com/alibaba/druid。

> ***前言2：本文内容***
这篇文章分享在spring-boot项目中整合druid连接池，并实现监控功能的方法和步骤，主要包含以下四点：
> * spring-boot整合druid；
> * 使用druid加密数据库密码
> * 使用日志记录sql实际执行的sql
> * spring、session监控配置
 
 
## 一、创建spring-boot工程
1、工程使用开发工具为IDEA，jdk1.8，spring-boot版本为2.0.1.RELEASE；
2、新建spring-boot工程，工程type选择Gradle Project，选择导入的依赖如下：
![new project dependencies][1]
3、创建完成后，build.gradle内容如下：
```
buildscript {
	ext {
		springBootVersion = '2.0.1.RELEASE'
	}
	repositories {
		mavenCentral()
	}
	dependencies {
		classpath("org.springframework.boot:spring-boot-gradle-plugin:${springBootVersion}")
	}
}

apply plugin: 'java'
apply plugin: 'idea'
apply plugin: 'org.springframework.boot'
apply plugin: 'io.spring.dependency-management'

group = 'louie.share'
version = '0.0.1-SNAPSHOT'
sourceCompatibility = 1.8
targetCompatibility = 1.8

repositories {
	mavenCentral()
}


dependencies {
	compile('org.springframework.boot:spring-boot-starter-data-jpa')
	compile('org.springframework.boot:spring-boot-starter-web')
	runtime('org.springframework.boot:spring-boot-devtools')
	runtime('org.postgresql:postgresql')
	testCompile('org.springframework.boot:spring-boot-starter-test')
}

```
## 二、整合druid
1、添加druid依赖，阿里巴巴为druid提供了spring-boot的自动化配置依赖，只需引入即可（在早期版本中，需要我们编码实现Configuration类）。
```
compile group: 'com.alibaba', name: 'druid-spring-boot-starter', version: '1.1.9'
```
2、数据库配置
在spring-boot的配置文件application.yml中添加数据库基本配置：
```
spring:
  datasource:
    druid:
      url: jdbc:postgresql://127.0.0.1:5432/activiti-demo
      username: louie
      password: louie1234
      driver-class-name: org.postgresql.Driver
```
> 注：以上配置中spring.datasource.druid.xxx为druid为spring-boot提供的配置属性，您也可以不适用它们，使用spring-boot原有的配置属性也是一样的。

3、启动服务，查看效果
执行Application类启动spring-boot，浏览器访问http://localhost:8080/druid/index.html查看监控效果：
![druid监控数据源][2]
截图中的数据源选项卡可以查看到目前的数据库连接池的情况，由于我们没有做相关配置，这里查看到的初始化连接大小、最小空闲连接数等信息是druid的默认值，在实际项目中就需要我们根据实际情况去修改application.yml文件了，配置的各项属性在druid的wiki中均可查阅，这里不再赘述。
> *druid提供了一份连接池的参考配置，[点击查看](https://github.com/alibaba/druid/wiki/%E9%85%8D%E7%BD%AE_DruidDataSource%E5%8F%82%E8%80%83%E9%85%8D%E7%BD%AE)。*

## 三、数据库密码加密
将数据库密码直接写在配置文件中，从安全方面考虑是具有风险的，druid的configFilter具备数据库密码加密功能。

1、密码加密
cmd进入druid包所在目录，执行以下命令，其中PASSWORD为您数据的密码：
```
java -cp druid-1.1.9.jar com.alibaba.druid.filter.config.ConfigTools PASSWORD
```
输出如下，其中password即为加密后的密码，publicKey为加密的公钥：
```
privateKey:MIIBVQIBADANBgkqhkiG9w0BAQEFAASCAT8wggE7AgEAAkEAjhd6PlxyNExMYJsPuIXnvJIVIL77q1ZE+YcZDFMaCMcK3hznsx89NrobrFXaEoHyLWuzRsD2q9/7tpv3fn+3owIDAQABAkBNS6I5OEr7/iFyUAfORjGY2BLcPGhlfUmKQB61IKPB781xWit3FtFocqKGde6iuRBjMGtDKINFD5CqUYYuIK8hAiEAxQ1r03pi+uAqSrb75pieniJuN9fljLlomE5+UGVN3ZMCIQC4mRRs9YsW0i9QjybqvzD615eIv8c9eQjJG67Ot4uHsQIhAJdJcptuv0d1i4LJciTc0AsAzDY7n5WnU9J7kScQX/PZAiEApw450uVfizaJhSEXugduXwOuah4MRBB9p+o6h27JgfECIHrHqsV57OZh9m2f28j5/Ax5O4xUDy9STvkWR/zvHhu5
publicKey:MFwwDQYJKoZIhvcNAQEBBQADSwAwSAJBAI4Xej5ccjRMTGCbD7iF57ySFSC++6tWRPmHGQxTGgjHCt4c57MfPTa6G6xV2hKB8i1rs0bA9qvf+7ab935/t6MCAwEAAQ==
password:TT0HLMKz6fPGiaih/F7r/s6zRonc9XiViRY5abqJSZTANPppXpxuMntcwuXqFEZH15v/qlE3UNnHx7pb9I3xWA==
```
2、修改application.yml
修改包括三部分：密码改为加密后的密码、开启druid的configFilter、启用加密并配置publicKey。修改后的配置为：
```
spring:
  datasource:
    druid:
      url: jdbc:postgresql://127.0.0.1:5432/activiti-demo
      username: louie
      password: TT0HLMKz6fPGiaih/F7r/s6zRonc9XiViRY5abqJSZTANPppXpxuMntcwuXqFEZH15v/qlE3UNnHx7pb9I3xWA== #加密后的密码
      driver-class-name: org.postgresql.Driver
      filter:
        config:
          enabled: true #开启configFilter
      connection-properties: config.decrypt=true;config.decrypt.key=${public_key} #启用加密，配置公钥
  jpa:
    hibernate:
      ddl-auto: none
    database: postgresql

public_key: MFwwDQYJKoZIhvcNAQEBBQADSwAwSAJBAI4Xej5ccjRMTGCbD7iF57ySFSC++6tWRPmHGQxTGgjHCt4c57MfPTa6G6xV2hKB8i1rs0bA9qvf+7ab935/t6MCAwEAAQ==
```

## 四、实现日志记录SQL
druid支持多种日志框架的日志输出，在测试和产品上线初期，为了方便我们查找错误可以将实际执行的sql通过日志记录下来，链接 [Druid中使用log4j2进行日志输出](https://github.com/alibaba/druid/wiki/Druid%E4%B8%AD%E4%BD%BF%E7%94%A8log4j2%E8%BF%9B%E8%A1%8C%E6%97%A5%E5%BF%97%E8%BE%93%E5%87%BA) 给出了详细说明和log4j2.xml的详细配置，这里不再过多说明。

## 五、spring监控
Druid提供了Spring和Jdbc的关联监控，基于spring AOP实现，可通过类型、方法名等多种方法配置拦截，其详细说明可以[点击这里查看](https://github.com/alibaba/druid/wiki/%E9%85%8D%E7%BD%AE_Druid%E5%92%8CSpring%E5%85%B3%E8%81%94%E7%9B%91%E6%8E%A7%E9%85%8D%E7%BD%AE)，以下示例为按方法名正则匹配拦截的配置：
```
spring
  datasource
    druid 
      aop-patterns: louie.share.druid.*
```
![TIM截图20180510160235.png-72.6kB][3]


  [1]: http://static.zybuluo.com/louie-001/q0nnf10enbneandxkoukyu2h/TIM%E6%88%AA%E5%9B%BE20180509231546.png
  [2]: http://static.zybuluo.com/louie-001/aiwy00fn65d758rv3287rjah/TIM%E6%88%AA%E5%9B%BE20180510122900.png
  [3]: http://static.zybuluo.com/louie-001/upldeffkphty0lgtpj2m1mh5/TIM%E6%88%AA%E5%9B%BE20180510160235.png