# 基础镜像
FROM eclipse-temurin:17-jre
# author
LABEL maintainer="wangxx"

# 挂载目录
VOLUME /home/lemnt
# 创建目录
RUN mkdir -p /home/lemnt
# 指定路径
WORKDIR /home/lemnt
# 复制jar文件到路径
COPY lemnt-open-start/target/*.jar /home/lemnt/app.jar
# 启动认证服务
ENTRYPOINT ["java","-jar","app.jar"]
