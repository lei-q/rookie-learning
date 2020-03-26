FROM ascdc/jdk8
RUN mkdir /usr/local/java/
ENTRYPOINT ["java","-jar","/usr/local/java/app.jar"]

# jar包不打到镜像中 运行时 '-v' 挂载jar到镜像中/usr/local/java/目录下
# 例：
# docker build -t imagesName .
# docker run -d -p 8082:8082 --restart=always --name containerName -v ***.jar:/usr/local/java/app.jar imagesName