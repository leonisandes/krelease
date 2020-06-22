FROM openjdk:11-jdk

ENV JAR_NAME krelease-*-all.jar

COPY build/libs/$JAR_NAME krelease.jar

ENTRYPOINT ["java", "-jar", "krelease.jar"]

CMD ["--help"]
