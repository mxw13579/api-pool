# =================================================================
# STAGE 1: Build the Vue.js Frontend
# =================================================================
FROM node:20-alpine AS frontend-builder

# 设置前端代码的工作目录
WORKDIR /app/web

# 拷贝 package.json 和 package-lock.json (利用Docker缓存)
COPY web/package*.json ./

# 安装前端依赖
RUN npm install

# 拷贝所有前端代码
COPY web/ ./

# 执行构建命令
RUN npm run build

# =================================================================
# STAGE 2: Build the Spring Boot Backend
# =================================================================
FROM maven:3.9-eclipse-temurin-17 AS backend-builder

# 设置后端代码的工作目录
WORKDIR /app

# 拷贝 pom.xml (利用Docker缓存)
COPY pom.xml ./

# 拷贝后端Java源代码
COPY src ./src

# 关键步骤：从上一个阶段(frontend-builder)拷贝构建好的前端静态文件
# 到Spring Boot的静态资源目录 (src/main/resources/static)
COPY --from=frontend-builder /app/web/dist ./src/main/resources/static/

# 执行Maven打包命令，跳过测试以加快构建速度
RUN mvn clean package -DskipTests

# =================================================================
# STAGE 3: Create the Final Production Image
# =================================================================
# 使用一个非常精简的Java运行时环境
FROM eclipse-temurin:17-jre-alpine

# 设置最终应用的工作目录
WORKDIR /app

# 从上一个阶段(backend-builder)拷贝打包好的jar文件
# 并重命名为 app.jar
COPY --from=backend-builder /app/target/*.jar app.jar

# 声明应用将监听的端口
EXPOSE 8080

# 容器启动时执行的命令
ENTRYPOINT ["java", "-jar", "app.jar"]
