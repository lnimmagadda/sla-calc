FROM tomcat:8-jre8-alpine

RUN apk --no-cache add curl

RUN mkdir  /root/.aws

#COPY /target/config /root/.aws/config
#COPY /target/credentials /root/.aws/credentials

# copy the WAR bundle to tomcat
#COPY /target/sla-calculator-service.war /usr/local/tomcat/webapps/sla-calculator-service.war

# command to run
#CMD ["catalina.sh", "run"]
