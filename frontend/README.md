# Build
mvn clean package && docker build -t de.incentergy/base-frontend .

# RUN

docker rm -f base-frontend || true && docker run -d -p 8080:8080 -p 4848:4848 --name base-frontend de.incentergy/base-frontend 