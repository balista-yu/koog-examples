FROM eclipse-temurin:21-jdk AS local
WORKDIR /app
COPY /app /app
ENV TZ=Asia/Tokyo
RUN apt-get update && \
    apt-get install -y curl gnupg && \
    curl -fsSL https://deb.nodesource.com/setup_lts.x | bash - && \
    apt-get install -y nodejs && \
    # Install Chrome dependencies and browser
    apt-get install -y \
        wget \
        gnupg && \
    # Install Google Chrome
    wget -q -O - https://dl-ssl.google.com/linux/linux_signing_key.pub | apt-key add - && \
    echo "deb [arch=amd64] http://dl.google.com/linux/chrome/deb/ stable main" >> /etc/apt/sources.list.d/google.list && \
    apt-get update && \
    apt-get install -y google-chrome-stable && \
    rm -rf /var/lib/apt/lists/*
RUN chmod +x gradlew && ./gradlew clean getDependencies -x test --stacktrace

FROM eclipse-temurin:21-jdk AS build
WORKDIR /app
RUN apt-get update && \
    apt-get install -y unzip curl && \
    curl -fsSL https://deb.nodesource.com/setup_lts.x | bash - && \
    apt-get install -y nodejs && \
    rm -rf /var/lib/apt/lists/*
COPY /app/gradlew /app/
COPY /app/gradle /app/gradle
COPY /app/*.gradle.kts /app/
RUN chmod +x gradlew && ./gradlew dependencies --daemon --parallel --build-cache
COPY /app/src /app/src
RUN chmod +x gradlew && ./gradlew build -x test --daemon --parallel --build-cache

FROM eclipse-temurin:21-jre AS production
ENV TZ=Asia/Tokyo
RUN apt-get update && \
    apt-get install -y curl && \
    curl -fsSL https://deb.nodesource.com/setup_lts.x | bash - && \
    apt-get install -y nodejs && \
    rm -rf /var/lib/apt/lists/*
RUN useradd -m -u 1001 spring
WORKDIR /app
COPY --from=build /app/build/libs/*.jar app.jar
RUN chown -R spring:spring /app
USER spring
ENTRYPOINT ["java", "-jar", "app.jar"]
EXPOSE 8080
