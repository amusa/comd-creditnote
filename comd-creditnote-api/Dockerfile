FROM java:8
MAINTAINER mayemij@gmail.com
EXPOSE 8085
COPY target/comd-creditnote-api-thorntail.jar /maven/
COPY lib/libsapjco3.so /usr/local/sbin/

CMD java -Djava.net.preferIPv4Stack=true  -Djava.library.path=/maven/lib/ -jar /maven/comd-creditnote-api-thorntail.jar