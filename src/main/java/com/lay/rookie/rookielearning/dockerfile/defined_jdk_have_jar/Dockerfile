#将jar打到镜像中
FROM ascdc/jdk8
COPY xxx.jar app.jar
ENTRYPOINT ["java","-jar","app.jar"]

# xxx.jar 要打到镜像中的jar包放在Dockerfile同级目录下
# docker build -t imagesName .
# docker run -d -p 8082:8082 --restart=always --name containerName imagesName