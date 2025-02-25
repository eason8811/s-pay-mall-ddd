FROM eclipse-temurin:17-jre

LABEL maintainer="eason"

ENV PARAMS=""

# 时区
ENV TZ=PRC
RUN ln -snf /usr/share/zoneinfo/$TZ /etc/localtime && echo $TZ > /etc/timezone

COPY /s-pay-mall-web-0.0.1-SNAPSHOT.jar /s-pay-mall-web-0.0.1-SNAPSHOT.jar

ENTRYPOINT ["java", "-jar", "/s-pay-mall-ddd-eason-app.jar"]